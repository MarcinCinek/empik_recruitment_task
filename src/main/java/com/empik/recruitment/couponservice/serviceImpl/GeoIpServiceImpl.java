package com.empik.recruitment.couponservice.serviceImpl;

import com.empik.recruitment.couponservice.geoip.GeoIpService;
import com.empik.recruitment.couponservice.geoip.client.IpApiClient;
import com.empik.recruitment.couponservice.geoip.dto.IpApiResponse;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeoIpServiceImpl implements GeoIpService {

  private final IpApiClient ipApiClient;

  @Value("${geoip.localhost-enabled:true}")
  private boolean localhostEnabled;

  public GeoIpServiceImpl(IpApiClient ipApiClient) {
    this.ipApiClient = ipApiClient;
  }

  private static final Set<String> LOCALHOST_IPS = Set.of("127.0.0.1", "::1", "0:0:0:0:0:0:0:1");

  @Override
  public String resolveCountry(String ipAddress) {

    if (localhostEnabled && LOCALHOST_IPS.contains(ipAddress)) {
      return "PL";
    }

    try {
      IpApiResponse response = ipApiClient.getIpDetails(ipAddress, "status,countryCode");

      if (response == null || !"success".equalsIgnoreCase(response.status())) {
        throw new RuntimeException("Failed to resolve IP: " + ipAddress);
      }

      return response.countryCode();

    } catch (Exception ex) {
      throw new RuntimeException("IP API call failed for ip=" + ipAddress, ex);
    }
  }
}
