package com.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.app.dto.DatasetInformationDTO;
import com.app.dto.DatasetsResponseDTO;
import com.app.dto.RunRequestDTO;
import com.app.dto.RunResponseDTO;
import com.app.service.ElectricService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class BaseController {

	private final ElectricService electricService;
	private String errorsH = "";
	private String language = "";
	private String sessionActual = "";

	public BaseController(ElectricService electricService) {
		this.electricService = electricService;
	}

	@GetMapping("/home")
	public String home(Model model, HttpServletRequest request) {
		Locale currentLocale = RequestContextUtils.getLocale(request);

		if (this.sessionActual == "" || this.sessionActual == null) {
			HttpSession newSession = request.getSession(true);
			this.sessionActual = newSession.getId();
		}

		this.language = currentLocale.getLanguage();

		try {
			List<String> pdatasets = electricService.getAllDatasets();
			List<String> savedatasets = electricService.getAllSavedDatasets();

			List<String> pdatasetsNames = pdatasets.stream().map(dataset -> dataset.split("\\.")[0])
					.collect(Collectors.toList());

			List<String> savedatasetsNames = savedatasets.stream().map(savdataset -> savdataset.split("\\.")[0])
					.collect(Collectors.toList());

			DatasetsResponseDTO show = new DatasetsResponseDTO();
			show.setModelsList(pdatasets);
			show.setModelsNames(pdatasetsNames);

			DatasetsResponseDTO showSavec = new DatasetsResponseDTO();
			showSavec.setModelsList(savedatasets);
			showSavec.setModelsNames(savedatasetsNames);

			model.addAttribute("resultDatatest", show);
			model.addAttribute("resultSavedDatatest", showSavec);

			if (!this.errorsH.isEmpty()) {
				model.addAttribute("errorsH", errorsH);
			}

		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		this.errorsH = "";
		return "public/home";
	}

	@PostMapping("/deleteDataset")
	public String delete(@RequestParam("item") String item) {
		try {
			electricService.delete(item);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		return "redirect:/home";
	}

	@PostMapping("/uploadDataset")
	public String guardarArchivo(@RequestParam("model2Save") MultipartFile archivo, Model model) {
		this.errorsH = "";
		try {
			if (!archivo.isEmpty()) {
				String extension = StringUtils.getFilenameExtension(archivo.getOriginalFilename());
				if ("h5".equalsIgnoreCase(extension)) {
					electricService.uploadDataset(archivo);
				} else {
					if ("en".equals(this.language)) {
						this.errorsH = "Extension ." + extension + " not allowed.";
					} else {
						this.errorsH = "Extensión ." + extension + " no permitida.";
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		return "redirect:/home";
	}

	@GetMapping("/preloaded")
	public String preload(@RequestParam(name = "selectedModel", required = false) String selectedModel, Model model) {

		List<String> data = new ArrayList<>();
		DatasetInformationDTO info = new DatasetInformationDTO();
		String[] tmp = selectedModel.split("\\.");

		try {
			data = electricService.getData();
			info = electricService.getDatasetInfo(tmp[0]);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		model.addAttribute("selectedModel", tmp[0]);
		if (info != null) {
			model.addAttribute("formData", info);
			model.addAttribute("formDataCheck", info);
		}
		model.addAttribute("resultData", data);

		return "public/preloaded";
	}

	@PostMapping("/runPreloaded")
	public String runPreloaded(RunRequestDTO data2Run, Model model) throws IOException {
		DatasetInformationDTO info = new DatasetInformationDTO();
		RunResponseDTO response = new RunResponseDTO();
		String errors = "";
		BufferedImage img1 = null;
		BufferedImage img2 = null;

		try {
			info = electricService.getDatasetInfo(data2Run.getNombre_req());
			if (info.getMax_to_check() >= data2Run.getAnalyzed_number_req()) {
				info.setMin_to_check(data2Run.getFirst_sample_req());
				info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
				response = electricService.run(data2Run, this.sessionActual, 0);
				img1 = electricService.getImage1(this.sessionActual);
				img2 = electricService.getImage2(this.sessionActual);

				if (img1 != null && img2 != null) {
					String img2Front1Encoded = encodeImageToBase64(img1);
					String img2Front2Encoded = encodeImageToBase64(img2);

					model.addAttribute("img2Front1Encoded", img2Front1Encoded);
					model.addAttribute("img2Front2Encoded", img2Front2Encoded);
				}

			} else {
				if ("en".equals(this.language)) {
					errors = "Value " + data2Run.getAnalyzed_number_req()
							+ " out of range. Should be equal or lower than " + info.getMax_to_check();
				} else {
					errors = "Valor " + data2Run.getAnalyzed_number_req()
							+ " fuera de rango. Debería ser igual o inferior a " + info.getMax_to_check();
				}
				info.setMin_to_check(data2Run.getFirst_sample_req());
				info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		model.addAttribute("selectedModel", data2Run.getNombre_req());
		if (info != null) {
			model.addAttribute("formData", info);
			model.addAttribute("formDataCheck", info);
		}

		if (response != null) {
			model.addAttribute("runResForm", response);
			model.addAttribute("n_healthy_used", data2Run.getHealthy_number_req());
			for (int i = 0; i < response.getFault_details().size(); i++) {
				model.addAttribute("data_" + response.getFault_type().get(i), response.getFault_details().get(i));
			}
		}

		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
		}

		return "public/preloaded";
	}

	private String encodeImageToBase64(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", byteArrayOutputStream);
		byte[] bytes = byteArrayOutputStream.toByteArray();
		return Base64.getEncoder().encodeToString(bytes);
	}

	@GetMapping("/newload")
	public String newload(@RequestParam(name = "selectedSavedModel", required = false) String selectedSavedModel,
			Model model) {
		DatasetInformationDTO info = new DatasetInformationDTO();
		String[] tmp = selectedSavedModel.split("\\.");

		try {
			info = electricService.getDatasetInfo(tmp[0]);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		model.addAttribute("selectedSavedModel", tmp[0]);
		
		if (!this.errorsH.isEmpty()) {
			model.addAttribute("errorsH", errorsH);
		}

		if (!"Dataset not found".equals(info.getNombre())) {
			model.addAttribute("formData", info);
			model.addAttribute("formDataCheckNew", info);
		} else {
			info.setBearing_type("");
			info.setBpfi(0.0);
			info.setBpfo(0.0);
			info.setBsf(0.0);
			info.setCarga(0.0);
			info.setFtf(0.0);
			info.setNombre(tmp[0]);
			info.setSampling_frequency(0.0);
			info.setShaft_frequency(0.0);
			info.setFiles_added(0);
			model.addAttribute("formData", info);
		}
		this.errorsH = "";
		return "public/newLoad";
	}

	@GetMapping("/saveDataset")
	public String saveDataset(DatasetInformationDTO infoForm, Model model) {
		try {
			if (infoForm.getId() == null) {
				electricService.createDatasetInDB(infoForm);
			} else {
				electricService.updateDatasetInDB(infoForm);
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		return "redirect:/newload?selectedSavedModel=" + infoForm.getNombre();
	}

	@PostMapping("/uploadNewData")
	public String guardarNewArchivos(@RequestParam("healthyData2Save") MultipartFile healthy,
			@RequestParam("newSample2Save") MultipartFile newSample,
			@RequestParam("name2send") String name, @RequestParam("id2send") int id, Model model) {
		
		this.errorsH = "";
		try {
			if (!healthy.isEmpty() && !newSample.isEmpty()) {
				String extension1 = StringUtils.getFilenameExtension(healthy.getOriginalFilename());
				String extension2 = StringUtils.getFilenameExtension(newSample.getOriginalFilename());
				if ("csv".equalsIgnoreCase(extension1) && "csv".equalsIgnoreCase(extension2)) {
					String healthyFileName = "healthy" + name + "." + extension1;
					String newSampleFileName = name + "." + extension2;
					
					electricService.uploadDataSample(healthy, healthyFileName, id);
					electricService.uploadDataSample(newSample, newSampleFileName, id);
				} else {
					if ("en".equals(this.language)) {
						this.errorsH = "Extension ." + extension1 + " not allowed.";
					} else {
						this.errorsH = "Extensión ." + extension1 + " no permitida.";
					}
				}
			} else {
				if ("en".equals(this.language)) {
					this.errorsH = "File not attached.";
				} else {
					this.errorsH = "Archivo no adjuntado.";
				}
			}
		} catch (Exception e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		return "redirect:/newload?selectedSavedModel=" + name;
	}
	
	@PostMapping("/runNewload")
	public String runNewload(RunRequestDTO data2Run, Model model) throws IOException {
		DatasetInformationDTO info = new DatasetInformationDTO();
		RunResponseDTO response = new RunResponseDTO();
		String errors = "";
		BufferedImage img1 = null;
		BufferedImage img2 = null;

		try {
			info = electricService.getDatasetInfo(data2Run.getNombre_req());
			if (info.getMax_to_check() >= data2Run.getAnalyzed_number_req()) {
				info.setMin_to_check(data2Run.getFirst_sample_req());
				info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
				response = electricService.run(data2Run, this.sessionActual, 1);
				img1 = electricService.getImage1(this.sessionActual);
				img2 = electricService.getImage2(this.sessionActual);

				if (img1 != null && img2 != null) {
					String img2Front1Encoded = encodeImageToBase64(img1);
					String img2Front2Encoded = encodeImageToBase64(img2);

					model.addAttribute("img2Front1Encoded", img2Front1Encoded);
					model.addAttribute("img2Front2Encoded", img2Front2Encoded);
				}

			} else {
				if ("en".equals(this.language)) {
					errors = "Value " + data2Run.getAnalyzed_number_req()
							+ " out of range. Should be equal or lower than " + info.getMax_to_check();
				} else {
					errors = "Valor " + data2Run.getAnalyzed_number_req()
							+ " fuera de rango. Debería ser igual o inferior a " + info.getMax_to_check();
				}
				info.setMin_to_check(data2Run.getFirst_sample_req());
				info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		model.addAttribute("selectedModel", data2Run.getNombre_req());
		if (info != null) {
			model.addAttribute("formData", info);
			model.addAttribute("formDataCheckNew", info);
		}

		if (response != null) {
			model.addAttribute("runResForm", response);
			model.addAttribute("n_healthy_used", data2Run.getHealthy_number_req());
			for (int i = 0; i < response.getFault_details().size(); i++) {
				model.addAttribute("data_" + response.getFault_type().get(i), response.getFault_details().get(i));
			}
		}

		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
		}

		return "public/newLoad";
	}
}
