package com.chargeback.controller;

public class Record {

	private String diskQuota;

	private String name;
	
	private Usage usage;

		public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDiskQuota() {
		return diskQuota;
	}

	public void setDiskQuota(String diskQuota) {
		this.diskQuota = diskQuota;
	}

	public Usage getUsage() {
		return usage;
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}
	
}
