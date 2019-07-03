# Releasing uPortal

## Prerequisites

There are 3 prerequisites to cutting releases:

1. [JIRA Account at at Sonatype](https://issues.sonatype.org/secure/Signup!default.jspa)
2. Permissions to release projects
    - This is granted via a Jira ticket from a uPortal committer
3. [Set up public PGP key on a server](https://central.sonatype.org/pages/working-with-pgp-signatures.html)
    - Generate a key pair `gpg --gen-key`
    - Determine the key ID and keyring file `gpg2 --list-secret-keys`
    - Distribute your public key `gpg2 --keyserver hkp://pool.sks-keyservers.net --send-keys {key ID}`

## Setup

in `$HOME/.gradle/gradle.properties` your credentials for Sonatype OSS
Repository Hosting and your configuration information for signing artifacts with
GNU Privacy Guard (GnuPG), from above prerequisites.
([details](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)).

```properties
ossrhUsername={username}
ossrhPassword={secret}

signing.keyId={key ID}
signing.password={secret}
signing.secretKeyRingFile={keyring file}
```
Setup is only required to be done once.

## Which repo?

We encourage performing releases directly from a clone of the official repository rather than a fork to avoid extra steps.

This means building `uPortal-start` with the only difference being the use of this `uPortal` release.

## Testing

Build a clean version of quickstart and perform at least light testing, especially around features that have been fixed or enhanced.

## Running a release

Run the following command:

```sh
./gradlew clean release --no-parallel
```

:warning: During the `release` task, you will be prompted for a release version
(e.g. `5.0.3`) and a new version number for the branch (e.g. `5.0.4-SNAPSHOT`)

## Close and Release from Nexus Staging Repository

Close the release in Sonatype to ensure the pushed Maven artifacts pass checks.
1. Log into https://oss.sonatype.org 
2. Search among _Staging Repositories_ for your username
3. Select the uPortal artifact you staged and hit "Close"
  - the lifecycle is Open --> Closed --> Released
4. Wait a few minutes for the uPortal artifact to close
5. Release the artifact

## Create Release Notes

1. Use Git to inspect the incremental commits since the last release (e.g. $ git log v5.0.5 ^v5.0.4 --no-merges)
2. Review the issue tracker and confirm that referenced issues have been Resolved
3. Enter the release notes on the GitHub releases page in the `uPortal` repo
  - Each commit type goes into a sub section with the type as a header
4. Upload binaries from `./gradlew tomcatZip` and `./gradlew tomcatTar` in `uPortal-start
  - Postpone this step until this version is added to `uPortal-start`

## Update uPortal-start

Open a Pull Request on uPortal-start to update uPortalVersion to the new release.

## Publish new docker demo of Quickstart

Publish a new apereo/uPortal-demo Docker image and update the :latest tag.
Prerequisites:
  - Docker Cloud account (https://cloud.docker.com)
  - Access to post to apereo group
  - This version has been added to `uPortal-start`

```sh
$ cd {uPortal-start repo}
$ ./gradlew dockerBuildImageDemo
$ docker login
$ docker tag {version in format like 5.1.0} apereo/uportal-demo:latest
$ docker push apereo/uportal-demo:latest
```

## Update Community
For a Milestone or RC release, email to uportal-dev is needed.  Be sure to acknowledge those who contributed to the release.

For a GA release also email uportal-user .  Be sure to put the release into context for an existing adopter to understand.

For a GA release also email jasig-announce , announcing the release to a general public audience.

Finally, have someone with access to the uPortal Twitter account announce the GA release.

## References

(https://apereo.atlassian.net/wiki/spaces/UPC/pages/102336655/Cutting+a+uPortal+Release)
(https://central.sonatype.org/pages/ossrh-guide.html)
(https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)
