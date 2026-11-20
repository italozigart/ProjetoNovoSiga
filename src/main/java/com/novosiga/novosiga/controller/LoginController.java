package com.novosiga.novosiga.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // A tela /home agora fica no HomeController (com os totais e destaques)
}
