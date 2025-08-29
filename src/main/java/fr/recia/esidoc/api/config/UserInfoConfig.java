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
package fr.recia.esidoc.api.config;

import fr.recia.esidoc.api.config.bean.EsidocDevProperties;
import fr.recia.esidoc.api.interceptor.bean.SoffitHolder;
import fr.recia.esidoc.api.pojo.UserInfo;
import fr.recia.esidoc.api.services.identite.ent.IdentiteEntService;
import fr.recia.esidoc.api.config.functional.interfaces.UserInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class UserInfoConfig {

    @Autowired
    EsidocDevProperties esidocDevProperties;

    @Autowired
    SoffitHolder soffitHolder;

    @Autowired
    IdentiteEntService identiteEntService;

    @Bean(name = "userInfoProvider")
    @Profile("mockUserInfo")
    UserInfoProvider mockUserInfo(){
        log.info("load mock user info bean");
        return ()->{ return new UserInfo(esidocDevProperties.getRne(),esidocDevProperties.getIdentiteEnt()); };
    }

    @Bean(name = "userInfoProvider")
    @ConditionalOnMissingBean
    UserInfoProvider userInfo(){
        log.info("load normal user info bean");
        return ()->{ return new UserInfo(soffitHolder.getRne(), identiteEntService.getIdentiteEnt(soffitHolder.getSub())); };
    }


}
