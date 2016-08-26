package com.chargeback.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chargeback.rest.client.ChargeBackApiClient;
import com.chargeback.rest.client.InfraApiClient;
import com.chargeback.vo.ChartVO;
import com.chargeback.vo.CostVO;
import com.chargeback.vo.PriceValueSummary;
import com.chargeback.vo.UsageRecord;

/**
 * This Controller gives the Chart Data after calling the metrics service to the
 * UI.
 * 
 * @author ambansal
 *
 */
@RestController
public class ChargeBackController {


	@Autowired
	private InfraApiClient infraApiClient;
	
	@Autowired
	private ChargeBackApiClient chargeBackApiClient;
	
	private List<PriceValueSummary> getSummary(final String startDate, final String endDate) throws ParseException {

		final CostVO costVO = infraApiClient.getCost(startDate, endDate);
		final List<String> orgList = chargeBackApiClient.getOrgList();

		final List<PriceValueSummary> priceValueSummaryList = new ArrayList<>();
		final List<UsageRecord> instanceData =  chargeBackApiClient.getAllApplicationInstanceData();
		
		

		final NumberFormat format = NumberFormat.getCurrencyInstance();


		final double allOrgsCpuSum = instanceData.stream()
				.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getCpu())).sum();
		final double allOrgsDiskSum = instanceData.stream()
				.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getDisk())).sum();
		final double allOrgMemorySum = instanceData.stream()
				.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getMemory())).sum();
		for (final String orgName : orgList) {


			final PriceValueSummary priceValueSummary = new PriceValueSummary();
			// Sum up for Memory
			double orgMemorySum = instanceData.stream()
					.filter(usageRecord -> usageRecord.getOrgName().equals(orgName))
					.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getMemory())).sum();

			final double pctMemoryUsed = (Double.valueOf(orgMemorySum) / Double.valueOf(allOrgMemorySum));
			final double amtForMemory = (format.parse(costVO.getMemory()).doubleValue()) * pctMemoryUsed;
			priceValueSummary.setMemory(amtForMemory);
			// Sum up for CPU
			final double orgCpuSum = instanceData.stream()
					.filter(usageRecord -> usageRecord.getOrgName().equals(orgName))
					.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getCpu())).sum();
			final double pctCpuUsed = (Double.valueOf(orgCpuSum) / Double.valueOf(allOrgsCpuSum));
			final double amtForCPU = (format.parse(costVO.getCpu()).doubleValue()) * pctCpuUsed;
			priceValueSummary.setCpu(amtForCPU);
			// SUM for DISK
			final double orgDiskSum = instanceData.stream()
					.filter(usageRecord -> usageRecord.getOrgName().equals(orgName))
					.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getDisk())).sum();
			final double pctDiskUsed = (Double.valueOf(orgDiskSum) / Double.valueOf(allOrgsDiskSum));
			final double amtForDisk = (format.parse(costVO.getDisk()).doubleValue()) * pctDiskUsed;
			priceValueSummary.setDisk(amtForDisk);
			priceValueSummary.setSummary(amtForDisk + amtForCPU + amtForMemory);
			priceValueSummary.setOrgName(orgName);
			priceValueSummaryList.add(priceValueSummary);

		}

		return priceValueSummaryList;

	}

	@RequestMapping(value = "/getCostDetails/{infoType}/{resourceType}/{startDate}/{endDate}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	private ChartVO getSummaryVal(@PathVariable String infoType, @PathVariable String resourceType, @PathVariable final String startDate, @PathVariable final String endDate)
			throws ParseException {

		final List<PriceValueSummary> priceValueSummaryList = getSummary(startDate,endDate);

		Function<List<PriceValueSummary>, List<String>> usedResourceFunction = null;
		Function<List<PriceValueSummary>, List<String>> appLabelFunction = null;

		if (resourceType.equals("SUMMARY")) {
			usedResourceFunction = summary -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.getSummary())).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(usageRecord -> usageRecord.getOrgName()
					.concat(" ").concat(String.valueOf(usageRecord.getSummary()))).collect(Collectors.toList());

		} else if (resourceType.equals("MEM")) {
			usedResourceFunction = usedMemory -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.getMemory())).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(
					usageRecord -> usageRecord.getOrgName().concat(" ").concat(String.valueOf(usageRecord.getMemory())))
					.collect(Collectors.toList());

		} else if (resourceType.equals("CPU")) {
			usedResourceFunction = usedCPU -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.getCpu())).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(
					usageRecord -> usageRecord.getOrgName().concat(" ").concat(String.valueOf(usageRecord.getCpu())))
					.collect(Collectors.toList());

		} else if (resourceType.equals("DISK")) {
			usedResourceFunction = usedCPU -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.getDisk())).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(
					usageRecord -> usageRecord.getOrgName().concat(" ").concat(String.valueOf(usageRecord.getDisk())))
					.collect(Collectors.toList());
		} else {
			throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
		}

		return getParameterizedUsageDetails(priceValueSummaryList, usedResourceFunction, appLabelFunction);

	}

	/**
	 * 
	 * @param usageType
	 * @param resourceType
	 * @param orgName
	 * @param space
	 * @return
	 */
	@RequestMapping(value = "/getResourceDetails/{usageType}/{resourceType}/{orgName:.+}/{space:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ChartVO getResourceUsage(@PathVariable String usageType, @PathVariable final String resourceType,
			@PathVariable final String orgName, @PathVariable final String space) {
	
		List<UsageRecord> instanceData =  chargeBackApiClient.getAllApplicationInstanceData();

		Function<List<UsageRecord>, List<String>> usedResourceFunction = null;
		Function<List<UsageRecord>, List<String>> appLabelFunction = null;

		if (resourceType.equals("MEM")) {
			usedResourceFunction = usedMemory -> instanceData.stream()
					.filter(usageRecord -> (usageRecord.getOrgName().equals(orgName)
							&& usageRecord.getSpaceName().equals(space)))
					.map(usageRecord -> usageRecord.getMemory()).collect(Collectors.toList());

		} else if (resourceType.equals("CPU")) {
			usedResourceFunction = usedCPU -> instanceData.stream()
					.filter(usageRecord -> (usageRecord.getOrgName().equals(orgName)
							&& usageRecord.getSpaceName().equals(space)))
					.map(usageRecord -> usageRecord.getCpu()).collect(Collectors.toList());

		} else if (resourceType.equals("DISK")) {
			usedResourceFunction = usedCPU -> instanceData.stream()
					.filter(usageRecord -> (usageRecord.getOrgName().equals(orgName)
							&& usageRecord.getSpaceName().equals(space)))
					.map(usageRecord -> usageRecord.getDisk()).collect(Collectors.toList());

		} else {
			throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
		}

		appLabelFunction = appLabel -> instanceData.stream().filter(
				usageRecord -> (usageRecord.getOrgName().equals(orgName) && usageRecord.getSpaceName().equals(space)))
				.map(usageRecord -> usageRecord.getAppname().concat(" - ").concat(usageRecord.getInstanceIndex()))
				.collect(Collectors.toList());

		if (usageType.equals("UNUSED")) {
			if (!resourceType.equals("DISK")) {
				String freeResource = chargeBackApiClient.getFreeResourceForResourceType(resourceType);
				return getUnUsedResource(instanceData, freeResource, usedResourceFunction, appLabelFunction);
			} else {
				throw new RuntimeException("Not able to get total disk usage as of now");
			}

		}
		return getUsageDetails(instanceData, usedResourceFunction, appLabelFunction);
	}

	@RequestMapping(value = "/getOrgList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getOrganizationNames() {
		
		return chargeBackApiClient.getOrgList();
	}

	@RequestMapping(value = "/getSpaceList/{orgName:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getSpaceList(@PathVariable String orgName) throws UnsupportedEncodingException {

		return chargeBackApiClient.getSpaceList(URLEncoder.encode(orgName, "UTF-8"));

	}

	private ChartVO getUnUsedResource(final List<UsageRecord> response,
			final String freeResourceVal,
			Function<List<UsageRecord>, List<String>> freeResourceFunction,
			Function<List<UsageRecord>, List<String>> appLabelFunction) {
		final List<String> freeResource = freeResourceFunction.apply(response);
		final List<String> appLabel = appLabelFunction.apply(response);
		freeResource.add(freeResourceVal);
		appLabel.add("Unutilised");
		ChartVO chartVO = new ChartVO();
		chartVO.setData(freeResource);
		chartVO.setLabel(appLabel);
		return chartVO;
	}

	private ChartVO getUsageDetails(final List<UsageRecord> response,
			Function<List<UsageRecord>, List<String>> resourceUsedFunction,
			Function<List<UsageRecord>, List<String>> appLabelFunction) {

		final List<String> resourceUsed = resourceUsedFunction.apply(response);
		final List<String> appLabel = appLabelFunction.apply(response);

		final ChartVO chartVO = new ChartVO();
		chartVO.setData(resourceUsed);
		chartVO.setLabel(appLabel);
		return chartVO;
	}

	private ChartVO getParameterizedUsageDetails(final List<PriceValueSummary> summaryList,
			Function<List<PriceValueSummary>, List<String>> resourceUsedFunction,
			Function<List<PriceValueSummary>, List<String>> appLabelFunction) {

		final List<String> resourceUsed = resourceUsedFunction.apply(summaryList);
		final List<String> appLabel = appLabelFunction.apply(summaryList);

		final ChartVO chartVO = new ChartVO();
		chartVO.setData(resourceUsed);
		chartVO.setLabel(appLabel);
		return chartVO;
	}
}