package dev.mccue.resolve.util;

import java.io.IOException;
import java.io.InputStream;

public final class ProgressTrackingInputStream extends InputStream {
    private final InputStream inputStream;
    private final long expectedNumberOfBytes;
    private long bytesRead = 0;

    public ProgressTrackingInputStream(InputStream inputStream, long expectedNumberOfBytes) {
        this.expectedNumberOfBytes = expectedNumberOfBytes;
        this.inputStream = inputStream;
    }

    public long bytesRead() {
        return bytesRead;
    }

    public long expectedNumberOfBytes() {
        return expectedNumberOfBytes;
    }

    @Override
    public int read() throws IOException {
        bytesRead++;
        return this.inputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        bytesRead += len;
        return this.inputStream.read(b, off, len);
    }
}
