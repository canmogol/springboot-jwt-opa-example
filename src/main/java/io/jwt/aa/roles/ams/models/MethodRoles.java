package io.jwt.aa.roles.ams.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Roles defined for a Method.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodRoles {

    private String methodName;
    private String methodDescription;
    private List<String> roles;

}
