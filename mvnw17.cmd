@echo off
setlocal

set "JAVA_HOME=E:\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call mvnw.cmd %*

endlocal