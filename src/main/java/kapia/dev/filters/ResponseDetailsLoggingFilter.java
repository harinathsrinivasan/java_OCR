package kapia.dev.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
@WebFilter(urlPatterns = "/getOCR")
public class ResponseDetailsLoggingFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ResponseDetailsLoggingFilter.class.getName());

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        chain.doFilter(request, response);

        String length = "0";
        if (httpResponse.getHeader("Content-Length") != null) {
            length = httpResponse.getHeader("Content-Length");
        }

        LOGGER.info(String.format("Response details: contentType=%s, contentLength=%s",
                httpResponse.getContentType(),
                length));

    }

}
