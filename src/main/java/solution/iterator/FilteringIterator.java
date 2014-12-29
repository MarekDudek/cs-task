package solution.iterator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilteringIterator<T> implements Iterator<T> {

    private final Iterator<T> unfiltered;
    private final Predicate<T> predicate;

    private T next;
    private boolean consumed = true;

    public FilteringIterator(final Iterator<T> unfiltered, final Predicate<T> predicate)
    {
        this.unfiltered = checkNotNull(unfiltered);
        this.predicate = checkNotNull(predicate);
    }

    @Override
    public boolean hasNext()
    {
        if (consumed) {
            next = findNext();
            consumed = false;
        }

        return next != null;
    }

    @Override
    public T next()
    {
        if (consumed) {
            next = findNext();
        }
        consumed = true;

        return next;
    }

    private T findNext()
    {
        do {
            if (unfiltered.hasNext()) {
                final T candidate = unfiltered.next();
                if (predicate.test(candidate)) {
                    return candidate;
                }
            } else {
                return null;
            }
        } while (true);
    }
}
