/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.tss.sa.SaItem;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.utilities.Id;
import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class SingleSpec {

    private SpecCollector spec = new SpecCollector();
    private SaItem item;
    private SingleTopComponent window;
    private static HashMap<Id, SingleTopComponent> activeWindows = new HashMap();
    private Id id;
    private String displayName;

    
    /*Constructor for MultiDocument Spec windows*/
    public SingleSpec(SaItem item) {
        this.item = item;
        spec.setJDSpec(item.toDocument());
    }

    /*Constructor for SingleDocuments*/
    public SingleSpec(X13Document x13, Id id, String displayName) {

        if (!activeWindows.containsKey(id)) {
            this.spec.setJDSpec(x13);
            this.id=id;
            this.displayName=displayName;
            window = new SingleTopComponent();
            window.setName("SpecGenerator for "+this.displayName);

            window.setSpecView(spec);
            window.open();
            window.requestActive();

            activeWindows.put(this.id, window);
        } else {
            window = activeWindows.get(this.id);
            if (window.isOpen()) {
                window.requestActive();
            } else {
                window.open();
                window.requestActive();
            }
        }
        window.setSpecView(spec);
            }

    public String getDisplayName(){
        return displayName;
    }
    
    public SpecCollector getSpecCollector(){
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
}
