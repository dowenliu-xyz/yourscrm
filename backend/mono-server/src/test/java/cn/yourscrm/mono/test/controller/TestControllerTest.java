package cn.yourscrm.mono.test.controller;

import cn.yourscrm.mono.TestcontainersConfiguration;
import cn.yourscrm.mono.auth.domain.jwt.SignKey;
import cn.yourscrm.mono.auth.domain.jwt.SignKeyService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class TestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SignKeyService signKeyService;

    @Test
    void opsHello() throws Exception {
        SignKey signKey = signKeyService.getOneForSigning();
        assertNotNull(signKey);
        String token = signOpsToken(signKey);
        mockMvc.perform(get("/ops/hello")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, ops!"));
    }

    private String signOpsToken(SignKey signKey) throws JOSEException {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(signKey.id() + "").build();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("ops")
                .expirationTime(Date.from(Instant.now().plus(Duration.ofMinutes(10))))
                .claim("usg", "ops")
                .build();
        JWSObject jwsObject = new JWSObject(header, claimsSet.toPayload());
        MACSigner signer = new MACSigner(signKey.secret());
        jwsObject.sign(signer);
        String token = jwsObject.serialize();
        MACVerifier verifier = new MACVerifier(signKey.secret());
        boolean verify = jwsObject.verify(verifier);
        assertTrue(verify);
        return token;
    }
}
