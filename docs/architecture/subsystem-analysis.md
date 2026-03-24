# uPortal Subsystem Analysis for Modernization

**Date:** 2026-02-14
**Purpose:** Identify logical subsystems for modularization targeting uPortal 6 (modernization) and uPortal 7 (portlet removal, cloud deployment).
**Status:** Draft - Team Review

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current Module Landscape](#current-module-landscape)
3. [Proposed Logical Subsystems](#proposed-logical-subsystems)
4. [Cross-Cutting Concerns That Block Clean Separation](#cross-cutting-concerns)
5. [Frontend and JSP Analysis](#frontend-and-jsp-analysis)
6. [Persistence Architecture](#persistence-architecture)
7. [Portlet Removal Blast Radius](#portlet-removal-blast-radius)
8. [Recommended Subsystem Boundaries](#recommended-subsystem-boundaries)
9. [Recommended First Moves (uPortal 6)](#recommended-first-moves)
10. [Appendix: Module Dependency Matrix](#appendix-module-dependency-matrix)

---

## Executive Summary

uPortal's 39 Gradle submodules cluster into **10 logical subsystems**, but cross-cutting
dependencies prevent clean separation today. The biggest blockers are:

- **`uPortal-utils-core`** aggregates 80+ external dependencies and is imported by nearly
  every module. It must be decomposed first.
- **Portlet APIs** are embedded in ~180 files (~21,000 LOC). However, key subsystems
  (layout, groups, security, session) are already surprisingly clean.
- **The rendering pipeline** is architecturally bound to Apache Pluto (JSR-286), but only 4
  of its 14 primary stages are portlet-specific.
- **Persistence** is well-centralized with 3 datasources and a clean DAO hierarchy, but
  legacy raw JDBC in the layout store and user identity store needs modernization.
- **The frontend** relies on Fluid Infusion 1.5.0 (unmaintained), jQuery 1.12.4,
  Bootstrap 3, and 111 JSPs. This is the most urgent modernization target alongside the
  Spring upgrade.

Java 9+ module boundaries are achievable if we address these blockers incrementally.

---

## Current Module Landscape

The codebase has **39 submodules** organized in a multi-module Gradle build:

| Group | Modules | Purpose |
|-------|---------|---------|
| **Core** | `core`, `concurrency`, `rdbm`, `spring` | Foundation interfaces and infrastructure |
| **Groups** (7) | `groups-core`, `-local`, `-ldap`, `-filesystem`, `-pags`, `-grouper`, `-smartldap` | Pluggable group/membership providers |
| **Security** (7) | `security-core`, `-authn`, `-mvc`, `-permissions`, `-services`, `-xslt`, `-filters` | Authentication and authorization |
| **Layout** (2) | `layout-core`, `layout-impl` | Distributed Layout Management (DLM) |
| **Rendering** (1) | `rendering` | Request processing pipeline |
| **Content** (2) | `content-portlet`, `content-publishing` | Portlet definitions and publishing |
| **IO** (3) | `io-core`, `io-jaxb`, `io-types` | Import/export of portal data |
| **API** (3) | `api-internal`, `api-rest`, `api-search` | REST and internal APIs |
| **Soffit** (3) | `soffit-core`, `soffit-connector`, `soffit-renderer` | REST-based portlet alternative |
| **Events** (1) | `events` | Event capture and analytics aggregation |
| **Utils** (3) | `utils-core`, `utils-url`, `utils-jmx` | Shared utilities |
| **Other** | `webapp`, `portlets`, `marketplace`, `persondir`, `session`, `i18n`, `tenants`, `index`, `health`, `tools`, `url`, `web`, `hibernate-dialects` | Various |

### Coupling Hubs (Most Depended-On Modules)

| Module | Depended on by | Role |
|--------|---------------|------|
| `uPortal-core` | 8+ modules | Foundation interfaces (`IPerson`, `IBasicEntity`, `EntityIdentifier`) |
| `uPortal-security-core` | 7 modules | Security hub |
| `uPortal-groups-core` | 6 modules | Group membership SPI |
| `uPortal-layout-core` | 5 modules | Layout interfaces |
| `uPortal-utils-core` | Nearly all | Utility aggregator (PROBLEM - see below) |
| `uPortal-rendering` | 4 modules | Pipeline hub |

### Most Coupled Outbound (High Out-Degree)

| Module | Depends on | Notes |
|--------|-----------|-------|
| `uPortal-webapp` | 10+ modules | WAR assembly - expected |
| `uPortal-security-authn` | 23 subprojects (transitive) | Most coupled module in codebase |
| `uPortal-api-internal` | 6 modules | Internal admin APIs |
| `uPortal-api-rest` | 4 modules | REST endpoints |

---

## Proposed Logical Subsystems

### 1. Core Platform

**Modules:** `core`, `concurrency`, `rdbm`, `spring`

The irreducible nucleus. `uPortal-core` defines key interfaces (`IPerson`, `IBasicEntity`,
`EntityIdentifier`) that nearly everything depends on.

**Key issue:** `IPortletWindow extends Pluto PortletWindow` lives in core, coupling the
foundation to portlet APIs. This must be extracted for uPortal 7.

### 2. Groups

**Modules:** `groups-core`, `groups-local`, `groups-ldap`, `groups-filesystem`, `groups-pags`,
`groups-grouper`, `groups-smartldap`

Already well-structured with a clean SPI pattern. Each provider depends only on `groups-core`.

**Extraction feasibility:** Moderate. Two blockers:
- `GroupService` singleton (static factory) used throughout instead of DI (129 files import
  from `org.apereo.portal.groups`)
- `groups-pags` breaks the pattern by depending on `layout-core`, `security-core`, and `url`

**Zero portlet imports** in the entire groups subsystem.

### 3. Security and Permissions

**Modules:** `security-core`, `security-authn`, `security-mvc`, `security-permissions`,
`security-services`, `security-xslt`, `security-filters`

`security-core` is a hub depended on by 7 modules. The authentication chain
(`ISecurityContext` implementations: CAS, LDAP, RemoteUser, Trust) is already pluggable.

Can't easily be extracted but can be better layered with a thin API vs. thick implementation
split. Only 2 `javax.portlet` imports in the entire security subsystem.

### 4. Layout (DLM)

**Modules:** `layout-core`, `layout-impl`

**Surprisingly clean.** Only 1 `javax.portlet` import in the entire layout subsystem. The
Distributed Layout Management system is fundamentally about merging XML layout trees -
it's portlet-agnostic.

Strong extraction candidate. Main coupling issue: `layout-impl` has a bidirectional
dependency with `rendering` through parameter processors.

### 5. Rendering Pipeline

**Module:** `rendering`

Core request processing using a decorator/pipeline pattern. The configuration class
(`RenderingPipelineConfiguration.java`) defines 20 total component beans, including
logging and caching wrappers. The **14 primary processing stages** break down as follows --
**4 are portlet-specific**, the rest are generic:

| Stage | Portlet-Specific? |
|-------|------------------|
| UserLayoutStoreComponent | No |
| DashboardWindowStateSettingsStAXComponent | **Yes (portlet window states)** |
| **PortletWindowAttributeIncorporationComponent** | **Yes** |
| StructureAttributeIncorporationComponent | No |
| StructureTransformComponent (XSLT) | No |
| StructureCachingComponent | No |
| **PortletRenderingInitiationStAXComponent** | **Yes - spawns portlet workers** |
| ThemeAttributeIncorporationComponent | No |
| ThemeTransformComponent (XSLT) | No |
| StaxSerializingComponent | No |
| ThemeCachingComponent | No |
| PortletRenderingInitiationCharacterComponent | **Yes - character phase** |
| **PortletRenderingIncorporationComponent** | **Yes - embeds portlet output** |
| AnalyticsIncorporationComponent | No |

Additionally, 6 logging/instrumentation components (LoggingStAXComponent,
LoggingCharacterComponent) wrap the primary stages for observability.

The module overall is deeply entangled: 58 files import `javax.portlet`, 44 import
`org.apache.pluto`. Contains the Pluto container wrapper, portlet execution thread pool, and
JSR-286 lifecycle management.

### 6. Content and Portlet Model

**Modules:** `content-portlet`, `content-publishing`, `portlets`, `marketplace`

The portlet definition/entity/preference data model. `IPortletDefinition`,
`IPortletEntity`, and `IPortletWindow` are referenced in 200+ files. For uPortal 7, these
need to become generic abstractions (`IComponentDefinition`, `IComponentInstance`, etc.).

### 7. Events and Analytics

**Module:** `events`

Self-contained event pipeline with its own separate datasources:

```
Portal Request --> Event Created (PortalEvent subclass)
  --> RequestScopedEventsTracker (collects in request)
  --> RawEventsDb (high-throughput write storage)
  --> PortalEventProcessingManagerImpl (batch aggregation)
  --> AggrEventsDb (time-series analytics)
```

Good extraction candidate. Already uses separate databases for scaling independence.

### 8. Soffit Framework

**Modules:** `soffit-core`, `soffit-connector`, `soffit-renderer`

REST-based alternative to JSR-286 portlets. `soffit-core` and `soffit-renderer` are
**already standalone** with zero internal dependencies. `soffit-core` is a pure JWT utility
library.

This is the blueprint for the post-portlet architecture.

### 9. IO/Import-Export

**Modules:** `io-core`, `io-jaxb`, `io-types`

Batch import/export of portal data with XSLT-based schema migration. `io-jaxb` is
standalone (pure JAXB generation). Important for deployment tooling but not a runtime
dependency.

### 10. Person Directory

**Module:** `persondir`

User attribute resolution with directory integration (LDAP, local DB, institutional
sources). Could be extracted with proper interfaces, but has embedded knowledge of uPortal
attribute requirements.

---

## Cross-Cutting Concerns

These are the key blockers to clean subsystem separation:

| Concern | Impact | Details |
|---------|--------|---------|
| **`uPortal-utils-core` mega-module** | CRITICAL | Aggregates 80+ deps (Spring, Hibernate, EHCache, Guava, Lucene, JGroups, AspectJ, etc.). Depended on by nearly everything. Must decompose first. |
| **`GroupService` singleton** | HIGH | Static factory used instead of DI throughout. Blocks groups extraction. |
| **Portlet object model in `core`** | HIGH | `IPortletWindow extends Pluto PortletWindow` - Pluto in the core interfaces. |
| **`security-core` hub** | MEDIUM | 7 modules depend on it. Needs thin API vs. thick impl split. |
| **Bidirectional layout/rendering** | MEDIUM | `layout-impl` depends on `rendering` AND vice versa. |
| **Spring XML configuration** | MEDIUM | ~70 XML context files in `webapp` wire everything together. |
| **Legacy raw JDBC** | MEDIUM | `RDBMDistributedLayoutStore` and `RDBMUserIdentityStore` bypass ORM. |

---

## Frontend and JSP Analysis

### Current State

The frontend is the most urgent modernization target. Key findings:

| Asset Type | Count | Location |
|-----------|-------|----------|
| JSP files | 111 | `uPortal-webapp/src/main/webapp/` |
| JavaScript files | 29 | `uPortal-webapp/.../media/skins/common/javascript/` and others |
| LESS files | 27 | `uPortal-webapp/.../media/skins/respondr/common/less/` |
| Image files | 157 | Various skin directories |

### JavaScript Library Inventory

**Primary framework: Fluid Infusion 1.5.0** (unmaintained)

| Library | Version | Status | Usage |
|---------|---------|--------|-------|
| **Fluid Infusion** | 1.5.0 | Unmaintained | Component architecture, data binding, rendering |
| **jQuery** | 1.12.4 | EOL | DOM manipulation, AJAX throughout |
| **jQuery Migrate** | 1.4.1 | EOL | Legacy compat shim |
| **jQuery UI** | 1.10.3 | Outdated | Draggable, droppable, autocomplete, dialog |
| **Bootstrap** | 3.4.1 (WebJar) / 3.3.5 (ResourceServingWebapp) | EOL | UI components, responsive grid |
| **Backbone.js** | 1.3.3 | Unmaintained | Optional MVC (light usage) |
| **Underscore.js** | 1.8.3 | Outdated | Utility functions (Backbone dependency) |
| **Modernizr** | 2.6.2 | Outdated | Feature detection |
| **jsTree** | 3.3.17 | Active | Tree widget (entity selector) |
| **DataTables** | 1.9.4 | Outdated | Table management |
| **Noty** | 2.2.0 | Outdated | Notifications |
| **Font Awesome** | 4.7.0 | Outdated (v6 current) | Icons |

**ES6+ Polyfills already present (WebJars):**
- `core-js-bundle` 3.40.0
- `regenerator-runtime` 0.14.1
- `whatwg-fetch` 3.6.20
- `webcomponents-js` 2.8.0

### JSP Breakdown

The 111 JSPs are organized across 26 Spring Web Flow types:

- **Admin flows** (permissions, cache, fragments, users): ~20 JSPs
- **Portlet management** (edit, publish, configure): ~10 JSPs
- **User account flows** (password, login, profile): ~10 JSPs
- **Layout management** (reset, selection): ~6 JSPs
- **Entity/group selection**: ~5 JSPs
- **Error pages and miscellaneous**: ~10 JSPs

JSPs use JSTL taglibs, Spring Web Flow tags, and portlet taglibs (`<portlet:renderURL>`,
`<portlet:actionURL>`, `<portlet:namespace/>`). They render server-side with jQuery for
progressive enhancement.

### Frontend Build Pipeline

```
Node.js 20.15.1 (auto-downloaded by Gradle)
  |
  +-- LESS --> lessc 4.2.0 --> defaultSkin.css
  +-- JS linting --> ESLint 9.0.0 + Prettier 3.5.0
  +-- CSS linting --> Stylelint 16.0.0
  +-- Resource aggregation --> Jasig Resource Server (WebJars unpacking + minification)
```

### REST API Consumption in Frontend

Three patterns coexist:

```javascript
// 1. jQuery AJAX (most common - throughout layout/admin JS)
$.ajax({ url: '/uPortal/api/...', type: 'POST', dataType: 'json' });

// 2. jQuery shorthand
$.getJSON('/uPortal/rest/...', data);

// 3. Fetch API (newest code only - portal-analytics.js)
fetch('/uPortal/api/analytics', { method: 'POST', body: JSON.stringify(data) });
```

### Web Components

The `webcomponents-js` polyfill (v2.8.0) is included but **no custom elements are defined
in this codebase yet**. Web component infrastructure exists in the broader uPortal ecosystem
(separate repos).

### Frontend Modernization Assessment

**What should be eliminated:**
- Fluid Infusion (unmaintained, custom component model superseded by web standards)
- jQuery + jQuery UI (ES6 covers DOM APIs; modern CSS covers most UI patterns)
- jQuery Migrate (only needed for legacy jQuery)
- Backbone.js + Underscore.js (light usage, replaceable with ES6 modules)
- Modernizr (unnecessary for modern browser targets)
- Noty (replaceable with native notification patterns or lightweight alternative)

**What can be kept/upgraded:**
- Bootstrap (upgrade to 5.x, or consider lighter alternative)
- jsTree (still actively maintained, useful for tree widgets)
- DataTables (upgrade to current version if still needed)
- Font Awesome (upgrade to 6.x)
- ESLint/Prettier/Stylelint toolchain (already modern)

**Recommended direction:**
- ES6 modules as the component model (no framework dependency)
- Web Components for reusable UI elements (polyfill already present)
- Fetch API for all REST calls (polyfill already present)
- CSS Grid/Flexbox to replace jQuery UI layout
- Progressive JSP-to-template migration (consider replacing JSPs with a modern template
  engine like Thymeleaf, or moving admin UIs to standalone SPAs consuming REST APIs)
- Minimize library count: target ES6 + a CSS framework + icon library + a few focused
  utilities (tree widget, data table) rather than a monolithic JS framework

---

## Persistence Architecture

### Current State: Well-Centralized, Hybrid Approach

The persistence layer uses **3 separate datasources**, each with its own Hibernate
SessionFactory, JPA persistence unit, and transaction manager:

| Persistence Unit | Purpose | Tables | Scaling |
|-----------------|---------|--------|---------|
| **PortalDb** | Core portal data | ~30 tables | Shared, cached (EHCache L2) |
| **RawEventsDb** | Event logging | 2 tables | Write-heavy, append-only |
| **AggrEventsDb** | Analytics aggregation | ~15 tables | OLAP, independently cacheable |

All three can point to the same physical database (dev default) or separate databases
(production).

### DAO Hierarchy

A clean `BaseJpaDao` hierarchy provides:

```
BaseJpaDao (abstract, in uPortal-utils-core)
  - Criteria API helpers, query caching, natural ID loading
  - Zero business logic - pure JPA infrastructure
  |
  +-- BasePortalJpaDao      (uPortal-utils-core) --> @PortalTransactional, EntityManager(PortalDb)
  +-- BaseRawEventsJpaDao   (uPortal-rdbm)       --> @RawEventsTransactional, EntityManager(RawEventsDb)
  +-- BaseAggrEventsJpaDao  (uPortal-rdbm)       --> @AggrEventsTransactional, EntityManager(AggrEventsDb)
```

**38 @Repository classes** distributed by domain:

| Module | DAO Count | Key Tables |
|--------|-----------|------------|
| `events` | 15+ | Time dimensions, aggregations, event storage |
| `web` | 5 | Portlet definitions, types, entities, cookies, ratings |
| `layout-core` | 3 | Stylesheets, user preferences, profile selection |
| `groups-pags` | 3 | PAGS groups, tests, test groups |
| `persondir` | 1 | Local accounts |
| `security-permissions` | 1 | Permission owners |
| `i18n` | 1 | Messages |
| `tenants` | 1 | Tenants |
| `api-rest` | 1 | Portlet lists |
| `utils-core` | 1 | Cluster locks |
| `tools` | 1 | Versions |

### Persistence as a Shared Library, Not a Centralized Subsystem

**The question:** Should persistence be a core subsystem, or a shared utility library where
each subsystem manages its own schema?

**Answer: Shared utility library.** Here's why:

A centralized persistence subsystem would be counter to microservice architecture. Each
subsystem should own its data and schema. The good news is that uPortal's persistence layer
is already structured to support this:

**What works well for per-subsystem ownership:**

1. **No shared table ownership.** Each module exclusively owns its tables. No two modules
   write to the same table.

2. **Cross-module access is read-only.** Where modules need data from other modules, they
   read (never write) via DAO interfaces:

   | Reader | Reads From | Owner | Pattern |
   |--------|-----------|-------|---------|
   | Layout (DLM) | `UP_PORTLET_ENT` | Content | Via `IPortletEntityDao` |
   | Events aggregation | `UP_PORTLET_DEF` | Content | Via `IPortletDefinitionDao` |
   | Layout (DLM) | `UP_USER` | PersonDir | Via `IUserIdentityStore` |

3. **The `BaseJpaDao` hierarchy has zero business logic.** It provides Criteria API helpers,
   query caching, and transaction annotations. This is a textbook shared utility library.

4. **Events already use separate databases.** RawEventsDb and AggrEventsDb demonstrate the
   pattern of independent persistence units working alongside shared infrastructure.

**What needs refactoring for per-subsystem ownership:**

1. **Raw JDBC code crosses domain boundaries.**
   `RDBMDistributedLayoutStore` (the layout store) uses raw `PreparedStatement`/`ResultSet`
   to directly query portlet entity tables. This bypasses all abstraction layers:
   ```
   // Current: Raw SQL joins across domains
   SELECT ... FROM UP_LAYOUT_STRUCT ls
   JOIN UP_PORTLET_ENT pe ON ...   // <-- Crosses into portlet domain
   ```
   This must be refactored to use service-layer calls or cached lookups.

2. **Cross-persistence-unit transactions lack consistency guarantees.**
   Example: `JpaAggregatedPortletLookupDao` runs an `@AggrEventsTransactional` method
   that calls `IPortletDefinitionDao` (which runs `@PortalTransactional`). Two separate
   transactions, two EntityManagers, potentially two databases. If one succeeds and the
   other fails, data is inconsistent. For microservices, these need eventual consistency
   patterns (caching, async sync, event-driven updates).

3. **Schema migration tooling doesn't exist.** uPortal uses a custom `tables.xml` for
   schema definition with no Liquibase/Flyway. Each subsystem will need its own migration
   strategy.

**Recommended approach:**

```
Phase 1: Extract shared persistence library
  - Move BaseJpaDao hierarchy to `uPortal-persistence-commons`
  - Standardize transaction annotation patterns
  - No behavior change, just organization

Phase 2: Enforce domain boundaries
  - Replace raw JDBC cross-domain queries with service-layer calls
  - Convert RDBMDistributedLayoutStore to use DAO interfaces
  - Convert RDBMUserIdentityStore to JPA or Spring Data

Phase 3: Per-subsystem schema ownership
  - Each subsystem owns its Hibernate config and entity mappings
  - Adopt Liquibase/Flyway per subsystem
  - Cross-subsystem data access only via service APIs (REST or in-process)

Phase 4: Independent deployment (uPortal 7+)
  - Subsystems that are microservices get their own datasource config
  - Shared library provides connection pooling, caching, transaction boilerplate
  - Event-driven synchronization replaces cross-PU transactions
```

### Table Ownership by Subsystem

| Subsystem | Tables Owned | Can Separate DB? |
|-----------|-------------|-----------------|
| **Content/Portlet** | `UP_PORTLET_DEF`, `UP_PORTLET_ENT`, `UP_PORTLET_TYPE`, `UP_PORTLET_PREFS`, `UP_PORTLET_LIFECYCLE`, etc. (8) | Yes (API for lookups) |
| **Layout** | `UP_LAYOUT_STRUCT`, `UP_LAYOUT_PARAM`, `UP_SS_DESC`, `UP_SS_USER_PREF`, `UP_PROFILE_SELECTION`, etc. (8) | Partial (needs portlet/user refs) |
| **Events (Raw)** | `UP_RAW_EVENTS`, `UP_ANALYTICS_EVENTS` (2) | Yes (already separate PU) |
| **Events (Aggregated)** | `UP_DATE_DIMENSION`, `UP_LOGIN_EVENT_AGGR`, `UP_PORTLET_EXEC_AGGR`, etc. (15) | Yes (already separate PU) |
| **Identity/PersonDir** | `UP_USER`, `UP_USER_PROFILE`, `UP_USER_LOCALE`, `UP_PERSON_DIR`, `UP_PERSON_ATTR` (5) | Yes (API for lookups) |
| **Groups** | `UP_GROUP`, `UP_GROUP_MEMBERSHIP`, `UP_ENTITY_TYPE`, `UP_PAGS_*` (6) | Yes |
| **Permissions** | `UP_PERMISSION`, `UP_PERMISSION_OWNER`, `UP_PERMISSION_ACTIVITY` (3) | No (cross-cutting) |
| **i18n** | `UP_MESSAGE` (1) | Yes |
| **Tenants** | `UP_TENANT` (1) | Yes |
| **Infrastructure** | `UP_SEQUENCE`, `UP_MUTEX`, `UP_ENTITY_LOCK` (3) | No (shared) |

---

## Portlet Removal Blast Radius

For planning the uPortal 7 transition:

### Tier 1 - Major Rewrite Required

| Module | Files with javax.portlet | Key Entanglement |
|--------|------------------------|------------------|
| `rendering` | 58 | Pluto container wrapper, execution workers, JSR-286 lifecycle |
| `core` | 3 | `IPortletWindow extends Pluto PortletWindow` |
| Database | - | 10+ portlet-specific tables |

### Tier 2 - Significant Refactoring

| Module | Files | Key Entanglement |
|--------|-------|------------------|
| `portlets` | 43 | Built-in portlet applications |
| `web` | 26 | Portlet execution coordination, rendering |
| `webapp` | 0 (Java) / ~18 (XML config) | XML config files reference portlets |
| `url` | 7 | WindowState/PortletMode URL encoding |
| `events` | 8 | Portlet mode/state in event model |
| `api-rest` | 6 | Portlet preference REST controllers |
| `api-internal` | 7 | Portlet-based admin APIs |

### Tier 3 - Moderate Decoupling

| Module | Files | Notes |
|--------|-------|-------|
| `soffit-connector` | 7 | Bridge code, designed for separation |
| `utils-core` | 5 | Utility portlet helpers |
| `content-portlet` | 3 | Portlet data model |

### Tier 4 - Already Clean

| Module | Portlet Imports | Notes |
|--------|----------------|-------|
| `layout` | 1 | Layout system is portlet-agnostic |
| `security` | 2 | Minimal coupling |
| `groups` | 0 | Zero portlet coupling |
| `session` | 0 | Standalone |
| `persondir` | 0 | Clean |
| `i18n` | 0 | Clean |
| `soffit-core` | 0 | Standalone JWT library |
| `soffit-renderer` | 0 | Standalone client library |

### Total Portlet-Specific Code

```
Files with javax.portlet imports:    ~179
Files with org.apache.pluto imports:  ~61
Files with 'portlet' in path:        552 (33% of codebase)
Estimated portlet-specific LOC:   ~21,000
```

---

## Recommended Subsystem Boundaries

Target architecture for Java 9+ module system:

```
+-----------------------------------------------------------+
|                     uPortal Platform                      |
|  +-------------+  +--------------+  +------------------+  |
|  | Core API    |  | Persistence  |  | Security         |  |
|  | (thin)      |  | Commons      |  | (api + impl)     |  |
|  +-------------+  | (shared lib) |  +------------------+  |
|                    +--------------+                        |
+-----------------------------------------------------------+
|  +-------------+  +--------------+  +------------------+  |
|  | Groups      |  | Layout       |  | Person Directory |  |
|  | (SPI)       |  | (DLM)        |  |                  |  |
|  +-------------+  +--------------+  +------------------+  |
+-----------------------------------------------------------+
|  +-------------+  +--------------+  +------------------+  |
|  | Rendering   |  | Portlet      |  | Soffit           |  |
|  | (generic)   |  | (legacy)     |  | (modern)         |  |
|  +-------------+  +--------------+  +------------------+  |
+-----------------------------------------------------------+
|  +-------------+  +--------------+  +------------------+  |
|  | REST API    |  | Events       |  | IO/Export        |  |
|  +-------------+  +--------------+  +------------------+  |
+-----------------------------------------------------------+
```

Key design decisions:
- **Persistence Commons** is a shared utility library, not a subsystem. Each subsystem owns
  its schema and entities. The library provides `BaseJpaDao`, transaction annotations,
  connection pool management, and dialect handling.
- **Rendering** splits into generic pipeline stages and a portlet-specific adapter module.
- **Portlet** becomes an isolated legacy module that plugs into the rendering pipeline.
- **Soffit** expands as the primary content integration model going forward.

---

## Recommended First Moves

These are ordered by dependency -- each step unblocks the next.

### 1. Decompose `uPortal-utils-core`

Split the 80+ dependency mega-module into focused libraries:

| New Module | Contents |
|-----------|----------|
| `utils-cache` | EHCache management |
| `utils-spring` | Spring integration helpers |
| `utils-xml` | Woodstox, StAX utilities |
| `utils-cluster` | JGroups clustering |
| `utils-crypto` | Encryption utilities |

This unblocks all other modularization work.

### 2. Split `core` into `core-api` and `core-portlet`

Move portlet-specific interfaces (`IPortletWindow`, `IPortletDefinition`,
`IPortletEntity`) out of core-api. Create generic `IComponentDefinition` interfaces.
Keep portlet-specific ones in `core-portlet` for backward compatibility.

### 3. Extract rendering pipeline from portlet container

The 10 generic pipeline stages go to `rendering-core`. The 4 portlet-specific stages plus
Pluto integration move to `rendering-portlet`. This creates the extension point for
alternative content models.

### 4. Replace `GroupService` singleton with dependency injection

Inject `ICompositeGroupService` everywhere the static factory is used (129 call sites).
This unblocks groups extraction and is good practice regardless.

### 5. Extract persistence commons

Move `BaseJpaDao` hierarchy to `uPortal-persistence-commons`. Refactor
`RDBMDistributedLayoutStore` and `RDBMUserIdentityStore` to use DAO interfaces instead of
raw JDBC. Adopt Liquibase or Flyway for schema management.

### 6. Modernize frontend

- Replace Fluid Infusion with ES6 modules and Web Components
- Drop jQuery (ES6 covers DOM APIs; Fetch API for HTTP)
- Upgrade Bootstrap 3 to 5.x (or evaluate lighter alternatives)
- Migrate JSPs incrementally (admin UIs as standalone SPAs, or adopt Thymeleaf)
- Target: ES6 + CSS framework + icon library + focused utilities (tree widget, data table)

### 7. Expand Soffit as the bridge to uPortal 7

`soffit-core` is already the architecture we want post-portlets. Make the connector
work as a servlet filter (not just a portlet) so content integration doesn't require the
portlet container.

---

## Appendix: Module Dependency Matrix

### Groups Subsystem Dependencies

| Module | Depends On |
|--------|-----------|
| `groups-core` | `content-portlet`, `rdbm` |
| `groups-local` | `groups-core` |
| `groups-ldap` | `groups-core` |
| `groups-filesystem` | `groups-core` |
| `groups-pags` | `groups-core`, `layout-core`, `security-core`, `url` |
| `groups-grouper` | `groups-core` |
| `groups-smartldap` | `groups-core` |

### Security Subsystem Dependencies

| Module | Depends On |
|--------|-----------|
| `security-core` | `content-portlet`, `groups-core`, `i18n`, `soffit-core`, `utils-url` |
| `security-authn` | `persondir`, `security-core`, `spring` |
| `security-mvc` | `core`, `events` |
| `security-permissions` | `api-internal`, `content-publishing`, `io-jaxb`, `security-core`, `spring` |
| `security-services` | `events`, `security-core` |
| `security-xslt` | `security-core` |
| `security-filters` | `core`, `events` |

### Rendering and Layout Dependencies

| Module | Depends On |
|--------|-----------|
| `rendering` | `api-search`, `marketplace`, `security-core`, `url`, Pluto |
| `layout-core` | `io-core`, `i18n`, `utils-core` |
| `layout-impl` | `content-portlet`, `layout-core`, `rendering`, `security-core`, `spring` |

### Standalone Modules (Zero Internal Dependencies)

- `soffit-core` - JWT utility library
- `soffit-renderer` - Client library for external apps
- `session` - Spring Session + Redis
- `api-search` - Pure JAXB data models
- `core` - Foundation interfaces (depends only on Hibernate/Spring)

### Portlet API Usage by Module

Counts are of Java source files (src/main/java) containing the respective imports.

| Module | javax.portlet Files | org.apache.pluto Files |
|--------|-------------------|----------------------|
| `rendering` | 58 | 44 |
| `portlets` | 43 | 1 |
| `web` | 26 | 6 |
| `events` | 8 | 0 |
| `url` | 7 | 3 |
| `soffit-connector` | 7 | 0 |
| `api-internal` | 7 | 0 |
| `api-rest` | 6 | 0 |
| `utils-core` | 5 | 0 |
| `core` | 3 | 3 |
| `spring` | 3 | 0 |
| `content-portlet` | 3 | 3 |
| `security` (all) | 2 | 1 |
| `layout` (all) | 1 | 0 |
| `groups` (all) | 0 | 0 |
