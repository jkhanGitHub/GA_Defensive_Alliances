import sys
import os
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.gridspec import GridSpec

#use csv file 
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
                break  # Stop at data rows
    return config

def visualize_ga_results(csv_file, worst_possible_fitness=None):
    # Create output directory same as CSV location
    output_dir = os.path.dirname(csv_file)

    # Parse configuration and load data
    config = parse_config(csv_file)  # Use previous parse_config implementation
    df = pd.read_csv(csv_file, comment='#')

    # Create figure with configuration box
    fig = plt.figure(figsize=(14, 18))
    gs = GridSpec(5, 1, figure=fig, height_ratios=[0.8, 2, 1, 1, 1])

    # Configuration text box
    ax0 = fig.add_subplot(gs[0])
    ax0.axis('off')
    config_text = (
        f"Graph: {config.get('Graph', 'N/A')}\n"
        f"Nodes: {config.get('Nodes', 'N/A')}, "
        f"Population: {config.get('Population', 'N/A')}, "
        f"Generations: {config.get('Generations', 'N/A')}\n"
        f"Node Prob: {config.get('Node Probability', 'N/A')}, "
        f"Mutation Rate: {config.get('Mutation Rate', 'N/A')}\n"
        f"Selection: {config.get('Selection', 'N/A')}, "
        f"Children/Parent: {config.get('Children/Parent', 'N/A')}\n"
        f"Mutation: {config.get('Mutation', 'N/A')}, "
        f"Learning: {config.get('Learning', 'N/A')}, "
        f"Recomb Prob: {config.get('Recombination Prob', 'N/A')}"
    )
    ax0.text(0.02, 0.5, config_text, fontsize=10, family='monospace',
             va='center', ha='left', bbox=dict(facecolor='whitesmoke', alpha=0.5))

    # Plot 1: Fitness Evolution
    ax1 = fig.add_subplot(gs[0])
    ax1.plot(df['generation'], df['best_fitness'], 'g-', lw=2, label='Best Fitness')
    ax1.plot(df['generation'], df['second_fitness'], 'b--', lw=1.5, label='2nd Best Fitness')
    ax1.plot(df['generation'], df['mean_fitness'], 'm:', lw=1.5, label='Mean Fitness')
    ax1.plot(df['generation'], df['worst_fitness'], 'r-', alpha=0.7, label='Worst Fitness')

    if worst_possible_fitness is not None:
        ax1.axhline(y=worst_possible_fitness, color='k', linestyle='-',
                    label=f'Min Possible ({worst_possible_fitness})')

    ax1.set_title('Fitness Evolution')
    ax1.set_ylabel('Fitness Score')
    ax1.legend()
    ax1.grid(True)

    # Plot 2: Alliance Size Metrics
    ax2 = fig.add_subplot(gs[1])
    ax2.plot(df['generation'], df['best_size'], 'g-', label='Best Genome in Alliance')
    ax2.plot(df['generation'], df['second_size'], 'b--', label='2nd Best Genome in Alliance')
    ax2.plot(df['generation'], df['mean_size'], 'm:', label='Mean Fitness in Alliance')
    ax2.plot(df['generation'], df['worst_size'], 'r-', alpha=0.7, label='Worst Genome in Alliance')
    ax2.fill_between(df['generation'], df['best_size'], df['worst_size'],
                     color='gray', alpha=0.1)

    ax2.set_title('Alliance Size Evolution')
    ax2.set_ylabel('Alliance Size')
    ax2.legend()
    ax2.grid(True)

    # Plot 3: Population Composition
    ax3 = fig.add_subplot(gs[2])
    width = 0.35
    ax3.bar(df['generation'], df['survivors'], width, label='Survivors')
    ax3.bar(df['generation'], df['offspring'], width, bottom=df['survivors'],label='Offspring')

    # Add genetic difference line (scaled to secondary axis)
    ax3_diff = ax3.twinx()
    #ax3_diff.plot(df['generation'], df['best_second_diff'], 'k-', alpha=0.7,label='Best-2nd Diff')
    ax3_diff.set_ylabel('Genetic Difference', color='k')

    ax3.set_title('Population Composition')
    ax3.set_ylabel('Count')
    ax3.legend(loc='upper left')
    ax3.grid(True)

    # Plot 4: Convergence Metrics
    ax4 = fig.add_subplot(gs[3])
    ax4.plot(df['generation'], df['best_current_vs_last_diff'], 'b-',
             label='Best vs Previous Gen')
    ax4.plot(df['generation'], df['best_second_diff'], 'g--',
             label='Best vs 2nd Best')
    ax4.plot(df['generation'], df['best_worst_diff'], 'r-',
             label='Best vs worst')

    # Highlight convergence threshold
    ax4.axhline(y=0.05, color='r', linestyle=':', alpha=0.7,
                label='Convergence Threshold')

    ax4.set_title('Convergence Metrics')
    ax4.set_xlabel('Generation')
    ax4.set_ylabel('Genetic Difference')
    ax4.legend()
    ax4.grid(True)


    # Save visualization in same directory
    plt.tight_layout()
    viz_file = os.path.join(output_dir, "ga_visualization.png")
    plt.savefig(viz_file, dpi=150, bbox_inches='tight')
    print(f"Visualization saved to {viz_file}")

    # Additionally save configuration to text file
    config_file = os.path.join(output_dir, "config.txt")
    with open(config_file, 'w') as f:
        for key, value in config.items():
            f.write(f"{key}: {value}\n")
    print(f"Configuration saved to {config_file}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python visualize_ga.py <csv_file> [worst_possible_fitness]")
        print("Example: python visualize_ga.py ga_stats_20250613_142735.csv 100")
        sys.exit(1)

    csv_file = sys.argv[1]
    worst_fitness = float(sys.argv[2]) if len(sys.argv) > 2 else None

    visualize_ga_results(csv_file, worst_fitness)