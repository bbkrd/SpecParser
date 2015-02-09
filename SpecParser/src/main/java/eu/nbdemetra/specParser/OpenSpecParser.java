/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

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
@NbBundle.Messages("CTL_OpenSpecParser=Open Spec Parser")

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

            //vllt Factory
//            new SingleSpec((X13Document) w.getElement(), w.getId(), w.getDisplayName());
           SingleSpec s = new SingleSpec(w);
//           w=s.getWorkspace();
           //            context.
//            context=WsNode(Children.createChildren(s.getWorkspace().getOwner(), w.getId()), s.getSpecCollector(), w.getId());
//            ((X13Document) w.getElement()).getSpecification();
        } else if (w.getElement() instanceof MultiProcessingDocument) {

            //vllt Factory
            MultiSpec m = new MultiSpec(w);
//            w=m.getWorkspace();

        } else {
            JOptionPane.showInputDialog(null, "nix");
        }
    }

}
