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

import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.AtleticaService;
import com.novosiga.novosiga.service.ProdutoService;
import com.novosiga.novosiga.util.ImagemUtil;
import com.novosiga.novosiga.util.UrlUtil;

import jakarta.validation.Valid;

/**
 * Cadastro dos produtos das atléticas (área administrativa).
 * A vitrine para compra fica no LojaController.
 */
@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    private static final int ITENS_POR_PAGINA = 10;

    // Injeção de dependência da service de produtos
    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private AtleticaService atleticaService;

    // Método para salvar um produto
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Produto produto, BindingResult result,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem,
            Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            carregarListas(model);
            return "produto/formularioProduto";
        }

        try {
            ImagemUtil.validar(imagem);
            if (ImagemUtil.foiEnviada(imagem)) {
                produto.setImagemProduto(imagem.getBytes());
                produto.setTipoImagem(imagem.getContentType());
            } else if (produto.getIdProduto() != null) {
                // Edição sem nova imagem: mantém a imagem atual
                Produto produtoExistente = produtoService.findById(produto.getIdProduto());

                if (produtoExistente != null) {
                    produto.setImagemProduto(produtoExistente.getImagemProduto());
                    produto.setTipoImagem(produtoExistente.getTipoImagem());
                }
            }

            produtoService.save(produto);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Produto \"" + produto.getDescricaoProduto() + "\" salvo com sucesso!");
            return "redirect:/produtos/listar";

        } catch (NegocioException e) {
            model.addAttribute("erro", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Não foi possível salvar o produto. Confira os dados e tente novamente.");
        }

        carregarListas(model);
        return "produto/formularioProduto";
    }

    // Método para listar os produtos com barra de busca e paginação
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        Page<Produto> resultado = produtoService.buscar(q, pagina, ITENS_POR_PAGINA);

        model.addAttribute("produtos", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("urlBase", UrlUtil.comParametro("/produtos/listar", "q", q));

        return "produto/listarProdutos";
    }

    // Método para abrir o formulário para cadastro do produto
    @GetMapping("/criar")
    public String criarForm(Model model) {

        model.addAttribute("produto", new Produto());
        carregarListas(model);

        return "produto/formularioProduto";
    }

    // Método para abrir o formulário com os dados do produto
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {

        Produto produto = produtoService.findById(id);

        if (produto == null) {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado.");
            return "redirect:/produtos/listar";
        }

        model.addAttribute("produto", produto);
        carregarListas(model);

        return "produto/formularioProduto";
    }

    // Método para excluir um produto pelo ID
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Produto excluído com sucesso.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("erro", "Não é possível excluir: existem registros ligados a este produto.");
        }
        return "redirect:/produtos/listar";
    }

    // Método para buscar a imagem do produto pelo ID
    @GetMapping("/imagem/{id}")
    public ResponseEntity<byte[]> imagem(@PathVariable Integer id) {

        Produto produto = produtoService.findById(id);

        if (produto == null) {
            return ResponseEntity.notFound().build();
        }

        return ImagemUtil.resposta(produto.getImagemProduto(), produto.getTipoImagem());
    }

    // Atléticas e sugestões de categoria para o formulário
    private void carregarListas(Model model) {
        model.addAttribute("atleticas", atleticaService.findAll());
        model.addAttribute("categorias", produtoService.categoriasSugeridas());
    }

}
