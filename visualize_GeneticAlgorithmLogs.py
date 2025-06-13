import sys
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.gridspec import GridSpec

def visualize_ga_results(csv_file, worst_possible_fitness=None):
    # Load data
    df = pd.read_csv(csv_file)

    # Create figure with subplots
    fig = plt.figure(figsize=(14, 16))
    gs = GridSpec(4, 1, figure=fig, height_ratios=[2, 1, 1, 1])

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

    plt.tight_layout()
    plt.savefig(csv_file.replace('.csv', '_visualization.png'), dpi=150)
    print(f"Visualization saved to {csv_file.replace('.csv', '_visualization.png')}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python visualize_ga.py <csv_file> [worst_possible_fitness]")
        print("Example: python visualize_ga.py ga_stats_20250613_142735.csv 100")
        sys.exit(1)

    csv_file = sys.argv[1]
    worst_fitness = float(sys.argv[2]) if len(sys.argv) > 2 else None

    visualize_ga_results(csv_file, worst_fitness)