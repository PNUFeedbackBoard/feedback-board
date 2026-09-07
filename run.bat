@echo off
REM 서버와 화면을 동시에 띄운다. 창이 두 개 열린다.
REM 서버  http://localhost:8080
REM 화면  http://localhost:5173   <- 브라우저로 여기 들어가면 된다
start "backend"  cmd /k "cd /d %~dp0backend && gradlew.bat bootRun"
start "frontend" cmd /k "cd /d %~dp0frontend && npm run dev"
