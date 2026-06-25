package com.siblo.rent.config;

import com.midtrans.Midtrans;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidtransConfig {

    @Value("${midtrans.server-key}")
    private String serverKey;

    @Value("${midtrans.client-key}")
    private String clientKey;

    @Value("${midtrans.is-production:false}")
    private boolean isProduction;

    @PostConstruct
    public void init() {
        Midtrans.serverKey = serverKey;
        Midtrans.clientKey = clientKey;
        Midtrans.isProduction = isProduction;
    }

    public String getServerKey() {
        return serverKey;
    }

    public String getClientKey() {
        return clientKey;
    }

    public boolean isProduction() {
        return isProduction;
    }
}