package com.example.secretpanda.data.model;

import java.util.Map;

public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> details;

    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public Map<String, String> getDetails() { return details; }

    public String getFullErrorMessage() {
        if (details != null && !details.isEmpty()) {
            StringBuilder sb = new StringBuilder(message).append(":\n");
            for (Map.Entry<String, String> entry : details.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            return sb.toString();
        }
        return message;
    }
}
