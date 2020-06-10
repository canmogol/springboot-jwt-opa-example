package io.jwt.aa;

import io.jwt.aa.rest.UserRestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

/**
 * User REST endpoint tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JwtAuthenticationAuthorizationExampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRestControllerTest {

    public static final String[] ROLES = new String[]{"USER_QUERY_AUTH_ROLES", "UPLOAD_USER"};
    private static final String USER_NAME = "myra.manson";
    public static final TestingAuthenticationToken AUTHENTICATION_TOKEN = new TestingAuthenticationToken(
        USER_NAME, null, ROLES);

    @Autowired
    private UserRestController userRestController;

    @Test
    public void whenGetUsername_thenReturnLisaJensen() {
        ResponseEntity<String> authorizedUserName = userRestController.getAuthorizedUserName(AUTHENTICATION_TOKEN);
        String userNameActual = authorizedUserName.getBody();
        assertEquals("UserName used for Authentication token and UserName got from REST Controller are different.", USER_NAME, userNameActual);
    }

    @Test
    public void whenGetAuthorizedUserRoles_thenReturnUserGetAuthRoles() {
        ResponseEntity<Set<String>> authorizedUserRolesResponse = userRestController.getAuthorizedUserRoles(AUTHENTICATION_TOKEN);
        Set<String> setOfRoles = authorizedUserRolesResponse.getBody();
        assertThat("Given roles and roles in the response are different", setOfRoles, containsInAnyOrder(ROLES));
    }

}
