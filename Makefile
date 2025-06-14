# Project directories
SRC_DIR=src
BIN_DIR=bin
PY_SCRIPT=visualize_GeneticAlgorithmLogs.py

# Python dependencies
PY_DEPS=pandas matplotlib

# Targets
all: run

compile:
	@echo "Compiling Java sources..."
	javac -encoding UTF-8 -d $(BIN_DIR) $(SRC_DIR)/*.java

run: compile
	@echo "Running Genetic Algorithm..."
	@OUTPUT_FILE=$$(mktemp); \
	java -cp $(BIN_DIR) Genetic_Algorithm > "$$OUTPUT_FILE" 2>&1; \
	cat "$$OUTPUT_FILE"; \
	CSV_PATH=$$(grep '^CSV_PATH:' "$$OUTPUT_FILE" | sed 's/CSV_PATH://'); \
	rm "$$OUTPUT_FILE"; \
	if [ -z "$$CSV_PATH" ]; then \
		echo "ERROR: CSV path not captured. Check program output."; \
		exit 1; \
	fi; \
	echo "Running visualization with $$CSV_PATH..."; \
	python3 $(PY_SCRIPT) "$$CSV_PATH"

install-deps:
	@echo "Installing Python dependencies..."
	pip install $(PY_DEPS)

clean:
	@echo "Cleaning compiled files..."
	rm -rf $(BIN_DIR)/*
	rm -rf LOG_Files/

.PHONY: all compile run install-deps clean
