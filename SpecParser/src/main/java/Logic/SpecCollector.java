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
import ec.tstoolkit.data.DataBlock;
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

    /*  index   -   reference to each single item in a multi doc the corresponding SpecCollector (only for multi docs important)
     *   name    -   */
    private int index;
    private String name;
    private String nameForWS;

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
    
    public String getRegData(){
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
            nameForWS=this.name;
        } else {
            //multi document: Name of mta?
            this.name = name;
        }
    }
    
    public void setNameForWS(String name){
        nameForWS=name;
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
     *   
     *   decided which Separator will be invoked
     *   starts the translation and saves the results
     */
    public void translate(TranslationTo_Type type) {

        if (type == TranslationTo_Type.JDSpec) {
            // Translation from WinX12Spec to JDemetra+Spec   

            WinX12SpecSeparator separator = new WinX12SpecSeparator();
            separator.setPath(path);
            separator.setMtaName(nameForWS);
            separator.buildSpec(winX12SpecText);

            if (separator.getTs().getTsData() != null) {
                ts = separator.getTs();
                jdSpec = separator.getResult();

                //regressors
                if (separator.getRegressorName() != null && separator.getRegressor() != null) {
                    
                    String reg_name = "reg_"+nameForWS; 
                    
                    TsVariable[] regressor = separator.getRegressor();
                    String[] regName = separator.getRegressorName();

                    if (wsItem.getOwner() != null) {
                        WorkspaceItem<TsVariables> wsVariables = (WorkspaceItem<TsVariables>) wsItem.getOwner().searchDocumentByName(VariablesDocumentManager.ID, reg_name);
//                        if (wsVariables==null || !wsVariables.getIdentifier().equals("reg_"+name)) {
//                            //new regressors
//                            IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(VariablesDocumentManager.ID);
//                            wsVariables = mgr.create(wsItem.getOwner());
//                            wsItem.getOwner().getContext().getTsVariableManagers().rename(wsVariables.getDisplayName(), "reg_" + name);
//                            wsVariables.setIdentifier("reg_" + name);
//                            wsVariables.setDisplayName("reg_" + name);

                        if (wsVariables == null) {
                            IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(VariablesDocumentManager.ID);

                            wsVariables = mgr.create(wsItem.getOwner());
                        }

                        
                        if (!wsVariables.getIdentifier().equals(reg_name)) {
//                            wsItem.getOwner().getContext().getTsVariableManagers().rename(wsVariables.getDisplayName(), "reg_" + name);

                            wsVariables.setIdentifier(reg_name);
                            wsVariables.setDisplayName(reg_name);//wsItem + tab
                            wsVariables.getElement().clear();
                            wsItem.getOwner().getContext().getTsVariableManagers().set(reg_name, wsVariables.getElement());

                            for (int i = 0; i < regressor.length; i++) {
                                if (!wsItem.getOwner().getContext().getTsVariableDictionary().contains(reg_name + "." + regName[i])) {
                                    wsVariables.getElement().set(regName[i], regressor[i]);
                                    System.out.println("1 "+name+": "+reg_name + "." + regName[i]);
                                }
                            }
                        } else {
                            //refresh regression
                            for (int i = 0; i < regressor.length; i++) {

                                if (!wsItem.getOwner().getContext().getTsVariableDictionary().contains(reg_name + "." + regName[i])) {
                                    //regressor ex. noch nicht
                                    wsVariables.getElement().set(regName[i], regressor[i]);
                                    System.out.println("2 "+name+": "+reg_name + "." + regName[i]);
                                } else {
                                    System.out.println("3 "+name+": "+reg_name + "." + regName[i]);
                                    //existiert bereits, Werte aktualisieren
                                   /* ArrayList<DataBlock> data = new ArrayList();
                                    DataBlock d = new DataBlock(regressor[i].getTsData().rextract(0, regressor[i].getTsData().getLength()));
                                    data.add(d);
                                    wsVariables.getElement().get(regName[i]).data(regressor[i].getDefinitionDomain(), data);*/
                                }
                            }
                        }
                    }
                }

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
            if (ts != null) {
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

                    //anfrage machen
                    regData = separator.getRegressors();
                    
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
