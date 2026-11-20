package com.novosiga.novosiga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.novosiga.novosiga.entity.Professor;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.ProfessorService;
import com.novosiga.novosiga.util.UrlUtil;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/professores")
public class ProfessorController {

    private static final int ITENS_POR_PAGINA = 10;

    // Injeção de dependência da service de professores
    @Autowired
    private ProfessorService professorService;

    // Método para salvar um professor
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Professor professor, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "professor/formularioProfessor";
        }

        try {
            professorService.save(professor);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Professor \"" + professor.getNomeProfessor() + "\" salvo com sucesso!");
            return "redirect:/professores/listar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Não foi possível salvar o professor. Confira os dados e tente novamente.");
            return "professor/formularioProfessor";
        }
    }

    // Método para listar os professores com barra de busca e paginação
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        Page<Professor> resultado = professorService.buscar(q, pagina, ITENS_POR_PAGINA);

        model.addAttribute("professores", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("urlBase", UrlUtil.comParametro("/professores/listar", "q", q));

        return "professor/listarProfessores";
    }

    // Método para abrir o formulário para cadastro do professor
    @GetMapping("/criar")
    public String criarForm(Model model) {
        model.addAttribute("professor", new Professor());
        return "professor/formularioProfessor";
    }

    // Método para abrir o formulário com os dados do professor
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Professor professor = professorService.findById(id);
        if (professor == null) {
            redirectAttributes.addFlashAttribute("erro", "Professor não encontrado.");
            return "redirect:/professores/listar";
        }
        model.addAttribute("professor", professor);
        return "professor/formularioProfessor";
    }

    // Método para excluir um professor pelo ID
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            professorService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Professor excluído com sucesso.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro", "Não é possível excluir: existem registros ligados a este professor.");
        }
        return "redirect:/professores/listar";
    }

}
