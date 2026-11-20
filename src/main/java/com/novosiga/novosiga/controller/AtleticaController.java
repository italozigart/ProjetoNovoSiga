package com.novosiga.novosiga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.novosiga.novosiga.entity.Atletica;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.AtleticaService;
import com.novosiga.novosiga.service.CursoService;
import com.novosiga.novosiga.util.ImagemUtil;
import com.novosiga.novosiga.util.UrlUtil;

import jakarta.validation.Valid;

/**
 * Cadastro das atléticas (quem vende os produtos da loja).
 */
@Controller
@RequestMapping("/atleticas")
public class AtleticaController {

    private static final int ITENS_POR_PAGINA = 10;

    @Autowired
    private AtleticaService atleticaService;

    @Autowired
    private CursoService cursoService;

    // Salvar atlética (com logo opcional)
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Atletica atletica, BindingResult result,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            carregarCursos(model);
            return "atletica/formularioAtletica";
        }

        try {
            ImagemUtil.validar(logo);
            if (ImagemUtil.foiEnviada(logo)) {
                atletica.setLogoAtletica(logo.getBytes());
                atletica.setTipoLogo(logo.getContentType());
            } else if (atletica.getIdAtletica() != null) {
                // Edição sem novo logo: mantém o logo atual
                Atletica existente = atleticaService.findById(atletica.getIdAtletica());
                if (existente != null) {
                    atletica.setLogoAtletica(existente.getLogoAtletica());
                    atletica.setTipoLogo(existente.getTipoLogo());
                }
            }
            atleticaService.save(atletica);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Atlética \"" + atletica.getNomeAtletica() + "\" salva com sucesso!");
            return "redirect:/atleticas/listar";

        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Não foi possível salvar a atlética. Confira os dados e tente novamente.");
        }

        carregarCursos(model);
        return "atletica/formularioAtletica";
    }

    // Listar atléticas (com barra de busca e paginação)
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        Page<Atletica> resultado = atleticaService.buscar(q, pagina, ITENS_POR_PAGINA);

        model.addAttribute("atleticas", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("urlBase", UrlUtil.comParametro("/atleticas/listar", "q", q));

        return "atletica/listarAtleticas";
    }

    @GetMapping("/criar")
    public String criarForm(Model model) {
        model.addAttribute("atletica", new Atletica());
        carregarCursos(model);
        return "atletica/formularioAtletica";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Atletica atletica = atleticaService.findById(id);
        if (atletica == null) {
            redirectAttributes.addFlashAttribute("erro", "Atlética não encontrada.");
            return "redirect:/atleticas/listar";
        }
        model.addAttribute("atletica", atletica);
        carregarCursos(model);
        return "atletica/formularioAtletica";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            atleticaService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Atlética excluída com sucesso.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro", "Não é possível excluir: existem registros ligados a esta atlética.");
        }
        return "redirect:/atleticas/listar";
    }

    // Devolve o logo da atlética para a tag <img>
    @GetMapping("/logo/{id}")
    public ResponseEntity<byte[]> logo(@PathVariable Integer id) {
        Atletica atletica = atleticaService.findById(id);
        if (atletica == null) {
            return ResponseEntity.notFound().build();
        }
        return ImagemUtil.resposta(atletica.getLogoAtletica(), atletica.getTipoLogo());
    }

    private void carregarCursos(Model model) {
        model.addAttribute("cursos", cursoService.findAll());
    }
}
