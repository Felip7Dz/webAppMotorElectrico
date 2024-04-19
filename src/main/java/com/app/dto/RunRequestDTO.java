package com.app.dto;

public class RunRequestDTO {
	private String nombre_req;
	private Double shaft_frequency_req;
	private Double sampling_frequency_req;
	private Double bpfo_req;
	private Double bpfi_req;
	private Double bsf_req;
	private Double ftf_req;
	private int healthy_number_req;
	private int analyzed_number_req;
	private int first_sample_req;

	public String getNombre_req() {
		return nombre_req;
	}

	public void setNombre_req(String nombre_req) {
		this.nombre_req = nombre_req;
	}

	public Double getShaft_frequency_req() {
		return shaft_frequency_req;
	}

	public void setShaft_frequency_req(Double shaft_frequency_req) {
		this.shaft_frequency_req = shaft_frequency_req;
	}

	public Double getSampling_frequency_req() {
		return sampling_frequency_req;
	}

	public void setSampling_frequency_req(Double sampling_frequency_req) {
		this.sampling_frequency_req = sampling_frequency_req;
	}

	public Double getBpfo_req() {
		return bpfo_req;
	}

	public void setBpfo_req(Double bpfo_req) {
		this.bpfo_req = bpfo_req;
	}

	public Double getBpfi_req() {
		return bpfi_req;
	}

	public void setBpfi_req(Double bpfi_req) {
		this.bpfi_req = bpfi_req;
	}

	public Double getBsf_req() {
		return bsf_req;
	}

	public void setBsf_req(Double bsf_req) {
		this.bsf_req = bsf_req;
	}

	public Double getFtf_req() {
		return ftf_req;
	}

	public void setFtf_req(Double ftf_req) {
		this.ftf_req = ftf_req;
	}

	public int getHealthy_number_req() {
		return healthy_number_req;
	}

	public void setHealthy_number_req(int healthy_number_req) {
		this.healthy_number_req = healthy_number_req;
	}

	public int getAnalyzed_number_req() {
		return analyzed_number_req;
	}

	public void setAnalyzed_number_req(int analyzed_number_req) {
		this.analyzed_number_req = analyzed_number_req;
	}

	public int getFirst_sample_req() {
		return first_sample_req;
	}

	public void setFirst_sample_req(int first_sample_req) {
		this.first_sample_req = first_sample_req;
	}
}
