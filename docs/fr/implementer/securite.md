# Sécurité dans uPortal

## Table des matières

1. [Filtre CORS](#cors-Filter)

## Filtre CORS

Le filtre CORS est une implémentation de CORS du W3C
(Cross-Origin Resource Sharing) spécification, qui
permet des requêtes d'origines croisées.

### Options de configuration du filtre CORS

<dl>
    <dt><code>cors.allowed.origins</code></dt>
    <dd>
      <p>Une liste des <a href="http://tools.ietf.org/html/rfc6454"> origines </a>
      qui sont autorisés à accéder à la ressource. Une astérisque <code> * </ code> peut être
      spécifié pour permettre l'accès à la ressource depuis n'importe quelle origine. Sinon,
      Une liste blanche des origines séparées par des virgules <code> , </ code> peut être fournie.
      Par exemple: <code> http://www.w3.org, https://www.apache.org </ code>.
      <strong> Valeurs par défaut: </ strong> <code> * </ code> (Toute origine est autorisée à
      accéder à la ressource). </p>
    </dd>
    <dt><code>cors.allowed.methods</code></dt>
    <dd>
      <p>Une liste de méthodes HTTP séparées par des virgules <code> , </ code> qui peuvent être utilisées pour accéder aux
      ressources, en utilisant des requêtes d'origines croisées. Ce sont les méthodes qui vont
      aussi être inclus dans le cadre de l'<code> Access-Control-Allow-Methods </ code>
      en-tête dans la réponse pré-envoi. Par exemple: <code> GET, POST </ code>.
      <strong> Par défaut: </ strong> <code> GET, HEAD </ code></p>
    </dd>
    <dt><code>cors.allowed.headers</code></dt>
    <dd>
      <p>Une liste d'entêtes de requête séparées par des virgules <code> , </ code> qui peuvent être utilisées quand
      on fait effectivement une requête. Ces en-têtes seront également retournés dans le cadre
      de l'en-tête <code> Access-Control-Allow-Headers </ code> dans le pré-envoi de la
      réponse. Par exemple: <code>Origin,Accept</code>. <strong> Valeurs par défaut: </ strong>
      <code>Origin, Accept, X-Requested-With, Content-Type,
      Access-Control-Request-Method, Access-Control-Request-Headers</code></p>
    </dd>
    <dt><code>cors.exposed.headers</code></dt>
    <dd>
      <p>Une liste d'en-têtes séparés par des virgules, autres que les simples en-têtes de réponse 
      auxquels les navigateurs sont autorisés à accéder. Ce sont les en-têtes qui seront également 
      inclus dans l'en-tête <code> Access-Control-Expose-Headers </ code> dans le pré-envoi de la réponse. Eg:
      <code>X-CUSTOM-HEADER-PING,X-CUSTOM-HEADER-PONG</code>.
      <strong>Default:</strong> None. Les en-têtes non simples ne sont pas affichés par défaut.</p>
    </dd>
    <dt><code>cors.preflight.maxage</code></dt>
    <dd>
      <p>La quantité de secondes, qu'un navigateur est autorisé à mettre en cache du résultat de la 
      requête. Cela sera inclus dans l'en-tête 
      <code> Access-Control-Max-Age </ code> dans le pré-envoi de la réponse. 
      Une valeur négative empêchera le filtre CORS d'ajouter cette en-tête de réponse 
      dans le pré-envoi de la réponse. <strong> Par défaut :</ strong> 
      <code> 1800 </ code></p>
    </dd>
    <dt><code>cors.support.credentials</code></dt>
    <dd>
       <p> Un Indicateur indiquant si la ressource prend en charge les informations d'identification de l'utilisateur.
       Ce drapeau est exposé dans le cadre de
       En-tête <code>Access-Control-Allow-Credentials</ code> dans le pré-envoi de la 
       réponse. Il aide le navigateur à déterminer si oui ou non une requête réelle
       peut être fait en utilisant des informations d'identification. <strong>Defaults:</ strong>
       <code> true </ code> </ p>
    </dd>
    <dt><code>cors.request.decorate</code></dt>
    <dd>
      <p>Un indicateur pour contrôler si les attributs spécifiques CORS doivent être ajoutés à
         l'objet <code>HttpServletRequest</code> ou non. <strong>Defaults:</strong>
      <code>true</code></p>
    </dd>
</dl>

See [CORS du W3C](http://www.w3.org/TR/cors/)
