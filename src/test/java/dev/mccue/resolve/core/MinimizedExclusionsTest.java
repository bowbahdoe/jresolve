package dev.mccue.resolve.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class MinimizedExclusionsTest {
    @Test
    public void emptyExclusionsToSet() {
        assertEquals(MinimizedExclusions.NONE.toSet(), Set.of());
    }

    @Test
    public void allExclusionsToSet() {
        assertEquals(MinimizedExclusions.ALL.toSet(), Set.of(
                new Exclusion(Organization.ALL, ModuleName.ALL)
        ));
    }

    @Test
    public void regularExclusionsToSet() {
        var exclusions = MinimizedExclusions.of(Set.of(
                new Exclusion(new Organization("apple"), ModuleName.ALL),
                new Exclusion(Organization.ALL, new ModuleName("metaverse")),
                new Exclusion(new Organization("com.google"), new ModuleName("abc"))
        ));

        assertEquals(exclusions.toSet(), Set.of(
                new Exclusion(new Organization("apple"), ModuleName.ALL),
                new Exclusion(Organization.ALL, new ModuleName("metaverse")),
                new Exclusion(new Organization("com.google"), new ModuleName("abc"))
        ));
    }

    @Test
    public void allModuleExclusionsReplaceMoreSpecificExclusions() {
        assertEquals(
                MinimizedExclusions.of(Set.of(
                        new Exclusion(new Organization("facebook"), new ModuleName("metaverse")),
                        new Exclusion(new Organization("facebook"), ModuleName.ALL)
                )).toSet(),
                Set.of(new Exclusion(new Organization("facebook"), ModuleName.ALL))
        );
    }

    @Test
    public void allOrganizationExclusionsReplaceMoreSpecificExclusions() {
        assertEquals(
                MinimizedExclusions.of(Set.of(
                        new Exclusion(Organization.ALL, new ModuleName("metaverse")),
                        new Exclusion(new Organization("facebook"), new ModuleName("metaverse")),
                        new Exclusion(new Organization("facebook"), new ModuleName("whatever"))
                )).toSet(),
                Set.of(
                        new Exclusion(Organization.ALL, new ModuleName("metaverse")),
                        new Exclusion(new Organization("facebook"), new ModuleName("whatever"))
                )
        );
    }

    @Test
    public void joiningWithAllExclusionsGetsJustAllExclusions() {
        assertEquals(
                MinimizedExclusions.of(Set.of(Exclusion.ALL)),
                MinimizedExclusions.of(Set.of(Exclusion.ALL)).join(
                    MinimizedExclusions.of(
                            Set.of(
                                    new Exclusion(new Organization("facebook"), new ModuleName("metaverse")),
                                    new Exclusion(new Organization("facebook"), new ModuleName("whatever"))
                            )
                    )
                )
        );

        assertEquals(
                MinimizedExclusions.of(Set.of(Exclusion.ALL)),
                MinimizedExclusions.of(
                        Set.of(
                                new Exclusion(new Organization("facebook"), new ModuleName("metaverse")),
                                new Exclusion(new Organization("facebook"), new ModuleName("whatever"))
                        )
                ).join(
                        MinimizedExclusions.of(Set.of(
                                new Exclusion(Organization.ALL, ModuleName.ALL)
                        ))
                )
        );
    }

    @Test
    public void excludeAllExcludesAll() {
        assertFalse(MinimizedExclusions.ALL.shouldInclude(new Organization("facebook"), new ModuleName("metaverse")));
        assertFalse(MinimizedExclusions.ALL.shouldInclude(new Organization("abc"), new ModuleName("def")));
    }

    @Test
    public void excludeNoneExcludesNone() {
        assertTrue(MinimizedExclusions.NONE.shouldInclude(new Organization("facebook"), new ModuleName("metaverse")));
        assertTrue(MinimizedExclusions.NONE.shouldInclude(new Organization("abc"), new ModuleName("def")));
    }

    @Test
    public void excludeSpecificDep() {
        var excl = MinimizedExclusions.of(List.of(
                        new Exclusion(new Organization("facebook"), new ModuleName("metaverse"))
        ));
        assertFalse(excl.shouldInclude(new Organization("facebook"), new ModuleName("metaverse")));
        assertTrue(excl.shouldInclude(new Organization("abc"), new ModuleName("def")));
        assertTrue(excl.shouldInclude(new Organization("facebook"), new ModuleName("def")));
        assertTrue(excl.shouldInclude(new Organization("abc"), new ModuleName("metaverse")));
    }

    @Test
    public void excludeAllWithArtifact() {
        var excl = MinimizedExclusions.of(List.of(
                new Exclusion(new Organization("*"), new ModuleName("metaverse"))
        ));
        assertFalse(excl.shouldInclude(new Organization("facebook"), new ModuleName("metaverse")));
        assertFalse(excl.shouldInclude(new Organization("google"), new ModuleName("metaverse")));
        assertTrue(excl.shouldInclude(new Organization("abc"), new ModuleName("def")));
        assertTrue(excl.shouldInclude(new Organization("facebook"), new ModuleName("def")));
    }

    @Test
    public void excludeAllInGroup() {
        var excl = MinimizedExclusions.of(List.of(
                new Exclusion(new Organization("google"), new ModuleName("*"))
        ));
        assertFalse(excl.shouldInclude(new Organization("google"), new ModuleName("guice")));
        assertFalse(excl.shouldInclude(new Organization("google"), new ModuleName("guava")));
        assertTrue(excl.shouldInclude(new Organization("facebook"), new ModuleName("guice")));
        assertTrue(excl.shouldInclude(new Organization("abc"), new ModuleName("metaverse")));
    }

    @Test
    public void meetWithNone() {
        assertEquals(
                MinimizedExclusions.NONE,
                MinimizedExclusions.of(List.of(
                        new Exclusion(new Organization("abc"), new ModuleName("def"))
                )).meet(MinimizedExclusions.NONE)
        );

        assertEquals(
                MinimizedExclusions.NONE,
                MinimizedExclusions.ALL.meet(MinimizedExclusions.NONE)
        );

        assertEquals(
                MinimizedExclusions.NONE,
                MinimizedExclusions.NONE.meet(MinimizedExclusions.NONE)
        );
    }
}
