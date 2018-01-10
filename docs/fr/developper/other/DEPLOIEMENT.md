# Déploiement de uPortal

## Installer

Ajoutez dans `$HOME/.gradle/gradle.properties` vos informations d'identification pour Sonatype OSS 
Repository Hosting et vos informations de configuration pour la signature des artefacts avec 
GNU Privacy Guard (GnuPG).
([détails](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)).

```properties
ossrhUsername={username}
ossrhPassword={secret}

signing.keyId=24875D73
signing.password={secret}
signing.secretKeyRingFile=/Users/me/.gnupg/secring.gpg
```

## Lancer une release

Lancez la commande suivante :

```sh
./gradlew clean release --no-parallel
```

:attention: Durant la tâche de `release`, il vous sera demandé une version de release 
(e.g. `5.0.3`) et une nouvelle version de branch (e.g. `5.0.4-SNAPSHOT`)