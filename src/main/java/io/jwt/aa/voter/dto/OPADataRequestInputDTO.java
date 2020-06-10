package io.jwt.aa.voter.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * Values necessary for OPA.
 */
@Getter
@Builder
public class OPADataRequestInputDTO {

    private Authentication auth;
    private String method;
    private String api;
    private String[] paths;
    private Map<String, String> headers;

}
