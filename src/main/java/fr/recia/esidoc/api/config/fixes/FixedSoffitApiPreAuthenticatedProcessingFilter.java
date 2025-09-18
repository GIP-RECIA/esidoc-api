/**
 * Copyright © 2025 GIP-RECIA (https://www.recia.fr/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.recia.esidoc.api.config.fixes;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.soffit.Headers;
import org.apereo.portal.soffit.security.SoffitApiPreAuthenticatedProcessingFilter;
import org.apereo.portal.soffit.security.SoffitApiUserDetails;
import org.apereo.portal.soffit.service.AbstractJwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@Slf4j
public class FixedSoffitApiPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
    private static final String USER_DETAILS_REQUEST_ATTRIBUTE =
            SoffitApiPreAuthenticatedProcessingFilter.class.getName()
                    + ".userDetailsRequestAttribute";

    private final String signatureKey;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public FixedSoffitApiPreAuthenticatedProcessingFilter(String signatureKey) {
        // Key for signing JWT.  Must match the provider (typically the portal).
        this.signatureKey = signatureKey;
        if (AbstractJwtService.DEFAULT_SIGNATURE_KEY.equals(signatureKey)) {
            logger.warn(
                    "A custom value for '{}' has not been specified;  the default value will be "
                            + "used.  This configuration is not production-safe!",
                    AbstractJwtService.SIGNATURE_KEY_PROPERTY);
        }
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authHeader)
                || !authHeader.startsWith(Headers.BEARER_TOKEN_PREFIX)) {
            /*
             * In authenticating the user, this filter has no opinion if either (1) the
             * Authorization header is not set or (2) the value isn't a Bearer token.
             */
            return null;
        }

        final String bearerToken = authHeader.substring(Headers.BEARER_TOKEN_PREFIX.length());

        try {
            // Validate & parse the JWT
            final Jws<Claims> claims =
                    Jwts.parser().setSigningKey(signatureKey).parseClaimsJws(bearerToken);

            logger.debug("Found the following pre-authenticated user:  {}", claims.toString());

            final List<String> groupsClaim = claims.getBody().get("groups", List.class);
            final List<String> groupsList =
                    groupsClaim != null ? groupsClaim : Collections.emptyList();
            final UserDetails result =
                    new SoffitApiUserDetails(claims.getBody().getSubject(), groupsList);
            request.setAttribute(USER_DETAILS_REQUEST_ATTRIBUTE, result);
            return result;
        } catch (Exception e) {
            logger.error("The following Bearer token is unusable: '{}'", bearerToken);
            logger.error("Failed to validate and/or parse the specified Bearer token", e);
        }
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getAttribute(USER_DETAILS_REQUEST_ATTRIBUTE) != null ? "N/A" : null;
    }
}
