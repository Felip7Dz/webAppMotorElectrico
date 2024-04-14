package com.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Base64;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.constants.MappingConstants;
import com.app.dto.RunResponseDTO;
import com.app.service.ElectricService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PdfController {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ElectricService electricService;
	
	private String sessionActual = "";

	public PdfController(ElectricService electricService) {
		this.electricService = electricService;
	}
	
	public String getMessage(String messageKey) {
		return this.getMessage(messageKey, null);
	}
	
	public String getMessage(String messageKey, Object[] args) {
		return this.messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
	}

	@GetMapping(MappingConstants.GENERATE_PDF)
	public ResponseEntity<String> generatePdf(HttpServletRequest request, RunResponseDTO data) throws IOException {

	    Principal test = request.getUserPrincipal();
	    
	    this.sessionActual = test.getName();

	    BufferedImage img1 = electricService.getImage(this.sessionActual, 7);
	    BufferedImage img2 = electricService.getImage(this.sessionActual, 5);
	    BufferedImage img3 = electricService.getImage(this.sessionActual, 8);
	    BufferedImage img4 = electricService.getImage(this.sessionActual, 6);

	    PDDocument document = new PDDocument();
	    PDPage page = new PDPage();
	    document.addPage(page);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PDPageContentStream contentStream = new PDPageContentStream(document, page);

	    // Título principal centrado
	    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
	    String mainTitle = this.getMessage("view.exp.main.title");
	    float mainTitleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(mainTitle) / 1000 * 18;
	    float mainTitleX = (page.getMediaBox().getWidth() - mainTitleWidth) / 2;
	    contentStream.beginText();
	    contentStream.newLineAtOffset(mainTitleX, 750);
	    contentStream.showText(mainTitle);
	    contentStream.endText();

	    // Títulos e imágenes centradas
	    float yPosition = 700;
	    float scale = 0.4f;

	    BufferedImage[] images = {img1, img2, img3, img4};
	    String[] titles = {this.getMessage("view.exp.matrix.one"), this.getMessage("view.exp.matrix.two"), this.getMessage("view.exp.matrix.three"), this.getMessage("view.exp.matrix.four")};

	    for (int i = 0; i < images.length; i++) {
	        // Si ya hemos agregado dos imágenes, creamos una nueva página
	        if (i == 2) {
	            document.addPage(new PDPage());
	            contentStream.close();
	            contentStream = new PDPageContentStream(document, document.getPage(1));
	            yPosition = 700; // Reiniciamos la posición Y en la nueva página
	        }
	    	
	        // Título de la imagen
	        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
	        String title = titles[i];
	        float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 14;
	        float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2;
	        contentStream.beginText();
	        contentStream.newLineAtOffset(titleX, yPosition);
	        contentStream.showText(title);
	        contentStream.endText();

	        // Imagen centrada
	        PDImageXObject pdImage = null;
	        if(images[i] != null) {
	            pdImage = LosslessFactory.createFromImage(document, images[i]);
	            float imageWidth = pdImage.getWidth() * scale;
	            float imageHeight = pdImage.getHeight() * scale;
	            float imageX = (page.getMediaBox().getWidth() - imageWidth) / 2;
	            float imageY = yPosition;
	            contentStream.drawImage(pdImage, imageX, imageY - imageHeight, imageWidth, imageHeight);
	        }

	        // Actualizar la posición Y para el siguiente título e imagen
	        yPosition -= 20 + (pdImage.getHeight() * scale) + 20;
	    }

	    // Agregar los datos de tipo de fallo
	    contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
	    float yPositionText = yPosition - 20;
	    for (int i = 0; i < data.getFault_type().size(); i++) {
	        contentStream.beginText();
	        contentStream.newLineAtOffset(50, yPositionText - (i + 1) * 12);
	        contentStream.showText(data.getFault_type().get(i) + " " + this.getMessage("view.exp.fault.detected"));
	        contentStream.endText();
	    }

	    contentStream.close();

	    // Guardar y retornar el PDF
	    document.save(baos);
	    document.close();
		
		String pdfBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=generated.pdf");

		return ResponseEntity.ok().headers(headers).body(pdfBase64);
	}

}
