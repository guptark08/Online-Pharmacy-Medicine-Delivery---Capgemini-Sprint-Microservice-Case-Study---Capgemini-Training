package com.pharmacy.admin.integration;

import lombok.Data;

@Data
public class RemoteUserResponse {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String mobile;
    private String role;
}
