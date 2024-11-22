@echo off
setlocal enabledelayedexpansion

:: Define the log file
set LOG_FILE=error.log

:: Clear the log file at the start
echo > %LOG_FILE%

:: Check if no arguments are provided
if "%1"=="" (
    echo No command provided. Use "run" or "build."
    echo No command provided. >> %LOG_FILE%
    exit /b 1
)

:: Extract the first argument
set ACTION=%1
shift

:: Combine all remaining arguments into a single variable
set OTHER_ARGS=
:loop
if "%1"=="" goto done
set OTHER_ARGS=!OTHER_ARGS! %1
shift
goto loop
:done

:: Handle "run" and "build"
if "%ACTION%"=="build" (
    echo Running Gradle build with arguments: %OTHER_ARGS%
    gradle clean build %OTHER_ARGS% 2>> %LOG_FILE%
    if errorlevel 1 (
        echo Gradle build failed. Check %LOG_FILE% for details.
    )
    exit /b
)

if "%ACTION%"=="run" (
    echo Running Gradle run with arguments: %OTHER_ARGS%
    gradle runClient %OTHER_ARGS% 2>> %LOG_FILE%
    if errorlevel 1 (
        echo Gradle run failed. Check %LOG_FILE% for details.
    )
    exit /b
)

:: Default case
echo Invalid action: %ACTION%
echo Invalid action: %ACTION% >> %LOG_FILE%
exit /b 1
