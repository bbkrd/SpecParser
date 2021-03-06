/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
        location = "Demetra",
        displayName = "#AdvancedOption_DisplayName_SpecParser",
        keywords = "#AdvancedOption_Keywords_SpecParser",
        keywordsCategory = "Demetra/SpecParser",
        id = SpecParserOptionsPanelController.ID
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_SpecParser=SpecParser", "AdvancedOption_Keywords_SpecParser=SpecParser"})
public final class SpecParserOptionsPanelController extends OptionsPanelController {

    public static final String ID = "Demetra/SpecParser";
    public static final String SPECPARSER_VARS_LOCATION = "specparser_vars_location";
    
    public static final String DEFAULT_MODE = "Default mode", 
                               CALENDAR_MODE = "Calendar mode";
    
    private SpecParserPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getPanel().store();
                changed = false;
            }
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private SpecParserPanel getPanel() {
        if (panel == null) {
            panel = new SpecParserPanel(this);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

}
