package io.github.mianalysis.mia.module;

import java.util.TreeSet;

import io.github.mianalysis.mia.object.refs.abstrakt.Ref;

public class Category extends Ref implements Comparable {
    private final Category parent;
    private final TreeSet<Category> children = new TreeSet<>();
    private final boolean showInMenu;
    private int childModuleCount = 0;

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

    public void resetModuleCount(boolean processChildren) {
        childModuleCount = 0;

        // Removing the count from all child categories
        if (processChildren)
            for (Category childCategory:children)
                childCategory.resetModuleCount(processChildren);
    }

    public void incrementModuleCount(boolean processParent) {
        childModuleCount++;

        // Adding this module to all parent's counts
        if (processParent && parent != null)
            parent.incrementModuleCount(processParent);
    }

    public int getModuleCount() {
        return childModuleCount;
    }

    @Override
    public int compareTo(Object o) {
        String n1 = getName();
        String n2 = ((Category) o).getName();

        return n1.compareTo(n2);

    }
}
