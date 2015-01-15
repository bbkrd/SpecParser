/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
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

    public MultiSpec(SaProcessing sa, Id id, String displayName) {

        SingleSpec single;
        for (SaItem item : sa) {
            single = new SingleSpec(item);
            singleSpecList.add(single);
        }
           
        //TODO: 
        //      Transformation der einzelnen SingleSpecs
        //      Liste pflegen mit warnings für MyCellRender im window uebergeben
        
        MultiTopComponent window;
        if (!activeWindows.containsKey(id)) {
            this.id=id;
            window = new MultiTopComponent(displayName);
            window.setName("SpecGenarator for "+displayName);
            
            window.open();
            window.requestActive();

            activeWindows.put(this.id, window);
        } else {
            window = activeWindows.get(id);
            if (window.isOpen()) {
                window.requestActive();
            } else {
                window.open();
                window.requestActive();
            }
        }
        window.setSingleSpecList(singleSpecList);
    }
}