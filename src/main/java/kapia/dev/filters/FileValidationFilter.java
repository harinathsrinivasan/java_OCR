package kapia.dev.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FileValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        System.out.println("FileValidationFilter called");

        // Check if the request is a POST request
        if (!request.getRequestURI().equals("/getOCR")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if request contains multipart/form-data
        if (request.getContentType() == null || !request.getContentType().startsWith("multipart/form-data")) {
            System.out.println("Request is not multipart/form-data - detected in FileValidationFilter");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Check if image is null or empty
        if (request.getPart("image") == null || request.getPart("image").getSize() == 0) {
            System.out.println("Image is null or empty");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Check if image is not an image
        if (!request.getPart("image").getSubmittedFileName().endsWith(".png") && !request.getPart("image").getSubmittedFileName().endsWith(".jpeg") && !request.getPart("image").getSubmittedFileName().endsWith(".jpg")) {
            System.out.println("Image is not an image with proper extension");
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        // Check if image content type is an image
        if (!request.getPart("image").getContentType().equals("image/png") && !request.getPart("image").getContentType().equals("image/jpeg")) {
            System.out.println("Image is not an image with proper content type");
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        filterChain.doFilter(request, response);

    }

}
