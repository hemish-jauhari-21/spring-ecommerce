package com.demo.ecommerce.exception;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ErrorResponseUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ErrorResponseUtil() {
    }

    public static Map<String, Object> build(String path, HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    public static void write(HttpServletResponse response,
                             HttpServletRequest request,
                             HttpStatus status,
                             String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                OBJECT_MAPPER.writeValueAsString(
                        build(request.getRequestURI(), status, message)));
    }
}