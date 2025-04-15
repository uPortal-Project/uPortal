# Import/Export Documentation for uPortal 5

## Overview

The Import/Export tool in uPortal 5 allows users to transfer configuration data and settings between instances, facilitating data migration and backup.

## Data tasks

- **dataImport**: Adds to or updates the portal database for the entities defined in the specified XML file(s). Requires -Dfile={path-to-file} or -Ddir={path-to-directory}

- **dataList**: With no arguments (-Dtype={entity-type}) lists all supported portal data types and the operations (export, delete) supported for each.

- **dataExport**: Creates XML files representing the requested entities and writes them to the specified file system location. Parameters: -Ddir={path-to-directory} -Dtype={entity-type} [-Dsysid={entity-identifier}]

- **dataDelete**: Deletes the specified entity. Requires -Dtype={entity-type} and -Dsysid={id}

- **dataInit**: Drop and recreate uPortal tables and reimport data


## Running Tasks via Gradle
To run the Import/Export tool via Gradle, navigate to the uPortal project directory and execute

```bash
./gradlew :overlays:uPortal:<task-name> -D<options>

```

## Importing Data

### Import Multiple Files

```bash
./gradlew :overlays:uPortal:dataImport -Ddir={path-to-directory} [-Dpattern={ant-pattern}]

```

### Import Single File

```bash
./gradlew :overlays:uPortal:dataImport -Dfile={path-to-file}

```

### Import Single File (uPortal 4.3+)

```bash
./gradlew :overlays:uPortal:dataImport -Dfiles={comma-separated-list-of-files}

```

### Import List File (uPortal 4.3+)

```bash
./gradlew :overlays:uPortal:dataImport -DfilesListFile={path-to-file}

```

## Listing Data

### List Types

```bash
./gradlew :overlays:uPortal:dataList

```

### List Data of a Specific Type

```bash
./gradlew :overlays:uPortal:dataList -Dtype={entity-type}

```


## Exporting Data

```bash
./gradlew :overlays:uPortal:dataExport -Ddir={path-to-directory} -Dtype={entity-type} [-Dsysid={entity-identifier}]

```


## Deleting Data

```bash
./gradlew :overlays:uPortal:dataDelete -Dtype={entity-type} [-Dsysid={entity-identifier}]

```

## Reinitializing Data (Drop & Recreate Tables)

```bash
./gradlew :overlays:uPortal:dataInit

```

## Import/Export Logs

Logs for Import/Export operations can be found in the `UPORTAL_ROOT/target/data-import-reports` directory. Look for `data-import.txt` for summary reports. If errors occur, individual error report files will also be generated for each failed object.

## Troubleshooting

Refer to the logs for detailed error messages if operations fail.

## Additional Resources

- [uPortal 4.1 Importing and Exporting Data](https://apereo.atlassian.net/wiki/spaces/UPM41/pages/103942373/Importing+and+Exporting+data)


