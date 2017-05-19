/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import Administration.MultiSpec;
import Logic.SpecCollector;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.satoolkit.ISaSpecification;
import ec.tss.sa.SaItem;
import ec.tstoolkit.utilities.IModifiable;
import ec.tstoolkit.utilities.Id;
import ec.ui.view.tsprocessing.DefaultProcessingViewer.Type;
import eu.nbdemetra.specParser.Miscellaneous.MyCellRenderer;
import eu.nbdemetra.specParser.Miscellaneous.MyFilter;
import eu.nbdemetra.specParser.Miscellaneous.TranslationTo_Type;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//eu.nbdemetra.specParser//MultiTopComponent//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MultiTopComponentTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "eu.nbdemetra.specParser.MultiTopComponentTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MultiTopComponentAction",
        preferredID = "MultiTopComponentTopComponent"
)
@Messages({
    "CTL_MultiTopComponentAction=MultiTopComponent",
    "CTL_MultiTopComponentTopComponent=MultiTopComponent Window",
    "HINT_MultiTopComponentTopComponent=This is a MultiTopComponent window"
})
public final class MultiTopComponent extends TopComponent {

    public MultiTopComponent() {

    }

    /*      spec_array      -   gets all generated SpecCollectors from SaItems of the MultiDocument
     *      activeWindows   -   collects the selected SingleWindows
     *      mta_files       -   collect all spc files for translate in a thread
     */
    private ArrayList<SpecCollector> spec_array = new ArrayList();
//    private ThreadObjectList mta_files = new ThreadObjectList();
    private LinkedList<String> specFromMTA = new LinkedList();

    /*       wsItem      -   WorspaceItem, to modify the item in the GUI (name, data, spec)
     *       wsNode      -   get WorkspaceItem and to show the rename of the WorkspaceItem
     *       path        -   to open the a directory for loading and saving, notice the last directory by loading   */
    private WorkspaceItem wsItem;
    private WsNode wsNode;
    private static String path = System.getProperty("user.home");

    private String mtaName;

    private ProgressHandle progressHandle;

    public MultiTopComponent(WsNode wsNode) {

        this.wsNode = wsNode;
        initComponents();

        setToolTipText(Bundle.HINT_MultiTopComponentTopComponent());
        progressHandle = ProgressHandleFactory.createHandle("calculate ...");
        this.wsItem = (WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class);
        specViewer = new SpecViewer(Type.APPLY_RESTORE_SAVE, new SpecCollector(wsItem));

        //button faerben
        specViewer.getWinDoc().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh.setForeground(Color.blue);
                refresh.setEnabled(true);
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh.setForeground(Color.blue);
                refresh.setEnabled(true);
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh.setForeground(Color.blue);
                refresh.setEnabled(true);
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        specViewer.refreshHeader();

        jSplitPane1.setRightComponent(specViewer);
//        add(specViewer);
        specViewer.setSpecificationsVisible(true);

    }

    private SpecViewer specViewer;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        top = new javax.swing.JPanel();
        load = new javax.swing.JButton();
        refresh = new javax.swing.JButton();
        refresh_all = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        specList = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(load, org.openide.util.NbBundle.getMessage(MultiTopComponent.class, "MultiTopComponent.load.text")); // NOI18N
        load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadActionPerformed(evt);
            }
        });
        top.add(load);

        org.openide.awt.Mnemonics.setLocalizedText(refresh, org.openide.util.NbBundle.getMessage(MultiTopComponent.class, "MultiTopComponent.refresh.text")); // NOI18N
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        top.add(refresh);

        org.openide.awt.Mnemonics.setLocalizedText(refresh_all, org.openide.util.NbBundle.getMessage(MultiTopComponent.class, "MultiTopComponent.refresh_all.text")); // NOI18N
        refresh_all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refresh_allActionPerformed(evt);
            }
        });
        top.add(refresh_all);

        add(top, java.awt.BorderLayout.PAGE_START);

        specList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                specListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(specList);

        jSplitPane1.setLeftComponent(jScrollPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadActionPerformed
        // TODO add your handling code here:
        if (load.isEnabled()) {
            JFileChooser fc = new JFileChooser(path);
            fc.setFileFilter(new MyFilter(".mta"));
            fc.setAcceptAllFileFilterUsed(false);

            //for choosing a mta file
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

                File mta_File = fc.getSelectedFile();
                String name = mta_File.getName();
                path = fc.getSelectedFile().getParent() + File.separator;
                name = Pattern.compile("\\.mta", Pattern.CASE_INSENSITIVE).matcher(name).replaceAll("");//name.replace(".mta", "").replace(".MTA", "");

                mtaName = name;

                MultiTopComponent.LoadRunnable loadProcess = new MultiTopComponent.LoadRunnable();
                loadProcess.setMta_File(mta_File);
                Thread thread = new Thread(loadProcess);
                thread.start();
                wsItem.setDisplayName(name);
                MultiTopComponent.this.setDisplayName("SpecParser for " + name);
                MultiTopComponent.this.repaint();

                load.setFocusable(false);
                refresh.setEnabled(true);
                refresh.setForeground(Color.black);
            } else {
                JOptionPane.showMessageDialog(this, "File isn't loaded");
            }
        }
    }//GEN-LAST:event_loadActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        SpecCollector newSpec = spec_array.get(specList.getSelectedIndex());
        newSpec.setWinX12Spec(specViewer.getWinX12Text());
        newSpec.translate(TranslationTo_Type.JDSpec);
        specViewer.refresh(newSpec);
        refresh.setEnabled(true);
        refresh.setForeground(Color.black);
    }//GEN-LAST:event_refreshActionPerformed

    private void specListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_specListValueChanged

        if (specList.getSelectedIndex() >= 0) {
            specViewer.refresh(spec_array.get(specList.getSelectedIndex()));
            refresh.setEnabled(true);
            refresh.setForeground(Color.black);
        } else {
            if (spec_array != null && spec_array.size() > 0) {
                specViewer.refresh(spec_array.get(0));
                refresh.setEnabled(true);
                refresh.setForeground(Color.black);
            }
        }


    }//GEN-LAST:event_specListValueChanged

    private void refresh_allActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refresh_allActionPerformed
        spec_array = new ArrayList<>();
        
        progressHandle = ProgressHandleFactory.createHandle("calculate ...");
        MultiTopComponent.LoadRunnable loadProcess = new MultiTopComponent.LoadRunnable();
        File file = new File(path + mtaName + ".mta");
        loadProcess.setMta_File(file);
        Thread thread = new Thread(loadProcess);
        thread.start();
        MultiTopComponent.this.repaint();
    }//GEN-LAST:event_refresh_allActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton load;
    private javax.swing.JButton refresh;
    private javax.swing.JButton refresh_all;
    private javax.swing.JList specList;
    private javax.swing.JPanel top;
    // End of variables declaration//GEN-END:variables

    /*METHODS TO FILL THE LIST OF SINGLE SPECS IN THE GUI*/
    public void setSpecArray(ArrayList<SpecCollector> list) {

        this.spec_array = list;
        this.setSpecList();
    }

    private void setSpecList() {

        DefaultListModel model = new DefaultListModel();
        for (SpecCollector item : spec_array) {
            model.addElement(item);
        }

        specList.setModel(model);
        specList.setCellRenderer(new MyCellRenderer());
    }

    public String getMtaName() {
        return mtaName;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening      
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
        Id id = wsItem.getId();
        MultiSpec.deleteWindow(id);

    }

    protected void deleteWindow(String id) {
        System.out.println("Closed");
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

    public WorkspaceItem getWs() {
        return (WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class);
    }

    public String getPath() {
        return path;
    }
    
    private class LoadRunnable implements Runnable {

        private File mta_File;

        public void setMta_File(File mta_File) {
            this.mta_File = mta_File;
        }

        @Override
        public void run() {

            int counter_mta = 0;
            int counter_trans = 0;
            ArrayList<String> missingTranslation = new ArrayList();
            try {
                //open this mta file
                FileReader mta_FileReader = new FileReader(mta_File);
                //read all lines of the mta file
                try (BufferedReader mta_BufferedReader = new BufferedReader(mta_FileReader)) {
//
                    String mta_line;
//                    if (!mta_files.isEmpty()) {
                    if (!specFromMTA.isEmpty()) {
//                        mta_files = new ThreadObjectList();
                        specFromMTA = new LinkedList<>();
                        spec_array = new ArrayList<>();
                    }
                    //lies Zeile aus MTA
                    while ((mta_line = mta_BufferedReader.readLine()) != null) {
                        mta_line = mta_line.trim();
                        if (!mta_line.isEmpty() && !mta_line.startsWith("#")) {
//                            mta_files.addSpec(mta_line);
                            specFromMTA.addLast(mta_line);
                            counter_mta++;
                        }
                    }
                    progressHandle.start(counter_mta);
                    int counter = 0;
                    String current;
                    while (!specFromMTA.isEmpty()) {
//                        ThreadObject object = mta_files.getSpec();
                        current = specFromMTA.pollFirst();
                        counter++;

                        File file;
                        if (current.contains(File.separator)) {
                            //absolute path
                            file = new File(current + ".spc");
                        } else {
                            //relative path
                            file = new File(path + (current) + ".spc");
                        }
                        try (FileReader spec_FileReader = new FileReader(file)) {

                            try (BufferedReader brSpec = new BufferedReader(spec_FileReader)) {
                                StringBuilder spec_StringBuilder = new StringBuilder();
                                String spec_line;
                                while ((spec_line = brSpec.readLine()) != null) {
                                    spec_StringBuilder.append(spec_line);
                                    spec_StringBuilder.append("\n");
                                }

                                SpecCollector spec = new SpecCollector(wsItem, counter);
                                spec.setPath(path);
                                spec.setWinX12Spec(spec_StringBuilder.toString());
                                spec.setName(current);
//                                spec.setNameForWS(MultiTopComponent.this.getMtaName());
                                spec.translate(TranslationTo_Type.JDSpec);

                                if (spec.getTs() != null) {
                                    if (spec.getTs().getTsData() != null) {
                                        SaItem item = new SaItem((ISaSpecification) spec.getJDSpec().getSpecification(), spec.getTs());
                                        item.setMetaData(spec.getMetaData());
                                        spec.setJDSpec(item.toDocument());
                                        counter_trans++;
                                    } else {
                                        missingTranslation.add(current);
                                    }
                                }
                                spec_array.add(spec);
                            }
                        } catch (IOException ex) {
                            //spc konnte nicht uebersetzt werden
                            //merke dir current
                            missingTranslation.add(current);
                        }
                        progressHandle.progress(counter);
                    }
                    setSpecList();
                    load.setEnabled(false);

                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(MultiTopComponent.this, "File doesn't exist");
            } finally {
                progressHandle.finish();
                wsNode.updateUI();
//                for (Node item : WsNode.items(wsItem.getOwner(), new LinearId("Utilities"))) {
//                     ((WsNode)item).updateUI();
//                    if (item instanceof ManagerWsNode && item.getDisplayName().equals("Variables")) {
//                        Node[] temp = ((ManagerWsNode) item).getChildren().getNodes();
//                        for (Node node : temp) {
//                            ((WsNode)node).updateUI();
//                        }
//                    }
//                }
                wsNode.getWorkspace().sort();
                JOptionPane.showMessageDialog(null, "Translation completed: " + counter_trans + " / " + counter_mta + " specs");
                if (!missingTranslation.isEmpty()) {

                    StringBuilder tmp = new StringBuilder("Not translated spc-files: \n");
                    for (String s : missingTranslation) {
                        tmp.append(path).append(s).append("\n");
                    }
                    JOptionPane.showMessageDialog(null, tmp.toString());
                }
            }
        }
    }
}
