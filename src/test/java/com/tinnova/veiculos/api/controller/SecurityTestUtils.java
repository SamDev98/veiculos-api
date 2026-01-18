package com.tinnova.veiculos.api.controller;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public class SecurityTestUtils {

    public static RequestPostProcessor usuarioAutenticado() {
        return SecurityMockMvcRequestPostProcessors
                .user("user")
                .roles("USER");
    }

    public static RequestPostProcessor adminAutenticado() {
        return SecurityMockMvcRequestPostProcessors
                .user("admin")
                .roles("ADMIN", "USER");
    }
}
