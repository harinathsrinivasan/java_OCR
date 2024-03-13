package kapia.dev.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FileValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().equals("/getOCR")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            validateRequest(request);
        } catch (ValidationException e) {
            response.sendError(e.getStatusCode(), e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);

    }

    private void validateRequest(HttpServletRequest request) throws ValidationException, ServletException, IOException {

        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            throw new ValidationException(HttpServletResponse.SC_BAD_REQUEST, "Request is not multipart/form-data");
        }

        Part image = request.getPart("image");
        if (image == null || image.getSize() == 0) {
            throw new ValidationException(HttpServletResponse.SC_BAD_REQUEST, "Image is null or empty");
        }

        String fileName = image.getSubmittedFileName();
        if (!fileName.endsWith(".png") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".jpg")) {
            throw new ValidationException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Image is not an image with proper extension");
        }

        String contentTypeOfImage = image.getContentType();
        if (!contentTypeOfImage.equals("image/png") && !contentTypeOfImage.equals("image/jpeg")) {
            throw new ValidationException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Image is not an image with proper content type");
        }

    }

    private static class ValidationException extends RuntimeException {

        private final int statusCode;

        public ValidationException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

}
