package dev.mccue.resolve.core;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MinimizedExclusionsTest {
    @Test
    public void emptyExclusionsToSet() {
        assertEquals(MinimizedExclusions.ZERO.toSet(), Set.of());
    }

    @Test
    public void allExclusionsToSet() {
        assertEquals(MinimizedExclusions.ONE.toSet(), Set.of(
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

}
