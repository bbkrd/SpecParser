/* 
 * Copyright 2016 Deutsche Bundesbank
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.nbdemetra.specParser;

import Logic.SpecCollector;
import Administration.SingleSpec;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.utilities.IModifiable;
import eu.nbdemetra.specParser.Miscellaneous.MyFilter;
import eu.nbdemetra.specParser.Miscellaneous.TranslationTo_Type;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * This class is the window for the single mode. 
 * @author Nina Gonschorreck
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

    /** GUI component @see SpecViewer */
    private SpecViewer specViewer;
    /** Identification number of the single window */
    private String id;
    /** path to open a directory for loading and saving, memorize the last directory */
    private static String path = System.getProperty("user.home");
    /** current workspace node, for refreshing the workspace */
    private WsNode wsNode;


    /** 
     * Creates the single window for a single document in the multi mode
     */
    public SingleTopComponent() {
        
        initComponents();
        setName(Bundle.CTL_SingleSpecWindowTopComponent());
        setToolTipText(Bundle.HINT_SingleSpecWindowTopComponent());

        setButtons();
        // button has to be disabled because of overloading the single spec
        load.setEnabled(false);
    }

    /** Creates the single window in the single mode
     * @param ws current Workspace node
     */
    public SingleTopComponent(WsNode ws) {

        this.wsNode = ws;
        progressHandle = ProgressHandleFactory.createHandle("calculate ...");

        initComponents();
        setName(Bundle.CTL_SingleSpecWindowTopComponent());
        setToolTipText(Bundle.HINT_SingleSpecWindowTopComponent());

        setButtons();
    }

    /**
     * Returns the current SpecView
     * @return current view of the specification
     */
    public SpecViewer getSpecViewer() {
        return specViewer;
    }

    /**
     * Sets a SpecView into the window
     * @param spec current @see SpecCollector
     */
    public void setSpecView(SpecCollector spec) {

        specViewer = SpecViewer.create(spec);
        specViewer = specViewer.refresh(spec);
        add(specViewer);
        specViewer.refreshHeader();

        if (spec.getWinX12Spec() != null) {
            refreshJD.setEnabled(true);
            save.setEnabled(true);
        }
        specViewer.getWinDoc().addDocumentListener(new MyDocumentListener());
    }

    /**
     * Sets an identification number for the window
     * @param id identification number for this window
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the identification number of this window
     * @return identificatiion number of this window
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the path of the latest directory
     * @return path of directory
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets path of directory for the multi mode
     */
    public void setPath(String path) {
        SingleTopComponent.path = path;
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

    /*TopComponent methods*/
    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {

            SingleSpec.deleteWindow(id);
            firePropertyChange("CLOSE", null, null);
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

    /*BUTTONS*/
    private JButton load = new JButton(new LoadAction());
    private JButton save = new JButton(new SaveAction());
    private JButton refreshX12 = new JButton(new RefreshWinAction());
    private JButton refreshJD = new JButton(new RefreshJDAction());
    
    /** Fortschrittsbalken */
    private ProgressHandle progressHandle;

    /*ACTIONS FOR BUTTONS*/
    public class LoadAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            JFileChooser fc = new JFileChooser(path);
            fc.setFileFilter(new MyFilter(".spc"));
            fc.setAcceptAllFileFilterUsed(true);
            int state = fc.showOpenDialog(null);

            if (state == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                path= file.getParent()+"\\";
                
                progressHandle.start(10);
                LoadRunnable load = new LoadRunnable();
                load.setSpc_File(file);
                Thread thread = new Thread(load);
                thread.start();
                String name = file.getName().replaceAll("\\.spc", "").replaceAll("\\.SPC", "");
                ((WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class)).setDisplayName(name);
                setDisplayName("SpecParser for " + name);

            }
        }
    }

    public class SaveAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            JFileChooser chooser = new JFileChooser(path);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new MyFilter(".spc"));
            int result = chooser.showSaveDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    try (FileWriter fw = new FileWriter(file)) {
                        fw.write(specViewer.getSpecCollector().getWinX12Spec());
                    }
                    if (!file.toString().endsWith(".spc")) {
                        file.renameTo(new File(file.toString() + ".spc"));
                    }

                    if (specViewer.getSpecCollector().getRegData() != null) {
                        File regFile = new File(chooser.getCurrentDirectory() + "\\dataRegressors.rgr");
                        try (FileWriter fw = new FileWriter(regFile)) {
                            fw.write(specViewer.getSpecCollector().getRegData());
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public class RefreshJDAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            SpecCollector sp = specViewer.getSpecCollector();
//            sp.setPath(path);
            //Default wiederherstellen
            sp.setJDSpec(new X13Document());
            sp.setWinX12Spec(specViewer.getWinX12Text());
            sp.translate(TranslationTo_Type.JDSpec);
            specViewer = specViewer.refresh(sp);
            refreshJD.setForeground(Color.black);
//            setSpecView(sp);
            if (wsNode != null) {
                wsNode.getWorkspace().sort();
            }
//            wsNode.getWorkspace().sortFamily(wsNode.lookup());
//            if (wsItem.getView() != null) {
//                wsItem.getView().close();
//            }
        }
    }

    public class RefreshWinAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            specViewer.pressApplyButton();

            SpecCollector sp = specViewer.getSpecCollector();
            sp.setJDSpec((SaDocument) specViewer.getDocument());
            sp.translate(TranslationTo_Type.WinX12Spec);
            specViewer = specViewer.refresh(sp);

            refreshJD.setEnabled(true);
            save.setEnabled(true);
            refreshJD.setForeground(Color.black);
        }

    }

    private void setButtons() {

        refreshJD.setEnabled(false);
        save.setEnabled(false);

        load.setText("Load WinX12Spec");
        save.setText("Save WinX12Spec");
        refreshX12.setText("Refresh WinX12Text");
        refreshJD.setText("Refresh JD+ Spec");

        load.setBackground(Color.LIGHT_GRAY);
        save.setBackground(Color.LIGHT_GRAY);
        refreshX12.setBackground(Color.LIGHT_GRAY);
        refreshJD.setBackground(Color.LIGHT_GRAY);

        javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        javax.swing.Box.Filler filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        javax.swing.Box.Filler filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));

        toolBar.add(load);
        toolBar.add(filler1);
        toolBar.add(save);
        toolBar.add(filler2);
        toolBar.add(refreshX12);
        toolBar.add(filler3);
        toolBar.add(refreshJD);
    }

    /*
     *       DocumentListner for changes in Textarea
     */
    public class MyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            refreshJD.setForeground(Color.blue);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            refreshJD.setForeground(Color.blue);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            refreshJD.setForeground(Color.blue);
        }
    }

    private class LoadRunnable implements Runnable {

        private File file;

        public void setSpc_File(File spec_File) {
            this.file = spec_File;
        }

        @Override
        public void run() {
            String message=null;
            int counter = 0;
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
                progressHandle.progress(2);

                SpecCollector sp = specViewer.getSpecCollector();
                sp.setWinX12Spec(s.toString());
                sp.setPath(path);
                sp.setName(file.getName());
                progressHandle.progress(3);
                sp.translate(TranslationTo_Type.JDSpec);
                progressHandle.progress(9);
                specViewer.refresh(sp);

                refreshJD.setEnabled(true);
                save.setEnabled(true);
                refreshJD.setForeground(Color.black);
                counter++;

            } catch (FileNotFoundException ex) {
                message="File not found";
            } catch (IOException ex) {
                message="File is not readable";
            } finally {
                progressHandle.finish();
//                wsNode.updateUI();
                wsNode.getWorkspace().sort();
                JOptionPane.showMessageDialog(null, "Translation completed: "+ counter +" / 1 specs");
           
                if(counter==0){
                    JOptionPane.showMessageDialog(null, "Not translated spc file: "+file.getName()+" "+message);
                }
            }
        }
    }
}
