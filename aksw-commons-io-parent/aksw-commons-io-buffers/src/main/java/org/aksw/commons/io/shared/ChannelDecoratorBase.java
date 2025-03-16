package org.aksw.commons.io.shared;

import java.io.IOException;
import java.nio.channels.Channel;

import org.aksw.commons.util.closeable.AutoCloseableWrapperBase;

public class ChannelDecoratorBase<T extends Channel>
    extends AutoCloseableWrapperBase<T>
    implements Channel
{
    public ChannelDecoratorBase(T decoratee) {
        super(decoratee);
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
