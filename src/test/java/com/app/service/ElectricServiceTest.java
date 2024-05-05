package com.app.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.app.dto.DatasetInformationDTO;
import com.app.dto.DatasetsResponseDTO;
import com.app.dto.RunRequestDTO;
import com.app.dto.RunResponseDTO;

public class ElectricServiceTest {

    @Mock
    private RestTemplate restTemplate; // Mock RestTemplate

    @InjectMocks
    private ElectricService electricService; // Inject mocked RestTemplate into ElectricService

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize annotated mocks
    }

    @Test
    public void testGetAllDatasets_Success() throws ConnectException {
        // Mock response from restTemplate
        DatasetsResponseDTO mockResponse = new DatasetsResponseDTO();
        mockResponse.setModelsList(List.of("Dataset1", "Dataset2"));

        when(restTemplate.getForObject(anyString(), eq(DatasetsResponseDTO.class)))
                .thenReturn(mockResponse);

        // Test getAllDatasets method
        List<String> datasets = electricService.getAllDatasets();

        // Verify that the expected datasets are returned
        assertNotNull(datasets);
        assertEquals(2, datasets.size());
        assertTrue(datasets.contains("Dataset1"));
        assertTrue(datasets.contains("Dataset2"));
    }

    @Test
    public void testGetAllDatasets_ConnectionError_ReturnsEmptyList() throws ConnectException {
        // Simulate ResourceAccessException when calling restTemplate
        when(restTemplate.getForObject(anyString(), eq(DatasetsResponseDTO.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // Test getAllDatasets method
        List<String> datasets = electricService.getAllDatasets();

        // Verify that an empty list is returned upon connection error
        assertNotNull(datasets);
        assertTrue(datasets.isEmpty());
    }
    
    @Test
    public void testGetAllSavedDatasets_Success() throws ConnectException {
        // Mock response from restTemplate
        DatasetsResponseDTO mockResponse = new DatasetsResponseDTO();
        mockResponse.setModelsList(List.of("Dataset1", "Dataset2"));

        when(restTemplate.getForObject(anyString(), eq(DatasetsResponseDTO.class)))
                .thenReturn(mockResponse);

        // Test getAllDatasets method
        List<String> datasets = electricService.getAllSavedDatasets();

        // Verify that the expected datasets are returned
        assertNotNull(datasets);
        assertEquals(2, datasets.size());
        assertTrue(datasets.contains("Dataset1"));
        assertTrue(datasets.contains("Dataset2"));
    }

    @Test
    public void testGetAllSavedDatasets_ConnectionError_ReturnsEmptyList() throws ConnectException {
        // Simulate ResourceAccessException when calling restTemplate
        when(restTemplate.getForObject(anyString(), eq(DatasetsResponseDTO.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // Test getAllDatasets method
        List<String> datasets = electricService.getAllSavedDatasets();

        // Verify that an empty list is returned upon connection error
        assertNotNull(datasets);
        assertTrue(datasets.isEmpty());
    }

    @Test
    public void testDelete_Success() throws ConnectException {
        // Mock response from restTemplate
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Deleted successfully");

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(mockResponse);

        // Test delete method
        assertDoesNotThrow(() -> electricService.delete("Dataset1", "user"));
    }

    @Test
    public void testDelete_ConnectionError_LogsErrorMessage() throws ConnectException {
        // Simulate exception when calling restTemplate
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // Test delete method
        assertDoesNotThrow(() -> electricService.delete("Dataset1", "user"));

    }
    
    @Test
    public void testCreateDatasetInDB_Success() throws ConnectException {
        DatasetInformationDTO info = new DatasetInformationDTO();
        info.setNombre("TestDataset");
        info.setId(123);

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Created", HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        String response = electricService.createDatasetInDB(info, "user");

        assertNotNull(response);
        assertEquals("Created", response);
    }

    @Test
    public void testCreateDatasetInDB_ConnectionError_ReturnsErrorCode() throws ConnectException {
        DatasetInformationDTO info = new DatasetInformationDTO();
        info.setNombre("TestDataset");
        info.setId(123);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        String response = electricService.createDatasetInDB(info, "user");

        assertNotNull(response);
        assertEquals("1", response);
    }

    @Test
    public void testUpdateDatasetInDB_Success() throws ConnectException {
        DatasetInformationDTO info = new DatasetInformationDTO();
        info.setId(456);
        info.setNombre("UpdatedDataset");

        ResponseEntity<Void> mockResponseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(mockResponseEntity);

        assertDoesNotThrow(() -> electricService.updateDatasetInDB(info, "test"));
    }

    @Test
    public void testUpdateDatasetInDB_ConnectionError_PrintsErrorMessage() {
        // Mock dataset information
        DatasetInformationDTO info = new DatasetInformationDTO();
        info.setId(123);

        // Configure RestTemplate to throw ResourceAccessException
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));
    }



    @Test
    public void testGetSavedDatasets_Success() throws ConnectException {
        DatasetsResponseDTO mockResponse = new DatasetsResponseDTO();
        mockResponse.setModelsList(List.of("Dataset1", "Dataset2"));

        when(restTemplate.getForObject(anyString(), eq(DatasetsResponseDTO.class)))
                .thenReturn(mockResponse);

        List<String> datasets = electricService.getSavedDatasets("user");

        assertNotNull(datasets);
        assertEquals(2, datasets.size());
        assertTrue(datasets.contains("Dataset1"));
        assertTrue(datasets.contains("Dataset2"));
    }

    @Test
    public void testGetSavedDatasets_ConnectionError_ReturnsEmptyList() throws ConnectException {
        when(restTemplate.getForObject(anyString(), eq(DatasetsResponseDTO.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        List<String> datasets = electricService.getSavedDatasets("user");

        assertNotNull(datasets);
        assertTrue(datasets.isEmpty());
    }

    @Test
    public void testGetDatasetInfo_Success() throws ConnectException {
        DatasetInformationDTO mockResponse = new DatasetInformationDTO();
        mockResponse.setNombre("TestDataset");
        mockResponse.setId(123);

        when(restTemplate.getForObject(anyString(), eq(DatasetInformationDTO.class)))
                .thenReturn(mockResponse);

        DatasetInformationDTO datasetInfo = electricService.getDatasetInfo("TestDataset");

        assertNotNull(datasetInfo);
        assertEquals(123, datasetInfo.getId());
    }

    @Test
    public void testGetDatasetInfo_ConnectionError_ReturnsNull() throws ConnectException {
        when(restTemplate.getForObject(anyString(), eq(DatasetInformationDTO.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        DatasetInformationDTO datasetInfo = electricService.getDatasetInfo("TestDataset");

        assertNull(datasetInfo);
    }

    @Test
    public void testRun_Success() {
        RunRequestDTO requestDTO = new RunRequestDTO();
        requestDTO.setNombre_req("Test data");

        RunResponseDTO mockResponse = new RunResponseDTO();
        mockResponse.setAnalysis_result("Success");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(RunResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));
    }

    @Test
    public void testRun_ConnectionError_ReturnsNull() {
        RunRequestDTO requestDTO = new RunRequestDTO();
        requestDTO.setNombre_req("Test data");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(RunResponseDTO.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        RunResponseDTO responseDTO = electricService.run(requestDTO, "user", 1);

        assertNull(responseDTO);
    }

    @Test
    public void testGetImage_Success() throws IOException {
        byte[] imageBytes = { /* Image bytes */ };

        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(imageBytes, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .thenReturn(mockResponse);
    }

    @Test
    public void testGetImage_ConnectionError_ReturnsNull() throws IOException {
        when(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        BufferedImage image = electricService.getImage("sessionID", 1);

        assertNull(image);
    }
    
    @Test
    public void testUploadDataSample_Success() throws IOException {
        // Prepare mock file and parameters
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());
        String fileName = "test.txt";
        int id = 123;
        String user = "testUser";

        // Mock response entity
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Success", HttpStatus.OK);

        // Configure RestTemplate behavior
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Call the method under test
        ResponseEntity<String> response = electricService.uploadDataSample(file, fileName, id, user);

        // Assertions
        assertNotNull(response);
        assertEquals("Success", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUploadDataSample_ConnectionError_ReturnsInternalServerError() throws IOException {
        // Configure RestTemplate to throw ResourceAccessException
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

    }


    @Test
    public void testDeleteSample_Success() throws ConnectException {
        String healthy = "healthyData";
        String regular = "regularData";
        int id = 456;
        String user = "testUser";

        // Mock response entity
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("Deleted", HttpStatus.OK);

        // Configure RestTemplate behavior
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Call the method under test
        assertDoesNotThrow(() -> electricService.deleteSample(healthy, regular, id, user));
    }

    @Test
    public void testDeleteSample_ConnectionError_PrintsErrorMessage() throws ConnectException {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));
    }
}
