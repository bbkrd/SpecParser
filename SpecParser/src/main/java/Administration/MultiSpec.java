/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Administration;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.SaItem;
import ec.tstoolkit.utilities.Id;
import eu.nbdemetra.specParser.MultiTopComponent;
import Logic.SpecCollector;
import ec.nbdemetra.ws.nodes.WsNode;
import ec.tstoolkit.utilities.IModifiable;
import eu.nbdemetra.specParser.Miscellaneous.TranslationTo_Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class MultiSpec {

    /*   activeWindows   -   collects the already opened multidoc generator windows
     *   singleSpecList  -   for the single documents/specs in the multidocument
     *   id              -   identification for singel open documents                   */
    private static HashMap<Id, MultiTopComponent> activeMultiWindows = new HashMap();
    private ArrayList<SpecCollector> specList = new ArrayList();
    private Id id;

    public MultiSpec(WsNode wsNode) {

        WorkspaceItem ws = (WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class);

        MultiTopComponent window;

        if (!activeMultiWindows.containsKey(ws.getId())) {
            this.id = ws.getId();
            window = new MultiTopComponent(wsNode);
            window.setName("SpecParser for " + ws.getDisplayName());

            /*if the list of items in a multi doc has contents */
            if (!((MultiProcessingDocument) ws.getElement()).getCurrent().isEmpty()) {
                SpecCollector specCollector;
                int counter = 0;
                for (SaItem item : ((MultiProcessingDocument) ws.getElement()).getCurrent()) {
                    if (!item.getEstimationMethod().name.contains("tramo")) {
//                        it is possible that here are also tramo docs in the list
                        specCollector = new SpecCollector((WorkspaceItem) wsNode.getWorkspace().searchDocument(wsNode.lookup(), IModifiable.class), counter);
                        specCollector.setPath(window.getPath());
                        specCollector.translate(TranslationTo_Type.WinX12Spec);
                        specCollector.setName(item.getTs().getRawName());
                        specList.add(specCollector);
                    }
                    counter++;
                }
                window.setSpecArray(specList);
            }
            
            activeMultiWindows.put(this.id, window);

            window.open();
            window.requestActive();
        } else {
            window = activeMultiWindows.get(ws.getId());
            window.setName("SpecParser for " + ws.getDisplayName());

            //close workspace and rename the document
            wsNode.getWorkspace().sortFamily(wsNode.lookup());
            
            window.requestActive();
        }
    }

    public static void deleteWindow(Id id) {
        activeMultiWindows.remove(id);
    }
}
