package com.example.hoverfly;

import io.restassured.http.ContentType;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflyCapture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@HoverflyCapture(
		config = @io.specto.hoverfly.junit5.api.HoverflyConfig(
				proxyPort = 61111,
				adminPort = 61112,
				proxyLocalHost = true,
				captureHeaders = "Content-Type",
				destination = "/service/time" // Use destination filter to capture just your SUT API
		),
		path = "target/HoverflyDemoApplicationExtensionIntegrationTests.capture.json")
@ExtendWith(HoverflyExtension.class)
public class HoverflyDemoApplicationExtensionIntegrationTests {

	@LocalServerPort
	private int port;

	private static Hoverfly hoverflySimulate;

	@BeforeAll
	public static void startHoverfly() {
		HoverflyConfig hoverflySimulateConfig = localConfigs()
				.proxyPort(61113)
				.adminPort(61114);
		hoverflySimulate = new Hoverfly(hoverflySimulateConfig, HoverflyMode.SIMULATE);
		// This must be called after hoverflyCapture.start(), so that the RestTemplate in your SUT can use the simulation instance as proxy
		hoverflySimulate.start();
	}

	@AfterAll
	public static void closeHoverfly() {
		hoverflySimulate.close();
	}

	@Test
	public void timeTestCaptureThenSimulateFile() {
		hoverflySimulate.simulate(SimulationSource.classpath("time.json"));

		given()
			.proxy("localhost", 61111) // Note: make sure the request is being captured
			.port(port)
			.contentType(ContentType.JSON)
		.when()
			.get("/service/time")
		.peek()
		.then()
			.statusCode(200)
			.body("date", is("11-07-2019"));
	}

	@Test
	public void timeTestCaptureThenSimulateDsl(Hoverfly hoverflySimulate) {
		hoverflySimulate.simulate(SimulationSource.dsl(
			service("time.jsontest.com").get(any()).willReturn(success("{\"date\": \"11-07-2019\",\"milliseconds_since_epoch\": 1573115261249,\"time\": \"08:27:41 AM\"}", "application/json"))
		));

		given()
			.port(port)
			.contentType(ContentType.JSON)
		.when()
			.get("/service/time")
		.peek()
		.then()
			.statusCode(200)
			.body("date", is("11-07-2019"));
	}
}
