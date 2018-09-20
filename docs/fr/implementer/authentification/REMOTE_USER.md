# Authentification `REMOTE_USER`

uPortal peut authentifier les utilisateurs en fonction de la valeur de `HttpServletRequest.getRemoteUser()`, qui sera non nulle si l'utilisateur se connecte _via_ une forme d'authentification gérée par conteneur.
uPortal peut prendre en charge une grande variété de fournisseurs d'authentification de cette manière, à la fois commerciale et open-source. Quelques exemples sont [Shibboleth][]/SAML (en utilisant `mod_shib` dans Apache HTTPD) et [CoSign][] (en utilisant `mod_cosign` dans Apache HTTPD).

## Note sur `RemoteUserPersonManager`

:notebook: À partir de uPortal 5.0.5, la configuration explicite de `RemoteUserPersonManager` dans Spring n'est plus requise. uPortal appliquera automatiquement le comportement de `RemoteUserPersonManager` quand` RemoteUserSecurityContextFactory` est activé.

[Shibboleth]:https://www.shibboleth.net/
[CoSign]:http://weblogin.org/
