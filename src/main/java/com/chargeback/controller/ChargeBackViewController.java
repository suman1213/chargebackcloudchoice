package com.chargeback.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ChargeBackViewController {

	@RequestMapping("/view")
	public String viewUsageDetails() {
		
	        return "charge.html";
	}
}
