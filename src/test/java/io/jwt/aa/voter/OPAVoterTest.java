package io.jwt.aa.voter;

import com.google.common.collect.ImmutableMap;
import io.jwt.aa.voter.dto.OPADataResponseDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Test for OPA Voter.
 */
public class OPAVoterTest {

    public static final Authentication AUTHENTICATION = new TestingAuthenticationToken("user", "pass");
    public static final ArrayList<ConfigAttribute> CONFIG_ATTRIBUTES = new ArrayList<>();
    public static final String OPA_URL = "http://localhost:8181/v1/data/restapi/authz";
    public static final String GET_METHOD = "GET";
    public static final String API = "api";
    public static final String VERSION = "v0.0.1";
    public static final String API_V0_0_1_USERS_ROLES = "api/v0.0.1/users/roles";
    public static final MockHttpServletRequest HTTP_SERVLET_REQUEST = new MockHttpServletRequest(GET_METHOD, API_V0_0_1_USERS_ROLES);
    public static final FilterInvocation FILTER_INVOCATION = new FilterInvocation(
        HTTP_SERVLET_REQUEST,
        new MockHttpServletResponse(),
        new MockFilterChain());
    public static final OPADataResponseDTO RESPONSE_DTO_EMPTY = new OPADataResponseDTO();
    public static final String ALLOW = "allow";
    public static final Map<String, Object> RESULTS_MAP = ImmutableMap.<String, Object>builder().put(ALLOW, Boolean.TRUE).build();
    public static final OPADataResponseDTO RESPONSE_DTO_ALLOW_TRUE = new OPADataResponseDTO(RESULTS_MAP);

    private static final int ACCESS_GRANTED = 1;
    private static final int ACCESS_ABSTAIN = 0;
    private static final int ACCESS_DENIED = -1;

    @Mock
    private RestTemplate clientEmpty;

    @Mock
    private RestTemplate clientAllowed;

    @Mock
    private OPAVoter opaVoter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(opaVoter, "opaUrl", OPA_URL);
        Whitebox.setInternalState(opaVoter, "apiPath", API);
        Whitebox.setInternalState(opaVoter, "apiVersion", VERSION);
    }

    @Test
    public void testSupportsConfigAttributes() throws Exception {
        ConfigAttribute attribute = null;
        OPAVoter opa = new OPAVoter();
        boolean supports = opa.supports(attribute);
        assertEquals("OPAVoter should support all the ConfigAttributes.", true, supports);
    }

    @Test
    public void testSupportsFilterInvocationClass() throws Exception {
        OPAVoter opa = new OPAVoter();
        boolean supports = opa.supports(FilterInvocation.class);
        assertEquals("OPAVoter should support FilterInvocation class.", true, supports);
    }

    @Test
    public void testVoteShouldReturnAbstainForNonFilterInvocation() throws Exception {
        OPAVoter opaVoter = new OPAVoter();
        int voteForNonFilterInvocation = opaVoter.vote(AUTHENTICATION, new Object(), CONFIG_ATTRIBUTES);
        assertEquals("OPAVoter should return ACCESS_ABSTAIN for anything other then FilterInvocation.", ACCESS_ABSTAIN, voteForNonFilterInvocation);
    }

    @Test
    public void testVoteShouldDeny() throws Exception {

        Mockito.when(clientEmpty.postForObject(eq(OPA_URL), any(), eq(OPADataResponseDTO.class))).thenReturn(RESPONSE_DTO_EMPTY);
        Whitebox.setInternalState(opaVoter, "client", clientEmpty);

        int voteForNonFilterInvocation = opaVoter.vote(AUTHENTICATION, FILTER_INVOCATION, CONFIG_ATTRIBUTES);
        assertEquals("OPAVoter should return ACCESS_DENIED", ACCESS_DENIED, voteForNonFilterInvocation);
    }


    @Test
    public void testVoteShouldGrant() throws Exception {

        Mockito.when(clientAllowed.postForObject(eq(OPA_URL), any(), eq(OPADataResponseDTO.class))).thenReturn(RESPONSE_DTO_ALLOW_TRUE);
        Whitebox.setInternalState(opaVoter, "client", clientAllowed);

        int voteForNonFilterInvocation = opaVoter.vote(AUTHENTICATION, FILTER_INVOCATION, CONFIG_ATTRIBUTES);
        assertEquals("OPAVoter should return ACCESS_GRANTED", ACCESS_GRANTED, voteForNonFilterInvocation);
    }

}
