# Authentification `REMOTE_USER`

uPortal peut authentifier les utilisateurs en fonction de la valeur de `HttpServletRequest.getRemoteUser()`, qui sera non nulle si l'utilisateur se connecte _via_ une forme d'authentification gérée par conteneur.
uPortal peut prendre en charge une grande variété de fournisseurs d'authentification de cette manière, à la fois commerciale et open-source. Quelques exemples sont [Shibboleth][]/SAML (en utilisant `mod_shib` dans Apache HTTPD) et [CoSign][] (en utilisant `mod_cosign` dans Apache HTTPD).

## Activer `RemoteUserSecurityContextFactory`

Pour permettre l'authentification `REMOTE_USER` dans uPortal, ajouter cette propriété à `uPortal.properties` :

```properties
org.apereo.portal.security.provider.RemoteUserSecurityContextFactory.enabled=true
```
Ce paramètre demandera à uPortal d'honorer `HttpServletRequest.getRemoteUser()` ; vous devrez peut-être également implémenter quelques lignes d'intégration dans le conteneur (en fonction du fournisseur d'authentification) pour que cette valeur soit présente dans la requête.

## Note sur `RemoteUserPersonManager`

:notebook: À partir de uPortal 5.0.5, la configuration explicite de `RemoteUserPersonManager` dans Spring n'est plus requise. uPortal appliquera automatiquement le comportement de `RemoteUserPersonManager` quand` RemoteUserSecurityContextFactory` est activé.

[Shibboleth]:https://www.shibboleth.net/
[CoSign]:http://weblogin.org/
