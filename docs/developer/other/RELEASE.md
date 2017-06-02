# Releasing uPortal

## Setup

in `$HOME/.gradle/gradle.properties` add ([details](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials))

```properties
signing.keyId=24875D73
signing.password=secret
signing.secretKeyRingFile=/Users/me/.gnupg/secring.gpg
```

## Running a release

Run

```sh
./gradlew publish -P sonatypeUser=[user] -P sonatypePassword={pass}
```
