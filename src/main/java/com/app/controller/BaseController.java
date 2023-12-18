package com.app.controller;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
import com.app.service.ElectricService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BaseController {

	private final ElectricService electricService;
	private String errorsH = "";
	private String language = "";

	public BaseController(ElectricService electricService) {
		this.electricService = electricService;
	}

	@GetMapping("/home")
	public String home(Model model, HttpServletRequest request) {
		Locale currentLocale = RequestContextUtils.getLocale(request);
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
	public String runPreloaded(RunRequestDTO data2Run, Model model) {
		DatasetInformationDTO info = new DatasetInformationDTO();
		String errors = "";

		try {
			info = electricService.getDatasetInfo(data2Run.getNombre_req());
			if (info.getMax_to_check() >= data2Run.getAnalyzed_number_req().intValue()) {
				info.setMin_to_check(data2Run.getFirst_sample_req().intValue());
				info.setMax_to_check(data2Run.getAnalyzed_number_req().intValue());
			} else {
				if ("en".equals(this.language)) {
					errors = "Value " + data2Run.getAnalyzed_number_req().intValue()
							+ " out of range. Should be equal or lower than " + info.getMax_to_check();
				} else {
					errors = "Valor " + data2Run.getAnalyzed_number_req().intValue()
							+ " fuera de rango. Debería ser igual o inferior a " + info.getMax_to_check();
				}
				info.setMin_to_check(data2Run.getFirst_sample_req().intValue());
				info.setMax_to_check(data2Run.getAnalyzed_number_req().intValue());
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		model.addAttribute("selectedModel", data2Run.getNombre_req());
		if (info != null) {
			model.addAttribute("formData", info);
			model.addAttribute("formDataCheck", info);
		}
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
		}

		return "public/preloaded";
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

		if (!"Dataset not found".equals(info.getNombre())) {
			model.addAttribute("formData", info);
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
			model.addAttribute("formData", info);
		}

		return "public/newLoad";
	}

	@GetMapping("/saveDataset")
	public String saveDataset(DatasetInformationDTO infoForm, Model model) {
		DatasetInformationDTO tmp = new DatasetInformationDTO();

		try {
			tmp = electricService.getDatasetInfo(infoForm.getNombre());
			if ("Dataset not found".equals(tmp.getNombre())) {
				electricService.createDatasetInDB(infoForm);
			} else {
				electricService.updateDatasetInDB(infoForm);
			}
		} catch (ConnectException e) {
			System.err.println("Error al conectar con la API: " + e.getMessage());
		}

		return "redirect:/newload?selectedSavedModel=" + infoForm.getNombre();
	}
}
