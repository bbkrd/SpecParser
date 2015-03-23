/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.SaItem;
import ec.tstoolkit.utilities.Id;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class MultiSpec {

    /*
     *   activeWindows   -   collects the already opened multidoc generator windows
     *   singleSpecList  -   for the single documents/specs in the multidocument
     *   id              -   identification for singel open documents 
     */
    private static HashMap<Id, MultiTopComponent> activeWindows = new HashMap();
    private ArrayList<SpecCollector> specList = new ArrayList();
    private Id id;

    public MultiSpec(WorkspaceItem ws) {

        MultiTopComponent window;
        
        if (!activeWindows.containsKey(ws.getId())) {
            
            this.id = ws.getId();
            window = new MultiTopComponent(ws);
            window.setName("SpecParser for " + ws.getDisplayName());

            SpecCollector specCollector;
            int counter = 0;
            
            for (SaItem item : ((MultiProcessingDocument) ws.getElement()).getCurrent()) {
                if (!item.getEstimationMethod().name.contains("tramo")) {
                    specCollector = new SpecCollector(ws, counter);
                    specList.add(specCollector);
                    counter++;
                }
            }

            window.setSpecArray(specList.toArray(new SpecCollector[0]));
            window.open();
            window.requestActive();

            activeWindows.put(this.id, window);
        } else {
            window = activeWindows.get(ws.getId());
            window.requestActive();
        }
    }

    protected static void deleteWindow(Id id) {
        activeWindows.remove(id);
    }
}
