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
package fr.recia.esidoc.api.services.identite.ent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fr.recia.esidoc.api.config.bean.IdentiteEntSiProperties;
import fr.recia.esidoc.api.config.bean.OAuth2Properties;
import fr.recia.esidoc.api.dto.IdentiteEntResponsePayload;
import fr.recia.esidoc.api.dto.TokenRequestPayload;
import fr.recia.esidoc.api.services.identite.ent.exceptions.IdentiteEntNonObtenueException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.cache.CacheManager;

@Service
@Slf4j
public class IdentiteEntService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OAuth2Properties oAuth2Properties;

    @Autowired
    private IdentiteEntSiProperties identiteEntSiProperties;

    @Autowired
    private CacheManager cacheManager;

    public String getIdentiteEnt(String id) {

        log.trace("Call to getIdentiteEnt for {}", id);

        // If value is cached, no need to request the API
        if(cacheManager.getCache("identiteEntCache").containsKey(id)){
            String value = cacheManager.getCache("identiteEntCache").get(id).toString();
            log.debug("Returned cached value {} for {}", value, id);
            return value;
        }

        String url = identiteEntSiProperties.getIdentiteEntSiUri().replace("{sub}",id);

        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            requestHeaders.set("X-API-KEY", identiteEntSiProperties.getIdentiteEntSiXApiKey());
            HttpEntity<String> requestEntity = new HttpEntity<String>(requestHeaders);

            log.debug("Requesting {} to retrieve an externalid", url);
            ResponseEntity<IdentiteEntResponsePayload> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, IdentiteEntResponsePayload.class);

            if(response.getStatusCode().isError()){
                throw new IdentiteEntNonObtenueException(String.format("Response status code %s", response.getStatusCode()));
            }

            if(!response.hasBody()){
                throw new IdentiteEntNonObtenueException("Response does not have a body");
            }

            try {
                IdentiteEntResponsePayload responsePayload = response.getBody();
                // Before returning we put the value in cache
                cacheManager.getCache("identiteEntCache").put(id, responsePayload.getId());
                log.debug("Put value {} in cache for {}", responsePayload.getId(), id);
                return responsePayload.getId();
            } catch (NullPointerException e) {
                log.error("Error when reading response body", e);
                throw new IdentiteEntNonObtenueException("Error when reading response body");
            }
            
        } catch (RestClientException | HttpMessageNotReadableException e) {
            throw new RuntimeException(e);
        }
    }
}
