package com.chargeback.vo;

public class PriceValueSummary {

	private double summary;
	
	private double cpu;
	
	private double disk;
	
	private double memory;
	
	private String orgName;

	

	public double getSummary() {
		return summary;
	}

	public void setSummary(double summary) {
		this.summary = summary;
	}

	public double getCpu() {
		return cpu;
	}

	public void setCpu(double cpu) {
		this.cpu = cpu;
	}

	public double getDisk() {
		return disk;
	}

	public void setDisk(double disk) {
		this.disk = disk;
	}

	public double getMemory() {
		return memory;
	}

	public void setMemory(double memory) {
		this.memory = memory;
	}

	public void setMemory(long memory) {
		this.memory = memory;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
	
}
