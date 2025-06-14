#!/bin/bash

# Exit immediately on error
set -e

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "----------------------------------------"
echo "   Running Genetic Algorithm Project"
echo "----------------------------------------"

# 1. Compile Java
echo "Step 1/3: Compiling Java..."
cd "$SCRIPT_DIR/src"
javac -encoding UTF-8 -d "$SCRIPT_DIR/bin" *.java

# Return to project root
cd "$SCRIPT_DIR"

# 2. Run Java and capture output
echo "Step 2/3: Running genetic algorithm..."
OUTPUT_FILE="$(mktemp)"
java -cp bin Genetic_Algorithm > "$OUTPUT_FILE" 2>&1

# 3. Display output and extract CSV path
CSV_PATH=""
while IFS= read -r line; do
    echo "$line"
    if [[ "$line" == CSV_PATH:* ]]; then
        CSV_PATH="${line#CSV_PATH:}"
    fi
done < "$OUTPUT_FILE"

# Remove temp file
rm "$OUTPUT_FILE"

# 4. Validate CSV path
if [[ -z "$CSV_PATH" ]]; then
    echo "!!! ERROR: CSV path not captured !!!"
    echo "Check if Java output contains 'CSV_PATH:' marker"
    exit 1
fi

# 5. Run visualization
echo "Step 3/3: Running visualization..."
echo "CSV file: $CSV_PATH"
echo "Project directory: $SCRIPT_DIR"


# Execute visualization
python3 "$SCRIPT_DIR/visualize_GeneticAlgorithmLogs.py" "$CSV_PATH"

echo "----------------------------------------"
echo "Operations complete. Press any key to exit."
read -n 1 -s
