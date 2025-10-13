@echo off

cd /D "%~dp0"
set "BASEDIR=%cd%"

set "srcDir=src"
set "destDir=%BASEDIR%\dest"
set "libDir=lib"
echo "%destDir%"
if "x%JAVA_HOME%" == "x" (
    set JAVAC="javac"
    set JAR="jar"
) else (
    set "JAVAC=%JAVA_HOME%\bin\javac"
    set "JAR=%JAVA_HOME%\bin\jar"
)

set "CLASSPATH=%BASEDIR%\conf;%BASEDIR%\rubikAutoPlayer.jar"
SETLOCAL ENABLEDELAYEDEXPANSION
for %%I in ("lib\*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%I"
)
for %%I in ("*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%I"
)
SETLOCAL DISABLEDELAYEDEXPANSION

:: clean target
if exist "%destDir%" (
    rmdir /s /q "%destDir%"
)

:: build

mkdir "%destDir%"

dir "%srcDir%"\*.java /s/b > "%destDir%"\srclist.txt
echo "javac -encoding utf-8 -d %destDir% -classpath %CLASSPATH% %srcDir%\*.java"
"%JAVAC%" -encoding utf-8 -d "%destDir%" -classpath "%CLASSPATH%" @"%destDir%"\srclist.txt
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)
del /f /q "%destDir%"\srclist.txt

:: clean jar
if exist "%BASEDIR%\rubikAutoPlayer.jar" (
    del /f /q "%BASEDIR%\rubikAutoPlayer.jar"
)

:: jar
echo "jar cfm FileSync.jar META-INF/MANIFEST.MF -C %destDir% com"
"%JAR%" cfm "%BASEDIR%\rubikAutoPlayer.jar" "%BASEDIR%\META-INF\MANIFEST.MF" -C "%destDir%" .
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)
rmdir /s /q "%destDir%"


