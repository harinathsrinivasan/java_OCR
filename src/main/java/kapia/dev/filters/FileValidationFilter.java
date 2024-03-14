package kapia.dev.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FileValidationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidationFilter.class);

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
            LOGGER.debug("Request does not have multipart/form-data content type");
            throw new ValidationException(HttpServletResponse.SC_BAD_REQUEST, "Request is not multipart/form-data");
        }

        Part image = request.getPart("image");
        if (image == null || image.getSize() == 0) {
            LOGGER.debug("Image part was null or empty");
            throw new ValidationException(HttpServletResponse.SC_BAD_REQUEST, "Image is null or empty");
        }

        String fileName = image.getSubmittedFileName();
        if (!fileName.endsWith(".png") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".jpg")) {
            LOGGER.debug("Image did not have proper extension");
            throw new ValidationException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Image is not an image with proper extension");
        }

        String contentTypeOfImage = image.getContentType();
        if (!contentTypeOfImage.equals("image/png") && !contentTypeOfImage.equals("image/jpeg")) {
            LOGGER.debug("Image was not of proper content type");
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
