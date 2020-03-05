package org.danielr;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public class GetCarbonIntensityRegionalTests {
	
	@SuppressWarnings("unchecked")
	@Test
	public void sortedListOfRegionsbyHighestIntensity() {
		Response response = getRegion("/regional");
		List<Map<String, Object>> regions = response.jsonPath().getList("data.regions");
		


		
		((List)regions.get(0)).stream()
		.sorted((region1, region2) -> Integer.compare(
                		((Map<String,Map<String, Integer>>)region2).get("intensity").get("forecast"), 
                		((Map<String,Map<String, Integer>>)region1).get("intensity").get("forecast")))

		.forEach(region -> {
			var forecast = ((Map<String,Map<String, Integer>>)region).get("intensity").get("forecast");
			var shortname = ((Map<String, String>)region).get("shortname");
			System.out.println(forecast + ", " + shortname);
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void ensureEnergyMixSumsTo100() {
		Response response = getRegion("/regional");
		List<Map<String, Object>> regions = response.jsonPath().getList("data.regions");
		
		((List<Map<String, ?>>)regions.get(0)).stream()
		
		.forEach(region -> {
			float acc = 0;
			var generationmixes = ((Map<String,List<Map<String, Number>>>)region).get("generationmix");
			for(Map<String, Number> regionmix: generationmixes) {
				float perc = regionmix.get("perc").floatValue();
				acc = acc + perc;
			}
			System.out.println("total: " + acc);
			Assert.assertEquals(acc, 100, 0.01);
		});
	}
	
	@Test
	public void Forecast() {
	Response response = getRegion("/intensity");
	List<Object> forecastValue = response.jsonPath().getList("data.intensity.forecast");
	System.out.println("Forecast is: " + forecastValue);
	}

	private Response getRegion(String region) {
		// BaseURI
		RestAssured.baseURI = "https://api.carbonintensity.org.uk";

		// request object
		RequestSpecification httpRequest = RestAssured.given();

		// response object
		Response response = httpRequest.request(Method.GET, region);

		// print response to console window
		String responseBody = response.getBody().asString();
		System.out.println("Response body is: " + responseBody);

		// status code
		int statusCode = response.getStatusCode();
		System.out.println("Status code is: " + statusCode);
		Assert.assertEquals(statusCode, 200);

		// status line
		String statusLine = response.getStatusLine();
		System.out.println("Status line is: " + statusLine);
		Assert.assertEquals(statusLine, "HTTP/1.1 200 OK");

		return response;
	}

}
