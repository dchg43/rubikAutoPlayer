@echo off

cd /D "%~dp0"
set "BASEDIR=%cd%"

set APP_NAME=rubikAutoPlayer
set "srcDir=%BASEDIR%\src"
set "libDir=%BASEDIR%\lib"
set "destDir=%BASEDIR%\jardest"
set "nativeImageAgentDir=%BASEDIR%\META-INF\native-image-new"

set "graalvmPath=E:\tools\GraalVM-Native-Image\graalvm-ce-java17-22.3.3"
set "JAVA=%graalvmPath%\bin\java"
set "JAVAC=%graalvmPath%\bin\javac"
set "JAR=%graalvmPath%\bin\jar"
set "NATIVE_IMAGE=%graalvmPath%\bin\native-image.cmd"


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
echo 编译：javac -encoding utf-8 -d %destDir% -classpath %CLASSPATH% %srcDir%\*.java
"%JAVAC%" -encoding utf-8  -d "%destDir%" -classpath "%CLASSPATH%" @"%destDir%"\srclist.txt
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
xcopy /e /q /y "%BASEDIR%\META-INF\native-image\" "%destDir%\META-INF\native-image\"
copy "%BASEDIR%\resources\ico.png" "%destDir%\"
echo 打包jar：jar cfm %APP_NAME%.jar META-INF/MANIFEST.MF -C %destDir% com
"%JAR%" cfm "%BASEDIR%\%APP_NAME%.jar" "%BASEDIR%\META-INF\MANIFEST.MF" -C "%destDir%" .
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)
rmdir /s /q "%destDir%"


echo 生成native-image-conf，需要运行程序生成
del /f /q "%nativeImageAgentDir%\.lock" 2>nul
for /f "delims=" %%I in ('dir /B "%nativeImageAgentDir%\"^|findstr "agent-pid"') do (
    rmdir /s /q "%nativeImageAgentDir%\%%I"
)
set "createOrMerge=config-merge-dir"
if not exist "%nativeImageAgentDir%" (
    mkdir "%nativeImageAgentDir%"
    set "createOrMerge=config-output-dir"
)
start %JAVA% -agentlib:native-image-agent=%createOrMerge%="%nativeImageAgentDir%" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.region=US -jar "%BASEDIR%\%APP_NAME%.jar" --autoTest 10 --display true -backgroundImage "%systemroot%\Web\Wallpaper\Windows\img0.jpg" -h
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)

