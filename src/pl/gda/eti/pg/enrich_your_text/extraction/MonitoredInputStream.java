package pl.gda.eti.pg.enrich_your_text.extraction;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * http://stackoverflow.com/questions/3100013/java-sax-parser-progress-monitoring
 *
 * A class that monitors the read progress of an input stream.
 *
 * @author Hermia Yeung "Sheepy"
 * @since 2012-04-05 18:42
 */
public class MonitoredInputStream extends FilterInputStream {

    private volatile long mark = 0;
    private volatile long lastTriggeredLocation = 0;
    private volatile long location = 0;
    private final int threshold;
    private final List<ChangeListener> listeners = new ArrayList<>(4);

    /**
     * Creates a MonitoredInputStream over an underlying input stream.
     *
     * @param in Underlying input stream, should be non-null because of no
     * public setter
     * @param threshold Min. position change (in byte) to trigger change event.
     */
    public MonitoredInputStream(InputStream in, int threshold) {
        super(in);
        this.threshold = threshold;
    }

    /**
     * Creates a MonitoredInputStream over an underlying input stream. Default
     * threshold is 16KB, small threshold may impact performance impact on
     * larger streams.
     *
     * @param in Underlying input stream, should be non-null because of no
     * public setter
     */
    public MonitoredInputStream(InputStream in) {
        super(in);
        this.threshold = WikipediaExtractor.EXTRACTION_STEP_DIVISOR * 16;
    }

    public void addChangeListener(ChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public int getProgress() {
        return (int)(location / WikipediaExtractor.EXTRACTION_STEP_DIVISOR);
    }

    protected void triggerChanged(final long location) {
        if (threshold > 0 && Math.abs(location - lastTriggeredLocation) < threshold) {
            return;
        }
        lastTriggeredLocation = location;
        if (listeners.size() <= 0) {
            return;
        }
        try {
            final ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener l : listeners) {
                l.stateChanged(evt);
            }
        } catch (ConcurrentModificationException e) {
            triggerChanged(location);  // List changed? Let's re-try.
        }
    }

    @Override
    public int read() throws IOException {
        final int i = super.read();
        if (i != -1) {
            triggerChanged(location++);
        }
        return i;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int i = super.read(b, off, len);
        if (i > 0) {
            triggerChanged(location += i);
        }
        return i;
    }

    @Override
    public long skip(long n) throws IOException {
        final long i = super.skip(n);
        if (i > 0) {
            triggerChanged(location += i);
        }
        return i;
    }

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
        mark = location;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        if (location != mark) {
            triggerChanged(location = mark);
        }
    }
}
