package com.app.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.app.constants.MappingConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LanguageController {

	@GetMapping(MappingConstants.CHANGE_LOCALE)
	public String changeLocale(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "es") String lang) {

		request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale(lang));

		String referer = request.getHeader("Referer");

		return "redirect:" + referer;
	}
}