package com.novosiga.novosiga.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.novosiga.novosiga.busca.Sugestao;
import com.novosiga.novosiga.service.BuscaService;

/**
 * Busca geral do sistema (barra de busca do cabeçalho).
 */
@Controller
@RequestMapping("/busca")
public class BuscaController {

    @Autowired
    private BuscaService buscaService;

    // Página com os resultados agrupados por tipo: /busca?q=joao
    @GetMapping
    public String buscar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("resultado", buscaService.buscar(q));
        model.addAttribute("q", q);
        model.addAttribute("termoBuscaGlobal", q); // mantém o texto na barra do cabeçalho
        return "busca/resultados";
    }

    // Sugestões em JSON para o autocompletar (chamado pelo JavaScript enquanto o usuário digita)
    @GetMapping("/sugestoes")
    @ResponseBody
    public List<Sugestao> sugestoes(@RequestParam(required = false) String q) {
        return buscaService.sugerir(q);
    }
}
