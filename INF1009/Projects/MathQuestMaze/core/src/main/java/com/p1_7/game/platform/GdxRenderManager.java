package com.p1_7.game.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.p1_7.abstractengine.render.IAssetStore;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.abstractengine.render.RenderManager;

/**
 * libgdx-specific render manager that provides concrete drawing resources.
 *
 * the factory methods create concrete wrapper types. the downcast in
 * createDrawContext() is safe because this class controls both factories.
 */
public class GdxRenderManager extends RenderManager {

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        super.render();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    protected ISpriteBatch createSpriteBatch() {
        return new GdxSpriteBatch();
    }

    @Override
    protected IShapeRenderer createShapeRenderer() {
        return new GdxShapeRenderer();
    }

    @Override
    protected IAssetStore createAssetStore() {
        return new GdxAssetStore();
    }

    /**
     * creates the libgdx draw context. the downcasts are safe here because
     * this manager's factories create the concrete types directly above.
     *
     * @param batch         the sprite batch (always a GdxSpriteBatch)
     * @param shapeRenderer the shape renderer (always a GdxShapeRenderer)
     * @param assetStore    the asset store
     * @return a new GdxDrawContext
     */
    @Override
    protected IDrawContext createDrawContext(ISpriteBatch batch,
                                             IShapeRenderer shapeRenderer,
                                             IAssetStore assetStore) {
        return new GdxDrawContext((GdxSpriteBatch) batch,
                                  (GdxShapeRenderer) shapeRenderer,
                                  assetStore);
    }
}
