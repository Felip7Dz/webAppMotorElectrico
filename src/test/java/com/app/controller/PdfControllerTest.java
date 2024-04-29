package com.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.app.dto.InterpretabilityReportDTO;
import com.app.dto.RunResponseDTO;
import com.app.service.ElectricService;

import jakarta.servlet.http.HttpServletRequest;

public class PdfControllerTest {

	@Mock
	private ElectricService electricService;

	@InjectMocks
	private PdfController pdfController;

	@BeforeEach
	public void setUp() {
		// Initialize mocks
		MockitoAnnotations.openMocks(this);
		
		// Mocks necesarios
		MessageSource messageSource = mock(MessageSource.class);
		
		// Create PdfController instance with mocked ElectricService
		pdfController = new PdfController(electricService);

		// Comportamiento de messageSource simulado
	    when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
        .thenReturn("Mocked Message");
	}

	@Test
	public void testGenerateExp_Success() throws IOException {
		// Prepare mock objects for the test
		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		Principal mockPrincipal = mock(Principal.class);
		RunResponseDTO mockData = mock(RunResponseDTO.class);
		BufferedImage mockImage1 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		BufferedImage mockImage2 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		BufferedImage mockImage3 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		BufferedImage mockImage4 = ImageIO.read(new File("src/test/resources/fail.jpg"));

		MessageSource ms = mock(MessageSource.class);
		pdfController.setMessageSource(ms);

		List<Double> expectedResultTimeReport = Arrays.asList(1.0, 0.99, -0.92, 0.97, 0.56, 0.96, 0.67, 0.71);
		List<Double> expectedResultFreqReport = Arrays.asList(1.0, 0.69, 0.61, 0.84, 0.63, 0.53, 0.14, -0.03, 0.8, 0.01,
				0.05);
		
		// Simulate behavior of mock objects
		when(mockRequest.getUserPrincipal()).thenReturn(mockPrincipal);
		when(mockPrincipal.getName()).thenReturn("admin");
		when(electricService.getImage("admin", 7)).thenReturn(mockImage1);
		when(electricService.getImage("admin", 6)).thenReturn(mockImage2);
		when(electricService.getImage("admin", 8)).thenReturn(mockImage3);
		when(electricService.getImage("admin", 5)).thenReturn(mockImage4);
		when(mockData.getResultTimeReport()).thenReturn(expectedResultTimeReport);
		when(mockData.getResultFreqReport()).thenReturn(expectedResultFreqReport);
		when(pdfController.getMessage("view.exp.main.title")).thenReturn("Main Title");
		when(pdfController.getMessage("view.exp.matrix.one")).thenReturn("Main Title");
		when(pdfController.getMessage("view.exp.matrix.two")).thenReturn("Main Title");
		when(pdfController.getMessage("view.exp.matrix.three")).thenReturn("Main Title");
		when(pdfController.getMessage("view.exp.matrix.four")).thenReturn("Main Title");


		// Call the method under test
		ResponseEntity<String> responseEntity = pdfController.generateExp(mockRequest, mockData);

		// Assertions and verifications
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		// Add more assertions as needed to verify the behavior of the method

		// Verify interactions with mock objects
		verify(electricService, times(4)).getImage(anyString(), anyInt());
	}
	
	@Test
	public void testGenerateInt_Success() throws IOException {
		// Prepare mock objects for the test
		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		Principal mockPrincipal = mock(Principal.class);
		
		InterpretabilityReportDTO mockData = mock(InterpretabilityReportDTO.class);
		
		BufferedImage mockImage1 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		BufferedImage mockImage2 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		BufferedImage mockImage3 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		BufferedImage mockImage4 = ImageIO.read(new File("src/test/resources/fail.jpg"));
		
		MessageSource ms = mock(MessageSource.class);
		pdfController.setMessageSource(ms);

		// Simulate behavior of mock objects
		when(mockRequest.getUserPrincipal()).thenReturn(mockPrincipal);
		when(mockPrincipal.getName()).thenReturn("admin");
		when(electricService.getImage("admin", 1)).thenReturn(mockImage1);
		when(electricService.getImage("admin", 2)).thenReturn(mockImage2);
		when(electricService.getImage("admin", 3)).thenReturn(mockImage3);
		when(electricService.getImage("admin", 4)).thenReturn(mockImage4);
		when(pdfController.getMessage("view.int.main.title")).thenReturn("Main Title");
		when(pdfController.getMessage("view.cont.fault.info.first")).thenReturn("Main Title");
		
		when(mockData.getDetails_in()).thenReturn(Arrays.asList(1.38, 2.56, 4.3));
		when(mockData.getDetails_out()).thenReturn(Arrays.asList(1.38, 2.56, 4.3));
		when(mockData.getDetails_balls()).thenReturn(Arrays.asList(1.38, 2.56, 4.3));
		when(mockData.getDetails_cage()).thenReturn(Arrays.asList(1.38, 2.56, 4.3));
		when(mockData.getAnalysis_result()).thenReturn("A fault has been detected");
		when(mockData.getFault_info()).thenReturn("A fault has been detected in an early stage");


		// Call the method under test
		ResponseEntity<String> responseEntity = pdfController.generateInt(mockRequest, mockData);

		// Assertions and verifications
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		// Add more assertions as needed to verify the behavior of the method

		// Verify interactions with mock objects
		verify(electricService, times(4)).getImage(anyString(), anyInt());
	}
}
