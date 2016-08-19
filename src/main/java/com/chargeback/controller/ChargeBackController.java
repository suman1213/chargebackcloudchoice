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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.chargeback.vo.ChartVO;
import com.chargeback.vo.UsageRecord;


/**
 *  This Controller gives the Chart Data after calling the metrics service to the UI.
 * @author ambansal
 *
 */
@RestController
public class ChargeBackController {
	
	
	private static final String INSTANCE_SUMMARY_URL = "http://localhost:8080/chargeback/getResourceDetailsSummary";
	// TODO :: Need to fetch this from Eureka Server Client Id by just giving application name 
	private static final String INSTANCE_METRICS_URL = "http://chargeback-api.cfapps.io/metrics/getInstanceMetrics";
	private static final String FREERESOURRCE_URL = "http://chargeback-api.cfapps.io/metrics/getFreeResource";
	
	private static final String ORG_LIST_URL = "http://chargeback-api.cfapps.io/metrics/getOrgList";
	private static final String SPACELIST_URL = "http://chargeback-api.cfapps.io/metrics/getSpaceList";

	@Autowired  private RestTemplate restTemplate; 
	
	
	
	/*
	@GET
	@Path("/friends")
	@Produces("application/json")*/
	@RequestMapping(value="/getResourceDetailsSummary", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	private String getSummary() {
		
		String jsonVal="[  "+
				"{  "+
				"\"summary\":\"1000\","+
				"\"cpu\":\"500\","+
				"\"memory\":\"400\","+
				"\"disk\":\"100\","+
				"\"orgName\":\"Org-1\""+
				"},"+
				"{ "+
				"\"summary\":\"5000.00\","+
				"\"cpu\":\"1000\","+
				"\"memory\":\"3000\","+
				"\"disk\":\"1000\","+
				"\"orgName\":\"Org-2\""+
				"},"+
				"{  "+
				"\"summary\":\"500.00\","+
				"\"cpu\":\"100\","+
				"\"memory\":\"350\","+
				"\"disk\":\"50\","+
				"\"orgName\":\"Org-3\""+
				"}"+
				"]";
		
		return jsonVal;
		
	    
	}
	
	@RequestMapping(value="/getResourceDetails/{infoType}/{resourceType}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	private ChartVO getSummaryVal(@PathVariable String infoType , @PathVariable String resourceType){
		final ResponseEntity<List<UsageRecord>> response = restTemplate.exchange(INSTANCE_SUMMARY_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<UsageRecord>>() {
				});
		
		Function<ResponseEntity<List<UsageRecord>>, List<String>> usedResourceFunction = null;
		Function<ResponseEntity<List<UsageRecord>>, List<String>> appLabelFunction = null;
		
		
		 /*appLabel ->response.getBody()
			.stream().filter(usageRecord -> (usageRecord.getOrgName().equals(orgName) && usageRecord.getSpaceName().equals(space) )).map(usageRecord -> usageRecord.getAppname().concat(" - ")
					.concat(usageRecord.getInstanceIndex())).collect(Collectors.toList());
					
					
					map(usageRecord -> usageRecord.getDisk()).collect(Collectors.toList());
*/
		if(resourceType.equals("SUMMARY")){
			usedResourceFunction = summary ->response.getBody()
				.stream().map(usageRecord -> usageRecord.getSummary().replace("$", "")).collect(Collectors.toList());
		
		}else if(resourceType.equals("MEM")){
			usedResourceFunction = usedMemory ->response.getBody()
					.stream().map(usageRecord -> usageRecord.getMemory().replace("$", "")).collect(Collectors.toList());
			
			}else if(resourceType.equals("CPU")){
				usedResourceFunction = usedCPU ->response.getBody()
						.stream().map(usageRecord -> usageRecord.getCpu().replace("$", "")).collect(Collectors.toList());

			}else if(resourceType.equals("DISK")){
				usedResourceFunction = usedCPU ->response.getBody()
						.stream().map(usageRecord -> usageRecord.getDisk().replace("$", "")).collect(Collectors.toList());

			}else{
				throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
			}
		
		appLabelFunction = appLabel ->response.getBody()
				.stream().map(usageRecord -> usageRecord.getOrgName()).collect(Collectors.toList());
		
		return getParameterizedUsageDetails(response, usedResourceFunction, appLabelFunction);
		
	}
	

	/**
	 * 
	 * @param usageType
	 * @param resourceType
	 * @param orgName
	 * @param space
	 * @return
	 */
	@RequestMapping(value="/getResourceDetails/{usageType}/{resourceType}/{orgName:.+}/{space:.+}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	 public ChartVO getResourceUsage(@PathVariable String usageType , @PathVariable String resourceType,  @PathVariable String orgName, @PathVariable String space){
		final ResponseEntity<List<UsageRecord>> response = restTemplate.exchange(INSTANCE_METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<UsageRecord>>() {
				});
		
		Function<ResponseEntity<List<UsageRecord>>, List<String>> usedResourceFunction = null;
		Function<ResponseEntity<List<UsageRecord>>, List<String>> appLabelFunction = null;
		
		if(resourceType.equals("MEM")){
		usedResourceFunction = usedMemory ->response.getBody()
				.stream().filter(usageRecord -> (usageRecord.getOrgName().equals(orgName) && usageRecord.getSpaceName().equals(space) ))
				.map(usageRecord -> usageRecord.getMemory()).collect(Collectors.toList());
		
		}else if(resourceType.equals("CPU")){
			usedResourceFunction = usedCPU ->response.getBody()
					.stream().filter(usageRecord -> (usageRecord.getOrgName().equals(orgName) && usageRecord.getSpaceName().equals(space) ))
					.map(usageRecord -> usageRecord.getCpu()).collect(Collectors.toList());

		}else if(resourceType.equals("DISK")){
			usedResourceFunction = usedCPU ->response.getBody()
					.stream().filter(usageRecord -> (usageRecord.getOrgName().equals(orgName) && usageRecord.getSpaceName().equals(space) ))
					.map(usageRecord -> usageRecord.getDisk()).collect(Collectors.toList());

		}else{
			throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
		}
		
		appLabelFunction = appLabel ->response.getBody()
				.stream().filter(usageRecord -> (usageRecord.getOrgName().equals(orgName) && usageRecord.getSpaceName().equals(space) )).map(usageRecord -> usageRecord.getAppname().concat(" - ")
						.concat(usageRecord.getInstanceIndex())).collect(Collectors.toList());

		if(usageType.equals("UNUSED")){
			if(!resourceType.equals("DISK")){
			final ResponseEntity<String> freeResourceResponse = restTemplate.exchange(FREERESOURRCE_URL + "/" + resourceType, HttpMethod.GET, HttpEntity.EMPTY,
					new ParameterizedTypeReference<String>() {
					});
			return getUnUsedResource(response, freeResourceResponse, usedResourceFunction, appLabelFunction);
			}else{
				throw new RuntimeException("Not able to get total disk usage as of now");
			}

		}
		return getParameterizedUsageDetails(response, usedResourceFunction, appLabelFunction);
	 }
	
@RequestMapping(value="/getOrgList", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
public List<String> getOrganizationNames(){
	
	final ResponseEntity<List<String>> response = restTemplate.exchange(ORG_LIST_URL, HttpMethod.GET, HttpEntity.EMPTY,
			new ParameterizedTypeReference<List<String>>() {
			});
	return response.getBody();
}

@RequestMapping(value = "/getSpaceList/{orgName:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)	
public List<String> getSpaceList(@PathVariable String orgName){
	
	final ResponseEntity<List<String>> response = restTemplate.exchange(SPACELIST_URL + "/" + orgName , HttpMethod.GET, HttpEntity.EMPTY,
			new ParameterizedTypeReference<List<String>>() {
			});
	return response.getBody();
}
	
	private ChartVO getUnUsedResource(final ResponseEntity<List<UsageRecord>> response, final ResponseEntity<String> freeResourceResponse, Function<ResponseEntity<List<UsageRecord>>, List<String>> freeResourceFunction,Function<ResponseEntity<List<UsageRecord>>, List<String>> appLabelFunction) {
		final List<String> freeResource = freeResourceFunction.apply(response);
		final List<String> appLabel = appLabelFunction.apply(response);
		freeResource.add(freeResourceResponse.getBody());
		appLabel.add("Unutilised");
		ChartVO chartVO = new ChartVO();
		chartVO.setData(freeResource);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
	

private ChartVO getParameterizedUsageDetails(final ResponseEntity<List<UsageRecord>> response, Function<ResponseEntity<List<UsageRecord>>, List<String>> resourceUsedFunction, Function<ResponseEntity<List<UsageRecord>>, List<String>> appLabelFunction){
	
	final List<String> resourceUsed = resourceUsedFunction.apply(response);
	final List<String> appLabel = appLabelFunction.apply(response);
	
	final ChartVO chartVO = new ChartVO();
	chartVO.setData(resourceUsed);
	chartVO.setLabel(appLabel);
	return chartVO;
}
}