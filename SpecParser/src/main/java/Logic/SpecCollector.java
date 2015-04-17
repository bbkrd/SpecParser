/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import eu.nbdemetra.specParser.Miscellaneous.TranslationTo_Type;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.x13.X13Specification;
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
    private String[] messages;
    private WorkspaceItem ws;
    private int index; //only for Multi documents
    private String name;


    /*Constructor for Single Documents*/
    public SpecCollector(WorkspaceItem w) {
        ws = w;

        if (ws.getElement() instanceof X13Document) {
            jdSpec = (X13Document) ws.getElement();
            ts = ((X13Document) ws.getElement()).getInput();
        }
    }

    /*Cnstructor for Multi Documents*/
    public SpecCollector(WorkspaceItem w, int i) {

        index = i;
        ws = w;
        if (((MultiProcessingDocument) ws.getElement()).getCurrent().size() - 1 >= index) {
            ts = ((MultiProcessingDocument) ws.getElement()).getCurrent().get(index).getTs();
            jdSpec = ((MultiProcessingDocument) ws.getElement()).getCurrent().get(index).toDocument();
        } else {
            jdSpec = null;
        }
    }

    public void setWinX12Spec(String text) {
        this.winX12SpecText = text;
    }

    public String getWinX12Spec() {
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

    public String[] getMessages() {
        if (messages == null) {
            messages = new String[]{};
        }
        return messages;
    }

    public void setName(String name) {
        //fuer single document wichtig im ws
        if (ws.getElement() instanceof X13Document) {
            if (!name.isEmpty()) {
                this.name = name;
                ws.setDisplayName(name);
                ts = ts.rename(name);
            }
        }
    }

    public void setName() {
        if (ws.getElement() instanceof MultiProcessingDocument) {
            this.name = ((MultiProcessingDocument) ws.getElement()).getCurrent().get(index).getTs().getRawName();
            ts = ts.rename(name);
        }
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public Ts getTs() {
        return ts;
    }

    public void translate(TranslationTo_Type type) {

        if (type == TranslationTo_Type.JDSpec) {
            // Translation from WinX13Spec to JDemetra+Spec
            WinX12SpecSeparator separator = new WinX12SpecSeparator();
            separator.buildSpec(winX12SpecText);
            jdSpec = separator.getResult();
            ts = separator.getTs();

            refreshWS();

            errors = separator.getErrorList();
            messages = separator.getMessageList();
        } else {
            //Translation from JDemetra+Spec to WinX13Spec
            JDSpecSeparator separator;

            if (ws.getElement() instanceof X13Document) {
                ts = ((X13Document) ws.getElement()).getInput();
                separator = new JDSpecSeparator((X13Specification) jdSpec.getSpecification(), ts);
            } else {
                ts = ((MultiProcessingDocument) ws.getElement()).getCurrent().get(index).getTs();
                separator = new JDSpecSeparator((X13Specification) jdSpec.getSpecification(), ts);
            }
            separator.build();
            winX12SpecText = separator.getResult();
            refreshWS();
            errors = separator.getErrorList();
            messages = separator.getMessageList();
        }

    }

    private void refreshWS() {
        if (ws != null) {
            if (ws.getElement() instanceof X13Document) {
                ws.setElement(jdSpec);
                if (ts != null) {
                    ((X13Document) ws.getElement()).setInput(ts);
                }
            } else {
                if (((MultiProcessingDocument) ws.getElement()).getCurrent().size() - 1 >= index) {
                    ((MultiProcessingDocument) ws.getElement()).getCurrent().replace(((MultiProcessingDocument) ws.getElement()).getCurrent().get(index), new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                } else {
//                        ((MultiProcessingDocument) ws.getElement()).getCurrent().add(index, new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                    ((MultiProcessingDocument) ws.getElement()).getCurrent().add(new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                    index = ((MultiProcessingDocument) ws.getElement()).getCurrent().size() - 1;
                }

            }
        }
    }
}
