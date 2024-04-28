package com.app.service;

import com.app.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LoginServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUserInDB_Success() throws ConnectException {
        UserDTO userDTO = new UserDTO(); // Crea un objeto UserDTO para simular datos
        userDTO.setUsuario("testUser");

        // Configurar el comportamiento esperado del restTemplate para postForEntity
        ResponseEntity<String> responseEntity = new ResponseEntity<>("OK", HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        String result = loginService.createUserInDB(userDTO);

        assertEquals("OK", result);
    }

    @Test
    public void testCreateUserInDB_ResourceAccessException() throws ConnectException {
        UserDTO userDTO = new UserDTO(); // Crea un objeto UserDTO para simular datos
        userDTO.setUsuario("testUser");

        // Configurar el comportamiento esperado del restTemplate para postForEntity
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        String result = loginService.createUserInDB(userDTO);

        assertEquals("1", result);
    }

    @Test
    public void testCheckUserInDB_Success() throws ConnectException {
        String usuario = "testUser";

        // Configurar el comportamiento esperado del restTemplate para getForEntity
        ResponseEntity<String> responseEntity = new ResponseEntity<>("userExists", HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(responseEntity);

        String result = loginService.checkUserInDB(usuario);

        assertEquals("userExists", result);
    }
    
    @Test
    public void testCheckUserInDB_ResourceAccessException() throws ConnectException {
        String usuario = "testUser";
        String apiUrl = "http://127.0.0.1:5000/checkUser/" + usuario;

        when(restTemplate.getForEntity(eq(apiUrl), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        String result = loginService.checkUserInDB(usuario);

        assertEquals("1", result);
    }

    @Test
    public void testGetAllUsers_Success() throws ConnectException {
        String url = "http://127.0.0.1:5000/getAllUsers";

        UserDTO[] userDTOs = new UserDTO[2];
        userDTOs[0] = new UserDTO();
        userDTOs[1] = new UserDTO();

        // Configurar el comportamiento esperado del restTemplate para getForEntity
        ResponseEntity<UserDTO[]> responseEntity = new ResponseEntity<>(userDTOs, HttpStatus.OK);
        when(restTemplate.getForEntity(url, UserDTO[].class))
                .thenReturn(responseEntity);

        List<UserDTO> result = loginService.getAllUsers();

        assertEquals(2, result.size());
    }
    
    @Test
    public void testGetCurrentUser_Success() throws ConnectException {
        String usuario = "testUser";
        String url = "http://127.0.0.1:5000/getUser/" + usuario;

        UserDTO userDTO = new UserDTO();
        when(restTemplate.getForObject(url, UserDTO.class))
                .thenReturn(userDTO);

        UserDTO result = loginService.getCurrentUser(usuario);

        assertNotNull(result);
    }

    @Test
    public void testUpdateCurrentUser_Success() throws ConnectException {
        UserDTO userToUpdate = new UserDTO();
        userToUpdate.setNombre("New Name");

        String url = "http://127.0.0.1:5000/updateUser/" + userToUpdate.getUsuario();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(responseEntity);

        assertDoesNotThrow(() -> loginService.updateCurrentUser(userToUpdate));
    }

    @Test
    public void testDeleteUser_Success() throws ConnectException {
        String userToDelete = "testUser";
        String url = "http://127.0.0.1:5000/deleteUser/" + userToDelete;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        when(restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class))
                .thenReturn(responseEntity);

        assertDoesNotThrow(() -> loginService.deleteUser(userToDelete));
    }
}
