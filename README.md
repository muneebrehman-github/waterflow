# SaafPaniGharGhar (WaterFlow SaaS)

Multi-tenant water filtration plant / delivery SaaS. Two sub-projects:

| Folder | Stack | Who uses it |
|---|---|---|
| [`backend/`](backend) | Java 17, Spring Boot 3.x, MariaDB, Redis, Thymeleaf | REST API + Super Admin portal |
| [`mobile-app/`](mobile-app) | Kotlin, Jetpack Compose, Hilt, Retrofit | Shop Owner, Staff, Delivery Boy, Customer |

Built feature-by-feature. This first slice covers **Auth + Shop registration &
Super Admin approval**, end to end across backend and mobile. See the SRS at
`D:\Personal Data\FTS\Projects\Water Filter Saas\WaterFlow_SRS_v1.0.0_DRAFT.docx`
for the full requirements this is being built against.

## Prerequisites

- **JDK 17** (already installed at `C:\Program Files\Java\jdk-17` on this machine)
- **Maven** (already installed)
- **Docker Desktop** (already installed) — runs MariaDB + Redis + backend locally
- **Android Studio + Android SDK** (already installed) — for `mobile-app/`,
  and/or Docker (see that folder's section below for why the build is
  containerized on this machine).

## Running the backend

```bash
cd backend
docker compose up -d --build   # starts MariaDB (33061), Redis (63791), and the backend (8081)
```

This runs the backend **inside a container** (via the `Dockerfile`), not just its
dependencies — on some Windows machines, corporate VPN/EDR software blocks a local
AF_UNIX loopback socket that the JVM's NIO selector needs, so a plain `java -jar` or
`mvn spring-boot:run` fails with `Unable to establish loopback connection` even
though nothing is wrong with the code. Running inside the container sidesteps that
entirely. If your machine doesn't hit that issue, `mvn spring-boot:run` (after
`docker compose up -d mariadb redis`) works fine too — just faster iteration
since there's no image rebuild step.

- API base URL: `http://localhost:8081/api/v1`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Super Admin portal: `http://localhost:8081/admin/login`
- A default Super Admin is auto-created on first boot:
  `admin@saafpanigharghar.local` / `ChangeMe123!` (override via
  `SUPER_ADMIN_EMAIL` / `SUPER_ADMIN_PASSWORD` env vars — do this for anything
  beyond local dev).

### Super Admin portal (Thymeleaf)

Served by the same backend process (session form-login, separate from JWT API auth):

- Login: `http://localhost:8081/admin/login`
- Shops: `http://localhost:8081/admin/shops`

Use the Super Admin credentials above to approve/reject/suspend/reinstate shops.

Quick API smoke test via Swagger or curl:
1. `POST /api/v1/shops/register` — register a shop (creates a PENDING owner + PENDING_APPROVAL shop).
2. Approve it in the Super Admin portal (or via `POST /api/v1/admin/shops/{id}/approve` with a Super Admin JWT).
3. `POST /api/v1/auth/login` as the shop owner — now succeeds.

## Running the mobile app (Shop Owner, Staff, Delivery Boy, Customer)

Native Android — Kotlin, Jetpack Compose, Hilt (DI), Retrofit + OkHttp
(with a `TokenAuthenticator` that silently refreshes on 401), encrypted
session storage (`androidx.security.crypto`), Navigation Compose for
role-based routing. This was originally planned as Flutter, but switched to
native Kotlin partway through (see conversation history) since Android
Studio + the Android SDK were already installed and working on this
machine, while Flutter's SDK would've been one more untested install.

**In Android Studio**: just open `mobile-app/` — it's a normal Gradle
project (`local.properties` points at `D:\sdk`, adjust if yours is
elsewhere). Run/debug the `app` module normally.

**From the command line**: this machine hit the same host-level Java NIO
bug described above (a bare `Selector.open()` fails), which breaks Gradle's
own daemon the same way it broke Tomcat — confirmed across three different
JDKs (Temurin 17/21/25, and Android Studio's bundled JBR 21), so it's not a
JDK choice issue. The fix is the same: build inside a container, this time
with its own **Linux**-native Android SDK — the Windows SDK's `build-tools`
binaries (`aapt2.exe`, `d8.bat`, ...) can't execute inside a Linux container,
so `Dockerfile.build` installs a fresh Linux SDK rather than mounting the
host's:

```bash
cd mobile-app
docker build -f Dockerfile.build -t spgg-android-build .
docker run --rm \
  -v "$(pwd):/workspace" \
  -v spgg_gradle_cache:/home/gradle/.gradle \
  -w /workspace \
  spgg-android-build \
  sh -c "./gradlew assembleDebug --no-daemon"
```

APK lands at `app/build/outputs/apk/debug/app-debug.apk`. On Git Bash /
MSYS, prefix with `export MSYS_NO_PATHCONV=1` first — otherwise it silently
mangles the container-internal `/workspace` path into a Windows path.

`gradle.properties`' `org.gradle.jvmargs` is tuned tight (`-Xmx1280m`, no
Metaspace cap, `org.gradle.parallel=false`, `kotlin.compiler.execution.strategy=in-process`)
because this machine's Docker Desktop WSL2 VM is capped at ~1.9GB total
regardless of `.wslconfig` (looks like a nested-virtualization ceiling on
this sandboxed host) — loosen these if you're building somewhere with more
headroom.

Install with `adb install -r app-debug.apk`. Default API base URL is
`http://192.168.1.3:8081/api/v1` — the backend machine's LAN IP, so a
physical device on the same network can reach it directly. Override at
build time with `-PapiBaseUrl=http://your-host:8081/api/v1` if your backend
runs elsewhere (an Android *emulator* specifically reaches the host's
`localhost` via `10.0.2.2` instead of a LAN IP).

Log in with a shop owner account created via the registration flow above
(after Super Admin approval) — Super Admin accounts are rejected by this app
on purpose; that role only uses the backend Super Admin portal.

## Architecture notes

- **Multi-tenancy**: shared MariaDB database, `tenant_id` column per tenant-owned
  table, enforced via a Hibernate `@Filter` enabled per-request
  (`TenantFilter` + `TenantContext`) based on the authenticated JWT's tenant claim.
- **A Shop *is* a tenant** — there's no separate `Tenant` table.
- **Auth**: JWT access token (15 min) + refresh token (7 days, tracked in Redis).
- Full architecture rationale: see the plan this slice was built from
  (multi-tenancy strategy, package layout, roadmap for the next feature slices).

## Roadmap

Next slices, in order: Product & bottle categories → Customer registration/addresses
→ Order placement & status workflow → Customer ledger → Manual payments → Bottle
deposit/balance tracking → Delivery assignment & status → FCM push notifications.
Phase 2/3 (subscriptions, inventory, expenses, reports, ratings, announcements,
full audit logging, staff permission matrix, platform billing, payment gateways,
iOS, GPS tracking, ...) come after.
