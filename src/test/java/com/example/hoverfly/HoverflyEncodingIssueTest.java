package com.example.hoverfly;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.HttpHeaders;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HoverflyEncodingIssueTest {

    public String getGithubUser() throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom()
                .useSystemProperties() // Note: support Hoverfly
                .build();
        HttpGet httpGet = new HttpGet("https://api.github.com/users/larsthorup");
        httpGet.setHeader(HttpHeaders.ACCEPT_ENCODING, "identity"); // Note: avoid gzip
        CloseableHttpResponse response = httpClient.execute(httpGet);
        InputStream inputStream = response.getEntity().getContent();
        String responseBody = org.apache.commons.io.IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
        return responseBody;
    }

    // @Test
    public void test_capture() throws Exception {
        Hoverfly hoverfly = new Hoverfly(localConfigs(), HoverflyMode.CAPTURE);
        hoverfly.start();
        String user = getGithubUser();
        System.out.println(user);
        boolean expected = true; // Note: verify that the "ø" is parsed correctly when not simulated
        assertEquals(expected, user.contains("\"location\":\"København, Danmark\""));
        hoverfly.exportSimulation(Paths.get("src/test/resources/hoverfly/capture.json"));
        hoverfly.close();
    }

    // @Test
    public void test_simulateFile() throws Exception {
        Hoverfly hoverfly = new Hoverfly(localConfigs(), HoverflyMode.SIMULATE);
        hoverfly.start();
        hoverfly.simulate(SimulationSource.file(Paths.get("src/test/resources/hoverfly/capture.json"    )));
        String user = getGithubUser();
        System.out.println(user);
        boolean expected = false; // ToDo: why is the "ø" not parsed correctly when simulated?
        assertEquals(expected, user.contains("\"location\":\"København, Danmark\""));
        hoverfly.close();
    }
}
