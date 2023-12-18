package com.app.dto;

import java.util.List;

public class DatasetsResponseDTO {
	private List<String> modelsList;
	private List<String> modelsNames;

	public List<String> getModelsList() {
		return modelsList;
	}

	public void setModelsList(List<String> modelsList) {
		this.modelsList = modelsList;
	}

	public List<String> getModelsNames() {
		return modelsNames;
	}

	public void setModelsNames(List<String> modelsNames) {
		this.modelsNames = modelsNames;
	}
}
