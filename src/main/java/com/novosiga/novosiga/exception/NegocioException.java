package com.novosiga.novosiga.exception;

/**
 * Erro de regra de negócio (ex.: estoque insuficiente, curso com alunos vinculados).
 * A mensagem é exibida diretamente para o usuário na tela.
 */
public class NegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
