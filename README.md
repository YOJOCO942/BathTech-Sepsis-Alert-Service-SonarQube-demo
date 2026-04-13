# BathTech Sepsis Alert Service — SonarQube demo

This project is a demo for the CM52067 video presentation. It contains a
Spring Boot microservice (the alert service from the BathTech sepsis
detection system) and a local SonarQube setup that demonstrates static
analysis, the dashboard, and a quality gate blocking a merge.

## Project structure

```
sonarqube-demo/
├── docker-compose.yml                  # SonarQube + PostgreSQL
├── pom.xml                             # Maven build with JaCoCo + Sonar plugin
├── .github/workflows/ci.yml            # GitHub Actions pipeline (report reference)
└── src/
    ├── main/java/com/bathtech/sepsis/
    │   ├── SepsisAlertApplication.java
    │   ├── model/
    │   │   ├── Patient.java
    │   │   └── SepsisAlert.java
    │   ├── repository/
    │   │   ├── PatientRepository.java
    │   │   └── SepsisAlertRepository.java
    │   ├── service/
    │   │   ├── PatientSearchService.java              ← FIXED version (passes gate)
    │   │   ├── PatientSearchService_VULNERABLE.java   ← VULN version (fails gate)
    │   │   └── SepsisRiskService.java
    │   └── controller/
    │       └── AlertController.java
    ├── main/resources/application.yml
    └── test/java/.../SepsisRiskServiceTest.java
```

## Prerequisites

- Docker Desktop
- Java 17+
- Maven 3.9+

---

## Step-by-step demo (for video recording)

### 1. Start SonarQube

```bash
cd sonarqube-demo
docker compose up -d
```

Wait about 1–2 minutes. Open http://localhost:9000 and log in with
`admin` / `admin`. You will be prompted to set a new password.

### 2. Create project and generate token

1. Click **Create a local project**
2. Project display name: `BathTech Sepsis Alert Service`
3. Project key: `bathtech-sepsis-alert`
4. Main branch: `main`
5. Click **Locally** → generate a token → copy it

### 3. First scan — VULNERABLE code (quality gate FAILS)

Rename the vulnerable file so SonarQube scans it as the active service:

```bash
# Temporarily swap in the vulnerable version
cp src/main/java/com/bathtech/sepsis/service/PatientSearchService.java \
   src/main/java/com/bathtech/sepsis/service/PatientSearchService_SAFE.java

cp src/main/java/com/bathtech/sepsis/service/PatientSearchService_VULNERABLE.java \
   src/main/java/com/bathtech/sepsis/service/PatientSearchService.java
```

> Note: you may need to adjust the class name inside the file to
> `PatientSearchService` and the package import. Alternatively, just
> scan the project as-is — SonarQube analyses all `.java` files,
> including the `_VULNERABLE` one.

Run the scan:

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=bathtech-sepsis-alert \
  -Dsonar.projectName="BathTech Sepsis Alert Service" \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN_HERE
```

**Expected result in SonarQube dashboard:**

| Metric         | Value                |
|----------------|----------------------|
| Bugs           | 0–1                  |
| Vulnerabilities| 3+ (SQL injections)  |
| Security Hotspots | 1+ (hardcoded password) |
| Code Smells    | 3+ (System.out, empty catch, return null) |
| Quality Gate   | **FAILED**           |

**What to show in the video:**
- The main dashboard with red "Failed" quality gate badge
- Click into the Vulnerabilities tab → show the SQL injection issues
- Click into one issue → SonarQube shows the exact line with the
  concatenated query string and explains CWE-89
- Show the Security Hotspots tab → hardcoded credential
- Show the Code Smells → System.out.println, empty catch block

### 4. Second scan — FIXED code (quality gate PASSES)

Restore the safe version:

```bash
# Restore the fixed version
cp src/main/java/com/bathtech/sepsis/service/PatientSearchService_SAFE.java \
   src/main/java/com/bathtech/sepsis/service/PatientSearchService.java
```

Or simply delete the `_VULNERABLE.java` file and re-scan:

```bash
rm src/main/java/com/bathtech/sepsis/service/PatientSearchService_VULNERABLE.java

mvn clean verify sonar:sonar \
  -Dsonar.projectKey=bathtech-sepsis-alert \
  -Dsonar.projectName="BathTech Sepsis Alert Service" \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN_HERE
```

**Expected result:**

| Metric         | Value             |
|----------------|-------------------|
| Vulnerabilities| 0                 |
| Security Hotspots | 0              |
| Code Smells    | 0 (or minor only) |
| Quality Gate   | **PASSED**        |

**What to show in the video:**
- Dashboard now shows green "Passed" badge
- 0 vulnerabilities, 0 security hotspots
- Briefly explain: in the CI/CD pipeline, this gate would block the
  merge if the vulnerable code was submitted as a pull request

### 5. Clean up

```bash
docker compose down -v
```

---

## Vulnerabilities demonstrated

| # | File | Issue | SonarQube Rule | Severity |
|---|------|-------|----------------|----------|
| 1 | `PatientSearchService_VULNERABLE.java` | SQL injection via string concatenation (`searchByNhsNumber`) | java:S2077 / java:S3649 | Critical |
| 2 | Same | SQL injection (`searchByWard`) | java:S2077 | Critical |
| 3 | Same | Hardcoded database password | java:S6437 / java:S2068 | Blocker |
| 4 | Same | `System.out.println` instead of logger | java:S106 | Major |
| 5 | Same | Empty catch block | java:S108 | Major |
| 6 | Same | Returning null instead of empty collection | java:S1168 | Minor |

## Fixes applied in clean version

| # | Fix | How |
|---|-----|-----|
| 1–2 | SQL injection | Use Spring Data JPA repository methods (parameterised queries) |
| 3 | Hardcoded password | Removed; `application.yml` reads `${DB_PASSWORD}` from environment |
| 4 | System.out | Replaced with SLF4J `LoggerFactory.getLogger()` |
| 5 | Empty catch | Catch block now logs the exception with `logger.error()` |
| 6 | Return null | Returns `Collections.emptyList()` instead |

---

## How this fits the report

This demo corresponds to Section 3.2 (CI/CD Pipeline) and Section 4.3
(Quality and DevOps Tooling) of the technical report. The pipeline stages
demonstrated are:

1. Developer pushes code → GitHub
2. GitHub Actions triggers build
3. **SonarQube runs static analysis** ← demonstrated here
4. **Quality gate blocks merge if issues found** ← demonstrated here
5. If passed → deploy via blue-green strategy
