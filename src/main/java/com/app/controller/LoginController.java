package com.app.controller;

import java.net.ConnectException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
	
	@Autowired
	private MessageSource messageSource;
	
	BCryptPasswordEncoder passwEncoder;
	
	public void setMessageSource(MessageSource ms){
        this.messageSource = ms;
    }

	public LoginController(LoginService loginService, BCryptPasswordEncoder passwEncoder) {
		this.loginService = loginService;
		this.passwEncoder = passwEncoder;
	}
	
	public String getMessage(String messageKey) {
		return this.getMessage(messageKey, null);
	}
	
	public String getMessage(String messageKey, Object[] args) {
		return this.messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
	}

	@GetMapping(MappingConstants.LOGIN_ROOT)
	public String login(Model model, @RequestParam(name = "error", required = false) String error, HttpServletRequest request) {
		if (error != null) {
			model.addAttribute("errorMessage", this.getMessage("view.cont.user.not"));
        }
		return ViewConstants.VIEW_LOGIN_PAGE;
	}

	@GetMapping(MappingConstants.LOGOUT_ROOT)
	public String logout() {
		return ViewConstants.REDIRECT_LOGIN_PAGE;
	}

	@PostMapping(MappingConstants.REGISTER_ROOT)
	public String register(UserDTO user, Model model, HttpServletRequest request) {
		try {
			String tmpCheck = loginService.checkUserInDB(user.getUsuario());
			if ("1".equals(tmpCheck)) {
				String tmpPass = passwEncoder.encode(user.getPassw());
				user.setPassw(tmpPass);
				user.setRole("USER");
				user.setMaxdataset(5);
				loginService.createUserInDB(user);
				model.addAttribute("userCreated", this.getMessage("view.cont.user.created"));
			} else {
				model.addAttribute("userAlreadyExists", this.getMessage("view.cont.user.exists"));
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
		model.addAttribute("maxdataset_register", usr.getMaxdataset());
		model.addAttribute("loggedUser", request.getUserPrincipal().getName());
		return ViewConstants.VIEW_MANAGE_ACCOUNT_PAGE;
	}
	
	@PostMapping(MappingConstants.UPDATE_ACCOUNT)
	public String updateAccount(UserDTO user2Update, HttpServletRequest request, Model model) {
		Principal loggedUser = request.getUserPrincipal();
		UserDTO usr = new UserDTO();
		
		try {
			if(user2Update.getUsuario().equals(loggedUser.getName())) {
				String tmpPass = passwEncoder.encode(user2Update.getPassw());
				user2Update.setPassw(tmpPass);
				loginService.updateCurrentUser(user2Update);
				usr = loginService.getCurrentUser(loggedUser.getName());
			}else {
				if("admin".equals(loggedUser.getName())) {
					String tmpPass = passwEncoder.encode(user2Update.getPassw());
					user2Update.setPassw(tmpPass);
					loginService.updateCurrentUser(user2Update);
					usr = loginService.getCurrentUser(user2Update.getUsuario());
				}
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		
		model.addAttribute("user_register", usr.getUsuario());
		model.addAttribute("name_register", usr.getNombre());
		model.addAttribute("surname_register", usr.getApellido());
		model.addAttribute("mail_register", usr.getEmail());
		model.addAttribute("maxdataset_register", usr.getMaxdataset());
		
		model.addAttribute("userCreated", this.getMessage("view.cont.user.updated"));
		model.addAttribute("loggedUser", request.getUserPrincipal().getName());
		
		if("admin".equals(loggedUser.getName())) {
			return "redirect:/webAppMotorElectrico/adminUsers";
		}
		
		return ViewConstants.VIEW_MANAGE_ACCOUNT_PAGE;
	}
	

	@GetMapping("/adminUsers")
	public String adminUsers(HttpServletRequest request, Model model) {
		Principal loggedUser = request.getUserPrincipal();
		if(!loggedUser.getName().equals("admin")) {
			return ViewConstants.REDIRECT_HOME_PAGE;
		}
		
		List<UserDTO> listUsers = new ArrayList<>();
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
