package com.novosiga.novosiga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.novosiga.novosiga.entity.Usuario;
import com.novosiga.novosiga.service.UsuarioService;

import jakarta.validation.Valid;



@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/criar")
    public String criarForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario/formularioUsuario";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Usuario usuario, BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Esta tela é pública: só cria usuários novos (nunca altera um existente)
        usuario.setIdUsuario(null);
        usuario.setRole("ROLE_USER");

        // Login repetido
        if (!result.hasFieldErrors("loginUsuario") && usuarioService.loginEmUso(usuario.getLoginUsuario())) {
            result.rejectValue("loginUsuario", "loginDuplicado", "Este login já está em uso. Escolha outro.");
        }

        if (result.hasErrors()) {
            return "usuario/formularioUsuario";
        }

        usuarioService.save(usuario);
        redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado! Entre com seu login e senha.");
        return "redirect:/login";
    }


}
