# Esidoc-API

API permettant d'interroger esidoc pour récupérer la liste des prêts d'une personne.

Versions :
- Java `11`
- Spring-boot `2.7.18`
- Fait pour tourner sur tomcat `9`

## Liste des routes

- GET `/empruntsUtilisateur` : retourne un JSON de type `UtilisateursResponsePayload` contenant la liste des emprunts (et s'ils sont en retard) ;
- GET `/health-check` : retourne 200 OK.

## Securité

L'API est conçue pour être appelée depuis un front déployé dans le contexte du portail car elle est protégée par soffit (voir `SoffitInterceptor`).

La soffit doit contenir les attributs suivants :
- L'uid de l'utilisateur ;
- L'établissement courant de l'utilisateur.

Ces deux informations seront utilisées pour requêter l'API esidoc.

## Appels API

Pour récupérer la liste des emprunts, on a besoin de faire plusieurs appels API :
- Un appel à l'API esidoc, qui est protégée par OAuth2.0 (en mode client_credentials) ;
- Un appel à l'API si-ent-api, pour récupérer l'id externe de l'utilisateur (celui connu par esidoc, pour ne pas le donner directement dans la soffit).

## Caches

Afin de ne pas faire de requêtes inutiles plusieurs caches sont mis en place :
- Un cache au niveau des requêtes à l'API esidoc (user <-> prets)
- Un cache au niveau des requêtes à l'API si-ent-api (uid <-> external_id)

Comme le token OAuth2.0 est global il est stocké directement au niveau de l'application (voir `ServiceToken`).

## Déploiement

- Pour faire tourner en local : `mvn clean package spring-boot:run`
- Pour pousser sur le nexus : `mvn clean package deploy`

### Commandes pour notice et license

- `mvn notice:check`
- `mvn notice:generate`
- `mvn license:check`
- `mvn license:format`
- `mvn license:remove`
