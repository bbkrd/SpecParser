/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.nbdemetra.sa.actions;

import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ui.Menus;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(category = "SaProcessing",
id = "ec.nbdemetra.sa.actions.Priority")
@ActionRegistration(displayName = "#CTL_Priority")
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 1500),
    @ActionReference(path = "Shortcuts", name = "p")
})
@Messages("CTL_Priority=Priority")
public final class Priority extends AbstractAction implements Presenter.Popup {
    
    public static final String PATH="/Priority";

    public Priority(){
        super(Bundle.CTL_Priority());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
    }
 
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu=new JMenu(this);
        Menus.fillMenu(menu, MultiProcessingManager.CONTEXTPATH+PATH);
        return menu;
    }
}

