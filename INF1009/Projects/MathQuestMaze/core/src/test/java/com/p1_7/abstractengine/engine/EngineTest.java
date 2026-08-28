package com.p1_7.abstractengine.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the core Engine lifecycle bootstrapper.
 * Ensures that registered managers receive their init, update, and shutdown calls,
 * and that dependency ordering is enforced by the topological sort.
 */
public class EngineTest {

    private Engine engine;

    // --- Dummy Managers to track lifecycle calls ---
    private static class DummyStandardManager extends Manager {
        boolean initCalled = false;
        boolean shutdownCalled = false;

        @Override protected void onInit() { initCalled = true; }
        @Override protected void onShutdown() { shutdownCalled = true; }
    }

    private static class DummyUpdatableManager extends UpdatableManager {
        boolean initCalled = false;
        boolean updateCalled = false;
        boolean shutdownCalled = false;

        @Override protected void onInit() { initCalled = true; }
        @Override protected void onUpdate(float dt) { updateCalled = true; }
        @Override protected void onShutdown() { shutdownCalled = true; }
    }

    // --- Ordered managers for dependency ordering test ---

    // records its own name into a shared list when initialised
    private static class OrderedManagerA extends Manager {
        private final List<String> log;
        OrderedManagerA(List<String> log) { this.log = log; }
        @Override protected void onInit() { log.add("A"); }
    }

    // declares a dependency on OrderedManagerA
    private static class OrderedManagerB extends Manager {
        private final List<String> log;
        OrderedManagerB(List<String> log) { this.log = log; }

        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends IManager>[] getDependencies() {
            return new Class[]{OrderedManagerA.class};
        }

        @Override protected void onInit() { log.add("B"); }
    }

    @BeforeEach
    public void setUp() {
        engine = new Engine();
    }

    @Test
    public void testEngineLifecycle() {
        DummyStandardManager standardManager = new DummyStandardManager();
        DummyUpdatableManager updatableManager = new DummyUpdatableManager();

        engine.registerManager(standardManager);
        engine.registerManager(updatableManager);

        // Act 1: Initialization
        engine.init();

        assertTrue(standardManager.initCalled, "Standard manager should be initialised");
        assertTrue(updatableManager.initCalled, "Updatable manager should be initialised");

        // Act 2: Update Loop
        engine.update(1.0f);

        assertTrue(updatableManager.updateCalled, "Updatable manager should receive update ticks");

        // Act 3: Shutdown
        engine.shutdown();

        assertTrue(standardManager.shutdownCalled, "Standard manager should be shut down");
        assertTrue(updatableManager.shutdownCalled, "Updatable manager should be shut down");
    }

    @Test
    public void testGetManager() {
        DummyStandardManager standardManager = new DummyStandardManager();
        engine.registerManager(standardManager);

        DummyStandardManager retrieved = engine.getManager(DummyStandardManager.class);
        assertNotNull(retrieved);
        assertEquals(standardManager, retrieved);
    }

    @Test
    public void testDependencyOrdering_dependencyInitialisedBeforeDependant() {
        // register in reverse order — the engine must still initialise A before B
        List<String> initOrder = new ArrayList<>();
        OrderedManagerA a = new OrderedManagerA(initOrder);
        OrderedManagerB b = new OrderedManagerB(initOrder);

        engine.registerManager(b); // registered first but depends on A
        engine.registerManager(a);
        engine.init();

        assertEquals(2, initOrder.size());
        assertEquals("A", initOrder.get(0),
            "OrderedManagerA (dependency) must be initialised before OrderedManagerB (dependant)");
        assertEquals("B", initOrder.get(1));
    }
}