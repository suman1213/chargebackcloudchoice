package com.chargeback.controller;

import java.util.List;
import java.util.stream.Collectors;

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

	

	@RequestMapping(value = "/getDetails", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
	public ChartVO getMemUsageDetails() {

		final String uri = "http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getmetrics";
		RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<List<Stats>> response = restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<Stats>>() {
				});
			return getMemoryUsageDetails(response);
		
		
	}

	@RequestMapping(value="/getUnusedDetails", produces={MediaType.APPLICATION_JSON_VALUE}, method=RequestMethod.GET)
	public ChartVO getUnutilizedMemoryDetails(){
		final String uri = "http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getmetrics";

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<Stats>> response = restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY,
		new ParameterizedTypeReference<List<Stats>>() {
		});
		
		return getUnUtilizedMemoryDetails(response);
		
	}

	private ChartVO getUnUtilizedMemoryDetails(ResponseEntity<List<Stats>> response) {
		final List<String> memFree = response.getBody().stream().map(e -> e.getRecords()).flatMap(record  -> record.stream())
		.map(r-> Long.valueOf(r.getMemQuota()) -Long.valueOf(r.getUsage().getMem())).map(e -> String.valueOf(e)).collect(Collectors.toList());
		final List<String> appLabel = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
				.stream().map(r -> r.getName()).collect(Collectors.toList());
		ChartVO chartVO = new ChartVO();
		chartVO.setData(memFree);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
	
	
	private ChartVO getMemoryUsageDetails(ResponseEntity<List<Stats>> response){
		final List<String> memUsed = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
				.stream().map(r -> r.getUsage().getMem()).collect(Collectors.toList());	
		final List<String> appLabel = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
					.stream().map(r -> r.getName()).collect(Collectors.toList());
		
		ChartVO chartVO = new ChartVO();
		chartVO.setData(memUsed);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
}
