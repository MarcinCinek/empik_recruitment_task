package com.empik.recruitment.couponservice.serviceImpl;

import com.empik.recruitment.couponservice.geoip.GeoIpService;
import org.springframework.stereotype.Service;

@Service
public class GeoIpServiceImpl implements GeoIpService {

    @Override
    public String resolveCountry(String ipAddress) {
        return "PL";
    }
}
