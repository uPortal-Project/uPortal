# Releasing uPortal

## Setup

in `$HOME/.gradle/gradle.properties` your credentials for Sonatype OSS Repository Hosting and your
configuration information for signing artifacts with GNU Privacy Guard (GnuPG).
([details](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)).

```properties
ossrhUsername={username}
ossrhPassword={secret}

signing.keyId=24875D73
signing.password={secret}
signing.secretKeyRingFile=/Users/me/.gnupg/secring.gpg
```

## Running a release

Run

```sh
./gradlew clean uploadArtifacts
```
