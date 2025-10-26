package org.tillerino.mormon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

class ResultSetIterator<T> implements Iterator<T> {
    private final ResultSet set;
    private final Mapping<? extends T> mapping;
    private boolean hasNext = false;
    private boolean consumed = true;

    ResultSetIterator(ResultSet set, Mapping<? extends T> mapping) {
        this.set = set;
        this.mapping = mapping;
    }

    @Override
    public boolean hasNext() {
        if (consumed) {
            try {
                hasNext = set.next();
                consumed = false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return hasNext;
    }

    @Override
    public T next() {
        consumed = true;
        try {
            T instance = mapping.constructor().newInstance();

            mapping.get(instance, set);

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
