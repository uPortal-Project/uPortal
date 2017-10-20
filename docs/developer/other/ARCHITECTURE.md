# Architecture

## Build Structure

uPortal leverages [Gradle][] for build and package management.
Gradle divides the work needed to build the portal into [projects and tasks][].
Each logical grouping of code within the portal is its own subproject, and each subproject has a graph of tasks.
Each task linking back to work (other tasks) that it depends on to function.

The most complete view of the task graph and build dependency structure comes from the `gradle release` task, seen below.

![uPortal release task graph](../../images/uportal-release-task.min.svg)

There are some interesting architectural features that can be extracted from the overall task graph.

### Loosely Coupled Subprojects

Loosely coupled subprojects will usually keep grouped together in the task graph, with limited linking out to other subprojects.
For example: the five subprojects shown below are completely decoupled.

![loose coupling in task graph](../../images/release-task-graph-loose-coupling.png)

:notebook: N.B. JavaDoc tasks do tend to be highly linked together, however cross-linking across JavaDoc does not necessarily signal tight coupling.

### Tightly coupled Subprojects

Projects with tight coupling tend to be pulled toward the center of the graph, and have many lines going outward from their `compileJava` task.
For example: the `uPortal-security/uPortal-security-authn` subproject depends on 23 other subprojects.

![tight coupling in task graph](../../images/release-task-graph-tight-coupling.png)

:notebook: N.B. JavaDoc tasks do tend to be highly linked together, however cross-linking across JavaDoc does not necessarily signal tight coupling.

[Gradle]: https://gradle.org
[projects and tasks]: https://docs.gradle.org/current/userguide/tutorial_using_tasks.html
