package dev.mccue.resolve;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public interface Cache {
    Optional<OutputStream> get(List<String> key);

    void put(List<String> key, InputStream data);
}
