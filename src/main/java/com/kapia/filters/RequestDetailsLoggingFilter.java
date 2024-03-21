package com.kapia.filters;

import com.kapia.util.HashingService;
import com.kapia.util.IpResolverService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestDetailsLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDetailsLoggingFilter.class);

    private final IpResolverService ipResolverService;
    private final HashingService hashingService;

    @Autowired
    public RequestDetailsLoggingFilter(IpResolverService ipResolverService, HashingService hashingService) {
        this.ipResolverService = ipResolverService;
        this.hashingService = hashingService;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String ipAddress = hashingService.hash(ipResolverService.extractIpFromRequestIfValid(httpRequest));

        LOGGER.info("Request details: contentLength=" + httpRequest.getContentLength() + ", uri=" + httpRequest.getRequestURI() + ", method=" + httpRequest.getMethod() + ", ipAddress=" + ipAddress);

        chain.doFilter(request, response);
    }

}
