package dev.mccue.resolve;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {
    @Test
    public void versionComparison() {
        assertEquals(0, new Version("1.7").compareTo(new Version("1.7")));
        assertTrue(new Version("1.7").compareTo(new Version("1.8")) < 0);
        assertTrue(new Version("1.8").compareTo(new Version("1.7")) > 0);
    }
}
