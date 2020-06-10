package io.jwt.aa.voter;

import io.jwt.aa.voter.dto.OPADataRequestDTO;
import io.jwt.aa.voter.dto.OPADataRequestInputDTO;
import io.jwt.aa.voter.dto.OPADataResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Open Policy Agent voter implementation.
 */
@Slf4j
@Component
public class OPAVoter implements AccessDecisionVoter<Object> {

    public static final String ALLOW = "allow";

    @Value("${opa.url}")
    private String opaUrl;

    @Value("${api.path}")
    private String apiPath;

    @Value("${api.version}")
    private String apiVersion;

    @Value("${service.description}")
    private String serviceDescription;

    @Autowired
    private RestTemplate client;


    /**
     * Indicates whether this voter is available for this config attribute.
     *
     * @param attribute configuration attribute.
     * @return true if the attribute is supported
     */
    @Override
    public final boolean supports(final ConfigAttribute attribute) {
        return true;
    }

    /**
     * Checks if that type of class is supported.
     *
     * @param clazz type of class
     * @return true if the class type is supported
     */
    @Override
    public final boolean supports(final Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    /**
     * Votes for access decision.
     *
     * @param authentication spring authentication abstraction for principal and authorities
     * @param obj            Spring web security filter invocation Object
     * @param attributes     Configuration attributes.
     * @return returns 1 for granted access, 0 for abstained, -1 for denied.
     */
    @Override
    public final int vote(
        final Authentication authentication,
        final Object obj,
        final Collection<ConfigAttribute> attributes) {

        // object should be a FilterInvocation
        if (!(obj instanceof FilterInvocation)) {
            return ACCESS_ABSTAIN;
        }
        FilterInvocation filter = (FilterInvocation) obj;

        // get request headers
        Map<String, String> headers = getHeaders(filter.getRequest());

        // find the request PATH and API
        String path = getRequestURI(filter);
        String api = getAPI(path);
        List<String> paths = getPaths(path);

        // build the request input map for OPA
        HttpEntity<?> requestData = buildRequestData(
            authentication,
            filter.getRequest().getMethod(),
            headers,
            api,
            paths);

        // send request to OPA
        return sendRequestToOPA(requestData);
    }

    /**
     * Returns paths as String list.
     * @param path String List
     * @return paths
     */
    private List<String> getPaths(final String path) {
        return Arrays.asList(path.split("/"));
    }

    /**
     * Returns the API path.
     * @param path API path
     * @return API path
     */
    private String getAPI(final String path) {
        return path.substring(String.format("%s/%s", apiPath, apiVersion).length());
    }

    /**
     * Returns the headers as a Map.
     * @param request http request
     * @return Map of headers
     */
    private Map<String, String> getHeaders(final HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(h -> h, request::getHeader));
    }

    /**
     * Builds the request data.
     *
     * @param authentication Authentication data
     * @param method         Request method
     * @param headers        Request headers
     * @param api            API URI
     * @param paths          paths as String array
     * @return Request Object
     */
    private HttpEntity<OPADataRequestDTO> buildRequestData(
        final Authentication authentication,
        final String method,
        final Map<String, String> headers,
        final String api,
        final List<String> paths) {
        OPADataRequestInputDTO input = OPADataRequestInputDTO.builder()
            .auth(authentication)
            .method(method)
            .api(api)
            .paths(paths.toArray(new String[0]))
            .headers(headers)
            .build();
        return new HttpEntity<>(new OPADataRequestDTO(input));
    }

    /**
     * Returns the request from FilterInvocation object.
     *
     * @param filter FilterInvocation object
     * @return URL as String
     */
    private String getRequestURI(final FilterInvocation filter) {
        return filter.getRequest().getRequestURI().replaceAll("^/|/$", "");
    }

    /**
     * Sends request to OPA backend.
     *
     * @param request Map of key value pairs necessary for OPA
     * @return returns 1 for granted, -1 for denied.
     */
    private int sendRequestToOPA(final HttpEntity<?> request) {
        try {
            OPADataResponseDTO dataResponseDTO = client.postForObject(this.opaUrl, request, OPADataResponseDTO.class);
            Boolean allowed = Optional.ofNullable(dataResponseDTO)
                .map(OPADataResponseDTO::getResult)
                .filter(m -> m.containsKey(ALLOW))
                .map(m -> m.get(ALLOW))
                .map(String::valueOf)
                .map(Boolean::valueOf)
                .orElse(false);
            if (Boolean.TRUE.equals(allowed)) {
                return ACCESS_GRANTED;
            } else {
                return ACCESS_DENIED;
            }
        } catch (Exception e) {
            log.error(String.format("got error at OPA request, error: %s", e.getMessage()), e);
            return ACCESS_DENIED;
        }
    }

}
