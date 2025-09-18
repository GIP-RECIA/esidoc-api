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

import fr.recia.esidoc.api.deserializers.SafeMapDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

//@Getter
@Slf4j
//@Getter
@Data
public class Items {

    Items() {}

    @JsonDeserialize(using = SafeMapDeserializer.class)
    private Map<String, ItemContent> en_cours;
    @JsonDeserialize(using = SafeMapDeserializer.class)
    private Map<String, ItemContent> en_retard;
    @JsonDeserialize(using = SafeMapDeserializer.class)
    private Map<String, ItemContent> historique;

    @Override
    public String toString() {
        return "Items{" +
                ", en_retard=" + en_retard +
                '}';
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        log.debug("add for in class Items avec un S {}, and {}", key, value);
    }

}
