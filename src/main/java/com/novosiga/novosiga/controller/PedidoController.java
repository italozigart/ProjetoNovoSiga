package com.novosiga.novosiga.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.novosiga.novosiga.dto.PedidoRequest;
import com.novosiga.novosiga.entity.Aluno;
import com.novosiga.novosiga.entity.Pedido;
import com.novosiga.novosiga.entity.Produto;
import com.novosiga.novosiga.exception.NegocioException;
import com.novosiga.novosiga.service.AlunoService;
import com.novosiga.novosiga.service.PedidoService;
import com.novosiga.novosiga.service.ProdutoService;
import com.novosiga.novosiga.util.UrlUtil;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private static final int ITENS_POR_PAGINA = 10;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private ProdutoService produtoService;

    // Endpoint responsável por salvar (JSON pelo fetch da tela "Novo pedido (balcão)")
    // Devolve um JSON simples para evitar a recursão Pedido -> Itens -> Pedido na conversão
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvarPedido(@RequestBody PedidoRequest request) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        try {
            Pedido pedido = pedidoService.salvarPedido(request);
            resposta.put("idPedido", pedido.getIdPedido());
            resposta.put("total", pedido.getTotalPedido());
            resposta.put("url", "/pedidos/detalhes/" + pedido.getIdPedido());
            return ResponseEntity.ok(resposta);
        } catch (NegocioException e) {
            resposta.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(resposta);
        }
    }

    // Lista de pedidos com busca (número, aluno, CPF ou forma de pagamento)
    @GetMapping("/listar")
    public String listar(@RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int pagina, Model model) {

        Page<Pedido> resultado = pedidoService.buscar(q, pagina, ITENS_POR_PAGINA);

        model.addAttribute("pedidos", resultado.getContent());
        model.addAttribute("pagina", resultado);
        model.addAttribute("q", q);
        model.addAttribute("urlBase", UrlUtil.comParametro("/pedidos/listar", "q", q));

        return "pedido/listarPedidos";
    }

    // Detalhes de um pedido (itens, aluno, total)
    @GetMapping("/detalhes/{id}")
    public String detalhes(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Pedido pedido = pedidoService.findById(id);
        if (pedido == null) {
            redirectAttributes.addFlashAttribute("erro", "Pedido não encontrado.");
            return "redirect:/pedidos/listar";
        }
        model.addAttribute("pedido", pedido);
        return "pedido/detalhePedido";
    }

    // abrir a tela de cadastro (pedido de balcão)
    @GetMapping("/criar")
    public String criarForm(Model model) {
        //alunos
        List<Aluno> alunos = alunoService.findAll();
        model.addAttribute("alunos", alunos);
        //produtos
        List<Produto> produtos = produtoService.findAll();
        model.addAttribute("produtos", produtos);
        //formas de pagamento
        model.addAttribute("formasPagamento", PedidoService.FORMAS_PAGAMENTO);

        return "pedido/formularioPedido";
    }

}
