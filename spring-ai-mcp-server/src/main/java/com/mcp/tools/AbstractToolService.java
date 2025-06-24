package com.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class AbstractToolService {

    protected static final String SUCCESS = "success";
    protected static final String ERROR = "error";
    protected final ObjectMapper mapper = new ObjectMapper();

    protected String errorMessage(String errorMessage) {
        return createJsonMessage(false, errorMessage);
    }

    protected String successMessage(Map<String, Object> result) {
        result.put(SUCCESS, true);
        return serializeResult(result);
    }

    private String createJsonMessage(boolean success, String message) {
        Map<String, Object> result = Map.of(SUCCESS, success, ERROR, message);
        return serializeResult(result);
    }

    private String serializeResult(Map<String, Object> result) {
        try {
            return mapper.writeValueAsString(result);
        } catch (Exception ex) {
            return "{\"success\": false, \"error\": \"Failed to serialize result\"}";
        }
    }
}
