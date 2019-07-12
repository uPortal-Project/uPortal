# Releasing uPortal

## Prerequisites

There are 3 prerequisites to cutting releases:

1. [JIRA Account at Sonatype](https://issues.sonatype.org/secure/Signup!default.jspa)
2. Permissions to release projects
    - This is granted via a Jira ticket from a uPortal committer
3. [Set up public PGP key on a server](https://central.sonatype.org/pages/working-with-pgp-signatures.html)
    - Generate a key pair `gpg2 --gen-key`
    - If you choose to have an expiration date, edit the key via `gpg2 --edit-key {key ID}`
    - Determine the key ID and keyring file `gpg2 --list-keys` (the key ID is the `pub` ID)
    - Distribute your public key `gpg2 --keyserver hkp://pool.sks-keyservers.net --send-keys {key ID}`

## Setup

Export your secret keyring via `gpg2 --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg`

In `$HOME/.gradle/gradle.properties` place your credentials for the Sonatype OSS Repository Hosting and your configuration information for signing artifacts with GNU Privacy Guard (GnuPG).  Use the key ID from the PGP prerequisite and the keyring file. ([details](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)).

```properties
ossrhUsername={username}
ossrhPassword={secret}

signing.keyId={key ID}
signing.password={secret}
signing.secretKeyRingFile={keyring file}
```
Setup is only required to be done once.

## Which Repo?

We encourage performing releases directly from a clone of the official repository rather than a fork to avoid extra steps.

This means when testing on `uPortal-start` for the release, you should use the `apereo` repository but configure the `uPortalVersion` to be the SNAPSHOT version that you'll build in the following steps.

## Testing

Build a clean version of `uPortal-start` with the quickstart data set and perform at least light testing, especially around features that have been fixed or enhanced.

### Prepare For Testing

Build uPortal locally:
```bash
cd ./uPortal
./gradlew clean install
```

Point your uPortal-start to the local uPortal build:
Using the uPortal version, `.../apereo/uPortal/gradle.properties` > `version=XYZ` , add it into `.../apereo/uPortal-start/gradle.properties` > `uPortalVersion=XYZ`

Run uPortal-start with the local build
```sh
cd ./uPortal-start
./gradlew clean portalInit
```

### Verify

* Unit tests are automatically run on commits, so ensure the latest commit's CI build passed.
* FUTURE - Need to run the cross browser platform tests and the performance tests
* Smoke test the UI manually
  * Login as different users
  * Enable alerts
  * Pay attention to new changes

## Cut Release

Run the following command in the uPortal clone's directory:

```sh
~.../apereo/uPortal$ ./gradlew clean release --no-parallel
```

:Note: During the `release` task, you will be prompted for a release version (e.g. `5.0.3`).  Press `Enter` for the default.  The release process will then create two commits - a commit to set the new version (`5.0.3`), and a commit to set the version to the new snapshot (`5.0.4-SNAPSHOT`)

## Close and Release from Nexus Staging Repository

Close the release in Sonatype to ensure the pushed Maven artifacts pass checks:
1. Log into <https://oss.sonatype.org>
2. Search among `Build Promotion` > `Staging Repositories` for your username
3. Review the release for the expected artifacts
4. Select the uPortal artifact you staged and hit "Close"
  - The lifecycle is `Open --> Closed --> Released`
5. Wait a few minutes for the uPortal artifact to close
6. Release the artifact, and put the tag name in the description

## Create Release Notes

1. Use Git to inspect the incremental commits since the last release (e.g. `$ git log v5.0.2..v5.0.3 --no-merges`)
2. Review the issue tracker and confirm that the referenced issues have been Resolved
3. Enter the release notes on the GitHub releases page in the `uPortal` repo
  - It's helpful to use the previous release notes as a guide
  - Each commit type goes into a sub section with the type as a header (Fixes, Chores, Features, etc...).

## Update uPortal-start

Open a Pull Request on `uPortal-start` to update `uPortalVersion` to the new release.

## Publish new docker demo of Quickstart

Publish a new apereo/uPortal-demo Docker image and update the `:latest` tag.
Prerequisites:
  - Docker Cloud account (https://cloud.docker.com)
  - Access to post to the apereo Docker group
  - The uPortal version has been added to `uPortal-start`

```sh
$ cd {uPortal-start repo}
$ ./gradlew dockerBuildImageDemo
$ docker login
$ docker tag {version in format like 5.1.0} apereo/uportal-demo:latest
$ docker push apereo/uportal-demo:latest
```

## Update Community
For any non-snapshot release, email an announcement to `uportal-dev`, `uportal-user`, and `jasig-announce`.
  - Be sure to acknowledge those who contributed to the release.
  - Be sure to put the release into context for existing adopters to understand.

Have someone with access to the uPortal Twitter account announce the release.

## References

(https://apereo.atlassian.net/wiki/spaces/UPC/pages/102336655/Cutting+a+uPortal+Release)
(https://central.sonatype.org/pages/ossrh-guide.html)
(https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)
