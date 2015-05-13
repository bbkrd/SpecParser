/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Administration;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.nbdemetra.x13.X13DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.utilities.IModifiable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Nina Gonschorreck
 */
@ActionID(category = "Tools",
        id = "eu.nbdemetra.x13spec.OpenSpecParser")
@ActionRegistration(displayName = "#CTL_OpenSpecParser")
@ActionReferences({
    @ActionReference(path = X13DocumentManager.ITEMPATH, position = 1000, separatorAfter = 1090),
    @ActionReference(path = MultiProcessingManager.ITEMPATH, position = 1000, separatorAfter = 1090)
})
@NbBundle.Messages("CTL_OpenSpecParser=Open SpecParser")

public class OpenSpecParser implements ActionListener {

    private WsNode context;

    public OpenSpecParser(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //search in actually Workspace MultiProcessingDocument or X13Document (both implement IModifiable)
        WorkspaceItem w = (WorkspaceItem) context.getWorkspace().searchDocument(context.lookup(), IModifiable.class);

        if (w.getElement() instanceof X13Document) {

            w.closeView();
            SingleSpec s = new SingleSpec(context);
            w.closeView();
            
        } else if (w.getElement() instanceof MultiProcessingDocument) {

            w.closeView();
            MultiSpec m = new MultiSpec(context);
            w.closeView();
        
        } else {
            JOptionPane.showMessageDialog(null, "nix");
        }
    }
}
