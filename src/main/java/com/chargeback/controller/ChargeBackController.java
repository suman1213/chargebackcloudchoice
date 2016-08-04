package com.chargeback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ChargeBackController {

	@Autowired
	private RestTemplate restTemplate;
	
	
	@RequestMapping(value="/view", produces = {
	        MediaType.TEXT_HTML_VALUE},  
	        method = RequestMethod.GET)
	public String viewUsageDetails () {
	    return "charge-memory";
	}
	
	
	
	@RequestMapping(value="/getDetails", produces = {
	        MediaType.APPLICATION_JSON_VALUE},  
	        method = RequestMethod.GET)
	public List<Records> v () {
		
		
		 ResponseEntity<List<Records>> response = restTemplate.exchange("http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getmetrics", 
				 HttpMethod.GET, HttpEntity.EMPTY, 
				 new ParameterizedTypeReference<List<Records>>(){});	
		 
		 System.out.println(restTemplate.getForObject("http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getmetrics", Records[].class).toString());
	    List<Records> records = response.getBody();
	    System.out.println(records.toString());
		 return records;
	}
	
	
	
	
}
