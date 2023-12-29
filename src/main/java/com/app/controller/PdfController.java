package com.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.app.dto.RunResponseDTO;
import com.app.service.ElectricService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PdfController {

	private final ElectricService electricService;
	private String language = "";
	private String sessionActual = "";

	public PdfController(ElectricService electricService) {
		this.electricService = electricService;
	}

	@GetMapping("/generate-pdf")
	public ResponseEntity<String> generatePdf(HttpServletRequest request, RunResponseDTO data) throws IOException {
		Locale currentLocale = RequestContextUtils.getLocale(request);
		this.sessionActual = request.getRequestedSessionId();
		this.language = currentLocale.getLanguage();

		BufferedImage img1 = electricService.getImage("null", 1);
		BufferedImage img2 = electricService.getImage("null", 2);

		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PDPageContentStream contentStream = new PDPageContentStream(document, page);

		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
		String texto = data.getFault_info();
		float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(texto) / 1000 * 12;
		float startX = (page.getMediaBox().getWidth() - textWidth) / 2;
		contentStream.beginText();
		contentStream.newLineAtOffset(startX, 700);
		contentStream.newLine();
		contentStream.showText(texto);
		contentStream.endText();

		float scale = 0.4f;
		PDImageXObject pdImage1 = LosslessFactory.createFromImage(document, img1);
		float image1Width = pdImage1.getWidth() * scale;
		float image1Height = pdImage1.getHeight() * scale;
		contentStream.drawImage(pdImage1, 50, 700 - image1Height - 3, image1Width, image1Height);

		float yPositionImage2 = 700 - image1Height - 20;
		PDImageXObject pdImage2 = LosslessFactory.createFromImage(document, img2);
		float image2Width = pdImage2.getWidth() * scale;
		float image2Height = pdImage2.getHeight() * scale;
		contentStream.drawImage(pdImage2, 50, yPositionImage2 - image2Height, image2Width, image2Height);

		float yPositionText = yPositionImage2 - image2Height - 20;
		contentStream.setFont(PDType1Font.HELVETICA, 12);

		for (int i = 0; i < data.getFault_type().size(); i++) {
		    contentStream.beginText();
		    contentStream.newLineAtOffset(50, yPositionText - (i + 1) * 12);
		    contentStream.showText(data.getFault_type().get(i) + " bearing failure detected.");
		    contentStream.endText();
		}
		
		contentStream.close();

		document.save(baos);
		document.close();

		String pdfBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=generated.pdf");

		return ResponseEntity.ok().headers(headers).body(pdfBase64);
	}

}
