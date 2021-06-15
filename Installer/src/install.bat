@echo off
TITLE Installing Reddit Wallpaper
@echo  _____  ______ _____  _____ _____ _______  __          __     _      _      _____        _____  ______ _____
@echo ^|  __ \^|  ____^|  __ \^|  __ \_   _^|__   __^| \ \        / /\   ^| ^|    ^| ^|    ^|  __ \ /\   ^|  __ \^|  ____^|  __ \
@echo ^| ^|__) ^| ^|__  ^| ^|  ^| ^| ^|  ^| ^|^| ^|    ^| ^|     \ \  /\  / /  \  ^| ^|    ^| ^|    ^| ^|__) /  \  ^| ^|__) ^| ^|__  ^| ^|__) ^|
@echo ^|  _  /^|  __^| ^| ^|  ^| ^| ^|  ^| ^|^| ^|    ^| ^|      \ \/  \/ / /\ \ ^| ^|    ^| ^|    ^|  ___/ /\ \ ^|  ___/^|  __^| ^|  _  /
@echo ^| ^| \ \^| ^|____^| ^|__^| ^| ^|__^| ^|^| ^|_   ^| ^|       \  /\  / ____ \^| ^|____^| ^|____^| ^|  / ____ \^| ^|    ^| ^|____^| ^| \ \
@echo ^|_^|  \_\______^|_____/^|_____/_____^|  ^|_^|        \/  \/_/    \_\______^|______^|_^| /_/    \_\_^|    ^|______^|_^|  \_\
@echo Welcome to the command line installation program of Reddit Wallpaper.
@echo If you are updating RW please first close it.
@echo[
:question
SET /P answer="Press Y to begin installation. N to cancel (Y/N) "
@echo[

if /i {%answer%}=={Y} (
    @echo This program is based on Java 11. Searching Java 11...
) else if /i {%answer%}=={N} (
    @echo Aborting...
    PAUSE
    exit /b 1
) else (
    goto :question
)

where java >nul 2>nul
if %errorlevel%==1 (
    @echo !! - Java not found. Install Java 11.
    @echo Recommended JRE https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jre_x64_windows_hotspot_11.0.10_9.msi
    exit
)

SET isJavaInstalled = "$(java -version)"
echo isJavaInstalled|find "11.0" >nul
if errorlevel 1 (
    @echo Java11 found
    @echo[
    java -jar install.jar
    @echo[
) else (
    @echo !! - Java Version not compatible. Install Java 11
    @echo Recommended JRE https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jre_x64_windows_hotspot_11.0.10_9.msi
)
PAUSE
start "" "C:\Users\%USERNAME%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\autostartRW.bat"
exit
