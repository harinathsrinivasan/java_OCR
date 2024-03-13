package kapia.dev.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import kapia.dev.util.IpResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(urlPatterns = "/getOCR")
public class RequestDetailsLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDetailsLoggingFilter.class);

    private IpResolverService ipResolverService;

    @Autowired
    public RequestDetailsLoggingFilter(IpResolverService ipResolverService) {
        this.ipResolverService = ipResolverService;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;


        String ipAddress = ipResolverService.extractIpFromRequestIfValid(httpRequest);

        LOGGER.info(String.format("Request details: contentType=%s, contentLength=%d, clientIpAddress=%s",
                httpRequest.getContentType(),
                httpRequest.getContentLength(),
                ipAddress));

        chain.doFilter(request, response);
    }

}
