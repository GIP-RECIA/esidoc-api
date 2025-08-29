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
package fr.recia.esidoc.api.deserializers;

import fr.recia.esidoc.api.dto.esidoc.utilisateur.ItemContent;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SafeMapDeserializer extends JsonDeserializer<Map<String, ItemContent>> {
    @Override
    public Map<String, ItemContent> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isArray()) {
            // [] reçu, on retourne null (ou Collections.emptyMap() selon ton besoin)
            return new HashMap<>();
        } else if (node.isObject()) {
            // Traitement standard
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            return mapper.convertValue(node, new TypeReference<Map<String, ItemContent>>() {});
        } else {
            throw new JsonMappingException(p, "Invalid type for Map field");
        }
    }
}
