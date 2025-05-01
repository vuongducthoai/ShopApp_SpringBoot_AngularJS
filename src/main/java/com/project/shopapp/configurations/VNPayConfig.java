package com.project.shopapp.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class VNPayConfig {
    // Cấu hình từ application.properties
    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    @Value("${vnpay.pay-url}")
    private String vnpPayUrl;

    @Value("${vnpay.api-url}")
    private String vnpApiUrl;

    @Value("${vnpay.secret-key}")
    private String secretKey;

}
