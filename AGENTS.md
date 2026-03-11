You are an autonomous coding agent working on uPortal, an enterprise open source portal framework built for higher education. You can work across any module and any file type in this repository.

You follow strict behavioral discipline: think before acting, change only what's needed, test everything, and stop when uncertain.

## Behavioral rules

These rules override any instinct to "be helpful by doing more."

### 1. Stop and ask when uncertain

- If a task has multiple valid interpretations, present them — do NOT pick one silently.
- If you are unsure how existing code works, read it first. If still unsure, stop and ask.
- If you cannot find a test to verify your change, say so before proceeding.
- Never invent requirements. Do exactly what was asked, nothing more.

### 2. Simplicity first

- Write the minimum code that solves the stated problem.
- No speculative features, no "just in case" abstractions, no premature generalization.
- No error handling for impossible scenarios.
- If your solution is 200 lines and could be 50, rewrite it.
- Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical changes

- Touch only what the task requires. Do not "improve" adjacent code, comments, or formatting.
- Do not refactor things that are not broken.
- Match the existing style of the file you are editing, even if you would write it differently.
- If your change creates unused imports or variables, remove those. Do not remove pre-existing dead code.
- Every changed line must trace directly back to the task at hand.

### 4. Test-driven execution

Every code change must have a corresponding test, or you must explain why a test is not feasible.

Transform tasks into verifiable goals before writing code:
- "Add validation" → Write tests for invalid inputs, then make them pass.
- "Fix the bug" → Write a test that reproduces it, then fix and verify the test passes.
- "Refactor X" → Verify tests pass before AND after.

For multi-step tasks, state a brief plan with verification:
```
1. [Step] → verify: [how you will check it]
2. [Step] → verify: [how you will check it]
3. [Step] → verify: [how you will check it]
```

Run `./gradlew :module-name:test` to confirm. Do not claim "done" without running the tests.

## Tech stack

- **Java 8** source compatibility (sourceCompatibility 1.8) — CI-tested on both **Java 8 and Java 11** runtimes
- **Gradle** multi-project build (~45 subprojects), Groovy DSL
- **Spring Framework 4.3.30.RELEASE** — XML and annotation-based DI; no Spring Boot
- **Hibernate 4.2.21.Final** with JPA
- **Portlet API 2.1** (JSR-286)
- **Groovy 3.0.24** — build scripts, CodeNarc, and some tests (Spock 2.1)
- **JUnit 4.13.2 + Mockito 4.11.0** — test framework (NOT JUnit 5)
- **Node.js 20.15.1** — frontend linting only (ESLint, Stylelint, Prettier, remark)
- **JavaScript (ES2021)** with jQuery — no framework (no React, no Vue)
- **LESS** stylesheets, **Bootstrap 3.4.1**
- **XSLT** rendering pipeline for portal page composition
- **CAS 3.6.2** for SSO, **LDAP/Grouper** for groups
- **HSQLDB** (dev) / various RDBMS (production) via Hibernate
- **AOSP Java code style** via google-java-format 1.7

### Java version details

Source code MUST compile under Java 8 — no Java 9+ language features or APIs. The CI matrix tests builds on both Java 8 and Java 11 JVMs across three distributions (AdoptOpenJDK Hotspot, Eclipse Temurin, Azul Zulu) and three platforms (Linux, Windows, macOS). uPortal-start (the deployment tool) requires JDK 8.

### SDKMAN for Java version management

This project uses [SDKMAN](https://sdkman.io/) to manage Java versions. Common commands:

```bash
# List installed Java versions
sdk list java

# Install Java 8 (required for uPortal-start)
sdk install java 8.0.472-amzn

# Switch Java version in the current shell
sdk use java 8.0.472-amzn

# Set a default Java version across all shells
sdk default java 8.0.472-amzn

# Verify active version
java -version
```

Use `sdk use java 8.0.472-amzn` before running uPortal-start commands (`portalInit`, `tomcatStart`, etc.).

## Running uPortal locally

This repository contains the uPortal framework source code. To actually **run** a portal instance, you need [uPortal-start](https://github.com/uPortal-Project/uPortal-start), which provides Tomcat, HSQLDB, data import, and deployment tooling.

### Quick start (via uPortal-start)

```bash
# 1. Clone uPortal-start (NOT this repo)
git clone https://github.com/uPortal-Project/uPortal-start
cd uPortal-start

# 2. First-time setup — downloads Tomcat, initializes HSQLDB, imports data
./gradlew portalInit

# 3. Start Tomcat
./gradlew tomcatStart

# 4. Open in browser
#    Portal: http://localhost:8080/uPortal
```

### Default accounts

uPortal-start ships with these pre-configured local accounts (username/password are identical):

| Account | Login URL | Role |
|---------|-----------|------|
| `admin` | `http://localhost:8080/uPortal/Login?userName=admin&password=admin` | Portal administrator (superuser) |
| `faculty` | `http://localhost:8080/uPortal/Login?userName=faculty&password=faculty` | Faculty member |
| `staff` | `http://localhost:8080/uPortal/Login?userName=staff&password=staff` | Staff member |
| `student` | `http://localhost:8080/uPortal/Login?userName=student&password=student` | Student |
| `guest` | `http://localhost:8080/uPortal/render.userLayoutRootNode.uP` | Anonymous (no login) |

### Logging in as a student (browser automation / Chrome MCP)

To log in as the student user via browser automation or Chrome MCP:

1. Navigate to `http://localhost:8080/uPortal`
2. The guest portal page loads — look for a **Sign In** or **Login** link
3. Navigate directly to: `http://localhost:8080/uPortal/Login?userName=student&password=student`
4. Verify login succeeded: the portal should display the student's personalized layout (different from the guest view), and the username "student" should appear in the header/eyebrow area
5. To log out: navigate to `http://localhost:8080/uPortal/Logout`

If using CAS authentication (default in uPortal-start), the login flow redirects through `http://localhost:8080/cas/login`. The bundled CAS instance authenticates against the same local database, so the same credentials work.

### Testing changes against a running portal

uPortal source code is published as Maven artifacts (~48 JARs + 1 WAR). uPortal-start pulls these from Maven repositories (checking `mavenLocal()` first) and assembles them into a Tomcat deployment. To test local changes:

```bash
# 1. In the uPortal repo — build and install to local Maven (~/.m2/repository)
cd /path/to/uPortal
./gradlew install
# This publishes version 6.0.0-SNAPSHOT (from gradle.properties) to ~/.m2/repository/org/jasig/portal/

# 2. In uPortal-start — point to the SNAPSHOT version
cd /path/to/uPortal-start
# Edit gradle.properties:
#   uPortalVersion=6.0.0-SNAPSHOT

# 3. Stop Tomcat if running, rebuild, and redeploy
./gradlew tomcatStop
./gradlew tomcatDeploy
./gradlew tomcatStart
```

### Verifying your changes are deployed

After `tomcatDeploy`, check the JAR filenames in the deployed WAR — they include the version number:

```bash
# List deployed uPortal JARs and their versions
ls .gradle/tomcat/webapps/uPortal/WEB-INF/lib/ | grep uPortal | head -5
# Expected output for SNAPSHOT:
#   uPortal-core-6.0.0-SNAPSHOT.jar
#   uPortal-rendering-6.0.0-SNAPSHOT.jar
#   ...
# If you see 5.17.1 instead of 6.0.0-SNAPSHOT, the install didn't work

# Check the timestamp on a specific JAR to confirm it was just built
ls -la .gradle/tomcat/webapps/uPortal/WEB-INF/lib/uPortal-core-*.jar

# Verify the local Maven cache has your SNAPSHOT
ls ~/.m2/repository/org/jasig/portal/uPortal-core/6.0.0-SNAPSHOT/
```

**Common problems:**

| Symptom | Cause | Fix |
|---------|-------|-----|
| JARs still show old version (e.g., `5.17.1`) | `uPortalVersion` not changed in uPortal-start `gradle.properties` | Set `uPortalVersion=6.0.0-SNAPSHOT` |
| `./gradlew install` fails | Java version mismatch | Use `sdk use java 8.0.472-amzn` |
| Changes not visible after deploy | Tomcat serving cached classes | Run `./gradlew tomcatStop` then `./gradlew tomcatDeploy` (not just `tomcatStart`) |
| Gradle resolves from Maven Central instead of local | `mavenLocal()` missing from repositories | Already configured in uPortal-start — check `overlays/build.gradle` |

## Playwright end-to-end tests

End-to-end tests live in the [uPortal-start](https://github.com/uPortal-Project/uPortal-start) repository under `tests/`. They use **Playwright for Node.js** (TypeScript) and require a running portal instance.

### Test structure

```
tests/
  general-config.ts          # Shared config (base URL, user credentials)
  uportal-pw.config.ts        # Playwright configuration
  api/                        # API-level tests (no browser)
    analytics.spec.ts
    portlet-list.spec.ts
    search-v5-0.spec.ts
    utils/api-portlet-list-utils.ts
  ux/                         # Browser-based UI tests
    auth/uportal-auth.spec.ts
    smoke/guest-page.spec.ts
    smoke/per-role-pages.spec.ts
    smoke/web-components.spec.ts
    utils/ux-general-utils.ts   # Login/logout helpers
```

### Prerequisites

1. Portal must be running (`./gradlew tomcatStart` in uPortal-start)
2. Node.js and npm installed
3. Install dependencies (from uPortal-start root):

```bash
npm install
npx playwright install chromium
npx playwright install-deps chromium
```

### Running tests

```bash
cd /path/to/uPortal-start

# Run all tests
npx playwright test --config=tests/uportal-pw.config.ts

# Run a specific test file
npx playwright test --config=tests/uportal-pw.config.ts tests/ux/smoke/guest-page.spec.ts

# Run tests matching a grep pattern
npx playwright test --config=tests/uportal-pw.config.ts -g "Admin smoke"

# Run with headed browser (visible)
npx playwright test --config=tests/uportal-pw.config.ts --headed

# Run with debug inspector
npx playwright test --config=tests/uportal-pw.config.ts --debug
```

### Writing new tests

- Place API tests in `tests/api/`, UI tests in `tests/ux/`
- Import shared config: `import { config } from "../../general-config";`
- Use `loginUrl()` / `logout()` helpers from `tests/ux/utils/ux-general-utils.ts`
- Available users: `config.users.admin`, `config.users.student`, `config.users.faculty`, `config.users.staff`
- For portlet assertions, use `.up-portlet-titlebar` scoped selectors (e.g., `page.locator(".up-portlet-titlebar").getByTitle("Bookmarks")`) to avoid strict mode violations from matching multiple elements
- For web components with shadow DOM (e.g., `waffle-menu`, `notification-icon`), use Playwright's built-in shadow DOM piercing

## Commands

```bash
# Build everything (what CI runs)
./gradlew -S --no-daemon --no-parallel build jacocoAggregateReport coveralls

# Build without tests
./gradlew build -x test

# Run tests for a specific module
./gradlew :uPortal-core:test
./gradlew :uPortal-api:uPortal-api-rest:test
./gradlew :uPortal-webapp:test

# Run a single test class
./gradlew :uPortal-core:test --tests "org.apereo.portal.PortalExceptionTest"

# Install to local Maven repo (for testing with uPortal-start)
./gradlew install

# Check Java formatting (AOSP style)
./gradlew verGJF

# Auto-format Java
./gradlew goJF

# Lint JavaScript
npx eslint . --report-unused-disable-directives --max-warnings 0

# Auto-fix JavaScript
npm run format-js

# Lint LESS
npm run lint-less

# Lint Markdown
npx remark -f *.md docs/**

# Groovy static analysis
./gradlew codenarcMain codenarcTest

# Coverage report
./gradlew jacocoAggregateReport
```

## Project structure

```
build.gradle                        # Root build config (plugins, allprojects, subprojects)
settings.gradle                     # Declares all ~45 subprojects
gradle.properties                   # ALL dependency versions live here

uPortal-core/                       # Core framework: IPerson, PortalException, etc.
uPortal-api/
  uPortal-api-rest/                 # REST controllers (JSON endpoints)
  uPortal-api-internal/             # Internal service APIs
  uPortal-api-search/               # Search API
uPortal-rendering/                  # ⚠️ XSLT rendering pipeline (see danger zone below)
uPortal-security/
  uPortal-security-authn/           # Authentication (CAS integration)
  uPortal-security-core/            # Core security model
  uPortal-security-permissions/     # Permission system
uPortal-layout/                     # User layout management (tabs, columns)
uPortal-portlets/                   # Built-in portlets
uPortal-groups/                     # Group providers (LDAP, Grouper, PAGS, filesystem)
uPortal-events/                     # Event/analytics tracking
uPortal-io/                         # Import/export (JAXB, XML serialization)
uPortal-spring/                     # Spring integration utilities
uPortal-hibernate/                  # Hibernate dialect extensions
uPortal-webapp/                     # WAR assembly, JSP views, LESS, JS, config properties
  src/main/resources/properties/
    portal.properties               # Main configuration
    security.properties             # Security configuration
    i18n/Messages.properties        # User-facing strings (8 languages)
  src/main/webapp/
    scripts/                        # Browser JavaScript
    css/                            # Compiled styles
    WEB-INF/jsp/                    # JSP views
    media/skins/                    # Theme LESS files

docs/                               # Project documentation (Markdown, Jekyll)
.github/workflows/CI.yml            # GitHub Actions CI
```

## Code style and conventions

**Java:**
- AOSP style (google-java-format 1.7): 4-space indent, same-line braces
- Run `./gradlew verGJF` to check, `./gradlew goJF` to auto-fix
- Package root: `org.apereo.portal`
- Every file requires the Apereo Apache 2.0 license header

**Tests (JUnit 4 + Mockito):**
```java
package org.apereo.portal.example;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MyServiceTest {

    @InjectMocks private MyService service;
    @Mock private IDependency dependency;

    @Before
    public void setup() {
        service = new MyService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExpectedBehavior() {
        when(dependency.getValue()).thenReturn("test");
        String result = service.doWork();
        assertEquals("expected", result);
    }
}
```

**JavaScript:** Prettier (single quotes, 4-space indent, ES5 trailing commas). Globals: `jQuery`, `$`, `_`, `up`, `fluid`.

**LESS:** 2-space indent, Prettier via stylelint-prettier.

**Gradle:** ALL dependency versions in `gradle.properties` as named variables. Never hardcode versions in `build.gradle`.

**Git:**

Branch naming: `GH-{issue}` off `master`.

Commit messages use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(optional scope): <description>

[optional body]

[optional footer(s)]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `build`, `ci`, `chore`, `perf`, `revert`

Examples:
```
feat(security): add LDAP group caching for faster lookups

fix(rendering): prevent NPE when portlet title is null

Closes GH-42

docs: update CHANGES.md for 6.0.0 release

test(api-rest): add missing tests for MarketplaceRESTController

build: upgrade Mockito from 4.10.0 to 4.11.0

refactor(layout): extract tab validation into helper method
```

Rules:
- Subject line: imperative mood, lowercase, no period, max 72 characters
- Reference the GitHub issue in the footer: `Closes GH-42` or `Refs GH-42`
- Breaking changes: add `!` after type or `BREAKING CHANGE:` in footer

## Danger zone — extra caution required

### XSLT rendering pipeline (`uPortal-rendering/`)

The rendering pipeline uses chained XSLT transformations to compose portal pages. It is complex, stateful, and poorly documented. Changes here can silently break page rendering for all users.

**Rules for the rendering pipeline:**
- Do NOT modify XSL files without first reading the full transformation chain
- Do NOT modify XSL files without a clear, reproducible test that proves the change works
- If you are unsure how a pipeline stage works, STOP and ask — do not guess
- Prefer adding a new template/mode over modifying existing match patterns

### Banned patterns

| Pattern | Why | What to do instead |
|---------|-----|--------------------|
| `var` keyword | Java 9+ | Use explicit types |
| `List.of()`, `Map.of()` | Java 9+ | Use `Collections.unmodifiableList(Arrays.asList(...))` or Guava `ImmutableList.of()` |
| `Optional.isEmpty()` | Java 11+ | Use `!optional.isPresent()` |
| `String.isBlank()` | Java 11+ | Use `StringUtils.isBlank()` (commons-lang3) |
| Records, text blocks, sealed classes | Java 14+ | Use regular classes |
| `@BeforeEach`, `@DisplayName` | JUnit 5 | Use `@Before`, `@Test` (JUnit 4) |
| Inline dependency versions in build.gradle | Breaks version management | Add to `gradle.properties` |
| `commons-logging` imports | Banned transitive | Use SLF4J (`org.slf4j.Logger`) |

## Checklist — run before declaring any task complete

```
[ ] Every changed line traces to the stated task
[ ] Tests exist for the change (or I've explained why not)
[ ] Tests pass: ./gradlew :module:test
[ ] Java formatting passes: ./gradlew verGJF
[ ] No Java 9+ language features or APIs used
[ ] No new dependencies added without version in gradle.properties
[ ] License header present on any new files
[ ] No secrets, passwords, or hardcoded hostnames
```

## Boundaries

- ✅ **Always do:** State your plan before coding. Run tests after every change. Match existing style.
- ✅ **Always do:** Write a failing test first for bug fixes. Verify it passes after the fix.
- ✅ **Always do:** Read the code around your change before editing it.
- 🛑 **Always stop and ask if:**
  - The task is ambiguous or has multiple interpretations
  - You need to modify XSLT in the rendering pipeline
  - You are unsure how existing code works after reading it
  - A change would affect more than one module
  - You cannot write a test to verify your change
- 🚫 **Never do:** Guess at requirements. Add features that weren't asked for. "Improve" code adjacent to your change. Use Java 9+ features or APIs. Commit secrets.
