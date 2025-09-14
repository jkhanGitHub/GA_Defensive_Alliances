# Genetic Algorithm Runner for Defensive Alliances in Graphs

This project implements a Java-based Genetic Algorithm (GA) to identify **minimal defensive alliances** in undirected graphs. Defensive alliances are subgroups of nodes that are robust against "attacks"—a concept from graph theory with applications in network security, biology, and social network analysis.

The project provides:
- A configurable GA engine in Java for alliance identification
- CSV logging of each experiment's configuration and results
- Optional Python visualization of GA progress and results

## ✨ What Are Defensive Alliances?

A **defensive alliance** in an undirected graph G=(V,E) is a subset S of V such that each member has at least as many neighbors within S as it has outside of S. 
Finding minimal such sets is important for analyzing network resilience and community structure.

This project aims to efficiently and effectively discover such defensive alliances using evolutionary computation, making it suitable for large or complex graphs where brute-force methods are impractical.

## 🔍 Core Functionality

- **Customizable Genetic Algorithm:** Configure population size, mutation/crossover rates, selection method, and stopping criteria via `run_config.properties`.
- **Multithreading Support:** Run in single or multi-threaded mode for performance.
- **Logging:** Every run auto-logs configurations and per-generation statistics.
- **Visualization:** Python script renders plots of fitness, population diversity, and more, from CSV logs.

## 🖼️ Example Workflow

1. **Prepare your graph and parameters** in `run_config.properties` (ensure node count matches your input graph).
2. **Run the algorithm** using provided shell/batch scripts.
3. **Review the logs:** Check `/LOG_Files/` for detailed CSV output.
4. **Visualize the results:** Python script automatically plots key metrics after each run.

## 🗂️ Project Structure

```
/src                  # Java source files
/bin                  # Compiled Java classes
/LOG_Files/           # Output logs (auto-created on run)
run_GeneticAlgorithm.bat  # Windows runner
run_GeneticAlgorithm.sh   # Unix/macOS runner
visualize_GeneticAlgorithmLogs.py  # Python visualization
requirements.txt      # Python dependencies
run_config.properties # All parameters for GA execution
```

## ⚙️ Configuration (`run_config.properties`)

Key parameters you can set:
- `graph_file`: Path to your input graph data
- `population_size`: Number of candidate solutions per generation
- `mutation_rate` / `crossover_rate`: Genetic algorithm tuning
- `num_generations`: Stopping condition
- `alliance_type`: Defensive alliance variant (if supported)
- And more (see file comments for all options)

## 🚦 How to Run

### Windows

```bash
run_GeneticAlgorithm.bat
```
- Compiles Java sources
- Runs the GA and logs output
- Invokes the Python visualization script with the log file

### Unix/macOS

> **Highly Recommended:** Use a Python virtual environment (`venv`) before running the shell script to ensure clean and isolated package management. This avoids dependency conflicts with other Python projects on your system and ensures the correct versions of required libraries are used for visualization.

**To create and activate a venv:**
```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

**Then run:**
```bash
chmod +x run_GeneticAlgorithm.sh
./run_GeneticAlgorithm.sh
```

**After you're done, you can deactivate the venv:**
```bash
deactivate
```

- The shell script will compile Java sources, execute the GA, log output, and invoke the Python visualization script within your venv.

## 📈 Output & Visualization

- Each run generates a timestamped directory in `/LOG_Files/` with `ga_stats.csv`:
  - Top: run configuration
  - Per-generation: fitness, population statistics, genome details, etc.
- The Python script (`visualize_GeneticAlgorithmLogs.py`) will plot metrics such as:
  - Best/average/worst fitness per generation
  - Alliance sizes over time
  - Population diversity

### Understanding the Output Data

- **Fitness Function**:  
  - The fitness value measures whether a genome (subgraph) is a defensive alliance and how desirable it is:
    - The fitness value is **negative** if the genome does not represent a valid defensive alliance.
    - The fitness value is **positive** if the genome is a valid defensive alliance.
    - A fitness value of **0** indicates a trivial case: either the subgraph represents the entire graph or the empty graph.
- **Genome/Subgraph Size**:  
  - The size of a genome (subgraph) is the number of nodes included in the subgraph. This tells you the size of the defensive alliance candidate.
- **Genetic Differences**:  
  - The genetic difference between two genomes is the number of positions (alleles) where their values differ.  
  - In practice, this is determined by comparing the genomes position by position: for every node, if one genome includes the node and the other does not, this counts as one difference.
  - In pseudo code:
    ```
    Set genetic_difference to 0
    For each index i from 1 to number_of_nodes:
        If genome1 at position i is not equal to genome2 at position i:
            Increase genetic_difference by 1
    Return genetic_difference
    ```
    **In words:**  
    The genetic difference is the number of nodes that are included in one subgraph but not the other. You compare each node's status in both genomes, and each time they differ, you add one to the difference count. The total is a measure of how distinct the two candidate solutions are.

The output CSV and the generated visualizations will help you analyze not just the progress and convergence of the genetic algorithm, but also the diversity and structural quality of the defensive alliances (subgraphs) found throughout the run.

## 📝 Example

Suppose you want to find the smallest defensive alliance in a 20-node social network graph:

1. Set `graph_file=graph_20nodes.csv` and **make sure** `number_of_nodes=20` in `run_config.properties` (the node count must match the meta data of your input graph).
2. Adjust other parameters (e.g., `population_size=100`) as needed.
3. Run the appropriate script for your OS (with venv activated if using Unix/macOS).
4. After completion, open the generated plot to see how the GA converged and which nodes form the optimal alliance.

> ⚠️ **Important:** Always read the meta data of your input graph before configuring `run_config.properties`. The `number_of_nodes` parameter must exactly match the number of nodes in your graph file. Otherwise, the algorithm may fail or produce invalid results.

## 🧩 Dependencies

- **Java JDK 8+**
- **Python 3.x** with `pandas`, `matplotlib` (install via `pip install -r requirements.txt`)

## 🐳 Optional: Docker

If you wish to containerize the project, you can create a `Dockerfile` (no extension) to automate setup and execution.

## 🔖 Notes

- Both shell and batch scripts compile and run the Java code, then trigger visualization.
- Java logging relies on a `Config` class containing all GA parameters.
- Logs and visualizations help you analyze parameter sensitivity and algorithm performance.
- For detailed parameter documentation, see comments in `run_config.properties`.

---

That’s it — you’re ready to run, log, and visualize your genetic algorithm experiments for defensive alliance detection!
