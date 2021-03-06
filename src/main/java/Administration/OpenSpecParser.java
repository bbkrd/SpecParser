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
 * This programme opens a SpecParser window depentend of the kind of document. 
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

    /**
     * The current workspace node.
     */
    private WsNode context;

    /**
     * Creates an object of this class with a workspace node 
     */
    public OpenSpecParser(WsNode context) {
        this.context = context;
    }
/**
 * This method decides which mode of the SpecParser will be opened.
 * There are single and multi mode. 
 *
 * @param e unused
 */
    @Override
    public void actionPerformed(ActionEvent e) {

        //search in actually Workspace MultiProcessingDocument or X13Document (both implement IModifiable)
        WorkspaceItem w = (WorkspaceItem) context.getWorkspace().searchDocument(context.lookup(), IModifiable.class);
        w.closeView();
        
        if (w.getElement() instanceof X13Document) {
            SingleSpec s = new SingleSpec(context);
        } else if (w.getElement() instanceof MultiProcessingDocument) {
//            MultiSpec m = new MultiSpec(context);    
            MultiSpec m = new MultiSpec(context); 
        } else {
            JOptionPane.showMessageDialog(null, "nix");
        }
    }
}
