# DevOps Pipelines Supplementary CA1 June 2026

## Description 

This repository contains a small Gradle multi-project build that is used for performance profiling Gradle and other build tools. The Gradle project consists of four sub-projects:

      - `app` is an application which depends on library `core`
      - `core` is a library which API depends on `domain`, and implementation depends on `utils`
      - `domain` is a library which has no dependency
      - `utils` is a library which has no dependency
   each project has 500 source files.

  
## Assignment tasks for completion

Using the knowledge and practical experience of Gradle you are required to explore how different features of Gradle (Gradle daemon/wrapper version, DSL language and optimisation features) impact on build performance. You should complete and document the following tasks:  

* Create an initial performance profile benchmark of the current project __*before*__ making any modifications. 
* Update the existing Gradle project to:  
  * Use the latest version of Gradle (currently version 9.5.1 as of June 2026).
  * Migrate the build scripts to use Kotlin DSL instead of Groovy.  
* Document your attempts to optimise the performance of the gradle build.

Please read the assignment specification on Blackboard learn for a complete list of deliverables a breakdown of marks and assessment criteria.

__Note__ you should begin the assignment by creating an initial performance benchmark. You can performance profile the project using either:

- [Gradle build scans](https://docs.gradle.org/current/userguide/build_scans.html)
- [Gradle profiler tool](https://github.com/gradle/gradle-profiler)

Regardless of which tool you use to profile the project and document the impact different versions and features of Gradle, you should methodically document the performance of the project build before, during and after you make any changes to the project. 