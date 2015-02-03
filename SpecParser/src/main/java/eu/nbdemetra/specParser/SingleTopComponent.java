/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays the SpecViewer.
 */
@ConvertAsProperties(
        dtd = "-//eu.nbdemetra.x13spec//SingleSpecWindow//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SingleSpecWindowTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "eu.nbdemetra.x13spec.SingleSpecWindowTopComponent")

@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SingleSpecWindowAction",
        preferredID = "SingleSpecWindowTopComponent"
)
@Messages({
    "CTL_SingleSpecWindowAction=SingleSpecWindow",
    "CTL_SingleSpecWindowTopComponent=SingleSpecWindow",
    "HINT_SingleSpecWindowTopComponent=This is a SingleSpecWindow"
})
public final class SingleTopComponent extends TopComponent {

    private boolean open;

    public SingleTopComponent() {

        initComponents();
        setName(Bundle.CTL_SingleSpecWindowTopComponent());
        setToolTipText(Bundle.HINT_SingleSpecWindowTopComponent());
        open = true;

        //Button for save WinX13 Spec to file ends with .spc
        JButton load = new JButton(new AbstractAction("Load WinX13Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new MyFilter(".spc"));
                fc.setAcceptAllFileFilterUsed(false);

                int state = fc.showOpenDialog(null);

                if (state == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

                    try {
                        FileReader f = new FileReader(file);
                        StringBuilder s;
                        try (BufferedReader br = new BufferedReader(f)) {
                            s = new StringBuilder();
                            String zeile;
                            while ((zeile = br.readLine()) != null) {
                                s.append(zeile);
                                s.append("\n");
                            }
                        }

                        SpecCollector sp = specViewer.getSpecCollector();
                        sp.setWinX13Spec(s.toString());
                        sp.translate(TranslationTo_Type.JDSpec);
                        specViewer.refresh(sp);

                    } catch (FileNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } 
//                    else {
//
////                    errormessage.setText("Auswahl abgebrochen");
//                }
            }
        });
        JButton save = new JButton(new AbstractAction("Save WinX13Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {

                String path = System.getProperty("user.home");
                JFileChooser chooser = new JFileChooser(path);
                chooser.setFileFilter(new MyFilter(".spc"));
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.showSaveDialog(null);
                try {
                    File file = chooser.getSelectedFile();
                    FileWriter fw = new FileWriter(file);
                    fw.write(specViewer.getSpecCollector().getWinX13Spec());
                    fw.close();
                    if (!file.toString().endsWith(".spc")) {
                        file.renameTo(new File(file.toString() + ".spc"));
                    }
                } catch (IOException ex) {
//                    winX13Text.setText("Nothing happend");
                }
            }
        });
        //Button for refresh winX13Spec from JD+ Spec
        JButton refreshX13 = new JButton(new AbstractAction("Refresh WinX13Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {

                JOptionPane.showInputDialog(new ImageIcon("C:/Daten/Bastelgarage (290).gif"));
            }
        });
        //Button for refresh JD+Spec from WinX13Spec
        JButton refreshJD = new JButton(new AbstractAction("Refresh JD+ Spec") {
            @Override
            public void actionPerformed(ActionEvent e) {
//               do nothing
//                JOptionPane.showInputDialog(new ImageIcon("C:/Daten/Bastelgarage (290).gif"));
                SpecCollector sp = specViewer.getSpecCollector();
                sp.setWinX13Spec(specViewer.getWinX13Text());
                sp.translate(TranslationTo_Type.JDSpec);
                specViewer.refresh(sp);
            }
        });

        toolBar.add(load, 0);
        toolBar.add(save, 1);
        toolBar.add(refreshX13, 2);
        toolBar.add(refreshJD, 3);
    }

    public void setSpecView(SpecCollector spec) {

        specViewer = SpecViewer.create(spec);

                //Button for load Winx13 Spec from file
        add(specViewer);
        specViewer.refreshHeader();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();

        setLayout(new java.awt.BorderLayout());

        toolBar.setRollover(true);
        add(toolBar, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
    private SpecViewer specViewer;
    
    public SpecViewer getSpecViewer(){
        return specViewer;
    }

//    public void refreshSpecCollector() {
//
//    }

    @Override
    public void componentOpened() {
        open = true;
    }

    @Override
    public void componentClosed() {
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
