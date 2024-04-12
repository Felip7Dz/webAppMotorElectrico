package com.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.app.constants.MappingConstants;
import com.app.constants.ViewConstants;
import com.app.dto.DatasetInformationDTO;
import com.app.dto.DatasetsResponseDTO;
import com.app.dto.RunRequestDTO;
import com.app.dto.RunResponseDTO;
import com.app.service.ElectricService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(MappingConstants.ROOT)
public class BaseController {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ElectricService electricService;
	
	private String errorsH = "";
	private String sessionActual = "";

	public BaseController(ElectricService electricService) {
		this.electricService = electricService;
	}

	public String getMessage(String messageKey) {
		return this.getMessage(messageKey, null);
	}
	
	public String getMessage(String messageKey, Object[] args) {
		return this.messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
	}
	
	@GetMapping(MappingConstants.HOME_ROOT)
	public String home(Model model, HttpServletRequest request) {
		Principal test = request.getUserPrincipal();
	
		this.sessionActual = test.getName();

		try {
			List<String> pdatasets = electricService.getAllDatasets();
			List<String> savedatasets = null;
			
			if(!"admin".equals(this.sessionActual)) {
				savedatasets = electricService.getSavedDatasets(this.sessionActual);
			}else {
				savedatasets = electricService.getAllSavedDatasets();
			}
			
			if(pdatasets.size() == 0) {
				model.addAttribute("errorsAPI", this.getMessage("view.home.api.connection"));
			}

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
			model.addAttribute("loggedUser", this.sessionActual);

			if (!this.errorsH.isEmpty()) {
				model.addAttribute("errorsH", errorsH);
			}

		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		this.errorsH = "";
		return ViewConstants.VIEW_HOME_PAGE;
	}

	@PostMapping(MappingConstants.DELETE_DATASET)
	public String delete(@RequestParam("item") String item) {
		try {
			electricService.delete(item, this.sessionActual);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		return ViewConstants.REDIRECT_HOME_PAGE;
	}
	
	/**
	@PostMapping(MappingConstants.UPLOAD_DATASET)
	public String guardarArchivo(@RequestParam("model2Save") MultipartFile archivo, Model model) {
		this.errorsH = "";
		if (archivo.isEmpty()) {
			return ViewConstants.REDIRECT_HOME_PAGE;
		}
		try {
			String extension = StringUtils.getFilenameExtension(archivo.getOriginalFilename());
			if (!"csv".equalsIgnoreCase(extension)) {
				this.errorsH = this.getMessage("view.cont.ext.first") + extension + " " + this.getMessage("view.cont.ext.second");
				return ViewConstants.REDIRECT_HOME_PAGE;
			}
			
			List<String> savedatasets = electricService.getAllSavedDatasets(this.sessionActual);
			for (int i = 0; i < savedatasets.size(); i++) {
				if (archivo.getOriginalFilename().equals(savedatasets.get(i))) {
					this.errorsH = this.getMessage("view.cont.name.first") + archivo.getOriginalFilename() + " "
							+ this.getMessage("view.cont.name.second");
					return ViewConstants.REDIRECT_HOME_PAGE;
				}
			}
			electricService.uploadDataset(archivo, this.sessionActual);
		} catch (Exception e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		return ViewConstants.REDIRECT_HOME_PAGE;
	}
	 */
	
	@GetMapping(MappingConstants.PRE_LOADED)
	public String preload(@RequestParam(name = "selectedModel", required = true) String selectedModel, Model model) {

		DatasetInformationDTO info = new DatasetInformationDTO();
		String[] tmp = selectedModel.split("\\.");

		try {
			info = electricService.getDatasetInfo(tmp[0]);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		model.addAttribute("selectedModel", tmp[0]);
		if (info != null) {
			model.addAttribute("formData", info);
			model.addAttribute("formDataCheck", info);
		}
		model.addAttribute("loggedUser", this.sessionActual);
		return ViewConstants.VIEW_PRELOADED_PAGE;
	}

	@PostMapping(MappingConstants.RUN_PRELOADED)
	public String runPreloaded(RunRequestDTO data2Run, Model model) throws IOException {
		DatasetInformationDTO info = new DatasetInformationDTO();
		RunResponseDTO response = new RunResponseDTO();
		String errors = "";
		BufferedImage img1 = null;
		BufferedImage img2 = null;
		BufferedImage img3 = null;
		BufferedImage img4 = null;

		try {
			info = electricService.getDatasetInfo(data2Run.getNombre_req());
			if (data2Run.getAnalyzed_number_req() > 0 && data2Run.getHealthy_number_req() > 0 && info.getMax_to_check() >= data2Run.getAnalyzed_number_req() && info.getMax_to_check() >= data2Run.getHealthy_number_req()) {
				info.setMin_to_check(data2Run.getFirst_sample_req());
				info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
				response = electricService.run(data2Run, this.sessionActual, 0);
				img1 = electricService.getImage(this.sessionActual, 1);
				img2 = electricService.getImage(this.sessionActual, 2);
				img3 = electricService.getImage(this.sessionActual, 3);
				img4 = electricService.getImage(this.sessionActual, 4);

				if (img1 != null) {
					String img2Front1Encoded = encodeImageToBase64(img1);
					model.addAttribute("img2Front1Encoded", img2Front1Encoded);
				}if(img2 != null) {				
					String img2Front2Encoded = encodeImageToBase64(img2);
					model.addAttribute("img2Front2Encoded", img2Front2Encoded);
				}if(img3 != null) {
					String img2Front3Encoded = encodeImageToBase64(img3);
					model.addAttribute("img2Front3Encoded", img2Front3Encoded);
				}if(img4 != null) {
					String img2Front4Encoded = encodeImageToBase64(img4);
					model.addAttribute("img2Front4Encoded", img2Front4Encoded);
				}
				
				if (img1 == null && img2 == null && img3 == null && img4 == null) {
					BufferedImage imgNotFault = electricService.getImage("faultless", 1);
					String img2FrontNotFaultEncoded = encodeImageToBase64(imgNotFault);
					model.addAttribute("img2FrontNotFaultEncoded", img2FrontNotFaultEncoded);
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.faultless"));
				}

			} else {
				if(info.getMax_to_check() <= data2Run.getHealthy_number_req() || data2Run.getHealthy_number_req() == 0) {
					errors = this.getMessage("view.cont.value.healthy.first") + data2Run.getHealthy_number_req() + " "
							+ this.getMessage("view.cont.value.second") + info.getMax_to_check() + " "
							+ this.getMessage("view.cont.value.third");
				} else {
					errors = this.getMessage("view.cont.value.first") + data2Run.getAnalyzed_number_req() + " "
							+ this.getMessage("view.cont.value.second") + info.getMax_to_check() + " "
							+ this.getMessage("view.cont.value.third");
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

		if (response.getAnalysis_result() != null) {
			model.addAttribute("runResForm", response);
			if (response.isFault_detected()) {
				for (int i = 0; i < response.getFault_details().size(); i++) {
					model.addAttribute("data_" + response.getFault_type().get(i), response.getFault_details().get(i));
					model.addAttribute("faults_" + response.getFault_type().get(i), response.getFault_details().get(i));
				}
				switch (response.getFault_info()) {
				case "A fault has been detected in an early stage":
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.fault.info.first"));
					break;
				case "A fault has been detected in a medium stage":
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.fault.info.second"));
					break;
				case "A fault has been detected in a last degradation stage":
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.fault.info.third"));
					break;

				}
			}
		}

		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
		}
		model.addAttribute("n_healthy_used", data2Run.getHealthy_number_req());
		model.addAttribute("loggedUser", this.sessionActual);
		return ViewConstants.VIEW_PRELOADED_PAGE;
	}

	private String encodeImageToBase64(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", byteArrayOutputStream);
		byte[] bytes = byteArrayOutputStream.toByteArray();
		return Base64.getEncoder().encodeToString(bytes);
	}

	@GetMapping(MappingConstants.NEW_LOAD)
	public String newload(@RequestParam(name = "selectedSavedModel", required = true) String selectedSavedModel,
			Model model) {
		DatasetInformationDTO info = new DatasetInformationDTO();
		String[] tmp = selectedSavedModel.split("\\.");
		if("admin".equals(this.sessionActual)) {
			String[] tmp2 = tmp[0].split("\\(");
			tmp[0] = tmp2[0];
		}

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
		model.addAttribute("loggedUser", this.sessionActual);
		return ViewConstants.VIEW_NEWLOADED_PAGE;
	}

	@GetMapping(MappingConstants.SAVE_DATASET)
	public String saveDataset(DatasetInformationDTO infoForm, Model model) {
		if("New Dataset".equals(infoForm.getNombre())) {
			return ViewConstants.REDIRECT_NEWLOADED_PAGE + infoForm.getNombre();
		}
		if(infoForm.getBpfi() > 0.0 && infoForm.getBpfo() > 0.0 && infoForm.getBsf() > 0.0 && infoForm.getCarga() > 0.0 && infoForm.getFtf() > 0.0 && 
				infoForm.getSampling_frequency() > 0.0 && infoForm.getShaft_frequency() > 0.0 && !infoForm.getBearing_type().equals("") && !infoForm.getBearing_type().equals(" ")) {
			try {
				if (infoForm.getId() == null) {
					electricService.createDatasetInDB(infoForm, this.sessionActual);
				} else {
					electricService.updateDatasetInDB(infoForm);
				}
			} catch (ConnectException e) {
				System.err.println("Error al conectar con la API: " + e.getMessage());
			}
		}else {
			model.addAttribute("errorsVals", this.getMessage("view.cont.vals.fault"));
		}

		return ViewConstants.REDIRECT_NEWLOADED_PAGE + infoForm.getNombre();
	}

	@PostMapping(MappingConstants.UPLOAD_NEW_DATASET)
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
					
					electricService.uploadDataSample(healthy, healthyFileName, id, this.sessionActual);
					electricService.uploadDataSample(newSample, newSampleFileName, id, this.sessionActual);
				} else {
					this.errorsH = this.getMessage("view.cont.ext.first") + extension1 + " " + this.getMessage("view.cont.ext.second");
				}
			} else {
				this.errorsH = this.getMessage("view.cont.file.not");
			}
		} catch (Exception e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		return ViewConstants.REDIRECT_NEWLOADED_PAGE + name;
	}
	
	@PostMapping(MappingConstants.DELETE_SAMPLE)
	public String deleteSample(@RequestParam("nombre") String nombre, @RequestParam("id") int id) {
		String healthyName = "healthy" + nombre + ".csv";
		String regularName = nombre + ".csv";
		try {
			electricService.deleteSample(healthyName, regularName, id, this.sessionActual);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		return ViewConstants.REDIRECT_NEWLOADED_PAGE + nombre;
	}

	@PostMapping(MappingConstants.RUN_NEWLOAD)
	public String runNewload(RunRequestDTO data2Run, Model model) throws IOException {
		DatasetInformationDTO info = new DatasetInformationDTO();
		RunResponseDTO response = new RunResponseDTO();
		String errors = "";
		BufferedImage img1 = null;
		BufferedImage img2 = null;
		BufferedImage img3 = null;
		BufferedImage img4 = null;

		try {
			info = electricService.getDatasetInfo(data2Run.getNombre_req());
			if (data2Run.getAnalyzed_number_req() > 0 && data2Run.getHealthy_number_req() > 0 && info.getMax_to_check() >= data2Run.getAnalyzed_number_req() && info.getMax_to_check() >= data2Run.getHealthy_number_req()) {
				info.setMin_to_check(data2Run.getFirst_sample_req());
				info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
				response = electricService.run(data2Run, this.sessionActual, 1);
				img1 = electricService.getImage(this.sessionActual, 1);
				img2 = electricService.getImage(this.sessionActual, 2);
				img3 = electricService.getImage(this.sessionActual, 3);
				img4 = electricService.getImage(this.sessionActual, 4);

				if (img1 != null) {
					String img2Front1Encoded = encodeImageToBase64(img1);
					model.addAttribute("img2Front1Encoded", img2Front1Encoded);
				}if(img2 != null) {				
					String img2Front2Encoded = encodeImageToBase64(img2);
					model.addAttribute("img2Front2Encoded", img2Front2Encoded);
				}if(img3 != null) {
					String img2Front3Encoded = encodeImageToBase64(img3);
					model.addAttribute("img2Front3Encoded", img2Front3Encoded);
				}if(img4 != null) {
					String img2Front4Encoded = encodeImageToBase64(img4);
					model.addAttribute("img2Front4Encoded", img2Front4Encoded);
				}
				
				if (img1 == null && img2 == null && img3 == null && img4 == null) {
					BufferedImage imgNotFault = electricService.getImage("faultless", 1);
					String img2FrontNotFaultEncoded = encodeImageToBase64(imgNotFault);
					model.addAttribute("img2FrontNotFaultEncoded", img2FrontNotFaultEncoded);
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.faultless"));
				}

			} else {
				if(info.getMax_to_check() <= data2Run.getHealthy_number_req() || data2Run.getHealthy_number_req() == 0) {
					errors = this.getMessage("view.cont.value.healthy.first") + data2Run.getHealthy_number_req() + " "
							+ this.getMessage("view.cont.value.second") + info.getMax_to_check() + " "
							+ this.getMessage("view.cont.value.third");
				} else {
					errors = this.getMessage("view.cont.value.first") + data2Run.getAnalyzed_number_req() + " "
							+ this.getMessage("view.cont.value.second") + info.getMax_to_check() + " "
							+ this.getMessage("view.cont.value.third");
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

		if (response.getAnalysis_result() != null) {
			model.addAttribute("runResForm", response);
			if (response.isFault_detected()) {
				for (int i = 0; i < response.getFault_details().size(); i++) {
					model.addAttribute("data_" + response.getFault_type().get(i), response.getFault_details().get(i));
					model.addAttribute("faults_" + response.getFault_type().get(i), response.getFault_details().get(i));
				}
				switch (response.getFault_info()) {
				case "A fault has been detected in an early stage":
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.fault.info.first"));
					break;
				case "A fault has been detected in a medium stage":
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.fault.info.second"));
					break;
				case "A fault has been detected in a last degradation stage":
					model.addAttribute("faultInfoTimming", this.getMessage("view.cont.fault.info.third"));
					break;

				}
			}
		}

		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
		}
		model.addAttribute("n_healthy_used", data2Run.getHealthy_number_req());
		model.addAttribute("loggedUser", this.sessionActual);
		return ViewConstants.VIEW_NEWLOADED_PAGE;
	}
}
