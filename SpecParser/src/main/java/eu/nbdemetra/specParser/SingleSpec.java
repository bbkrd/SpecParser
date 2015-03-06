/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.SaItem;
import ec.tstoolkit.utilities.Id;
import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class SingleSpec {

    private SpecCollector spec;
    private SaItem item;
    private SingleTopComponent window;
    private static HashMap<Id, SingleTopComponent> activeWindows = new HashMap();
    private Id id;
    private String displayName;
    private WorkspaceItem ws;

   

    /*Constructor for MultiDocument Spec windows*/
    public SingleSpec(SaItem item, WorkspaceItem w) {
        this.item = item;
        this.ws=w;
        spec = new SpecCollector(ws);
        spec.setJDSpec(item.toDocument());
    }

    /*Constructor for SingleDocuments*/
    public SingleSpec(WorkspaceItem w) {

        if (!activeWindows.containsKey(w.getId())) {
            this.ws = w;
            this.spec = new SpecCollector(w);
            this.id = w.getId();
            this.displayName = w.getDisplayName();
            window = new SingleTopComponent();
            window.setName("SpecParser for " + this.displayName);

            window.setSpecView(spec);
            window.open();
            window.requestActive();

            activeWindows.put(this.id, window);
            window.setSpecView(spec);
        } else {
            window = activeWindows.get(w.getId());
            spec = window.getSpecViewer().getSpecCollector();
            window.setName("SpecParser for "+w.getDisplayName());
            
            if (window.isOpen()) {
                window.requestActive();
            } else {
                window.open();
                window.requestActive();
            }
        }
    }

//    public String getDisplayName() {
//        return displayName;
//    }

    public SpecCollector getSpecCollector() {
        return spec;
    }

    public SaItem getSaItem() {
        return item;
    }

    public Id getId() {
        return id;
    }

    public SingleTopComponent getActiveWindow() {
        if (activeWindows.containsKey(id)) {
            return activeWindows.get(id);
        } else {
            return null;
        }
    }

//    public WorkspaceItem getWorkspace() {
////        w = spec.getWS();
//        return ws;
//    }
}
