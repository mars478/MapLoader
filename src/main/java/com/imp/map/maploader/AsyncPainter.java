package com.imp.map.maploader;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncPainter implements ImageObserver {

    protected final AtomicInteger lock;
    protected final DoneHandler onDone;
    protected final ErrorHandler onError;
    protected final Thread owner;

    public AsyncPainter(int size) {
        this(Thread.currentThread(), null, null);
        counterSet(size);
    }

    public AsyncPainter(Thread owner, DoneHandler onDone, ErrorHandler onError) {
        this.lock = new AtomicInteger(0);
        this.onDone = onDone;
        this.onError = onError;
        this.owner = owner;
    }

    @Override
    public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
        if ((flags & ALLBITS) != 0) {
            counterDecrement();
            return false; // done
        }
        return true; // drawing
    }

    public void lockReady() {
        if (lock.get() > 0) {
            synchronized (owner) {
                try {
                    owner.wait();
                } catch (InterruptedException ex) {
                    if (onError != null) {
                        onError.onError(ex);
                    }
                }
            }
        }
    }

    public int counterDecrement() {
        int ret = lock.decrementAndGet();
        if (ret == 0) {
            done();
        } else if (ret < 0) {
            counterReset();
        }
        return ret;
    }

    public void done() {
        counterReset();
        try {
            synchronized (owner) {
                owner.notify();
            }

            if (onDone != null) {
                onDone.onDone();
            }
        } catch (Exception e) {
            if (onError != null) {
                onError.onError(e);
            }
        }
    }

    public final AsyncPainter counterSet(int x) {
        lock.set(x);
        return this;
    }

    protected final AsyncPainter counterReset() {
        return counterSet(0);
    }

    public interface ErrorHandler {

        public void onError(Exception e);
    }

    public interface DoneHandler {

        public void onDone();
    }
}
