package wbif.sjx.MIA.Module;

import java.util.TreeSet;

public class Category implements Comparable {
    private final String name;
    private final String description;

    private final Category parent;
    private final TreeSet<Category> children = new TreeSet<>();

    public Category(String name, String description, Category parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        if (parent != null) 
            parent.addChild(this);
    }

    public void addChild(Category child) {
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getParent() {
        return parent;
    }

    public TreeSet<Category> getChildren() {
        return children;
    }

    @Override
    public int compareTo(Object o) {
        String n1 = getName();
        String n2 = ((Category) o).getName();

        return n1.compareTo(n2);

    }
}
