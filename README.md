The online monitoring solution is a software component responsible to integrate data from sensors during production experiments (thermocouples or any other sensors used) with simulation data as well as the curing cycle data into a unified visual interactive tool. Its results and data logs are meant to be saved and added to the main knowledge base. It is part of the [SIMUTOOL Knowledge Management Suite](https://github.com/simutool/general) of software, for more information about the project please visit that link.

# Installation

To run the software there are two options, one for software developers and one for end users. The usual option will  be to install directly using a distribution package online-monitoring.zip for Windows(x64), see below. Unzip it and run online-monitoring-system.exe to start the app. For software developers, they can built the solution from source code using the instructions provided in the README.md file and manual included with the software package. 

Distribution package (Windows x64):

https://github.com/simutool/online-monitoring-tool/blob/master/online-monitoring.zip

User manual:

https://github.com/simutool/online-monitoring-tool/blob/master/user_manual.pdf


## Development Buidling Instructions

##### Prerequisites:

1. nodejs - [download](https://nodejs.org/en/download/)
2. Eclipse (Neon or newer) with Maven support - [download](https://www.eclipse.org/downloads/packages/release/neon/3)
3. Launch4j - [download](https://sourceforge.net/projects/launch4j/files/latest/download)
4. JDK 8 (just JRE is not sufficient) - [download](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 
#### Structure
The app consists of three major parts: 
- Spring Boot app (`/java-app` folder)
- Grafana (`/src` folder) 
- influx database (`/influxdb` folder).

Spring Boot manages file input/output and passes data to influx. Grafana pulls data from influx for visualisation.
#### Step 1:  Clone the repository

If you get errors due to long file names, run `git config --system core.longpaths true` in prompt with Administrator permissions to make git ignore this issue.


#### Step 2: Build Frontend

Frontend assets of first two (non-grafana) pages are withing Spring Boot project (`java-app\src\main\resources`).
All the rest files are under `src\github.com\grafana\grafana\public`.

Navigate to `src\github.com\grafana\grafana` and run in prompt:

```bash
npm install -g yarn
yarn install --pure-lockfile
yarn watch
```
Do not close prompt if you want yarn to recompile code on runtime every time you save any changes.

#### Step 3: Import and run app
Import folder `/java-app` to Eclipse as existing Maven project and run it as Java Application or Spring Boot Application.

Entry point is `java-app\src\main\java\simutool\app\StartApp.java`

If grafana or influxdb will request special permissions, grant them.

Default browser will launch automatically. If the page is not displayed correctly in some browser, start it manually by entering `http://localhost:8090` in Chrome.


#### Step 4: Build production package

1. in `application.properties` set BUILD_DIR variable empty. This line shall look like `BUILD_DIR = `
2. Create a new Maven Build configuration, choosing `influxdb-java` as main directory and setting goals to `package spring-boot:repackage`. In `JRE` tab choose JDK folder as execution environment (add JDK as new environment if it is not available yet). Run configuration.
3. At `java-app\target` find JAR file `influxdb-java-2.14-SNAPSHOT.jar`, convert it to .exe called `online-monitoring-system.exe` using any available tool (*Launch4j* recommended) and put into project root
4. Remove folders `java-app` and `src\github.com\grafana\grafana\node_modules` to reduce package size
5. The app is launched by `online-monitoring-system.exe` that is in the project root and runs on `http://localhost:8090`
App also uses ports `:8080` and `:8086`, make sure they are all free.

Grafana credentials for this project are preconfigured:<br>
**Username**: admin<br>
**Password**: 12345


## Authors

* Valentyna Voronova - Development
* Nasr Kasrin - Project Management
* Daniela Nicklas - Lead, Architecture

