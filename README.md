# Simutool monitoring tool


Distribution package (Windows x64):

https://github.com/simutool/online-monitoring-tool/blob/master/online-monitoring.zip

User manual:

https://github.com/simutool/online-monitoring-tool/blob/master/user_manual.pdf


## Development

#### Structure
The app consists of three major parts: 
- Spring Boot app (`/java-app` folder)
- Grafana (`/src` folder) 
- influx database (`/influxdb` folder).

Spring Boot manages file input/output and passes data to influx. Grafana pulls data from influx for visualisation.


#### Cloning the repository

If you get errors due to long file names, run `git config --system core.longpaths true` in prompt with Administrator permissions to make git ignore this issue.

Grafana credentials for this project are preconfigured:<br>
**Username**: admin<br>
**Password**: 12345


#### Frontend

Frontend assets of first two (non-grafana) pages are withing Spring Boot project (`java-app\src\main\resources`).
All the rest files are under `src\github.com\grafana\grafana\public`.

You will need to install several node dependencies. Make sure you have nodejs installed. Navigate to `src\github.com\grafana\grafana` and run in prompt:

```bash
npm install -g yarn
yarn install --pure-lockfile
yarn watch
```
Do not close prompt if you want yarn to recompile code on runtime every time you save any changes.

Entry point is `java-app\src\main\java\simutool\app\StartApp.java`


#### Build production package

1. in `application.properties` set BUILD_DIR variable empty. This line shall look like `BUILD_DIR = `
2. Run app with Maven Build configuration, setting goals to `package spring-boot:repackage`
3. At `java-app\target` find JAR file `influxdb-java-2.14-SNAPSHOT.jar`, convert it to .exe using any available tool (*Launch4j* recommended) and put into project root
4. Remove folders `java-app` and `src\github.com\grafana\grafana\node_modules` to reduce package size

#### Start app
The app is launched by `online-monitoring-system.exe` that is in the project root and runs on `http://localhost:8090`
App also uses ports `:8080` and `:8086`, make sure they are all free.

