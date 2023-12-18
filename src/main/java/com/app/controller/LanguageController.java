package com.app.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LanguageController {

    @GetMapping("/change-locale")
    public String changeLocale(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(defaultValue = "es") String lang) {

        // Establecer el idioma en la sesión
        request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale(lang));

        // Obtener la URL de referencia (página de origen)
        String referer = request.getHeader("Referer");

        // Redirigir de vuelta a la página de origen
        return "redirect:" + referer;
    }
}