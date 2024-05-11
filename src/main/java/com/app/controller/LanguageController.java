package com.app.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.app.constants.MappingConstants;
import com.app.constants.ViewConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Clase LanguageController para cambiar el idioma.
 */
@Controller
@RequestMapping(MappingConstants.ROOT)
public class LanguageController {
	
    private final CookieLocaleResolver localeResolver = new CookieLocaleResolver();

	/**
	 * Metodo para cambiar el idioma.
	 *
	 * @param request the request
	 * @param response the response
	 * @param lang idioma al que se va a cambiar
	 * @return vista de home
	 */
	@GetMapping(MappingConstants.CHANGE_LOCALE)
	public String changeLocale(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = true) String lang) {

		localeResolver.setLocale(request, response, new Locale(lang));

		return ViewConstants.REDIRECT_HOME_PAGE;
	}

	/**
	 * Creacion de la cookie de idioma
	 *
	 * @return the locale resolver
	 */
	public CookieLocaleResolver getLocaleResolver() {
		return localeResolver;
	}
}