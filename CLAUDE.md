# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Apereo uPortal is an enterprise portal framework for higher education, built on the JSR-286 Portlet specification. It provides a customizable portal where users see a personalized layout of portlets. The project is maintained by the Apereo Foundation under the Apache 2.0 license.

**Important**: uPortal is typically deployed via [uPortal-start](https://github.com/uPortal-Project/uPortal-start), which manages the servlet container, database, and configuration. This repository builds the core framework libraries and WAR.

## Build Commands

**Before building**, load the correct Java version (Java 8) via sdkman:
```bash
sdk env
```

```bash
# Full build with tests and coverage
./gradlew build jacocoAggregateReport

# Build without tests
./gradlew build -x test

# Run all tests
./gradlew check

# Run tests for a specific module
./gradlew :uPortal-core:test
./gradlew :uPortal-rendering:test

# Install to local Maven repository (for uPortal-start consumption)
./gradlew install

# Java code formatting (AOSP style via Google Java Format)
./gradlew verGJF          # Verify formatting
./gradlew goJF            # Apply formatting

# JavaScript/CSS
./gradlew :npm_run_lint-js       # Lint JS
./gradlew npm_run_format-js      # Format JS
./gradlew npm_run_compile-less   # Compile LESS to CSS
```

CI runs: `./gradlew -S --no-daemon --no-parallel build jacocoAggregateReport coveralls`

## Tech Stack & Requirements

- **Java 8** (source compatibility 1.8, tested on Java 8 and 11)
- **Gradle** build system (use included `./gradlew` wrapper)
- **Spring Framework 4.3.30** with Spring Security 4.2.20 and Spring Webflow 2.4.8
- **Hibernate 4.2.21** for persistence
- **Apache Pluto 2.1.0-M3** as the JSR-286 portlet container
- **HSQLDB** as default dev database
- **Node.js 20.15.1** (automatically downloaded by Gradle for frontend tasks)
- **Lombok 1.18.36** for annotation processing

## Code Style

- **Java**: AOSP style enforced by Google Java Format (`./gradlew verGJF` / `./gradlew goJF`)
- **JavaScript**: ESLint + Prettier (single quotes, 4-space indent, ES5 trailing commas)
- **CSS/LESS**: Stylelint with Prettier
- **Static analysis**: SpotBugs, ErrorProne, Nebula Lint (critical rules for dependency conflicts)

## Architecture

### Module Organization (~40 submodules)

The project is a multi-module Gradle build. Key module groups:

- **uPortal-core** — Core interfaces: `IPerson`, `IUserLayoutManager`, `IPortletDefinition`
- **uPortal-webapp** — Main WAR assembly, Spring context configuration, web.xml
- **uPortal-rendering** — Rendering pipeline (the central request processing mechanism)
- **uPortal-layout** (core + impl) — Distributed Layout Management (DLM) system
- **uPortal-security** (authn, core, mvc, permissions, services, xslt, filters) — Authentication & authorization
- **uPortal-groups** (core, local, ldap, filesystem, pags, grouper, smartldap) — Pluggable group/membership providers
- **uPortal-io** (core, jaxb, types) — Import/export of portal data (users, portlets, layouts, permissions)
- **uPortal-soffit** (core, connector, renderer) — REST-based alternative to JSR-286 portlets
- **uPortal-api** (internal, rest, search) — REST API endpoints
- **uPortal-persondir** — Person Directory integration for user attribute resolution
- **uPortal-events** — Portal event system
- **uPortal-session** — Optional Spring Session + Redis clustering

### Rendering Pipeline (Decorator Pattern)

The core request flow is a composable pipeline of `CharacterPipelineComponent` stages defined in `RenderingPipelineConfiguration.java`:

```
HTTP Request → IPortalRenderingPipeline → RenderingPipelineBranchPoint[] → DynamicRenderingPipeline
```

Pipeline stages (bottom-to-top execution):
1. **UserLayoutStoreComponent** — Loads user's layout XML from database
2. **StructureAttributeIncorporationComponent** — Merges user preferences
3. **StructureTransformComponent** — XSLT transform of layout structure
4. **StructureCachingComponent** — Caches transformed structure
5. **PortletRenderingInitiationComponent** — Spawns async portlet worker threads
6. **ThemeAttributeIncorporationComponent** — Adds theme data
7. **ThemeTransformComponent** — XSLT transform to HTML
8. **StaxSerializingComponent** — SAX to character stream
9. **PortletRenderingIncorporationComponent** — Embeds portlet output into page

Adopters extend the pipeline via `RenderingPipelineBranchPoint` beans for conditional routing (e.g., redirect to CAS, alternate UIs).

### Distributed Layout Management (DLM)

Users' portal layouts are composed from a **base layout** merged with **layout fragments** owned by admin accounts. `DistributedLayoutManager` orchestrates this:

- Fragments are portal user accounts that own reusable layout templates
- Evaluators (group membership, person attributes, profile) determine which fragments apply to each user
- The merged layout is an XML document of folders and channels (portlet references)

### Authentication

Pluggable via `ISecurityContext` implementations:
- `RemoteUserSecurityContext` — HTTP REMOTE_USER from servlet container/reverse proxy
- `CasAssertionSecurityContext` — CAS protocol
- `SimpleLdapSecurityContext` — Direct LDAP bind
- `TrustSecurityContext` — Pre-authenticated

Each has a corresponding `ContextFactory`. Configuration is in the security Spring contexts.

### Import/Export System

`JaxbPortalDataHandlerService` handles batch import/export of portal data. Each data type (users, portlets, layouts, groups, permissions, etc.) has:
- `IDataImporter` — XML to domain object
- `IDataExporter` — Domain object to XML
- `IDataUpgrader` — XSLT-based schema migration
- `IDataTemplatingStrategy` — SpEL parameter substitution in XML files

JAXB classes are generated from XSD schemas in `uPortal-io-jaxb`.

### Soffit Framework

REST-based alternative to JSR-286 portlet development. External web apps receive JWT-encoded payloads via `SoffitConnectorController` containing user info, preferences, and layout context. Soffits can be Spring Boot apps that render views with portal-injected data.

### Spring Configuration

Mixed XML and Java @Configuration:
- XML contexts in `uPortal-webapp/src/main/resources/properties/contexts/` (datasource, persistence, groups, LDAP, portlet configs)
- Java configs: `RenderingPipelineConfiguration`, `PersonDirectoryConfiguration`, `GroupServiceConfiguration`, `SoffitConnectorConfiguration`
- Convention-based discovery: `RenderingPipelineBranchPoint`, `IPortalDataType`, and `IComponentGroupService` beans are auto-discovered

### Key Configuration Files

- `uPortal-webapp/src/main/resources/properties/portal.properties` — Main portal config (overridable via `${portal.home}/uPortal.properties`)
- `uPortal-webapp/src/main/resources/properties/rdbm.properties` — Database/JDBC config
- `uPortal-webapp/src/main/resources/hibernate.properties` — Hibernate/JPA settings
- `gradle.properties` — 80+ dependency version declarations

### Dependency Coupling

`uPortal-security-authn` is the most highly coupled module (depends on 23 subprojects). Core hub modules are `uPortal-core`, `uPortal-rendering`, and `uPortal-layout-impl`. Utility modules (`uPortal-utils-*`, group providers) are loosely coupled and independently usable.

## Contributing Conventions

- Individual Contributor License Agreement (Apereo) required
- Topic branches named with issue numbers (e.g., `GH-42`)
- Commit messages should reference GitHub issue numbers
- Run `./gradlew check` before submitting PRs
- Tests required for bug fixes and new features (JUnit 4, Spock, Mockito, EasyMock)
