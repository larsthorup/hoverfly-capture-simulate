package com.example.hoverfly;

import io.restassured.http.ContentType;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HoverflyDemoApplicationIntegrationTests {

	@LocalServerPort
	private int port;

	@Test
	public void timeTest() {
		given()
			.port(port)
			.contentType(ContentType.JSON)
		.when()
			.get("/service/time")
		.peek()
		.then()
			.statusCode(200);
	}

	@Test
	public void timeTestSimulate() {
		HoverflyConfig hoverflySimulateConfig = localConfigs()
			.proxyPort(61113)
			.adminPort(61114);
		try(Hoverfly hoverflySimulate = new Hoverfly(hoverflySimulateConfig, HoverflyMode.SPY)) {
			hoverflySimulate.start();
			hoverflySimulate.simulate(SimulationSource.classpath("time.json"));

			given()
				.port(port)
				.contentType(ContentType.JSON)
			.when()
				.get("/service/time")
			.peek()
			.then()
				.statusCode(200)
				.body("date", is("11-07-2019")); // Note: verify that request was simulated with time.json
		}
	}

	@Test
	public void timeTestCapture() throws Exception {
		HoverflyConfig hoverflyCaptureConfig = localConfigs()
			.proxyPort(61111)
			.adminPort(61112)
			.captureHeaders("Content-Type")
			.proxyLocalHost() // Note: to capture requests to service under test
				;
		try (Hoverfly hoverflyCapture = new Hoverfly(hoverflyCaptureConfig, HoverflyMode.CAPTURE)) {
			hoverflyCapture.start();

			given()
				.port(port)
				.contentType(ContentType.JSON)
			.when()
				.get("/service/time")
			.peek()
			.then()
				.statusCode(200);

			Path path = Paths.get("target/timeTestCapture.capture.json");
			hoverflyCapture.exportSimulation(path);
			boolean expected = true; // Note: verify that request to service under test was captured
			assertEquals(expected, new String(Files.readAllBytes(path)).contains("\"value\" : \"/service/time\""));
		}
	}

	@Test
	public void timeTestCaptureThenSimulateExplicitProxy() throws Exception {
		HoverflyConfig hoverflyCaptureConfig = localConfigs()
				.proxyPort(61111)
				.adminPort(61112)
				.captureHeaders("Content-Type")
				.proxyLocalHost() // Note: to capture requests to service under test
				;
		try (Hoverfly hoverflyCapture = new Hoverfly(hoverflyCaptureConfig, HoverflyMode.CAPTURE)) {
			hoverflyCapture.start();

            // Note: starting hoverflySimulate last causes the service under test to have its requests simulated
			HoverflyConfig hoverflySimulateConfig = localConfigs()
					.proxyPort(61113)
					.adminPort(61114);
			try(Hoverfly hoverflySimulate = new Hoverfly(hoverflySimulateConfig, HoverflyMode.SPY)) {
				hoverflySimulate.start();
				hoverflySimulate.simulate(SimulationSource.classpath("time.json"));

				// What you're doing here is correct
				given()
					.proxy("localhost", 61111) // Note: make sure the request is being captured
					.port(port)
					.contentType(ContentType.JSON)
				.when()
					.get("/service/time")
				.peek()
				.then()
					.statusCode(200)
					.body("date", is("11-07-2019")); // Note: verify that request was simulated with time.json
			}

			Path path = Paths.get("target/timeTestCaptureThenSimulateExplicitProxy.capture.json");
			hoverflyCapture.exportSimulation(path);
			boolean expected = true;  // Note: verify that request to service under test was captured
			assertEquals(expected, new String(Files.readAllBytes(path)).contains("\"value\" : \"/service/time\""));
		}
	}
}
