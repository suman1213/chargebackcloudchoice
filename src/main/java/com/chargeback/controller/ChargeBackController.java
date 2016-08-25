package com.chargeback.controller;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.chargeback.vo.CostVO;
import com.chargeback.vo.PriceValueSummary;
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
	private static final String INSTANCE_METRICS_URL = "http://chargeback-api.cglean.com/metrics/getInstanceMetrics";
	private static final String FREERESOURRCE_URL = "http://chargeback-api.cglean.com/metrics/getFreeResource";
	
	private static final String ORG_LIST_URL = "http://chargeback-api.cglean.com/metrics/getOrgList";
	private static final String SPACELIST_URL = "http://chargeback-api.cglean.com/metrics/getSpaceList";
	private static final String GETMAX_QUOTA = "http://chargeback-api.cglean.com/metrics/getQuota";
	
	private static final String INFRA_API="http://infrastructure-api.cglean.com/cost?start=2016-08-24&end=2016-08-24";
	private static final String GETQUOTA= "http://chargeback-api.cglean.com/metrics/getTotalQuota";
	@Autowired  private RestTemplate restTemplate; 
	
	//@Autowired private InfraApiClient infraClient; 
	
	
	@RequestMapping(value="/getResourceDetailsSummary", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	private List<PriceValueSummary> getSummary() throws ParseException {
		
		final SimpleDateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd");
		final ResponseEntity<CostVO> infraApiResponse = restTemplate.exchange(INFRA_API, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<CostVO>() {
				});
		//CostVO costVO = infraClient.getCost(dateFormat.format(new Date()), dateFormat.format(new Date()));
		
		CostVO costVO = infraApiResponse.getBody();
		
		List<PriceValueSummary> priceValueSummaryList = new ArrayList<>();
		
		
		/*final ResponseEntity<List<UsageRecord>> instanceMetrics = restTemplate.exchange(INSTANCE_METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<UsageRecord>>() {
				});*/
		
		final ResponseEntity<List<String>> orgListResponse = restTemplate.exchange(ORG_LIST_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<String>>() {
				});
		
	
		NumberFormat format = NumberFormat.getCurrencyInstance();
		
		final ResponseEntity<Double> accountMemoryQuotaResponse = restTemplate.exchange(GETQUOTA + "/" + "MEM"  ,
				HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Double>() {
				});
		double accountMemoryQuota = accountMemoryQuotaResponse.getBody();
		
		final ResponseEntity<Double> accountCPUQuotaResponse = restTemplate.exchange(GETQUOTA + "/" + "CPU" , HttpMethod.GET,
				HttpEntity.EMPTY, new ParameterizedTypeReference<Double>() {
				});
		double accountCPUQuota = accountCPUQuotaResponse.getBody();
		final ResponseEntity<Double> accountDiskQuotaResponse = restTemplate.exchange(GETQUOTA + "/" + "DISK" ,
				HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Double>() {
				});
		Double accountDiskQuota = accountDiskQuotaResponse.getBody();
		
		for (String orgName : orgListResponse.getBody()) {
			
			final ResponseEntity<Double> totalMemoryResponse = restTemplate.exchange(GETMAX_QUOTA + "/" + "MEM" + "/" +orgName ,
					HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Double>() {
					});
			double memoryQuota = totalMemoryResponse.getBody();
			
			final ResponseEntity<Double> totalCPUResponse = restTemplate.exchange(GETMAX_QUOTA + "/" + "CPU" + "/" +orgName, HttpMethod.GET,
					HttpEntity.EMPTY, new ParameterizedTypeReference<Double>() {
					});
			double totalCPUQuota = totalCPUResponse.getBody();
			final ResponseEntity<Double> totalDiskResponse = restTemplate.exchange(GETMAX_QUOTA + "/" + "DISK" + "/" +orgName,
					HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Double>() {
					});
			Double totalDiskQuota = totalDiskResponse.getBody();
			
			PriceValueSummary priceValueSummary = new PriceValueSummary();
			// Sum up for Memory
			
			double pctMemoryUsed = (Double.valueOf(memoryQuota) / Double.valueOf(accountMemoryQuota));
			double amtForMemory =(format.parse(costVO.getMemory()).doubleValue()) * pctMemoryUsed;
			priceValueSummary.setMemory(amtForMemory);
			// Sum up for CPU
		
			double pctCpuUsed = (Double.valueOf(totalCPUQuota) / Double.valueOf(accountCPUQuota));
			double amtForCPU =(format.parse(costVO.getCpu()).doubleValue()) * pctCpuUsed;
			priceValueSummary.setCpu(amtForCPU);
			// SUM for DISK
			
			double pctDiskUsed = (Double.valueOf(totalDiskQuota) / Double.valueOf(accountDiskQuota));
			double amtForDisk = (format.parse(costVO.getDisk()).doubleValue()) * pctDiskUsed;
			priceValueSummary.setDisk(amtForDisk);
			priceValueSummary.setSummary(amtForDisk + amtForCPU + amtForMemory);
			priceValueSummary.setOrgName(orgName);
			priceValueSummaryList.add(priceValueSummary);

		}

		return priceValueSummaryList;
	    
	}
	
	@RequestMapping(value="/getCostDetails/{infoType}/{resourceType}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	private ChartVO getSummaryVal(@PathVariable String infoType , @PathVariable String resourceType){
		final ResponseEntity<List<UsageRecord>> response = restTemplate.exchange(INSTANCE_SUMMARY_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<UsageRecord>>() {
				});
	

		
		Function<ResponseEntity<List<UsageRecord>>, List<String>> usedResourceFunction = null;
		Function<ResponseEntity<List<UsageRecord>>, List<String>> appLabelFunction = null;
		
		
	
		if(resourceType.equals("SUMMARY")){
			usedResourceFunction = summary ->response.getBody()
				.stream().map(usageRecord -> usageRecord.getSummary().replace("$", "")).collect(Collectors.toList());
			appLabelFunction = appLabel ->response.getBody()
					.stream().map(usageRecord -> usageRecord.getOrgName().concat(" ").concat(usageRecord.getSummary())).collect(Collectors.toList());
		
		}else if(resourceType.equals("MEM")){
			usedResourceFunction = usedMemory ->response.getBody()
					.stream().map(usageRecord -> usageRecord.getMemory().replace("$", "")).collect(Collectors.toList());
			appLabelFunction = appLabel ->response.getBody()
					.stream().map(usageRecord -> usageRecord.getOrgName().concat(" ").concat(usageRecord.getMemory())).collect(Collectors.toList());

			
			}else if(resourceType.equals("CPU")){
				usedResourceFunction = usedCPU ->response.getBody()
						.stream().map(usageRecord -> usageRecord.getCpu().replace("$", "")).collect(Collectors.toList());
				appLabelFunction = appLabel ->response.getBody()
						.stream().map(usageRecord -> usageRecord.getOrgName().concat(" ").concat(usageRecord.getCpu())).collect(Collectors.toList());


			}else if(resourceType.equals("DISK")){
				usedResourceFunction = usedCPU ->response.getBody()
						.stream().map(usageRecord -> usageRecord.getDisk().replace("$", "")).collect(Collectors.toList());
				appLabelFunction = appLabel ->response.getBody()
						.stream().map(usageRecord -> usageRecord.getOrgName().concat(" ").concat(usageRecord.getDisk())).collect(Collectors.toList());


			}else{
				throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
			}
		
		/*appLabelFunction = appLabel ->response.getBody()
				.stream().map(usageRecord -> usageRecord.getOrgName().concat(" ").concat(usageRecord.get)).collect(Collectors.toList());
		*/
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