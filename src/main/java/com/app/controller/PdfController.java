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
		int contador = 0;
		this.sessionActual = test.getName();

		BufferedImage img1 = electricService.getImage(this.sessionActual, 7);
		BufferedImage img2 = electricService.getImage(this.sessionActual, 6);
		BufferedImage img3 = electricService.getImage(this.sessionActual, 8);
		BufferedImage img4 = electricService.getImage(this.sessionActual, 5);

		String[] timeTitles = { "HI", "RMS", "Sk", "K", "CF", "SF", "IF", "MF" };
		String[] freqTitles = { "HI", "Fund. Filtered", "BPFO Filtered", "BPFI Filtered", "FTF Filtered",
				"BSF Filtered", "Fundamental", "BPFO", "BPFI", "FTF", "BSF" };

		String timeLabel = this.getMessage("view.exp.time.uno");
		String freqLabel = this.getMessage("view.exp.freq.uno");

		for (int i = 0; i < data.getResultTimeReport().size(); i++) {
			if (data.getResultTimeReport().get(i) > 0.59) {
				timeLabel += " " +  timeTitles[i] + " " + this.getMessage("view.exp.time.dos") + " "
						+ data.getResultTimeReport().get(i);
				contador++;
			}
			if (contador > 1 && data.getResultTimeReport().get(i) > 0.59
					&& i == data.getResultTimeReport().size() - 1) {
				timeLabel += ", " + this.getMessage("view.exp.time.tres") + " " + timeTitles[i] + " "
						+ this.getMessage("view.exp.time.dos") + " " + data.getResultTimeReport().get(i) + ".";
			}
			if (i == data.getResultTimeReport().size() - 1 && contador == 1) {
				timeLabel += ".";
			}
		}
		contador = 0;
		for (int i = 0; i < data.getResultFreqReport().size(); i++) {
			if (data.getResultFreqReport().get(i) > 0.59) {
				freqLabel += " " + freqTitles[i] + " " + this.getMessage("view.exp.time.dos") + " "
						+ data.getResultFreqReport().get(i);
				contador++;
			}
			if (contador > 1 && data.getResultFreqReport().get(i) > 0.59
					&& i == data.getResultFreqReport().size() - 1) {
				freqLabel += ", " + this.getMessage("view.exp.time.tres") + " " + freqTitles[i] + " "
						+ this.getMessage("view.exp.time.dos") + " " + data.getResultFreqReport().get(i) + ".";
			}
			if (i == data.getResultFreqReport().size() - 1 && contador == 1) {
				freqLabel += ".";
			}
		}

		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());

		// Título principal centrado
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
		String mainTitle = this.getMessage("view.exp.main.title");
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

		BufferedImage[] images = { img1, img2, img3, img4 };
		String[] titles = { this.getMessage("view.exp.matrix.one"), this.getMessage("view.exp.matrix.four"),
				this.getMessage("view.exp.matrix.three"), this.getMessage("view.exp.matrix.two") };

		for (int i = 0; i < images.length; i++) {

			// Si ya hemos agregado dos imágenes, creamos una nueva página
			if (i == 2) {
				contentStream.close();
				page = new PDPage(pageSize);
				document.addPage(page);
				contentStream = new PDPageContentStream(document, page);
				yPosition = startY;
			}

			// Imagen centrada
			PDImageXObject pdImage = LosslessFactory.createFromImage(document, images[i]);
			float imageWidth = pdImage.getWidth() * scale;
			float imageHeight = pdImage.getHeight() * scale;
			float imageX = startX + (width - imageWidth) / 2;
			float imageY = 0;
			if (i == 1 || i == 3) {
				imageY = yPosition - imageHeight * 0.95f;
			}
			if (i == 0 || i == 2) {
				imageY = yPosition - imageHeight * 1.10f;
			}
			contentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);

			// Título de la imagen
			String title = titles[i];
			float titleFontSize = 12;

			if (i == 0) {
				List<String> titleTime = splitTextToFitWidth(timeLabel, PDType1Font.HELVETICA_BOLD, titleFontSize,
						width);
				contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);
				contentStream.setLeading(leading);
				for (int j = 0; j < titleTime.size(); j++) {
					String line = titleTime.get(j);
					contentStream.beginText();
					float charSpacing = 0;
					if (line.length() > 1) {
						float size = titleFontSize * PDType1Font.HELVETICA_BOLD.getStringWidth(line) / 1000;
						float free = width - size;
						if (free > 0) {
							charSpacing = free / (line.length() - 1);
						}
					}
					contentStream.setCharacterSpacing(charSpacing);
					if (j == titleTime.size() - 1) {
						contentStream.setCharacterSpacing(0);
					}

					contentStream.newLineAtOffset(startX, yPosition);
					contentStream.showText(line);
					contentStream.newLine(); // Move to the next line
					contentStream.endText();

					yPosition -= leading; // Adjust the startY position for the next line
				}
			}

			if (i == 2) {
				List<String> titleFreq = splitTextToFitWidth(freqLabel, PDType1Font.HELVETICA_BOLD, titleFontSize,
						width);
				contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);
				contentStream.setLeading(leading);
				for (int j = 0; j < titleFreq.size(); j++) {
					String line = titleFreq.get(j);
					contentStream.beginText();
					float charSpacing = 0;
					if (line.length() > 1) {
						float size = titleFontSize * PDType1Font.HELVETICA_BOLD.getStringWidth(line) / 1000;
						float free = width - size;
						if (free > 0) {
							charSpacing = free / (line.length() - 1);
						}
					}
					contentStream.setCharacterSpacing(charSpacing);
					if (j == titleFreq.size() - 1) {
						contentStream.setCharacterSpacing(0);
					}

					contentStream.newLineAtOffset(startX, yPosition);
					contentStream.showText(line);
					contentStream.newLine(); // Move to the next line
					contentStream.endText();

					yPosition -= leading; // Adjust the startY position for the next line
				}
			}

			// Dividir el título en líneas para ajustarlo dentro del margen
			List<String> titleLines = splitTextToFitWidth(title, PDType1Font.HELVETICA, titleFontSize, width);

			// Escribir el título de la imagen
			contentStream.setFont(PDType1Font.HELVETICA, titleFontSize);
			contentStream.setLeading(leading);
			for (String line : titleLines) {
				contentStream.beginText();
				contentStream.newLineAtOffset(startX, yPosition);
				contentStream.showText(line);
				contentStream.newLine();
				contentStream.endText();
				yPosition -= leading;
			}
			// Actualizar la posición Y para el siguiente título e imagen
			yPosition -= imageHeight - 15; // Espacio entre imágenes
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
	public ResponseEntity<String> generateInt(HttpServletRequest request, @RequestBody InterpretabilityReportDTO data)
			throws IOException {
		Principal test = request.getUserPrincipal();

		this.sessionActual = test.getName();

		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		String outer_race_title = "";
		String inner_race_title = "";
		String bearing_balls_title = "";
		String cage_title = "";

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

		if (data.getDetails_out().size() > 1) {
			outer_race_title = this.getMessage("view.int.out.uno") + data.getDetails_out().get(0) + " "
					+ this.getMessage("view.int.out.dos") + " "
					+ data.getDetails_out().get(data.getDetails_out().size() - 1) + " "
					+ this.getMessage("view.int.out.tres");
		} else {
			outer_race_title = this.getMessage("view.int.out.uno") + data.getDetails_out().get(0) + " "
					+ this.getMessage("view.int.out.tres");
		}

		if (data.getDetails_in().size() > 1) {
			inner_race_title = this.getMessage("view.int.inn.uno") + data.getDetails_in().get(0) + " "
					+ this.getMessage("view.int.out.dos") + " "
					+ data.getDetails_in().get(data.getDetails_in().size() - 1) + " "
					+ this.getMessage("view.int.out.tres");
		} else {
			inner_race_title = this.getMessage("view.int.inn.uno") + data.getDetails_in().get(0) + " "
					+ this.getMessage("view.int.out.tres");
		}

		if (data.getDetails_balls().size() > 1) {
			bearing_balls_title = this.getMessage("view.int.ball.uno") + data.getDetails_balls().get(0) + " "
					+ this.getMessage("view.int.out.dos") + " "
					+ data.getDetails_balls().get(data.getDetails_balls().size() - 1) + " "
					+ this.getMessage("view.int.out.tres");
		} else {
			bearing_balls_title = this.getMessage("view.int.ball.uno") + data.getDetails_balls().get(0) + " "
					+ this.getMessage("view.int.out.tres");
		}

		if (data.getDetails_cage().size() > 1) {
			cage_title = this.getMessage("view.int.cage.uno") + data.getDetails_cage().get(0) + " "
					+ this.getMessage("view.int.out.dos") + " "
					+ data.getDetails_cage().get(data.getDetails_cage().size() - 1) + " "
					+ this.getMessage("view.int.out.tres");
		} else {
			cage_title = this.getMessage("view.int.cage.uno") + data.getDetails_cage().get(0) + " "
					+ this.getMessage("view.int.out.tres");
		}

		BufferedImage[] images = { electricService.getImage(this.sessionActual, 1),
				electricService.getImage(this.sessionActual, 2), electricService.getImage(this.sessionActual, 3),
				electricService.getImage(this.sessionActual, 4) };
		String[] titles = { outer_race_title, inner_race_title, bearing_balls_title, cage_title };

		for (int i = 0; i < images.length; i++) {
			// Si la imagen es nula, omitir su pintura
			if (images[i] == null) {
				continue;
			}

			// Si ya hemos agregado dos imágenes, creamos una nueva página
			if (i == 2) {
				contentStream.close();
				page = new PDPage(pageSize);
				document.addPage(page);
				contentStream = new PDPageContentStream(document, page);
				yPosition = startY;
			}

			// Imagen centrada y más grande
			PDImageXObject pdImage = LosslessFactory.createFromImage(document, images[i]);
			float imageWidth = pdImage.getWidth() * scale;
			float imageHeight = pdImage.getHeight() * scale;
			float imageX = startX + (width - imageWidth) / 2;
			float imageY = yPosition - imageHeight * 1.02f;
			contentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);

			// Título de la imagen
			String title = titles[i];
			float titleFontSize = 12;

			// Dividir el título en líneas para ajustarlo dentro del margen
			List<String> lines = splitTextToFitWidth(title, PDType1Font.HELVETICA_BOLD, titleFontSize, width);

			// Escribir el título de la imagen justificado
			contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);
			contentStream.setLeading(leading);
			for (int j = 0; j < lines.size(); j++) {
				String line = lines.get(j);
				contentStream.beginText();
				float charSpacing = 0;
				if (line.length() > 1) {
					float size = titleFontSize * PDType1Font.HELVETICA_BOLD.getStringWidth(line) / 1000;
					float free = width - size;
					if (free > 0) {
						charSpacing = free / (line.length() - 1);
					}
				}
				contentStream.setCharacterSpacing(charSpacing);
				if (j == lines.size() - 1) {
					contentStream.setCharacterSpacing(0);
				}

				contentStream.newLineAtOffset(startX, yPosition);
				contentStream.showText(line);
				contentStream.newLine(); // Move to the next line
				contentStream.endText();

				yPosition -= leading; // Adjust the startY position for the next line
			}

			// Actualizar la posición Y para el siguiente título e imagen
			yPosition -= imageHeight - 15; // Espacio entre imágenes
		}

		String finalText = "";

		switch (data.getFault_info()) {
		case "A fault has been detected in an early stage":
			finalText = this.getMessage("view.cont.fault.info.first");
			break;
		case "A fault has been detected in a medium stage":
			finalText = this.getMessage("view.cont.fault.info.second");
			break;
		case "A fault has been detected in a last degradation stage":
			finalText = this.getMessage("view.cont.fault.info.third");
			break;

		}

		// Dividir el texto final en líneas para ajustarlo dentro del margen
		List<String> finalTextLines = splitTextToFitWidth(finalText, PDType1Font.HELVETICA_BOLD, 12, width);

		// Escribir el texto final
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
		contentStream.setLeading(leading);
		for (String line : finalTextLines) {
			contentStream.beginText();
			contentStream.newLineAtOffset(startX, yPosition + 15);
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

	private List<String> splitTextToFitWidth(String text, PDFont font, float fontSize, float maxWidth)
			throws IOException {
		List<String> lines = new ArrayList<>();
		int lastSpace = -1;
		while (text.length() > 0) {
			int spaceIndex = text.indexOf(' ', lastSpace + 1);
			if (spaceIndex < 0)
				spaceIndex = text.length();
			String subString = text.substring(0, spaceIndex);
			float size = fontSize * font.getStringWidth(subString) / 1000;
			if (size > maxWidth) {
				if (lastSpace < 0)
					lastSpace = spaceIndex;
				subString = text.substring(0, lastSpace);
				lines.add(subString);
				text = text.substring(lastSpace).trim();
				lastSpace = -1;
			} else if (spaceIndex == text.length()) {
				lines.add(text);
				text = "";
			} else {
				lastSpace = spaceIndex;
			}
		}
		return lines;
	}
}
