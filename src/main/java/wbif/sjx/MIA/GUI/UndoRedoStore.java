package wbif.sjx.MIA.GUI;

import java.util.LinkedList;

import wbif.sjx.MIA.Module.ModuleCollection;

public class UndoRedoStore {
    private int limit = 100;
    private LinkedList<ModuleCollection> undoStore = new LinkedList<>();
    private LinkedList<ModuleCollection> redoStore = new LinkedList<>();

    public void addUndo(ModuleCollection modules) {
        undoStore.addFirst(modules.duplicate());
        checkLimit();

        // Clear the redo store
        redoStore = new LinkedList<>();

    }

    public ModuleCollection getNextUndo(ModuleCollection modules) {
        if (undoStore.size() == 0) return null;

        if (modules != null) redoStore.addFirst(modules);
        return undoStore.pop().duplicate();

    }

    public ModuleCollection getNextRedo(ModuleCollection modules) {
        if (redoStore.size() == 0) return null;

        if (modules != null) undoStore.addFirst(modules);
        return redoStore.pop().duplicate();

    }

    public void reset() {
        undoStore = new LinkedList<>();
        redoStore = new LinkedList<>();
    }

    void checkLimit() {
        while (undoStore.size() > limit) {
            undoStore.removeLast();
        }
    }

    public int getUndoSize() {
        return undoStore.size();
    }

    public int getRedoSize() {
        return redoStore.size();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
