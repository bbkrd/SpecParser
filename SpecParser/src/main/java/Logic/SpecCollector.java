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
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;

/**
 *
 * @author Nina Gonschorreck
 */
public class SpecCollector {

    /*  winX12SpecText  -   saves the content of a WinX12 spc file
     *   jdSpec          -   saves the JD+ spec as a SaDocument
     *   ts              -   a time serie object to handle data     */
    private String winX12SpecText;
    private SaDocument jdSpec;
    private Ts ts;

   /*   regressor   -   object for regression (data, GUI, ...)
    *   regName     -   name of regressor                           */
    private DynamicTsVariable regressor;
    private String regName;

    /*  errors      -   collects errors which appear by translating
    *   messages    -   collects messages which appear by translating   */
    private String[] errors;
    private String[] messages;

    /*  index   -   reference to each single item in a multi doc the corresponding SpecCollector (only for multi docs important)
    *   name    -   */
    private int index; 
    private String name;

    /*       wsItem      -   WorspaceItem, to modify the item in the GUI (name, data, spec)
     *       path        -   to open the a directory for loading and saving, notice the last directory by loading   */
    private WorkspaceItem wsItem;
    private String path;


    /*Constructor for Single Documents*/
    public SpecCollector(WorkspaceItem w) {

        wsItem = w;
        if (wsItem.getElement() instanceof X13Document) {
            jdSpec = (X13Document) wsItem.getElement();
            ts = ((X13Document) wsItem.getElement()).getInput();
        }
    }

    /*Cnstructor for Multi Documents*/
    public SpecCollector(WorkspaceItem w, int i) {

        index = i;
        wsItem = w;

        if (((MultiProcessingDocument) wsItem.getElement()).getCurrent().size() - 1 >= index) {
            ts = ((MultiProcessingDocument) wsItem.getElement()).getCurrent().get(index).getTs();
            jdSpec = ((MultiProcessingDocument) wsItem.getElement()).getCurrent().get(index).toDocument();
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

    public DynamicTsVariable getRegressor() {
        return regressor;
    }

    public String getRegressorName() {
        return regName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setJDSpec(SaDocument x13) {

        this.jdSpec = x13;
        if (wsItem != null) {
            if (wsItem.getElement() instanceof X13Document) {
                wsItem.setElement(jdSpec);
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

        if (wsItem.getElement() instanceof X13Document) {
            if (!name.isEmpty()) {
                this.name = name.replaceAll("\\.spc", "").replaceAll("\\.SPC", "");
                if (ts != null) {
                    ts = ts.rename(name);
                }
            }
        } else {
            this.name = name;
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

    /*  MAIN METHOD
    *   decided which Separator will be invoked
    *   starts the translation and saves the results
    */
    public void translate(TranslationTo_Type type) {

        if (type == TranslationTo_Type.JDSpec) {
            // Translation from WinX12Spec to JDemetra+Spec   
            
            WinX12SpecSeparator separator = new WinX12SpecSeparator();            
            separator.setPath(path);
            separator.buildSpec(winX12SpecText);

            if (separator.getTs().getTsData() != null) {
                ts = separator.getTs();
                jdSpec = separator.getResult();
                //regressor
//                if (separator.getRegressorName() != null) {
//                    regressor=separator.getRegressor();
//                    regName=separator.getRegressorName();
//                }

                refreshWS();

                errors = separator.getErrorList();
                messages = separator.getMessageList();
            } else {
//              if data are missing
                errors = new String[1];
                errors[0] = "NO DATA";
                messages = new String[1];
                messages[0] = "NO DATA";
            }
        } else {
            //Translation from JDemetra+Spec to WinX12Spec           
            if (wsItem.getElement() instanceof X13Document) {
                ts = ((X13Document) wsItem.getElement()).getInput();
            } else {
                ts = ((MultiProcessingDocument) wsItem.getElement()).getCurrent().get(index).getTs();
            }
            
            if (ts.getTsData() != null) {
                JDSpecSeparator separator = new JDSpecSeparator((X13Specification) jdSpec.getSpecification(), ts, wsItem.getOwner().getContext());
                separator.build();
                winX12SpecText = separator.getResult();
                
                refreshWS();
                
                errors = separator.getErrorList();
                messages = separator.getMessageList();
            } else {
                //if data are missing
                errors = new String[1];
                errors[0] = "NO DATA No Work";
                messages = new String[1];
                messages[0] = "NO DATA!!!";
            }
        }
    }

    private void refreshWS() {

        if (wsItem != null) {
            if (wsItem.getElement() instanceof X13Document) {
//                set generated JD Spec and Ts data to the workspace
                wsItem.setElement(jdSpec);
                ((X13Document) wsItem.getElement()).setInput(ts);
            } else {
                
                if (((MultiProcessingDocument) wsItem.getElement()).getCurrent().size() - 1 >= index) {
//                    replace a single Item to the workspace
                    ((MultiProcessingDocument) wsItem.getElement()).getCurrent().replace(((MultiProcessingDocument) wsItem.getElement()).getCurrent().get(index), new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                } else {
//                    add a new singl item to the workspace
                    ((MultiProcessingDocument) wsItem.getElement()).getCurrent().add(new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                    index = ((MultiProcessingDocument) wsItem.getElement()).getCurrent().size() - 1;
                }
            }
        }//wsItem == null : do nothing
    }
}
