package com.app.controller;

import java.net.ConnectException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.app.dto.UserDTO;
import com.app.service.LoginService;

@Controller
public class LoginController {
	
	  @Autowired
	
	private final LoginService loginService;
	
	public LoginController(LoginService loginService) {
		this.loginService = loginService;
	}

	
	@GetMapping("/login")
	public String login(Model model) {
		return "public/login";
	}
	
	@GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }
	
	@PostMapping("/register")
	public String register(UserDTO user, Model model) {
		try {
			String tmpCheck = loginService.checkUserInDB(user.getUsuario());
			if("1".equals(tmpCheck)) {
				String tmpPass = user.getPassw();
				user.setPassw(tmpPass);
				user.setRole("USER");
				loginService.createUserInDB(user);
				model.addAttribute("userCreated", "User Created Successfully");
			}else {
				model.addAttribute("userAlreadyExists", "User Already Exists");
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		
		return "public/login";
	}
}
