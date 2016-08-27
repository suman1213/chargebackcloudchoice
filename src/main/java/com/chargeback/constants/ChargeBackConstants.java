package com.chargeback.constants;

public class ChargeBackConstants {


	public static final String GET_COST_DETAILS = "/getCostDetails/{infoType}/{resourceType}/{startDate}/{endDate}";
	public static final String GET_USAGE_DETAILS = "/getResourceDetails/{usageType}/{resourceType}/{orgName:.+}/{space:.+}";
	public static final String SUMMARY = "SUMMARY";
	public static final String MEMORY = "MEM";
	public static final String CPU = "CPU";
	public static final String DISK = "DISK";
	public static final String GET_ORG_LIST = "/getOrgList";
	public static final String GET_SPACE_LIST = "/getSpaceList/{orgName:.+}";
	
	public static final String UNUSED  = "UNUSED";
	public static final String UNUTILISED =  "Unutilised";
	private ChargeBackConstants() {
	}

}
