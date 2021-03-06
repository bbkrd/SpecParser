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
package Logic;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ui.variables.VariablesDocumentManager;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import eu.nbdemetra.specParser.Miscellaneous.TranslationInfo;
import eu.nbdemetra.specParser.Miscellaneous.TranslationTo_Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import options.SpecParserOptionsPanelController;
import org.openide.util.NbPreferences;

/**
 * This class collects all information for the specification of a document.
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
    private MetaData meta;

    /*   regressor   -   object for regression (data, GUI, ...)
     *   regName     -   name of regressor                           */
//    private DynamicTsVariable[] regressor;
//    private String[] regName;

    /*  errors      -   collects errors which appear by translating
     *   messages    -   collects messages which appear by translating   */
    private HashMap<String, TranslationInfo> translation_messages = new HashMap<>();

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

    public MetaData getMetaData() {
        return meta;
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
            }/*else{
             ((MultiProcessingDocument) wsItem.getElement()).getCurrent().get(index).setPointSpecification((ISaSpecification) jdSpec.getSpecification());
             }*/

        }
    }

    public SaDocument getJDSpec() {
        return jdSpec;
    }

    public HashMap<String, TranslationInfo> getTranslation_messages() {
        return translation_messages;
    }

    public List<String> getErrors() {

        if (!translation_messages.containsValue(TranslationInfo.ERROR)) {
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<String, TranslationInfo> entry : translation_messages.entrySet()) {
            if (entry.getValue().equals(TranslationInfo.ERROR)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<String> getMessages() {
        if (!translation_messages.containsValue(TranslationInfo.MESSAGE)) {
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<String, TranslationInfo> entry : translation_messages.entrySet()) {
            if (entry.getValue().equals(TranslationInfo.MESSAGE)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<String> getWarnings1() {
        if (!translation_messages.containsValue(TranslationInfo.WARNING1)) {
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<String, TranslationInfo> entry : translation_messages.entrySet()) {
            if (entry.getValue().equals(TranslationInfo.WARNING1)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<String> getWarnings2() {
        if (!translation_messages.containsValue(TranslationInfo.WARNING2)) {
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<String, TranslationInfo> entry : translation_messages.entrySet()) {
            if (entry.getValue().equals(TranslationInfo.WARNING2)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public String getRegData() {
        return regData;
    }

    public void setName(String name) {

        if (wsItem.getElement() instanceof X13Document) {
            if (!name.isEmpty()) {
                this.name = name.replace(".spc", "").replace(".SPC", "");
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
            // Translation from WinX12Spec to JDemetra+ Spec

            WinX12SpecSeparator separator = new WinX12SpecSeparator();
            separator.setPath(path);
//            separator.setMtaName(nameForWS);
            separator.setName(name);
            separator.buildSpec(winX12SpecText);

            ts = separator.getTs();
            meta = separator.getMetaData();
            //regressors
            if (separator.getRegressorName() != null && separator.getRegressor() != null) {
                if (wsItem.getOwner() != null) {
                    writeRegressors(separator);
                }
            }

            jdSpec = separator.getResult();

            translation_messages = separator.getTranslationInfos();
            if (ts.getTsData() != null) {
                refreshWS(separator.getName());
            } else {
                translation_messages.put("NO DATA", TranslationInfo.ERROR);
            }
        }
    }

    private void refreshWS(String name) {

        if (wsItem != null) {

            if (wsItem.getElement() instanceof X13Document) {
//                set generated JD Spec and Ts data to the workspace
                wsItem.setElement(jdSpec);
                ((X13Document) wsItem.getElement()).setInput(ts);
            } else {
                // Does the index already exist in the document?
                SaProcessing processing = ((MultiProcessingDocument) wsItem.getElement()).getCurrent();
                SaItem saItem = new SaItem((ISaSpecification) jdSpec.getSpecification(), ts);
                saItem.setMetaData(jdSpec.getMetaData());
                saItem.setName(name);
                if (processing.size() - 1 >= index) {
//                    replace a single Item to the workspace
                    processing.replace(processing.get(index), saItem);
                } else {
//                    add a new single item to the workspace
                    processing.add(saItem);
                    index = processing.size() - 1;
                }
            }
        }//wsItem == null : do nothing
    }

    private void writeRegressors(WinX12SpecSeparator separator) {

//        String reg_name = "reg_" + nameForWS;
        String reg_SpecParser = "reg_SpecParser";
        String curName;

        TsVariable[] regressor = separator.getRegressor();
        String[] regName = separator.getRegressorName();
        String[] regTyp = separator.getRegressorTyp();

        ArrayList<String> td = new ArrayList();
        ArrayList<TsVariableDescriptor> user = new ArrayList();

        WorkspaceItem<TsVariables> wsVariables = (WorkspaceItem<TsVariables>) wsItem.getOwner().searchDocumentByName(VariablesDocumentManager.ID, reg_SpecParser);
        if (wsVariables == null) {
            IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(VariablesDocumentManager.ID);
            wsVariables = mgr.create(wsItem.getOwner());
        }

        //1. exists var list with name reg_name?
        if (!wsVariables.getIdentifier().equals(reg_SpecParser)) {
            wsVariables.setIdentifier(reg_SpecParser);
            wsVariables.setDisplayName(reg_SpecParser);
            wsVariables.getElement().clear();
            wsItem.getOwner().getContext().getTsVariableManagers().set(reg_SpecParser, wsVariables.getElement());
        }

        for (int i = 0; i < regressor.length; i++) {
            curName = regName[i];

            int counter_single_usertype = i;
            if (regTyp.length == 1 && regTyp.length != regressor.length) {
                counter_single_usertype = 0;
            }

            //is there a regressor with this curName?
            if (wsItem.getOwner().getContext().getTsVariableDictionary().contains(reg_SpecParser + "." + curName)) {

                //check: are data not equal?
                if (!((TsVariable) wsVariables.getElement().get(curName)).getTsData().equals(regressor[i].getTsData())) {

                    //create curname= curname + [i]
                    boolean found = false;
                    int iterator = 1;
                    String ends;
                    while (found == false) {
                        ends = "[" + iterator + "]";
                        //wenn bereits curname[...] existiert
                        if (wsItem.getOwner().getContext().getTsVariableDictionary().contains(reg_SpecParser + "." + curName + ends)) {
                            //guck ob Daten gleich sind
                            if (((TsVariable) wsVariables.getElement().get(curName + ends)).getTsData().equals(regressor[i].getTsData())) {
                                //Daten sind gleich
                                found = true;
                                curName = curName + ends;
                            } else {
                                iterator++;
                            }
                        } else {
                            //wenn noch nicht existiert dann kann neu angelegt werden
                            String newName = curName.concat(ends);
                            wsVariables.getElement().set(newName, regressor[i]);
                            found = true;
                            //in Spec umsetzen

                            //if in td
                            String[] tdVars = separator.getCurrentSpec().getRegArimaSpecification().getRegression().getTradingDays().getUserVariables();
                            if (tdVars != null && tdVars.length != 0) {
                                for (int j = 0; j < tdVars.length; j++) {
                                    if (tdVars[j].equals(curName)) {
                                        tdVars[j] = newName;
                                    }
                                }
                                separator.getCurrentSpec().getRegArimaSpecification().getRegression().getTradingDays().setUserVariables(tdVars);
                            }

                            //if in userdefiend
                            if (separator.getCurrentSpec().getRegArimaSpecification().getRegression().getUserDefinedVariablesCount() != 0) {

                                TsVariableDescriptor[] tmp = separator.getCurrentSpec().getRegArimaSpecification().getRegression().getUserDefinedVariables();

                                for (int j = 0; j < tmp.length; j++) {
                                    if (tmp[j].getName().equals(curName)) {
                                        tmp[j].setName(newName);
                                    }
                                }
                                separator.getCurrentSpec().getRegArimaSpecification().getRegression().setUserDefinedVariables(tmp);
                            }
                            curName = newName;
                        }
                    }
                } else {
                    //later: startdate equal, refresh start date
                }
            } else {
                wsVariables.getElement().set(curName, regressor[i]);
            }

            Preferences node = NbPreferences.forModule(SpecParserOptionsPanelController.class);
            String vars_loc = node.get(SpecParserOptionsPanelController.SPECPARSER_VARS_LOCATION, SpecParserOptionsPanelController.DEFAULT_MODE);

            if (vars_loc.equals(SpecParserOptionsPanelController.CALENDAR_MODE)) {
//                switch (vars_loc.toLowerCase()) {
//                case SpecParserOptionsPanelController.CALENDAR_MODE:
                td.add(reg_SpecParser + "." + curName);
//                    break;
            } else {
//                case SpecParserOptionsPanelController.DEFAULT_MODE:
//                default:
                switch (regTyp[counter_single_usertype].toUpperCase()) {
                    case "TD":
                        td.add(reg_SpecParser + "." + curName);
                        break;
                    case "USER":
                        TsVariableDescriptor userVar = new TsVariableDescriptor();
                        userVar.setName(reg_SpecParser + "." + curName);

                        // differences for final = user
                        if (separator.isFinalUser()) {
                            userVar.setEffect(TsVariableDescriptor.UserComponentType.Series);
                        } else {
                            //default: final != user
                            userVar.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
                        }
                        user.add(userVar);
                        break;
                    case "SEASONAL":
                        TsVariableDescriptor var = new TsVariableDescriptor();
                        var.setName(reg_SpecParser + "." + curName);
                        var.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
                        user.add(var);
                        break;
                    case "LS":
                        TsVariableDescriptor v = new TsVariableDescriptor();
                        v.setName(reg_SpecParser + "." + curName);
                        v.setEffect(TsVariableDescriptor.UserComponentType.Trend);
                        user.add(v);
                        break;
                    /*Version 1.5.6 Sylwias Mail vom 12.10.*/
                    case "HOLIDAY":
//                    td.add(reg_SpecParser + "." + curName);
                        TsVariableDescriptor vars = new TsVariableDescriptor();
                        vars.setName(reg_SpecParser + "." + curName);
                        vars.setEffect(TsVariableDescriptor.UserComponentType.Undefined);
                        user.add(vars);
                        break;
                    default:
                        TsVariableDescriptor userVar2 = new TsVariableDescriptor();
                        userVar2.setName(reg_SpecParser + "." + curName);

                        // differences for final = user
                        userVar2.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
                        user.add(userVar2);
                        break;
                }
//            }
            }
        }
        separator.setRegressorsInSpec(td.toArray(new String[0]), user.toArray(new TsVariableDescriptor[0]));
    }
}
