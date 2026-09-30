#!/usr/bin/env python3
import subprocess
import time
import json

REPO = "YeamimHossainSajid/uber-doma-architecture"

# Fetch existing issues to avoid duplicates
existing_raw = subprocess.run(["gh", "issue", "list", "--repo", REPO, "--json", "title", "--limit", "100"], capture_output=True, text=True)
existing_titles = set()
if existing_raw.returncode == 0:
    for item in json.loads(existing_raw.stdout):
        existing_titles.add(item["title"])

print(f"Found {len(existing_titles)} existing issues on GitHub.")

ISSUES = [
    # ================= BEGINNER LEVEL =================
    {
        "title": "[Beginner] Implement Input Validation & DTO Constraints in supply-locator-service",
        "labels": "level:beginner,good-first-issue,domain:mobility",
        "body": """### 🎯 Objective
Add comprehensive validation annotations to the Driver Location Update request DTO in `supply-locator-service` to ensure GPS coordinates and duty status are sanitized before processing.

---

### 📍 Target Microservice
`domain-mobility/supply-locator-service`

---

### 💡 Architectural Context
In Uber's DISCO mobility system, driver mobile applications stream GPS coordinates every 4 seconds. Invalid coordinates (e.g. `latitude > 90` or `null` timestamps) can corrupt the spatial index and break matching calculations.

---

### 🛠️ Step-by-Step Implementation Guide
1. Open or create the DTO: `domain-mobility/supply-locator-service/src/main/java/com/uber/doma/domain_mobility/supply_locator_service/dto/DriverLocationRequestDto.java`.
2. Add Jakarta validation annotations (`jakarta.validation.constraints.*`):
   - `@NotNull(message = "Driver ID must not be null")` on `driverId`
   - `@DecimalMin(value = "-90.0")` and `@DecimalMax(value = "90.0")` on `latitude`
   - `@DecimalMin(value = "-180.0")` and `@DecimalMax(value = "180.0")` on `longitude`
   - `@Min(value = 0)` on `timestampMs`
3. Add a `@RestControllerAdvice` in `com.uber.doma.domain_mobility.supply_locator_service.advice.ValidationExceptionHandler` to catch `MethodArgumentNotValidException` and return HTTP `400 BAD_REQUEST` with field error details.
4. Write a unit test in `src/test/java/.../DriverLocationValidationTest.java` using `ValidatorFactory` to assert that invalid coordinates produce validation violations.

---

### 🧪 Verification
```bash
./mvnw -pl domain-mobility/supply-locator-service test
```

---

### ✅ Acceptance Criteria
- [ ] DTO rejects coordinates outside valid Earth boundaries (-90 to 90 lat, -180 to 180 lng).
- [ ] Unit tests pass with 100% assertions on null/invalid inputs.
- [ ] Returns structured JSON error response on validation failure."""
    },
    {
        "title": "[Beginner] Configure Spring Boot Actuator & Health Probes in fare-quotation-service",
        "labels": "level:beginner,good-first-issue,domain:billing",
        "body": """### 🎯 Objective
Expose and customize Spring Boot Actuator health endpoints in `fare-quotation-service` with custom probes for database and gRPC readiness.

---

### 📍 Target Microservice
`domain-billing/fare-quotation-service`

---

### 💡 Architectural Context
Kubernetes and the Tier-1 Edge Gateway use liveness and readiness probes to determine whether a service can accept rider fare estimate requests. If pricing rates cannot be loaded, the pod must report `DOWN` to prevent riders from being quoted stale prices.

---

### 🛠️ Step-by-Step Implementation Guide
1. In `domain-billing/fare-quotation-service/pom.xml`, verify `spring-boot-starter-actuator` is present.
2. In `src/main/resources/application.yml`, expose health endpoints:
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: "health,info,metrics"
     endpoint:
       health:
         show-details: always
         probes:
           enabled: true
   ```
3. Create custom indicator: `src/main/java/.../health/PricingEngineHealthIndicator.java`:
   - Implement `org.springframework.boot.actuate.health.HealthIndicator`.
   - Return `Health.up().withDetail("pricing_version", "v2.4").build()`.
4. Write a Spring Boot integration test with `@SpringBootTest` asserting `GET /actuator/health` returns status `200 UP`.

---

### 🧪 Verification
```bash
curl http://localhost:9116/actuator/health
```

---

### ✅ Acceptance Criteria
- [ ] `/actuator/health` endpoint returns `UP` status.
- [ ] Liveness and readiness endpoints `/actuator/health/liveness` and `/readiness` are active.
- [ ] Integration test verifies status."""
    },
    {
        "title": "[Beginner] Implement Vehicle Class Enum & Validation in vehicle-registry-service",
        "labels": "level:beginner,good-first-issue,domain:driver",
        "body": """### 🎯 Objective
Define an authoritative `VehicleClass` enumeration (`UBER_X`, `UBER_XL`, `UBER_BLACK`, `UBER_COMFORT`) with seat capacity constraints and validator logic.

---

### 📍 Target Microservice
`domain-driver/vehicle-registry-service`

---

### 💡 Architectural Context
When a driver registers a vehicle, Uber must classify it according to year, make, model, and passenger seat count to match dispatch requests properly.

---

### 🛠️ Step-by-Step Implementation Guide
1. Create `com.uber.doma.domain_driver.vehicle_registry_service.model.VehicleClass`:
   - Fields: `int minimumSeats`, `int maximumAgeYears`, `boolean luxuryRequired`.
   - Values:
     - `UBER_X` (4 seats, 15 years max, luxury: false)
     - `UBER_XL` (6 seats, 15 years max, luxury: false)
     - `UBER_BLACK` (4 seats, 5 years max, luxury: true)
     - `UBER_COMFORT` (4 seats, 7 years max, luxury: false)
2. Add a helper method `public boolean meetsCriteria(int seats, int modelYear, boolean luxury)`.
3. Add unit test asserting valid and invalid vehicles matching each class.

---

### 🧪 Verification
```bash
./mvnw -pl domain-driver/vehicle-registry-service test
```

---

### ✅ Acceptance Criteria
- [ ] Enum defines minimum seats and maximum age thresholds.
- [ ] Unit tests pass with 100% boundary checks."""
    },
    {
        "title": "[Beginner] Configure SLF4J Structured Logging & Correlation IDs in edge-gateway",
        "labels": "level:beginner,good-first-issue",
        "body": """### 🎯 Objective
Configure standard structured JSON logging with MDC correlation IDs (`X-Trace-ID`, `X-Rider-ID`) in the Tier-1 Edge API Gateway.

---

### 📍 Target Microservice
`edge-gateway`

---

### 💡 Architectural Context
Distributed tracing starts at the Edge Gateway. Every inbound request must be assigned a unique `X-Trace-ID` header if not present, propagated to logs via SLF4J MDC so log aggregators (ELK / Loki) can correlate downstream logs.

---

### 🛠️ Step-by-Step Implementation Guide
1. In `edge-gateway`, create a `GlobalFilter` component named `TraceIdFilter.java`.
2. Inspect `exchange.getRequest().getHeaders().getFirst("X-Trace-ID")`.
3. If absent, generate `UUID.randomUUID().toString()`.
4. Mutate request headers to include the trace ID.
5. In log statements, include the trace ID in MDC context.
6. Verify output in terminal logs.

---

### 🧪 Verification
```bash
curl -i http://localhost:8080/actuator/health
```

---

### ✅ Acceptance Criteria
- [ ] Every request receives or preserves `X-Trace-ID`.
- [ ] Outgoing response contains `X-Trace-ID` header."""
    },
    {
        "title": "[Beginner] Implement Rider Rating Boundary Validator in mutual-rating-service",
        "labels": "level:beginner,good-first-issue,domain:rider",
        "body": """### 🎯 Objective
Implement strict score boundary validation ($1 \le \text{stars} \le 5$) with optional feedback tag checks in `mutual-rating-service`.

---

### 📍 Target Microservice
`domain-rider/mutual-rating-service`

---

### 💡 Architectural Context
Uber's two-way rating system requires riders and drivers to rate each other on a 1-to-5 integer scale after every trip. Values outside this range (e.g. 0 or 6) must be rejected with informative error messages.

---

### 🛠️ Step-by-Step Implementation Guide
1. In `domain-rider/mutual-rating-service`, create DTO `SubmitRatingRequest.java`.
2. Annotate `stars` with `@Min(1)` and `@Max(5)`.
3. If stars <= 3, require at least one feedback tag (e.g. `NAVIGATION_ISSUE`, `CLEANLINESS`, `HARSH_DRIVING`).
4. Write unit tests covering boundary cases (0, 1, 5, 6 stars).

---

### 🧪 Verification
```bash
./mvnw -pl domain-rider/mutual-rating-service test
```

---

### ✅ Acceptance Criteria
- [ ] Ratings strictly between 1 and 5 stars accepted.
- [ ] Low ratings require feedback reason tag."""
    },
    {
        "title": "[Advanced] Compile-Time Architectural Boundary Verification via ArchUnit",
        "labels": "level:advanced,architecture",
        "body": """### 🎯 Objective
Create a centralized ArchUnit test suite verifying DOMA rules across all 40 microservices:
1. Zero upward dependencies (Layer N cannot import Layer N+1).
2. Zero cross-domain direct leaf package imports (Domain A service cannot import Domain B service).
3. All persistence access must remain inside repository packages.

---

### 📍 Target Module
`common/common-domain-core` or root test suite

---

### 💡 Architectural Context
Without automated architecture tests, engineers inadvertently import classes across domain boundaries, creating circular dependencies that degrade modular autonomy.

---

### 🛠️ Step-by-Step Implementation Guide
1. In `common/common-domain-core/pom.xml`, add `com.tngtech.archunit:archunit-junit5`.
2. Create `src/test/java/com/uber/doma/architecture/DomaArchitectureRulesTest.java`.
3. Add rules asserting zero upward calls and zero direct cross-domain package references.
4. Run `./mvnw test` to verify architecture compliance.

---

### 🧪 Verification
```bash
./mvnw -pl common/common-domain-core test
```

---

### ✅ Acceptance Criteria
- [ ] ArchUnit tests fail if cross-domain imports are introduced.
- [ ] Verifies downward-only dependency law."""
    }
]

for issue in ISSUES:
    if issue["title"] in existing_titles:
        print(f"Skipping existing issue: {issue['title']}")
        continue

    print(f"Publishing: {issue['title']}")
    cmd = [
        "gh", "issue", "create",
        "--repo", REPO,
        "--title", issue["title"],
        "--body", issue["body"],
        "--label", issue["labels"]
    ]
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, check=True)
        print(f"   ✓ Created: {res.stdout.strip()}")
        time.sleep(2)
    except subprocess.CalledProcessError as e:
        print(f"   ✗ Error creating issue: {e.stderr}")

print("✨ Successfully published issues to GitHub!")
