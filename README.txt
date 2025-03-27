CsvReader ist eine Sammlung aus Methoden eine csv Datei zu lesen und beinhaltet eine Methode welche eine csv Datei mit Kantenbeschreibung in eine graph Matrix umwandelt.
FitnessFunctions ist eine Sammlung aus FitnessFunctionen
Genetic Algorithm ist die Klasse, welche den genetischen Algorithmus enthält, jedoch muss hier immer wieder die Selectionsmethode manuell geändert werden, da Selection, REcombination und Fitnessfunction zurzeit nicht als Parameter übergeben werden.
Genome ist ein Subgraph vom Gesamtgraphen mit einem Binären eindimensionalen Array Mit der Länge der Knotenanzahl, wobei 1 aussagt, dass der Knoten mit der korrespondierenden ID, also int[ID] im Subgraph enthalten ist und im Falle 0 nicht enthalten ist.
Genome enthält desweiteren eine Liste mit den Degrees der Knoten innerhalb des Subgraphen.
Mutation enthält Mutationsmethoden
OneGenom beschreibt den gesamtgraph
Population ist ein Genom[] hier statische größe gewählt, da die Größe der Population gleichbleibt
Recombination enthält verschiedenen Recombinationsmethoden
Selection enthält Selektionsmethoden, welche zurzeit auch Recombination und update der Population und Fitnessfunktion beinhalten.

Der Genetische Algorithmus wird mit den Parametern der Genetic_Algorithm datei ausgeführt.
Falls der Algorithmus zu viele Threads nutzt, nutze bitte die Methode Selection.tournamentSelectionElimination_ProababilityIntersection statt Selection.tournamentSelectionElimination_ProababilityIntersection_Threaded
