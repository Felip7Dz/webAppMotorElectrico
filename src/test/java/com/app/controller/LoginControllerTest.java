package com.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;

import com.app.constants.ViewConstants;
import com.app.dto.UserDTO;
import com.app.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;

public class LoginControllerTest {

    @Mock
    private LoginService loginService;
    
    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;
    
    @Mock
    private BCryptPasswordEncoder passwEncoder;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
		MessageSource messageSource = mock(MessageSource.class);
	    when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
        .thenReturn("Mocked Message");
    }

    @Test
    public void testLogin_Error() {
        // Prepare
        String error = "error";
        when(model.addAttribute(anyString(), anyString())).thenReturn(model);

		MessageSource ms = mock(MessageSource.class);
		loginController.setMessageSource(ms);
        
        // Execute
        String result = loginController.login(model, error, request);

        // Verify
        assertEquals(ViewConstants.VIEW_LOGIN_PAGE, result);
        verify(model).addAttribute("errorMessage", loginController.getMessage("view.cont.user.not"));
    }

    @Test
    public void testLogout() {
        // Execute
        String result = loginController.logout();

        // Verify
        assertEquals(ViewConstants.REDIRECT_LOGIN_PAGE, result);
    }

    @Test
    public void testRegister_UserCreated() throws ConnectException {
        // Prepare
        UserDTO userDTO = new UserDTO();
        userDTO.setUsuario("test");
        userDTO.setPassw("test");
        when(loginService.checkUserInDB(anyString())).thenReturn("1");
        
		MessageSource ms = mock(MessageSource.class);
		loginController.setMessageSource(ms);

        // Execute
        String result = loginController.register(userDTO, model, request);

        // Verify
        assertEquals(ViewConstants.VIEW_LOGIN_PAGE, result);
        verify(loginService).createUserInDB(userDTO);
        verify(model).addAttribute("userCreated", loginController.getMessage("view.cont.user.created"));
    }

    @Test
    public void testRegister_UserExists() throws ConnectException {
        // Prepare
        UserDTO userDTO = new UserDTO();
        userDTO.setUsuario("test");
        userDTO.setPassw("test");
        when(loginService.checkUserInDB(anyString())).thenReturn("0");

		MessageSource ms = mock(MessageSource.class);
		loginController.setMessageSource(ms);
        
        // Execute
        String result = loginController.register(userDTO, model, request);

        // Verify
        assertEquals(ViewConstants.VIEW_LOGIN_PAGE, result);
        verify(model).addAttribute("userAlreadyExists", loginController.getMessage("view.cont.user.exists"));
    }

    @Test
    public void testAdminAccount() throws ConnectException {
        // Prepare
        Principal principal = mock(Principal.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("admin");
        UserDTO userDTO = new UserDTO();
        when(loginService.getCurrentUser(anyString())).thenReturn(userDTO);

        // Execute
        String result = loginController.adminAccount(null, request, model);

        // Verify
        assertEquals(ViewConstants.VIEW_MANAGE_ACCOUNT_PAGE, result);
        verify(loginService).getCurrentUser("admin");
        verify(model).addAttribute("user_register", userDTO.getUsuario());
        verify(model).addAttribute("name_register", userDTO.getNombre());
        verify(model).addAttribute("surname_register", userDTO.getApellido());
        verify(model).addAttribute("mail_register", userDTO.getEmail());
    }

    @Test
    public void testUpdateAccount_SameUser() throws ConnectException {
        // Prepare
        UserDTO userDTO = new UserDTO();
        userDTO.setUsuario("test");
        userDTO.setPassw("test");
        Principal principal = mock(Principal.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user");
        when(loginService.getCurrentUser(anyString())).thenReturn(userDTO);

		MessageSource ms = mock(MessageSource.class);
		loginController.setMessageSource(ms);
        
        // Execute
        loginController.updateAccount(userDTO, request, model);
    }

    @Test
    public void testUpdateAccount_AdminUser() throws ConnectException {
        // Prepare
        UserDTO userDTO = new UserDTO();
        userDTO.setUsuario("test");
        userDTO.setPassw("test");
        Principal principal = mock(Principal.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("admin");
        when(loginService.getCurrentUser(anyString())).thenReturn(userDTO);

		MessageSource ms = mock(MessageSource.class);
		loginController.setMessageSource(ms);
		
		//when(loginController.getMessage("view.exp.main.title")).thenReturn("Main Title");
        
        // Execute
        loginController.updateAccount(userDTO, request, model);
    }

    @Test
    public void testAdminUsers() throws ConnectException {
        // Prepare
        Principal principal = mock(Principal.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("admin");
        List<UserDTO> userList = new ArrayList<>();
        when(loginService.getAllUsers()).thenReturn(userList);

        // Execute
        String result = loginController.adminUsers(request, model);

        // Verify
        assertEquals("public/adminUsers", result);
        verify(loginService).getAllUsers();
        verify(model).addAttribute(eq("userCreated"), anyList());
    }

    @Test
    public void testDeleteUser() throws ConnectException {
        // Execute
        String result = loginController.deleteUser("username");

        // Verify
        assertEquals("redirect:/webAppMotorElectrico/adminUsers", result);
        verify(loginService).deleteUser("username");
    }
}
