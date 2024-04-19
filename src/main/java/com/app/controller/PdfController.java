package com.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.app.constants.MappingConstants;
import com.app.dto.InterpretabilityReportDTO;
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

		BufferedImage img1 = electricService.getImage(this.sessionActual, 5);
		BufferedImage img2 = electricService.getImage(this.sessionActual, 7);
		BufferedImage img3 = electricService.getImage(this.sessionActual, 6);
		BufferedImage img4 = electricService.getImage(this.sessionActual, 8);

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

		BufferedImage[] images = { img1, img2, img3, img4 };
		String[] titles = { this.getMessage("view.exp.matrix.two"), this.getMessage("view.exp.matrix.one"),
				this.getMessage("view.exp.matrix.four"), this.getMessage("view.exp.matrix.three") };

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

			// Actualizar la posición Y para el siguiente título e imagen
			yPosition -= 10; // Espacio entre el título y la imagen

			// Imagen centrada
			PDImageXObject pdImage = null;
			if (images[i] != null) {
				pdImage = LosslessFactory.createFromImage(document, images[i]);
				float imageWidth = pdImage.getWidth() * scale;
				float imageHeight = pdImage.getHeight() * scale;
				float imageX = (page.getMediaBox().getWidth() - imageWidth) / 2;
				float imageY = yPosition - imageHeight;
				contentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);
			}

			// Actualizar la posición Y para el siguiente título e imagen
			yPosition -= 20 + (pdImage.getHeight() * scale) + 20; // Espacio entre imágenes
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

	@PostMapping("/generateInt")
	public ResponseEntity<String> generateInt(HttpServletRequest request, @RequestBody InterpretabilityReportDTO data) throws IOException {
		Principal test = request.getUserPrincipal();

		this.sessionActual = test.getName();

		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());

		// Título principal centrado
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
		String mainTitle = this.getMessage("view.int.main.title");
		float mainTitleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(mainTitle) / 1000 * 18;
		float mainTitleX = (page.getMediaBox().getWidth() - mainTitleWidth) / 2;
		contentStream.beginText();
		contentStream.newLineAtOffset(mainTitleX, 750);
		contentStream.showText(mainTitle);
		contentStream.endText();
		
	    // Establecer márgenes
	    float margin = 72; // 1 pulgada = 72 puntos
	    float width = pageSize.getWidth() - (2 * margin);
	    float startX = margin;
	    float startY = pageSize.getHeight() - margin;
	    float leading = 15f;

		// Títulos e imágenes centradas
		float yPosition = 700;
		float scale = 0.4f;

		String outer_race_title = this.getMessage("view.int.out.uno") + data.getDetails_out().get(0) + " "
				+ this.getMessage("view.int.out.dos") + " " + data.getDetails_out().get(data.getDetails_out().size() - 1) + " "
				+ this.getMessage("view.int.out.tres");
		
		String inner_race_title = this.getMessage("view.int.inn.uno") + data.getDetails_in().get(0) + " "
				+ this.getMessage("view.int.out.dos") + " " + data.getDetails_in().get(data.getDetails_in().size() - 1) + " "
				+ this.getMessage("view.int.out.tres");
		
		String bearing_balls_title = this.getMessage("view.int.ball.uno") +data.getDetails_balls().get(0) + " "
				+ this.getMessage("view.int.out.dos") + " " + data.getDetails_balls().get(data.getDetails_balls().size() - 1) + " "
				+ this.getMessage("view.int.out.tres");
		
		String cage_title = this.getMessage("view.int.cage.uno") + data.getDetails_cage().get(0) + " "
				+ this.getMessage("view.int.out.dos") + " " + data.getDetails_cage().get(data.getDetails_cage().size() - 1) + " "
				+ this.getMessage("view.int.out.tres");

	    BufferedImage[] images = {
	            electricService.getImage(this.sessionActual, 1),
	            electricService.getImage(this.sessionActual, 2),
	            electricService.getImage(this.sessionActual, 3),
	            electricService.getImage(this.sessionActual, 4)
	        };
		String[] titles = { outer_race_title, inner_race_title,	bearing_balls_title, cage_title };

	    for (int i = 0; i < images.length; i++) {
	        // Si la imagen es nula, omitir su pintura
	        if (images[i] == null) {
	            continue;
	        }

	        // Si ya hemos agregado dos imágenes, creamos una nueva página
	        if (i == 2) {
	            document.addPage(new PDPage());
	            contentStream.close();
	            page = new PDPage(pageSize);
	            document.addPage(page);
	            contentStream = new PDPageContentStream(document, page);
	            yPosition = startY;
	        }

	        // Título de la imagen
	        String title = titles[i];
	        float titleFontSize = 12;

	        // Dividir el título en líneas para ajustarlo dentro del margen
	        List<String> titleLines = splitTextToFitWidth(title, PDType1Font.HELVETICA_BOLD, titleFontSize, width);

	        // Escribir el título de la imagen
	        contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);
	        contentStream.setLeading(leading);
	        for (String line : titleLines) {
	            contentStream.beginText();
	            contentStream.newLineAtOffset(startX, yPosition);
	            contentStream.showText(line);
	            contentStream.newLine();
	            contentStream.endText();
	            yPosition -= leading;
	        }
	        // Imagen centrada y más grande
	        PDImageXObject pdImage = LosslessFactory.createFromImage(document, images[i]);
	        float imageWidth = pdImage.getWidth() * scale;
	        float imageHeight = pdImage.getHeight() * scale;
	        float imageX = startX + (width - imageWidth) / 2;
	        float imageY = yPosition - imageHeight;
	        contentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);

	        // Actualizar la posición Y para el siguiente título e imagen
	        yPosition -= imageHeight + 20; // Espacio entre imágenes
	    }
	    
	    String finalText = "";
	    
		switch (data.getFault_info()) {
		case "A fault has been detected in an early stage":
			finalText =  this.getMessage("view.cont.fault.info.first");
			break;
		case "A fault has been detected in a medium stage":
			finalText =  this.getMessage("view.cont.fault.info.second");
			break;
		case "A fault has been detected in a last degradation stage":
			finalText = this.getMessage("view.cont.fault.info.third");
			break;

		}

	    // Dividir el texto final en líneas para ajustarlo dentro del margen
	    List<String> finalTextLines = splitTextToFitWidth(finalText, PDType1Font.HELVETICA, 12, width);

	    // Escribir el texto final
	    contentStream.setFont(PDType1Font.HELVETICA, 12);
	    contentStream.setLeading(leading);
	    for (String line : finalTextLines) {
	        contentStream.beginText();
	        contentStream.newLineAtOffset(startX, yPosition);
	        contentStream.showText(line);
	        contentStream.newLine();
	        contentStream.endText();
	        yPosition -= leading;
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
	
	private List<String> splitTextToFitWidth(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
	    List<String> lines = new ArrayList<>();
	    float textWidth = font.getStringWidth(text) / 1000 * fontSize;

	    // Verificar si el texto cabe en una línea
	    if (textWidth <= maxWidth) {
	        lines.add(text);
	        return lines;
	    }

	    StringBuilder line = new StringBuilder();
	    StringBuilder word = new StringBuilder();
	    float lineWidth = 0;

	    for (int i = 0; i < text.length(); i++) {
	        char c = text.charAt(i);
	        float charWidth = font.getStringWidth(String.valueOf(c)) / 1000 * fontSize;

	        if (c == ' ' || c == '\n') {
	            float wordWidth = font.getStringWidth(word.toString()) / 1000 * fontSize;
	            if (lineWidth + wordWidth > maxWidth) {
	                lines.add(line.toString());
	                line.setLength(0);
	                lineWidth = 0;
	            }
	            line.append(word);
	            line.append(c);
	            lineWidth += wordWidth + charWidth;
	            word.setLength(0);
	            if (c == '\n') {
	                // Si el caracter actual es un salto de línea, iniciar una nueva línea
	                lines.add(line.toString());
	                line.setLength(0);
	                lineWidth = 0;
	            }
	        } else {
	            word.append(c);
	        }
	    }

	    // Agregar la última palabra si existe
	    if (word.length() > 0) {
	        float wordWidth = font.getStringWidth(word.toString()) / 1000 * fontSize;
	        if (lineWidth + wordWidth > maxWidth) {
	            lines.add(line.toString());
	            line.setLength(0);
	        }
	        line.append(word);
	    }

	    // Agregar la última línea si existe
	    if (line.length() > 0) {
	        lines.add(line.toString());
	    }

	    return lines;
	}

}
