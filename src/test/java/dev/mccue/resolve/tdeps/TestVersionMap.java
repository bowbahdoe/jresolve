package dev.mccue.resolve.tdeps;

import dev.mccue.resolve.core.Module;
import dev.mccue.resolve.core.ModuleName;
import dev.mccue.resolve.core.Organization;
import dev.mccue.resolve.core.Version;
import dev.mccue.resolve.util.LL;
import org.junit.jupiter.api.Test;

import java.util.List;

public final class TestVersionMap {
    @Test
    public void testVersionMap() {
        var versionMap = new VersionMap();
        versionMap.addVersion(
                new Lib("apache"),
                new Coordinate() {
                    @Override
                    public CoordinateId id() {
                        return new CoordinateId("mvn/versions: 1..3");
                    }
                },
                new Path(new LL.Nil<>()),
                new CoordinateId("mvn/versions: 1..3")
        );
        System.out.println(versionMap);
    }
}
