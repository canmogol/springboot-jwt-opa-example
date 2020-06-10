package io.jwt.aa.config;

import io.jwt.aa.voter.OPAVoter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * OPA Access decision manager.
 */
@Component
public class OPAAccessDecisionManager extends UnanimousBased {

    /**
     * Constructor.
     * @param opaVoter OPA Voter
     */
    public OPAAccessDecisionManager(@Autowired final OPAVoter opaVoter) {
        super(Collections.singletonList(opaVoter));
    }

}
