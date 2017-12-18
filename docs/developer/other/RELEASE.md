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

Run the following command:

```sh
./gradlew clean release --no-parallel
```

:warning: During the `release` task, you will be prompted for a release version (e.g. `5.0.3`) and a
new version number for the branch (e.g. `5.0.4-SNAPSHOT`)
