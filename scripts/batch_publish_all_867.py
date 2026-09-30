#!/usr/bin/env python3
import subprocess
import time
import json
import sys
import os

REPO = "YeamimHossainSajid/uber-doma-architecture"
LOG_FILE = "/Users/sajid/Documents/Uber-doma/docs/issue_publishing.log"

def log(msg):
    timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
    entry = f"[{timestamp}] {msg}"
    print(entry, flush=True)
    with open(LOG_FILE, "a") as f:
        f.write(entry + "\n")
        f.flush()

# 40 Microservices
SERVICES = [
    # Domain 1: Mobility & Dispatch
    ("domain-mobility", "supply-locator-service", "Driver Supply & Geolocation Tracking", "domain:mobility"),
    ("domain-mobility", "demand-pin-service", "Rider Pickup Pin Ingestion & Demand Heatmaps", "domain:mobility"),
    ("domain-mobility", "dispatch-coordinator-service", "DISCO Algorithmic Matching Engine", "domain:mobility"),
    ("domain-mobility", "dynamic-surge-service", "Real-Time Dynamic Surge Pricing Multiplier", "domain:mobility"),
    ("domain-mobility", "routing-engine-service", "Gurafu Street Graph Navigation & Route Calculation", "domain:mobility"),
    ("domain-mobility", "deep-eta-service", "DeepETA Machine Learning Travel Predictor", "domain:mobility"),
    ("domain-mobility", "h3-spatial-index-service", "Uber H3 Hexagonal Spatial Index Partitioning", "domain:mobility"),
    ("domain-mobility", "pool-batching-service", "UberX Share / Pool Carpool Detour Optimizer", "domain:mobility"),

    # Domain 2: Trip Fulfillment & Lifecycle
    ("domain-trip", "trip-state-machine-service", "Trip Lifecycle State Machine Coordinator", "domain:trip"),
    ("domain-trip", "trip-reservation-service", "Uber Reserve Scheduled Booking Manager", "domain:trip"),
    ("domain-trip", "trip-cancellation-service", "Cancellation Policy & Fee Evaluation Engine", "domain:trip"),
    ("domain-trip", "safety-telematics-service", "RideCheck Sensor Crash & Speed Detection", "domain:trip"),
    ("domain-trip", "in-trip-chat-service", "End-to-End Masked VoIP & Messaging", "domain:trip"),
    ("domain-trip", "lost-and-found-service", "Post-Trip Left Belonging Resolution Engine", "domain:trip"),
    ("domain-trip", "toll-highway-surcharge-service", "Electronic Toll Detection & Highway Surcharge", "domain:trip"),

    # Domain 3: Billing, Payments & Fares
    ("domain-billing", "fare-quotation-service", "Upfront Guaranteed Fare Pricing Quotation", "domain:billing"),
    ("domain-billing", "payment-orchestrator-service", "Multi-PSP Payment Processing Gateway", "domain:billing"),
    ("domain-billing", "invoicing-ledger-service", "Double-Entry Bookkeeping Ledger Engine", "domain:billing"),
    ("domain-billing", "driver-instant-payout-service", "Driver Instant Pay & Weekly ACH Disbursement", "domain:billing"),
    ("domain-billing", "fare-split-service", "Multi-Rider Fare Splitting Coordinator", "domain:billing"),
    ("domain-billing", "promo-rider-discount-service", "Rider Promo Coupons & Seasonal Discounts", "domain:billing"),
    ("domain-billing", "tax-compliance-service", "Airport Surcharges & Municipal Tax Compliance", "domain:billing"),

    # Domain 4: Driver & Vehicle Asset
    ("domain-driver", "driver-onboarding-service", "Driver Registration, Identity KYC & MVR Check", "domain:driver"),
    ("domain-driver", "vehicle-registry-service", "Vehicle Make, Model, Class & Document Validation", "domain:driver"),
    ("domain-driver", "driver-compliance-service", "Hours of Service (HOS) Fatigue Monitor", "domain:driver"),
    ("domain-driver", "fleet-partner-service", "Fleet Owner Asset Sharing & Rentals", "domain:driver"),
    ("domain-driver", "driver-quest-incentives-service", "Driver Quest Bonus Milestones & Streak Incentives", "domain:driver"),
    ("domain-driver", "driver-earnings-analytics-service", "Hourly Wage Projections & Weekly Summaries", "domain:driver"),

    # Domain 5: Rider Identity & Trust
    ("domain-rider", "rider-profile-service", "Rider Accounts, Saved Places & Preferences", "domain:rider"),
    ("domain-rider", "auth-security-service", "OAuth2, JWT Token Minting & Biometric Auth", "domain:rider"),
    ("domain-rider", "mutual-rating-service", "Two-Way Mutual 5-Star Rating Aggregation", "domain:rider"),
    ("domain-rider", "uber-one-membership-service", "Uber One Subscription Perks & Cashback", "domain:rider"),
    ("domain-rider", "fraud-gps-spoofing-service", "Anti-Fraud ML Detector & GPS Spoofing Guard", "domain:rider"),
    ("domain-rider", "omnichannel-notifications-service", "Push Notifications (APNs/FCM) & SMS Alerts", "domain:rider"),

    # Domain 6: Core Platform Infrastructure
    ("domain-platform", "telemetry-gps-ingestion-service", "High-Frequency GPS Ping Ingestion Stream", "domain:platform"),
    ("domain-platform", "event-streaming-hub-service", "Kafka CDC Event Bus Bridge", "domain:platform"),
    ("domain-platform", "experimentation-flipr-service", "Dynamic Feature Flags & A/B XP Distribution", "domain:platform"),
    ("domain-platform", "audit-compliance-service", "Immutable Regulatory Audit Log & GDPR", "domain:platform"),
    ("domain-platform", "service-registry-discovery-service", "Dynamic gRPC Service Directory & Health Probes", "domain:platform"),
    ("domain-platform", "distributed-rate-limiter-service", "Redis Token-Bucket Rate Limiter & Quotas", "domain:platform"),
]

GATEWAYS = [
    ("edge-gateway", "Tier 1: Global Edge API Gateway", "architecture"),
    ("domain-mobility/mobility-domain-gateway", "Tier 2: Mobility Domain Gateway", "domain:mobility"),
    ("domain-trip/trip-domain-gateway", "Tier 2: Trip Fulfillment Gateway", "domain:trip"),
    ("domain-billing/billing-domain-gateway", "Tier 2: Billing & Fare Domain Gateway", "domain:billing"),
    ("domain-driver/driver-domain-gateway", "Tier 2: Driver & Vehicle Gateway", "domain:driver"),
    ("domain-rider/rider-domain-gateway", "Tier 2: Rider Identity Gateway", "domain:rider"),
]

# Task Templates per service (21 tasks per service = 840 issues, + 27 gateway/system issues = 867)
TASK_TEMPLATES = [
    # Tier 1: Beginner (Tasks 1-6)
    ("level:beginner,good-first-issue", "[Beginner] Implement Input Validation & DTO Constraints in {svc}",
     "Add Jakarta validation constraints (@NotNull, @DecimalMin, @Size) on incoming request DTOs in `{path}`.",
     "Validate all request payload parameters before processing to prevent invalid state.",
     "1. Open `{path}/src/main/java/.../dto`\n2. Add `@NotNull`, `@Min`, `@Size` annotations.\n3. Add `@RestControllerAdvice` to catch `MethodArgumentNotValidException`.\n4. Write a unit test verifying invalid payloads return 400 Bad Request.",
     "./mvnw -pl {path} test"),

    ("level:beginner,good-first-issue", "[Beginner] Configure Spring Boot Actuator Health Indicators in {svc}",
     "Implement custom Actuator health check indicator in `{path}`.",
     "Expose readiness and liveness probes for Kubernetes pod lifecycle management.",
     "1. Verify `spring-boot-starter-actuator` in pom.xml.\n2. Create a custom `HealthIndicator` bean.\n3. Check database and downstream dependency availability.\n4. Write integration test asserting `/actuator/health` returns status UP.",
     "./mvnw -pl {path} test"),

    ("level:beginner,good-first-issue", "[Beginner] Implement SLF4J Structured Logging & Context Propagation in {svc}",
     "Configure structured JSON logging with MDC trace context in `{path}`.",
     "Standardize log output for centralized aggregation in ELK / Loki with correlation IDs.",
     "1. Configure Logback JSON encoder in `src/main/resources/logback-spring.xml`.\n2. Inject `X-Trace-ID` and `X-Rider-ID` into SLF4J MDC.\n3. Verify log statements print JSON formatted strings.",
     "./mvnw -pl {path} test"),

    ("level:beginner,good-first-issue", "[Beginner] Write Unit Tests with Mockito for Service Layer in {svc}",
     "Develop comprehensive unit tests with Mockito for core service business logic in `{path}`.",
     "Ensure high code coverage (>85%) on domain service methods and exception handling.",
     "1. Create `src/test/java/.../{clazz}ServiceTest.java`.\n2. Mock repository and external clients with `@Mock`.\n3. Assert return values and verify interaction counts.\n4. Verify tests pass with `./mvnw test`.",
     "./mvnw -pl {path} test"),

    ("level:beginner,good-first-issue", "[Beginner] Document Protocol Buffer IDL Fields in {svc}",
     "Add detailed Javadoc and Proto docstrings to Protocol Buffer message definitions for `{path}`.",
     "Ensure all fields document units (meters, milliseconds, micro-USD) and nullability.",
     "1. Open `proto-contracts/src/main/proto`.\n2. Add comments explaining field ranges, semantics, and default behavior.\n3. Re-compile protobufs with `./mvnw compile`.",
     "./mvnw compile"),

    ("level:beginner,good-first-issue", "[Beginner] Implement Global Exception Translator for gRPC in {svc}",
     "Map standard Java domain exceptions to gRPC `StatusRuntimeException` codes in `{path}`.",
     "Translate `EntityNotFoundException` to `Status.NOT_FOUND` and `IllegalArgumentException` to `Status.INVALID_ARGUMENT`.",
     "1. Create `@GrpcAdvice` or global interceptor in `{path}`.\n2. Handle custom domain exceptions.\n3. Attach rich error details to metadata.",
     "./mvnw -pl {path} test"),

    # Tier 2: Moderate (Tasks 7-13)
    ("level:moderate", "[Moderate] Implement JPA Repositories & Liquibase Changelog in {svc}",
     "Create PostgreSQL entity mappings and Liquibase migration scripts for `{path}`.",
     "Ensure isolated schema tables, foreign key constraints, and performance indexes are managed via code.",
     "1. Create JPA `@Entity` classes extending `BaseEntity`.\n2. Add `db/changelog/db.changelog-master.xml` Liquibase script.\n3. Configure primary keys, unique constraints, and B-Tree indexes.\n4. Write Testcontainers PostgreSQL integration test.",
     "./mvnw -pl {path} test"),

    ("level:moderate", "[Moderate] Build Transactional Outbox Event Producer in {svc}",
     "Implement the Transactional Outbox pattern to publish domain events to Kafka reliably in `{path}`.",
     "Guarantee atomic database commits and message publication without distributed 2PC transactions.",
     "1. Inside the business transaction, write to entity table AND `outbox_events` table.\n2. Use `OutboxEventPublisher` to push to Kafka topic.\n3. Mark event published upon receiving Kafka ACK.\n4. Write test asserting event is saved in outbox table.",
     "./mvnw -pl {path} test"),

    ("level:moderate", "[Moderate] Implement Redis Cache-Aside Layer with TTL in {svc}",
     "Integrate Spring Data Redis cache-aside caching with automatic TTL in `{path}`.",
     "Reduce database query pressure by caching frequently accessed immutable domain models.",
     "1. Inject `DistributedCacheManager` into service layer.\n2. Check Redis cache before executing SQL queries.\n3. Set cache values with 5-minute TTL.\n4. Evict cache on entity update.",
     "./mvnw -pl {path} test"),

    ("level:moderate", "[Moderate] Implement gRPC Server BindableService Implementation in {svc}",
     "Implement the auto-generated gRPC stub server in `{path}` with non-blocking StreamObserver.",
     "Provide high-throughput binary IPC endpoint for Tier-2 Domain Gateway orchestration.",
     "1. Create class extending `{clazz}ServiceGrpc.{clazz}ServiceImplBase`.\n2. Annotate with `@GrpcService`.\n3. Implement business logic and invoke `responseObserver.onNext()`.\n4. Handle errors with `responseObserver.onError()`.",
     "./mvnw -pl {path} test"),

    ("level:moderate", "[Moderate] Build Kafka Dead Letter Queue (DLQ) & Retry Policy in {svc}",
     "Configure Kafka listener container factory with exponential backoff and DLQ routing in `{path}`.",
     "Prevent message processing loops on poison-pill messages by offloading to dead-letter topics.",
     "1. Configure `DefaultErrorHandler` with `ExponentialBackOff`.\n2. Route failed messages to `{svc}.DLQ` after 3 retries.\n3. Write test case injecting bad payload and verifying arrival in DLQ topic.",
     "./mvnw -pl {path} test"),

    ("level:moderate", "[Moderate] Implement Optimistic Locking with @Version in {svc}",
     "Add concurrency collision detection using JPA `@Version` column in `{path}`.",
     "Prevent race conditions when multiple concurrent threads attempt to update the same record.",
     "1. Add `@Version private Long version;` to entity.\n2. Handle `OptimisticLockingFailureException` in service layer with retry.\n3. Write multi-threaded integration test verifying optimistic lock rejection.",
     "./mvnw -pl {path} test"),

    ("level:moderate", "[Moderate] Implement Distributed Idempotency Key Validator in {svc}",
     "Use Redis `SETNX` to enforce idempotent request execution on duplicate submissions in `{path}`.",
     "Ensure network retries (e.g. charge payment, create booking) do not create duplicate transactions.",
     "1. Extract `Idempotency-Key` header from request.\n2. Execute `redis.setIfAbsent(key, status, 120s)`.\n3. If key exists, return cached previous response.\n4. Write unit test testing repeated requests with identical key.",
     "./mvnw -pl {path} test"),

    # Tier 3: Advanced (Tasks 14-19)
    ("level:advanced", "[Advanced] Implement Reactive Scatter-Gather Query Aggregation in {svc}",
     "Orchestrate parallel downstream queries using Project Reactor `Mono.zip` in `{path}`.",
     "Eliminate sequential network wait times by fanning out non-blocking calls concurrently.",
     "1. Wire reactive gRPC client stubs.\n2. Dispatch queries concurrently using `Mono.zip()`.\n3. Attach individual `.timeout()` and `.onErrorReturn()` fallback handlers.\n4. Write reactive test with `StepVerifier` asserting execution duration equals max latency.",
     "./mvnw -pl {path} test"),

    ("level:advanced", "[Advanced] Configure Resilience4j Sliding Window Circuit Breaker in {svc}",
     "Protect downstream dependencies with Resilience4j CircuitBreaker and Bulkhead in `{path}`.",
     "Prevent cascading failures by opening circuit when failure rate exceeds 50% over a 100-call sliding window.",
     "1. Add `resilience4j-spring-boot3` dependency.\n2. Configure `resilience4j.circuitbreaker.instances` in application.yml.\n3. Annotate remote client calls with `@CircuitBreaker(name = '...', fallbackMethod = '...')`.\n4. Write test simulating downstream 500 errors and verifying state transitions.",
     "./mvnw -pl {path} test"),

    ("level:advanced", "[Advanced] Enforce Compile-Time DOMA Architectural Rules via ArchUnit in {svc}",
     "Implement ArchUnit assertions verifying strict bounded context isolation in `{path}`.",
     "Guarantee that services in this domain never import classes from other leaf domains directly.",
     "1. Create `src/test/java/.../ArchitectureRulesTest.java`.\n2. Define ArchRule asserting no classes depend on forbidden domain packages.\n3. Assert zero direct cross-domain leaf calls.\n4. Verify tests pass on `./mvnw test`.",
     "./mvnw -pl {path} test"),

    ("level:advanced", "[Advanced] Implement Uber H3 Hexagonal Spatial Queries in {svc}",
     "Integrate Uber's H3 spatial index library for O(1) hexagonal neighbor lookups in `{path}`.",
     "Convert latitude/longitude coordinates into H3 Resolution 8/9 indexes for spatial clustering.",
     "1. Import `com.uber:h3` library.\n2. Method `geoToH3Address(lat, lng, resolution)`.\n3. Method `kRing(originH3, radius)` to fetch surrounding hexagonal rings.\n4. Unit test geospatial coverage around coordinate anchors.",
     "./mvnw -pl {path} test"),

    ("level:advanced", "[Advanced] Implement Micrometer Custom Performance Metrics & Timers in {svc}",
     "Instrument critical domain algorithms with Micrometer Timers and Distribution Summaries in `{path}`.",
     "Expose p50, p95, and p99 latency percentiles to Prometheus for Grafana dashboard visualization.",
     "1. Inject `MeterRegistry`.\n2. Create `Timer.builder(\"uber.{svc}.duration\").publishPercentiles(0.5, 0.95, 0.99)`.\n3. Wrap critical algorithmic block in `timer.record(() -> ...)`.\n4. Verify metrics endpoint `/actuator/metrics` exposes custom timer.",
     "./mvnw -pl {path} test"),

    ("level:advanced", "[Advanced] Implement W3C Distributed Tracing Context Propagation in {svc}",
     "Propagate OpenTelemetry `traceparent` headers across Netty thread pools and gRPC channels in `{path}`.",
     "Enable end-to-end distributed trace reconstruction in Jaeger across all 40 microservices.",
     "1. Add `io.micrometer:micrometer-tracing-bridge-otel`.\n2. Configure gRPC client and server tracing interceptors.\n3. Verify span IDs are injected into log statements and outgoing gRPC headers.",
     "./mvnw -pl {path} test"),

    # Tier 4: Staff Architect (Tasks 20-21)
    ("level:advanced", "[Staff Architect] Implement Contraction Hierarchy Bidirectional Search in {svc}",
     "Implement sub-10ms graph routing using pre-contracted road network graphs in `{path}`.",
     "Enable lightning-fast turn-by-turn routing across large urban street topologies.",
     "1. Implement bidirectional Dijkstra search.\n2. Add shortcut edge traversal logic.\n3. Benchmark routing query across a 50,000 node graph fixture asserting latency < 10ms.",
     "./mvnw -pl {path} test"),

    ("level:advanced", "[Staff Architect] Implement Chaos Fault Injection and Recovery Validation in {svc}",
     "Build automated chaos resiliency test suite simulating network partitions and packet loss in `{path}`.",
     "Verify that fallback degradation strategies maintain core booking SLAs under 30% packet loss.",
     "1. Write Chaos test with Toxiproxy / Testcontainers.\n2. Inject 500ms network latency and 20% dropped connections.\n3. Verify fallback stubs respond without throwing unhandled exceptions.",
     "./mvnw -pl {path} test"),
]

def main():
    log("🚀 Starting Full 867-Issue Batch Publisher...")

    # Load existing issue titles from GitHub
    existing_raw = subprocess.run(["gh", "issue", "list", "--repo", REPO, "--json", "title", "--limit", "1000"], capture_output=True, text=True)
    existing_titles = set()
    if existing_raw.returncode == 0:
        try:
            for item in json.loads(existing_raw.stdout):
                existing_titles.add(item["title"])
        except Exception:
            pass
    log(f"Found {len(existing_titles)} pre-existing issues on GitHub.")

    # Build the full 867 issue catalog
    all_issues = []

    # 1. 40 Services x 21 tasks = 840 issues
    for domain, svc, desc, dom_label in SERVICES:
        for labels, tmpl_title, tmpl_summary, tmpl_context, tmpl_steps, tmpl_cmd in TASK_TEMPLATES:
            title = tmpl_title.format(svc=svc)
            path = f"{domain}/{svc}"
            cls_name = "".join(x.capitalize() for x in svc.replace("-service", "").split("-"))
            full_labels = f"{labels},{dom_label}"

            body = f"""### 🎯 Objective
{tmpl_summary.format(path=path)}

---

### 📍 Target Microservice
`{path}` ({desc})

---

### 💡 Architectural Context
{tmpl_context.format(path=path)}

---

### 🛠️ Step-by-Step Implementation Guide
{tmpl_steps.format(path=path, clazz=cls_name)}

---

### 🧪 Verification Command
```bash
{tmpl_cmd.format(path=path)}
```

---

### ✅ Acceptance Criteria
- [ ] Implementation completed in `{path}`.
- [ ] Unit and integration tests pass with 100% assertions.
- [ ] No compile-time or architecture boundary violations.
- [ ] Verified locally via terminal commands."""

            all_issues.append({"title": title, "body": body, "labels": full_labels})

    # 2. Gateway and System Issues = 27 issues (Total 867)
    gateway_tasks = [
        ("[Beginner] Implement JWT Claims Parsing in {gw}", "Parse bearer tokens and extract rider/driver claims in `{gw}`.", "level:beginner,good-first-issue"),
        ("[Beginner] Configure Netty Epoll Native Transport in {gw}", "Enable high-throughput Linux Epoll socket transport in `{gw}`.", "level:beginner,good-first-issue"),
        ("[Moderate] Configure Redis Token Bucket Global Rate Limiting in {gw}", "Enforce 1000 RPS perimeter rate limit per IP using Redis in `{gw}`.", "level:moderate"),
        ("[Moderate] Implement Dynamic Route Refresh via Spring Cloud Config in {gw}", "Refresh gateway route definitions without restarting the gateway in `{gw}`.", "level:moderate"),
        ("[Advanced] Implement Protocol Transcoding from HTTP REST to gRPC in {gw}", "Transcode inbound JSON payload to Protobuf binary stub calls in `{gw}`.", "level:advanced"),
    ]

    for gw_path, gw_desc, gw_label in GATEWAYS:
        for title_tmpl, desc_tmpl, lvl_label in gateway_tasks:
            if len(all_issues) >= 867:
                break
            gw_title = title_tmpl.format(gw=gw_path.split("/")[-1])
            gw_body = f"""### 🎯 Objective
{desc_tmpl.format(gw=gw_path)}

---

### 📍 Target Gateway
`{gw_path}` ({gw_desc})

---

### 🛠️ Implementation Steps
1. Open `{gw_path}` configuration and filter packages.
2. Implement required gateway logic.
3. Test locally with cURL against gateway port.

---

### ✅ Acceptance Criteria
- [ ] Gateway route and filter working properly.
- [ ] End-to-end routing verified."""
            all_issues.append({"title": gw_title, "body": gw_body, "labels": f"{lvl_label},{gw_label}"})

    # Fill remaining to hit exactly 867 if any
    while len(all_issues) < 867:
        idx = len(all_issues) + 1
        all_issues.append({
            "title": f"[Moderate] Implement End-to-End Synthetic Heartbeat Check #{idx}",
            "body": "Implement synthetic ping heartbeat to verify inter-gateway routing health.",
            "labels": "level:moderate,architecture"
        })

    log(f"Catalog ready: {len(all_issues)} comprehensive issues defined.")

    # Publish loop
    published = 0
    skipped = 0
    for i, issue in enumerate(all_issues):
        if issue["title"] in existing_titles:
            skipped += 1
            continue

        cmd = [
            "gh", "issue", "create",
            "--repo", REPO,
            "--title", issue["title"],
            "--body", issue["body"],
            "--label", issue["labels"]
        ]

        success = False
        retries = 0
        while not success and retries < 5:
            try:
                res = subprocess.run(cmd, capture_output=True, text=True, check=True)
                published += 1
                log(f"[{published + skipped}/{len(all_issues)}] ✓ Created: {issue['title']}")
                success = True
                time.sleep(2.5) # Safe rate-limit delay
            except subprocess.CalledProcessError as e:
                err = e.stderr.strip()
                if "abuse" in err.lower() or "secondary rate limit" in err.lower() or "403" in err:
                    log(f"⚠️ Secondary rate limit hit. Sleeping 60s before retry... (Attempt {retries+1}/5)")
                    time.sleep(60)
                    retries += 1
                else:
                    log(f"✗ Error creating issue '{issue['title']}': {err}")
                    break

    log(f"🎉 Completed! Published: {published}, Skipped: {skipped}, Total in catalog: {len(all_issues)}")

if __name__ == "__main__":
    main()
