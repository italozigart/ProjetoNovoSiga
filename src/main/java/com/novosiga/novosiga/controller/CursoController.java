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

import com.novosiga.novosiga.entity.Curso;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.CursoService;
import com.novosiga.novosiga.util.UrlUtil;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    private static final int ITENS_POR_PAGINA = 10;

    @Autowired
    private CursoService cursoService;

    // Listar cursos (com barra de busca e paginação)
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        Page<Curso> resultado = cursoService.buscar(q, pagina, ITENS_POR_PAGINA);

        model.addAttribute("cursos", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("urlBase", UrlUtil.comParametro("/cursos/listar", "q", q));

        return "curso/listarCursos";
    }

    // Abrir formulário para criar curso
    @GetMapping("/criar")
    public String criarForm(Model model) {

        model.addAttribute("curso", new Curso());

        return "curso/formularioCurso";
    }

    // Salvar curso
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Curso curso, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "curso/formularioCurso";
        }

        try {
            cursoService.save(curso);
            redirectAttributes.addFlashAttribute("sucesso", "Curso \"" + curso.getNomeCurso() + "\" salvo com sucesso!");
            return "redirect:/cursos/listar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Não foi possível salvar o curso. Confira os dados e tente novamente.");
            return "curso/formularioCurso";
        }
    }

    // Abrir formulário para editar
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Integer id,
            Model model, RedirectAttributes redirectAttributes) {

        Curso curso = cursoService.findById(id);

        if (curso == null) {
            redirectAttributes.addFlashAttribute("erro", "Curso não encontrado.");
            return "redirect:/cursos/listar";
        }

        model.addAttribute("curso", curso);

        return "curso/formularioCurso";
    }

    // Excluir curso (bloqueado se houver alunos, disciplinas ou atléticas ligados a ele)
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            cursoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Curso excluído com sucesso.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro", "Não é possível excluir: existem registros ligados a este curso.");
        }
        return "redirect:/cursos/listar";
    }
}
