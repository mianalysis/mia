/*
 * "Concave" hulls by Glenn Hudson and Matt Duckham
 *
 * Source code downloaded from https://archive.md/l3Un5#selection-571.0-587.218 on 3rd November 2021.
 *
 * - This software is Copyright (C) 2008 Glenn Hudson released under Gnu Public License (GPL). Under 
 *   GPL you are free to use, modify, and redistribute the software. Please acknowledge Glenn Hudson 
 *   and Matt Duckham as the source of this software if you do use or adapt the code in further research 
 *   or other work. For full details of GPL see http://www.gnu.org/licenses/gpl-3.0.txt.
 * - This software comes with no warranty of any kind, expressed or implied.
 * 
 * A paper with full details of the characteristic hulls algorithm is published in Pattern Recognition.
 * Duckham, M., Kulik, L., Worboys, M.F., Galton, A. (2008) Efficient generation of simple polygons for
 * characterizing the shape of a set of points in the plane. Pattern Recognition v41, 3224-3236
 *
 * The software was developed by Glenn Hudson while working with me as an RA. The characteristic shapes 
 * algorithm is collaborative work between Matt Duckham, Lars Kulik, Antony Galton, and Mike Worboys.
 * 
 */

package signalprocesser.shared;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JFrame;

public class StatusDialog extends javax.swing.JDialog {
    
    public static void start(Window parent, String title, String caption, final Thread thread) {
        StatusDialog _dialog;
        if ( parent instanceof Frame ) {
            _dialog = new StatusDialog((Frame)parent,  title, caption);
        } else if ( parent instanceof Dialog ) {
            _dialog = new StatusDialog((Dialog)parent,  title, caption);
        } else {
            throw new RuntimeException("Unknown window type " + parent.getClass().getName());
        }
        final StatusDialog dialog = _dialog;
        Thread dialogthread = new Thread() {
            public void run() {
                dialog.setVisible(true);
            }
        };
        dialogthread.start();
        
        // Start thread to run job
        new Thread() {
            public void run() {
                // Start the thread
                thread.start();
                synchronized ( thread ) {
                    try {
                        thread.wait();
                    } catch ( InterruptedException e ) {
                        // Do nothing as the user has selected this
                        throw new RuntimeException(e);
                    }
                }
                
                // Close the dialog
                dialog.setVisible(false);
                dialog.dispose();
            }
        }.start();
    }
    
    public StatusDialog(Frame parent, String title, String caption) {
        super(parent, true);
        initComponents();
        this.setTitle(title);
        lblCaption.setText(caption);
    }
    
    public StatusDialog(Dialog parent, String title, String caption) {
        super(parent, true);
        initComponents();
        this.setTitle(title);
        lblCaption.setText(caption);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        panelCenter = new javax.swing.JPanel();
        lblCaption = new javax.swing.JLabel();
        panelGap = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        panelCenter.setLayout(new java.awt.BorderLayout());

        panelCenter.add(lblCaption, java.awt.BorderLayout.NORTH);

        panelGap.setLayout(null);

        panelGap.setPreferredSize(new java.awt.Dimension(220, 5));
        panelCenter.add(panelGap, java.awt.BorderLayout.CENTER);

        progressBar.setIndeterminate(true);
        panelCenter.add(progressBar, java.awt.BorderLayout.SOUTH);

        getContentPane().add(panelCenter, new java.awt.GridBagConstraints());

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-319)/2, (screenSize.height-127)/2, 319, 127);
    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCaption;
    private javax.swing.JPanel panelCenter;
    private javax.swing.JPanel panelGap;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
    
}
