/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.documents.X13Document;
import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class SingleSpec {

//    private SpecCollector spec;
    private SingleTopComponent window;
    private static HashMap<String, SingleTopComponent> activeSingleWindows = new HashMap();
    private String id;
    private String displayName;

    public SingleSpec(WorkspaceItem w) {

        if (w.getElement() instanceof X13Document) {
            if (!activeSingleWindows.containsKey(w.getId() + "")) {

                window = new SingleTopComponent();
                this.id = w.getId() + "";
                window.setId(id);

                SpecCollector spec = new SpecCollector(w);
                window.setSpecView(spec);

                activeSingleWindows.put(id, window);
            } else {
                window = activeSingleWindows.get(id);
            }
            displayName = w.getDisplayName();
            window.setName("SpecParser for " + displayName);

            window.open();
            window.requestActive();

        }
    }

    public String getId() {
        return id;
    }

    public SingleTopComponent getActiveWindow() {
        if (activeSingleWindows.containsKey(id)) {
            return activeSingleWindows.get(id);
        } else {
            return null;
        }
    }

    protected static void deleteWindow(String id) {
        activeSingleWindows.remove(id);
    }
}
