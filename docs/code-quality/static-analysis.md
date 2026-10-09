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

#### AI usage:

Initial skeleton was drafted with LLM