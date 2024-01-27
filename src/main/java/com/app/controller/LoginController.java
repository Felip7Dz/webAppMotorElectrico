package com.app.controller;

import java.net.ConnectException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.app.constants.MappingConstants;
import com.app.constants.ViewConstants;
import com.app.dto.UserDTO;
import com.app.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

	@Autowired
	private final LoginService loginService;
	
	BCryptPasswordEncoder passwEncoder;

	public LoginController(LoginService loginService, BCryptPasswordEncoder passwEncoder) {
		this.loginService = loginService;
		this.passwEncoder = passwEncoder;
	}

	@GetMapping(MappingConstants.LOGIN_ROOT)
	public String login(Model model) {
		return ViewConstants.VIEW_LOGIN_PAGE;
	}

	@GetMapping(MappingConstants.LOGOUT_ROOT)
	public String logout() {
		return ViewConstants.REDIRECT_LOGIN_PAGE;
	}

	@PostMapping(MappingConstants.REGISTER_ROOT)
	public String register(UserDTO user, Model model, HttpServletRequest request) {
		Locale currentLocale = RequestContextUtils.getLocale(request);
		try {
			String tmpCheck = loginService.checkUserInDB(user.getUsuario());
			if ("1".equals(tmpCheck)) {
				String tmpPass = passwEncoder.encode(user.getPassw());
				user.setPassw(tmpPass);
				user.setRole("USER");
				loginService.createUserInDB(user);
				if("en".equals(currentLocale.getLanguage())) {
					model.addAttribute("userCreated", "User Created Successfully");
				}else {
					model.addAttribute("userCreated", "Usuario Creado con Exito");
				}
			} else {
				if("en".equals(currentLocale.getLanguage())) {
					model.addAttribute("userAlreadyExists", "User Already Exists");
				}else {
					model.addAttribute("userAlreadyExists", "El Nombre de Usuario ya Existe");
				}
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		return ViewConstants.VIEW_LOGIN_PAGE;
	}
}
