package com.chargeback.rest.client;

import static com.chargeback.constants.ChargeBackConstants.GET_ORG_LIST;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.chargeback.vo.UsageRecord;

//@FeignClient("CHARGEBACK-API")
@FeignClient(name="CHARGEBACK-API", url="http://lnar-pbbc005.corp.capgemini.com:8081")
public interface ChargeBackApiClient {

	@RequestMapping(value = "/getInstanceMetrics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UsageRecord> getAllApplicationInstanceData();

	@RequestMapping(value = "/getFreeResource/{resourceType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getFreeResourceForResourceType(@PathVariable("resourceType") final String resourceType);

	@RequestMapping(value = GET_ORG_LIST, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getOrgList();

	@RequestMapping(value = "/getSpaceList/{orgName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getSpaceList(@PathVariable("orgName") final String orgName);
}
