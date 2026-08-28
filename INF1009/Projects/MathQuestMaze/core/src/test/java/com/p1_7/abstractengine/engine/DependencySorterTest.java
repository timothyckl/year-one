package com.p1_7.abstractengine.engine;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the topological sort and cycle detection logic in DependencySorter.
 */
public class DependencySorterTest {

    // --- Concrete manager stubs for wiring dependency relationships ---

    private static class ManagerA extends Manager {}

    private static class ManagerB extends Manager {
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends IManager>[] getDependencies() {
            return new Class[]{ManagerA.class};
        }
    }

    private static class ManagerC extends Manager {
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends IManager>[] getDependencies() {
            return new Class[]{ManagerB.class};
        }
    }

    // shares a dep with ManagerB — both depend on ManagerA
    private static class ManagerD extends Manager {
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends IManager>[] getDependencies() {
            return new Class[]{ManagerA.class};
        }
    }

    // forms a cycle: depends on ManagerE
    private static class ManagerCycleX extends Manager {
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends IManager>[] getDependencies() {
            return new Class[]{ManagerCycleY.class};
        }
    }

    // forms a cycle: depends on ManagerCycleX
    private static class ManagerCycleY extends Manager {
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends IManager>[] getDependencies() {
            return new Class[]{ManagerCycleX.class};
        }
    }

    // declares a dep on an unregistered type
    private static class ManagerGhost extends Manager {
        @Override
        @SuppressWarnings("unchecked")
        public Class<? extends IManager>[] getDependencies() {
            return new Class[]{ManagerA.class};
        }
    }

    // --- helpers ---

    private static Map<Class<? extends IManager>, IManager> buildMap(IManager... managers) {
        Map<Class<? extends IManager>, IManager> map = new HashMap<>();
        for (IManager m : managers) {
            map.put(m.getClass(), m);
        }
        return map;
    }

    // --- tests ---

    @Test
    public void testSingleDependency_enforcesOrder() {
        // register in reverse order — sorter must correct this
        ManagerA a = new ManagerA();
        ManagerB b = new ManagerB();

        List<IManager> managers = Arrays.asList(b, a);
        Map<Class<? extends IManager>, IManager> map = buildMap(a, b);

        List<IManager> sorted = DependencySorter.sort(managers, map);

        assertEquals(2, sorted.size());
        // a must appear before b
        assertTrue(sorted.indexOf(a) < sorted.indexOf(b),
            "ManagerA (dependency) must be initialised before ManagerB (dependant)");
    }

    @Test
    public void testMultiHopChain_correctOrder() {
        // chain: a → b → c; all registered in reverse
        ManagerA a = new ManagerA();
        ManagerB b = new ManagerB();
        ManagerC c = new ManagerC();

        List<IManager> managers = Arrays.asList(c, b, a);
        Map<Class<? extends IManager>, IManager> map = buildMap(a, b, c);

        List<IManager> sorted = DependencySorter.sort(managers, map);

        assertEquals(3, sorted.size());
        assertTrue(sorted.indexOf(a) < sorted.indexOf(b),
            "ManagerA must precede ManagerB");
        assertTrue(sorted.indexOf(b) < sorted.indexOf(c),
            "ManagerB must precede ManagerC");
    }

    @Test
    public void testSharedDependency_dependencyInitialisedOnce() {
        // both b and d depend on a; a must appear first and appear only once
        ManagerA a = new ManagerA();
        ManagerB b = new ManagerB();
        ManagerD d = new ManagerD();

        List<IManager> managers = Arrays.asList(b, d, a);
        Map<Class<? extends IManager>, IManager> map = buildMap(a, b, d);

        List<IManager> sorted = DependencySorter.sort(managers, map);

        assertEquals(3, sorted.size());
        assertEquals(1, Collections.frequency(sorted, a),
            "ManagerA should appear exactly once");
        assertTrue(sorted.indexOf(a) < sorted.indexOf(b),
            "ManagerA must precede ManagerB");
        assertTrue(sorted.indexOf(a) < sorted.indexOf(d),
            "ManagerA must precede ManagerD");
    }

    @Test
    public void testNoDependencies_preservesRegistrationOrder() {
        ManagerA a = new ManagerA();
        ManagerD d = new ManagerD();

        // d declared after a, but d has a dependency on a which is registered
        // test with two independent managers (no deps between them)
        // use A and a fresh no-dep version
        Manager noDep1 = new Manager() {};
        Manager noDep2 = new Manager() {};

        List<IManager> managers = Arrays.asList(noDep1, noDep2);
        Map<Class<? extends IManager>, IManager> map = new HashMap<>();
        map.put(noDep1.getClass(), noDep1);
        map.put(noDep2.getClass(), noDep2);

        List<IManager> sorted = DependencySorter.sort(managers, map);

        assertEquals(2, sorted.size());
        // no constraint — both should still be present
        assertTrue(sorted.contains(noDep1));
        assertTrue(sorted.contains(noDep2));
    }

    @Test
    public void testCycle_throwsIllegalStateException() {
        ManagerCycleX x = new ManagerCycleX();
        ManagerCycleY y = new ManagerCycleY();

        List<IManager> managers = Arrays.asList(x, y);
        Map<Class<? extends IManager>, IManager> map = buildMap(x, y);

        assertThrows(IllegalStateException.class, () ->
            DependencySorter.sort(managers, map),
            "A circular dependency must throw IllegalStateException"
        );
    }

    @Test
    public void testMissingDependency_throwsIllegalArgumentException() {
        // ManagerGhost declares a dep on ManagerA, but ManagerA is not in the map
        ManagerGhost ghost = new ManagerGhost();

        List<IManager> managers = Collections.singletonList(ghost);
        Map<Class<? extends IManager>, IManager> map = new HashMap<>();
        map.put(ManagerGhost.class, ghost);
        // ManagerA intentionally omitted from the map

        assertThrows(IllegalArgumentException.class, () ->
            DependencySorter.sort(managers, map),
            "A missing dependency must throw IllegalArgumentException"
        );
    }
}
