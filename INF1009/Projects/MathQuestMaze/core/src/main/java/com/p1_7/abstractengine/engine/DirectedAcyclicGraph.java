package com.p1_7.abstractengine.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * generic directed acyclic graph backed by an adjacency list and in-degree map.
 * nodes are stored in insertion order.
 *
 * @param <T> the node type
 */
public class DirectedAcyclicGraph<T> {

    /** nodes in insertion order */
    private final List<T> nodes = new ArrayList<>();

    /** outgoing edges for each node */
    private final Map<T, List<T>> adjacency = new HashMap<>();

    /** number of incoming edges per node */
    private final Map<T, Integer> inDegree = new HashMap<>();

    /**
     * registers a node in the graph.
     *
     * @param node the node to add
     */
    public void addNode(T node) {
        nodes.add(node);
        adjacency.put(node, new ArrayList<>());
        inDegree.put(node, 0);
    }

    /**
     * adds a directed edge indicating that from must come before to.
     * increments the in-degree of to.
     *
     * @param from the predecessor node
     * @param to   the successor node
     */
    public void addEdge(T from, T to) {
        adjacency.get(from).add(to);
        inDegree.put(to, inDegree.get(to) + 1);
    }

    /**
     * returns all nodes in insertion order.
     *
     * @return the node list
     */
    public List<T> getNodes() {
        return nodes;
    }

    /**
     * returns the adjacency list (outgoing neighbours) for the given node.
     *
     * @param node the node to query
     * @return the list of successor nodes
     */
    public List<T> getNeighbours(T node) {
        return adjacency.get(node);
    }

    /**
     * returns the in-degree (number of incoming edges) for the given node.
     *
     * @param node the node to query
     * @return the in-degree count
     */
    public int getInDegree(T node) {
        return inDegree.get(node);
    }

    /**
     * returns the total number of nodes in the graph.
     *
     * @return the node count
     */
    public int size() {
        return nodes.size();
    }
}
