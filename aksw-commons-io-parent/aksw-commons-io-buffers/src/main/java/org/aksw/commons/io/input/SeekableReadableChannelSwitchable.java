package org.aksw.commons.io.input;

import java.io.IOException;

public class SeekableReadableChannelSwitchable<A>
    extends ReadableChannelSwitchableBase<A, SeekableReadableChannel<A>>
    implements SeekableReadableChannel<A>
{
    public SeekableReadableChannelSwitchable(SeekableReadableChannel<A> delegate) {
        super(delegate);
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
        return new SeekableReadableChannelSwitchable<>(delegate.cloneObject());
    }
}
