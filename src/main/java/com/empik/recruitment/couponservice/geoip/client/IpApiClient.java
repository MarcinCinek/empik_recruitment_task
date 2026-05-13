package com.empik.recruitment.couponservice.geoip.client;

import com.empik.recruitment.couponservice.geoip.dto.IpApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ip-api-client", url = "http://ip-api.com")
public interface IpApiClient {

  @GetMapping("/json/{ip}")
  IpApiResponse getIpDetails(@PathVariable("ip") String ip, @RequestParam("fields") String fields);
}
