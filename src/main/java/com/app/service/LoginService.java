package com.app.service;

import java.net.ConnectException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.app.dto.UserDTO;

@Service
public class LoginService {

	private final RestTemplate restTemplate;

	public LoginService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String createUserInDB(UserDTO info) throws ConnectException {
		String url = "http://127.0.0.1:5000/registerUser";
		String response = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<UserDTO> request = new HttpEntity<>(info, headers);

		try {
			ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
			response = responseEntity.getBody();
		} catch (ResourceAccessException e) {
			System.err.println("Error de conexión con la API: " + e.getMessage());
			return "1";
		}

		if (response != null) {
			return response;
		} else {
			return "1";
		}
	}

	public String checkUserInDB(String usuario) throws ConnectException{
		String apiUrl = "http://127.0.0.1:5000/checkUser/" + usuario;

		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
			String response = responseEntity.getBody();

			if (response != null) {
				return response;
			} else {
				throw new RuntimeException("La respuesta de la API es nula.");
			}
		} catch (ResourceAccessException e) {
			System.err.println("Error de conexión con la API: " + e.getMessage());
			return "1";
		} catch (Exception e) {
			System.err.println("Error al procesar la respuesta de la API: " + e.getMessage());
			return "1";
		}
	}

	public UserDTO getCurrentUser(String usuario) throws ConnectException{
		String url = "http://127.0.0.1:5000/getUser/" + usuario;
		UserDTO response = null;
		
		try {
			response = restTemplate.getForObject(url, UserDTO.class);
		} catch (ResourceAccessException e) {
			System.err.println("Error de conexión con la API: " + e.getMessage());
			return response;
		}

		if (response != null) {
			return response;
		} else {
			return response;
		}
	}

	public void updateCurrentUser(UserDTO user2Update) throws ConnectException{
		String url = "http://127.0.0.1:5000/updateUser/" + user2Update.getUsuario();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<UserDTO> requestEntity = new HttpEntity<>(user2Update, headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
			System.out.println("Dataset actualizado correctamente");
		} catch (Exception e) {
			System.err.println("Error al actualizar el dataset: " + e.getMessage());
		}		
	}
}
