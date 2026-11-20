package com.novosiga.novosiga.util;

import java.util.Locale;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.novosiga.novosiga.exception.NegocioException;

/**
 * Validação e entrega das imagens salvas no banco (foto do aluno,
 * imagem do produto e logo da atlética).
 */
public final class ImagemUtil {

    // Só aceitamos imagens comuns (SVG fica de fora porque pode conter scripts)
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/jpg", "image/pjpeg", "image/png", "image/webp", "image/gif");

    private ImagemUtil() {
    }

    // Verdadeiro quando o usuário escolheu um arquivo no formulário
    public static boolean foiEnviada(MultipartFile arquivo) {
        return arquivo != null && !arquivo.isEmpty();
    }

    // Lança erro amigável se o arquivo não for uma imagem
    public static void validar(MultipartFile arquivo) {
        if (!foiEnviada(arquivo)) {
            return;
        }
        String tipo = arquivo.getContentType();
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo.toLowerCase(Locale.ROOT))) {
            throw new NegocioException("Envie uma imagem nos formatos JPG, PNG, WEBP ou GIF.");
        }
    }

    // Monta a resposta HTTP com os bytes da imagem
    public static ResponseEntity<byte[]> resposta(byte[] bytes, String tipo) {
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType;
        try {
            mediaType = (tipo == null) ? MediaType.IMAGE_JPEG : MediaType.parseMediaType(tipo);
        } catch (RuntimeException e) {
            mediaType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok().contentType(mediaType).body(bytes);
    }
}
