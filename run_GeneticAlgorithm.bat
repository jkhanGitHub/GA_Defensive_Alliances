@echo off
setlocal enabledelayedexpansion

:: Get script directory
set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

title Genetic Algorithm Runner
echo ----------------------------------------
echo    Running Genetic Algorithm Project
echo ----------------------------------------

:: 1. Compile Java
echo Step 1/3: Compiling Java...
cd /d "%SCRIPT_DIR%\src"
javac -encoding UTF-8 -d "%SCRIPT_DIR%\bin" *.java
if errorlevel 1 (
    echo !!! COMPILATION FAILED !!!
    pause
    exit /b
)

:: Return to project root
cd /d "%SCRIPT_DIR%"

:: 2. Run Java and capture output
echo Step 2/3: Running genetic algorithm...
set "output_file=%temp%\ga_output.txt"
java -cp bin Genetic_Algorithm > "%output_file%" 2>&1

:: 3. Display output and capture CSV path
set "CSV_PATH="
for /f "usebackq delims=" %%i in ("%output_file%") do (
    echo %%i
    set "line=%%i"
    if "!line:CSV_PATH:=!" neq "!line!" (
        set "CSV_PATH=!line:CSV_PATH:=!"
    )
)

:: Cleanup
del "%output_file%"

:: 4. Check if path was captured
if not defined CSV_PATH (
    echo !!! ERROR: CSV path not captured !!!
    echo Check if Java output contains "CSV_PATH:" marker
    pause
    exit /b
)

:: 5. Run visualization - WITH ROBUST PATH HANDLING
echo Step 3/3: Running visualization...
echo CSV file: "!CSV_PATH!"
echo Project directory: "%SCRIPT_DIR%"


:: Run with explicit path
echo Executing Python...
echo ----------------------------------------
python "%SCRIPT_DIR%\visualize_GeneticAlgorithmLogs.py" "!CSV_PATH!"

:: 6. Keep window open
echo ----------------------------------------
echo Operations complete. Press any key to exit.
pause >nul
endlocal