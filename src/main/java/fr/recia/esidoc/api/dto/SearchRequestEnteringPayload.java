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
package fr.recia.esidoc.api.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@NoArgsConstructor
@Slf4j
public class SearchRequestEnteringPayload {

    private String requete;
    private String tri_critere;
    @Getter(AccessLevel.NONE)
    private String tri_ordre;
    private int numero_premier_resultat;

    @Override
    public String toString() {
        return "SearchPayload{" +
                "requete='" + requete + '\'' +
                ", tri_critere='" + tri_critere + '\'' +
                ", tri_ordre='" + tri_ordre + '\'' +
                ", numero_premier_resultat=" + numero_premier_resultat +
                '}';
    }

    public String getTri_ordre(){
        if(Objects.nonNull(tri_critere)){
            return Objects.nonNull(tri_ordre) ? tri_ordre : "desc";
        }
        return null; // le parametre tri_ordre est INTERDIT si on n'as pas défini de critère de tri
    }


}
