@echo off

cd /D "%~dp0"
set "BASEDIR=%cd%"

set "srcDir=src"
set "destDir=%BASEDIR%\dest"
set "libDir=lib"

set "graalvmPath=E:\tools\GraalVM-Native-Image\graalvm-ce-java17-22.3.3"
set "JAVA=%graalvmPath%\bin\java"
set "JAVAC=%graalvmPath%\bin\javac"
set "JAR=%graalvmPath%\bin\jar"
set "NATIVE=%graalvmPath%\bin\native-image.cmd"


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
"%JAVAC%" -encoding utf-8  -d "%destDir%" -classpath "%CLASSPATH%" @"%destDir%"\srclist.txt
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
xcopy /e /q /y "%BASEDIR%\META-INF\native-image\" "%destDir%\META-INF\native-image\"
echo "jar cfm FileSync.jar META-INF/MANIFEST.MF -C %destDir% com"
"%JAR%" cfm "%BASEDIR%\rubikAutoPlayer.jar" "%BASEDIR%\META-INF\MANIFEST.MF" -C "%destDir%" .
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)


echo 生成native-image-conf，需要运行程序生成
del /f /q "%BASEDIR%\META-INF\native-image\.lock" 2>nul
for /f "delims=" %%I in ('dir /B "%BASEDIR%\META-INF\native-image\"^|findstr "agent-pid"') do (
    rmdir /s /q "%BASEDIR%\META-INF\native-image\%%I"
)
start %JAVA% -Dfile.encoding=utf-8 -agentlib:native-image-agent=config-merge-dir="%BASEDIR%\META-INF\native-image" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.region=US -jar "%BASEDIR%\rubikAutoPlayer.jar" --display true -backgroundImage "%systemroot%\Web\Wallpaper\Windows\img0.jpg"

rem 等待一段时间并结束进程
timeout /nobreak /T 5 >nul
::for /f "tokens=2" %%a in ('tasklist /fi "IMAGENAME eq java.exe"^|findstr /i "java.exe"') do taskkill /pid:%%a >nul
taskkill /fi "IMAGENAME eq java.exe" >nul


::jpackage --type app-image --name spring --input target --main-jar spring-1.0.jar --win-console --dest dist
::jpackage --input . --name rubikAutoPlayer --main-jar rubikAutoPlayer.jar --win-console --win-shortcut

timeout /nobreak /T 1 >nul
del /f /q "%BASEDIR%\META-INF\native-image\.lock" 2>nul
for /f "delims=" %%I in ('dir /B "%BASEDIR%\META-INF\native-image\"^|findstr "agent-pid"') do (
    echo "%BASEDIR%\META-INF\native-image\%%I"
    rmdir /s /q "%BASEDIR%\META-INF\native-image\%%I"
)
rmdir /s /q "%destDir%"
mkdir "%destDir%"
cd "%destDir%"


::--no-fallback 构建不依赖jvm的native image或显示构建失败
::-H:EnableURLProtocols参数用于启用必要的网络协议支持
::-H:+ReportExceptionStackTraces 显示构建异常的堆栈
::--link-at-build-time 在构建时报告类和包的链接错误
::-H:ConfigurationFileDirectories 配置采集到的meta信息的路径
::-H:+AddAllCharsets 支持所有字符集，防止中文乱码
call "C:\Program Files\Microsoft Visual Studio\2022\Professional\VC\Auxiliary\Build\vcvars64.bat"
call "%NATIVE%" "--no-fallback" ^
    "-H:ConfigurationFileDirectories=%BASEDIR%\META-INF\native-image" ^
    "-H:+ReportExceptionStackTraces" ^
    "-H:+AddAllCharsets" ^
    "-H:Name=rubikAutoPlayer" ^
    "--enable-url-protocols=http,https" ^
    "--install-exit-handlers" ^
    "--link-at-build-time" ^
    "--enable-preview" ^
    "--verbose" ^
    "-O1" ^
    "-jar" "%BASEDIR%\rubikAutoPlayer.jar"

::复制依赖文件
del /f /q "%destDir%\rubikAutoPlayer.build_artifacts.txt" >nul 2>nul
mkdir "%destDir%\lib\"
copy "%BASEDIR%\lib\fontconfig.bfc" "%destDir%\lib\"

::去除命令行窗口
editbin /subsystem:windows "%destDir%\rubikAutoPlayer.jar" 2>nul

