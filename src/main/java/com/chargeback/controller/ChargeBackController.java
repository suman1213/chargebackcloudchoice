package com.chargeback.controller;

import java.util.List;
import java.util.function.Function;
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
	private static final String METRICS_URL = "http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getmetrics";
	private static final String FREERESOURRCE_URL = "http://metricsfetchdemo-unflaming-overcensoriousness.cfapps.io/metrics/getFreeResource";
	
	@Autowired private RestTemplate restTemplate; 

	/**
	 * This Method gives the Memory usage Details of the Application. Usage is
	 * from the Memory Quota allocated to each application
	 * 
	 * @return Returns the ChartVO in json format with label as list and and data
	 *         as list
	 */
	@RequestMapping(value = "/getDetails", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.GET)
	public ChartVO getMemUsageDetails() {
			final ResponseEntity<List<Stats>> response = restTemplate.exchange(METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<Stats>>() {
				});
			
			Function<ResponseEntity<List<Stats>>, List<String>> memUsedLambda = memused ->response.getBody().stream().map(stat -> stat.getRecords()).flatMap(records-> records.stream()).collect(Collectors.toList())
					.stream().map(record -> record.getUsage().getMem()).collect(Collectors.toList());
			return getUsageDetails(response,memUsedLambda);
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
		final ResponseEntity<String> frememResponse = restTemplate.exchange(FREERESOURRCE_URL + "/MEM", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<String>() {
				});
		
		Function<ResponseEntity<List<Stats>>, List<String>> unUsedMemoryLambda = unUsedMem -> response.getBody().stream().map(e -> e.getRecords()).flatMap(record  -> record.stream())
		.map(r-> Long.valueOf(r.getMemQuota()) -Long.valueOf(r.getUsage().getMem())).map(e -> String.valueOf(e)).collect(Collectors.toList());
		return getUnUtilizedResourceDetails(response, frememResponse, unUsedMemoryLambda);
	}
	
	/**
	 * This Method gives the CPU usage Details of the Application. Usage is
	 * from the CPU allocated to each application
	 * 
	 * @return Returns the ChartVO in JSON format with label as list and and data
	 *         as list
	 */
	@RequestMapping(value="/getCPUUsage", produces={MediaType.APPLICATION_JSON_VALUE}, method=RequestMethod.GET)
	public ChartVO getCPUUsage(){
	
		final ResponseEntity<List<Stats>> response = restTemplate.exchange(METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
		new ParameterizedTypeReference<List<Stats>>() {
		});
		
		Function<ResponseEntity<List<Stats>>, List<String>> cpuUsedLambda = cpuUsed ->response.getBody().stream().map(stat -> stat.getRecords()).flatMap(records-> records.stream()).collect(Collectors.toList())
				.stream().map(record -> record.getUsage().getCpu()).collect(Collectors.toList());
		return getUsageDetails(response,cpuUsedLambda);
	}
	
	/**
	 * This Method gives the free CPU of each Application as well as total
	 * Free CPU at the Org Level Free cpu for each application is
	 * calculated by subtracting the usage from the quota assigned. 
	 * 
	 * @return Returns the ChartVO in json format with label as list and and data as list
	 */
	@RequestMapping(value="/getFreeCPUDetails", produces={MediaType.APPLICATION_JSON_VALUE}, method=RequestMethod.GET)
	public ChartVO getUnUsedCPUDetails(){
	
		final ResponseEntity<List<Stats>> response = restTemplate.exchange(METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
		new ParameterizedTypeReference<List<Stats>>() {
		});
		final ResponseEntity<String> freeCPUResponse = restTemplate.exchange(FREERESOURRCE_URL + "/CPU", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<String>() {
				});
		Function<ResponseEntity<List<Stats>>, List<String>> freeCPULambda = freeCPU -> response.getBody().stream().map(e -> e.getRecords()).flatMap(record  -> record.stream())
				.map(r-> r.getUsage().getCpu()).collect(Collectors.toList());

		return getUnUtilizedResourceDetails(response, freeCPUResponse, freeCPULambda);
		}
	
	/**
	 * This Method gives the DISK usage Details of the Application. Usage is
	 * from the DISK allocated to each application
	 * 
	 * @return Returns the ChartVO in JSON format with label as list and and data
	 *         as list
	 */
	@RequestMapping(value="/getDiskUsage", produces={MediaType.APPLICATION_JSON_VALUE}, method=RequestMethod.GET)
	public ChartVO getDiskUsage(){
	
		final ResponseEntity<List<Stats>> response = restTemplate.exchange(METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
		new ParameterizedTypeReference<List<Stats>>() {
		});
		
		Function<ResponseEntity<List<Stats>>, List<String>> diskUsageLambda = diskUsed ->response.getBody().stream().map(stat -> stat.getRecords()).flatMap(records-> records.stream()).collect(Collectors.toList())
				.stream().map(record -> record.getUsage().getDisk()).collect(Collectors.toList());
		return getUsageDetails(response,diskUsageLambda);
	}

	/**
	 *  This Method calculates the Unutilized  Memory from the data received from metrics Service 
	 * @param response
	 * @param frememResponse
	 * @return
	 */
	private ChartVO getUnUtilizedResourceDetails(final ResponseEntity<List<Stats>> response, final ResponseEntity<String> freeResourceResponse, Function<ResponseEntity<List<Stats>>, List<String>> function) {
		final List<String> freeResource = function.apply(response);
		final List<String> appLabel = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
				.stream().map(r -> r.getName()).collect(Collectors.toList());
		freeResource.add(freeResourceResponse.getBody());
		appLabel.add("Unutilised");
		ChartVO chartVO = new ChartVO();
		chartVO.setData(freeResource);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
	
	/**
	 * This Method calculates the Utilized Resource from the data received from
	 * metrics Service
	 * 
	 * @param response
	 * @return Returns the details in ChartVO
	 */
private ChartVO getUsageDetails(final ResponseEntity<List<Stats>> response, Function<ResponseEntity<List<Stats>>, List<String>> function){
		
		final List<String> cpuUsed = function.apply(response);
		final List<String> appLabel = response.getBody().stream().map(o -> o.getRecords()).flatMap(l -> l.stream()).collect(Collectors.toList())
					.stream().map(r -> r.getName()).collect(Collectors.toList());
		
		final ChartVO chartVO = new ChartVO();
		chartVO.setData(cpuUsed);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
}