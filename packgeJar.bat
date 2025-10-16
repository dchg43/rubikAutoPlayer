@echo off

cd /D "%~dp0"
set "BASEDIR=%cd%"

set APP_NAME=rubikAutoPlayer
set "srcDir=%BASEDIR%\src"
set "libDir=%BASEDIR%\lib"
set "destDir=%BASEDIR%\jardest"

if "x%JAVA_HOME%" == "x" (
    set JAVAC="javac"
    set JAR="jar"
) else (
    set "JAVAC=%JAVA_HOME%\bin\javac"
    set "JAR=%JAVA_HOME%\bin\jar"
)

set "CLASSPATH=%BASEDIR%\conf;%BASEDIR%\%APP_NAME%.jar"
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
if exist "%BASEDIR%\%APP_NAME%.jar" (
    del /f /q "%BASEDIR%\%APP_NAME%.jar"
)

:: jar
copy "%BASEDIR%\resources\*" "%destDir%\"
echo "jar cfm %APP_NAME%.jar META-INF/MANIFEST.MF -C %destDir% com"
"%JAR%" cfm "%BASEDIR%\%APP_NAME%.jar" "%BASEDIR%\META-INF\MANIFEST.MF" -C "%destDir%" .
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)
rmdir /s /q "%destDir%"


