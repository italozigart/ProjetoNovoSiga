package com.novosiga.novosiga.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.novosiga.novosiga.entity.Usuario;
import com.novosiga.novosiga.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario save(Usuario usuario) {
        // Criptografa a senha antes de salvar o usuário
        usuario.setSenhaUsuario(
            passwordEncoder.encode(usuario.getSenhaUsuario())
        );
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // Verdadeiro se já existe um usuário com esse login
    public boolean loginEmUso(String login) {
        return login != null && usuarioRepository.existsByLoginUsuario(login);
    }
}
