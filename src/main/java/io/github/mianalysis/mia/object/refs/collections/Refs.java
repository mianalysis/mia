package io.github.mianalysis.mia.object.refs.collections;

import java.util.Collection;

import io.github.mianalysis.mia.object.refs.abstrakt.Ref;

public interface Refs<R extends Ref>  {
    public Collection<R> values();
    public boolean add(R ref);
}
