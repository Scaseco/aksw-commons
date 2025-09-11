package org.aksw.commons.io.buffer.ring;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.input.ReadableSource;

/**
 * A fixed size buffer with start and end pointers.
 * The read operations increment the start pointer.
 * The fill operation reads from a source and increments the end pointer.
 * Both pointers 'overflow' when reaching the end of the array and start from offset 0 again.
 *
 * If a read operation causes the start and end pointers to meet then the buffer is considered empty.
 * If a fill operation causes the start and end pointers to meet then the buffer is considered full.
 * A flag is used to discriminate those cases.
 *
 * @param <A>
 */
public abstract class RingBufferBase<A>
    implements ReadableSource<A> // XXX ReadableSource is ReadableChannel without IO - naming not optimal because 'source' suggests factory for channels.
{
    protected A buffer;
    protected int bufferLen;

    protected int start;
    protected int end;
    protected boolean isEmpty; // true when start meets end

    public RingBufferBase(A buffer) {
        this(buffer, 0, 0, true);
    }

    public RingBufferBase(A buffer, int start, int end, boolean isEmpty) {
        super();
        this.buffer = Objects.requireNonNull(buffer);
        this.bufferLen = getArrayOps().length(buffer);
        this.start = start;
        this.end = end;
        this.isEmpty = isEmpty;

        if (isEmpty && !(start == end)) {
            throw new IllegalArgumentException("When setting isEmpty=true then it must hold that start==end");
        }
    }

    /** The internal array is not exposed because the implementation may change to e.g. use
     *  an array of arrays to store the data. */
    // public A getArray() {
    //     return buffer;
    // }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     *
     * @param source
     * @param targetAmount Fill the buffer to this amount (NOT the amount to read from the source)
     * @return
     * @throws IOException
     */
    public int fill(ReadableSource<A> source, int targetAmount) throws IOException {
        int result = 0;
        while (available() < targetAmount) {
            int contrib = fill(source);
            if (contrib == 0) {
                // Contribution of 0 bytes means EOF reached
                break;
            }
            result += contrib;
        }
        return result;
    }

    /**
     * Returns the maxmimum number of bytes that can be appended in a single operation
     * before the end marker flips over or reaches the start marker.
     * */
    public int appendCapacity() {
        // If the end marker reaches bufferLen then it is immediately normalized to 0.
        // So the end marker never equals bufferLen.
        int n = start < end || isEmpty
            ? bufferLen - end
            : start - end;
        return n;
    }

    // A value of 0 means that no bytes were read from source
    // This can be due to: source consumed or no capacity left -
    // the return value should indicate the cause.
    public int fill(ReadableSource<A> source) throws IOException {
        // If the end marker reaches bufferLen then it is immediately normalized to 0.
        // So the end marker never equals bufferLen.

        int n;
        if (start < end || isEmpty) {
            // [end, bufferLen)
            int remainingSpace = bufferLen - end;
            n = source.read(buffer, end, remainingSpace);
        } else {
            // [end, start)
            int d = start - end;
            if (d == 0) { // !isEmpty is implied here -  && !isEmpty) {
                n = 0; // no capacity
            } else {
                n = source.read(buffer, end, d);
            }
        }

        incEnd(n);

        if (n < 0) {
            n = 0;
        }

        return n;
    }

    protected void incEnd(int n) {
        if (n > 0) {
            end += n;
            isEmpty = false;
            if (end == bufferLen) {
                end = 0;
            } else if (end > bufferLen) {
                throw new IllegalStateException("Should not happen: End pointer exceeded buffer length");
            }
        }
    }

    /** The number of used bytes - distance between start and end. */
    public int available() {
        return bufferLen - capacity();
    }

    /** The number of unused bytes - distance between end and start. */
    public int capacity() {
        return isEmpty
            ? bufferLen
            : end > start
                ? (bufferLen - end) + start
                : start - end;
    }

    public int length() {
        return bufferLen;
    }

    /** Increment the start pointer by the given amount. Raises an invalid argument exception if the length is
     *  greater than {@link #available()}. */
    public RingBufferBase<A> skip(int length) {
        int n = available();
        if (length > n) {
            throw new IllegalArgumentException("Requested skipping " + length + " elements but only " + n + " available.");
        }
        incStart(length);
        return this;
    }

    /** Take items from the start of the buffer. */
    @Override
    public int read(A tgt, int tgtOffset, int length) throws IOException {
        int savedStart = start;
        int result = preRead(length);
        if (result > 0) {
            getArrayOps().copy(buffer, savedStart, tgt, tgtOffset, result);
        }
        return result;
    }

    /**
     * Returns a number of 'n' bytes that can be read from the current start marker up
     * to the requested length.
     *
     * When this method returns, then the fields of this buffer have been updated to
     * the state after the read.
     * However, the actual data can be accessed by reading 'n' bytes from the original start
     * position. (See implementation of read.)
     */
    protected int preRead(int length) {
        int result;
        if (start == end && isEmpty) {
            result = -1;
        } else {
            if (start < end) {
                int remainingSpace = end - start;
                result = Math.min(remainingSpace, length);
                // getArrayOps().copy(buffer, start, tgt, tgtOffset, result);
            } else {
                int remainingSpace = bufferLen - start;
                result = Math.min(remainingSpace, length);
                // getArrayOps().copy(buffer, start, tgt, tgtOffset, result);
            }

            incStart(result);
        }
        return result;
    }

    protected void incStart(int amount) {
        if (amount > 0) {
            start += amount;
            if (start == bufferLen) {
                start = 0;
            }

            if (start == end) {
                start = 0;
                end = 0;
                isEmpty = true;
            }
        }
    }

    /**
     * Set the buffer to a new size.
     *
     * @implNote
     *   Internally copies existing data to a new array.
     *   The new size must be at least 1 and greater or equal to the available data, otherwise
     *   an {@link IllegalArgumentException} is raised.
     */
    public void resize(int newSize) {
        if (newSize < 1) {
            throw new IllegalArgumentException("Requested new size (" + newSize + ") must be at least 1");
        }

        int avail = available();
        if (newSize < avail) {
            throw new IllegalArgumentException("Requested new size (" + newSize + ") must be greater than or equal to the amount of available data (" + avail + ")");
        }

        A newBuffer = getArrayOps().create(newSize);
        try {
            ReadableChannels.readFully(this, newBuffer, 0, avail);
        } catch (IOException e) {
            throw new RuntimeException("Should not happen", e);
        }

        this.buffer = newBuffer;
        this.bufferLen = newSize;
        this.start = 0;
        this.end = avail;
        this.isEmpty = avail == 0;
    }
}
