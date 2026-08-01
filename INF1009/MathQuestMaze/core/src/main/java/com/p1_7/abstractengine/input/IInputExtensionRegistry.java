package com.p1_7.abstractengine.input;

/**
 * registry for game-layer input extensions.
 *
 * allows the game package to register additional input types with InputManager
 * at startup without modifying the engine. scenes retrieve extensions via
 * getExtension() using their concrete interface type as the key.
 */
public interface IInputExtensionRegistry {

    /**
     * registers an input extension under its interface type.
     *
     * @param type      the interface class used as the registry key
     * @param extension the extension implementation
     * @throws IllegalArgumentException if type or extension is null
     */
    <T extends IInputExtension> void registerExtension(Class<T> type, T extension);

    /**
     * retrieves a previously registered input extension.
     *
     * @param type the interface class used as the registry key
     * @return the registered extension
     * @throws IllegalArgumentException if no extension is registered for type
     */
    <T extends IInputExtension> T getExtension(Class<T> type);

    /**
     * returns whether an extension has been registered for the given type.
     *
     * @param type the interface class to check
     * @return true if an extension is registered
     */
    <T extends IInputExtension> boolean hasExtension(Class<T> type);
}
