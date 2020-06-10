package io.jwt.aa.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST example with un-authorized and authorized endpoints.
 */
@RestController
@RequestMapping("${api.endpoint.users}")
@PreAuthorize("hasAuthority('USER_QUERY')")
public class UserRestController {

    /**
     * REST method to get the authenticated user's username.
     *
     * @param authentication user abstraction injected by Spring.
     * @return REST Response with the authenticated user's "username" as String.
     */
    @GetMapping(path = "/username")
    public ResponseEntity<String> getAuthorizedUserName(final Authentication authentication) {
        // the "name" property in the "authentication" abstraction is the value of the "user_name" entry in the JWT,
        // if the default implementation used.
        return ResponseEntity.ok(authentication.getName());
    }

    /**
     * REST method to get the authenticated user's roles/authorities which is a set of Strings.
     *
     * @param authentication user abstraction injected by Spring.
     * @return REST Response with the authenticated user's roles/authorities as a Set of Strings.
     */
    @GetMapping(path = "/roles")
    @PreAuthorize("hasAnyAuthority('USER_QUERY_AUTH_ROLES')")
    public ResponseEntity<Set<String>> getAuthorizedUserRoles(final Authentication authentication) {
        // "roles" are the "authorities" entry defined in the JWT,
        // if the default implementation used.
        Set<String> roles = authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        return ResponseEntity.ok(roles);
    }
}
