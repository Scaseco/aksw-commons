package org.aksw.commons.util.closeable;

public class AutoCloseableWrapperBase<T extends AutoCloseable>
    implements AutoCloseable
{
    protected T delegate;

    public AutoCloseableWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    protected T getDelegate() {
        return delegate;
    }

    @Override
    public void close() throws Exception {
        T tmp = getDelegate();
        close(tmp);
    }

    public static void close(AutoCloseable autoCloseable) throws Exception {
        // Wrapping the NULL-check in a method protects against concurrent modification of the reference.
        if (autoCloseable != null) {
            autoCloseable.close();
        }
    }
}
