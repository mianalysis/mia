package io.github.mianalysis.mia.object;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkspacesTest {

    @Test
    public void testGetNewWorkspace() {
        Workspaces collection = new Workspaces();

        // Checking the collection is empty to start with
        assertEquals(0,collection.size());

        // Adding a Workspace
        Workspace workspace = collection.getNewWorkspace(new File("test1"),2);
        assertEquals(1,collection.size());
        assertEquals(1,workspace.getID());
        assertEquals("test1",workspace.getMetadata().getFilename());
        assertEquals(2,workspace.getMetadata().getSeriesNumber());

        // Adding another Workspace
        workspace = collection.getNewWorkspace(new File("second test"),6);
        assertEquals(2,collection.size());
        assertEquals(2,workspace.getID());
        assertEquals("second test",workspace.getMetadata().getFilename());
        assertEquals(6,workspace.getMetadata().getSeriesNumber());

    }
}