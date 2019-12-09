package com.example.hoverfly;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ConnectionController {

    public static RestTemplate getProxyRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8500));
        requestFactory.setProxy(proxy);
        return new RestTemplate(requestFactory);
    }

    public static RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
