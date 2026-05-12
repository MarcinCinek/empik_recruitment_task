package com.empik.recruitment.couponservice.serviceImpl;

import com.empik.recruitment.couponservice.geoip.GeoIpService;
import com.empik.recruitment.couponservice.geoip.client.IpApiClient;
import com.empik.recruitment.couponservice.geoip.dto.IpApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class GeoIpServiceImpl implements GeoIpService {

    private final IpApiClient ipApiClient;

    /*private static final Set<String> LOCALHOST_IPS = Set.of(
            "127.0.0.1",
            "::1",
            "0:0:0:0:0:0:0:1"
    );*/

    @Override
    public String resolveCountry(String ipAddress) {

        /*if (LOCALHOST_IPS.contains(ipAddress)) {
            return "PL";
        }*/

        IpApiResponse response = ipApiClient.getIpDetails(
                ipAddress,
                "status,countryCode"
        );

        if (!"success".equalsIgnoreCase(response.status())) {
            throw new RuntimeException("Failed to resolve IP: " + ipAddress);
        }

        return response.countryCode();
    }
}
