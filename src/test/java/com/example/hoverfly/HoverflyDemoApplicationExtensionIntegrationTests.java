package com.example.hoverfly;

import io.restassured.http.ContentType;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.any;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@HoverflyCore(mode = HoverflyMode.SPY, config = @io.specto.hoverfly.junit5.api.HoverflyConfig(proxyPort = 61113, adminPort = 61114))
@ExtendWith(HoverflyExtension.class)
public class HoverflyDemoApplicationExtensionIntegrationTests {

	@LocalServerPort
	private int port;

	private static Hoverfly hoverflyCapture;

	// @BeforeAll
	public static void startHoverfly() {
		HoverflyConfig hoverflyCaptureConfig = localConfigs()
				.proxyPort(61111)
				.adminPort(61112)
				.captureHeaders("Content-Type")
				.proxyLocalHost() // Note: to capture requests to service under test
				;
		hoverflyCapture = new Hoverfly(hoverflyCaptureConfig, HoverflyMode.CAPTURE);
		hoverflyCapture.start();
	}

	// @AfterAll
	public static void saveCapture() throws Exception {
		Path path = Paths.get("target/HoverflyDemoApplicationExtensionIntegrationTests.capture.json");
		hoverflyCapture.exportSimulation(path);
		boolean expected = true; // Note: verify that request to service under test was captured
		assertEquals(expected, new String(Files.readAllBytes(path)).contains("\"value\" : \"/service/time\""));
	}

	@Test
	public void timeTestCaptureThenSimulateFile(Hoverfly hoverflySimulate) {
		hoverflySimulate.simulate(SimulationSource.classpath("time.json"));

		given()
			.port(port)
			.contentType(ContentType.JSON)
		.when()
			.get("/service/time")
		.peek()
		.then()
			.statusCode(200)
			.body("date", is("11-07-2019")); // ToDo: why was request not simulated while capturing?
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
			.body("date", is("11-07-2019")); // ToDo: why was request not simulated while capturing?
	}
}