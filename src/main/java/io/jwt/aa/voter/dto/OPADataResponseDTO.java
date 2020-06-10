package io.jwt.aa.voter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * OPA Response POJO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OPADataResponseDTO {

    private Map<String, Object> result;

}
