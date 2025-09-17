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
package fr.recia.esidoc.api.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import fr.recia.esidoc.api.config.bean.MappingProperties;
import fr.recia.esidoc.api.interceptor.bean.SoffitHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Profile("!test")
public class SoffitInterceptor implements HandlerInterceptor {

  private final SoffitHolder soffitHolder;

  @Autowired
  MappingProperties mappingProperties;

  public SoffitInterceptor(SoffitHolder soffitHolder) {
    this.soffitHolder = soffitHolder;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    log.warn("begin pre handle");
    String path = request.getRequestURI().substring(request.getContextPath().length());
    log.warn("begin pre handle B");

    if (path.startsWith("/recherche")) {
      log.info("IT IS TRUE");
      return true;
    }

    String token = request.getHeader("Authorization");
    if (token == null) {
      return true;
//      log.debug("No Authorization header found");
//      response.setStatus(HttpStatus.BAD_REQUEST.value()); //bad request car si on est arrivé là c'est soit qu'on a pas besoin de bearer (route non protégée, soit qu'on en a un si la route est protege et donc on est deja protege
//      return false;
    }

    Base64.Decoder decoder = Base64.getUrlDecoder();
    String payload = new String(decoder.decode(token.replace("Bearer ", "").split("\\.")[1]));

    ObjectMapper objectMapper = new ObjectMapper();

    TypeFactory factory;
    MapType type;

//    factory = TypeFactory.defaultInstance();
//    type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);

    Map<String,Object> soffit = new HashMap<>();
    try {
      log.warn("in try");
      soffit = objectMapper.readValue(payload,TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));

      boolean isGuest = Pattern.matches("^guest.*", (CharSequence) soffit.get(("sub")));
      if(isGuest){
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
      }
      soffitHolder.setSub(soffit.get("sub").toString());
      // UAI CURRENT
      Object rawObject = soffit.get(mappingProperties.getRneInSoffit());
      try {
        List<String> values = new ObjectMapper().convertValue(rawObject, new TypeReference<>() {
        });
        log.debug("{} {}", mappingProperties.getRneInSoffit(), values);
        if(!values.isEmpty()){
          soffitHolder.setRne(values.get(0));
        }else {
          log.error("Soffit has empty string collection for {}", mappingProperties.getRneInSoffit());
          response.setStatus(HttpStatus.UNAUTHORIZED.value());
          return false;
        }
      }catch (IllegalArgumentException illegalArgumentException){
        log.error("Soffit does not contains string collection for {}", mappingProperties.getRneInSoffit(), illegalArgumentException);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
      }
    } catch (IOException ignored) {
      log.error("Unable to read soffit" + soffit);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      return false;
    }
    catch (NullPointerException e) {
      log.error("A user info attribute is missing in the token.");
      response.setStatus(HttpStatus.BAD_REQUEST.value());
      return false;
    }
    log.info(soffitHolder.toString());
    return true;
  }
}
