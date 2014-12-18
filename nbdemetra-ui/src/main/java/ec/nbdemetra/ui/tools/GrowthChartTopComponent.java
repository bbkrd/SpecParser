/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.ui.tools;

import ec.nbdemetra.ui.ComponentFactory;
import ec.nbdemetra.ui.nodes.ControlNode;
import ec.ui.ATsGrowthChart;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ec.nbdemetra.ui.tools//GrowthChart//EN",
        autostore = false)
@TopComponent.Description(preferredID = "GrowthChartTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "ec.nbdemetra.ui.tools.GrowthChartTopComponent")
@ActionReference(path = "Menu/Tools/Container", position = 300)
@TopComponent.OpenActionRegistration(displayName = "#CTL_GrowthChartAction")
@Messages({
    "CTL_GrowthChartAction=GrowthChart",
    "CTL_GrowthChartTopComponent=GrowthChart",
    "HINT_GrowthChartTopComponent=This is a GrowthChart window"
})
public final class GrowthChartTopComponent extends TopComponent implements ExplorerManager.Provider {

    private final ExplorerManager mgr = new ExplorerManager();

    public GrowthChartTopComponent() {
        initComponents();
        setName(Bundle.CTL_GrowthChartTopComponent());
        setToolTipText(Bundle.HINT_GrowthChartTopComponent());
        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap()));
        add(ComponentFactory.getDefault().newTsGrowthChart(), BorderLayout.CENTER);
    }

    @Override
    public void open() {
        super.open();
        Mode mode = WindowManager.getDefault().findMode("output");
        if (mode != null && mode.canDock(this)) {
            mode.dockInto(this);
        }
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        ControlNode.onComponentOpened(mgr, getGrowthChart());
        getGrowthChart().connect();
    }

    @Override
    public void componentClosed() {
        mgr.setRootContext(Node.EMPTY);
        getGrowthChart().dispose();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        ToolsPersistence.writeTsCollection(getGrowthChart(), p);
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        ToolsPersistence.readTsCollection(getGrowthChart(), p);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    public ATsGrowthChart getGrowthChart() {
        return (ATsGrowthChart) getComponent(0);
    }
}
