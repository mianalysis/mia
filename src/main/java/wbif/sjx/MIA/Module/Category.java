package wbif.sjx.MIA.Module;

import java.util.TreeSet;

import wbif.sjx.MIA.Object.References.Abstract.Ref;

public class Category extends Ref implements Comparable {
    private final Category parent;
    private final TreeSet<Category> children = new TreeSet<>();
    private final boolean showInMenu;

    public Category(String name, String description, Category parent) {
        super(name);
        this.setDescription(description);
        this.parent = parent;
        if (parent != null)
            parent.addChild(this);
        this.showInMenu = true;
    }

    public Category(String name, String description, Category parent, boolean showInMenu) {
        super(name);
        this.setDescription(description);
        this.parent = parent;
        if (parent != null)
            parent.addChild(this);
        this.showInMenu = showInMenu;
    }

    public void addChild(Category child) {
        children.add(child);
    }

    public Category getParent() {
        return parent;
    }

    public TreeSet<Category> getChildren() {
        return children;
    }

    public boolean showInMenu() {
        return showInMenu;
    }

    @Override
    public int compareTo(Object o) {
        String n1 = getName();
        String n2 = ((Category) o).getName();

        return n1.compareTo(n2);

    }
}
