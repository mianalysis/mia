package io.github.mianalysis.mia.Object.Refs.Collections;

import java.util.Collection;

import io.github.mianalysis.mia.Object.Refs.Abstract.Ref;

public interface Refs<R extends Ref>  {
    public Collection<R> values();
    public boolean add(R ref);
}
