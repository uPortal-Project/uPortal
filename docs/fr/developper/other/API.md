# Documentation API

## APIs REST

uPortal inclut une suite complète d'APIs REST qu'il utilise pour accéder et manipuler les données du portail
à partir de l'interface utilisateur. La documentation et les outils basés sur Swagger sont disponibles pour ces API, mais désactivés (par défaut) par mesure de sécurité.

Pour activer la documentation de l'API Swagger, ajoutez la propriété suivante à `uPortal.properties`:

```properties
org.apereo.portal.rest.swagger.SwaggerConfiguration.enabled=true
```

Après avoir redémarré le conteneur Tomcat, vous pouvez accéder à l'interface utilisateur Swagger sur `/uPortal/api/swagger-ui.html`.

![Documentation API Swagger](../../../images/swagger.png)