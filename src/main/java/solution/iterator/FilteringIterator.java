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
	do
	{
	    if (unfiltered.hasNext())
	    {
		next = unfiltered.next();
		if (predicate.test(next)) {
		    return true;
		}
	    }
	    else {
		return false;
	    }
	} while (true);
    }

    @Override
    public T next() {
	return next;
    }
}
