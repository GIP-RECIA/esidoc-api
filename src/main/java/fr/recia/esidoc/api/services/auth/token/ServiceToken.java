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
package fr.recia.esidoc.api.services.auth.token;

import fr.recia.esidoc.api.config.bean.OAuth2Properties;
import fr.recia.esidoc.api.dto.TokenRequestPayload;
import fr.recia.esidoc.api.dto.TokenResponsePayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Objects;

@Service
@Slf4j
@Scope("singleton")
public class ServiceToken {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OAuth2Properties oAuth2Properties;

    TokenHolder tokenHolder = null;

    public String getToken(){
        if(isTokenInvalid()){
            synchronized (ServiceToken.class) {
                if(isTokenInvalid()){
                    //invoke token creation
                    this.tokenHolder =  fetchToken();
                }
            }
        }
        log.debug("Retieved token from cache, token is {}, with expiry in {}, and now {}", tokenHolder.token, tokenHolder.expiry, Instant.now() );
        return tokenHolder.token;
    }

    public TokenHolder fetchToken() {
        TokenRequestPayload tokenRequestPayload = new TokenRequestPayload(oAuth2Properties.getAuthorizationGrantType(), oAuth2Properties.getClientId(), oAuth2Properties.getClientSecret());
        String json;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            json  = ow.writeValueAsString(tokenRequestPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String url = oAuth2Properties.getOauth2TokenUri();

        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> requestEntity = new HttpEntity<>(json, requestHeaders);
            log.info("Requesting {} to retrieve a new token", url);
            ResponseEntity<TokenResponsePayload> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity,TokenResponsePayload.class);
            TokenResponsePayload responsePayload = response.getBody();
            return new TokenHolder(
                    responsePayload.getAccess_token(),
                    Instant.now().plusSeconds((long) (responsePayload.getExpires_in()*0.95f))
            );
        } catch (RestClientException | HttpMessageNotReadableException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isTokenInvalid(){
        return Objects.isNull(tokenHolder) || tokenHolder.expiry.isBefore(Instant.now());
    }

    @AllArgsConstructor
    public static class TokenHolder {
        String token;
        Instant expiry;
    }
}
