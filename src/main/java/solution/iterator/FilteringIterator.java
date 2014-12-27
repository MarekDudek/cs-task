package solution.iterator;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilteringIterator<T> implements Iterator<T> {

    private final Iterator<T> unfiltered;
    private final Predicate<T> predicate;

    private T next;

    public FilteringIterator(final Iterator<T> unfiltered, final Predicate<T> predicate)
    {
	this.unfiltered = checkNotNull(unfiltered);
	this.predicate = checkNotNull(predicate);
    }

    @Override
    public boolean hasNext()
    {
	next = findNext();
	if (next == null) {
	    return false;
	} else {
	    return true;
	}
    }

    @Override
    public T next() {
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
