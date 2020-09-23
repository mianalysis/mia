package wbif.sjx.MIA.Object.References.Collections;

import java.util.Collection;

import wbif.sjx.MIA.Object.References.Abstract.Ref;

public interface RefCollection<R extends Ref>  {
    public Collection<R> values();
    public boolean add(R ref);
}
