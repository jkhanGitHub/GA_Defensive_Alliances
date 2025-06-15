import sys
import os
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.gridspec import GridSpec
import textwrap


def main():
    print("Python visualization script started")
    print(f"Python version: {sys.version}")
    print(f"Arguments received: {sys.argv}")

    if len(sys.argv) < 2:
        print("Error: Missing CSV file argument")
        sys.exit(1)

    csv_file = sys.argv[1]
    print(f"Processing CSV file: {csv_file}")

    if not os.path.exists(csv_file):
        print(f"Error: File not found - {csv_file}")
        sys.exit(2)

    try:
        # Add debug output before plot creation
        print("Attempting to load CSV...")
        df = pd.read_csv(csv_file, comment='#')
        print(f"Successfully loaded {len(df)} rows")

        visualize_ga_results(csv_file)

        print("Visualization completed successfully")
    except Exception as e:
        print(f"Critical error during visualization: {str(e)}")
        sys.exit(3)
def parse_config(csv_file):
    """Parse configuration from CSV header"""
    config = {}
    with open(csv_file, 'r') as f:
        for line in f:
            if line.startswith('#'):
                if ':' in line:
                    key, value = line.strip('#').split(':', 1)
                    config[key.strip()] = value.strip()
            else:
                break
    return config


def visualize_ga_results(csv_file):
    # Parse configuration
    config = parse_config(csv_file)
    output_dir = os.path.dirname(csv_file)

    # Extract worst possible fitness if available
    worst_possible_fitness = None
    if "Break Fitness" in config:
        try:
            worst_possible_fitness = float(config["Break Fitness"])
        except ValueError:
            pass

    # Load data
    df = pd.read_csv(csv_file, comment='#')

    # Create figure with proper spacing
    plt.figure(figsize=(14, 20))

    # Create GridSpec layout with proper spacing
    gs = GridSpec(5, 1, height_ratios=[0.5, 2, 1, 1, 1], hspace=0.4)

    # Configuration text box with wrapping
    ax0 = plt.subplot(gs[0])
    ax0.axis('off')

    # Format configuration text
    config_text = "\n".join([f"{key}: {value}" for key, value in config.items()])
    wrapped_text = "\n".join(textwrap.wrap(config_text, width=120))

    ax0.text(0.02, 0.5, wrapped_text, fontsize=9, family='monospace',
             va='center', ha='left', bbox=dict(facecolor='whitesmoke', alpha=0.5))

    # Plot 1: Fitness Evolution
    ax1 = plt.subplot(gs[1])
    ax1.plot(df['generation'], df['best_fitness'], 'g-', lw=2, label='Best Fitness')
    ax1.plot(df['generation'], df['second_fitness'], 'b--', lw=1.5, label='2nd Best Fitness')
    ax1.plot(df['generation'], df['mean_fitness'], 'm:', lw=1.5, label='Mean Fitness')
    ax1.plot(df['generation'], df['worst_fitness'], 'r-', alpha=0.7, label='Worst Fitness')


    ax1.set_title('Fitness Evolution', fontsize=12, pad=10)
    ax1.set_ylabel('Fitness Score')
    ax1.legend()
    ax1.grid(True)

    # Plot 2: Alliance Size Metrics
    ax2 = plt.subplot(gs[2])
    ax2.plot(df['generation'], df['best_size'], 'g-', label='Best Genome')
    ax2.plot(df['generation'], df['second_size'], 'b--', label='2nd Best Genome')
    ax2.plot(df['generation'], df['mean_size'], 'm:', label='Mean Size')
    ax2.plot(df['generation'], df['worst_size'], 'r-', alpha=0.7, label='Worst Genome')
    ax2.fill_between(df['generation'], df['best_size'], df['worst_size'],
                     color='gray', alpha=0.1)

    ax2.set_title('Alliance Size Evolution', fontsize=12, pad=10)
    ax2.set_ylabel('Genome Sizes')
    ax2.legend()
    ax2.grid(True)

    # Plot 3: Population Composition
    ax3 = plt.subplot(gs[3])
    width = 0.35
    ax3.bar(df['generation'], df['survivors'], width, label='Survivors')
    ax3.bar(df['generation'], df['offspring'], width, bottom=df['survivors'],
            label='Offspring')

    

    ax3.set_title('Population Composition', fontsize=12, pad=10)
    ax3.set_ylabel('Count')
    ax3.legend(loc='upper left')
    ax3.grid(True)

    # Plot 4: Convergence Metrics
    ax4 = plt.subplot(gs[4])
    ax4.plot(df['generation'], df['best_current_vs_last_diff'], 'b-',
             label='Best vs Previous Gen')
    ax4.plot(df['generation'], df['best_second_diff'], 'g--',
             label='Best vs 2nd Best')
    ax4.plot(df['generation'], df['best_worst_diff'], 'r-',
             label='Best vs worst')

    # Highlight convergence threshold
    ax4.axhline(y=0.05, color='r', linestyle=':', alpha=0.7,
                label='Convergence Threshold')

    ax4.set_title('Convergence Metrics', fontsize=12, pad=10)
    ax4.set_xlabel('Generation')
    ax4.set_ylabel('Genetic Difference')
    ax4.legend()
    ax4.grid(True)

    # Add main title with run information
    plt.suptitle('Genetic Algorithm Results: Defensive Alliances in Graphs',
                 fontsize=14, y=0.98)

    # Save visualization
    viz_file = os.path.join(output_dir, "ga_visualization.png")
    plt.tight_layout()
    plt.subplots_adjust(top=0.95, hspace=0.4)  # Add extra space
    plt.savefig(viz_file, dpi=150)
    print(f"Visualization saved to {viz_file}")


if __name__ == "__main__":
    main()