package com.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.ConnectException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;

import com.app.constants.ViewConstants;
import com.app.dto.DatasetInformationDTO;
import com.app.dto.DatasetsResponseDTO;
import com.app.dto.RunRequestDTO;
import com.app.dto.RunResponseDTO;
import com.app.dto.RunUploadFileRequestDTO;
import com.app.dto.UserDTO;
import com.app.service.ElectricService;
import com.app.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;

public class BaseControllerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ElectricService electricService;

    @Mock
    private LoginService loginService;

    @InjectMocks
    private BaseController baseController;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Principal principal;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
		MessageSource messageSource = mock(MessageSource.class);
	    when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
        .thenReturn("Mocked Message");
    }

    @Test
    public void testHome() throws Exception {
        // Prepare
        List<String> mockDatasets = new ArrayList<>();
        mockDatasets.add("dataset1");
        mockDatasets.add("dataset2");
        
        List<UserDTO> mockUsers = new ArrayList<>();
        UserDTO tmp1 = new UserDTO();
        tmp1.setUsuario("admin");
        UserDTO tmp2 = new UserDTO();
        tmp2.setUsuario("user");
        mockUsers.add(tmp1);
        mockUsers.add(tmp2);

        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("testUser");
        when(electricService.getAllDatasets()).thenReturn(mockDatasets);
        when(loginService.getAllUsers()).thenReturn(mockUsers);

        // Execute
        String result = baseController.home(model, request);

        // Verify
        assertEquals(ViewConstants.VIEW_HOME_PAGE, result);
        verify(model).addAttribute(eq("resultDatatest"), any(DatasetsResponseDTO.class));
        verify(model).addAttribute(eq("resultSavedDatatest"), any(DatasetsResponseDTO.class));
        verify(model).addAttribute(eq("loggedUser"), any(String.class));
    }

    @Test
    public void testDelete() throws ConnectException {
        // Prepare
        String item = "dataset1.csv";

        // Execute
        String result = baseController.delete(item);

        // Verify
        assertEquals(ViewConstants.REDIRECT_HOME_PAGE, result);
        verify(electricService).delete(eq(item), any(String.class));
    }
    
    @Test
    public void testPreload() throws Exception {
        // Mock dataset and selected model
        String selectedModel = "testDataset.csv";
        DatasetInformationDTO mockInfo = new DatasetInformationDTO();
        when(electricService.getDatasetInfo(anyString())).thenReturn(mockInfo);

        // Execute
        String result = baseController.preload(selectedModel, model);

        // Verify
        assertEquals(ViewConstants.VIEW_PRELOADED_PAGE, result);
        verify(model).addAttribute(eq("selectedModel"), eq("testDataset"));
        verify(model).addAttribute(eq("formData"), eq(mockInfo));
        verify(model).addAttribute(eq("formDataCheck"), eq(mockInfo));
    }
    
    @Test
    public void testRunPreloaded() throws Exception {
        // Mock input data and dependencies
        RunRequestDTO mockData2Run = new RunRequestDTO();
        mockData2Run.setNombre_req("testDataset");
        mockData2Run.setAnalyzed_number_req(30);
        mockData2Run.setHealthy_number_req(300);
		DatasetInformationDTO mockInfo = new DatasetInformationDTO();
		mockInfo.setMax_to_check(900);
		
		BufferedImage mockImage1 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		
		RunResponseDTO mockRunResponse = new RunResponseDTO();
		List<String> mockListUno = new ArrayList<>();
		mockListUno.add("Outer_race");
		mockListUno.add("Cage");
		List<List<Double>> mockListDos = new ArrayList<>();
		List<Double> list1 = Arrays.asList(1.38, 2.56, 4.3);
		List<Double> list2 = Arrays.asList(1.38, 2.56, 4.3);
		List<Double> expectedResultTimeReport = Arrays.asList(1.0, 0.99, -0.92, 0.97, 0.56, 0.96, 0.67, 0.71);
		List<Double> expectedResultFreqReport = Arrays.asList(1.0, 0.69, 0.61, 0.84, 0.63, 0.53, 0.14, -0.03, 0.8, 0.01, 0.05);
		mockListDos.add(list1);
		mockListDos.add(list2);
		mockRunResponse.setAnalysis_result("A fault has been detected");
		mockRunResponse.setFault_detected(true);
		mockRunResponse.setFault_info("A fault has been detected in an early stage");
		mockRunResponse.setFault_type(mockListUno);
		mockRunResponse.setFault_details(mockListDos);
		mockRunResponse.setResultFreqReport(expectedResultFreqReport);
		mockRunResponse.setResultTimeReport(expectedResultTimeReport);
		
        when(electricService.getDatasetInfo(mockData2Run.getNombre_req())).thenReturn(mockInfo);
        when(electricService.run(any(RunRequestDTO.class), anyString(), anyInt())).thenReturn(mockRunResponse);
        when(electricService.getImage(anyString(), anyInt())).thenReturn(mockImage1);

        MessageSource ms = mock(MessageSource.class);
        baseController.setMessageSource(ms);
        
        // Execute
        String result = baseController.runPreloaded(mockData2Run, model);

        // Verify
        assertEquals(ViewConstants.VIEW_PRELOADED_PAGE, result);
        verify(model).addAttribute(eq("selectedModel"), eq("testDataset"));
        verify(model).addAttribute("runResForm", mockRunResponse);
    }
    
    @Test
    public void testNewload() throws Exception {
        // Mock input data and dependencies
        String selectedSavedModel = "testModel.csv";
        DatasetInformationDTO mockInfo = new DatasetInformationDTO();
        when(electricService.getDatasetInfo(anyString())).thenReturn(mockInfo);

        // Execute
        String result = baseController.newload(selectedSavedModel, model);

        // Verify
        assertEquals(ViewConstants.VIEW_NEWLOADED_PAGE, result);
        verify(model).addAttribute(eq("selectedSavedModel"), eq("testModel"));
        verify(model).addAttribute(eq("formData"), eq(mockInfo));
    }
    
    @Test
    public void testSaveDataset() throws Exception {
        // Mock input data and dependencies
        DatasetInformationDTO mockInfo = new DatasetInformationDTO();
        mockInfo.setBearing_type("test");
		mockInfo.setBpfi(300.0);
		mockInfo.setBpfo(300.0);
		mockInfo.setBsf(300.0);
		mockInfo.setCarga(300.0);
		mockInfo.setFtf(300.0);
		mockInfo.setNombre("test");
		mockInfo.setSampling_frequency(300.0);
		mockInfo.setShaft_frequency(300.0);
		mockInfo.setFiles_added(0);
		mockInfo.setId(77);
        when(electricService.createDatasetInDB(any(DatasetInformationDTO.class), anyString())).thenReturn("");
        when(electricService.getDatasetInfo(mockInfo.getNombre())).thenReturn(mockInfo);
        
        MessageSource ms = mock(MessageSource.class);
        baseController.setMessageSource(ms);
        
        // Execute
        String result = baseController.saveDataset(mockInfo, model);

        // Verify
        assertEquals(ViewConstants.REDIRECT_NEWLOADED_PAGE + mockInfo.getNombre(), result);
    }
    
    @Test
    public void testSaveDataset2() throws Exception {
        // Mock input data and dependencies
        DatasetInformationDTO mockInfo = new DatasetInformationDTO();
        mockInfo.setBearing_type("");
		mockInfo.setBpfi(0.0);
		mockInfo.setBpfo(0.0);
		mockInfo.setBsf(0.0);
		mockInfo.setCarga(0.0);
		mockInfo.setFtf(0.0);
		mockInfo.setNombre("");
		mockInfo.setSampling_frequency(0.0);
		mockInfo.setShaft_frequency(0.0);
		mockInfo.setFiles_added(0);
		mockInfo.setId(null);
        when(electricService.createDatasetInDB(any(DatasetInformationDTO.class), anyString())).thenReturn("");
        when(electricService.getDatasetInfo(mockInfo.getNombre())).thenReturn(mockInfo);

        MessageSource ms = mock(MessageSource.class);
        baseController.setMessageSource(ms);
        
        // Execute
        String result = baseController.saveDataset(mockInfo, model);

        // Verify
        assertEquals(ViewConstants.REDIRECT_NEWLOADED_PAGE + mockInfo.getNombre(), result);
    }
    
    @Test
    public void testSaveDataset3() throws Exception {
        // Mock input data and dependencies
        DatasetInformationDTO mockInfo = new DatasetInformationDTO();
        mockInfo.setBearing_type("test");
		mockInfo.setBpfi(300.0);
		mockInfo.setBpfo(300.0);
		mockInfo.setBsf(300.0);
		mockInfo.setCarga(300.0);
		mockInfo.setFtf(300.0);
		mockInfo.setNombre("test");
		mockInfo.setSampling_frequency(300.0);
		mockInfo.setShaft_frequency(300.0);
		mockInfo.setFiles_added(0);
		mockInfo.setId(null);
        when(electricService.createDatasetInDB(any(DatasetInformationDTO.class), anyString())).thenReturn("");
        when(electricService.getDatasetInfo(mockInfo.getNombre())).thenReturn(mockInfo);
        
        MessageSource ms = mock(MessageSource.class);
        baseController.setMessageSource(ms);
        
        // Execute
        String result = baseController.saveDataset(mockInfo, model);

        // Verify
        assertEquals(ViewConstants.REDIRECT_NEWLOADED_PAGE + mockInfo.getNombre(), result);
    }
    
    @Test
    public void testSaveDataset4() throws ConnectException {
        // Setup
        DatasetInformationDTO mockInfo = new DatasetInformationDTO();
        mockInfo.setNombre("New Dataset");

        // Test
        String result = baseController.saveDataset(mockInfo, model);

        // Verify
        assertEquals(ViewConstants.REDIRECT_NEWLOADED_PAGE + mockInfo.getNombre(), result);
    }
    
    @Test
    public void testGuardarNewArchivos() throws Exception {
        // Mock input data and dependencies
        MockMultipartFile mockFile = new MockMultipartFile("dataFiles", "test.csv", "text/csv", "test data".getBytes());
        when(electricService.uploadDataSample(any(), anyString(), anyInt(), anyString())).thenReturn(ResponseEntity.accepted().build());

        // Execute
        String result = baseController.guardarNewArchivos(mockFile, "test", 1, model);

        // Verify
        assertEquals(ViewConstants.REDIRECT_NEWLOADED_PAGE + "test", result);
    }
    
    @Test
    public void testDeleteSample() throws Exception {
        // Mock input data and dependencies
        String nombre = "test";
        int id = 1;

        // Execute
        String result = baseController.deleteSample(nombre, id);

        // Verify
        assertEquals(ViewConstants.REDIRECT_NEWLOADED_PAGE + "test", result);
        verify(electricService).deleteSample(eq("healthytest.csv"), eq("test.csv"), eq(1), anyString());
    }
    
    @Test
    public void testRunNewloadedWithoutFile() throws Exception {
        // Mock input data and dependencies
        RunRequestDTO mockData2Run = new RunRequestDTO();
        mockData2Run.setNombre_req("testDataset");
        mockData2Run.setAnalyzed_number_req(30);
        mockData2Run.setHealthy_number_req(300);
		DatasetInformationDTO mockInfo = new DatasetInformationDTO();
		mockInfo.setMax_to_check(900);
		
		BufferedImage mockImage1 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		
		RunResponseDTO mockRunResponse = new RunResponseDTO();
		List<String> mockListUno = new ArrayList<>();
		mockListUno.add("Outer_race");
		mockListUno.add("Cage");
		List<List<Double>> mockListDos = new ArrayList<>();
		List<Double> list1 = Arrays.asList(1.38, 2.56, 4.3);
		List<Double> list2 = Arrays.asList(1.38, 2.56, 4.3);
		List<Double> expectedResultTimeReport = Arrays.asList(1.0, 0.99, -0.92, 0.97, 0.56, 0.96, 0.67, 0.71);
		List<Double> expectedResultFreqReport = Arrays.asList(1.0, 0.69, 0.61, 0.84, 0.63, 0.53, 0.14, -0.03, 0.8, 0.01, 0.05);
		mockListDos.add(list1);
		mockListDos.add(list2);
		mockRunResponse.setAnalysis_result("A fault has been detected");
		mockRunResponse.setFault_detected(true);
		mockRunResponse.setFault_info("A fault has been detected in an early stage");
		mockRunResponse.setFault_type(mockListUno);
		mockRunResponse.setFault_details(mockListDos);
		mockRunResponse.setResultFreqReport(expectedResultFreqReport);
		mockRunResponse.setResultTimeReport(expectedResultTimeReport);
		
        when(electricService.getDatasetInfo(mockData2Run.getNombre_req())).thenReturn(mockInfo);
        when(electricService.run(any(RunRequestDTO.class), anyString(), anyInt())).thenReturn(mockRunResponse);
        when(electricService.getImage(anyString(), anyInt())).thenReturn(mockImage1);

        MessageSource ms = mock(MessageSource.class);
        baseController.setMessageSource(ms);
        
        // Execute
        String result = baseController.runNewload(null, mockData2Run, null, model);

        // Verify
        assertEquals(ViewConstants.VIEW_NEWLOADED_PAGE, result);
        verify(model).addAttribute(eq("selectedModel"), eq("testDataset"));
        verify(model).addAttribute("runResForm", mockRunResponse);
    }
    
    @Test
    public void testRunNewloadedWithFile() throws Exception {
        // Mock input data and dependencies
    	MockMultipartFile mockFile = new MockMultipartFile("dataFiles", "test.csv", "text/csv", "test data".getBytes());
    	RunRequestDTO mockData = new RunRequestDTO();
        mockData.setNombre_req("testDataset");
    	RunUploadFileRequestDTO mockData2Run = new RunUploadFileRequestDTO();
        mockData2Run.setNombre_req_upp("testDataset");
        mockData2Run.setHealthy_number_req_upp(300);
		DatasetInformationDTO mockInfo = new DatasetInformationDTO();
		mockInfo.setMax_to_check(900);
		
		BufferedImage mockImage1 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		
		RunResponseDTO mockRunResponse = new RunResponseDTO();
		List<String> mockListUno = new ArrayList<>();
		mockListUno.add("Outer_race");
		mockListUno.add("Cage");
		List<List<Double>> mockListDos = new ArrayList<>();
		List<Double> list1 = Arrays.asList(1.38, 2.56, 4.3);
		List<Double> list2 = Arrays.asList(1.38, 2.56, 4.3);
		List<Double> expectedResultTimeReport = Arrays.asList(1.0, 0.99, -0.92, 0.97, 0.56, 0.96, 0.67, 0.71);
		List<Double> expectedResultFreqReport = Arrays.asList(1.0, 0.69, 0.61, 0.84, 0.63, 0.53, 0.14, -0.03, 0.8, 0.01, 0.05);
		mockListDos.add(list1);
		mockListDos.add(list2);
		mockRunResponse.setAnalysis_result("A fault has been detected");
		mockRunResponse.setFault_detected(true);
		mockRunResponse.setFault_info("A fault has been detected in an medium stage");
		mockRunResponse.setFault_type(mockListUno);
		mockRunResponse.setFault_details(mockListDos);
		mockRunResponse.setResultFreqReport(expectedResultFreqReport);
		mockRunResponse.setResultTimeReport(expectedResultTimeReport);
		
        when(electricService.getDatasetInfo(mockData.getNombre_req())).thenReturn(mockInfo);
        when(electricService.uploadDataSample(any(), anyString(), anyInt(), anyString())).thenReturn(ResponseEntity.ok().build());
        when(electricService.run(any(RunRequestDTO.class), anyString(), anyInt())).thenReturn(mockRunResponse);
        when(electricService.getImage(anyString(), anyInt())).thenReturn(mockImage1);

        MessageSource ms = mock(MessageSource.class);
        baseController.setMessageSource(ms);
        
        // Execute
        String result = baseController.runNewload(mockFile, mockData, mockData2Run, model);

        // Verify
        assertEquals(ViewConstants.VIEW_NEWLOADED_PAGE, result);
        verify(model).addAttribute(eq("selectedModel"), eq("testDataset"));
        verify(model).addAttribute("runResForm", mockRunResponse);
    }
    
    @Test
    public void testRunNewloadedWithFile2() throws Exception {
        // Mock input data and dependencies
    	MockMultipartFile mockFile = new MockMultipartFile("dataFiles", "test.h5", "text/csv", "test data".getBytes());
    	RunRequestDTO mockData = new RunRequestDTO();
        mockData.setNombre_req("testDataset");
    	RunUploadFileRequestDTO mockData2Run = new RunUploadFileRequestDTO();
        mockData2Run.setNombre_req_upp("testDataset");
        mockData2Run.setHealthy_number_req_upp(300);
		DatasetInformationDTO mockInfo = new DatasetInformationDTO();
		mockInfo.setMax_to_check(900);
		
        when(electricService.getDatasetInfo(mockData.getNombre_req())).thenReturn(mockInfo);

        MessageSource ms = mock(MessageSource.class);
        baseController.setMessageSource(ms);
        
        // Execute
        String result = baseController.runNewload(mockFile, mockData, mockData2Run, model);

        // Verify
        assertEquals(ViewConstants.VIEW_NEWLOADED_PAGE, result);
        verify(model).addAttribute("errors", "nullh5 null");
        verify(model).addAttribute("formData", mockInfo);
        verify(model).addAttribute("formDataCheckNew", mockInfo);
        verify(model).addAttribute("loggedUser", "");
    }
}
