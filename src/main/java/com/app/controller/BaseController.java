package com.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.app.dto.RunUploadFileRequestDTO;
import com.app.dto.UserDTO;
import com.app.service.ElectricService;
import com.app.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(MappingConstants.ROOT)
public class BaseController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ElectricService electricService;

	@Autowired
	private LoginService loginService;

	private String errorsH = "";
	private String loggedUser = "";
	private String loggedUserRole = "";
	private String ownerUser = "";

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

		this.loggedUser = test.getName();

		try {
			List<String> pdatasets = electricService.getAllDatasets();
			List<String> savedatasets = null;

			List<UserDTO> listUsers = loginService.getAllUsers();
			for (int i = 0; i < listUsers.size(); i++) {
				if (listUsers.get(i).getUsuario().equals(this.loggedUser)) {
					this.loggedUserRole = listUsers.get(i).getRole();
					break;
				}
			}

			if (!"ADMIN".equals(this.loggedUserRole)) {
				savedatasets = electricService.getSavedDatasets(this.loggedUser);
			} else {
				savedatasets = electricService.getAllSavedDatasets();
			}

			if (pdatasets.size() == 0) {
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
			model.addAttribute("loggedUser", this.loggedUser);

			if (!this.errorsH.isEmpty()) {
				model.addAttribute("errorsH", errorsH);
			}

		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		this.errorsH = "";
		this.ownerUser = "";
		return ViewConstants.VIEW_HOME_PAGE;
	}

	@PostMapping(MappingConstants.DELETE_DATASET)
	public String delete(@RequestParam("item") String item) {
		try {
			electricService.delete(item, this.loggedUser);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		return ViewConstants.REDIRECT_HOME_PAGE;
	}

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
		model.addAttribute("loggedUser", this.loggedUser);
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
			if (data2Run.getAnalyzed_number_req() >= 5 && data2Run.getHealthy_number_req() >= 30
					&& info.getMax_to_check() >= data2Run.getAnalyzed_number_req()
					&& info.getMax_to_check() >= data2Run.getHealthy_number_req()) {

				info.setMin_to_check(data2Run.getFirst_sample_req());
				info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
				response = electricService.run(data2Run, this.loggedUser, 0);
				img1 = electricService.getImage(this.loggedUser, 1);
				img2 = electricService.getImage(this.loggedUser, 2);
				img3 = electricService.getImage(this.loggedUser, 3);
				img4 = electricService.getImage(this.loggedUser, 4);

				if (img1 != null) {
					String img2Front1Encoded = encodeImageToBase64(img1);
					model.addAttribute("img2Front1Encoded", img2Front1Encoded);
				}
				if (img2 != null) {
					String img2Front2Encoded = encodeImageToBase64(img2);
					model.addAttribute("img2Front2Encoded", img2Front2Encoded);
				}
				if (img3 != null) {
					String img2Front3Encoded = encodeImageToBase64(img3);
					model.addAttribute("img2Front3Encoded", img2Front3Encoded);
				}
				if (img4 != null) {
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
				if (info.getMax_to_check() <= data2Run.getHealthy_number_req()
						|| data2Run.getHealthy_number_req() <= 30) {
					errors = this.getMessage("view.cont.value.healthy");
				} else {
					errors = this.getMessage("view.cont.value.regular");
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

		if (response == null) {
			model.addAttribute("n_healthy_used", data2Run.getHealthy_number_req());
			model.addAttribute("loggedUser", this.loggedUser);
			return ViewConstants.VIEW_PRELOADED_PAGE;
		}

		if (response.getAnalysis_result() != null) {
			model.addAttribute("runResForm", response);
			if (response.isFault_detected()) {
		        
				List<List<Double>> listaSinDuplicadosYOrdenada = new ArrayList<>();
				for (List<Double> sublista : response.getFault_details()) {
		            HashSet<Double> conjuntoSinDuplicados = new HashSet<>(sublista);
		            List<Double> sublistaSinDuplicados = new ArrayList<>(conjuntoSinDuplicados);
		            Collections.sort(sublistaSinDuplicados);
		            
		            listaSinDuplicadosYOrdenada.add(sublistaSinDuplicados);
		        }
				
		        for (int i = 0; i < response.getFault_details().size(); i++) {
					model.addAttribute("data_" + response.getFault_type().get(i), listaSinDuplicadosYOrdenada.get(i));
					model.addAttribute("faults_" + response.getFault_type().get(i), listaSinDuplicadosYOrdenada.get(i));
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
		model.addAttribute("loggedUser", this.loggedUser);
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
		if ("ADMIN".equals(this.loggedUserRole) && !selectedSavedModel.equals("New Dataset")) {
			String[] tmp2 = tmp[0].split("\\(");
			tmp[0] = tmp2[0].trim();
			if (this.ownerUser == "") {
				this.ownerUser = tmp2[1].trim().substring(0, tmp2[1].length() - 1);
			}
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
		model.addAttribute("loggedUser", this.loggedUser);
		return ViewConstants.VIEW_NEWLOADED_PAGE;
	}

	@GetMapping(MappingConstants.SAVE_DATASET)
	public String saveDataset(DatasetInformationDTO infoForm, Model model) {
		if ("New Dataset".equals(infoForm.getNombre())) {
			return ViewConstants.REDIRECT_NEWLOADED_PAGE + infoForm.getNombre();
		}
		if (infoForm.getBpfi() > 0.0 && infoForm.getBpfo() > 0.0 && infoForm.getBsf() > 0.0 && infoForm.getCarga() > 0.0
				&& infoForm.getFtf() > 0.0 && infoForm.getSampling_frequency() > 0.0
				&& infoForm.getShaft_frequency() > 0.0 && !infoForm.getBearing_type().equals("")
				&& !infoForm.getBearing_type().equals(" ")) {
			try {
				if (infoForm.getId() == null) {
					electricService.createDatasetInDB(infoForm, this.loggedUser);
					return ViewConstants.REDIRECT_HOME_PAGE;
				} else {
					electricService.updateDatasetInDB(infoForm);
				}
			} catch (ConnectException e) {
				System.err.println("Error al conectar con la API: " + e.getMessage());
			}
		} else {
			model.addAttribute("errorsVals", this.getMessage("view.cont.vals.fault"));
		}

		return ViewConstants.REDIRECT_NEWLOADED_PAGE + infoForm.getNombre();
	}

	@PostMapping(MappingConstants.UPLOAD_NEW_DATASET)
	public String guardarNewArchivos(@RequestParam("healthyData2Save") MultipartFile dataFiles,
			@RequestParam("name2send") String name, @RequestParam("id2send") int id, Model model) {

		String owner = "";
		if (this.ownerUser != "") {
			owner = this.ownerUser;
		} else {
			owner = this.loggedUser;
		}
		if (dataFiles.isEmpty()) {
			this.errorsH = this.getMessage("view.cont.file.not");
			return ViewConstants.REDIRECT_NEWLOADED_PAGE + name;
		}
		this.errorsH = "";
		try {
			String extension = StringUtils.getFilenameExtension(dataFiles.getOriginalFilename());
			if ("csv".equalsIgnoreCase(extension)) {
				String newSampleFileName = name + "." + extension;
				ResponseEntity<String> response = electricService.uploadDataSample(dataFiles, newSampleFileName, id, owner);
				if (response.getStatusCode() == HttpStatus.ACCEPTED) {
					this.errorsH = this.getMessage("view.cont.file.regular.lines");
					return ViewConstants.REDIRECT_NEWLOADED_PAGE + name;
				}
			} else {
				this.errorsH = this.getMessage("view.cont.ext.first") + extension + " "
						+ this.getMessage("view.cont.ext.second");
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
		String owner = "";
		if (this.ownerUser != "") {
			owner = this.ownerUser;
		} else {
			owner = this.loggedUser;
		}
		try {
			electricService.deleteSample(healthyName, regularName, id, owner);
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}
		return ViewConstants.REDIRECT_NEWLOADED_PAGE + nombre;
	}

	@PostMapping(MappingConstants.RUN_NEWLOAD)
	public String runNewload(@RequestParam(name = "uploadDataSampleInput", required = false) MultipartFile dataFiles,
			RunRequestDTO data2Run, RunUploadFileRequestDTO dataUpload2Run, Model model) throws IOException {
		DatasetInformationDTO info = new DatasetInformationDTO();
		RunResponseDTO response = new RunResponseDTO();
		String errors = "";
		BufferedImage img1 = null;
		BufferedImage img2 = null;
		BufferedImage img3 = null;
		BufferedImage img4 = null;

		String owner = "";
		if (this.ownerUser != "") {
			owner = this.ownerUser;
		} else {
			owner = this.loggedUser;
		}
		
		try {
			info = electricService.getDatasetInfo(data2Run.getNombre_req());
			if (dataFiles == null) {
				if (data2Run.getAnalyzed_number_req() >= 5 && data2Run.getHealthy_number_req() >= 30
						&& info.getMax_to_check() >= data2Run.getAnalyzed_number_req()
						&& info.getMax_to_check() >= data2Run.getHealthy_number_req()) {

					info.setMin_to_check(data2Run.getFirst_sample_req());
					info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());

					response = electricService.run(data2Run, owner, 1);

					img1 = electricService.getImage(owner, 1);
					img2 = electricService.getImage(owner, 2);
					img3 = electricService.getImage(owner, 3);
					img4 = electricService.getImage(owner, 4);

					if (img1 != null) {
						String img2Front1Encoded = encodeImageToBase64(img1);
						model.addAttribute("img2Front1Encoded", img2Front1Encoded);
					}
					if (img2 != null) {
						String img2Front2Encoded = encodeImageToBase64(img2);
						model.addAttribute("img2Front2Encoded", img2Front2Encoded);
					}
					if (img3 != null) {
						String img2Front3Encoded = encodeImageToBase64(img3);
						model.addAttribute("img2Front3Encoded", img2Front3Encoded);
					}
					if (img4 != null) {
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
					if (info.getMax_to_check() <= data2Run.getHealthy_number_req()
							|| data2Run.getHealthy_number_req() <= 30) {
						errors = this.getMessage("view.cont.value.healthy");
					} else {
						errors = this.getMessage("view.cont.value.regular");
					}
					info.setMin_to_check(data2Run.getFirst_sample_req());
					info.setMax_to_check(data2Run.getFirst_sample_req() + data2Run.getAnalyzed_number_req());
				}
			} else {
				info = electricService.getDatasetInfo(dataUpload2Run.getNombre_req_upp());
				String extension = StringUtils.getFilenameExtension(dataFiles.getOriginalFilename());
				if ("csv".equalsIgnoreCase(extension)) {
					data2Run.setNombre_req(dataUpload2Run.getNombre_req_upp());
					data2Run.setBpfi_req(dataUpload2Run.getBpfi_req_upp());
					data2Run.setBpfo_req(dataUpload2Run.getBpfo_req_upp());
					data2Run.setBsf_req(dataUpload2Run.getBsf_req_upp());
					data2Run.setFtf_req(dataUpload2Run.getFtf_req_upp());
					data2Run.setHealthy_number_req(dataUpload2Run.getHealthy_number_req_upp());
					data2Run.setSampling_frequency_req(dataUpload2Run.getSampling_frequency_req_upp());
					data2Run.setShaft_frequency_req(dataUpload2Run.getShaft_frequency_req_upp());
					data2Run.setFirst_sample_req(0);
					data2Run.setAnalyzed_number_req(1);

					String newSampleFileName = "tmp" + data2Run.getNombre_req() + "." + extension;
					ResponseEntity<String> responseUpp = electricService.uploadDataSample(dataFiles, newSampleFileName,
							dataUpload2Run.getId_upp(), owner);
					if (responseUpp.getStatusCode() != HttpStatus.OK) {
						model.addAttribute("errorsH", this.getMessage("view.cont.file.regular.lines"));
						return ViewConstants.REDIRECT_NEWLOADED_PAGE + data2Run.getNombre_req();
					}

					response = electricService.run(data2Run, owner, 3);

					img1 = electricService.getImage(owner, 1);
					img2 = electricService.getImage(owner, 2);
					img3 = electricService.getImage(owner, 3);
					img4 = electricService.getImage(owner, 4);

					if (img1 != null) {
						String img2Front1Encoded = encodeImageToBase64(img1);
						model.addAttribute("img2Front1Encoded", img2Front1Encoded);
					}
					if (img2 != null) {
						String img2Front2Encoded = encodeImageToBase64(img2);
						model.addAttribute("img2Front2Encoded", img2Front2Encoded);
					}
					if (img3 != null) {
						String img2Front3Encoded = encodeImageToBase64(img3);
						model.addAttribute("img2Front3Encoded", img2Front3Encoded);
					}
					if (img4 != null) {
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
					model.addAttribute("loggedUser", this.loggedUser);
					return ViewConstants.REDIRECT_NEWLOADED_PAGE + dataUpload2Run.getNombre_req_upp();
				}
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
				List<List<Double>> listaSinDuplicadosYOrdenada = new ArrayList<>();
				for (List<Double> sublista : response.getFault_details()) {
		            HashSet<Double> conjuntoSinDuplicados = new HashSet<>(sublista);
		            List<Double> sublistaSinDuplicados = new ArrayList<>(conjuntoSinDuplicados);
		            Collections.sort(sublistaSinDuplicados);
		            
		            listaSinDuplicadosYOrdenada.add(sublistaSinDuplicados);
		        }
				
		        for (int i = 0; i < response.getFault_details().size(); i++) {
					model.addAttribute("data_" + response.getFault_type().get(i), listaSinDuplicadosYOrdenada.get(i));
					model.addAttribute("faults_" + response.getFault_type().get(i), listaSinDuplicadosYOrdenada.get(i));
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
		model.addAttribute("loggedUser", this.loggedUser);
		return ViewConstants.VIEW_NEWLOADED_PAGE;
	}
}
