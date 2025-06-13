#!/bin/bash

# Genetic Algorithm Runner for macOS/Linux
# ----------------------------------------

# Configuration
JAVA_HOME="/usr/lib/jvm/default-java"  # Update to your Java path
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"  # Script directory
PYTHON_CMD="python3"  # Use python3 explicitly
VIS_SCRIPT="visualize_GeneticAlgorithmLogs.py"

# Header
echo "----------------------------------------"
echo "   Running Genetic Algorithm Project"
echo "----------------------------------------"

# Step 1: Compile Java
echo -e "\nStep 1/3: Compiling Java..."
find "${PROJECT_DIR}/src" -name "*.java" > sources.txt
javac -encoding UTF-8 -d "${PROJECT_DIR}/bin" @sources.txt

if [ $? -ne 0 ]; then
    echo "!!! COMPILATION FAILED !!!"
    rm sources.txt
    exit 1
fi
rm sources.txt

# Step 2: Run Java and capture output
echo -e "\nStep 2/3: Running genetic algorithm..."
CSV_PATH=""
while IFS= read -r line; do
    echo "$line"
    if [[ "$line" == CSV_PATH:* ]]; then
        CSV_PATH="${line#CSV_PATH:}"
    fi
done < <(java -cp "${PROJECT_DIR}/bin" Genetic_Algorithm)

# Step 3: Verify and run visualization
echo -e "\nStep 3/3: Running visualization..."
if [ -z "$CSV_PATH" ]; then
    echo "!!! ERROR: CSV path not captured !!!"
    echo "Check if Java output contains 'CSV_PATH:' marker"
    exit 1
fi

# Convert Windows paths to Unix if needed
CSV_PATH=$(echo "$CSV_PATH" | tr '\\' '/')

echo "CSV file: \"$CSV_PATH\""
echo "Python script: \"${PROJECT_DIR}/${VIS_SCRIPT}\""

# Verify Python script exists
if [ ! -f "${PROJECT_DIR}/${VIS_SCRIPT}" ]; then
    echo "ERROR: Python script not found. Creating placeholder..."
    cat > "${PROJECT_DIR}/${VIS_SCRIPT}" << EOF
import sys
print("Placeholder visualization script")
print(f"Received CSV file: {sys.argv[1]}")
# Add your visualization code here
EOF
    chmod +x "${PROJECT_DIR}/${VIS_SCRIPT}"
fi

# Run visualization
$PYTHON_CMD "${PROJECT_DIR}/${VIS_SCRIPT}" "$CSV_PATH"

echo "----------------------------------------"
echo "Operations complete."