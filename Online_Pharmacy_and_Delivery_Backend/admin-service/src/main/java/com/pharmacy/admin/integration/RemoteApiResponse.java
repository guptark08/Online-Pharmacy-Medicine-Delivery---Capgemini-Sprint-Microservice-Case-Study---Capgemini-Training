package com.pharmacy.admin.integration;

import lombok.Data;

@Data
public class RemoteApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
}
