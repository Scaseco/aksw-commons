package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.aksw.commons.io.buffer.array.ArrayOps;

public class ReadableSourceOverByteBuffer
    implements ReadableSource<byte[]>
{
    protected ByteBuffer byteBuffer;
    protected int index;

    public ReadableSourceOverByteBuffer(ByteBuffer byteBuffer, int index) {
        super();
        this.byteBuffer = byteBuffer;
        this.index = index;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    @Override
    public int read(byte[] array, int position, int length) throws IOException {
        int remaining = byteBuffer.remaining();
        int n = Math.min(remaining, length);
        if (n > 0) {
            byteBuffer.get(index, array, position, n);
            index += n;
        } else {
            n = -1;
        }
        return n;
    }
}
