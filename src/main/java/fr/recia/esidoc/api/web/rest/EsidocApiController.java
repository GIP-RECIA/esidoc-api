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
package fr.recia.esidoc.api.web.rest;

import fr.recia.esidoc.api.dto.UtilisateursResponsePayload;
import fr.recia.esidoc.api.services.utilisateurs.prets.PretsService;
import fr.recia.esidoc.api.services.recherche.SearchService;
import fr.recia.esidoc.api.services.auth.token.ServiceToken;
import fr.recia.esidoc.api.config.functional.interfaces.UserInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/")
public class EsidocApiController {


    @Autowired
    ServiceToken serviceToken;


    @Autowired
    PretsService pretsService;

    @Autowired
    SearchService searchService;

    @Autowired
    UserInfoProvider userInfoProvider;

    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck(){
        return ResponseEntity.ok("health-check ok");
    }

    @GetMapping("/empruntsUtilisateur")
    public ResponseEntity<UtilisateursResponsePayload> empruntsUtilisateur(){
        return ResponseEntity.ok(pretsService.getInfo());
    }
}