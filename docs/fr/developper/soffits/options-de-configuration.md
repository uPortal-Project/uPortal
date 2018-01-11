# Options de configuration

## Cache

La mise en cache d'une Soffit est possible _via_ le header HTTP standard
`Cache-Control`.  Vous devez définir `Cache-Control` comme une entête de réponse HTTP
pour profiter de cette fonctionnalité.

### Exemple

``` http
Cache-Control: public, max-age=300
```
La portée du cache peut être `public` (partagée par tous les utilisateurs) ou `private` (mise en cache
par l'utilisateur). Spécifiez `max-age` en secondes.

La re-validation du cache n'est pas encore supportée, donc...

``` http
Cache-Control: no-store
```

et...

``` http
Cache-Control: no-cache
```

ont actuellement le même effet.
