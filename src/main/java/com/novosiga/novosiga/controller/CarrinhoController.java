package com.novosiga.novosiga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.novosiga.novosiga.carrinho.ResumoCarrinho;
import com.novosiga.novosiga.entity.Pedido;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.AlunoService;
import com.novosiga.novosiga.service.CarrinhoService;
import com.novosiga.novosiga.service.PedidoService;
import com.novosiga.novosiga.util.UrlUtil;

/**
 * Carrinho de compras: adicionar, alterar quantidade, remover e finalizar a compra.
 */
@Controller
@RequestMapping("/carrinho")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private AlunoService alunoService;

    // Tela do carrinho
    @GetMapping
    public String ver(Model model) {
        model.addAttribute("resumo", carrinhoService.resumo());
        return "carrinho/carrinho";
    }

    // Adiciona um produto. "voltar" = página para onde o usuário retorna (ex.: a loja com os filtros)
    @PostMapping("/adicionar")
    public String adicionar(@RequestParam Integer idProduto,
            @RequestParam(defaultValue = "1") int quantidade,
            @RequestParam(required = false) String voltar,
            RedirectAttributes redirectAttributes) {
        try {
            Produto produto = carrinhoService.adicionar(idProduto, quantidade);
            redirectAttributes.addFlashAttribute("sucesso",
                    "\"" + produto.getDescricaoProduto() + "\" foi adicionado ao carrinho.");
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return UrlUtil.redirecionar(voltar, "/carrinho");
    }

    // Altera a quantidade de um item
    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Integer idProduto,
            @RequestParam(defaultValue = "1") int quantidade,
            RedirectAttributes redirectAttributes) {
        try {
            carrinhoService.alterarQuantidade(idProduto, quantidade);
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    // Remove um item
    @PostMapping("/remover")
    public String remover(@RequestParam Integer idProduto, RedirectAttributes redirectAttributes) {
        carrinhoService.remover(idProduto);
        redirectAttributes.addFlashAttribute("sucesso", "Produto removido do carrinho.");
        return "redirect:/carrinho";
    }

    // Esvazia o carrinho
    @PostMapping("/limpar")
    public String limpar(RedirectAttributes redirectAttributes) {
        carrinhoService.limpar();
        redirectAttributes.addFlashAttribute("sucesso", "Carrinho esvaziado.");
        return "redirect:/carrinho";
    }

    // Tela de finalização: escolher o aluno comprador e a forma de pagamento
    @GetMapping("/finalizar")
    public String finalizarForm(Model model, RedirectAttributes redirectAttributes) {
        ResumoCarrinho resumo = carrinhoService.resumo();

        if (resumo.isVazio()) {
            redirectAttributes.addFlashAttribute("erro", "Seu carrinho está vazio.");
            return "redirect:/carrinho";
        }
        if (resumo.isProblemaEstoque()) {
            redirectAttributes.addFlashAttribute("erro",
                    "Ajuste as quantidades: alguns produtos não têm estoque suficiente.");
            return "redirect:/carrinho";
        }

        model.addAttribute("resumo", resumo);
        model.addAttribute("alunos", alunoService.findAll());
        model.addAttribute("formasPagamento", PedidoService.FORMAS_PAGAMENTO);
        return "carrinho/finalizar";
    }

    // Confirma a compra: grava o pedido, baixa o estoque e esvazia o carrinho
    @PostMapping("/finalizar")
    public String finalizar(@RequestParam(required = false) Integer idAluno,
            @RequestParam(required = false) String formaPagamento,
            RedirectAttributes redirectAttributes) {
        try {
            Pedido pedido = pedidoService.finalizarCompra(idAluno, formaPagamento, carrinhoService.getItens());
            carrinhoService.limpar();
            redirectAttributes.addFlashAttribute("sucesso",
                    "Pedido #" + pedido.getIdPedido() + " realizado com sucesso!");
            return "redirect:/pedidos/detalhes/" + pedido.getIdPedido();
        } catch (NegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/carrinho/finalizar";
        }
    }
}
