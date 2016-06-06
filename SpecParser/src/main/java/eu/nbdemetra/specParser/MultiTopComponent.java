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

import Administration.MultiSpec;
import Logic.SpecCollector;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.satoolkit.ISaSpecification;
import ec.tss.sa.SaItem;
import ec.tstoolkit.utilities.IModifiable;
import eu.nbdemetra.specParser.Miscellaneous.MyCellRenderer;
import eu.nbdemetra.specParser.Miscellaneous.MyFilter;
import eu.nbdemetra.specParser.Miscellaneous.TranslationTo_Type;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays a list of SingleSpecs from the MultiDocument
 */
@ConvertAsProperties(
        dtd = "-//eu.nbdemetra.x13spec//MultiDocSpecWindow//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MultiDocSpecWindowTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "eu.nbdemetra.x13spec.MultiDocSpecWindowTopComponent")

@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MultiDocSpecWindowAction",
        preferredID = "MultiDocSpecWindowTopComponent"
)
@Messages({
    "CTL_MultiDocSpecWindowAction=MultiDocSpecWindow",
    "CTL_MultiDocSpecWindowTopComponent=MultiDocSpecWindow",
    "HINT_MultiDocSpecWindowTopComponent=This is a MultiDocSpecWindow"
})

public final class MultiTopComponent extends TopComponent {

    /*      spec_array      -   gets all generated SpecCollectors from SaItems of the MultiDocument
     *      activeWindows   -   collects the selected SingleWindows
     *      mta_files       -   collect all spc files for translate in a thread
     */
    private ArrayList<SpecCollector> spec_array = new ArrayList();
    private Map<String, SingleTopComponent> activeSingleWindows = new HashMap();
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

    /*CONSTRUCTORS*/
    public MultiTopComponent() {
        //never used, but important for TopComponent
    }

    public MultiTopComponent(WsNode w) {

        wsNode = w;
        initComponents();
        setToolTipText(Bundle.HINT_MultiDocSpecWindowTopComponent());
        progressHandle = ProgressHandleFactory.createHandle("calculate ...");
        this.wsItem = (WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class);
    }

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        specList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        errorText = new javax.swing.JTextArea();
        singleSpecName = new javax.swing.JLabel();
        toolBar1 = new javax.swing.JToolBar();
        load = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));

        specList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                specListMouseClicked(evt);
            }
        });
        specList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                specListValueChanged(evt);
            }
        });
        specList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                specListKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(specList);

        errorText.setEditable(false);
        errorText.setColumns(20);
        errorText.setRows(5);
        errorText.setText(org.openide.util.NbBundle.getMessage(MultiTopComponent.class, "MultiTopComponent.errorText.text")); // NOI18N
        jScrollPane2.setViewportView(errorText);

        singleSpecName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(singleSpecName, org.openide.util.NbBundle.getMessage(MultiTopComponent.class, "MultiTopComponent.singleSpecName.text")); // NOI18N

        toolBar1.setRollover(true);

        load.setBackground(Color.LIGHT_GRAY);
        org.openide.awt.Mnemonics.setLocalizedText(load, org.openide.util.NbBundle.getMessage(MultiTopComponent.class, "MultiTopComponent.load.text")); // NOI18N
        load.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadMtaFiles(evt);
            }
        });
        toolBar1.add(load);
        toolBar1.add(filler1);
        toolBar1.add(filler2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(singleSpecName)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(toolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(singleSpecName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    /*METHODS FOR FUNCTIONALITY OF GUI*/
    private void specListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_specListMouseClicked
        /*  This method opens the SingleSpecWindow of the selected item
         *  from the list in front, when you click two times or more on the item.
         *  With one click you see information of the SingleSpec transformation
         *  on the rigth.
         */
        try {
            if (evt.getClickCount() >= 2) {
                openSingleWindow();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //daneben geklickt
        }
    }//GEN-LAST:event_specListMouseClicked

    private void loadMtaFiles(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadMtaFiles

        if (load.isEnabled()) {
            JFileChooser fc = new JFileChooser(path);
            fc.setFileFilter(new MyFilter(".mta"));
            fc.setAcceptAllFileFilterUsed(false);

            //for choosing a mta file
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

                File mta_File = fc.getSelectedFile();
                String name = mta_File.getName();
                path = fc.getSelectedFile().getParent() + "\\";
                name = name.replaceAll("\\.mta", "").replaceAll("\\.MTA", "");

                mtaName = name;

                LoadRunnable loadProcess = new LoadRunnable();
                loadProcess.setMta_File(mta_File);
                Thread thread = new Thread(loadProcess);
                thread.start();
                wsItem.setDisplayName(name);
                MultiTopComponent.this.setDisplayName("SpecParser for " + name);
                MultiTopComponent.this.repaint();

                load.setFocusable(false);
            } else {
                JOptionPane.showMessageDialog(this, "File isn't loaded");
            }
        }
    }//GEN-LAST:event_loadMtaFiles

    private void specListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_specListValueChanged
        // TODO add your handling code here:
        try {
            singleSpecName.setText(specList.getSelectedIndex() + 1 + "    " + spec_array.get(specList.getSelectedIndex()).getName());
            String[] errors = spec_array.get(specList.getSelectedIndex()).getErrors();
            String[] messages = spec_array.get(specList.getSelectedIndex()).getMessages();
            String[] warnings = spec_array.get(specList.getSelectedIndex()).getWarnings();

            errorText.setText("ERRORS:\n"
                    + "******\n");
            if (errors.length != 0) {
                for (String tmp : errors) {
                    errorText.append(tmp + "\n");
                }
            } else {
                errorText.append("No errors\n");
            }

            errorText.append("\nWARNINGS:\n"
                    + "*********\n");
            if (warnings.length != 0) {
                for (String tmp : warnings) {
                    errorText.append(tmp + "\n");
                }
            } else {
                errorText.append("\nNo warnings\n");
            }

            errorText.append("\nMESSAGES:\n"
                    + "*********\n");
            if (messages.length != 0) {
                for (String tmp : messages) {
                    errorText.append(tmp + "\n");
                }
            } else {
                errorText.append("\nNo messages\n");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //click not on item
        }
    }//GEN-LAST:event_specListValueChanged

    private void specListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_specListKeyPressed
        // TODO add your handling code here:

        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            openSingleWindow();
        }
    }//GEN-LAST:event_specListKeyPressed

    private void openSingleWindow() {

        SingleTopComponent window;

        SpecCollector s = spec_array.get(specList.getSelectedIndex());

        s.setPath(path);

        //check for window of selected item in map activeWindows
        if (!activeSingleWindows.containsKey(specList.getSelectedIndex() + "")) {
            //create new one
            window = new SingleTopComponent();
            window.setSpecView(s);
            window.setDisplayName("SpecParser for " + s.getName());
            window.setPath(path);
            window.setId(specList.getSelectedIndex() + "");
            window.open();
            window.requestActive();
            activeSingleWindows.put(specList.getSelectedIndex() + "", window);

            //add a listener for closing STC when the corresponding MTC is closing
            window.addPropertyChangeListener("CLOSE", new MyPropertyChangeListener());
        } else {
            //open the old one (with with new SpecCollector, maybe changes)
            window = activeSingleWindows.get(specList.getSelectedIndex() + "");
            window.setSpecView(s);
            window.open();
            window.requestActive();
        }
        /*} else {
         JOptionPane.showMessageDialog(this, "No Data");
         }*/
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea errorText;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton load;
    private javax.swing.JLabel singleSpecName;
    private javax.swing.JList specList;
    private javax.swing.JToolBar toolBar1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
//
        for (SingleTopComponent window : activeSingleWindows.values().toArray(new SingleTopComponent[0])) {
            window.close();
        }
        MultiSpec.deleteWindow(wsItem.getId());
    }

    protected void deleteWindow(String id) {
        //if STC is closing, delete STC from list
        activeSingleWindows.remove(id);
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

    public class MyPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String s = evt.getPropertyName();

            if (s.equals("CLOSE")) {
                deleteWindow(((SingleTopComponent) evt.getSource()).getId());
            }
        }
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
                        if (!mta_line.isEmpty()) {
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

                        try (FileReader spec_FileReader = new FileReader(new File(path + (current) + ".SPC"))) {

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
                        tmp.append(s).append("\n");
                    }
                    JOptionPane.showMessageDialog(null, tmp.toString());
                }
            }
        }
    }
}
