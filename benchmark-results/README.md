# Gradle performance benchmark results

This directory contains the performance measurements collected while upgrading and optimising the Gradle project.

## Initial baseline: Gradle 7.5.1

### Provenance

- Date: 20 August 2026
- Branch: `feature/gradle-upgrade`
- Baseline commit: `12568fb6c9c92a74464949a3cda45416d9eacbb3`
- Gradle version: 7.5.1
- Gradle Profiler version: 0.25.2
- Java: Eclipse Temurin OpenJDK 17.0.20
- Operating system: Windows 10 Home 64-bit, version 10.0.19045
- Processor: AMD Ryzen 7 4800H with Radeon Graphics
- Installed memory: 15.42 GB

### Method

The existing `performance.scenarios` file supplied with the project was used without modification. It defines two incremental compilation scenarios:

- `abiChange` applies a change to the public application binary interface of `utils/src/main/java/Utils.java`.
- `nonAbiChange` changes the implementation without changing its public binary interface.

Each scenario used six warm-up builds followed by ten measured builds. Gradle Profiler used the Gradle Tooling API and a dedicated Gradle user home outside the repository. Configuration/task-start time and garbage-collection time were also measured.

The benchmark command was equivalent to:

```text
gradle-profiler --benchmark --project-dir . --scenario-file performance.scenarios --gradle-version 7.5.1 --gradle-user-home <isolated-directory> --output-dir benchmark-results/baseline-gradle-7.5.1 --warmups 6 --iterations 10 --csv-format long --measure-config-time --measure-gc abiChange nonAbiChange
```

### Baseline results

| Scenario | Mean | Median | Minimum | Maximum | Standard deviation |
| --- | ---: | ---: | ---: | ---: | ---: |
| ABI change | 2,100.68 ms | 2,105.55 ms | 2,050.89 ms | 2,183.36 ms | 39.92 ms |
| Non-ABI change | 1,179.80 ms | 1,180.45 ms | 1,137.06 ms | 1,207.64 ms | 22.73 ms |

The ABI-changing scenario was 920.88 ms, or approximately 78.05%, slower on average than the non-ABI-changing scenario. An ABI change can invalidate compilation outputs in dependent projects, whereas a non-ABI implementation change normally limits recompilation.

The task-start measurements were similar for both scenarios, and garbage-collection time was small. This indicates that the main difference was associated with compilation work rather than configuration or garbage collection.

### Result files

- `baseline-gradle-7.5.1/benchmark.html` - interactive benchmark report.
- `baseline-gradle-7.5.1/benchmark.csv` - raw measurements in long CSV format.
- `baseline-gradle-7.5.1/profile.log` - Gradle Profiler execution log.

Gradle Profiler restored the mutated source file after completing the benchmark. A subsequent Git diff confirmed that the original project source remained unchanged.

### Baseline Build Scan

A clean build was also profiled with a Gradle Build Scan.

- Date: 21 August 2026
- Branch: `feature/gradle-upgrade`
- Commit: `aac7732ef56db5758052df7762d77a1f85472f9d`
- Command: `.\gradlew.bat clean build --scan --no-build-cache`
- Build Scan: https://gradle.com/s/qebsl67wenu5o
- Total build time: 1m 6.734s
- Initialization: 2.758s
- Configuration: 5.419s
- Execution: 58.525s
- End of build: 0.032s
- Garbage collection: 0.441s
- Peak heap usage: 219.1 MiB / 1 GiB (21.4%)
- Actionable tasks executed: 12
- Build cache: disabled for this baseline measurement

Execution accounted for most of the total build time. The scan also showed that the build cache was disabled, providing a clear optimisation candidate for later controlled testing.
## Reference

Gradle Profiler documentation: <https://github.com/gradle/gradle-profiler>

## Gradle 7.6.4 compatibility benchmark

### Provenance

- Branch: `feature/gradle-upgrade`
- Commit: `dd8b122114c66461827208a60191cbd47e9d91fb`
- Gradle version: 7.6.4
- Gradle Profiler version: 0.25.2
- Method: six warm-up builds and ten measured builds for each scenario, using an isolated Gradle user home.

### Results and comparison with the initial baseline

| Scenario | Gradle 7.5.1 mean | Gradle 7.6.4 mean | Difference | Change |
| --- | ---: | ---: | ---: | ---: |
| ABI change | 2,100.68 ms | 2,251.89 ms | +151.21 ms | +7.20% |
| Non-ABI change | 1,179.80 ms | 1,201.19 ms | +21.39 ms | +1.81% |

For this machine and these scenarios, the Gradle 7.6.4 compatibility upgrade completed successfully but did not improve incremental compilation performance. The ABI-change scenario showed the larger regression. This result is retained as an intermediate comparison point; later optimisation changes will be benchmarked separately rather than attributing a performance benefit to the version upgrade alone.

The measured results also showed moderate variation: the standard deviation was 66.96 ms for the ABI-change scenario and 16.72 ms for the non-ABI-change scenario.

### Result files

- `gradle-7.6.4/benchmark.html`
- `gradle-7.6.4/benchmark.csv`
- `gradle-7.6.4/profile.log`


## Gradle 9.5.1 upgrade and optimisation

### Upgrade changes

The project was upgraded from Gradle 7.5.1 to Gradle 9.5.1. The build scripts were migrated from Groovy DSL to Kotlin DSL, and the Gradle wrapper files were updated accordingly.

The Kotlin DSL build scripts document the project structure and dependency scopes. The `core` module exposes the `domain` module through `api`, while internal dependencies use `implementation`.

The upgraded project was validated with successful Gradle builds and performance benchmarks.

### Gradle 9.5.1 benchmark

The same Gradle Profiler methodology was used for the Gradle 9.5.1 benchmark as for the initial baseline:

- six warm-up builds;
- ten measured builds;
- `abiChange` and `nonAbiChange` scenarios;
- configuration time and garbage-collection time measurement;
- isolated Gradle user home.

The benchmark results were recorded in commit [`e304286f`](https://github.com/KK22334/DevOps-Sup-CA1-26/commit/e304286f).

| Scenario | Gradle 7.5.1 mean | Gradle 9.5.1 mean | Difference | Change |
| --- | ---: | ---: | ---: | ---: |
| ABI change | 2,100.68 ms | 2,414.02 ms | +313.34 ms | +14.92% |
| Non-ABI change | 1,179.80 ms | 1,330.23 ms | +150.43 ms | +12.75% |

The Gradle 9.5.1 benchmark did not improve incremental compilation performance in this project. The mean execution time increased for both scenarios. This result applies to the tested project, hardware, operating system and benchmark configuration and should not be generalised to all Gradle builds.

The complete Gradle 9.5.1 benchmark files are available in the [`gradle-9.5.1-complete`](https://github.com/KK22334/DevOps-Sup-CA1-26/tree/e304286f/benchmark-results/gradle-9.5.1-complete) directory.

### Build optimisation with configuration cache

The project already had Gradle build caching enabled through `gradle.properties`. Configuration cache was added with:

```properties
org.gradle.configuration-cache=true
```

This change was recorded in commit [`12943273`](https://github.com/KK22334/DevOps-Sup-CA1-26/commit/12943273).

The project configuration already enabled the Gradle build cache. The baseline scan intentionally used `--no-build-cache`, so it was a measurement baseline rather than evidence that build caching had just been enabled.

#### Validation results

An initial post-upgrade clean build without configuration cache completed in 1 minute 38 seconds. The first build with configuration cache completed in 19 seconds and stored an entry. A subsequent build reused the configuration cache and completed in 18 seconds.

The persistent setting was validated by running `.\gradlew.bat clean build --scan` without the `--configuration-cache` command-line option. Gradle reported `Reusing configuration cache` and `Configuration cache entry reused`. The build completed successfully in 18 seconds; 8 tasks were executed and 4 tasks were taken from the build cache.

This observed reduction from 1 minute 38 seconds to 18 seconds is not a controlled comparison and must not be attributed only to configuration cache. The Gradle daemon and existing build cache also contributed.

#### Relevant Build Scans

- [Initial Gradle 9.5.1 clean build — 1m 38s](https://gradle.com/s/j4r3wpto5r44i)
- [First configuration-cache build — 19s](https://gradle.com/s/7hkzxirhwdyom)
- [Configuration-cache reuse validation — 18s](https://gradle.com/s/3qj7c3ljzyjnk)
- [Persistent configuration-cache validation — 18s](https://gradle.com/s/co2tnlhezbwgo)

#### Environment limitation

Gradle reported that some Windows performance counters could not be initialised. These warnings did not cause the builds to fail, but resource-usage information in the Build Scans may be incomplete.