package com.careercompass.backend.role.service;

import com.careercompass.backend.role.dto.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(Long id);

    RoleResponse getRoleByTitle(String title);
}