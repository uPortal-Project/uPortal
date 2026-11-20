/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.soffit.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apereo.portal.soffit.model.v1_0.Bearer;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

/**
 * Round-trip coverage for JWT creation and parsing through {@link BearerService}, which exercises
 * the {@link AbstractJwtService} signing and verification paths.
 *
 * <p>These tests exist because the jjwt 0.11 -&gt; 0.13 migration rewrote token creation and
 * parsing with no test touching either path, so a green build said nothing about whether tokens
 * still round-tripped. They also pin the claim shapes (single-value vs. collection attributes,
 * groups, expiry) so a future jjwt upgrade cannot quietly change them.
 *
 * <p>{@link JwtEncryptor} is mocked as a pass-through so these tests cover the JWT paths only;
 * encryption is covered separately by {@link JwtEncryptorTest}.
 */
public class BearerServiceTest {

    /** Same shape as DEFAULT_SIGNATURE_KEY: BASE64, comfortably over the 256-bit HMAC minimum. */
    private static final String TEST_SIGNATURE_KEY =
            "dGVzdC1zaWduYXR1cmUta2V5LXRoYXQtaXMtY29tZm9ydGFibHktbG9uZ2VyLXRoYW4tdGhpcnR5LXR3by1ieXRlcw==";

    /** BASE64 for "short" -- 5 bytes, far below the 256-bit minimum. */
    private static final String WEAK_SIGNATURE_KEY = "c2hvcnQ=";

    /**
     * Sets a private field, walking up the hierarchy. Plain reflection rather than Spring's
     * ReflectionTestUtils, which drags in commons-logging that this module's test runtime does not
     * have.
     */
    private static void setField(Object target, String name, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                final Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to set field " + name, e);
            }
        }
        throw new IllegalStateException("No such field: " + name);
    }

    private BearerService newBearerService(String signatureKey) {
        return newBearerService(
                signatureKey, JwtSignatureAlgorithmFactory.SIGNATURE_ALGORITHM_DEFAULT);
    }

    private BearerService newBearerService(String signatureKey, String signatureAlgorithm) {
        // Pass-through encryptor; constructing the real one requires jasypt + commons-logging,
        // which are not on this module's test runtime classpath.
        final JwtEncryptor encryptor = mock(JwtEncryptor.class);
        when(encryptor.encryptIfConfigured(ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(encryptor.decryptIfConfigured(ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final JwtSignatureAlgorithmFactory algorithmFactory = new JwtSignatureAlgorithmFactory();
        setField(algorithmFactory, "algorithmStr", signatureAlgorithm);
        algorithmFactory.init();

        final BearerService result = new BearerService();
        setField(result, "signatureKey", signatureKey);
        setField(result, "jwtEncryptor", encryptor);
        setField(result, "algorithmFactory", algorithmFactory);
        result.init();
        return result;
    }

    /** Decodes the "alg" header of a compact JWT. */
    private static String algHeaderOf(String jwt) {
        final String header = jwt.substring(0, jwt.indexOf('.'));
        return new String(java.util.Base64.getUrlDecoder().decode(header));
    }

    private Map<String, List<String>> sampleAttributes() {
        final Map<String, List<String>> result = new HashMap<>();
        result.put("displayName", Collections.singletonList("Test User"));
        result.put("eduPersonAffiliation", Arrays.asList("student", "member"));
        return result;
    }

    @Test
    public void testCreateAndParseBearerRoundTrip() {
        final BearerService service = newBearerService(TEST_SIGNATURE_KEY);
        final List<String> groups = Arrays.asList("Everyone", "Students");
        final Date expires = new Date(System.currentTimeMillis() + 60000L);

        final Bearer created = service.createBearer("student", sampleAttributes(), groups, expires);
        assertNotNull("Token was not generated", created.getEncryptedToken());
        assertTrue(
                "Expected a well-formed JWT",
                JwtEncryptor.JWT_PATTERN.matcher(created.getEncryptedToken()).matches());

        final Bearer parsed = service.parseBearerToken(created.getEncryptedToken());

        assertEquals("Username did not survive the round trip", "student", parsed.getUsername());
        assertEquals("Groups did not survive the round trip", groups, parsed.getGroups());
        assertEquals(
                "Single-value attribute did not survive the round trip",
                Collections.singletonList("Test User"),
                parsed.getAttributes().get("displayName"));
        assertEquals(
                "Multi-value attribute did not survive the round trip",
                Arrays.asList("student", "member"),
                parsed.getAttributes().get("eduPersonAffiliation"));
    }

    @Test
    public void testExpiredTokenIsRejected() {
        final BearerService service = newBearerService(TEST_SIGNATURE_KEY);
        final Date expired = new Date(System.currentTimeMillis() - 60000L);

        final Bearer created =
                service.createBearer(
                        "student",
                        sampleAttributes(),
                        Collections.singletonList("Students"),
                        expired);

        try {
            service.parseBearerToken(created.getEncryptedToken());
            fail("Expected an expired token to be rejected");
        } catch (RuntimeException e) {
            // jjwt may reject it first (ExpiredJwtException), or the explicit expiry check in
            // parseEncryptedToken may. Either is acceptable; silently accepting it is not.
            assertTrue(
                    "Expected an expiry-related failure but got: " + e,
                    e.getClass().getSimpleName().contains("Expired")
                            || e instanceof SecurityException);
        }
    }

    @Test
    public void testTokenSignedWithADifferentKeyIsRejected() {
        final BearerService issuer = newBearerService(TEST_SIGNATURE_KEY);
        final BearerService verifier = newBearerService(AbstractJwtService.DEFAULT_SIGNATURE_KEY);
        final Date expires = new Date(System.currentTimeMillis() + 60000L);

        final Bearer created =
                issuer.createBearer(
                        "student",
                        sampleAttributes(),
                        Collections.singletonList("Students"),
                        expires);

        try {
            verifier.parseBearerToken(created.getEncryptedToken());
            fail("Expected a token signed with a different key to be rejected");
        } catch (SignatureException e) {
            // Expected
        }
    }

    @Test
    public void testWeakSignatureKeyFailsAtInitNamingTheProperty() {
        try {
            newBearerService(WEAK_SIGNATURE_KEY);
            fail("Expected an undersized signature key to be rejected");
        } catch (IllegalStateException e) {
            assertTrue(
                    "Failure should name " + AbstractJwtService.SIGNATURE_KEY_PROPERTY,
                    String.valueOf(e.getMessage())
                            .contains(AbstractJwtService.SIGNATURE_KEY_PROPERTY));
            assertTrue(
                    "Failure should retain the underlying jjwt cause",
                    e.getCause() instanceof WeakKeyException);
        }
    }

    @Test
    public void testSignatureAlgorithmPropertyIsHonored() {
        // The documented property must still select the algorithm. jjwt 0.12+ enforces only a
        // MINIMUM key length per algorithm, so the default key (1072 bits) is valid for all three.
        final BearerService service = newBearerService(TEST_SIGNATURE_KEY, "HS256");
        final Date expires = new Date(System.currentTimeMillis() + 60000L);

        final Bearer created =
                service.createBearer(
                        "student",
                        sampleAttributes(),
                        Collections.singletonList("Students"),
                        expires);

        assertTrue(
                "Expected the configured HS256 algorithm in the JWT header, got: "
                        + algHeaderOf(created.getEncryptedToken()),
                algHeaderOf(created.getEncryptedToken()).contains("HS256"));

        // ...and it must still round-trip
        assertEquals(
                "student", service.parseBearerToken(created.getEncryptedToken()).getUsername());
    }

    @Test
    public void testUnsupportedSignatureAlgorithmFallsBackToDefault() {
        final BearerService service = newBearerService(TEST_SIGNATURE_KEY, "NOT-AN-ALGORITHM");
        final Date expires = new Date(System.currentTimeMillis() + 60000L);

        final Bearer created =
                service.createBearer(
                        "student",
                        sampleAttributes(),
                        Collections.singletonList("Students"),
                        expires);

        assertTrue(
                "Expected fallback to the documented default (HS512)",
                algHeaderOf(created.getEncryptedToken()).contains("HS512"));
    }
}
