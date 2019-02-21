# Simutool monitoring tool

## Building application


### Cloning the repository

If you get errors due to long file names, run `git config --system core.longpaths true` in prompt with Administrator permissions to make git ignore this issue.


### Build Frontend

Make sure you have nodejs installed. Navigate to `online-monitoring-client\src\github.com\grafana\grafana` and run in prompt:
```bash
npm install -g yarn
yarn install --pure-lockfile
yarn watch
```
Do not close prompt if you want yarn to recompile code on runtime every time you save any changes.


### Start app
The app is launched by
`java-app\src\main\java\simutool\app\StartApp.java`