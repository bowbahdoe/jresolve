package dev.mccue.resolve;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public enum NoOpCache implements Cache {
    INSTANCE;

    @Override
    public Optional<OutputStream> get(List<String> key) {
        return Optional.empty();
    }

    @Override
    public void put(List<String> key, InputStream data) {

    }

    @Override
    public String toString() {
        return "NoOpCache";
    }
}
