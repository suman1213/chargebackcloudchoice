package com.chargeback.vo;

/**
 * These are value classes only 
 * @author amit
 *
 */
public final class PriceValueSummary {

	public final  double summary;
	
	public final double cpu;
	
	public final double disk;
	
	public final double memory;
	
	public final String orgName;

	public PriceValueSummary(final double summary, final double cpu, final double disk, final double memory, final String orgName) {
		this.summary = summary;
		this.cpu = cpu;
		this.disk = disk;
		this.memory = memory;
		this.orgName = orgName;
	}
	
}
