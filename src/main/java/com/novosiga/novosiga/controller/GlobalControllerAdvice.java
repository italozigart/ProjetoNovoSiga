package com.novosiga.novosiga.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.novosiga.novosiga.carrinho.Carrinho;
import com.novosiga.novosiga.security.UserDetailsImpl;
import com.novosiga.novosiga.util.UrlUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Configurações aplicadas a TODOS os controllers:
 *  - limpa espaços dos campos de texto dos formulários;
 *  - envia para todas as telas a quantidade de itens do carrinho, o usuário logado
 *    e o endereço atual (usado no menu e nos botões "voltar");
 *  - trata o erro de imagem maior que 2 MB.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private Carrinho carrinho;

    // " Maria  " vira "Maria" e campos vazios viram null (assim o @NotBlank funciona)
    @InitBinder
    public void configurarFormularios(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // Número exibido no ícone do carrinho (cabeçalho)
    @ModelAttribute("qtdCarrinho")
    public int qtdCarrinho() {
        try {
            return carrinho.getTotalUnidades();
        } catch (RuntimeException e) {
            return 0; // fora de uma sessão (ex.: página de erro)
        }
    }

    // Nome do usuário logado (cabeçalho)
    @ModelAttribute("usuarioLogado")
    public String usuarioLogado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            return null;
        }
        if (autenticacao.getPrincipal() instanceof UserDetailsImpl detalhes) {
            return detalhes.getNome();
        }
        return autenticacao.getName();
    }

    // Caminho da página atual, ex.: "/alunos/listar" (deixa o item do menu ativo)
    @ModelAttribute("uriAtual")
    public String uriAtual(HttpServletRequest request) {
        return request.getRequestURI();
    }

    // Caminho + parâmetros, ex.: "/loja?categoria=Camiseta" (volta para a mesma página após uma ação)
    @ModelAttribute("urlAtual")
    public String urlAtual(HttpServletRequest request) {
        String parametros = request.getQueryString();
        return parametros == null ? request.getRequestURI() : request.getRequestURI() + "?" + parametros;
    }

    // Upload maior que o limite do application.properties (2 MB)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String arquivoMuitoGrande(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "A imagem enviada é maior que 2 MB. Escolha um arquivo menor.");
        return UrlUtil.redirecionar(paginaDeOrigem(request), "/home");
    }

    // Página de onde o formulário foi enviado (cabeçalho Referer), somente o caminho local
    private String paginaDeOrigem(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            return null;
        }
        try {
            URI uri = URI.create(referer);
            String caminho = uri.getRawPath();
            return uri.getRawQuery() == null ? caminho : caminho + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
