@echo off
TITLE Reddit-Wallpaper old version uninstaller

@echo Welcome to the command line uninstalling utility of Reddit-Wallpaper for versions 0.3.2 and lower.
@echo !!! - If RW is active, please first close it. - !!!


@echo[
:question
SET /P answer="Press Y to begin. N to cancel (Y/N) "
@echo[

if /i {%answer%}=={Y} (
    @echo Uninstalling...
) else if /i {%answer%}=={N} (
    @echo Aborting...
    PAUSE
    exit /b 1
) else (
    goto :question
)

:rwfolder
if exist "C:\ProgramData\Reddit Wallpaper\" (
  @echo Old installation directory found, deleting...
  @RD /S /Q "C:\ProgramData\Reddit Wallpaper\" || REM
  if not %errorlevel%==0 (
      @echo !!!!!! - RW is still active, can't delete the files, please close it first
      PAUSE
      goto :rwfolder
  )
) else (
  @echo !! - Old installation directory not found
  PAUSE
)

:rwautostart
if exist "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\autostartRW.bat" (
  @echo Link for auto-startup found, deleting...
  DEL "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\autostartRW.bat"
  if not %errorlevel%==0 (
       @echo !!!!!! - RW is still active, can't delete the file, please close it first
       PAUSE
       goto :rwautostart
  )
) else (
  @echo !! - Link for auto-startup not found
  PAUSE
)


@echo Completed succesfully
PAUSE

