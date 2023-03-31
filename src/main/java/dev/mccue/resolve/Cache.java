package dev.mccue.resolve;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public interface Cache {
    Path fetchIfAbsent(List<String> key, Supplier<InputStream> data);
}
