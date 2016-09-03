package com.chargeback.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * This Controller is meant for UI purposes only to show the charge.html
 * @author ambansal
 *
 */
@Controller
public class ChargeBackViewController {
	private Log log = LogFactory.getLog(ChargeBackViewController.class);

	/**
	 * This method returns the main HTML page containing the chargeback details
	 * @return This returns the view name 
	 */
	@RequestMapping("/")
	public String viewUsageDetails() {
		log.info("Returning charge.html");
		// TODO:: Add suffix in application.properties file so that .html need not be added 
	    // Move the constants to Constant file 
		return "charge.html";
	}
}
