package io.jwt.aa.roles.ams.models;

import lombok.Data;

import java.util.List;

/**
 * Roles defined for a Controller.
 */
@Data
public class ControllerRoles {

    private String controllerName;
    private String controllerDescription;
    private List<String> roles;
    private List<MethodRoles> methodRoles;
}
