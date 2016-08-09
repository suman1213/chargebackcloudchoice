package com.chargeback.controller;

import java.util.List;
import java.util.stream.Collectors;

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

import com.chargeback.vo.ChartVO;
import com.chargeback.vo.Stats;
/**
 *  This Controller gives the Chart Data after calling the metrics service to the UI.
 * @author ambansal
 *
 */
@RestController
public class ChargeBackController {

	// TODO :: Need to fetch this from Eureka Server Client Id by just giving application name 
	public static final String METRICS_URL = "http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getmetrics";
	public static final String FREEMEM_URL = "http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getFreeMematOrg";
	
	@Autowired private RestTemplate restTemplate; 

	/**
	 * This Method gives the Memory usage Details of the Application. Usage is
	 * from the Memory Quota allocated to each application
	 * 
	 * @return Returns the ChartVO in json formatwith label as list and and data
	 *         as list
	 */
	@RequestMapping(value = "/getDetails", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
	public ChartVO getMemUsageDetails() {
			final ResponseEntity<List<Stats>> response = restTemplate.exchange(METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<Stats>>() {
				});
			return getMemoryUsageDetails(response);
	}

	/**
	 * This Method gives the free Memory of each Application as well as total
	 * Unutilised memory at the Org Level Free memory for each application is
	 * calculated by subtracting the usage from the quota assigned. Free memory
	 * at org level is calculated by subtracting the used memory of each
	 * application from the total memory at each account level
	 * 
	 * @return Returns the ChartVO in json format with label as list and and data as list
	 */
	@RequestMapping(value="/getUnusedDetails", produces={MediaType.APPLICATION_JSON_VALUE}, method=RequestMethod.GET)
	public ChartVO getUnutilizedMemoryDetails(){
	
		final ResponseEntity<List<Stats>> response = restTemplate.exchange(METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
		new ParameterizedTypeReference<List<Stats>>() {
		});
		final ResponseEntity<Long> frememResponse = restTemplate.exchange(FREEMEM_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<Long>() {
				});
		return getUnUtilizedMemoryDetails(response, frememResponse);
	}

	/**
	 *  This Method calculates the Unutilized  Memory from the data received from metrics Service 
	 * @param response
	 * @param frememResponse
	 * @return
	 */
	private ChartVO getUnUtilizedMemoryDetails(final ResponseEntity<List<Stats>> response, final ResponseEntity<Long> frememResponse) {
		final List<String> memFree = response.getBody().stream().map(e -> e.getRecords()).flatMap(record  -> record.stream())
		.map(r-> Long.valueOf(r.getMemQuota()) -Long.valueOf(r.getUsage().getMem())).map(e -> String.valueOf(e)).collect(Collectors.toList());
		final List<String> appLabel = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
				.stream().map(r -> r.getName()).collect(Collectors.toList());
		final String freememAtOrg = String.valueOf(frememResponse.getBody());
		memFree.add(freememAtOrg);
		appLabel.add("Unutilised");
		ChartVO chartVO = new ChartVO();
		chartVO.setData(memFree);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
	
	/**
	 * This Method calculates the Utilized Memory from the data received from
	 * metrics Service
	 * 
	 * @param response
	 * @return Returns the details in ChartVO
	 */
	private ChartVO getMemoryUsageDetails(final ResponseEntity<List<Stats>> response){
		
		final List<String> memUsed = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
				.stream().map(r -> r.getUsage().getMem()).collect(Collectors.toList());	
		
		final List<String> appLabel = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
					.stream().map(r -> r.getName()).collect(Collectors.toList());
		
		final ChartVO chartVO = new ChartVO();
		chartVO.setData(memUsed);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
}