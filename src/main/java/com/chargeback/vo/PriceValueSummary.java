package com.chargeback.vo;

/**
 * These are value classes only 
 * @author amit
 *
 */
public class PriceValueSummary {

	public final  double summary;
	
	public final double cpu;
	
	public final double disk;
	
	public final double memory;
	
	public final String orgName;

	public PriceValueSummary(double summary, double cpu, double disk, double memory, String orgName) {
		this.summary = summary;
		this.cpu = cpu;
		this.disk = disk;
		this.memory = memory;
		this.orgName = orgName;
	}
	
}
