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

import com.novosiga.novosiga.entity.Disciplina;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.CursoService;
import com.novosiga.novosiga.service.DisciplinaService;
import com.novosiga.novosiga.service.ProfessorService;
import com.novosiga.novosiga.util.UrlUtil;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/disciplinas")
public class DisciplinaController {

    private static final int ITENS_POR_PAGINA = 10;

    // Injeção de dependência da service de disciplinas
    @Autowired
    private DisciplinaService disciplinaService;

    // Service dos cursos
    @Autowired
    private CursoService cursoService;

    // Service dos professores
    @Autowired
    private ProfessorService professorService;


    // Método para salvar uma disciplina
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Disciplina disciplina, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            carregarListas(model);
            return "disciplina/formularioDisciplina";
        }

        try {
            disciplinaService.save(disciplina);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Disciplina \"" + disciplina.getNomeDisciplina() + "\" salva com sucesso!");
            return "redirect:/disciplinas/listar";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Não foi possível salvar a disciplina. Confira os dados e tente novamente.");
            carregarListas(model);
            return "disciplina/formularioDisciplina";
        }
    }


    // Método para listar as disciplinas com barra de busca e paginação
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        Page<Disciplina> resultado = disciplinaService.buscar(q, pagina, ITENS_POR_PAGINA);

        model.addAttribute("disciplinas", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("urlBase", UrlUtil.comParametro("/disciplinas/listar", "q", q));

        return "disciplina/listarDisciplinas";
    }


    // Método para abrir o formulário para cadastro da disciplina
    @GetMapping("/criar")
    public String criarForm(Model model) {

        model.addAttribute("disciplina", new Disciplina());
        carregarListas(model);

        return "disciplina/formularioDisciplina";
    }


    // Método para abrir o formulário com os dados da disciplina
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {

        Disciplina disciplina = disciplinaService.findById(id);

        if (disciplina == null) {
            redirectAttributes.addFlashAttribute("erro", "Disciplina não encontrada.");
            return "redirect:/disciplinas/listar";
        }

        model.addAttribute("disciplina", disciplina);
        carregarListas(model);

        return "disciplina/formularioDisciplina";
    }


    // Método para excluir uma disciplina pelo ID
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            disciplinaService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Disciplina excluída com sucesso.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro", "Não é possível excluir: existem registros ligados a esta disciplina.");
        }
        return "redirect:/disciplinas/listar";
    }

    // Cursos e professores para os selects do formulário
    private void carregarListas(Model model) {
        model.addAttribute("cursos", cursoService.findAll());
        model.addAttribute("professores", professorService.findAll());
    }

}
