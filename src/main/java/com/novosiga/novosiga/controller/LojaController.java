package com.novosiga.novosiga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.novosiga.novosiga.dto.FiltroLoja;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.service.AtleticaService;
import com.novosiga.novosiga.service.ProdutoService;

/**
 * Vitrine da loja das atléticas: busca, filtros, ordenação e detalhes do produto.
 */
@Controller
@RequestMapping("/loja")
public class LojaController {

    private static final int PRODUTOS_POR_PAGINA = 12;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private AtleticaService atleticaService;

    // /loja?q=camiseta&atletica=1&categoria=Camiseta&precoMin=10&precoMax=80&disponiveis=true&ordem=menorPreco
    @GetMapping
    public String loja(@ModelAttribute("filtro") FiltroLoja filtro, BindingResult result,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        // BindingResult: se alguém digitar um preço inválido na URL, o filtro é ignorado em vez de dar erro

        Page<Produto> resultado = produtoService.buscarNaLoja(filtro, pagina, PRODUTOS_POR_PAGINA);

        model.addAttribute("produtos", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("urlBase", filtro.getUrlBase());
        model.addAttribute("atleticas", atleticaService.findAll());
        model.addAttribute("categorias", produtoService.listarCategorias());
        model.addAttribute("atleticaSelecionada",
                filtro.getAtletica() == null ? null : atleticaService.findById(filtro.getAtletica()));

        return "loja/loja";
    }

    // Página de detalhes do produto
    @GetMapping("/produto/{id}")
    public String produto(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Produto produto = produtoService.findById(id);
        if (produto == null) {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado.");
            return "redirect:/loja";
        }
        model.addAttribute("produto", produto);
        model.addAttribute("relacionados", produtoService.relacionados(produto));
        return "loja/produto";
    }
}
