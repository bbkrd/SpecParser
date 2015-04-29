/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Administration;

import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.documents.X13Document;
import eu.nbdemetra.specParser.SingleTopComponent;
import Logic.SpecCollector;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.tstoolkit.utilities.IModifiable;
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

    public SingleSpec(WsNode ws) {

        WorkspaceItem w = (WorkspaceItem) ws.getWorkspace().searchDocument(ws.lookup(), IModifiable.class);
        if (w.getElement() instanceof X13Document) {
            id = w.getId() + "";

            if (!activeSingleWindows.containsKey(id)) {

                window = new SingleTopComponent(ws);
                window.setId(id);

                SpecCollector spec = new SpecCollector((WorkspaceItem) ws.getWorkspace().searchDocument(ws.lookup(), IModifiable.class));
                spec.setPath(window.getPath());
                window.setSpecView(spec);

                activeSingleWindows.put(id, window);
            } else {
                window = activeSingleWindows.get(id);
            }
            displayName = w.getDisplayName();
            window.setName("SpecParser for " + displayName);

//            w.reload();
            window.open();
            window.requestActive();
//            ws.getWorkspace().sortFamily(ws.lookup());

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

    public static void deleteWindow(String id) {
        activeSingleWindows.remove(id);
    }
}
