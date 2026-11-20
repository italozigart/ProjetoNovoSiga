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

import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.AlunoService;
import com.novosiga.novosiga.service.CursoService;
import com.novosiga.novosiga.util.ImagemUtil;
import com.novosiga.novosiga.util.UrlUtil;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/alunos")
public class AlunoController {

    private static final int ITENS_POR_PAGINA = 10;

    // Injeção de dependência da service de alunos
    @Autowired
    private AlunoService alunoService;

    @Autowired
    private CursoService cursoService;

    // Método para salvar um aluno (@Valid confere as regras da entidade: nome, CPF, CEP...)
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Aluno aluno, BindingResult result,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Model model, RedirectAttributes redirectAttributes) {

        // Algum campo inválido: volta para o formulário mostrando os erros
        if (result.hasErrors()) {
            carregarCursos(model);
            return "aluno/formularioAluno";
        }

        try {
            ImagemUtil.validar(foto);
            if (ImagemUtil.foiEnviada(foto)) {
                aluno.setFotoAluno(foto.getBytes());
                aluno.setTipoFoto(foto.getContentType());
            } else if (aluno.getIdAluno() != null) {
                // Edição sem nova foto: mantém a foto que já estava salva
                Aluno alunoExistente = alunoService.findById(aluno.getIdAluno());
                if (alunoExistente != null) {
                    aluno.setFotoAluno(alunoExistente.getFotoAluno());
                    aluno.setTipoFoto(alunoExistente.getTipoFoto());
                }
            }
            alunoService.save(aluno);
            redirectAttributes.addFlashAttribute("sucesso", "Aluno \"" + aluno.getNomeAluno() + "\" salvo com sucesso!");
            return "redirect:/alunos/listar";

        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Não foi possível salvar o aluno. Confira os dados e tente novamente.");
        }

        carregarCursos(model);
        return "aluno/formularioAluno";
    }

    // Método para listar os alunos com barra de busca e paginação
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        Page<Aluno> resultado = alunoService.buscar(q, pagina, ITENS_POR_PAGINA);

        model.addAttribute("alunos", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("urlBase", UrlUtil.comParametro("/alunos/listar", "q", q));

        return "aluno/listarAlunos";
    }

    // Método para abrir o formulário para cadastro do aluno
    @GetMapping("/criar")
    public String criarForm(Model model) {
        model.addAttribute("aluno", new Aluno());
        carregarCursos(model);
        return "aluno/formularioAluno";
    }

    // Método para abrir o formulário com os dados do aluno
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Aluno aluno = alunoService.findById(id);
        if (aluno == null) {
            redirectAttributes.addFlashAttribute("erro", "Aluno não encontrado.");
            return "redirect:/alunos/listar";
        }
        model.addAttribute("aluno", aluno);
        carregarCursos(model);
        return "aluno/formularioAluno";
    }

    // Método para excluir um aluno pelo ID
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            alunoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Aluno excluído com sucesso.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro", "Não é possível excluir: existem registros ligados a este aluno.");
        }
        return "redirect:/alunos/listar";
    }

    // Devolve a foto do aluno para a tag <img>
    @GetMapping("/foto/{id}")
    public ResponseEntity<byte[]> foto(@PathVariable Integer id) {
        Aluno aluno = alunoService.findById(id);
        if (aluno == null) {
            return ResponseEntity.notFound().build();
        }
        return ImagemUtil.resposta(aluno.getFotoAluno(), aluno.getTipoFoto());
    }

    private void carregarCursos(Model model) {
        model.addAttribute("cursos", cursoService.findAll());
    }
}
