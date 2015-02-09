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
    private ArrayList<SingleSpec> singleSpecList = new ArrayList();
//    private MultiTopComponent window;
    private Id id;
    private WorkspaceItem ws;

    public MultiSpec(WorkspaceItem ws) {

        //TODO: 
        //      Transformation der einzelnen SingleSpecs
        //      Liste pflegen mit warnings für MyCellRender im window uebergeben
        MultiTopComponent window;
        if (!activeWindows.containsKey(ws.getId())) {
            this.ws = ws;
            this.id = ws.getId();
            window = new MultiTopComponent(ws);
            window.setName("SpecParser for " + ws.getDisplayName());

            SingleSpec single;
            for (SaItem item : ((MultiProcessingDocument) ws.getElement()).getCurrent()) {
                if (!item.getEstimationMethod().name.contains("tramo")) {
                    single = new SingleSpec(item, ws);
                    singleSpecList.add(single);
                }
            }

            window.setSingleSpecList(singleSpecList);
            window.open();
            window.requestActive();

            activeWindows.put(this.id, window);
        } else {
            window = activeWindows.get(ws.getId());
            if (window.isOpen()) {
                window.requestActive();
            } else {
                window.open();
                window.requestActive();
            }
        }
        this.ws=activeWindows.get(this.ws.getId()).getWs();
    }
    
//    public WorkspaceItem getWorkspace(){
//        return ws;
//    }
}
