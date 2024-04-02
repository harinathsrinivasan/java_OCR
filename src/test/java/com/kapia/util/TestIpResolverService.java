package com.kapia.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {IpResolverService.class})
@ExtendWith(SpringExtension.class)
public class TestIpResolverService {

    @Test
    public void givenRequest_whenExtractIpFromRequest_thenReturnIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String ips = "13.173.46.180, 13.173.46.180, 78.18.253.140, 99.156.46.64, 159.239.149.100, 5.3.245.242";
        String expectedIp = "13.173.46.180";
        request.addHeader("X-Forwarded-For", ips);

        IpResolverService ipResolverService = new IpResolverService();
        String extractedIp = ipResolverService.extractIpFromRequest(request);

        assert extractedIp.equals(expectedIp);
    }

    @Test
    public void givenRequestWithNoXForwardedFor_whenExtractIpFromRequest_thenReturnRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String expectedIp = request.getRemoteAddr();
        IpResolverService ipResolverService = new IpResolverService();

        String extractedIp = ipResolverService.extractIpFromRequest(request);

        assert extractedIp.equals(request.getRemoteAddr());
    }

    @Test
    public void givenRequest_whenExtractIpFromRequestIfValid_thenReturnIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String ips = request.getRemoteAddr();
        IpResolverService ipResolverService = new IpResolverService();

        String extractedIp = ipResolverService.extractIpFromRequestIfValid(request);

        assert extractedIp.equals(ips);
    }

    @Test
    public void givenInvalidIp_whenExtractIpFromRequestIfValid_thenReturnNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String ip = request.getRemoteAddr() + "n";
        request.addHeader("X-Forwarded-For", ip);
        IpResolverService ipResolverService = new IpResolverService();

        String extractedIp = ipResolverService.extractIpFromRequestIfValid(request);

        assert extractedIp == null;
    }

    @Test
    public void givenValidIp_whenIsIpAddressValid_thenReturnTrue() {
        String ip = "13.173.46.180";
        IpResolverService ipResolverService = new IpResolverService();

        boolean isValid = ipResolverService.isIpAddressValid(ip);

        assert isValid;
    }

    @Test
    public void givenInvalidIp_whenIsIpAddressValid_thenReturnFalse() {
        String ip = "13.173.46.1801";
        IpResolverService ipResolverService = new IpResolverService();

        boolean isValid = ipResolverService.isIpAddressValid(ip);

        assert !isValid;
    }

}
