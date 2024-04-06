package com.kapia.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class FileValidationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileValidationFilter.class);

    private static final String ACCEPTED_CONTENT_TYPES = "image/png,image/jpeg";
    private static final String ACCEPTED_EXTENSIONS = ".png,.jpeg,.jpg";
    private static final String ACCEPTED_REQUEST_CONTENT_TYPE = "multipart/form-data";
    private static final String MULTIPART_FILE_NAME = "image";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().equals("/getOCR")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            validateRequest(request);
            filterChain.doFilter(request, response);
        } catch (ValidationException e) {
            LOGGER.debug(e.getMessage());
            response.setStatus(e.getStatusCode());
            response.getWriter().write(e.getMessage());
        }
    }

    private void validateRequest(HttpServletRequest request) throws ValidationException, IOException, ServletException {
        validateContentType(request);
        validateImagePart(request);
    }

    private void validateContentType(HttpServletRequest request) throws ValidationException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith(ACCEPTED_REQUEST_CONTENT_TYPE)) {
            throw new ValidationException(HttpServletResponse.SC_BAD_REQUEST, "Request is not of proper content type");
        }
    }

    private void validateImagePart(HttpServletRequest request) throws ValidationException, IOException, ServletException {
        Part image = request.getPart(MULTIPART_FILE_NAME);
        if (image == null || image.getSize() == 0) {
            throw new ValidationException(HttpServletResponse.SC_BAD_REQUEST, "Image is null or empty");
        }

        String fileName = image.getSubmittedFileName();
        if (fileName == null || !isValidImageExtension(fileName)) {
            throw new ValidationException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Image is not an image with proper extension");
        }

        String contentTypeOfImage = image.getContentType();
        if (contentTypeOfImage == null || !ACCEPTED_CONTENT_TYPES.contains(contentTypeOfImage)) {
            throw new ValidationException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Image is not an image with proper content type");
        }
    }

    private boolean isValidImageExtension(String fileName) {
        return Arrays.stream(ACCEPTED_EXTENSIONS.split(",")).anyMatch(fileName::endsWith);
    }

    private static class ValidationException extends Exception {

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