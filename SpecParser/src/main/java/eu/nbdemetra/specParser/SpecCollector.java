/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;

/**
 *
 * @author Nina Gonschorreck
 */
public class SpecCollector {

    /**
     * This class colletcs both specifications (WinX13, JD+) of a SingleSpec.
     *
     * @param winX13SpecText saves the content of a WinX13Specification in a
     * form of *.SPC file
     * @param jdSpec saves the JDemetra+ specification as an object of
     * SaDocument
     * @param errors collects the errors by translation of specifications
     */
    private String winX12SpecText;
    private SaDocument jdSpec;
    private Ts ts;
    private String[] errors;
    private WorkspaceItem ws;
    private int index; //only for Multi documents
    

    /*Constructor for Single Documents*/
    public SpecCollector(WorkspaceItem w) {
        ws = w;

        if (ws.getElement() instanceof X13Document) {
            jdSpec = (X13Document) ws.getElement();
        }
    }

    /*Cnstructor for Multi Documents*/
    public SpecCollector(WorkspaceItem w, int i) {
        index = i;
        ws = w;
        if (((MultiProcessingDocument) ws.getElement()).getCurrent().size() - 1 >= index) {
            jdSpec = ((MultiProcessingDocument) ws.getElement()).getCurrent().get(index).toDocument();
        } else {
            jdSpec = null;
        }
    }

    public void setWinX13Spec(String text) {
        this.winX12SpecText = text;
    }

    public String getWinX13Spec() {
        return winX12SpecText;
    }


    public void setJDSpec(SaDocument x13) {
        this.jdSpec = x13;
        if (ws != null) {
            if (ws.getElement() instanceof X13Document) {
                ws.setElement(jdSpec);
            }
        }
    }

    public SaDocument getJDSpec() {
        return jdSpec;
    }

    public String[] getErrors() {
        if (errors == null) {
            errors = new String[]{};
        }
        return errors;
    }

    public void setName(String name) {
        //fuer single document wichtig im ws
        if (ws.getElement() instanceof X13Document) {
            ws.setDisplayName(name);
        }
    }

    public String getName() {

        if (ws.getElement() instanceof MultiProcessingDocument) {
            return ((MultiProcessingDocument) ws.getElement()).getCurrent().get(index).toString();
        }
        return "";
    }

    public Ts getTs() {
        return ts;
    }

    public void translate(TranslationTo_Type type) {

        if (type == TranslationTo_Type.JDSpec) {
            // Translation from WinX13Spec to JDemetra+Spec
            WinX13SpecSeparator separator = new WinX13SpecSeparator();
            separator.buildSpec(winX12SpecText);
            jdSpec = separator.getResult();
            ts = separator.getTs();
            
            if (ws != null) {
                if (ws.getElement() instanceof X13Document) {
                    ws.setElement(jdSpec);
                    if (separator.getTs().getTsData() != null) {
                        ((X13Document) ws.getElement()).setInput(ts);
                    }
                } else {
                    if (((MultiProcessingDocument) ws.getElement()).getCurrent().size()-1>=index) {
                        ((MultiProcessingDocument) ws.getElement()).getCurrent().replace(((MultiProcessingDocument) ws.getElement()).getCurrent().get(index), new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                    } else {
                        ((MultiProcessingDocument) ws.getElement()).getCurrent().add(index, new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                    }
                }
            }
            errors = separator.getErrorList();
        } else {
            //Translation from JDemetra+Spec to WinX13Spec
            JDSpecSeparator separator = new JDSpecSeparator();
            //...
        }

    }
}
