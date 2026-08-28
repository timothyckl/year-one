package com.p1_7.abstractengine.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * sorts managers into dependency-resolved initialisation order using kahn's
 * algorithm over a directed acyclic graph built from each manager's declared
 * dependencies.
 */
public final class DependencySorter {

    private DependencySorter() {
    }

    /**
     * builds a dependency graph from the given managers and returns them in
     * topologically sorted order. managers with no dependency relationship
     * retain their original registration order.
     *
     * @param managers   the managers to sort, in registration order
     * @param managerMap type-keyed index used to resolve dependency types
     * @return managers in dependency-resolved order
     * @throws IllegalArgumentException if a dependency type is not registered
     * @throws IllegalStateException    if a circular dependency is detected
     */
    public static List<IManager> sort(
            List<IManager> managers,
            Map<Class<? extends IManager>, IManager> managerMap) {

        // build the dependency graph
        DirectedAcyclicGraph<IManager> graph = buildGraph(managers, managerMap);

        // run kahn's algorithm
        return kahnSort(graph);
    }

    /**
     * constructs a dag where an edge from a to b means a must be initialised
     * before b.
     *
     * @param managers   the managers to add as nodes
     * @param managerMap type-keyed index for resolving dependency types
     * @return the populated directed acyclic graph
     */
    private static DirectedAcyclicGraph<IManager> buildGraph(
            List<IManager> managers,
            Map<Class<? extends IManager>, IManager> managerMap) {

        DirectedAcyclicGraph<IManager> graph = new DirectedAcyclicGraph<>();

        // register every manager as a node
        for (int i = 0; i < managers.size(); i++) {
            graph.addNode(managers.get(i));
        }

        // add edges from each dependency to the dependant
        for (int i = 0; i < managers.size(); i++) {
            IManager manager = managers.get(i);
            if (!(manager instanceof Manager)) {
                continue;
            }
            Class<? extends IManager>[] deps = ((Manager) manager).getDependencies();
            for (Class<? extends IManager> depType : deps) {
                IManager depManager = managerMap.get(depType);
                if (depManager == null) {
                    throw new IllegalArgumentException(
                        manager.getClass().getSimpleName()
                        + " depends on " + depType.getSimpleName()
                        + " but no such manager is registered");
                }
                // dependency must come before dependant
                graph.addEdge(depManager, manager);
            }
        }

        return graph;
    }

    /**
     * runs kahn's algorithm on the graph, returning nodes in topological order.
     *
     * @param graph the directed acyclic graph to sort
     * @return sorted node list
     * @throws IllegalStateException if the graph contains a cycle
     */
    private static List<IManager> kahnSort(DirectedAcyclicGraph<IManager> graph) {
        List<IManager> nodes = graph.getNodes();
        int n = graph.size();

        // track mutable in-degrees without modifying the graph
        int[] inDegree = new int[n];
        for (int i = 0; i < n; i++) {
            inDegree[i] = graph.getInDegree(nodes.get(i));
        }

        // seed queue with zero-in-degree nodes in registration order (fifo for stability)
        List<Integer> queue = new ArrayList<>();
        int head = 0;
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<IManager> sorted = new ArrayList<>(n);
        while (head < queue.size()) {
            int current = queue.get(head++);
            sorted.add(nodes.get(current));

            List<IManager> neighbours = graph.getNeighbours(nodes.get(current));
            for (int j = 0; j < neighbours.size(); j++) {
                int neighbourIdx = nodes.indexOf(neighbours.get(j));
                inDegree[neighbourIdx]--;
                if (inDegree[neighbourIdx] == 0) {
                    queue.add(neighbourIdx);
                }
            }
        }

        if (sorted.size() != n) {
            // identify managers involved in the cycle
            StringBuilder cycleInfo = new StringBuilder("circular dependency detected among: ");
            boolean first = true;
            for (int i = 0; i < n; i++) {
                if (inDegree[i] > 0) {
                    if (!first) {
                        cycleInfo.append(", ");
                    }
                    cycleInfo.append(nodes.get(i).getClass().getSimpleName());
                    first = false;
                }
            }
            throw new IllegalStateException(cycleInfo.toString());
        }

        return sorted;
    }
}
