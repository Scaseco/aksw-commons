package org.aksw.commons.io.buffer.ring;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.ToIntFunction;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.ReadableChannels;
import org.apache.commons.io.IOUtils;

/** Adapter of {@link RingBufferBase} for bytes. */
public class RingBufferForBytes
    extends RingBufferBase<byte[]>
{
     public RingBufferForBytes(int size) {
        super(new byte[size]);
    }

    protected RingBufferForBytes(byte[] data, int start, int end, boolean isEmpty) {
        super(data, start, end, isEmpty);
    }

    public byte get(int offset) {
        int pos = start + offset;
        if (pos >= bufferLen) {
            pos -= bufferLen;
        }

        byte result = buffer[pos];
        return result;
    }

    public int read(ByteBuffer out) {
        int length = out.remaining();
        int savedStart = start;
        int result = preRead(length);
        if (result > 0) {
            out.put(buffer, savedStart, result);
        }
        return result;
    }

    @Override
    public RingBufferForBytes skip(int length) {
        super.skip(length);
        return this;
    }

    /**
     * Provide a byte buffer view over the ring buffer to
     * directly write bytes into a backing array of this buffer.
     * The buffer provides place for at most capacity() bytes.
     */
    public int append(int maxRemaining, ToIntFunction<ByteBuffer> callback) {
        int cap = appendCapacity();
        int len = Math.min(maxRemaining, cap);

        int e = this.end;
        // Could use a static byte buffer instance!
        ByteBuffer bb = ByteBuffer.wrap(buffer, e, len);
        int n = callback.applyAsInt(bb);
        incEnd(n);
        return n;
    }

    /** Append data from the byte buffer. */
//    public int fill(ByteBuffer in, int pos, int length) {
//        return new ReadableSourceOverByteBuffer(in, pos).read(buffer, pos, length);
//    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    /**
     * Returns a new instance with the same data but a copy of the start and end pointers.
     * The purpose is to support reading data from the buffer and eventually reset the state.
     */
    // XXX Likely mark() and reset() would be the cleaner approach.
    public RingBufferForBytes shallowClone() {
        return new RingBufferForBytes(buffer, start, end, isEmpty);
    }

    // XXX Note: Clones will retain their own array copy when resize is called.
    public RingBufferForBytes shallowClone(int customStart, int length) {
        if (length > bufferLen) {
            throw new IllegalArgumentException("Length cannot be larger than the buffer");
        }

        boolean customIsEmpty = length == 0;
        int customEnd = customStart + length;
        if (customEnd >= bufferLen) {
            customEnd -= bufferLen;
        }
        return new RingBufferForBytes(buffer, customStart, customEnd, customIsEmpty);
    }

    @Override
    public String toString() {
        String result = toString(StandardCharsets.UTF_8);
        return result;
    }

    public String toString(Charset charset) {
        String result;
        try (InputStream in = ReadableChannels.newInputStream(ReadableChannels.newChannel(shallowClone()))) {
            result = IOUtils.toString(in, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
