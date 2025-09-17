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
package fr.recia.esidoc.api.config.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Slf4j
@ConfigurationProperties(prefix = "mapping")
public class MappingProperties {


    String rneInSoffit ="";
    String utilisateursCacheName ="";

    @Override
    public String toString() {
        return "MappingProperties{" +
                "rneInSoffit='" + rneInSoffit + '\'' +
                ", utilisateursCacheName='" + utilisateursCacheName + '\'' +
                '}';
    }

    @PostConstruct
    private void init() throws JsonProcessingException {
        log.info("Mapping Properties: {}", this);
    }

}
