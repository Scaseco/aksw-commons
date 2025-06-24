package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

public class SeekableReadableChannelDecoratorBase<A, X extends SeekableReadableChannel<A>>
    extends ReadableChannelDecoratorBase<A, X>
    implements SeekableReadableChannel<A>
{
    public SeekableReadableChannelDecoratorBase(X delegate) {
        super(delegate);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return delegate.getArrayOps();
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        return delegate.read(array, position, length);
    }

    @Override
    public long position() throws IOException {
        return delegate.position();
    }

    @Override
    public void position(long pos) throws IOException {
        delegate.position(pos);
    }

    @Override
    public SeekableReadableChannel<A> cloneObject() {
        throw new UnsupportedOperationException("clone not supported");
    }
}
