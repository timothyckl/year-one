package com.p1_7.abstractengine.render;

/**
 * abstraction over texture and asset loading and retrieval.
 */
public interface IAssetStore {

    /**
     * loads a texture from the given asset path, blocking until ready.
     *
     * @param assetPath the relative path to the texture file
     * @return an opaque handle to the loaded texture
     */
    Object loadTexture(String assetPath);

    /**
     * flushes any queued loading operations.
     */
    void finishLoading();

    /**
     * disposes all loaded assets and releases resources.
     */
    void dispose();
}
