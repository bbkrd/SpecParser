/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import eu.nbdemetra.specParser.Miscellaneous.TranslationTo_Type;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ui.variables.VariablesDocumentManager;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.Ts;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import java.util.ArrayList;

/**
 *
 * @author Nina Gonschorreck
 */
public class SpecCollector {

    /*   winX12SpecText  -   saves the content of a WinX12 spc file
     *   jdSpec          -   saves the JD+ spec as a SaDocument
     *   ts              -   a time serie object to handle data     */
    private String winX12SpecText;
    private SaDocument jdSpec;
    private Ts ts;

    /*   regressor   -   object for regression (data, GUI, ...)
     *   regName     -   name of regressor                           */
//    private DynamicTsVariable[] regressor;
//    private String[] regName;

    /*  errors      -   collects errors which appear by translating
     *   messages    -   collects messages which appear by translating   */
    private String[] errors;
    private String[] messages;
    private String[] warnings;

    /*  index   -   reference to each single item in a multi doc the corresponding SpecCollector (only for multi docs important)
     *   name    -   */
    private int index;
    private String name;
//    private String nameForWS;

    /*       wsItem      -   WorspaceItem, to modify the item in the GUI (name, data, spec)
     *       path        -   to open the a directory for loading and saving, notice the last directory by loading   */
    private WorkspaceItem wsItem;
    private String path;

    private String regData;

    /*Constructor for Single Documents*/
    public SpecCollector(WorkspaceItem w) {

        wsItem = w;
        if (wsItem.getElement() instanceof X13Document) {
            jdSpec = (X13Document) wsItem.getElement();
            ts = ((X13Document) wsItem.getElement()).getInput();
        }
    }

    /*Constructor for Multi Documents*/
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

    public String[] getWarnings() {
        if (warnings == null) {
            warnings = new String[]{};
        }
        return warnings;
    }

    public String getRegData() {
        return regData;
    }

    public void setName(String name) {

        if (wsItem.getElement() instanceof X13Document) {
            if (!name.isEmpty()) {
                this.name = name.replaceAll("\\.spc", "").replaceAll("\\.SPC", "");
                if (ts != null) {
                    ts = ts.rename(name);
                }
            }
//            nameForWS = this.name;
        } else {
            //multi document: Name of mta?
            this.name = name;
        }
    }

//    public void setNameForWS(String name) {
//        nameForWS = name;
//    }
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
     *   
     *   decided which Separator will be invoked
     *   starts the translation and saves the results
     */
    public void translate(TranslationTo_Type type) {

        if (type == TranslationTo_Type.JDSpec) {
            // Translation from WinX12Spec to JDemetra+Spec   

            WinX12SpecSeparator separator = new WinX12SpecSeparator();
            separator.setPath(path);
//            separator.setMtaName(nameForWS);
            separator.setName(name);
            separator.buildSpec(winX12SpecText);

            if (separator.getTs().getTsData() != null) {
                ts = separator.getTs();

                //regressors
                if (separator.getRegressorName() != null && separator.getRegressor() != null) {
                    if (wsItem.getOwner() != null) {
                        writeRegressors(separator);
                    }
                }

                jdSpec = separator.getResult();
                refreshWS();

//                errors = separator.getErrorList();
//                warnings=separator.getWarningList();
//                messages = separator.getMessageList();
            }
//            else {
////              if data are missing
//                errors = new String[1];
//                errors[0] = "NO DATA";
////                messages = separator.getMessageList();
//            }
            errors = separator.getErrorList();
            warnings = separator.getWarningList();
            messages = separator.getMessageList();
        } else {
            //Translation from JDemetra+Spec to WinX12Spec 
            if (ts != null) {
                if (wsItem.getElement() instanceof X13Document) {
                    ts = ((X13Document) wsItem.getElement()).getInput();
                } else {
                    ts = ((MultiProcessingDocument) wsItem.getElement()).getCurrent().get(index).getTs();
                }

                if (ts.getTsData() != null) {
                    JDSpecSeparator separator = new JDSpecSeparator((X13Specification) jdSpec.getSpecification(), ts, wsItem.getOwner().getContext());
                    separator.build();
                    winX12SpecText = separator.getResult();

                    //anfrage machen
//                    regData = separator.getRegressors();
                    refreshWS();

                    errors = separator.getErrorList();
                    messages = separator.getMessageList();
                } else {
                    //if data are missing
                    errors = new String[1];
                    errors[0] = "NO DATA No Work";
//                    messages = new String[1];
//                    messages[0] = "NO DATA!!!";
                }

            } else {
                //if data are missing
                errors = new String[1];
                errors[0] = "NO DATA No Work";
//                messages = new String[1];
//                messages[0] = "NO DATA!!!";
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
//                    add a new single item to the workspace
                    ((MultiProcessingDocument) wsItem.getElement()).getCurrent().add(new SaItem((ISaSpecification) jdSpec.getSpecification(), ts));
                    index = ((MultiProcessingDocument) wsItem.getElement()).getCurrent().size() - 1;
                }
            }
        }//wsItem == null : do nothing
    }

    private void writeRegressors(WinX12SpecSeparator separator) {

//        String reg_name = "reg_" + nameForWS;
        String reg_name = "reg_SpecParser";
        String curName;
        TsVariable[] regressor = separator.getRegressor();
        String[] regName = separator.getRegressorName();

        WorkspaceItem<TsVariables> wsVariables = (WorkspaceItem<TsVariables>) wsItem.getOwner().searchDocumentByName(VariablesDocumentManager.ID, reg_name);

        if (wsVariables == null) {
            IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(VariablesDocumentManager.ID);
            wsVariables = mgr.create(wsItem.getOwner());
        }

        String[] regTyp = separator.getRegressorTyp();
        ArrayList<String> td = new ArrayList();
        ArrayList<TsVariableDescriptor> user = new ArrayList();

        //1. exists var list with name reg_name?
        if (!wsVariables.getIdentifier().equals(reg_name)) {
            wsVariables.setIdentifier(reg_name);
            wsVariables.setDisplayName(reg_name);
            wsVariables.getElement().clear();
            wsItem.getOwner().getContext().getTsVariableManagers().set(reg_name, wsVariables.getElement());
        }

        for (int i = 0; i < regressor.length; i++) {
            curName = regName[i];

            //is there a regressor with this curName?
            if (wsItem.getOwner().getContext().getTsVariableDictionary().contains(reg_name + "." + curName)) {

                //check: are data not equal?
                if (!((TsVariable) wsVariables.getElement().get(curName)).getTsData().equals(regressor[i].getTsData())) {

                    //create curname= curname + [i]
                    boolean found = false;
                    int iterator = 1;
                    String ends;
                    while (found == false) {
                        ends = "[" + iterator + "]";
                        //wenn bereits curname[...] existiert
                        if (wsItem.getOwner().getContext().getTsVariableDictionary().contains(reg_name + "." + curName + ends)) {
                            //guck ob Daten gleich sind
                            if (((TsVariable) wsVariables.getElement().get(curName + ends)).getTsData().equals(regressor[i].getTsData())) {
                                //Daten sind gleich
                                found = true;
                            } else {
                                iterator++;
                            }
                        } else {
                            //wenn noch nicht existiert dann kann neu angelegt werden
                            curName = curName.concat(ends);
                            wsVariables.getElement().set(curName, regressor[i]);
                            found = true;
                        }
                    }
                }else{
                    //startdate gleich, refresh datum
                    
                }
            } else {

                wsVariables.getElement().set(curName, regressor[i]);
            }

            switch (regTyp[i].toUpperCase()) {
                case "TD":
                    td.add(reg_name + "." + curName);
                    break;
                case "USER":
                    TsVariableDescriptor userVar = new TsVariableDescriptor();
                    userVar.setName(reg_name + "." + curName);
                    //unterscheidung je nach final = user
                    if (separator.isFinalUser()) {
                        userVar.setEffect(TsVariableDescriptor.UserComponentType.Series);

                    } else {
                        userVar.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
                    }
                    user.add(userVar);
                    break;
                case "SEASONAL":
                    TsVariableDescriptor var = new TsVariableDescriptor();
                    var.setName(reg_name + "." + curName);
                    var.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
                    user.add(var);
                    break;
                default:
                    TsVariableDescriptor userVar2 = new TsVariableDescriptor();
                    userVar2.setName(reg_name + "." + curName);
                    //unterscheidung je nach final = user
                    userVar2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
                    user.add(userVar2);
                    break;
            }
        }
        separator.setRegressorsInSpec(td.toArray(new String[0]), user.toArray(new TsVariableDescriptor[0]));

    }
}
