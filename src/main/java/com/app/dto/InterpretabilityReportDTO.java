package com.app.dto;

import java.util.List;

public class InterpretabilityReportDTO {
	
	private String fault_info;
	private String fault_type;
	private String analysis_result;
	private List<Double> details_out;
	private List<Double> details_in;
	private List<Double> details_balls;
	private List<Double> details_cage;

	public String getFault_info() {
		return fault_info;
	}

	public void setFault_info(String fault_info) {
		this.fault_info = fault_info;
	}

	public String getFault_type() {
		return fault_type;
	}

	public void setFault_type(String fault_type) {
		this.fault_type = fault_type;
	}

	public String getAnalysis_result() {
		return analysis_result;
	}

	public void setAnalysis_result(String analysis_result) {
		this.analysis_result = analysis_result;
	}

	public List<Double> getDetails_out() {
		return details_out;
	}

	public void setDetails_out(List<Double> details_out) {
		this.details_out = details_out;
	}

	public List<Double> getDetails_in() {
		return details_in;
	}

	public void setDetails_in(List<Double> details_in) {
		this.details_in = details_in;
	}

	public List<Double> getDetails_balls() {
		return details_balls;
	}

	public void setDetails_balls(List<Double> details_balls) {
		this.details_balls = details_balls;
	}

	public List<Double> getDetails_cage() {
		return details_cage;
	}

	public void setDetails_cage(List<Double> details_cage) {
		this.details_cage = details_cage;
	}

}
