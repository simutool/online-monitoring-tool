# Notes on starting application:

1. Influx server has to be installed and launched separately.
by default it runs on port 8086, otherwise port needs to be specified in config file under:
##### java-app\src\main\resources\application.properties

2. Grafana server is started by
##### src\github.com\grafana\grafana\bin\grafana-server.exe

3. Then the app itself is launched by (runs on port 8090)
##### java-app\src\main\java\simutool\app\StartApp.java