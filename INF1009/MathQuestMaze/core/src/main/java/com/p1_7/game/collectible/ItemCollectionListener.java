package com.p1_7.game.collectible;

/**
 * callback invoked when a collectable item is successfully picked up.
 *
 * register with Item.bindListener() so the scene layer can react to
 * collection events without coupling item classes to audio or other systems.
 */
public interface ItemCollectionListener {

    /**
     * called after the item's pickup effect has been applied.
     *
     * @param item the item that was collected
     */
    void onItemCollected(Item item);
}
