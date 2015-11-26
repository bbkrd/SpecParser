/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import WriteAndRead.DataLoaderRegression;
import WriteAndRead.DataLoader;
import WriteAndRead.Izisl;
import eu.nbdemetra.specParser.Miscellaneous.*;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsMoniker;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.*;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.openide.util.Lookup;

/**
 *
 * @author Nina Gonschorreck
 */
public class WinX12SpecSeparator {

    /*
     spec   -   collect the x13Specification for JD+
     errors -   collect message by translating
     period -   Period from WinX12, in JD+ it is given by the data
     */
    private X13Specification spec = new X13Specification();

    private ArrayList<String> errors = new ArrayList();
    private ArrayList<String> messages = new ArrayList();
    private ArrayList<String> warnings = new ArrayList();

    //no equivalence in a JD+ spec item, this information is given in Ts
    //for timeseries
    private DataLoader dataLoader = new DataLoader();
    private String tsName = null;
    //for regression variables
    private boolean regressionSpec = false;
    private DataLoaderRegression regressionLoader = new DataLoaderRegression();
    private String[] regressionTyp;
    private String mtaName;
    private String name;

    //nessecary for File loading
    private String path;

    //transformation conditions
    private boolean transformNone = false;
    private boolean transformLog = false;
    private boolean transformAuto = false;

    //
    private boolean onlyX11 = true;
    private boolean outlierDefaults = false;
    private boolean automdlDefault = false;
    private boolean calendarsigmaSelect = false;
    private boolean finalIsUser = false;

    public WinX12SpecSeparator() {
//        setDefaults();
        spec.getRegArimaSpecification().getBasic().setPreprocessing(false);
        setDefaults();
    }

    private void setX12Part() {
        onlyX11 = false;
        spec.getRegArimaSpecification().getBasic().setPreprocessing(true);
        setDefaults();
    }

    private void setOUTLIERDefaults() {

        outlierDefaults = true;

        spec.getRegArimaSpecification().getOutliers().setLSRun(0);
        spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddOne);
        spec.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
        spec.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
        spec.getRegArimaSpecification().getOutliers().setMonthlyTCRate(0.7);
    }

    private void setAUTOMDLDefaults() {
        //automdl

        automdlDefault = true;

        spec.getRegArimaSpecification().getAutoModel().setEnabled(true);
        spec.getRegArimaSpecification().getAutoModel().setAcceptDefault(false);
        spec.getRegArimaSpecification().getAutoModel().setCheckMu(true);
        spec.getRegArimaSpecification().getAutoModel().setMixed(true);
        spec.getRegArimaSpecification().getAutoModel().setLjungBoxLimit(0.95);
        spec.getRegArimaSpecification().getAutoModel().setArmaSignificance(1.0);
        spec.getRegArimaSpecification().getAutoModel().setBalanced(false);
        spec.getRegArimaSpecification().getAutoModel().setHannanRissanen(false);
        spec.getRegArimaSpecification().getAutoModel().setPercentReductionCV(0.14268);
        spec.getRegArimaSpecification().getAutoModel().setUnitRootLimit(1.05);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMtaName(String name) {
        mtaName = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getErrorList() {
        return errors.toArray(new String[errors.size()]);
    }

    public String[] getMessageList() {
        return messages.toArray(new String[messages.size()]);
    }

    public String[] getWarningList() {
        return warnings.toArray(new String[warnings.size()]);
    }

    public X13Document getResult() {
        X13Document x13 = new X13Document();
        x13.setSpecification(spec);
        return x13;
    }

    public Ts getTs() {
        if(tsName==null){
            tsName=name;
        }
        return TsFactory.instance.createTs(tsName, dataLoader.getMoniker(), null, dataLoader.getData());
    }

    public String[] getRegressorName() {
        return regressionLoader.getRegressorName();
    }

    public TsVariable[] getRegressor() {
        if (regressionSpec) {
            if (regressionLoader.getRegressors() != null) {
                return regressionLoader.getRegressors();
            }
            messages.add("REGRESSION: " + regressionLoader.getMessages());
        }
        return null;
    }

    public String[] getRegressorTyp() {
        return regressionTyp;
    }

    public boolean isFinalUser() {
        return finalIsUser;
    }

    public void buildSpec(String winX13Text) {

        //0. delete all comments and empty lines
        StringBuilder sb;
        StringBuilder collector = new StringBuilder();

        boolean linebreak = false;
        String[] allLines = winX13Text.split("\n");
        for (String line : allLines) {
            line = line.trim();
            if (line.contains("#")) {
                line = line.substring(0, line.indexOf("#"));
            }
            if (line.contains("!")) {
                line = line.substring(0, line.indexOf("!"));
            }
            if (!line.replaceAll("\\s", "").isEmpty()) {
                sb = new StringBuilder(";");
                line = sb.append(line).toString();
            }
            if (linebreak == true) {
                if (line.contains(")")) {
                    linebreak = false;

                }
                line = line.replaceAll("\\n", " ");
                line = line.replaceAll(";", " ");
//                sb = new StringBuilder();
//                line = sb.append(line).toString();
            }
            if (line.contains("(")) {
                if (!line.contains(")")) {
                    linebreak = true;
                }
            }
            collector.append(line);
        }
        winX13Text = collector.toString();

        //1. split on "}" to seperate the specification parts
        String[] specParts = winX13Text.split("}");

        Method m;
        StringBuilder method;
        SpecificationPart specPartName;
        String[] specPartSplitted, lines, lineSplitted;
        boolean noArgument = false;

        //2. for each specification part split on "{" to separate name and content
        for (String item : specParts) {
            specPartSplitted = item.split("\\{");
            //seperate the specification part name
            specPartSplitted[0] = specPartSplitted[0].replaceAll(";", "");
            specPartSplitted[0] = specPartSplitted[0].replaceAll("\\s", "");
            try {
                specPartName = SpecificationPart.valueOf(specPartSplitted[0].toUpperCase());
                if (!specPartName.equals(SpecificationPart.X11) && !specPartName.equals(SpecificationPart.SERIES)) {
                    if (onlyX11) {
                        setX12Part();
                    }
                }
                if (specPartSplitted.length > 1) {
                    //all values for one argument in one line
                    specPartSplitted[1] = specPartSplitted[1].replaceAll("\n", " ");

                    if (!specPartSplitted[1].replaceAll(";", "").replaceAll("\\s*", "").isEmpty()) {
                        //3. split on line breaks signed by ;
                        lines = specPartSplitted[1].split(";");

//                //4. for each line split on "=" to separate arguments and values
                        for (String tmp : lines) {
                            if (tmp.contains("=")) {
                                lineSplitted = tmp.split("=", 2);
                                lineSplitted[0] = lineSplitted[0].replaceAll("\\s", "");
                                method = new StringBuilder("read_");
                                method.append(lineSplitted[0].toLowerCase());

                                try {
                                    //5. try to invoke the method for the argument
                                    m = this.getClass().getDeclaredMethod(method.toString().toLowerCase(), SpecificationPart.class, String.class);
                                    m.setAccessible(true);
                                    m.invoke(this, specPartName, lineSplitted[1]);
                                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                                    warnings.add(specPartName.name() + ": No support for argument " + lineSplitted[0].toUpperCase());
                                    //eventuell aufgliedern
                                }
                            }
                        }
                    } else {
                        noArgument = true;
                    }
                } else {
                    noArgument = true;
                }

                if (noArgument) {
                    //if in {} no arguments then set Defaults for the specification part
                    method = new StringBuilder("set");
                    method.append(specPartName.name().toUpperCase()).append("Defaults");
                    try {
                        m = this.getClass().getDeclaredMethod(method.toString());
                        m.setAccessible(true);
                        m.invoke(this);

                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                        warnings.add(specPartName.name() + ": No support for defaults. Please define some arguments.");
                    }
                }
            } catch (IllegalArgumentException ex) {
                warnings.add(specPartSplitted[0].toUpperCase() + ": No support for spec " + specPartSplitted[0].toUpperCase());
            }
            noArgument = false;
        }  //if regression user defined variables are in the spec
//        if (regressionTyp != null) {
//            ArrayList<String> td = new ArrayList();
//            ArrayList<TsVariableDescriptor> user = new ArrayList();
//            String[] regNames = regressionLoader.getRegressorName();
//
//            for (int i = 0; i < regressionTyp.length; i++) {
//                switch (regressionTyp[i]) {
//                    case "TD":
//                        td.add("reg_" + mtaName + "." + name + "_" + regNames[i]);
//                        break;
//                    case "USER":
//                        TsVariableDescriptor userVar = new TsVariableDescriptor();
//                        userVar.setName("reg_" + mtaName + "." + name + "_" + regNames[i]);
//                        if (isFinalUser()) {
//                            userVar.setEffect(TsVariableDescriptor.UserComponentType.Series);
//                        } else {
//                            userVar.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
//                        }
//                        user.add(userVar);
//                        break;
//                    default: //darf eig nicht auftreten 
//                        break;
//                }
//            }
//
//        }
    }

    public void setRegressorsInSpec(String[] td, TsVariableDescriptor[] user) {
//        if (td != null) {
        if (td.length > 0) {
            spec.getRegArimaSpecification().getRegression().getTradingDays().setUserVariables(td);
        }
        if (user.length > 0) {
//            if (user != null) {
            spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(user);
        }
    }

    private void setDefaults() {

        if (spec.getRegArimaSpecification().equals(RegArimaSpecification.RGDISABLED)) {

            //X11
            spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
            spec.getX11Specification().setSeasonal(true);
            spec.getX11Specification().setSeasonalFilter(SeasonalFilterOption.Msr);

            spec.getX11Specification().setSigma(1.5, 2.5);
            spec.getX11Specification().setForecastHorizon(0);
            spec.getRegArimaSpecification().getBasic().setPreprocessing(false);

        } else {
//X13

//            x11
            spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
            spec.getX11Specification().setSeasonal(true);
            spec.getX11Specification().setSeasonalFilter(SeasonalFilterOption.Msr);
            spec.getX11Specification().setSigma(1.5, 2.5);
            spec.getX11Specification().setForecastHorizon(-1);
            spec.getRegArimaSpecification().getBasic().setPreprocessing(true);

            //arima: default is airline model
            spec.getRegArimaSpecification().getArima().setP(0);
            spec.getRegArimaSpecification().getArima().setD(1);
            spec.getRegArimaSpecification().getArima().setQ(1);
            spec.getRegArimaSpecification().getArima().setBP(0);
            spec.getRegArimaSpecification().getArima().setBD(1);
            spec.getRegArimaSpecification().getArima().setBQ(1);

            //estimate
            spec.getRegArimaSpecification().getEstimate().setTol(1.0e-5);

            //no outliers
            spec.getRegArimaSpecification().getOutliers().reset();

            //transform
            spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
            spec.getRegArimaSpecification().getTransform().setAICDiff(-2.0);
            spec.getRegArimaSpecification().getTransform().setAdjust(LengthOfPeriodType.None);
        }
    }

    private void read_acceptdefault(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toUpperCase();

        if (!automdlDefault) {
            setAUTOMDLDefaults();
        }
        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setAcceptDefault(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setAcceptDefault(false);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument ACCEPTDEFAULT");
                break;
        }

    }

    private void read_adjust(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        switch (content.toUpperCase()) {
            case "LOM":
            case "LOQ":
                spec.getRegArimaSpecification().getTransform().setAdjust(LengthOfPeriodType.LengthOfPeriod);
                break;
            case "LPYEAR":
                spec.getRegArimaSpecification().getTransform().setAdjust(LengthOfPeriodType.LeapYear);
                break;
            case "NONE":
                spec.getRegArimaSpecification().getTransform().setAdjust(LengthOfPeriodType.None);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument ADJUST");
                spec.getRegArimaSpecification().getTransform().setAdjust(LengthOfPeriodType.None);
                break;
        }
    }

    private void read_aicdiff(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        try {
            double value = Double.parseDouble(content);

            if (!automdlDefault) {
                setAUTOMDLDefaults();
            }
            spec.getRegArimaSpecification().getTransform().setAICDiff(value);
        } catch (NumberFormatException e) {
            messages.add(partName + ": No support for value " + content + " in argument AICDIFF");
        }
    }

    private void read_ar(SpecificationPart partName, String content) {

        /*  assigned String
         *   case 1: (x, ..., y);
         *   case 2: (, x);
         *   case 3: (x,);
         *
         *   x, y with or without 'f'
         */
//        1. Delete all unnecassary letters
        content = content.replaceAll(";", "");
        String s = content.replaceAll("\\(", "").replaceAll("\\)", "").trim();
        s = s.toLowerCase();

//        2. Get coefficients vector with zeros and default values (calculated in read_model)
        Parameter[] phi = spec.getRegArimaSpecification().getArima().getPhi();
        Parameter[] bPhi = spec.getRegArimaSpecification().getArima().getBPhi();

//        3. Case with blank value
        StringBuilder sb = new StringBuilder();
        if (s.startsWith(",")) {
            sb.append(";").append(s);
            s = sb.toString();
            sb = new StringBuilder();
        }
        if (s.endsWith(",")) {
            sb.append(s).append(";");
            s = sb.toString();
        }
        s = s.replaceAll(",\\s*,", ",;,");

        //Parameters for loops
        String[] tmp;
        tmp = s.split(",");
        int counter = 0;
        double value;

//        4. Check there are enough assigned values
        int assigned = tmp.length;
        int required = 0;
        if (phi != null) {
            for (Parameter p : phi) {
                if (p.getValue() == -0.1) {
                    required++;
                }
            }
        }
        if (bPhi != null) {
            for (Parameter p : bPhi) {
                if (p.getValue() == -0.1) {
                    required++;
                }
            }
        }
        if (assigned == required) {

//        5. Set assigned parameters on the correct position in the vector
            if (phi != null) {
                for (Parameter p : phi) {
                    if (p.getValue() == -0.1) {
                        if (!tmp[counter].equals(";")) {
                            if (tmp[counter].contains("f")) {
                                p.setType(ParameterType.Fixed);
                                tmp[counter] = tmp[counter].substring(0, tmp[counter].indexOf("f"));
                            } else {
                                p.setType(ParameterType.Initial);
                            }
                            value = Double.parseDouble(tmp[counter]) * -1.0;
                        } else {
                            value = -0.1;
                            p.setType(ParameterType.Undefined);
                        }
                        p.setValue(value);
                        counter++;
                    }
                }
            }
            if (bPhi != null) {
                for (Parameter p : bPhi) {
                    if (p.getValue() == -0.1) {
                        if (!tmp[counter].equals(";")) {
                            if (tmp[counter].contains("f")) {
                                p.setType(ParameterType.Fixed);
                                tmp[counter] = tmp[counter].substring(0, tmp[counter].indexOf("f"));
                            } else {
                                p.setType(ParameterType.Initial);
                            }
                            value = Double.parseDouble(tmp[counter]) * -1.0;
                        } else {
                            value = -0.1;
                            p.setType(ParameterType.Undefined);
                        }
                        p.setValue(value);
                        counter++;
                    }
                }
            }
        } else {
            messages.add(partName + ": The number of values for argument AR is not conform to the number of values in argument MODEL.");
        }
    }

    private void read_armalimit(SpecificationPart partName, String content) {

        if (!automdlDefault) {
            setAUTOMDLDefaults();
        }

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setArmaSignificance(value);
        } catch (NumberFormatException ex) {
            messages.add(partName + ": No support for value " + content + " in argument ARMALIMIT");
        } catch (X13Exception e) {
            messages.add(partName + ": " + e.toString());
        }
    }

    private void read_balanced(SpecificationPart partName, String content) {

        if (!automdlDefault) {
            setAUTOMDLDefaults();
        }
        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setBalanced(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setBalanced(false);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument BALANCED");
                break;
        }
    }

    private void read_calendarsigma(SpecificationPart partName, String content) {

        X11Specification x11 = spec.getX11Specification();
        content = content.replaceAll(";", "").trim().toUpperCase();
        switch (content) {
            case "ALL":
                x11.setCalendarSigma(CalendarSigma.All);
                break;
            case "SELECT":
                x11.setCalendarSigma(CalendarSigma.Select);
                calendarsigmaSelect = true;
                break;
            case "SIGNIF":
                x11.setCalendarSigma(CalendarSigma.Signif);
                break;
            default:
                messages.add(partName + ": No support for value " + content + "in argument CALENDARSIGMA. Calendarsigma will be set to None.");
            case "NONE":
                x11.setCalendarSigma(CalendarSigma.None);
                break;
        }
    }

    private void read_centeruser(SpecificationPart partName, String content) {

//        content = content.replaceAll(";", "").trim().toUpperCase();
//        if (content.equals("SEASONAL")) {
        warnings.add(partName + ": No suppport for argument CENTERUSER. Please use a centred regression variable.");
//        }
    }

    private void read_checkmu(SpecificationPart partName, String content) {

        if (!automdlDefault) {
            setAUTOMDLDefaults();
        }
        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setCheckMu(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setCheckMu(false);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument CHECKMU");
                break;
        }
    }

    private void read_critical(SpecificationPart partName, String content) {

        if (!outlierDefaults) {
            setOUTLIERDefaults();
        }

        content = content.replaceAll(";", "").trim();

        String[] s = content.replaceAll("\\(", "").replaceAll("\\)", "").split(",");

        SingleOutlierSpec[] o = spec.getRegArimaSpecification().getOutliers().getTypes();
        if (o != null) {
            if (s.length == o.length) {
                for (int i = 0; i < s.length; i++) {
                    try {
                        o[i].setCriticalValue(Double.parseDouble(s[i]));
                    } catch (NumberFormatException e) {
                        messages.add(partName + ": No support for value " + content + " in argument CRITICAL");
                    }
                }
            } else {
                try {
                    spec.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(Double.parseDouble(s[0]));
                } catch (NumberFormatException e) {
                    messages.add(partName + ": No support for value " + content + " in argument CRITICAL");
                }
            }
//        errors.add(partName + ": No support for more than one critical value , critical value is set to " + s[0]);
        } else {
            messages.add(partName + ": argument TYPES have to be defined before argument CRITICAL");
        }
    }

    private void read_data(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();

        switch (partName) {
            case SERIES:
                dataLoader.load(content);
                break;
            case REGRESSION:
                regressionSpec = true;
                regressionLoader.load(content);
                if (regressionTyp == null) {
                    regressionTyp = new String[]{"TD"};
                }
                break;
            default: //Fehler
                messages.add(partName + ": No support for argument DATA in " + partName.name().toUpperCase());
                break;
        }
    }

    private void read_format(SpecificationPart partName, String content) {
        messages.add(partName + ": Argument FORMAT is ignored. Please have a look in SpecParser UserGuide");
    }

    private void read_file(SpecificationPart partName, String content) {
        /*
         Load data from a file
         possible endings are .dat or .ser or .rgr
         */

        //delete this letters: ; and ' and "
        content = content.replaceAll("[;'\"]", "").trim();
        File file;
        if (!content.contains("\\")) {
            file = new File((path + content));
        } else {
            file = new File(content);
        }

        switch (partName) {
            case SERIES:
                dataLoader.load(file);
                if (!dataLoader.getMessages().isEmpty()) {
                    errors.add(partName + ": " + dataLoader.getMessages());
                }
                break;
            case REGRESSION:
                regressionSpec = true;
                regressionLoader.load(file);
                if (!regressionLoader.getMessages().isEmpty()) {
                    errors.add(partName + ": " + regressionLoader.getMessages());
                }
                break;
            default:
                messages.add(partName + ": No support for argument FILE in " + partName.name().toUpperCase());
                break;
        }
    }

    private void read_final(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toLowerCase();

        if (content.equals("user")) {
            finalIsUser = true;
        } else {
            warnings.add(partName + ": No support for value " + content.toUpperCase() + " in argument FINAL.");
        }
    }

    private void read_function(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        switch (content.toUpperCase()) {
            case "LOG":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
                spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                transformLog = true;
                break;
            case "NONE":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
                spec.getX11Specification().setMode(DecompositionMode.Additive);
                transformNone = true;
                break;
            case "AUTO":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Auto);
                spec.getX11Specification().setMode(DecompositionMode.Undefined);
                transformAuto = true;
                break;
            default:
                warnings.add(partName + ": No support for value " + content + " in argument FUNCTION. Value is changed to NONE");
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
                break;
        }
    }

    private void read_hrinitial(SpecificationPart partName, String content) {

        if (!automdlDefault) {
            setAUTOMDLDefaults();
        }
        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setHannanRissanen(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setHannanRissanen(false);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument for HRINITIAL");
                break;
        }
    }

    private void read_ljungboxlimit(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);

            if (!automdlDefault) {
                setAUTOMDLDefaults();
            }
            spec.getRegArimaSpecification().getAutoModel().setLjungBoxLimit(value);
        } catch (NumberFormatException ex) {
            messages.add(partName + ": No support for value " + content + " in argument LJUNGBOXLIMIT");
        }
    }

    private void read_lsrun(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        try {
            int value = Integer.parseInt(content);

            if (!outlierDefaults) {
                setOUTLIERDefaults();
            }
            spec.getRegArimaSpecification().getOutliers().setLSRun(value);
        } catch (NumberFormatException e) {
            messages.add(partName + ": No support for value " + content + " in argument LSRUN");
        }
    }

    private void read_ma(SpecificationPart partName, String content) {
        /*  assigned String
         *   case 1: (x, ..., y);
         *   case 2: (, x);
         *   case 3: (x,);
         *
         *   x, y with or without 'f'
         */

//        1. Delete all unnecassary letters
        content = content.replaceAll(";", "");
        String s = content.replaceAll("\\(", "").replaceAll("\\)", "").trim();
        s = s.toUpperCase();

//        2. Get coefficients vector with zeros and default values (calculated in read_model)
        Parameter[] theta = spec.getRegArimaSpecification().getArima().getTheta();
        Parameter[] bTheta = spec.getRegArimaSpecification().getArima().getBTheta();

//        3. Case with blank value
        StringBuilder sb = new StringBuilder();
        if (s.startsWith(",")) {
            sb.append(";").append(s);
            s = sb.toString();
            sb = new StringBuilder();
        }
        if (s.endsWith(",")) {
            sb.append(s).append(";");
            s = sb.toString();
        }
        s = s.replaceAll(",\\s*,", ",;,");

        //Parameters for loops
        String[] tmp;
        tmp = s.split(",");
        int counter = 0;
        double value;

//         4. Check there are enough assigned values
        int assigned = tmp.length;
        int required = 0;
        if (theta != null) {
            for (Parameter p : theta) {
                if (p.getValue() == -0.1) {
                    required++;
                }
            }
        }
        if (bTheta != null) {
            for (Parameter p : bTheta) {
                if (p.getValue() == -0.1) {
                    required++;
                }
            }
        }
        if (assigned == required) {

//        5. Set assigned parameters on the correct position in the vector
            if (theta != null) {
                for (Parameter q : theta) {
                    if (q.getValue() == -0.1) {
                        if (!tmp[counter].equals(";")) {
                            if (tmp[counter].contains("F")) {
                                q.setType(ParameterType.Fixed);
                                tmp[counter] = tmp[counter].substring(0, tmp[counter].indexOf("F"));
                            } else {
                                q.setType(ParameterType.Initial);
                            }
                            value = Double.parseDouble(tmp[counter]) * -1.0;
                        } else {
                            value = -0.1;
                            q.setType(ParameterType.Undefined);
                        }
                        q.setValue(value);
                        counter++;
                    }
                }
            }
            if (bTheta != null) {
                for (Parameter q : bTheta) {
                    if (q.getValue() == -0.1) {
                        if (!tmp[counter].equals(";")) {
                            if (tmp[counter].contains("F")) {
                                q.setType(ParameterType.Fixed);
                                tmp[counter] = tmp[counter].substring(0, tmp[counter].indexOf("F"));
                            } else {
                                q.setType(ParameterType.Initial);
                            }
                            value = Double.parseDouble(tmp[counter]) * -1.0;
                        } else {
                            value = -0.1;
                            q.setType(ParameterType.Undefined);
                        }
                        q.setValue(value);
                        counter++;
                    }
                }
            }
        } else {
            messages.add(partName + ": The number of values for argument MA is not conform to the number of values in argument MODEL.");
        }
    }

    private void read_maxlead(SpecificationPart partName, String content) {

        content = content.trim();

        try {
            int forecast = Integer.parseInt(content);
            spec.getX11Specification().setForecastHorizon(forecast);
        } catch (NumberFormatException e) {
            messages.add(partName + ": No support for value " + content + " in argument MAXLEAD");
        }

    }

    private void read_method(SpecificationPart partName, String content) {

        if (!outlierDefaults) {
            setOUTLIERDefaults();
        }

        content = content.replaceAll(";", "").trim();
        switch (content.toUpperCase()) {
            case "ADDONE":
                spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddOne);
                break;
            case "ADDALL":
                warnings.add(partName + ": No support for value " + content.toUpperCase() + " in argument METHOD");
//                spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddAll);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument METHOD");
                break;
        }
    }

    private void read_mixed(SpecificationPart partName, String content) {

        if (!automdlDefault) {
            setAUTOMDLDefaults();
        }
        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setMixed(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setMixed(false);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument MIXED");
                break;
        }
    }

    private void read_mode(SpecificationPart partName, String content) {

        /*
         *   Select the correct DecompositionMode for JD+
         */
        if (!transformAuto) {
            content = content.replaceAll(";", "").trim();
            content = content.replaceAll(" ", "");
            switch (content.toLowerCase()) {
                case "add":
                    if (!transformLog) {
                        spec.getX11Specification().setMode(DecompositionMode.Additive);
                    } else {
                        warnings.add(SpecificationPart.TRANSFORM + ": Decompostion mode = add is not possible for transform function = log. Value changed to mult");
                        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                    }
                    break;
                case "mult":
                    if (!transformNone) {
                        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                    } else {
                        warnings.add(partName + ": For transform function = none is only mode=add in JD+ possible, Value is changed to add");
                        spec.getX11Specification().setMode(DecompositionMode.Additive);
                    }
                    break;
                case "logadd":
                    if (!transformNone) {
                        spec.getX11Specification().setMode(DecompositionMode.LogAdditive);
                    } else {
                        warnings.add(partName + ": For transform function = none is only mode=add in JD+ possible, Value is changed to mult");
                        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                    }
                    break;
                case "pseudoadd":
                    if (!transformNone) {
                        warnings.add(partName + ": No support for value " + content.toUpperCase() + " in argument MODE");
                    } else {
                        warnings.add(partName + ": For transform function = none is only mode=add in JD+ possible. Value changed in add");
                        spec.getX11Specification().setMode(DecompositionMode.Additive);
                    }
                    break;
                default:
                    messages.add(partName + ": No support for value " + content.toUpperCase() + " in argument MODE");
                    break;
            }
        } else {
            messages.add(SpecificationPart.TRANSFORM + ": For transform function = auto it is not possible to set a decomposition mode");
            spec.getX11Specification().setMode(DecompositionMode.Undefined);
        }
    }

    private void read_model(SpecificationPart partName, String content) {

        /*  assigned String
         *   case 1: "(p d q)(P D Q)delta;"
         *   case 2: "(p d q);"
         *   case 3: "(P D Q)delta;"
         *
         *  P,Q are 0 or 1
         *  p,q are integers or [x ... z]
         *  delta is an integer (12 for months, 4 for quater year, ...)
         */
        spec.getRegArimaSpecification().getAutoModel().setEnabled(false);
        //important if one Arima part is not specified
        spec.getRegArimaSpecification().getArima().setP(0);
        spec.getRegArimaSpecification().getArima().setD(0);
        spec.getRegArimaSpecification().getArima().setQ(0);
        spec.getRegArimaSpecification().getArima().setBP(0);
        spec.getRegArimaSpecification().getArima().setBD(0);
        spec.getRegArimaSpecification().getArima().setBQ(0);

        //1. Split on ")(" with or without spaces
        content = content.replaceAll(";", "").trim();
        //invalid format for ARIMA model: (...)(...)3(...)
        String[] match = content.split("\\d+\\(");
        if (match.length > 1) {
            warnings.add(partName + ": No support for value " + content + " in argument MODEL");
        } else {

            String[] sep = content.split("\\s*\\)\\s*\\(\\s*");

            boolean sarima;
            String p, d, q;
            String[] p_array, q_array;
            Parameter[] p_para, q_para;
            int start, end;
            String s;

//        2. After split there is one or two strings to analyze
            for (int i = 0; i < sep.length; i++) {
                s = sep[i].trim();
                p = null;
                q = null;
                sarima = false;

//            3. Check it is the part of saisonal (SARIMA)
                match = s.split("\\)");
                if ((i + 1) == 2) {
                    sarima = true;
                }
                if (match.length == 2) {
                    sarima = true;
                    if (dataLoader.getPeriod() != null) {
                        if (!match[1].trim().equals(dataLoader.getPeriod().intValue() + "")) {
                            messages.add(partName + ": Period are not conform to the period of the data. Model period is changed into the period of data.");
                        } //else all right
                    } else {
                        read_period(partName, match[1]);
                    }
                }

                s = match[0].trim();
                s = s.replaceAll("\\(", "").trim();

//            4. Look for number of arguments for p/P and q/Q
//            a) Look for arguments inside [...], then p,q!=null
                if (s.startsWith("[")) {
                    end = s.indexOf("]");
                    p = s.substring(1, end).trim();
                    p = p.replaceAll("\\s+", ",");
                    s = s.substring(end + 1).trim();
                }
                if (s.endsWith("]")) {
                    start = s.indexOf("[");
                    q = s.substring(start).trim();
                    q = q.replaceAll("\\s+", ",");
                    s = s.substring(0, start).trim();
                }

//           b) Between two arguments has to be ","
                if (s.contains(",")) {
                    s = s.replaceAll("\\s+", "");
                } else {
                    s = s.replaceAll("\\s+", ",");
                }

//            c) Check cases of p,q null or not 
                if (p == null) {
//                i) extract p from string
                    end = s.indexOf(",");
                    p = s.substring(0, end);
                    s = s.substring(end + 1);

//                ii) set default parameter
                    p_para = new Parameter[Integer.parseInt(p)];
                    for (int j = 0; j < p_para.length; j++) {
                        p_para[j] = new Parameter(-0.1, ParameterType.Undefined);
                    }

                } else {
//                i) extract arguments in [...]
                    p = p.replaceAll("\\[", "").replaceAll("\\]", "");
                    p_array = p.split(",");

//              ii) set parameter, when not use 0.0 else default
                    p = p_array[p_array.length - 1];
                    p_para = new Parameter[Integer.parseInt(p)];
                    for (int j = 0; j < p_para.length; j++) {
                        p_para[j] = new Parameter(0.0, ParameterType.Fixed);
                    }
                    for (String a : p_array) {
                        p_para[Integer.parseInt(a) - 1] = new Parameter(-0.1, ParameterType.Undefined);
                    }
                }
                if (q == null) {
//                i) extract q
                    if (s.startsWith(",")) {
                        start = s.indexOf(",");
                        s = s.substring(start).trim();
                    }
                    start = s.indexOf(",");
                    q = s.substring(start + 1);
                    s = s.substring(0, start);

//                ii) set default parameter
                    q_para = new Parameter[Integer.parseInt(q)];
                    for (int j = 0; j < q_para.length; j++) {
                        q_para[j] = new Parameter(-0.1, ParameterType.Undefined);
                    }

                } else {
//                i) extract arguments in [...]
                    q = q.replaceAll("\\[", "").replaceAll("\\]", "");
                    q_array = q.split(",");
                    q = q_array[q_array.length - 1];

//                ii) set parameter
                    q_para = new Parameter[Integer.parseInt(q)];
                    for (int j = 0; j < q_para.length; j++) {
                        q_para[j] = new Parameter(0.0, ParameterType.Fixed);
                    }
                    for (String a : q_array) {
                        q_para[Integer.parseInt(a) - 1] = new Parameter(-0.1, ParameterType.Undefined);
                    }
                }

//            c) extract d
                d = s.replaceAll(",", "").replaceAll(" ", "");

//            d) set parameters in X13Specification
                try {
                    if (sarima == false) {
//                    i) ARIMA part
                        spec.getRegArimaSpecification().getArima().setP(Integer.parseInt(p));
                        spec.getRegArimaSpecification().getArima().setD(Integer.parseInt(d));
                        spec.getRegArimaSpecification().getArima().setQ(Integer.parseInt(q));
//
                        spec.getRegArimaSpecification().getArima().setPhi(p_para);
                        spec.getRegArimaSpecification().getArima().setTheta(q_para);
                    } else {
//                    ii) SARIMA part
                        spec.getRegArimaSpecification().getArima().setBP(Integer.parseInt(p));
                        spec.getRegArimaSpecification().getArima().setBD(Integer.parseInt(d));
                        spec.getRegArimaSpecification().getArima().setBQ(Integer.parseInt(q));
//
                        spec.getRegArimaSpecification().getArima().setBPhi(p_para);
                        spec.getRegArimaSpecification().getArima().setBTheta(q_para);
                    }
                } catch (NumberFormatException e) {
                    messages.add(partName + ": No support for value " + content + " in argument MODEL");
                } catch (X13Exception e) {
                    warnings.add(partName + ": No support for parameters in argument MODEL");
                }
            }
        }
    }

    private void read_modelspan(SpecificationPart partName, String content) {

        read_span(SpecificationPart.ESTIMATE, content);

    }

    private void read_name(SpecificationPart partName, String content) {

//        content = content.replaceAll(";", "").trim();
//        switch (partName) {
//            case SERIES:
//                tsName = content.replaceAll("'", "");
////                if (mtaName == null) {
////                    mtaName = tsName;
////                }
//                break;
//            case REGRESSION:
//            default:
//                messages.add(partName + ": No support for argument NAME");
//                break;
//        }
    }

    private void read_period(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        try {
            int p = Integer.parseInt(content);
            switch (p) {
                case 12:
                    dataLoader.setPeriod(TsFrequency.Monthly);
                    regressionLoader.setPeriod(TsFrequency.Monthly);
                    break;
                case 4:
                    //change period
                    dataLoader.setPeriod(TsFrequency.Quarterly);
                    regressionLoader.setPeriod(TsFrequency.Quarterly);

                    //change start date
                    dataLoader.setStart(DateConverter.changeToQuarter(dataLoader.getStart()));
                    regressionLoader.setStart(DateConverter.changeToQuarter(regressionLoader.getStart()));

                    //change span
                    TsPeriodSelector sel;
                    if (spec.getRegArimaSpecification().getBasic() != null && spec.getRegArimaSpecification().getBasic().getSpan() != null && !spec.getRegArimaSpecification().getBasic().getSpan().getType().equals(PeriodSelectorType.All)) {
                        sel = spec.getRegArimaSpecification().getBasic().getSpan();
                        if (sel.getD0() != null) {
                            sel.setD0(DateConverter.changeToQuarter(sel.getD0()));
//                            sel.setD0(toQuarterDay(sel.getD0(), "SERIES", "span start date"));
                        }
                        if (sel.getD1() != null) {
                            sel.setD1(DateConverter.changeToQuarter(sel.getD1()));
//                            sel.setD1(toQuarterDay(sel.getD1(), "SERIES", "span ending date"));
                        }
                        spec.getRegArimaSpecification().getBasic().setSpan(sel);
                    }
                    //change modelspan
                    if (spec.getRegArimaSpecification().getEstimate() != null && spec.getRegArimaSpecification().getEstimate().getSpan() != null && !spec.getRegArimaSpecification().getEstimate().getSpan().getType().equals(PeriodSelectorType.All)) {
                        sel = spec.getRegArimaSpecification().getEstimate().getSpan();
                        if (sel.getD0() != null) {
                            sel.setD0(DateConverter.changeToQuarter(sel.getD0()));
//                            sel.setD0(toQuarterDay(sel.getD0(), "SERIES", "modelspan start date"));
                        }
                        if (sel.getD1() != null) {
                            sel.setD1(DateConverter.changeToQuarter(sel.getD1()));
//                            sel.setD1(toQuarterDay(sel.getD1(), "SERIES", "model span ending date"));
                        }
                        spec.getRegArimaSpecification().getEstimate().setSpan(sel);
                    }

                    break;
                default:
                    warnings.add(partName + ": No support for value " + content + " in argument PERIOD. Set to default 12");
                    break;
            }
        } catch (NumberFormatException e) {
            messages.add(partName + ": No support for value " + content + " in argument PERIOD.");
        }
    }

    private void read_power(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            if (value == 0.0) {

                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
            }
        } catch (NumberFormatException e) {
            messages.add(partName + ": No support for value " + content + " in argument POWER");
        }
    }

    private void read_reducecv(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);

            if (!automdlDefault) {
                setAUTOMDLDefaults();
            }
            spec.getRegArimaSpecification().getAutoModel().setPercentReductionCV(value);
        } catch (NumberFormatException ex) {
            messages.add(partName + ": No support for value " + content + " in argument REDUCECV");
        } catch (X13Exception e) {
            messages.add(partName + ": " + e.getMessage());
        }
    }

    private void read_seasonalma(SpecificationPart partName, String content) {

        /*
         *   Select the correct seasonal filters for JD+
         */
        spec.getX11Specification().setSeasonal(true);

        //Delete the brackets
        content = content.replaceAll(";", "").trim();
        content = content.replaceAll("\\(", "");
        content = content.replaceAll("\\)", "");

        String[] filter = content.split("\\s");

        ArrayList<SeasonalFilterOption> tmp = new ArrayList();
        for (String item : filter) {
            if (!item.isEmpty()) {
                item = item.toLowerCase();
                switch (item) {
                    case "s3x1":
                        tmp.add(SeasonalFilterOption.S3X1);
                        break;
                    case "s3x3":
                        tmp.add(SeasonalFilterOption.S3X3);
                        break;
                    case "s3x5":
                        tmp.add(SeasonalFilterOption.S3X5);
                        break;
                    case "s3x9":
                        tmp.add(SeasonalFilterOption.S3X9);
                        break;
                    case "s3x15":
                        tmp.add(SeasonalFilterOption.S3X15);
                        break;
                    case "stable":
                        tmp.add(SeasonalFilterOption.Stable);
                        break;
                    case "x11default":
                        tmp.add(SeasonalFilterOption.X11Default);
                        break;
                    case "msr":
                        tmp.add(SeasonalFilterOption.Msr);
                        break;
                    default:
                        messages.add(partName + ": No Support for value " + content + " in argument SEASONALMA");
                        tmp.add(null);
                        break;
                }
            }
        }
        if (tmp.size() == 1 || tmp.size() == dataLoader.getPeriod().intValue()) {
            spec.getX11Specification().setSeasonalFilters((SeasonalFilterOption[]) tmp.toArray(new SeasonalFilterOption[tmp.size()]));
        } else {
            messages.add(partName + ": Period is not conform to the period of the data. Seasonal filter is set to " + tmp.get(0));
            spec.getX11Specification().setSeasonalFilter(tmp.get(0));
        }
    }

    private void read_sigmalim(SpecificationPart partName, String content) {

        /* 
         *    Selects the values for lower and upper sigma
         */
        content = content.replaceAll(";", "").trim();

        String cont = content.replaceAll("\\s+", " ");
        cont = cont.replaceAll("\\s*\\(\\s*", "");
        cont = cont.replaceAll("\\s*\\)\\s*", "");

//        if (cont.startsWith("\\s")) {
//            cont = cont.substring(1);
//        }
//        if (cont.endsWith("\\s")) {
//            cont = cont.substring(0, cont.length() - 2);
//        }
        String[] tmp;
        if (cont.contains(",")) {
            tmp = cont.split(",");
        } else if (cont.contains("\t")) {
            tmp = cont.split("\t");
        } else {
            tmp = cont.split(" ");
        }

        try {
            if (tmp.length == 1) {
                spec.getX11Specification().setSigma(Double.parseDouble(tmp[0]), 2.5);
            } else {
                if (tmp[0].isEmpty() || tmp[0].equals(" ")) {
                    spec.getX11Specification().setSigma(1.5, Double.parseDouble(tmp[1]));
                } else if (tmp[1].isEmpty() || tmp[1].equals(" ")) {
                    spec.getX11Specification().setSigma(Double.parseDouble(tmp[0]), 2.5);
                } else {
                    spec.getX11Specification().setSigma(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
                }
            }
        } catch (NumberFormatException e) {
            messages.add(partName + ": No support for value " + content + " in argument SIGMALIM");
        }
    }

    private void read_sigmavec(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        String s = content.replaceAll("\\(", " ");
        s = s.replaceAll("\\)", " ").toLowerCase();

        if (calendarsigmaSelect) {
            SigmavecOption[] vec = new SigmavecOption[dataLoader.getPeriod().intValue()];

            //Default setting
            for (int i = 0; i < vec.length; i++) {
                vec[i] = SigmavecOption.Group2;
            }

            boolean setGroup1 = false;

            if (s.contains("jan") || s.contains("q1")) {
                vec[0] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("feb") || s.contains("q2")) {
                vec[1] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("mar") || s.contains("q3")) {
                vec[2] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("apr") || s.contains("q4")) {
                vec[3] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("may")) {
                vec[4] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("jun")) {
                vec[5] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("jul")) {
                vec[6] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("aug")) {
                vec[7] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("sep")) {
                vec[8] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("oct")) {
                vec[9] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("nov")) {
                vec[10] = SigmavecOption.Group1;
                setGroup1 = true;
            }
            if (s.contains("dec")) {
                vec[11] = SigmavecOption.Group1;
                setGroup1 = true;
            }

            if (setGroup1) {
                spec.getX11Specification().setSigmavec(vec);
            } else {
                messages.add(partName + ": For argument SIGMACVEC group 1 contains no element.");
            }

        } else {
            messages.add(partName + ": Argument SIGMAVEC needs argument CALENDARSIGMA=select");
        }
    }

    private void read_span(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        String s = content.replaceAll("\\(", " ");
        s = s.replaceAll("\\)", " ");
        String[] split = s.split(",");

        if (split.length == 2) {

            TsPeriodSelector p = new TsPeriodSelector();
            if (split[0].replaceAll("\\s+", "").isEmpty()) {
                if (split[1].replaceAll("\\s+", "").isEmpty()) {
                    // beides leer, sowas in der art ( , )
                    messages.add(partName + ": No support for value " + content + " in argument SPAN");
                } else {
                    //to
                    p.setType(PeriodSelectorType.To);
                    p.to(DateConverter.toJD(split[1], dataLoader.getPeriod()));
//                    p.to(calcDay(partName, split[1]));
                }
            } else {
                if (split[1].replaceAll("\\s+", "").isEmpty()) {
                    //from
                    p.setType(PeriodSelectorType.From);
                    p.from(DateConverter.toJD(split[0], dataLoader.getPeriod()));
//                    p.from(calcDay(partName, split[0]));

                } else {
                    //between
                    p.setType(PeriodSelectorType.Between);
                    p.between(DateConverter.toJD(split[0], dataLoader.getPeriod()), DateConverter.toJD(split[1], dataLoader.getPeriod()));
//                    p.between(calcDay(partName, split[0]), calcDay(partName, split[1]));
                }
            }

            switch (partName.toString().toUpperCase()) {
                case "OUTLIERS":
                    if (!outlierDefaults) {
                        setOUTLIERDefaults();
                    }
                    spec.getRegArimaSpecification().getOutliers().setSpan(p);
                    break;
                case "ESTIMATE":
                    spec.getRegArimaSpecification().getEstimate().setSpan(p);
                    break;
                case "SERIES":
                    spec.getRegArimaSpecification().getBasic().setSpan(p);
                    break;
                default:
                    messages.add(partName + ": No support for argument SPAN");
                    break;
            }
        } else {
            //Fehler
            //drueber nachdenken
            messages.add((String) (partName == SpecificationPart.ESTIMATE ? SpecificationPart.SERIES : partName + ": No support for value " + content + " in argument SPAN"));
        }
    }

    private void read_start(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        switch (partName) {
            case SERIES:
                dataLoader.setStart(DateConverter.toJD(content, dataLoader.getPeriod()));
                break;
            case REGRESSION:
                content = content.replaceAll("\\(", "").replaceAll("\\)", "").trim();
                regressionLoader.setStart(DateConverter.toJD(content, regressionLoader.getPeriod()));
                break;
            default:
                messages.add(partName + ": No support for argument START");
                break;
        }
    }

    private void read_tcrate(SpecificationPart partName, String content) {

        String s = content.replaceAll(";", "").trim();
        try {
            double value = Double.parseDouble(s);

            spec.getRegArimaSpecification().getOutliers().setMonthlyTCRate(value);
        } catch (NumberFormatException e) {
            messages.add(partName + ": No support for value " + content + " in argument TCRATE");
        }
    }

    private void read_title(SpecificationPart partName, String content) {
//        if (partName == SpecificationPart.SERIES) {
//            tsName = content.replaceAll(";", "").trim();
//        }
    }

    private void read_tol(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getEstimate().setTol(value);
        } catch (NumberFormatException ex) {
            messages.add(partName + ": No support for value " + content + " in argument TOL");
        } catch (X13Exception e) {
            messages.add(partName + ": " + e.getMessage());
        }
    }

    private void read_trendma(SpecificationPart partName, String content) {

        /*
         *   Set the correct length of Hendersonfilter for JD+
         */
        content = content.replaceAll(";", "").trim();
        content = content.replaceAll(" ", "");
        try {
            int t = Integer.parseInt(content);
            spec.getX11Specification().setHendersonFilterLength(t);
        } catch (NumberFormatException ex) {
            messages.add(partName + ": No support for value " + content + " in argument TRENDMA");
        }
    }

    private void read_types(SpecificationPart partName, String content) {

        if (!outlierDefaults) {
            setOUTLIERDefaults();
        }

        spec.getRegArimaSpecification().getOutliers().clearTypes();

        String s = content.replaceAll(";", "").trim();

        s = s.replaceAll("\\(", "").replaceAll("\\)", "").trim();
        String[] split;
        if (s.contains(",")) {
            split = s.split(",");
        } else {
            split = s.split("\\s+");
        }
//            ArrayList<SingleOutlierSpec> value = new ArrayList();
        for (String t : split) {
            t = t.trim().toUpperCase();
            switch (t) {
                case "ALL":
                    spec.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
                    spec.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
                    spec.getRegArimaSpecification().getOutliers().add(OutlierType.TC);
//                        value.add(new SingleOutlierSpec(OutlierType.AO));
//                        value.add(new SingleOutlierSpec(OutlierType.LS));
//                        value.add(new SingleOutlierSpec(OutlierType.TC));
                    break;
                case "NONE":
                    spec.getRegArimaSpecification().getOutliers().clearTypes();
                    break;
                case "AO":
                    spec.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
//                        value.add(new SingleOutlierSpec(OutlierType.AO));
                    break;
                case "LS":
                    spec.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
//                        value.add(new SingleOutlierSpec(OutlierType.LS));
                    break;
                case "TC":
                    spec.getRegArimaSpecification().getOutliers().add(OutlierType.TC);
//                        value.add(new SingleOutlierSpec(OutlierType.TC));
                    break;
                default:
                    messages.add(partName + ": No support for value " + t + " in argument TYPES");
                    break;
            }
        }
//            spec.getRegArimaSpecification().getOutliers().setTypes((SingleOutlierSpec[]) value.toArray());

    }

    private void read_urfinal(SpecificationPart partName, String content) {

        if (!automdlDefault) {
            setAUTOMDLDefaults();
        }
        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setUnitRootLimit(value);
        } catch (NumberFormatException ex) {
            messages.add(partName + ": No support for value " + content + " in argument URFINAL");
        } catch (X13Exception e) {
            messages.add(partName + ": " + e.getMessage());
        }
    }

    private void read_user(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();
        String[] regressors = content.split("\\s+");
        regressionLoader.setRegressorName(regressors);
        if (regressionLoader.isStartDefault()) {
            regressionLoader.setStart(dataLoader.getStart());
        }
        regressionLoader.setPeriod(dataLoader.getPeriod());

        if (regressionTyp == null) {
            regressionTyp = new String[regressors.length];
            for (int i = 0; i < regressionTyp.length; i++) {
                //default usertype
                regressionTyp[i] = "USER";
            }
        }
        regressionSpec = true;
    }

    private void read_usertype(SpecificationPart partName, String content) {
        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();
        String[] regressors = content.split("\\s+");

        for (int i = 0; i < regressors.length; i++) {
            switch (regressors[i].toUpperCase().trim()) {
                case "TD":
                    regressionTyp[i] = "TD";
                    break;
                case "USER":
                    regressionTyp[i] = "USER";
                    break;
                default:
                    warnings.add(partName + ": No support for value " + regressors[i].toUpperCase() + " in argument USERTYPE. Values changed to value USER");
                    regressionTyp[i] = "USER";
                    break;
            }
        }
        regressionSpec = true;
    }

    private void read_variables(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();

        if (content.contains("sincos")) {
            //hier die kommas tauschen durch semikolon
            //beachten wenn sincos mal genutzt werden sollte
            int beginIndex = content.indexOf("sincos");
            int endIndex = content.indexOf("]", beginIndex);
            content = content.substring(beginIndex, endIndex).replaceAll(",", ";");
        }

        String[] variables;

        if (content.contains(",")) {
            variables = content.split(",");
        } else {
            variables = content.split("\\s+");
        }

        String method;
        String assign;
        for (String var : variables) {

            var = var.trim();
            if (var.contains("[")) {
                //easter
                method = "do_" + var.substring(0, var.indexOf("[")).trim().toLowerCase();
                assign = var.substring(var.indexOf("[") + 1, var.indexOf("]")).trim();

            } else if (var.length() > 2 && (var.charAt(2) == '1' || var.charAt(2) == '2')) {
                //outlier oder ramp: rp1999.1 or LS2002.4

                method = "do_" + var.substring(0, 2).toLowerCase();
                assign = var.substring(2);

            } else {
                //const oder td
                method = "do_" + var.toLowerCase();
                assign = "";
            }
            try {
                //5. try to invoke the method for the argument
                Method m = this.getClass().getDeclaredMethod(method.toString().toLowerCase(), SpecificationPart.class, String.class);
                m.setAccessible(true);
                m.invoke(this, partName, assign);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                messages.add(partName.name() + ": " + (assign == null ? "Argument VARIABLES is empty" : "Value " + content.toUpperCase() + " is not supported"));
            }

            //dynamisch mit method und string, exception handling
        }
    }

    private void read_zewil(SpecificationPart partName, String content) {
        read_zisl(partName, content);
    }

    private void read_zisl(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();
        Izisl zisl = Lookup.getDefault().lookup(Izisl.class);
        if (zisl != null) {
            zisl.setId(content);
            TsData dataFromWebServive = zisl.getData();
            TsMoniker moniker = zisl.getMoniker();

            if (dataFromWebServive != null) {
                switch (partName) {
                    case SERIES:
                        if (!dataLoader.isDataFromWebserviceSet()) {
                            dataLoader.setDataFromWebService(dataFromWebServive);
                            dataLoader.setMoniker(moniker);
                            dataLoader.setPeriod(dataFromWebServive.getFrequency());
                            if (!dataLoader.getMessages().isEmpty()) {
                                errors.add(partName + ": " + dataLoader.getMessages());
                            }
                        }
                        break;
                    case REGRESSION:
                        regressionLoader.addRegFromWebServive(dataFromWebServive);
                        regressionLoader.setMoniker(moniker);
                        regressionLoader.setPeriod(dataFromWebServive.getFrequency());
//                    regressionLoader.setRegressorName(content);
                        if (!regressionLoader.getMessages().isEmpty()) {
                            errors.add(partName + ": " + regressionLoader.getMessages());
                        }
                        regressionSpec = true;
                        break;
                    default:
                        messages.add(partName + ": Hier wird zisl nicht unterstützt!");
                        break;
                }
            } else {
                errors.add(partName + ": Keine Werte vom Webservice verfügbar");
            }
        } else {
            errors.add(partName + ": WebService-Plugin nicht vorhanden");
        }
    }

    /*methods for variables*/
    private void do_const(SpecificationPart partName, String content) {

        spec.getRegArimaSpecification().getArima().setMean(true);
    }

    private void do_td(SpecificationPart partName, String content) {

        TradingDaysSpec td = spec.getRegArimaSpecification().getRegression().getTradingDays();
        td.setAutoAdjust(true);
        td.setTradingDaysType(TradingDaysType.TradingDays);
        td.setTest(RegressionTestSpec.None);
        td.setHolidays(null);
        td.setUserVariables(null);
    }

    private void do_seasonal(SpecificationPart partName, String content) {
        warnings.add(partName + ": No support for value SEASONAL in argument VARIABLES");
    }

    private void do_easter(SpecificationPart partName, String content) {

        MovingHolidaySpec easter = new MovingHolidaySpec();
        int w = 7;
        if (!content.isEmpty()) {
            w = Integer.parseInt(content);//abfangen
        }
        easter.setType(MovingHolidaySpec.Type.Easter);
        easter.setTest(RegressionTestSpec.None);
        easter.setW(w);
        spec.getRegArimaSpecification().getRegression().add(easter);
    }

    private void do_ao(SpecificationPart partName, String content) {

        OutlierDefinition o = new OutlierDefinition(DateConverter.toJD(content, dataLoader.getPeriod()), OutlierType.AO, true);
//        OutlierDefinition o = new OutlierDefinition(calcDay(partName, content), OutlierType.AO, true);
        spec.getRegArimaSpecification().getRegression().add(o);
    }

    private void do_ls(SpecificationPart partName, String content) {

        OutlierDefinition o = new OutlierDefinition(DateConverter.toJD(content, dataLoader.getPeriod()), OutlierType.LS, true);
        spec.getRegArimaSpecification().getRegression().add(o);
        warnings.add(partName+": It is possible WinX13 an d JD+ have different results for the value LS in argument VARIABLES.");
    }

    private void do_tc(SpecificationPart partName, String content) {

        OutlierDefinition o = new OutlierDefinition(DateConverter.toJD(content, dataLoader.getPeriod()), OutlierType.TC, true);
        spec.getRegArimaSpecification().getRegression().add(o);
        warnings.add(partName+": It is possible WinX13 an d JD+ have different results for the value TC in argument VARIABLES.");
    }

    private void do_so(SpecificationPart partName, String content) {
        OutlierDefinition o = new OutlierDefinition(DateConverter.toJD(content, dataLoader.getPeriod()), OutlierType.SO, true);
        spec.getRegArimaSpecification().getRegression().add(o);

    }

    private void do_rp(SpecificationPart partName, String content) {

        String[] ramps = content.split("-");
        Day start = DateConverter.toJD(ramps[0].trim(), dataLoader.getPeriod());
        Day end = DateConverter.toJD(ramps[1].trim(), dataLoader.getPeriod());
//        Day start = calcDay(partName, ramps[0].trim());
//        Day end = calcDay(partName, ramps[1].trim());

        spec.getRegArimaSpecification().getRegression().add(new Ramp(start, end));
    }

    /*
     * empty methods
     *
     *   argument is not supported, but it is not an error
     */
    private void read_appendfcst(SpecificationPart partName, String content) {
    }

    private void read_decimals(SpecificationPart partName, String content) {
    }

    private void read_precision(SpecificationPart partName, String content) {
    }

    private void read_print(SpecificationPart partName, String content) {
    }

    private void read_save(SpecificationPart partName, String content) {
    }

    private void read_savelog(SpecificationPart partName, String content) {
    }

    private void read_type(SpecificationPart partName, String content) {
    }
}
