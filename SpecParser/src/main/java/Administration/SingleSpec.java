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

import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.documents.X13Document;
import eu.nbdemetra.specParser.SingleTopComponent;
import Logic.SpecCollector;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.tstoolkit.utilities.IModifiable;
import java.util.HashMap;

/**
 * This class represents the single mode.
 * 
 * @author Nina Gonschorreck
 * 
 * @param activeSingleWindows list of all active single windows
 */
public class SingleSpec {
 
    /** Collect all open single windows */
    private static HashMap<String, SingleTopComponent> activeSingleWindows = new HashMap();
    /** current single window */
    private SingleTopComponent window;
    /** reference number for the single window */
    private String id;
    /** name of the single window */
    private String displayName;

    /** Creates an object of this class with the workspace node
     * @param ws current workspace node
     */
    public SingleSpec(WsNode ws) {

        WorkspaceItem w = (WorkspaceItem) ws.getWorkspace().searchDocument(ws.lookup(), IModifiable.class);

        if (w.getElement() instanceof X13Document) {
            // Saving id from the current workspace
                id = w.getId() + "";

                // Is there an active window with this id?
            if (!activeSingleWindows.containsKey(id)) {
                // creates a new single window
                window = new SingleTopComponent(ws);
                window.setId(id);

                // Preparation of the window 
                SpecCollector spec = new SpecCollector(w);
                spec.setPath(window.getPath());
                window.setSpecView(spec);

                // put window into the list of active single windows
                activeSingleWindows.put(id, window);
            } else {
                // open the window with this id
                window = activeSingleWindows.get(id);
            }
            // open window
            displayName = w.getDisplayName();
            window.setName("SpecParser for " + displayName);
            window.open();
            window.requestActive();            
        }
    }

    /**
     * Returns the identification number of this window
     * @return identification number of the window
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the current active window
     * @return the current active single window
     */
    public SingleTopComponent getActiveWindow() {
        if (activeSingleWindows.containsKey(id)) {
            return activeSingleWindows.get(id);
        } else {
            return null;
        }
    }

    /**
     * Removes a single window from the list of active windows
     * @param id identification number of the window which will be closed
     */
    public static void deleteWindow(String id) {
        activeSingleWindows.remove(id);
    }
}
