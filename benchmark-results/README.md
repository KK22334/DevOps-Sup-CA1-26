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

## Reference

Gradle Profiler documentation: <https://github.com/gradle/gradle-profiler>
