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
package fr.recia.esidoc.api.services.utilisateurs.prets;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.recia.esidoc.api.config.bean.EsidocProperties;
import fr.recia.esidoc.api.config.bean.MappingProperties;
import fr.recia.esidoc.api.dto.ItemForResponse;
import fr.recia.esidoc.api.dto.UtilisateursResponsePayload;
import fr.recia.esidoc.api.dto.esidoc.utilisateur.ItemContent;
import fr.recia.esidoc.api.dto.esidoc.utilisateur.ApiUtilisateurEsidocResponsePayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.recia.esidoc.api.services.auth.token.ServiceToken;
import fr.recia.esidoc.api.config.functional.interfaces.UserInfoProvider;
import fr.recia.esidoc.api.services.identite.ent.exceptions.EsidocRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class PretsService {

    private static final String EDITEUR_SLUG = "{editeur}";
    private static final String RNE_SLUG = "{rne}";
    private static final String IDENTITE_ENT_SLUG = "{identite_ent}";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserInfoProvider userInfoProvider;

    @Autowired
    private EsidocProperties esidocProperties;

    @Autowired
    private ServiceToken serviceToken;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MappingProperties mappingProperties;

    public UtilisateursResponsePayload getInfo(){
        String identiteEnt = userInfoProvider.getUserInfo().getIdentiteEnt();
        String rne = userInfoProvider.getUserInfo().getRne();

        log.info("Requesting prets for {} on etab {}", identiteEnt, rne);

        // First, try to get value from cache for this user
        Optional<UtilisateursResponsePayload> cacheResult = readFromCache(identiteEnt, rne);
        if(cacheResult.isPresent()) {
            return cacheResult.get();
        }

        // If value is not in cache then we need to make a request
        String url = esidocProperties.getPretsUri().replace(EDITEUR_SLUG, esidocProperties.getEditeur()).replace(RNE_SLUG, rne).replace(IDENTITE_ENT_SLUG, identiteEnt);
        try {
            log.debug("Requesting {}", url);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            requestHeaders.setBearerAuth(serviceToken.getToken());
            HttpEntity<String> requestEntity = new HttpEntity<String>(requestHeaders);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            if(response.hasBody()){
                log.trace("Received response from API : {}", response.getBody());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

            ApiUtilisateurEsidocResponsePayload userDataEsidocResponsePayload = objectMapper.readValue(response.getBody(), ApiUtilisateurEsidocResponsePayload.class);
            List<ItemForResponse> itemForResponseList =  new ArrayList<>();

            //ADD NULL CHECK BEFORE
            log.debug("Retrieving items en_retard");
            for(ItemContent itemContent: userDataEsidocResponsePayload.getPrets().getItems().getEn_retard().values()){
                ItemForResponse itemForResponse = new ItemForResponse(itemContent.getPermalien(), itemContent.getTitre(), true);
                log.debug("Adding item {} to list", itemForResponse);
                itemForResponseList.add(itemForResponse);
            }

            log.debug("Retrieving items en_cours");
            for(ItemContent itemContent: userDataEsidocResponsePayload.getPrets().getItems().getEn_cours().values()){
                ItemForResponse itemForResponse = new ItemForResponse(itemContent.getPermalien(), itemContent.getTitre(), false);
                log.debug("Adding item {} to list", itemForResponse);
                itemForResponseList.add(itemForResponse);
            }

            // Put the value in cache for this user
            writeToCache(itemForResponseList, identiteEnt, objectMapper);

            return new UtilisateursResponsePayload(itemForResponseList, instantParisTimeZoneNow());

        } catch (RestClientException | HttpMessageNotReadableException | JsonProcessingException e) {
            log.error("An exception occured when trying to get prets for user {}", identiteEnt, e);
            throw new EsidocRequestException(e.getMessage());
        }
    }

    private Instant instantParisTimeZoneNow(){
        ZonedDateTime dateTimeParis = ZonedDateTime.now();
        return dateTimeParis.toInstant();
    }

    private void writeToCache(List<ItemForResponse> itemForResponseList, String identiteEnt, ObjectMapper objectMapper) throws JsonProcessingException {
        UtilisateursResponsePayload utilisateursResponsePayload = new UtilisateursResponsePayload(itemForResponseList, instantParisTimeZoneNow());
        String responsePayload = objectMapper.writeValueAsString(utilisateursResponsePayload);
        Cache cache = this.cacheManager.getCache(mappingProperties.getUtilisateursCacheName());
        if(Objects.isNull(cache)){
            log.warn("No cache found for {}", mappingProperties.getUtilisateursCacheName());
            log.warn("Existing caches are {}", cacheManager.getCacheNames());
        } else {
            log.debug("Found cache with name {}", mappingProperties.getUtilisateursCacheName());
            log.debug("Put {} in cache for key {}", responsePayload, identiteEnt);
            cache.put(identiteEnt, responsePayload);
        }
    }

    private Optional<UtilisateursResponsePayload> readFromCache(String identiteEnt, String rne){
        Cache cache = this.cacheManager.getCache(mappingProperties.getUtilisateursCacheName());

        if (Objects.isNull(cache)) {
            log.warn("No cache found for {}", mappingProperties.getUtilisateursCacheName());
            log.warn("Existing caches are {}", cacheManager.getCacheNames());
            return Optional.empty();
        }

        log.debug("Found cache with name {}", mappingProperties.getUtilisateursCacheName());
        Cache.ValueWrapper valueWrapper = cache.get(identiteEnt);

        if(Objects.isNull(valueWrapper)){
            log.debug("Value wrapper for key {} not found", identiteEnt);
            return Optional.empty();
        }

        log.debug("Found value wrapper for key {}", identiteEnt);

        if(Objects.isNull(valueWrapper.get())){
            log.warn("Value wrapper for key {} is null", identiteEnt);
            return Optional.empty();
        }

        if(!(valueWrapper.get() instanceof String)){
            log.error("Store value in cache is not of type String");
            return Optional.empty();
        }

        String s = (String)valueWrapper.get();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        UtilisateursResponsePayload utilisateursResponsePayload;
        try {
            utilisateursResponsePayload = objectMapper.readValue(s,UtilisateursResponsePayload.class);
        } catch (JsonProcessingException e) {
            log.error("Exception when parsing string stored in cache", e);
            return Optional.empty();
        }

        log.debug("Returning cached value {}", utilisateursResponsePayload);
        return Optional.of(utilisateursResponsePayload);
    }



}
