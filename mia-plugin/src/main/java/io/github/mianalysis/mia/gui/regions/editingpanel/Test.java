package io.github.mianalysis.mia.gui.regions.editingpanel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class Test {
    public static void main(String[] args) { 
        // Create a JFrame (the main window for the application). 
        JFrame frame = new JFrame("JLayeredPane Example"); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        frame.setSize(400, 400); 
  
        // Create a JLayeredPane to manage the layering of components. 
        JLayeredPane layeredPane = new JLayeredPane(); 
        frame.add(layeredPane); // Add the JLayeredPane to the JFrame. 
  
        // Create three colored panels to add to the layered pane. 
        JPanel panel1 = createColoredPanel(Color.RED, 100, 100, 200, 200); 
        JPanel panel2 = createColoredPanel(Color.GREEN, 150, 150, 200, 200); 
        JPanel panel3 = createColoredPanel(Color.BLUE, 200, 200, 200, 200); 
  
        // Add the panels to the layered pane with different layer values. 
        // The layers determine the stacking order of the panels. 
        layeredPane.add(panel1, JLayeredPane.DEFAULT_LAYER); 
        layeredPane.add(panel2, JLayeredPane.PALETTE_LAYER); 
        layeredPane.add(panel3, JLayeredPane.MODAL_LAYER); 
  
        frame.setVisible(true); // Make the JFrame visible. 
    } 
  
    private static JPanel createColoredPanel(Color color, int x, int y, int width, int height) { 
        // Create a colored JPanel with specified color and position. 
        JPanel panel = new JPanel(); 
        panel.setBackground(color); 
        panel.setBounds(x, y, width, height); 
        return panel; 
    } 
}
