# Building uPortal

uPortal uses Gradle for its project configuration and build system.  uPortal comes with a Gradle wrapper if you don't want to install the build tool (`./gradlew` in the root directory of the repo).

## uPortal-start

Unless you have a specific reason to build uPortal directly from the repo, [uPortal-start](https://github.com/uPortal-Project/uPortal-start) is the recommended way to deploy uPortal builds.  By using uPortal-start, there are gradle tasks that allow ingestion of the data xml files used to customize uPortal.

## Install uPortal to Maven Local

When making changes in the uPortal code base, and wanting to test them locally, build uPortal and store the binary in your Maven local repo.  You can do so from the uPortal base directory:
```bash
./gradlew install
```

## Gradle tasks

For a full list of uPortal Gradle tasks run `./gradlew tasks` from the root directory.

