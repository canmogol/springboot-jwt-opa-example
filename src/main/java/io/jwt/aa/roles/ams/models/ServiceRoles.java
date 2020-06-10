package io.jwt.aa.roles.ams.models;

import lombok.Data;

import java.util.List;

/**
 * Roles defined for a Service.
 */
@Data
public class ServiceRoles {

    private String serviceName;
    private String serviceDescription;
    private List<String> roleList;
    private List<ControllerRoles> controllerRoles;

}
