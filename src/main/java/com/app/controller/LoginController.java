package com.app.controller;

import java.net.ConnectException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.app.constants.MappingConstants;
import com.app.constants.ViewConstants;
import com.app.dto.UserDTO;
import com.app.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(MappingConstants.ROOT)
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
	
	@GetMapping(MappingConstants.ADMIN_ACCOUNT)
	public String adminAccount(@RequestParam(name = "usuario", required = false) String usuario, HttpServletRequest request, Model model) {
		Principal tmp = request.getUserPrincipal();
		String loggedUser = tmp.getName();
		UserDTO usr = new UserDTO();
		
		if(usuario != "" && usuario != null) {
			loggedUser = usuario;
		}
		
		try {
			usr = loginService.getCurrentUser(loggedUser);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		
		model.addAttribute("user_register", usr.getUsuario());
		model.addAttribute("name_register", usr.getNombre());
		model.addAttribute("surname_register", usr.getApellido());
		model.addAttribute("mail_register", usr.getEmail());
		
		return ViewConstants.VIEW_MANAGE_ACCOUNT_PAGE;
	}
	
	@PostMapping(MappingConstants.UPDATE_ACCOUNT)
	public String updateAccount(UserDTO user2Update, HttpServletRequest request, Model model) {
		Principal loggedUser = request.getUserPrincipal();
		Locale currentLocale = RequestContextUtils.getLocale(request);
		UserDTO usr = new UserDTO();
		
		try {
			if(user2Update.getUsuario().equals(loggedUser.getName())) {
				String tmpPass = passwEncoder.encode(user2Update.getPassw());
				user2Update.setPassw(tmpPass);
				loginService.updateCurrentUser(user2Update);
			}
			usr = loginService.getCurrentUser(loggedUser.getName());
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		
		model.addAttribute("user_register", usr.getUsuario());
		model.addAttribute("name_register", usr.getNombre());
		model.addAttribute("surname_register", usr.getApellido());
		model.addAttribute("mail_register", usr.getEmail());
		
		if("en".equals(currentLocale.getLanguage())) {
			model.addAttribute("userCreated", "User Updated Successfully");
		}else {
			model.addAttribute("userCreated", "Usuario Actualizado con Exito");
		}
		
		return ViewConstants.VIEW_MANAGE_ACCOUNT_PAGE;
	}
	

	@GetMapping("/adminUsers")
	public String adminUsers(HttpServletRequest request, Model model) {
		Principal loggedUser = request.getUserPrincipal();
		if(!loggedUser.getName().equals("admin")) {
			return ViewConstants.REDIRECT_HOME_PAGE;
		}
		
		ArrayList<UserDTO> listUsers = new ArrayList<>();
		try {
			listUsers = loginService.getAllUsers();
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		
		model.addAttribute("userCreated", listUsers);
		
		return "public/adminUsers";
	}
	
	@PostMapping("/deleteUser")
	public String deleteUser(@RequestParam("item") String item) {
		try {
			loginService.deleteUser(item);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		return "redirect:/webAppMotorElectrico/adminUsers";
	}
}
