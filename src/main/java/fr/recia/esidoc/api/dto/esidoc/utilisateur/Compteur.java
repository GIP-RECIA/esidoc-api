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

import lombok.Data;

@Data

public class Compteur {
    int en_cours;
    int en_retard;
    int historique;

    @Override
    public String toString() {
        return "Compteur{" +
                "en_cours=" + en_cours +
                ", en_retard=" + en_retard +
                ", historique=" + historique +
                '}';
    }
}
