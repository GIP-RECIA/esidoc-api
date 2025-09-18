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
package fr.recia.esidoc.api.dto.esidoc.utilisateur;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ItemContent {
    String exemplaire;
    String date_emprunt;
    String date_retour;
    String permalien;
    String titre;
    String cote;
    String support;
    String notice_id;

    @JsonAnySetter
    public void add(String key, Object value) {
        log.info("add for {}, and {}", key, value);
    }

    @Override
    public String toString() {
        return "ItemContent{" +
                "exemplaire='" + exemplaire + '\'' +
                ", date_emprunt='" + date_emprunt + '\'' +
                ", date_retour='" + date_retour + '\'' +
                ", permalien='" + permalien + '\'' +
                ", titre='" + titre + '\'' +
                ", cote='" + cote + '\'' +
                ", support='" + support + '\'' +
                ", notice_id='" + notice_id + '\'' +
                '}';
    }
}
