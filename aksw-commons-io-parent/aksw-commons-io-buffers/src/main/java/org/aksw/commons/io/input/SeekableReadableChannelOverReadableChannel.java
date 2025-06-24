package org.aksw.commons.io.input;

import java.io.IOException;

public class SeekableReadableChannelOverReadableChannel<A>
    extends ReadableChannelDecoratorBase<A, ReadableChannel<A>>
    implements SeekableReadableChannel<A>
{
    protected long basePos;
    protected long relPos;
    protected long requestedPos;

    public SeekableReadableChannelOverReadableChannel(ReadableChannel<A> delegate, long basePos) {
        super(delegate);
        this.basePos = basePos;
        this.requestedPos = -1;
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        if (requestedPos != -1) {
            long delta = requestedPos - basePos;
            if (delta < 0) {
                throw new IllegalStateException("Requested position is before base offset");
            } else if (delta > 0) {
                long n = ReadableChannels.skip(delegate, delta, array, position, length);
                relPos += n;
            }
        }

        int result = delegate.read(array, position, length);
        if (result > 0) {
            relPos += result;
        }
        return result;
    }

    @Override
    public void position(long pos) {
        this.requestedPos = pos;
    }

    @Override
    public SeekableReadableChannel<A> cloneObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position() {
        return requestedPos >= 0 ? requestedPos : basePos + relPos;
    }
}
