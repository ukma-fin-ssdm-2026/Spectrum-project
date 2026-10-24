## Spectrum-project static analysis

Performed at commit `c04ee0d1`

### Analyzer and configuration

The `spectrum-app` runs [SpotBugs](https://spotbugs.github.io/) through
`com.github.spotbugs:spotbugs-maven-plugin:4.10.4.1`.
Also, [Find Security Bugs](https://find-sec-bugs.github.io/)
`com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0` was added.

The plugin is configured in `spectrum-app/pom.xml` with:

| Setting        | Value                       |
|----------------|-----------------------------|
| `effort`       | `Max`                       |
| `threshold`    | `Medium`                    |
| `plugins`      | Find Security Bugs `1.14.0` |
| execution goal | `check`                     |

Run the check from the application module with

```powershell
cd spectrum-app
.\mvnw spotbugs:check
```

Findings can be reviewed in the Maven output and generated `SpotBugs`
XML report under `spectrum-app/target`.

### Findings at `Medium` threshold

```terminaloutput
[ERROR] Medium: edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError.getFields() may expose internal representation by returning ValidationError.fields [edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError] At ValidationError.java:[line 16] EI_EXPOSE_REP
[ERROR] Medium: new edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError(List) may expose internal representation by storing an externally mutable object into ValidationError.fields [edu.ukma.ssdm.teamspectrum.api.common.error.ValidationError] At ValidationError.java:[line 12] EI_EXPOSE_REP2
[ERROR] Medium: edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse.getItems() may expose internal representation by returning PageResponse.items [edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse] At PageResponse.java:[line 23] EI_EXPOSE_REP
[ERROR] Medium: new edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse(List, long, PaginationParams) may expose internal representation by storing an externally mutable object into PageResponse.items [edu.ukma.ssdm.teamspectrum.api.common.page.PageResponse] At PageResponse.java:[line 15] EI_EXPOSE_REP2
```

The run found 4 `Medium`-priority `EI_EXPOSE_REP` / `EI_EXPOSE_REP2`.
They indicate that the DTOs keep or return a reference to a mutable list, allowing callers
with a reference to that list to change its contents.

| Class             | Findings                                                          | Details                                                                                            |
|-------------------|-------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `ValidationError` | `EI_EXPOSE_REP2` at constructor; `EI_EXPOSE_REP` at `getFields()` | The constructor stores the supplied `fields` list directly, and the getter returns that same list. |
| `PageResponse`    | `EI_EXPOSE_REP2` at constructor; `EI_EXPOSE_REP` at `getItems()`  | The constructor stores the supplied `items` list directly, and the getter returns that same list.  |

These findings describe a mutability and encapsulation risk; by themselves they do not
demonstrate an exploitable security vulnerability. In the current call sites, the lists
are produced by `Stream.toList()`, which returns an unmodifiable list. The DTO
classes do not enforce this for every possible caller. \
Findings were fixed with adding `List.copyOf(...)` in the constructors to make saved list
immutable.

### Low-priority endpoint marker

`FindSecBugs` also reported `JAXRS_ENDPOINT` for `HotelController.getHotel(UUID)` in a run
configured with a `Low` threshold. This is an endpoint identification/review marker, not a
finding of a specific vulnerability. It is not included at the configured Medium
threshold.

### Pre-commit

2 false positives were found by pre-commit when making commit for auth functionality and
suppressed:

```terminaloutput
Spotless format..........................................................Passed
SpotBugs static analysis.................................................Failed
- hook id: spotbugs
- exit code: 1
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------< edu.ukma.ssdm.teamspectrum:spectrum-app >---------------
...
[INFO] Done SpotBugs Analysis....
[INFO] Skipping com.github.spotbugs:spotbugs-maven-plugin:4.10.4.1:spotbugs report goal
[INFO] 
[INFO] <<< spotbugs:4.10.4.1:check (default-cli) < :spotbugs @ spectrum-app <<<
[INFO] 
[INFO] 
[INFO] --- spotbugs:4.10.4.1:check (default-cli) @ spectrum-app ---
[INFO] BugInstance size is 1
[INFO] Error size is 0
[INFO] Total bugs: 1
[ERROR] Medium: new edu.ukma.ssdm.teamspectrum.logic.auth.AuthServiceImpl(UserRepository, String) may expose internal representation by storing an externally mutable object into AuthServiceImpl.userRepository [edu.ukma.ssdm.teamspectrum.logic.auth.AuthServiceImpl] At AuthServiceImpl.java:[line 28] EI_EXPOSE_REP2
[INFO] 
To see bug detail using the Spotbugs GUI, use the following command "mvn spotbugs:gui"
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.235 s
[INFO] Finished at: 2026-10-09T20:30:14+03:00
[INFO] ------------------------------------------------------------------------
Detect hardcoded secrets.................................................Failed
- hook id: gitleaks
- exit code: 1
○
    │╲
    │ ○
    ○ ░
    ░    gitleaks
Finding:     ...static final String passwordExample = "REDACTED"
Secret:      REDACTED
RuleID:      generic-api-key
Entropy:     3.700440
File:        spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/auth/RegisterRequest.java
Line:        13
Fingerprint: spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/auth/RegisterRequest.java:generic-api-key:13
```

1. First one was about valid dependency for repository by `SpotBugs`
2. Second one was about secret leak, but was just an example for openapi specification,
   found by `gitleaks`

### Security analyzer result and threat T-01

Performed at commit `76870d7a`.

SpotBugs with Find Security Bugs found **no security vulnerability**: the only `Medium`
findings were the `EI_EXPOSE_REP` / `EI_EXPOSE_REP2` warnings above. The configuration is the
one in the table above; the analysis covers all compiled classes of
`spectrum-app/src/main/java` (packages `api` and `logic`), test code is not analysed.

Because the analyzer found nothing, we take a threat from the
[threat model](../architecture/threat-model.md) and show how the code counters it.

| ID   | Component / boundary | STRIDE   | Scenario                                                                                 | Rating |
|------|----------------------|----------|------------------------------------------------------------------------------------------|--------|
| T-01 | TB-1, login endpoint | Spoofing | An attacker guesses or reuses leaked passwords to log in as another client or worker.    | High   |

#### How the code counters T-01

| Measure | Where in the code | Effect |
|---------|-------------------|--------|
| Passwords are stored only as bcrypt hashes | [`UserFactoryImpl.java:18`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/UserFactoryImpl.java#L18): `BcryptUtil.bcryptHash(password)` | A leak of user data does not reveal passwords; bcrypt is slow, so offline guessing is expensive (SEC-002) |
| Login compares the password with the hash | [`AuthServiceImpl.java:51`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/AuthServiceImpl.java#L51): `BcryptUtil.matches(...)` | The plain password is never stored or compared directly |
| The same `401` for an unknown email and a wrong password | [`AuthController.java:69-70`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/auth/AuthController.java#L69-L70) | The response does not tell an attacker which emails are registered |
| Passwords of 12–72 characters | [`RegisterRequest.java:11`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/auth/RegisterRequest.java#L11): `@Size(min = 12, max = 72)` | Short passwords are rejected; 72 is the bcrypt input limit |
| Short-lived token in a protected cookie | [`TokenFactoryImpl.java:15`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/TokenFactoryImpl.java#L15) (15 minutes), [`AuthController.java:80-82`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/auth/AuthController.java#L80-L82) (`Secure`, `HttpOnly`, `SameSite=Lax`) | A stolen token works for at most 15 minutes and cannot be read by JavaScript (also T-02) |

#### Not countered yet

| Gap | Risk | Rating | Planned action |
|-----|------|--------|----------------|
| No limit on login attempts | Online password guessing is not slowed down | High | Rate limiting of the login endpoint in Nginx, as planned in the threat model |
| bcrypt runs only when the email exists | The response time shows whether an email is registered | Low | Compare against a fixed dummy hash when the user is not found |

#### Finding from manual review

Reading the code for T-01 revealed a problem that SpotBugs does not report:

- **What:** [`AuthServiceImpl.java:34`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/auth/AuthServiceImpl.java#L34) logs
  `logger.debug("Register request: {}", request)`. `RegisterRequest` is a record, and the
  `toString()` of a record contains all fields, so the plain-text password is written to the
  log whenever the `DEBUG` level is enabled.
- **Why it is a risk:** logs are read by more people and kept longer than the database;
  passwords must exist only as hashes (SEC-002).
- **Rating:** Medium. The default log level is `INFO`, so nothing is logged today, but one
  configuration change exposes every new password.
- **Decision:** fix it by logging only the normalised email (as `login` already does),
  in a separate pull request with a regression test.

A second gap from the same review: `POST /v1/api/hotels`
([`HotelController.java:59`](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/api/hotel/HotelController.java#L59)) requires no
authentication, so anyone can create a hotel. This is threat T-09 / SEC-003; authorization
for hotel and room management is in progress (see [progress](progress.md)).

#### AI usage:

Initial skeleton was drafted with LLM.
The section "Security analyzer result and threat T-01" was drafted with LLM from the threat
model and the source code; the line references were checked against commit `76870d7a`.
Verification: reviewed in the pull request before merging.
