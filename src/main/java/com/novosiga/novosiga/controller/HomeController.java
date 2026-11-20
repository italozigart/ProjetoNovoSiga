package com.novosiga.novosiga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.novosiga.novosiga.service.AlunoService;
import com.novosiga.novosiga.service.AtleticaService;
import com.novosiga.novosiga.service.CursoService;
import com.novosiga.novosiga.service.DisciplinaService;
import com.novosiga.novosiga.service.PedidoService;
import com.novosiga.novosiga.service.ProdutoService;
import com.novosiga.novosiga.service.ProfessorService;

@Controller
public class HomeController {

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private DisciplinaService disciplinaService;

    @Autowired
    private AtleticaService atleticaService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private PedidoService pedidoService;

    // Endereço raiz: manda para a tela inicial (ou para o login, se não estiver logado)
    @GetMapping("/")
    public String raiz() {
        return "redirect:/home";
    }

    // Tela inicial com atalhos, totais e produtos em destaque
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("totalAlunos", alunoService.contar());
        model.addAttribute("totalProfessores", professorService.contar());
        model.addAttribute("totalCursos", cursoService.contar());
        model.addAttribute("totalDisciplinas", disciplinaService.contar());
        model.addAttribute("totalAtleticas", atleticaService.contar());
        model.addAttribute("totalProdutos", produtoService.contar());
        model.addAttribute("totalPedidos", pedidoService.contar());
        model.addAttribute("destaques", produtoService.destaques());
        return "home";
    }
}
