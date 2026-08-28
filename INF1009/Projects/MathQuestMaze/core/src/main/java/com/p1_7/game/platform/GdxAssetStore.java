package com.p1_7.game.platform;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.p1_7.abstractengine.render.IAssetStore;

/**
 * libgdx implementation of IAssetStore that wraps an AssetManager.
 */
public class GdxAssetStore implements IAssetStore {

    /** the underlying libgdx asset manager */
    private final AssetManager assetManager = new AssetManager();

    /**
     * loads a texture from the given asset path, blocking until ready.
     *
     * @param assetPath the relative path to the texture file
     * @return the loaded libgdx Texture
     */
    @Override
    public Object loadTexture(String assetPath) {
        if (!assetManager.isLoaded(assetPath, Texture.class)) {
            assetManager.load(assetPath, Texture.class);
            assetManager.finishLoading();
        }
        return assetManager.get(assetPath, Texture.class);
    }

    @Override
    public void finishLoading() {
        assetManager.finishLoading();
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
