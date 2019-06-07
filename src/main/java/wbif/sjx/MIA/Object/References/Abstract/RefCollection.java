package wbif.sjx.MIA.Object.References.Abstract;

import java.util.Collection;

public interface RefCollection<R extends Ref> {
    public Collection<R> values();
    public boolean add(R ref);
}
