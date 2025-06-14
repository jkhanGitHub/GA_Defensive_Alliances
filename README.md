
# Genetic Algorithm Runner

This project is a Java-based Genetic Algorithm implementation with CSV logging and an optional Python-based result visualization. It’s configured for both Windows (`.bat` scripts) and Unix-like systems (`.sh` scripts).

---

## 📦 Project Structure

```
/src                  # Java source files
/bin                  # Compiled Java classes
/LOG_Files            # Output logs (auto-created on run)
/                     # Root directory
    run_GeneticAlgorithm.bat        # Windows runner script
    run_GeneticAlgorithm.sh         # Unix/macOS runner script
    visualize_GeneticAlgorithmLogs.py  # Python visualization script
    requirements.txt  # Python dependencies
	run_config.properties #this file has all attributes and variables relevant for the execution of the Genetic Algorithm
```

---

## 📋 Prerequisites

- **Java JDK 8+**
- **Python 3.x**
    pip install wheel
  - `pandas`
  - `matplotlib`

To install Python dependencies:

```bash
pip install -r requirements.txt
```

---

## set up your parameters inside run_config.properties

-all attributes and variables are stored in here
-make sure to read the meta data of the graph you are trying to explore
- number of nodes has to be the same as in the graph you chose



## 🖥️ How to Run

### Windows (via `run_GeneticAlgorithm.bat`)

1. Open `cmd` or a terminal in the project directory.
2. Execute:

   ```bash
   run_GeneticAlgorithm.bat
   ```
   
   - Compiles Java files in `/src`
   - Runs the Genetic Algorithm
   - Captures the output CSV log file path into LOGFiles\
   - Invokes the Python visualization script with that path
   
   -or double click run_GeneticAlgorithm.bat

---

### Unix/macOS (via `run_GeneticAlgorithm.sh`)

0. (Optional but recommended: USE Virtual enviroment venv)

  ```bash
    python3 -m venv path/to/venv
    source path/to/venv/bin/activate
    python3 -m pip install -r requirements.txt
    ```


1. Make sure the script is executable:

   ```bash
   chmod +x run_GeneticAlgorithm.sh
   ```

2. Run it:

   ```bash
   ./run_GeneticAlgorithm.sh
   ```

   - Same workflow: compile, run GA, capture CSV path, run Python visualization

3. (deactivate venv when used)

  ```bash
  deactivate
  ```

---

## 📈 CSV Logging

Each run creates a timestamped directory inside `/LOG_Files/`, containing a `ga_stats.csv` file. This includes:

- Configurations used for the run
- Per-generation statistics:
  - Fitness metrics
  -General Population Data of the current Generation
  - Genome sizes
  - Differences between best, second-best, and worst genomes

---

## 📊 Visualization

After the Genetic Algorithm run, a Python script `visualize_GeneticAlgorithmLogs.py` is called automatically. It reads the generated CSV log and displays/plots relevant performance graphs using `pandas` and `matplotlib`.

---

## 📝 Notes

- **Shell & Batch scripts are equivalent** — they both:
  1. Compile Java sources
  2. Execute the Genetic Algorithm
  3. Locate and pass the CSV file to the visualization script
- Java logging relies on a `Config` class containing parameters like mutation rate, selection method, etc.
- CSV logs include these configuration values at the top for reference.

---

## 📦 Optional: Docker (if applicable)

If you'd like to containerize this project, you can create a `Dockerfile` without an extension — Dockerfiles **do not have file extensions**.

---

## 📑 Build Tool Notes

- Currently, the project builds via `javac` in scripts.

---

That’s it — you’re ready to run, log, and visualize your genetic algorithm experiments!
