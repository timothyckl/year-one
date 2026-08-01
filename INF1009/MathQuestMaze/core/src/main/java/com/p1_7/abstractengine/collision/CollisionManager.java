package com.p1_7.abstractengine.collision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.p1_7.abstractengine.engine.UpdatableManager;

/**
 * abstract per-frame manager that tests all registered ICollidable entities
 * for pairwise overlap and delegates resolution to concrete subclasses.
 * when world bounds are set via setWorldBounds, detection uses a SpatialTree
 * broad phase to reduce unnecessary pair checks. without world bounds,
 * detection falls back to brute-force.
 */
public abstract class CollisionManager extends UpdatableManager {

    /** all collidable entities managed by this manager */
    private final List<ICollidable> collidables = new ArrayList<>();

    /** stateless detector that performs the overlap test */
    private final CollisionDetector detector = new CollisionDetector();

    /** detected collisions from the current frame */
    private final List<CollisionPair> detectedCollisions = new ArrayList<>();

    /** minimum corner of the world region for broad-phase indexing */
    private float[] worldMinPosition;

    /** extent (size) of the world region for broad-phase indexing */
    private float[] worldExtent;

    /** whether world bounds have been configured */
    private boolean worldBoundsSet = false;

    /** reusable buffer for spatial tree candidate retrieval */
    private final List<ICollidable> candidateBuffer = new ArrayList<>();

    /**
     * adds an ICollidable to the detection list.
     *
     * @param collidable the collidable entity to register
     */
    public void registerCollidable(ICollidable collidable) {
        collidables.add(collidable);
    }

    /**
     * removes an ICollidable from the detection list.
     *
     * @param collidable the collidable entity to unregister
     */
    public void unregisterCollidable(ICollidable collidable) {
        collidables.remove(collidable);
    }

    /**
     * sets the world bounds for broad-phase spatial indexing. once set,
     * detect() uses a SpatialTree instead of brute-force.
     *
     * @param minPosition minimum corner of the world region
     * @param extent      size of the world region in each dimension
     */
    public void setWorldBounds(float[] minPosition, float[] extent) {
        this.worldMinPosition = minPosition;
        this.worldExtent = extent;
        this.worldBoundsSet = true;
    }

    /**
     * runs collision detection and resolution in two phases:
     * first detects all collisions, then resolves them.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    protected void onUpdate(float deltaTime) {
        detect();
        resolve(detectedCollisions);
    }

    /**
     * detects all collisions for the current frame. uses spatial tree
     * broad phase when world bounds are set, otherwise falls back to
     * brute-force pairwise checking.
     */
    protected void detect() {
        detectedCollisions.clear();

        if (worldBoundsSet) {
            detectWithSpatialTree();
        } else {
            detectBruteForce();
        }
    }

    /**
     * brute-force pairwise collision detection.
     */
    private void detectBruteForce() {
        for (int i = 0; i < collidables.size() - 1; i++) {
            ICollidable a = collidables.get(i);
            for (int j = i + 1; j < collidables.size(); j++) {
                ICollidable b = collidables.get(j);
                if (detector.checkCollision(a, b)) {
                    detectedCollisions.add(new CollisionPair(a, b));
                }
            }
        }
    }

    /**
     * spatial-tree-accelerated collision detection. builds a fresh tree each
     * frame, inserts all collidables, then retrieves candidates per entity
     * and narrow-phase tests only those pairs.
     */
    private void detectWithSpatialTree() {
        SpatialTree tree = new SpatialTree(worldMinPosition, worldExtent);

        // insert all collidables into the spatial tree
        for (int i = 0; i < collidables.size(); i++) {
            tree.insert(collidables.get(i));
        }

        // build an index map for pair deduplication
        Map<ICollidable, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < collidables.size(); i++) {
            indexMap.put(collidables.get(i), i);
        }

        // retrieve candidates and narrow-phase test
        for (int indexA = 0; indexA < collidables.size(); indexA++) {
            ICollidable a = collidables.get(indexA);

            candidateBuffer.clear();
            tree.retrieve(candidateBuffer, a);

            for (int c = 0; c < candidateBuffer.size(); c++) {
                ICollidable b = candidateBuffer.get(c);

                // skip self
                if (a == b) continue;

                // deduplicate: only check pairs where indexB > indexA
                Integer indexB = indexMap.get(b);
                if (indexB == null || indexB <= indexA) continue;

                if (detector.checkCollision(a, b)) {
                    detectedCollisions.add(new CollisionPair(a, b));
                }
            }
        }
    }

    /**
     * returns the list of registered collidables.
     *
     * @return the collidable entities managed by this manager
     */
    protected List<ICollidable> getCollidables() {
        return collidables;
    }

    /**
     * resolves detected collisions for the current frame.
     *
     * @param collisions the list of detected collision pairs from this frame
     */
    protected abstract void resolve(List<CollisionPair> collisions);
}
