package com.p1_7.abstractengine.collision;

import java.util.ArrayList;
import java.util.List;

/**
 * dimension-agnostic spatial index for broad-phase collision detection.
 * named SpatialTree rather than Quadtree or Octree because it generalises
 * to arbitrary dimensions: each node subdivides along all d dimensions at
 * the midpoint, producing 2^d children per node. for 2D this behaves as a
 * quadtree (4 children), for 3D an octree (8 children), and so on.
 */
public class SpatialTree {

    /** default maximum tree depth before subdivision stops */
    private static final int DEFAULT_MAX_DEPTH = 5;

    /** default entity threshold that triggers subdivision */
    private static final int DEFAULT_MAX_ENTITIES = 4;

    /** minimum corner position of this node's region */
    private final float[] minPosition;

    /** extent (size) of this node's region in each dimension */
    private final float[] extent;

    /** number of spatial dimensions (derived from array lengths) */
    private final int dimensions;

    /** current depth of this node in the tree */
    private final int depth;

    /** maximum depth before subdivision stops */
    private final int maxDepth;

    /** entity count threshold that triggers subdivision */
    private final int maxEntities;

    /** entities stored at this node */
    private final List<ICollidable> entities;

    /** child nodes; null until this node subdivides, length 2^d when allocated */
    private SpatialTree[] children;

    /**
     * constructs a spatial tree root with the given world bounds and default
     * capacity settings.
     *
     * @param minPosition minimum corner of the world region
     * @param extent      size of the world region in each dimension
     */
    public SpatialTree(float[] minPosition, float[] extent) {
        this(minPosition, extent, 0, DEFAULT_MAX_DEPTH, DEFAULT_MAX_ENTITIES);
    }

    /**
     * constructs a spatial tree node with explicit depth and capacity settings.
     *
     * @param minPosition minimum corner of this node's region
     * @param extent      size of this node's region in each dimension
     * @param depth       current depth of this node
     * @param maxDepth    maximum depth before subdivision stops
     * @param maxEntities entity count threshold that triggers subdivision
     */
    private SpatialTree(float[] minPosition, float[] extent,
                        int depth, int maxDepth, int maxEntities) {
        this.minPosition = minPosition;
        this.extent = extent;
        this.dimensions = minPosition.length;
        this.depth = depth;
        this.maxDepth = maxDepth;
        this.maxEntities = maxEntities;
        this.entities = new ArrayList<>();
        this.children = null;
    }

    /**
     * inserts a collidable into the deepest node that fully contains it.
     * entities spanning a midpoint remain in the parent node.
     *
     * @param collidable the entity to insert
     */
    public void insert(ICollidable collidable) {
        // if already subdivided, try to place in a child
        if (children != null) {
            int childIndex = findChildIndex(collidable);
            if (childIndex != -1) {
                children[childIndex].insert(collidable);
                return;
            }
        }

        // store in this node
        entities.add(collidable);

        // subdivide if threshold exceeded and depth permits
        if (children == null && entities.size() > maxEntities && depth < maxDepth) {
            subdivide();

            // redistribute existing entities into children where possible
            for (int i = entities.size() - 1; i >= 0; i--) {
                int childIndex = findChildIndex(entities.get(i));
                if (childIndex != -1) {
                    children[childIndex].insert(entities.remove(i));
                }
            }
        }
    }

    /**
     * collects all entities that could potentially collide with the given
     * collidable by traversing the tree from this node downward.
     *
     * @param result     the list to populate with candidate collidables
     * @param collidable the entity to find candidates for
     */
    public void retrieve(List<ICollidable> result, ICollidable collidable) {
        // always include entities stored at this node
        result.addAll(entities);

        // if subdivided, recurse into the relevant child (or all if spanning)
        if (children != null) {
            int childIndex = findChildIndex(collidable);
            if (childIndex != -1) {
                children[childIndex].retrieve(result, collidable);
            } else {
                // entity spans midpoint — must check all children
                for (SpatialTree child : children) {
                    child.retrieve(result, collidable);
                }
            }
        }
    }

    /**
     * clears all entities from this node and all descendants,
     * resetting the tree for the next frame.
     */
    public void clear() {
        entities.clear();
        if (children != null) {
            for (SpatialTree child : children) {
                child.clear();
            }
            children = null;
        }
    }

    /**
     * determines which child node fully contains the given collidable by
     * computing a bitmask from which half of each axis the entity falls in.
     *
     * bit i is 0 for the low half of dimension i, 1 for the high half.
     * returns -1 if the entity spans the midpoint on any axis, indicating
     * it must remain in the parent node.
     *
     * @param collidable the entity to classify
     * @return child index (0 to 2^d - 1), or -1 if the entity spans a midpoint
     */
    private int findChildIndex(ICollidable collidable) {
        IBounds bounds = collidable.getBounds();
        float[] entityMin = bounds.getMinPosition();
        float[] entityExtent = bounds.getExtent();

        int index = 0;
        for (int i = 0; i < dimensions; i++) {
            float midpoint = minPosition[i] + extent[i] / 2f;
            float entityMax = entityMin[i] + entityExtent[i];

            if (entityMin[i] >= midpoint) {
                // entirely in high half — set bit i
                index |= (1 << i);
            } else if (entityMax <= midpoint) {
                // entirely in low half — bit i stays 0
            } else {
                // spans the midpoint
                return -1;
            }
        }
        return index;
    }

    /**
     * subdivides this node by creating 2^d children, splitting each dimension
     * at the midpoint. child k's region for dimension i uses the low half if
     * bit i of k is 0, or the high half if bit i of k is 1.
     */
    private void subdivide() {
        int childCount = 1 << dimensions;
        children = new SpatialTree[childCount];

        float[] halfExtent = new float[dimensions];
        for (int i = 0; i < dimensions; i++) {
            halfExtent[i] = extent[i] / 2f;
        }

        for (int k = 0; k < childCount; k++) {
            float[] childMin = new float[dimensions];
            for (int i = 0; i < dimensions; i++) {
                // low half if bit i is 0, high half if bit i is 1
                childMin[i] = ((k >> i) & 1) == 0
                    ? minPosition[i]
                    : minPosition[i] + halfExtent[i];
            }
            children[k] = new SpatialTree(
                childMin, halfExtent.clone(), depth + 1, maxDepth, maxEntities
            );
        }
    }
}
