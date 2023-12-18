package com.app.dto;

public class DatasetInformationDTO {

	private Integer id;
	private String nombre;
	private Double shaft_frequency;
	private Double sampling_frequency;
	private Double carga;
	private String bearing_type;
	private Double bpfo;
	private Double bpfi;
	private Double bsf;
	private Double ftf;
	private int min_to_check;
	private int max_to_check;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Double getShaft_frequency() {
		return shaft_frequency;
	}

	public void setShaft_frequency(Double shaft_frequency) {
		this.shaft_frequency = shaft_frequency;
	}

	public Double getSampling_frequency() {
		return sampling_frequency;
	}

	public void setSampling_frequency(Double sampling_frequency) {
		this.sampling_frequency = sampling_frequency;
	}

	public Double getCarga() {
		return carga;
	}

	public void setCarga(Double carga) {
		this.carga = carga;
	}

	public String getBearing_type() {
		return bearing_type;
	}

	public void setBearing_type(String bearing_type) {
		this.bearing_type = bearing_type;
	}

	public Double getBpfo() {
		return bpfo;
	}

	public void setBpfo(Double bpfo) {
		this.bpfo = bpfo;
	}

	public Double getBpfi() {
		return bpfi;
	}

	public void setBpfi(Double bpfi) {
		this.bpfi = bpfi;
	}

	public Double getBsf() {
		return bsf;
	}

	public void setBsf(Double bsf) {
		this.bsf = bsf;
	}

	public Double getFtf() {
		return ftf;
	}

	public void setFtf(Double ftf) {
		this.ftf = ftf;
	}

	public int getMin_to_check() {
		return min_to_check;
	}

	public void setMin_to_check(int min_to_check) {
		this.min_to_check = min_to_check;
	}

	public int getMax_to_check() {
		return max_to_check;
	}

	public void setMax_to_check(int max_to_check) {
		this.max_to_check = max_to_check;
	}

}
