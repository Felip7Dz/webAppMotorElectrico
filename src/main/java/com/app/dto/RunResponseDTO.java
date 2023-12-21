package com.app.dto;

import java.util.List;

public class RunResponseDTO {
	private boolean fault_detected;
	private String fault_info;
	private List<String> fault_type;
	private List<List<Double>> fault_details;
	private String analysis_result;

	public boolean isFault_detected() {
		return fault_detected;
	}

	public void setFault_detected(boolean fault_detected) {
		this.fault_detected = fault_detected;
	}

	public String getFault_info() {
		return fault_info;
	}

	public void setFault_info(String fault_info) {
		this.fault_info = fault_info;
	}

	public List<String> getFault_type() {
		return fault_type;
	}

	public void setFault_type(List<String> fault_type) {
		this.fault_type = fault_type;
	}

	public List<List<Double>> getFault_details() {
		return fault_details;
	}

	public void setFault_details(List<List<Double>> fault_details) {
		this.fault_details = fault_details;
	}

	public String getAnalysis_result() {
		return analysis_result;
	}

	public void setAnalysis_result(String analysis_result) {
		this.analysis_result = analysis_result;
	}

}
