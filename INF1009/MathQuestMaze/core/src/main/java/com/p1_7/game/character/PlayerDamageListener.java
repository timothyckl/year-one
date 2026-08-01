package com.p1_7.game.character;

/**
 * callback invoked when the player successfully takes enemy damage.
 *
 * register with Player.bindDamageListener() so the scene layer can react
 * without coupling the Player entity to audio or other systems.
 */
public interface PlayerDamageListener {

    /**
     * called after health has been reduced and the hurt animation triggered.
     */
    void onPlayerDamaged();
}
