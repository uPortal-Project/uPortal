# Releasing uPortal

## Prerequisites

There are 3 prerequisites to cutting releases:

1. [Sonatype Account at Central Publisher Portal](https://central.sonatype.com/)
    - NOTE!! Do not link a social account -- create a local account!
    - See first part of [Register to Publish Via the Central Portal](https://central.sonatype.org/register/central-portal/)
    - **Note:** The legacy OSSRH service (oss.sonatype.org) was sunset June 2025. All publishing now goes through the Central Publisher Portal.
3. Permissions to release projects
    - Namespace permissions are managed via the [Central Publisher Portal](https://central.sonatype.com/)
    - If you need access to the `org.jasig.portal` namespace, contact a uPortal committer
    - Expect approval to take a few days to complete
4. [Set up public PGP key on a server](https://central.sonatype.org/pages/working-with-pgp-signatures.html)
    - Generate a key pair `gpg2 --gen-key`
    - If you choose to have an expiration date, edit the key via `gpg2 --edit-key {key ID}`
    - Determine the key ID and keyring file `gpg2 --list-keys` (the key ID is the `pub` ID)
    - Distribute your public key `gpg2 --keyserver hkp://keyserver.ubuntu.com --send-keys {key ID}`

## Setup

Export your secret keyring via `gpg2 --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg`

In `$HOME/.gradle/gradle.properties` place your credentials for the Central Publisher Portal and your configuration information for signing artifacts with GNU Privacy Guard (GnuPG).  Use the key ID from the PGP prerequisite and the keyring file. ([details](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)).

NOTE: you must use **Central Portal user tokens**, not your login password. Generate tokens at
<https://central.sonatype.com> under your profile/account settings.

```properties
ossrhUsername={portal token username}
ossrhPassword={portal token password}

signing.keyId={key ID}
signing.password={secret}
signing.secretKeyRingFile={keyring file}
```
Setup is only required to be done once.

See the [OSSRH Staging API guide](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/) and
the [Central Publisher Portal guide](https://central.sonatype.org/publish/publish-portal-guide/) for more assistance.

## Which Repo?

We encourage performing releases directly from a clone of the official repository rather than a fork to avoid extra steps.

This means when testing on `uPortal-start` for the release, you should use the `apereo` repository but configure the `uPortalVersion` to be the SNAPSHOT version that you'll build in the following steps.

## Send Pre-Release Notice to Community

For any non-snapshot release, email a notice to `uportal-dev` a couple of working days prior to cutting the release.
- Request any in-progress Pull Requests be merged by a certain date/time
- Request adopters test the tip of `master`

## Review Dependencies

Before releasing uPortal and updating uPortal-start with the latest uPortal version, review the dependencies and update dependencies where appropriate.

Assuming adopters use the community build of uPortal, there are three primary sets of dependencies to be considered:
- uPortal
- uPortal-start (community version)
- uPortal-start (customized)

Ideally, dependencies in all of the above areas should be at the latest versions.  In reality, dependencies may lag, and be out of sync with each other.

### Specific Dependencies

- `nodejsVersion` - uPortal and uPortal-start use NodeJS for building artifacts and Gradle tasks. They are not required to be in sync. Ideally they should be on the same major version, but minor and patch version don't need to match
- `lombokVersion` - Should stay in sync across the dependency areas
- `slf4jVersion` - Should stay in sync across the dependency areas
- `resourceServerVersion` - Should use the latest version in uPortal, and should generally stay at `1.0.48` in uPortal-start. These two resource server versions are due to uPortal-start needing some older dependencies to run a portal with the included UX.
- `resourceServer13Version` - Only in uPortal-start. Should always be at the latest version
- `personDirectoryVersion` - Should stay in sync across the dependency areas

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
  * Pay attention to new changes since last release

## Cut Release

Run the following command in the uPortal clone's directory:

```sh
~.../apereo/uPortal$ ./gradlew clean release --no-parallel
```

:Note: During the `release` task, you will be prompted for a release version (e.g. `5.0.3`).  Press `Enter` for the default.  The release process will then create two commits - a commit to set the new version (`5.0.3`), and a commit to set the version to the new snapshot (`5.0.4-SNAPSHOT`)

## Verify and Publish from Central Publisher Portal

Because uPortal uses Gradle's legacy `uploadArchives` (a Maven-API-like plugin), the OSSRH Staging API does **not** automatically close the staging repository. After the `release` task completes, you must manually trigger the upload to the Central Portal.

### Push staged artifacts to the portal

Run this from the **same machine** that ran the release (same IP is required):

```sh
curl -X POST \
  "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/org.jasig.portal" \
  -H "Authorization: Bearer $(echo -n '{ossrhUsername}:{ossrhPassword}' | base64)"
```

Replace `{ossrhUsername}` and `{ossrhPassword}` with your Central Portal user token credentials (the same ones in `~/.gradle/gradle.properties`).

### Review and publish

1. Log into <https://central.sonatype.com>
2. Navigate to your deployments — the staged artifacts should now be visible
3. Review the release for the expected artifacts
4. Verify the artifacts pass validation checks (signatures, POM requirements)
5. Publish the deployment to make it available on Maven Central

If the deployment does not appear, you can search for open repositories:

```sh
curl -X GET \
  "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?ip=any&profile_id=org.jasig.portal" \
  -H "Authorization: Bearer $(echo -n '{ossrhUsername}:{ossrhPassword}' | base64)"
```

## Create Release Notes

1. Use Git to inspect the incremental commits since the last release (e.g. `$ git log v5.0.2..v5.0.3 --no-merges`)
2. Review the issue tracker and confirm that the referenced issues have been Resolved
3. Enter the release notes on the GitHub releases page in the `uPortal` repo
  - It's helpful to use the previous release notes as a guide
  - Each commit type goes into a sub section with the type as a header (Fixes, Chores, Features, etc...).

## Update uPortal-start

Open a Pull Request on `uPortal-start` to update `uPortalVersion` to the new release.

## Update Community
For any non-snapshot release, email an announcement to `uportal-dev`, `uportal-user`, and `jasig-announce`.
  - Be sure to acknowledge those who contributed to the release.
  - Be sure to put the release into context for existing adopters to understand.

Have someone with access to the uPortal Twitter account announce the release.

## References

(https://apereo.atlassian.net/wiki/spaces/UPC/pages/102336655/Cutting+a+uPortal+Release)
(https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/)
(https://central.sonatype.org/publish/publish-portal-guide/)
(https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)
