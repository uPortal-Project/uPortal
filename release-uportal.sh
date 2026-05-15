#!/usr/bin/env bash
#
# Licensed to Apereo under one or more contributor license
# agreements. See the NOTICE file distributed with this work
# for additional information regarding copyright ownership.
# Apereo licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file
# except in compliance with the License.  You may obtain a
# copy of the License at the following location:
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# uPortal release preflight + cut script (Gradle).
#
# Authoritative procedure — see the uPortal release guide:
#   docs/developer/other/RELEASE.md
# Sister script for Maven-based portlets:
#   ../<PortletRepo>/release-portlet.sh (see Maven release guide:
#   https://uportal-project.github.io/manuals/en/uportal5-manual/developer/maven-release-process.md)
# Updates to the release flow should land in those docs first, then mirror here.

set -euo pipefail

usage() {
  cat <<'EOF'
Usage: release-uportal.sh --release X.Y.Z --next X.Y.Z+1-SNAPSHOT [options]

Required:
  --release VER       version to cut, e.g. 5.17.9
  --next VER          next snapshot, e.g. 5.17.10-SNAPSHOT

Options:
  --branch NAME       branch to release from (default: master)
  --require-pr SHA    require commit SHA reachable from HEAD (repeatable)

Credentials are read from ~/.gradle/gradle.properties:
  ossrhUsername / ossrhPassword  — Central Portal user token
  signing.keyId / signing.password / signing.secretKeyRingFile

Phases:
  1. Tree state — clean, on expected branch, in sync with upstream
  2. Env — Java 11, ~/.gradle/gradle.properties has all creds + signing,
     GPG key on keys.openpgp.org
  3. License header check — modified .java/.xml/.jsp since last release
     all carry the Apache 2.0 header
  4. Build smoke — ./gradlew clean install
  5. Artifact-not-on-Central check — uPortal-core-<release>.pom is 404
  6. Summary + y/N confirmation
  7. Cut release — ./gradlew clean release --no-parallel with non-interactive
     -Prelease.useAutomaticVersion=true / -Prelease.releaseVersion=<X.Y.Z>
     / -Prelease.newVersion=<X.Y.Z+1>-SNAPSHOT
  8. OSSRH POST — push staged repo into Central Portal staging UI
  9. Push tag to upstream — the Gradle release plugin only pushes to origin
 10. Print next steps (Publish in Portal UI, watch propagation)

NOTE on NOTICE/license tooling: uPortal's Gradle build does not currently
have automated NOTICE-file regeneration (no Gradle equivalent of mvn
notice:generate). Before running this script, manually review NOTICE for
copyright currency per docs/developer/other/RELEASE.md §"Review NOTICE
and License Headers".
EOF
  exit 1
}

RELEASE=""; NEXT=""; BRANCH="master"; REQUIRE_PRS=()
while [[ $# -gt 0 ]]; do
  case "$1" in
    --release) RELEASE="$2"; shift 2 ;;
    --next) NEXT="$2"; shift 2 ;;
    --branch) BRANCH="$2"; shift 2 ;;
    --require-pr) REQUIRE_PRS+=("$2"); shift 2 ;;
    -h|--help) usage ;;
    *) echo "unknown arg: $1" >&2; usage ;;
  esac
done
[[ -z "$RELEASE" || -z "$NEXT" ]] && usage

if [[ -f .sdkmanrc && -s "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
  set +u; source "$HOME/.sdkman/bin/sdkman-init.sh"; sdk env > /dev/null; set -u
fi

ok()   { printf '  \033[32m✓\033[0m %s\n' "$*"; }
warn() { printf '  \033[33m⚠\033[0m %s\n' "$*"; }
fail() { printf '  \033[31m✗\033[0m %s\n' "$*" >&2; exit 1; }
hdr()  { printf '\n→ %s\n' "$*"; }

# 1. Tree state
hdr "Tree state"
DIRTY=$(git status --porcelain)
if [[ -n "$DIRTY" ]]; then
  printf '\n%s\n\n' "$DIRTY"
  fail "working tree not clean — move/commit/stash the above before re-running (the Gradle release plugin requires a fully clean tree, including untracked files like .agent-tmp/)"
fi
ok "working tree clean"

CURRENT=$(git rev-parse --abbrev-ref HEAD)
[[ "$CURRENT" == "$BRANCH" ]] || fail "on '$CURRENT', expected '$BRANCH'"
ok "on branch $BRANCH"

git fetch upstream "$BRANCH" --tags --quiet
LOCAL_HEAD=$(git rev-parse HEAD)
UPSTREAM_HEAD=$(git rev-parse "upstream/$BRANCH")
if [[ "$LOCAL_HEAD" != "$UPSTREAM_HEAD" ]]; then
  AHEAD=$(git rev-list --count "upstream/$BRANCH..HEAD")
  BEHIND=$(git rev-list --count "HEAD..upstream/$BRANCH")
  fail "local $BRANCH diverged from upstream (ahead $AHEAD / behind $BEHIND) — reconcile first"
fi
ok "HEAD == upstream/$BRANCH (${LOCAL_HEAD:0:10})"

for SHA in "${REQUIRE_PRS[@]:-}"; do
  [[ -z "$SHA" ]] && continue
  git merge-base --is-ancestor "$SHA" HEAD || fail "required commit $SHA not reachable from HEAD"
  ok "required commit ${SHA:0:10} reachable"
done

# Current version must be a SNAPSHOT to be releasable
CURRENT_VERSION=$(grep '^version=' gradle.properties | cut -d= -f2)
[[ "$CURRENT_VERSION" == *-SNAPSHOT ]] || fail "current version '$CURRENT_VERSION' is not a SNAPSHOT; the release plugin needs to start from a -SNAPSHOT"
ok "current version: $CURRENT_VERSION (will release as $RELEASE, then bump to $NEXT)"

# 2. Env & credentials
hdr "Env & credentials"
JAVA_VER=$(java -version 2>&1 | awk -F\" 'NR==1{print $2}')
[[ "$JAVA_VER" == 11.* ]] || warn "java $JAVA_VER — expected 11.x"
ok "java $JAVA_VER"

GRADLE_PROPS="$HOME/.gradle/gradle.properties"
[[ -f "$GRADLE_PROPS" ]] || fail "$GRADLE_PROPS missing — see docs/developer/other/RELEASE.md §Setup"
for KEY in ossrhUsername ossrhPassword signing.keyId signing.password signing.secretKeyRingFile; do
  grep -q "^$KEY=" "$GRADLE_PROPS" || fail "$KEY not set in $GRADLE_PROPS"
done
ok "~/.gradle/gradle.properties has ossrh + signing config"

SIGNING_KEYID=$(grep '^signing.keyId=' "$GRADLE_PROPS" | cut -d= -f2 | tr -d ' ')
FINGERPRINT=$(gpg --list-keys --with-colons "$SIGNING_KEYID" 2>/dev/null | awk -F: '/^fpr:/{print $10; exit}')
[[ -n "$FINGERPRINT" ]] || fail "signing.keyId=$SIGNING_KEYID not found in keyring"
ok "signing key: $SIGNING_KEYID → fingerprint $FINGERPRINT"

HTTP=$(curl -so /dev/null -w "%{http_code}" "https://keys.openpgp.org/vks/v1/by-fingerprint/$FINGERPRINT")
[[ "$HTTP" == "200" ]] || fail "key $FINGERPRINT not on keys.openpgp.org (HTTP $HTTP) — upload via https://keys.openpgp.org/upload"
ok "key reachable on keys.openpgp.org"

# 3. License header check on changed source files since the last release
hdr "License headers on changed source files"
PREV_TAG=$(git describe --tags --abbrev=0 2>/dev/null || true)
if [[ -z "$PREV_TAG" ]]; then
  warn "no previous tag found; skipping header check"
else
  ok "comparing against $PREV_TAG"
  MISSING=""
  while IFS= read -r f; do
    [[ -z "$f" ]] && continue
    [[ ! -f "$f" ]] && continue
    if ! head -5 "$f" | grep -qE "Licensed to Apereo|Licensed under the Apache License"; then
      MISSING+="$f"$'\n'
    fi
  done < <(git diff --name-only "$PREV_TAG"..HEAD -- '*.java' '*.groovy' '*.xml' '*.jsp' '*.jspf' '*.tld' '*.sh' 2>/dev/null)
  if [[ -n "$MISSING" ]]; then
    printf '\n%s\n' "$MISSING"
    fail "files above are missing the Apache 2.0 license header — add headers and commit before releasing"
  fi
  ok "all changed source files carry the Apache 2.0 header"
fi

# 4. Build smoke
hdr "Build smoke (./gradlew clean install)"
./gradlew clean install --no-parallel > /tmp/release-build.log 2>&1 \
  || { echo; tail -30 /tmp/release-build.log; fail "build failed (see /tmp/release-build.log)"; }
ok "clean install passed"

# 5. Artifact-not-on-Central check
hdr "Artifact discovery"
GROUP_ID="org.jasig.portal"
CANARY_ARTIFACT="uPortal-core"
ok "groupId:        $GROUP_ID"
ok "canary artifact: $CANARY_ARTIFACT"

CANARY_URL="https://repo1.maven.org/maven2/org/jasig/portal/$CANARY_ARTIFACT/$RELEASE/$CANARY_ARTIFACT-$RELEASE.pom"
EXISTING=$(curl -sI -o /dev/null -w "%{http_code}" "$CANARY_URL")
[[ "$EXISTING" == "404" ]] || fail "$CANARY_ARTIFACT-$RELEASE already on Central (HTTP $EXISTING) — pick a new version"
ok "$CANARY_ARTIFACT-$RELEASE not on Central (HTTP 404, as expected)"

OSSRH_USER=$(grep '^ossrhUsername=' "$GRADLE_PROPS" | cut -d= -f2-)

# 6. Summary
cat <<EOF

═══ RELEASE SUMMARY ═══
  Repo:          $(basename "$PWD")
  Branch:        $BRANCH @ ${LOCAL_HEAD:0:10}
  HEAD subject:  $(git log -1 --pretty=%s)
  groupId:       $GROUP_ID
  Current ver:   $CURRENT_VERSION
  Release ver:   $RELEASE          (tag: v$RELEASE)
  Next snapshot: $NEXT
  Signing key:   $FINGERPRINT
  ossrh user:    $OSSRH_USER
  Canary URL:    $CANARY_URL
═══════════════════════

EOF
read -r -p "Proceed with release? (y/N) " ANS
[[ "$ANS" == "y" || "$ANS" == "Y" ]] || { echo "aborted"; exit 0; }

# 7. Cut release
hdr "./gradlew clean release --no-parallel"
./gradlew clean release --no-parallel \
  -Prelease.useAutomaticVersion=true \
  -Prelease.releaseVersion="$RELEASE" \
  -Prelease.newVersion="$NEXT"
ok "release cut + tag pushed to origin"

# 8. OSSRH POST — push staged artifacts to Central Portal
hdr "POST staged artifacts to Central Portal"
OSSRH_PASS=$(grep '^ossrhPassword=' "$GRADLE_PROPS" | cut -d= -f2-)
AUTH=$(printf '%s:%s' "$OSSRH_USER" "$OSSRH_PASS" | base64)
HTTP=$(curl -s -o /tmp/release-ossrh.out -w "%{http_code}" -X POST \
  "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/$GROUP_ID" \
  -H "Authorization: Bearer $AUTH")
[[ "$HTTP" == "200" ]] || fail "OSSRH POST HTTP $HTTP (see /tmp/release-ossrh.out)"
ok "OSSRH POST: HTTP 200"

# 9. Push tag to upstream — the Gradle release plugin only pushes to origin
hdr "Push v$RELEASE tag to upstream"
git push upstream "v$RELEASE"
ok "v$RELEASE tag on upstream"

# 10. Next steps
cat <<EOF

✓ Release prepared.

Next steps:
  1. Log into https://central.sonatype.com
  2. Navigate to Deployments → verify uPortal artifacts (uPortal-core,
     uPortal-webapp, etc.) at version $RELEASE → click Publish
  3. Wait for propagation (~10-30 min); poll:
       curl -sI -o /dev/null -w "HTTP %{http_code}\\n" "$CANARY_URL"
  4. After propagation: gh release create v$RELEASE --repo uPortal-Project/uPortal
     with release notes drafted from git log v<prev>..v$RELEASE
  5. Open PR on uPortal-start bumping uPortalVersion=$RELEASE in gradle.properties.example

EOF
