@echo off

cd /D "%~dp0"
set "BASEDIR=%cd%"

set APP_NAME=rubikAutoPlayer
set "srcDir=%BASEDIR%\src"
set "libDir=%BASEDIR%\lib"
set "destDir=%BASEDIR%\%APP_NAME%"

set "VisualStudioDir=C:\Program Files\Microsoft Visual Studio\2022\Professional"
set "VisualStudioKits=C:\Program Files (x86)\Windows Kits\10"
set "MSVC_NATIVE_TOOLS=%VisualStudioDir%\VC\Auxiliary\Build\vcvars64.bat"
set "MSVC_EDITBIN=%VisualStudioDir%\VC\Tools\MSVC\14.44.35207\bin\Hostx64\x64\editbin.exe"
set "SDK_KITS_MT=%VisualStudioKits%\bin\10.0.19041.0\x64\mt.exe"

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
echo ���룺javac -encoding utf-8 -d %destDir% -classpath %CLASSPATH% %srcDir%\*.java
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
echo ���jar��jar cfm FileSync.jar META-INF/MANIFEST.MF -C %destDir% com
"%JAR%" cfm "%BASEDIR%\%APP_NAME%.jar" "%BASEDIR%\META-INF\MANIFEST.MF" -C "%destDir%" .
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)
rmdir /s /q "%destDir%"


echo ����native-image-conf����Ҫ���г�������
del /f /q "%BASEDIR%\META-INF\native-image\.lock" 2>nul
for /f "delims=" %%I in ('dir /B "%BASEDIR%\META-INF\native-image\"^|findstr "agent-pid"') do (
    rmdir /s /q "%BASEDIR%\META-INF\native-image\%%I"
)
start %JAVA% -Dfile.encoding=utf-8 -agentlib:native-image-agent=config-merge-dir="%BASEDIR%\META-INF\native-image" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.region=US -jar "%BASEDIR%\%APP_NAME%.jar" --display true -backgroundImage "%systemroot%\Web\Wallpaper\Windows\img0.jpg"
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)

echo �ȴ�һ��ʱ�䲢��������
timeout /nobreak /T 5 >nul
::for /f "tokens=2" %%a in ('tasklist /fi "IMAGENAME eq java.exe"^|findstr /i "java.exe"') do taskkill /pid:%%a >nul
taskkill /fi "IMAGENAME eq java.exe" >nul


::jpackage --type app-image --name spring --input target --main-jar spring-1.0.jar --win-console --dest dist
::jpackage --input . --name %APP_NAME% --main-jar %APP_NAME%.jar --win-console --win-shortcut

timeout /nobreak /T 1 >nul
del /f /q "%BASEDIR%\META-INF\native-image\.lock" 2>nul
for /f "delims=" %%I in ('dir /B "%BASEDIR%\META-INF\native-image\"^|findstr "agent-pid"') do (
    rmdir /s /q "%BASEDIR%\META-INF\native-image\%%I"
)

mkdir "%destDir%"
cd "%destDir%"

echo ʹ��VS�����exe�ļ�
::--no-fallback ����������jvm��native image����ʾ����ʧ��
::-H:EnableURLProtocols�����������ñ�Ҫ������Э��֧��
::-H:+ReportExceptionStackTraces ��ʾ�����쳣�Ķ�ջ
::--link-at-build-time �ڹ���ʱ������Ͱ������Ӵ���
::-H:ConfigurationFileDirectories ���òɼ�����meta��Ϣ��·��
::-H:+AddAllCharsets ֧�������ַ�������ֹ��������
call "%MSVC_NATIVE_TOOLS%"
call "%NATIVE_IMAGE%" "--no-fallback" ^
    "-H:ConfigurationFileDirectories=%BASEDIR%\META-INF\native-image" ^
    "-H:+ReportExceptionStackTraces" ^
    "-H:+AddAllCharsets" ^
    "-H:Name=%APP_NAME%" ^
    "--enable-url-protocols=http,https" ^
    "--install-exit-handlers" ^
    "--link-at-build-time" ^
    "--enable-preview" ^
    "--verbose" ^
    "-O1" ^
    "-jar" "%BASEDIR%\%APP_NAME%.jar"
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)

echo ���������ļ�
del /f /q "%destDir%\%APP_NAME%.build_artifacts.txt" >nul 2>nul
mkdir "%destDir%\lib\"
copy "%BASEDIR%\lib\fontconfig.bfc" "%destDir%\lib\"

echo ȥ�������д���
"%MSVC_EDITBIN%" /subsystem:windows "%destDir%\%APP_NAME%.exe"
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)

echo ����ϵͳdpi���Ÿ�֪
"%SDK_KITS_MT%" -manifest "%BASEDIR%\META-INF\rubikAutoPlayer.exe.manifest" -outputresource:"%destDir%\%APP_NAME%.exe;#1"
if not "%errorlevel%" == "0" (
    pause
    exit /b %errorlevel%
)

