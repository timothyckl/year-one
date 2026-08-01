package com.p1_7.abstractengine.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the core Graph data structure used for engine dependency mapping.
 */
public class DirectedAcyclicGraphTest {

    private DirectedAcyclicGraph<String> graph;

    @BeforeEach
    public void setUp() {
        // Use simple Strings as nodes for testing
        graph = new DirectedAcyclicGraph<>();
    }

    @Test
    public void testAddNode() {
        graph.addNode("Manager_A");
        graph.addNode("Manager_B");

        assertEquals(2, graph.size(), "Graph should contain exactly 2 nodes");
        assertTrue(graph.getNodes().contains("Manager_A"));
        assertEquals(0, graph.getInDegree("Manager_A"), "New nodes should have an in-degree of 0");
    }

    @Test
    public void testAddEdgeUpdatesInDegreeAndNeighbours() {
        graph.addNode("Manager_A");
        graph.addNode("Manager_B");

        // Act: Create a directional edge pointing from A to B
        // (Meaning A must be initialized before B)
        graph.addEdge("Manager_A", "Manager_B");

        // Assert: B now has 1 incoming edge. A still has 0.
        assertEquals(0, graph.getInDegree("Manager_A"), "Source node should maintain 0 in-degree");
        assertEquals(1, graph.getInDegree("Manager_B"), "Target node should have an in-degree of 1");

        // Assert: B should be listed as a neighbor of A
        List<String> neighboursOfA = graph.getNeighbours("Manager_A");
        assertEquals(1, neighboursOfA.size());
        assertTrue(neighboursOfA.contains("Manager_B"));

        // Assert: A should NOT be listed as a neighbor of B (it is directional)
        List<String> neighboursOfB = graph.getNeighbours("Manager_B");
        assertTrue(neighboursOfB.isEmpty(), "Target node should have no outgoing edges in this setup");
    }

    @Test
    public void testMultiHopChain_inDegreesAreCorrect() {
        // chain: A → B → C
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        assertEquals(0, graph.getInDegree("A"), "Root node must have in-degree 0");
        assertEquals(1, graph.getInDegree("B"), "Middle node must have in-degree 1");
        assertEquals(1, graph.getInDegree("C"), "Leaf node must have in-degree 1");
    }

    @Test
    public void testMultipleIncomingEdges_inDegreeAccumulates() {
        // both B and C point to D — D should have in-degree 2
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addEdge("B", "D");
        graph.addEdge("C", "D");

        assertEquals(0, graph.getInDegree("B"));
        assertEquals(0, graph.getInDegree("C"));
        assertEquals(2, graph.getInDegree("D"),
            "A node with two incoming edges must report in-degree 2");
    }

    @Test
    public void testGetNeighbours_unregisteredNode_returnsNull() {
        // the adjacency map has no entry for a node that was never added
        assertNull(graph.getNeighbours("Ghost"),
            "getNeighbours on an unregistered node must return null");
    }

    @Test
    public void testSize_emptyGraph_returnsZero() {
        assertEquals(0, graph.size(), "Empty graph must report size 0");
    }
}