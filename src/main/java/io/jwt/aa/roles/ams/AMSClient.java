package io.jwt.aa.roles.ams;

import feign.Headers;
import feign.RequestLine;
import io.jwt.aa.roles.ams.models.ServiceRoles;

/**
 * Authorities management service endpoint.
 */
public interface AMSClient {

    /**
     * Registers the service roles to AMS.
     * @param serviceRoles roles that belongs to this service.
     */
    @RequestLine("POST")
    @Headers("Content-Type: application/json")
    void register(ServiceRoles serviceRoles);

}
