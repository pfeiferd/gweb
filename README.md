[comment]: # (“Commons Clause” License Condition v1.0)
[comment]: # ()  
[comment]: # (The Software is provided to you by the Licensor under the License,)
[comment]: # (as defined below, subject to the following condition.)
[comment]: # ()  
[comment]: # (Without limiting other conditions in the License, the grant of rights under the License)
[comment]: # (will not include, and the License does not grant to you, the right to Sell the Software.)
[comment]: # ()
[comment]: # (For purposes of the foregoing, “Sell” means practicing any or all of the rights granted)
[comment]: # (to you under the License to provide to third parties, for a fee or other consideration)
[comment]: # (including without limitation fees for hosting or consulting/ support services related to)
[comment]: # (the Software, a product or service whose value derives, entirely or substantially, from the)
[comment]: # (functionality of the Software. Any license notice or attribution required by the License)
[comment]: # (must also include this Commons Clause License Condition notice.)
[comment]: # ()
[comment]: # (Software: gweb)
[comment]: # ()
[comment]: # (License: Apache 2.0)
[comment]: # ()
[comment]: # (Licensor: Daniel Pfeifer, daniel.pfeifer@progotec.de)

GWeb  
====

GWeb is a hybrid Web application to run jobs for genomic analysis using [Genestrip](https://github.com/pfeiferd/genestrip).

## License

[GWeb is free for non-commercial use.](./LICENSE.txt) Please contact [daniel.pfeifer@progotec.de](mailto:daniel.pfeifer@progotec.de) if you are interested in a commercial license.

## Building and installing

GWeb is structured as a standard project for [Maven 3.5 or higher](https://maven.apache.org/) with modules and is compatible with the [JDK](https://jdk.java.net/) 11 or higher.
Generating the local variant of GWeb requires a Windows machine and uses [JLink](https://docs.oracle.com/en/java/javase/11/tools/jlink.html).
 
GWeb is implemented as a hybrid Web application such that 
* it can be run on a Servlet-based Web server (tested under Tomcat 10),
* it can be installed and executed entirely locally on a Windows machine.

### Web server variant

`mvn clean install` will compile and test GWeb and generate the `war` file `./gweb-ui/target/gweb.war`, which may by deployed under Tomcat for example.
The deployment requires a Postgres database (tested under Postgres 14) to be in reach under the JDBC URL `jdbc:postgresql://localhost/gweb` with a Postgres admin user `postgres` and password `postgres`.
You may configure this via the file [`./gweb-ui/src/main/webapp/META-INF/context.xml`](https://github.com/pfeiferd/gweb/blob/master/gweb-ui/src/main/webapp/META-INF/context.xml).

### Local variant

To build the local variant of GWeb, you must set the property `javapackager.jdkPath` in [`./gweb-launcher/pom.xml`](https://github.com/pfeiferd/gweb/blob/master/gweb-launcher/pom.xml) to the path of your corresponding JDK 11 (or higher) on a Windows system.
`mvn -P winexe clean install` will then (eventually) create the file
`./gweb-launcher/target/gweb-??-windows-x64.zip` which contains the entire local application (including a stripped but sufficient JRE).

To install `gweb-??-windows-x64.zip` just unzip it and the folder `gweb` will appear. Start the file `gweb/bin/GWeb.exe` via double click or from the command line from within the folder `gweb/bin`.
(You may place the folder `gweb` with its contents anywhere you want for this.)
`GWeb.exe` starts a local Web server on port 81 and opens the URL `http://localhost:81` in your standard Web browser. The UI of GWeb will appear with the `admin` user logged in by default.

The local installation uses a local, file-based [HSQL](https://hsqldb.org/) database, which stores its data under `gweb/data`. Any other application data, 
such as genomic databases and analysis files are also kept under `gweb/data`.
To migrate from one version of GWeb to the next, you may just backup and copy the folder `data` to a new installation folder `gweb`.
When started, the old application data should all be present again in the new installation.

A ready-made zip file of the local variant is available under [https://genestrip.it.hs-heilbronn.de/files/bin/](https://genestrip.it.hs-heilbronn.de/files/bin/).

### Help for End Users

More detailed installation instructions and basic usage information tailored to end users can be found here
[https://genestrip.it.hs-heilbronn.de/site/](https://genestrip.it.hs-heilbronn.de/site/) in English and
also here 
[https://genestrip.it.hs-heilbronn.de/site/de](https://genestrip.it.hs-heilbronn.de/site/de) in German.

## Functionality

GWeb's main functionality is to run jobs for genomic analysis using [Genestrip](https://github.com/pfeiferd/genestrip).

The main features are:
* creating and executing jobs to analyze fastq files,
* creating URLs and paths for downloading fastq files during analysis or placing them in the local file system,
* installing related genomic [Genestrip databases](https://github.com/pfeiferd/genestrip/blob/master/README.md#genestrip-databases) for use during analysis,
* creating user entries and assigning roles to them to handle user rights,
* creating person entries associated with users,
* viewing and downloading result files from analysis,
* viewing and downloading information files regarding Genestrip database contents.

### User roles

There are four user roles, where stronger roles subsume the rights of a weaker roles:
* "No Login" - the user must not login. This is mainly to deny a user access but keeping his application data in the system,
* "Viewer" - may view job results assigned to her / him and view Genestrip database information,
* "Job Executor" - may create and execute jobs and view related results but only based on her / his application data,
* "Admin" - may access and do everything, including user and person management.

### Default user

The system configuration allows for defining a default user who is logged in be default. Regarding the local variant of GWeb, 
the (initial) default user `admin` with password `admin` has the [role "Admin"](#user-roles). 
You may always logout the default user, which allows you to login as a different user.

### Supplying fastq files

There are four ways to supply a job with fastq files:
1. Via a URL added in the tab "Fastq Sources": The URL must point to a respective fastq file (with public access). 
The file will be [streamed for analysis and will not be stored on server](https://github.com/pfeiferd/genestrip/blob/master/README.md#reading-streaming-and-downloading-fastq-files).
2. Via a local folder on the server-side: This option is particularly useful under the [local variant of GWeb](#local-variant). 
The location of the folder is user-specific and can be displayed with a respective UI button in the job details.
Once a fastq file is placed inside the folder, GWeb will detect it and offer it in the UI for selection in the job details.
3. [When configured accordingly](#configuration), users under the [roles "Admin" or "Job Executor"](#user-roles) may set server-side paths to fastq files in the 
tab "Fastq Sources". This option supports [glob patterns for file names of paths as described here](https://github.com/pfeiferd/genestrip/blob/master/README.md#usage-and-goals).
All fastq files matching the path pattern will be jointly analyzed. Obviously, 
this requires fastq files to be present in respective locations on the server-side. This option is particularly useful under the [local variant of GWeb](#local-variant).
4. [When configured accordingly](#configuration), users under the [roles "Admin" or "Job Executor"](#user-roles) may upload of an arbitrary number of fastq files per job from the client-side. 
The files will be [streamed for analysis and will not be stored on the server](https://github.com/pfeiferd/genestrip/blob/master/README.md#reading-streaming-and-downloading-fastq-files).
This option is only enabled, when no other jobs are pending or being processed on the server-side. 
*The browser tab, where the upload job was started, must remain 
open until the respective job has finished, because otherwise, the upload request supplying the data stream will be closed by the browser itself.*

Regarding options 1 to 3, no more than two entries for fastq files may be added per job. All matching fastq files will be jointly analyzed during the corresponding job.

## Technical documentation

### Basic architecture

GWeb is light-weight and uses no frameworks. On the server-side, it is based on the following Java-related technologies:
* Servlets and JSP (based on the [Jakarta Servlet](https://github.com/jakartaee/servlet) API),
* JDBC (using SQL-statements compatible with Postgres and HSQL),
* JAX-RS (using the [Jersey reference implementation](https://de.wikipedia.org/wiki/Eclipse_Jersey) as a pure library),
* [Genestrip](https://github.com/pfeiferd/genestrip/) with its dependent libraries.

GWeb (re-)uses worker threads for executing Jobs on the server-side. It queues jobs using a [Timer](https://www.baeldung.com/java-timer-and-timertask) such that only one job is executed at a time. To do so,
[Genestrip is used as a library](https://github.com/pfeiferd/genestrip/blob/master/README.md#api-based-usage) and invoked in-process. (This implies that your Web server may require a lot of heap but also CPU time during analysis.)
The number of worker threads is [configurable](#configuration).

On the client-side GWeb is based on HTML 5, CSS and JavaScript without additional JavaScript libraries.
The GUI is a thin client without page reloads. Data exchange with the server works almost entirely via [REST](https://en.wikipedia.org/wiki/REST).
The GUI was tested for compatibility on several modern Web browsers.

### Location of application data

Application data including
* installed Genestrip databases,
* analysis files from jobs,
* log files from jobs and
* database information files

are stored in the folder `data` (or in respective sub-folders). The path to `data` must be [configured](#configuration).
For the local variant of GWeb the `data` folder is in the [installation folder of `gweb`](#local-variant).

All other application data is stored in a relational database (using Postgres or HSQL).
For the local variant of GWeb the HSQL database files are [also under the folder the `data`](#local-variant).

### Configuration

The configuration of the Web server variant must all be done in [`./gweb-ui/src/main/webapp/WEB-INF/web.xml`](https://github.com/pfeiferd/gweb/blob/master/gweb-ui/src/main/webapp/WEB-INF/web.xml) via
`init-param` settings (for the Servlet `...GenestripRestApplication`) and some `context-param` settings. 
The [shipped `web.xml`](https://github.com/pfeiferd/gweb/blob/master/gweb-ui/src/main/webapp/WEB-INF/web.xml) shows where to place these settings accordingly. 

The local variant of GWeb can be configured in [`./gweb-local/src/main/webapp/WEB-INF/web.xml`](https://github.com/pfeiferd/gweb/blob/master/gweb-local/src/main/webapp/WEB-INF/web.xml) but that is rarely necessary.

Obviously, rebuilding and reinstalling is required afterwards.

The following table describes the settings.

| Name        | Category | Type | Default Value | Description |
| ----------- | -------- | ---- | ------------- | ----------- |
| `localInstall` | `context-param` | `boolean` | `false` | Whether users not in an ["Admin" role](#user-roles) may see the server-side path to their fastq file folder. This should only be set to true for the [local variant of GWeb](#local-variant) |
| `defaultUser` | `init-param` | `String` | `null` | Login name of the [default user](default-user) that is automatically logged in when accessing the GWeb UI. For this to work, the default user must have a database entry with identical login and password. If not set, the default user functionality is off. | 
| `initDefaultUser` | `init-param` | `boolean` | `true` | Whether the [default user](default-user) should automatically be created with the [role "Job Viewer"](#user-roles) during Servlet initialization. If the default user already exists, it will neither be created nor changed. | 
| `jobDelay` | `init-param` | long | 1000 | Time in ms to check the job queue for the execution of the next job (when idle). |
| `jobPeriod` | `init-param` | long | 10000 | Waiting time in ms for the job queue to execute the next job after the former job was finished. |
| `initAdmin` | `init-param` | `boolean` | `true` |  Whether the user `admin` with password `admin` and [role "Admin"](#user-roles) should automatically be created during Servlet initialization. If the user `admin` already exists, it will neither be created nor changed. |
| `genestripBaseDir` | `init-param` | String | `./data` | The server-side path to [the folder `data`](#location-of-application-data). It must exist upfront and the Web server must have write access to it. |
| `sqlDialect` | `init-param` | `String` | `POSTGRES` | Only `HSQL` and `POSTGRES` are valid values and must match the connected database system type. | 
| `threads` | `init-param` | `integer` | `-1` | The number of [consumer threads of Genestrip](https://github.com/pfeiferd/genestrip/blob/master/ConfigParams.md) used during analysis of fastq files. | 
| `initDBs` | `init-param` | `boolean` | `true` | Whether to create a set of URL entries for Genestrip databases as given by [Genestrip DB](https://github.com/pfeiferd/genestrip-db). The entries will be added as part of the initial database schema creation only. (Installation must still be done manually by clicking a respective UI button.) |
| `filePathRole` | `context-param` | String | `null` | The minimum [role](#user-roles) in which a user can set [server-side paths to fastq files in the UI's "Fastq Sources" tab](#supplying-fastq-files-and-genestrip-databases). Possible values are `ADMIN` and `RUN_JOBS`. If not set, the corresponding feature is disabled. |
| `uploadPathRole` | `context-param` | String | `null` | The minimum [role](#user-roles) in which a user can [upload to fastq files in the UI's "Jobs" tab](#supplying-fastq-files-and-genestrip-databases). Possible values are `ADMIN` and `RUN_JOBS`. If not set, the corresponding feature is disabled. |
