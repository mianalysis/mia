package io.github.mianalysis.MIA.Object.References.Collections;

import java.util.Collection;

import io.github.mianalysis.MIA.Object.References.Abstract.Ref;

public interface RefCollection<R extends Ref>  {
    public Collection<R> values();
    public boolean add(R ref);
}
