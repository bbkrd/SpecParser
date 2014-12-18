/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.nbdemetra.tramoseats;

import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.ui.WorkspaceTsTopComponent;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.ui.view.tsprocessing.TsProcessingViewer;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ec.nbdemetra.tramoseats//TramoSeats//EN",
autostore = false)
@TopComponent.Description(preferredID = "TramoSeatsTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Seasonal Adjustment", id = "ec.nbdemetra.tramoseats.TramoSeatsTopComponent")
@ActionReference(path = "Menu/Statistical methods/Seasonal Adjustment/Single Analysis", position = 1000)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TramoSeatsAction")
@NbBundle.Messages({
    "CTL_TramoSeatsAction=TramoSeats",
    "CTL_TramoSeatsTopComponent=TramoSeats Window",
    "HINT_TramoSeatsTopComponent=This is a TramoSeats window"
})
public final class TramoSeatsTopComponent extends WorkspaceTsTopComponent<TramoSeatsDocument> {

    private static TramoSeatsDocumentManager manager() {
        return WorkspaceFactory.getInstance().getManager(TramoSeatsDocumentManager.class);
    }

    public TramoSeatsTopComponent() {
        super(manager().create(WorkspaceFactory.getInstance().getActiveWorkspace()));
        initDocument();
    }

    public TramoSeatsTopComponent(WorkspaceItem<TramoSeatsDocument> doc) {
        super(doc);
        initDocument();
    }

    private void initDocument() {
        initComponents();
        setToolTipText(NbBundle.getMessage(TramoSeatsTopComponent.class, "HINT_TramoSeatsTopComponent"));
        panel = TsProcessingViewer.create(getDocument().getElement());
        this.add(panel);
        setName(getDocument().getDisplayName());
        panel.refreshHeader();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

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

    @Override
    protected String getContextPath() {
        return TramoSeatsDocumentManager.CONTEXTPATH;
    }
}
