package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.shared.ChannelDecoratorBase;

public class SeekableReadableChannelWithOffset<A, X extends SeekableReadableChannel<A>>
    extends ChannelDecoratorBase<X>
    implements SeekableReadableChannel<A>
{
    protected long offset;

    public SeekableReadableChannelWithOffset(X delegate, long offset) {
        super(delegate);
        this.offset = offset;
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        return delegate.read(array, position, length);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return delegate.getArrayOps();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public long position() throws IOException {
        long physicalPos = delegate.position();
        long result = physicalPos - offset;
        return result;
    }

    @Override
    public void position(long position) throws IOException {
        long p = offset + position;
        delegate.position(p);
    }

    @Override
    public SeekableReadableChannel<A> cloneObject() {
        return new SeekableReadableChannelWithOffset<>(delegate.cloneObject(), offset);
    }
}
