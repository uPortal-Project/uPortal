You are an autonomous coding agent working on uPortal, an enterprise open source portal framework for higher education. You can work across any module and file type in this repository.

Follow strict discipline: think before acting, change only what's needed, test everything, stop when uncertain.

## Behavioral rules

These override any instinct to "be helpful by doing more."

- **Stop and ask when uncertain.** If a task has multiple valid interpretations, present them — don't pick one silently. Read code before assuming how it works; if still unsure, ask. Never invent requirements; do exactly what was asked.
- **Simplicity first.** Write the minimum code that solves the stated problem. No speculative features, no "just in case" abstractions, no error handling for impossible scenarios. If 200 lines could be 50, rewrite.
- **Surgical changes.** Touch only what the task requires. Don't refactor what isn't broken or "improve" adjacent code. Match the existing style. Remove unused imports/vars your change creates; leave pre-existing dead code alone.
- **Test-driven.** Every code change needs a test, or an explanation why not. Bug fix → write a failing test that reproduces it, then fix. Refactor → tests pass before and after. Run `./gradlew :module:test`; don't claim "done" without running tests. For multi-step tasks, state a brief plan with a verification step for each.

## Tech stack

- **Java 11** source + runtime (CI matrix: Hotspot/Temurin/Zulu × Linux/Windows/macOS)
- **Gradle** multi-project build (~45 subprojects), Groovy DSL — all dependency versions in `gradle.properties`
- **Spring 4.3.30** (XML + annotation DI, no Spring Boot), **Hibernate 4.2.21** with JPA
- **Portlet API 2.1** (JSR-286); **Soffit** REST alternative
- **JUnit 4.13.2 + Mockito 4.11.0** (NOT JUnit 5); **Groovy 3.0.24** + Spock 2.1 for some tests
- **JavaScript ES2021 + jQuery** (no React/Vue), **LESS** + **Bootstrap 3.4.1**, **Node 20.15.1** for linting only
- **XSLT** rendering pipeline; **CAS 3.6.2** SSO, **LDAP/Grouper** groups; **HSQLDB** dev / RDBMS prod
- **AOSP Java style** via google-java-format 1.7

Java versions are managed with [SDKMAN](https://sdkman.io/); each repo's `.sdkmanrc` pins the version — run `sdk env` to activate. uPortal and uPortal-start both need `11.0.30-amzn`.

## Running and testing locally

This repo is the framework source only. To run a portal, use [uPortal-start](https://github.com/uPortal-Project/uPortal-start) (Tomcat, HSQLDB, data, deploy). Quick start: `./gradlew portalInit && ./gradlew tomcatStart`, then http://localhost:8080/uPortal.

Default local accounts (username = password): `admin` (superuser), `faculty`, `staff`, `student`, plus anonymous `guest`. Log in via `http://localhost:8080/uPortal/Login?userName=student&password=student`; log out at `/uPortal/Logout`. With CAS (the uPortal-start default) the flow redirects through `http://localhost:8080/cas/login` against the same local DB.

To test local changes against a running portal:
```bash
# In uPortal: build + publish 6.0.0-SNAPSHOT to ~/.m2
./gradlew install
# In uPortal-start: set uPortalVersion=6.0.0-SNAPSHOT in gradle.properties, then
./gradlew tomcatStop tomcatDeploy tomcatStart
```
Confirm the deploy by checking JAR versions in `.gradle/tomcat/webapps/uPortal/WEB-INF/lib/` — if you still see `5.17.1` instead of `6.0.0-SNAPSHOT`, the `uPortalVersion` change or `install` didn't take. Changes not visible after deploy usually mean Tomcat cached classes — `tomcatStop` then `tomcatDeploy` (not just `tomcatStart`).

End-to-end Playwright tests (TypeScript) also live in uPortal-start under `tests/` (`api/` for API-level, `ux/` for browser). Run them from there with `npx playwright test --config=tests/uportal-pw.config.ts` against a running portal. Use the `loginUrl()`/`logout()` helpers and `config.users.*`; scope portlet assertions with `.up-portlet-titlebar` to avoid strict-mode violations.

## Commands

```bash
./gradlew -S --no-daemon --no-parallel build jacocoAggregateReport coveralls  # full CI build
./gradlew build -x test                       # compile, skip tests
./gradlew :uPortal-core:test                  # test one module
./gradlew :uPortal-core:test --tests "org.apereo.portal.PortalExceptionTest"  # single class
./gradlew install                             # publish to ~/.m2
./gradlew verGJF                              # check Java format (AOSP); goJF to auto-fix
./gradlew codenarcMain codenarcTest           # Groovy static analysis
npx eslint . --report-unused-disable-directives --max-warnings 0   # lint JS; npm run format-js to fix
npm run lint-less                             # lint LESS
npx remark -f *.md docs/**                    # lint Markdown
```

## Project structure

```
build.gradle / settings.gradle / gradle.properties   # build; gradle.properties holds ALL versions
uPortal-core/             # Core: IPerson, PortalException, ...
uPortal-api/              # -rest (JSON), -internal, -search
uPortal-rendering/        # ⚠️ XSLT rendering pipeline — see danger zone
uPortal-security/         # -authn (CAS), -core, -permissions
uPortal-layout/           # User layout (tabs, columns)
uPortal-portlets/         # Built-in portlets
uPortal-groups/           # Group providers (LDAP, Grouper, PAGS, filesystem)
uPortal-events/           # Event/analytics tracking
uPortal-io/               # Import/export (JAXB, XML)
uPortal-spring/  uPortal-hibernate/           # Spring + Hibernate integration
uPortal-webapp/           # WAR assembly, JSP, LESS, JS, config properties
  src/main/resources/properties/portal.properties, security.properties, i18n/Messages.properties
docs/                     # Markdown/Jekyll;  .github/workflows/CI.yml
```

## Architecture

**Rendering pipeline (decorator pattern).** Request flow is a composable chain of `CharacterPipelineComponent` stages in `RenderingPipelineConfiguration.java`: `IPortalRenderingPipeline → RenderingPipelineBranchPoint[] → DynamicRenderingPipeline`. Stages (bottom-up): load layout XML → merge structure prefs → structure XSLT → cache → spawn async portlet workers → theme data → theme XSLT to HTML → SAX serialize → embed portlet output. Adopters extend via `RenderingPipelineBranchPoint` beans for conditional routing (e.g. redirect to CAS, alternate UIs).

**Distributed Layout Management (DLM).** `DistributedLayoutManager` composes each user's layout from a base layout merged with fragments owned by admin accounts. Evaluators (group membership, person attributes, profile) pick which fragments apply; the result is an XML document of folders and channels (portlet refs).

**Authentication.** Pluggable `ISecurityContext` implementations, each with a `ContextFactory`, configured in security Spring contexts: `RemoteUserSecurityContext` (REMOTE_USER), `CasAssertionSecurityContext` (CAS), `SimpleLdapSecurityContext` (LDAP bind), `TrustSecurityContext` (pre-auth).

**Import/Export.** `JaxbPortalDataHandlerService` batch-imports/exports portal data. Each type has `IDataImporter`, `IDataExporter`, `IDataUpgrader` (XSLT migration), `IDataTemplatingStrategy` (SpEL substitution). JAXB classes generated from XSDs in `uPortal-io-jaxb`.

**Soffit.** REST alternative to JSR-286 portlets: external web apps (can be Spring Boot) receive JWT payloads via `SoffitConnectorController` with user info, prefs, and layout context, and render views with portal-injected data.

**Spring config.** Mixed XML (`uPortal-webapp/src/main/resources/properties/contexts/` — datasource, persistence, groups, LDAP, portlets) and Java `@Configuration` (`RenderingPipelineConfiguration`, `PersonDirectoryConfiguration`, `GroupServiceConfiguration`, `SoffitConnectorConfiguration`). `RenderingPipelineBranchPoint`, `IPortalDataType`, and `IComponentGroupService` beans are auto-discovered.

**Coupling.** `uPortal-security-authn` is the most coupled module (23 subprojects). Hubs: `uPortal-core`, `uPortal-rendering`, `uPortal-layout-impl`. Utility modules (`uPortal-utils-*`, group providers) are loosely coupled.

Key config files: `portal.properties` (overridable via `${portal.home}/uPortal.properties`), `rdbm.properties` (JDBC), `hibernate.properties`, `gradle.properties` (versions).

## Code style

- **Java:** AOSP via google-java-format 1.7 (4-space indent, same-line braces). Package root `org.apereo.portal`. Every file needs the Apereo Apache 2.0 license header. `./gradlew verGJF` / `goJF`.
- **Tests:** JUnit 4 + Mockito — `@Before`/`@Test`, `@Mock`/`@InjectMocks`, `MockitoAnnotations.initMocks(this)`. Static-import `org.junit.Assert.*` and `org.mockito.Mockito.*`.
- **JavaScript:** Prettier (single quotes, 4-space indent, ES5 trailing commas). Globals: `jQuery`, `$`, `_`, `up`, `fluid`.
- **LESS:** 2-space indent, Prettier via stylelint-prettier.
- **Gradle:** all versions in `gradle.properties` as named vars — never hardcode in `build.gradle`.
- **Git:** branch `GH-{issue}` off `master`. [Conventional Commits](https://www.conventionalcommits.org/): `feat(security): add LDAP group caching`. Subject imperative, lowercase, no period, ≤72 chars. Reference the issue in the footer (`Closes GH-42` / `Refs GH-42`). Breaking changes: `!` after type or `BREAKING CHANGE:` footer.

## Danger zone

**XSLT rendering pipeline (`uPortal-rendering/`)** is complex, stateful, and poorly documented; changes can silently break rendering for all users. Do NOT modify XSL files without first reading the full transformation chain and having a reproducible test that proves the change. If unsure how a stage works, STOP and ask. Prefer adding a new template/mode over editing existing match patterns.

**Banned patterns.** Source compiles under Java 11, so Java 9–11 features (`var`, `List.of()`, `Map.of()`, `Optional.isEmpty()`, `String.isBlank()`) are fine. The ban line is Java 12+:

| Pattern | Why | Instead |
|---------|-----|---------|
| Switch expressions (`->`) | Java 14+ | classic `switch` |
| Text blocks (`"""`) | Java 15+ | concatenated literals |
| Records | Java 16+ | regular classes |
| Pattern matching `instanceof` | Java 16+ | `instanceof` + cast |
| Sealed classes | Java 17+ | regular classes |
| Pattern matching `switch` | Java 21+ | classic `switch` |
| `@BeforeEach`, `@DisplayName` | JUnit 5 | `@Before`, `@Test` (JUnit 4) |
| Inline versions in build.gradle | breaks version mgmt | add to `gradle.properties` |
| `commons-logging` imports | banned transitive | SLF4J (`org.slf4j.Logger`) |

## Before declaring any task complete

```
[ ] Every changed line traces to the stated task
[ ] Tests exist (or explained why not) and pass: ./gradlew :module:test
[ ] Java formatting passes: ./gradlew verGJF
[ ] No Java 12+ features or APIs; no inline dependency versions
[ ] License header on new files; no secrets or hardcoded hostnames
```

**Always stop and ask if:** the task is ambiguous, you'd need to modify XSLT in the rendering pipeline, you're unsure how existing code works after reading it, a change spans more than one module, or you can't write a test to verify it.
