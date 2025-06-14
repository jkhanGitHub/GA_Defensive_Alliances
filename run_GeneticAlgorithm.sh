#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "----------------------------------------"
echo "   Running Genetic Algorithm Project"
echo "----------------------------------------"

# 1. Compile Java
echo "Step 1/3: Compiling Java..."
cd "$SCRIPT_DIR/src"
javac -encoding UTF-8 -d "$SCRIPT_DIR/bin" *.java || {
  echo "!!! COMPILATION FAILED !!!"
  exit 1
}

cd "$SCRIPT_DIR"

# 2. Run Java and capture output
echo "Step 2/3: Running genetic algorithm..."
output_file=$(mktemp)
java -cp bin Genetic_Algorithm > "$output_file" 2>&1

# 3. Process output
CSV_PATH=""
while IFS= read -r line; do
  echo "$line"
  [[ "$line" == CSV_PATH:* ]] && CSV_PATH="${line#CSV_PATH:}"
done < "$output_file"
rm "$output_file"

# 4. Validate CSV path
[[ -z "$CSV_PATH" ]] && {
  echo "!!! ERROR: CSV path not captured !!!"
  exit 1
}

# 5. Run visualization with dependency check
echo "Step 3/3: Running visualization..."
echo "CSV file: \"$CSV_PATH\""
echo "Project directory: \"$SCRIPT_DIR\""

# Function to check Python dependencies
check_python_deps() {
  local script_file="$1"
  local required_deps=("pandas" "matplotlib")
  
  # Check if Python script exists
  if [[ ! -f "$script_file" ]]; then
    echo "Creating placeholder Python script..."
    cat > "$script_file" <<'EOF'
import sys
print(f"Placeholder visualization script")
print(f"Received CSV file: {sys.argv[1]}")
# Add your real visualization code here
EOF
  fi

  # Check for required Python modules
  local missing_deps=()
  for dep in "${required_deps[@]}"; do
    python3 -c "import $dep" 2>/dev/null || missing_deps+=("$dep")
  done

  if [[ ${#missing_deps[@]} -gt 0 ]]; then
    echo "!!! MISSING PYTHON DEPENDENCIES: ${missing_deps[*]} !!!"
    echo "Please install them using:"
    echo "  pip3 install ${missing_deps[*]}"
    echo "Or for system-wide installation:"
    echo "  sudo pip3 install ${missing_deps[*]}"
    exit 1
  fi
}

# Check dependencies before running
check_python_deps "$SCRIPT_DIR/visualize_GeneticAlgorithmLogs.py"

# Run visualization
python3 "$SCRIPT_DIR/visualize_GeneticAlgorithmLogs.py" "$CSV_PATH"

echo "----------------------------------------"
echo "Operations complete."
read -p "Press enter to exit..."