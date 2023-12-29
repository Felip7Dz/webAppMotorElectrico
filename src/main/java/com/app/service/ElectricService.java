package com.app.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.app.dto.DatasetInformationDTO;
import com.app.dto.DatasetsResponseDTO;
import com.app.dto.RunRequestDTO;
import com.app.dto.RunResponseDTO;

@Service
public class ElectricService {

	private final RestTemplate restTemplate;

	public ElectricService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public List<String> getAllDatasets() throws ConnectException {
		String url = "http://127.0.0.1:5000/getModelsList";
		DatasetsResponseDTO response = null;

		try {
			response = restTemplate.getForObject(url, DatasetsResponseDTO.class);
		} catch (ResourceAccessException e) {
			System.err.println("Error de conexión con la API: " + e.getMessage());
			return Collections.emptyList();
		}

		if (response != null) {
			return response.getModelsList();
		} else {
			return Collections.emptyList();
		}
	}

	public List<String> getAllSavedDatasets() throws ConnectException {
		String url = "http://127.0.0.1:5000/getSavedModelsList";
		DatasetsResponseDTO response = null;

		try {
			response = restTemplate.getForObject(url, DatasetsResponseDTO.class);
		} catch (ResourceAccessException e) {
			System.err.println("Error de conexión con la API: " + e.getMessage());
			return Collections.emptyList();
		}

		if (response != null) {
			return response.getModelsList();
		} else {
			return Collections.emptyList();
		}
	}

	public void delete(String item) throws ConnectException {
		String url = "http://127.0.0.1:5000/deleteDataset";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		DatasetInformationDTO dto = new DatasetInformationDTO();
		dto.setNombre(item);

		HttpEntity<DatasetInformationDTO> requestEntity = new HttpEntity<>(dto, headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
			System.out.println("Respuesta del servidor: " + response.getBody());
		} catch (Exception e) {
			System.err.println("Error al llamar al método deleteDataset: " + e.getMessage());
		}
	}

	public List<String> getData() throws ConnectException {
		String url = "http://127.0.0.1:5000/getData";
		DatasetsResponseDTO response = null;

		try {
			response = restTemplate.getForObject(url, DatasetsResponseDTO.class);
		} catch (ResourceAccessException e) {
			System.err.println("Error de conexión con la API: " + e.getMessage());
			return Collections.emptyList();
		}

		if (response != null) {
			return response.getModelsList();
		} else {
			return Collections.emptyList();
		}
	}

	public ResponseEntity<String> uploadDataset(MultipartFile file) {
		String apiUrl = "http://127.0.0.1:5000/guardar_archivo";

		try {
			ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			};

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("archivo", fileResource);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			return restTemplate.postForEntity(apiUrl, requestEntity, String.class);

		} catch (IOException e) {
			System.err.println("Error de E/S al leer el archivo: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo");
		}
	}

	public DatasetInformationDTO getDatasetInfo(String name) throws ConnectException {
		String url = "http://127.0.0.1:5000/getDatasetByName/" + name;
		DatasetInformationDTO response = null;

		try {
			response = restTemplate.getForObject(url, DatasetInformationDTO.class);
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

	public String createDatasetInDB(DatasetInformationDTO info) throws ConnectException {
		String url = "http://127.0.0.1:5000/createDataset";
		String response = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<DatasetInformationDTO> request = new HttpEntity<>(info, headers);

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

	public void updateDatasetInDB(DatasetInformationDTO info) throws ConnectException {
		String url = "http://127.0.0.1:5000/updateDataset/" + info.getId();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<DatasetInformationDTO> requestEntity = new HttpEntity<>(info, headers);

		RestTemplate restTemplate = new RestTemplate();

		try {
			restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
			System.out.println("Dataset actualizado correctamente");
		} catch (Exception e) {
			System.err.println("Error al actualizar el dataset: " + e.getMessage());
		}
	}

	public RunResponseDTO run(RunRequestDTO data2Run, String sess, int flag) {
		String url = "http://127.0.0.1:5000/analyze_data/" + sess + "/" + flag;

		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    HttpEntity<RunRequestDTO> requestEntity = new HttpEntity<>(data2Run, headers);

	    RestTemplate restTemplate = new RestTemplate();
	    RunResponseDTO responseDTO = null;

	    try {
	        ResponseEntity<RunResponseDTO> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, RunResponseDTO.class);
	        responseDTO = responseEntity.getBody();
	    } catch (Exception e) {
	        System.err.println("Error al correr la appt: " + e.getMessage());
	    }

	    return responseDTO;
	}
	
	public BufferedImage getImage(String sessionID, int flag) {
		String url = "http://127.0.0.1:5000/get_image/" + sessionID + "/" + flag;

		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class);
			byte[] imageBytes = responseEntity.getBody();

			InputStream inputStream = new ByteArrayInputStream(imageBytes);
			return ImageIO.read(inputStream);
		} catch (IOException e) {
			System.err.println("Error al recuperar las imagenes: " + e.getMessage());
			return null;
		}
	}
	
	public ResponseEntity<String> uploadDataSample(MultipartFile file, String fileName, int id) {
		String apiUrl = "http://127.0.0.1:5000/saveData/" + id;

		try {
			ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			};

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("archivo", fileResource);
			body.add("fileName", fileName);
			body.add("ogName", file.getOriginalFilename());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			return restTemplate.postForEntity(apiUrl, requestEntity, String.class);

		} catch (IOException e) {
			System.err.println("Error de E/S al leer el archivo: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo");
		}
	}
	
	public void deleteSample(String healthy, String regular, int id) throws ConnectException {
	    String url = "http://127.0.0.1:5000/deleteSample/" + id;

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    Map<String, String> parametros = new HashMap<>();
	    parametros.put("healthy", healthy);
	    parametros.put("regular", regular);

	    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(parametros, headers);

	    RestTemplate restTemplate = new RestTemplate();

	    try {
	        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
	        System.out.println("Respuesta del servidor: " + response.getBody());
	    } catch (Exception e) {
	        System.err.println("Error al llamar al método deleteDataset: " + e.getMessage());
	    }
	}
}
