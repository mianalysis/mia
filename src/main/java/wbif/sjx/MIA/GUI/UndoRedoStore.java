package wbif.sjx.MIA.GUI;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.ModuleCollection;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

public class UndoRedoStore {
    private int limit = 5;
    private LinkedList<ModuleCollection> undoStore = new LinkedList<>();
    private LinkedList<ModuleCollection> redoStore = new LinkedList<>();

    public void addUndo(ModuleCollection modules) {
        try {
            undoStore.addFirst(modules.duplicate());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        MIA.log.writeDebug("(Add) Undo size = "+undoStore.size()+", redo size = "+redoStore.size());
    }

    public ModuleCollection getNextUndo() {
        if (undoStore.size() == 0) return null;

        try {
            ModuleCollection modules = undoStore.pop();
            redoStore.addFirst(modules);
            MIA.log.writeDebug("(Undo) Undo size = "+undoStore.size()+", redo size = "+redoStore.size());
            return modules.duplicate();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ModuleCollection getNextRedo() {
        if (redoStore.size() == 0) return null;

        try {
            ModuleCollection modules = redoStore.pop();
            undoStore.addFirst(modules);
            MIA.log.writeDebug("(Redo) Undo size = "+undoStore.size()+", redo size = "+redoStore.size());
            return modules.duplicate();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    void checkLimit() {
        while (undoStore.size() > limit) {
            undoStore.removeLast();
        }

        while (redoStore.size() > limit) {
            redoStore.removeLast();
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
