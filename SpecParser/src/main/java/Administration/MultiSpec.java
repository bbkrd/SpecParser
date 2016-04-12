/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Administration;

import ec.nbdemetra.ws.WorkspaceItem;
import ec.tstoolkit.utilities.Id;
import eu.nbdemetra.specParser.MultiTopComponent;
import Logic.SpecCollector;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.tstoolkit.utilities.IModifiable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents the multi mode. 
 * 
 * @author Nina Gonschorreck
 */
public class MultiSpec {

    /** Collect all active multi windows */
    private static HashMap<Id, MultiTopComponent> activeMultiWindows = new HashMap();
    /** Collect all SpecColletors for the single documents in the multi mode */
    private ArrayList<SpecCollector> specList = new ArrayList();
    /** idetification number of the current multi document*/
    private Id id;

    /** 
     * Creates an object of the multi mode
     * @param wsNode current workspace node
     */
    public MultiSpec(WsNode wsNode) {

        WorkspaceItem ws = (WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class);
        MultiTopComponent window;

        // Is there a multi window with the id from the workspace node?
        if (!activeMultiWindows.containsKey(ws.getId())) {
            // Preparation of the new multi window
            this.id = ws.getId();
            window = new MultiTopComponent(wsNode);
            window.setName("SpecParser for " + ws.getDisplayName());

            // later: Implemantion of reverse direction (JD+ to winx12)
            /*if the list of items in a multi doc has contents */
           /* if (!((MultiProcessingDocument) ws.getElement()).getCurrent().isEmpty()) {
                SpecCollector specCollector;
                int counter = 0;
                for (SaItem item : ((MultiProcessingDocument) ws.getElement()).getCurrent()) {
                    if (!item.getEstimationMethod().name.contains("tramo")) {
//                        it is possible that here are also tramo docs in the list
                        specCollector = new SpecCollector((WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class), counter);
                        specCollector.setPath(window.getPath());
//                        specCollector.translate(TranslationTo_Type.WinX12Spec);
                        specCollector.setName(item.getTs().getRawName());
                        specList.add(specCollector);
                    }
                    counter++;
                    
                }
                window.setSpecArray(specList);
            }*/
            
            // Put the window to the list of active windows
            activeMultiWindows.put(this.id, window);

            // Open this window
            window.open();
            window.requestActive();
        } else {
            // Get the window with this id
            window = activeMultiWindows.get(ws.getId());
            window.setName("SpecParser for " + ws.getDisplayName());

            //close workspace and rename the document
            wsNode.getWorkspace().sortFamily(wsNode.lookup());
            
            // open the window
            window.requestActive();
        }
    }

    /**
     * Removes the window of the list of active windos
     * @param id identification number of the window
     */
    public static void deleteWindow(Id id) {
        activeMultiWindows.remove(id);
    }
}
