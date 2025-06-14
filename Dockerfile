# Use an OpenJDK + Python image
FROM openjdk:21-slim

# Install Python and pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install --no-cache-dir pandas matplotlib

# Set working directory
WORKDIR /app

# Copy project files
COPY . /app

# Compile Java sources
RUN javac -encoding UTF-8 -d bin src/*.java

# Default command: run the algorithm
CMD bash -c "\
    OUTPUT_FILE=\$(mktemp) && \
    java -cp bin Genetic_Algorithm > \"\$$OUTPUT_FILE\" 2>&1 && \
    cat \"\$$OUTPUT_FILE\" && \
    CSV_PATH=\$(grep '^CSV_PATH:' \"\$$OUTPUT_FILE\" | sed 's/CSV_PATH://') && \
    rm \"\$$OUTPUT_FILE\" && \
    if [ -z \"\$$CSV_PATH\" ]; then \
        echo 'ERROR: CSV path not captured.' && exit 1; \
    fi && \
    echo 'Running visualization with '\$CSV_PATH'...' && \
    python3 visualize_GeneticAlgorithmLogs.py \"\$CSV_PATH\""
