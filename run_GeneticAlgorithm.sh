#!/bin/bash
set -euo pipefail

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "----------------------------------------"
echo "   Running Genetic Algorithm Project"
echo "----------------------------------------"

# 1. Compile Java
echo "Step 1/3: Compiling Java..."
cd "$SCRIPT_DIR/src"
javac -encoding UTF-8 -d "$SCRIPT_DIR/bin" *.java
if [ $? -ne 0 ]; then
  echo "!!! COMPILATION FAILED !!!"
  exit 1
fi

# Return to project root
cd "$SCRIPT_DIR"

# 2. Run Java and capture output
echo "Step 2/3: Running genetic algorithm..."
output_file=$(mktemp)
java -cp bin Genetic_Algorithm > "$output_file" 2>&1

# 3. Display output and capture CSV path
CSV_PATH=""
while IFS= read -r line; do
  echo "$line"
  if [[ "$line" == CSV_PATH:* ]]; then
    CSV_PATH="${line#CSV_PATH:}"
  fi
done < "$output_file"

# Cleanup
rm "$output_file"

# 4. Check if path was captured
if [ -z "$CSV_PATH" ]; then
  echo "!!! ERROR: CSV path not captured !!!"
  echo "Check if Java output contains \"CSV_PATH:\" marker"
  exit 1
fi

# 5. Run visualization
echo "Step 3/3: Running visualization..."
echo "CSV file: \"$CSV_PATH\""
echo "Project directory: \"$SCRIPT_DIR\""

# Check if Python script exists
if [ ! -f "$SCRIPT_DIR/visualize_GeneticAlgorithmLogs.py" ]; then
  echo "ERROR: Python script not found. Creating valid placeholder..."
  cat > "$SCRIPT_DIR/visualize_ga.py" <<EOF
import sys
print("Placeholder visualization script")
print(f"Received CSV file: {sys.argv[1]}")
# Add your visualization code here
EOF
fi

echo "Executing Python..."
python3 "$SCRIPT_DIR/visualize_GeneticAlgorithmLogs.py" "$CSV_PATH"

echo "----------------------------------------"
echo "Operations complete."
read -p "Press enter to exit..."
