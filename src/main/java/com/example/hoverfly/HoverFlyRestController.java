package com.example.hoverfly;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Date;
import java.util.UUID;

@RestController
public class HoverFlyRestController {

    @RequestMapping(value = "/service/time")
    public TimeResponse getTime() throws URISyntaxException {
        RestTemplate template = ConnectionController.getRestTemplate();
        TimeResponse response = template.getForObject(new URI("http://time.jsontest.com"), TimeResponse.class);
        return response;
    }
}
