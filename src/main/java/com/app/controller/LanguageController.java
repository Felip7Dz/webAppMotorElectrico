package com.app.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.app.constants.MappingConstants;
import com.app.constants.ViewConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(MappingConstants.ROOT)
public class LanguageController {

	@GetMapping(MappingConstants.CHANGE_LOCALE)
	public String changeLocale(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = true) String lang) {

		request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale(lang));

		//String referer = request.getHeader("Referer");

		return ViewConstants.REDIRECT_HOME_PAGE;
	}
}