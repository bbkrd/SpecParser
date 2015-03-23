/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.X13Document;
import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class SingleSpec {

    private SpecCollector spec;
    private SaItem item;
    private SingleTopComponent window;
    private static HashMap<String, SingleTopComponent> activeWindows = new HashMap();
    private String id;
    private String displayName;


    public SingleSpec(WorkspaceItem w) {

        if (w.getElement() instanceof X13Document) {
            if (!activeWindows.containsKey(w.getId()+"")) {

                window = new SingleTopComponent();
                this.id = w.getId()+"";
                window.setId(id);

                this.spec = new SpecCollector(w);
                window.setSpecView(spec);

                activeWindows.put(id, window);
            } else {
                window = activeWindows.get(id);
            }
            displayName = w.getDisplayName();
            window.setName("SpecParser for " + displayName);

            window.open();
            window.requestActive();
        } else {
            spec = new SpecCollector(w);
        }
    }

    public SpecCollector getSpecCollector() {
        return spec;
    }

    public SaItem getSaItem() {
        return item;
    }

    public String getId() {
        return id;
    }

    public SingleTopComponent getActiveWindow() {
        if (activeWindows.containsKey(id)) {
            return activeWindows.get(id);
        } else {
            return null;
        }
    }

    protected static void deleteWindow(String id) {
        activeWindows.remove(id);
    }
}
