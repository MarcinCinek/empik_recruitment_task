package com.empik.recruitment.couponservice.serviceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.empik.recruitment.couponservice.geoip.client.IpApiClient;
import com.empik.recruitment.couponservice.geoip.dto.IpApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GeoIpServiceImplTest {

  @Mock private IpApiClient ipApiClient;

  @InjectMocks private GeoIpServiceImpl geoIpService;

  @Test
  void shouldReturnPLForLocalhostWhenEnabled() {
    ReflectionTestUtils.setField(geoIpService, "localhostEnabled", true);

    String result = geoIpService.resolveCountry("127.0.0.1");

    assertEquals("PL", result);
    verifyNoInteractions(ipApiClient);
  }

  @Test
  void shouldCallApiAndReturnCountryCode() {
    ReflectionTestUtils.setField(geoIpService, "localhostEnabled", false);

    when(ipApiClient.getIpDetails("8.8.8.8", "status,countryCode"))
        .thenReturn(new IpApiResponse("success", "US"));

    String result = geoIpService.resolveCountry("8.8.8.8");

    assertEquals("US", result);
    verify(ipApiClient).getIpDetails("8.8.8.8", "status,countryCode");
  }

  @Test
  void shouldThrowExceptionWhenApiFails() {
    ReflectionTestUtils.setField(geoIpService, "localhostEnabled", false);

    when(ipApiClient.getIpDetails(any(), any())).thenReturn(new IpApiResponse("fail", null));

    assertThrows(RuntimeException.class, () -> geoIpService.resolveCountry("8.8.8.8"));
  }

  @Test
  void shouldIgnoreLocalhostWhenFeatureDisabled() {
    ReflectionTestUtils.setField(geoIpService, "localhostEnabled", false);

    when(ipApiClient.getIpDetails(any(), any())).thenReturn(new IpApiResponse("success", "PL"));

    String result = geoIpService.resolveCountry("127.0.0.1");

    assertEquals("PL", result);
    verify(ipApiClient).getIpDetails("127.0.0.1", "status,countryCode");
  }
}
