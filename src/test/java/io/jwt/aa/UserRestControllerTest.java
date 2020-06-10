package io.jwt.aa;

import io.jwt.aa.rest.UserRestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

/**
 * User REST endpoint tests.
 */
@WithMockUser(username = "myra.manson", authorities = {"USER_QUERY_AUTH_ROLES", "USER_QUERY"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JwtAuthenticationAuthorizationExampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRestControllerTest {

    private static final String[] ROLES = new String[]{"USER_QUERY_AUTH_ROLES", "USER_QUERY"};
    private static final String USER_NAME = "myra.manson";

    @Autowired
    private UserRestController userRestController;

    @Test
    public void whenGetUsername_thenReturnMyraManson() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ResponseEntity<String> authorizedUserName = userRestController.getAuthorizedUserName(authentication);
        String userNameActual = authorizedUserName.getBody();
        assertEquals("UserName used for Authentication token and UserName got from REST Controller are different.", USER_NAME, userNameActual);
    }

    @Test
    public void whenGetAuthorizedUserRoles_thenReturnUserAuthenticatedRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ResponseEntity<Set<String>> authorizedUserRolesResponse = userRestController.getAuthorizedUserRoles(authentication);
        Set<String> setOfRoles = authorizedUserRolesResponse.getBody();
        assertThat("Given roles and roles in the response are different", setOfRoles, containsInAnyOrder(ROLES));
    }

}
