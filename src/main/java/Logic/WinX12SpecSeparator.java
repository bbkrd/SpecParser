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

import WriteAndRead.DataLoader;
import WriteAndRead.DataLoaderRegression;
import WriteAndRead.Izisl;
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
import ec.tstoolkit.MetaData;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.modelling.arima.x13.X13Exception;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import eu.nbdemetra.specParser.Miscellaneous.DateConverter;
import eu.nbdemetra.specParser.Miscellaneous.SpecificationPart;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Nina Gonschorreck
 */
public class WinX12SpecSeparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinX12SpecSeparator.class);

    /*
     spec   -   collect the x13Specification for JD+
     errors -   collect message by translating
     period -   Period from WinX12, in JD+ it is given by the data
     */
    private X13Specification spec = new X13Specification();

    private ArrayList<String> errors = new ArrayList();
    private ArrayList<String> messages = new ArrayList();
    private ArrayList<String> warnings = new ArrayList();
    private ArrayList<String> tests = new ArrayList();

    //no equivalence in a JD+ spec item, this information is given in Ts
    //for timeseries
    private DataLoader dataLoader = new DataLoader();
    private String tsName = null;
    //for regression variables
    private boolean regressionSpec = false;
    private DataLoaderRegression regressionLoader = new DataLoaderRegression();
    private String[] regressionTyp;
//    private String mtaName;
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
    private boolean x12Defaults = false;

    //ZISD
    private MetaData meta = new MetaData();

    // collect all variables and user names in order of appereance in the spc file for b argument
    ArrayList<String> fixedRegressors = new ArrayList<>();

    public WinX12SpecSeparator() {
//        setDefaults();
        spec.getRegArimaSpecification().getBasic().setPreprocessing(false);
        setX11Defaults();
        //setDefaults();
    }

    private void setDefaults() {

        //setX11Defaults();
//        if (spec.getRegArimaSpecification().equals(RegArimaSpecification.RGDISABLED)) {
//            //X11
//            spec.getRegArimaSpecification().getBasic().setPreprocessing(false);
//
//        } else {
        //X13
//            x11
        if (!x12Defaults) {

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
        x12Defaults = true;
        //}
    }

    private void setX12Part() {
        onlyX11 = false;
        spec.getRegArimaSpecification().getBasic().setPreprocessing(true);
        setDefaults();
    }

    private void setOUTLIERDefaults() {

        outlierDefaults = true;
        if (!x12Defaults) {
            setX12Part();
        }

        spec.getRegArimaSpecification().getOutliers().setLSRun(0);
        spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddOne);
        spec.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
        spec.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
        spec.getRegArimaSpecification().getOutliers().setMonthlyTCRate(0.7);

        spec.getRegArimaSpecification().getOutliers().setSpan(spec.getRegArimaSpecification().getBasic().getSpan());
    }

    private void setAUTOMDLDefaults() {
        //automdl

        automdlDefault = true;
        if (!x12Defaults) {
            setX12Part();
        }

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

    private void setX11Defaults() {

        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
        spec.getX11Specification().setSeasonal(true);
        spec.getX11Specification().setSeasonalFilter(SeasonalFilterOption.Msr);

        spec.getX11Specification().setSigma(1.5, 2.5);
        spec.getX11Specification().setForecastHorizon(0);

    }

    public void setPath(String path) {
        this.path = path;
    }

//    public void setMtaName(String name) {
//        mtaName = name;
//    }
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

    public String[] getTestsList() {
        return tests.toArray(new String[tests.size()]);
    }

    public X13Document getResult() {
        X13Document x13 = new X13Document();
        x13.setSpecification(spec);
        x13.getMetaData().putAll(meta);
        return x13;
    }

    private TsData data;

    public Ts getTs() {
//        TsData data = dataLoader.getData();
        if (dataLoader.isStartDefault() && !dataLoader.isDataFromWebserviceSet()) {
            messages.add("SERIES: Start date is set to 01/01/1970. (Code: 1409)");
        }
        if (tsName == null) {
            tsName = name;
        }

        if (dataLoader.getZislId() != null) {
            tsName = tsName + " from " + dataLoader.getZislId();
        }

        //MetaData
        return TsFactory.instance.createTs(tsName, dataLoader.getMoniker(), null, data);
    }

    public MetaData getMetaData() {
        return meta;
    }

    public String[] getRegressorName() {
        return regressionLoader.getRegressorName();
    }

    public String[] getRegDesc() {
        return regressionLoader.getRegressorDesc();
    }
//     public String[] getRegressorZislId() {
//        return regressionLoader.getRegressorZislIds();
//    }

    public TsVariable[] getRegressor() {
        if (regressionSpec) {
            TsVariable[] reg = regressionLoader.getRegressors();
            if (regressionLoader.getMessages().length() > 0) {
                messages.add("REGRESSION: " + regressionLoader.getMessages() + " (Code:1304)");
            }
            if (reg != null) {
                return reg;
            }

        }
        return null;
    }

    public String[] getRegressorTyp() {
        return regressionTyp;
    }

    public boolean isFinalUser() {
        return finalIsUser;
    }

    public X13Specification getCurrentSpec() {
        return spec;
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
                                    LOGGER.error(ex.toString());
                                    warnings.add(specPartName.name() + ": No support for argument " + lineSplitted[0].toUpperCase() + " (Code:1901)");
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
                        LOGGER.error(ex.toString());
                        warnings.add(specPartName.name() + ": No support for defaults. Please define some arguments." + " (Code:1902)");
                    }
                }
                if (specPartName.equals(SpecificationPart.SERIES)) {
                    data = dataLoader.getData();
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.error(ex.toString());
                warnings.add(specPartSplitted[0].toUpperCase() + ": No support for spec " + specPartSplitted[0].toUpperCase() + " (Code:1903)");
            }
            noArgument = false;

        }
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
                messages.add(partName + ": No support for value " + content + " in argument ACCEPTDEFAULT" + " (Code:1801)");
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
                messages.add(partName + ": No support for value " + content + " in argument ADJUST" + " (Code:1802)");
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
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument AICDIFF" + " (Code:1803)");
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
        if (assigned >= required) {

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
        } 
            else {
            messages.add(partName + ": The number of values for argument AR is not conform to the number of values in argument MODEL." + " (Code:1506)");
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
            LOGGER.error(ex.toString());
            messages.add(partName + ": No support for value " + content + " in argument ARMALIMIT" + " (Code:1804)");
        } catch (X13Exception e) {
            LOGGER.error(e.toString());
            messages.add(partName + ": " + e.toString());
        }
    }

    private void read_b(SpecificationPart partName, String content) {

        // see Reference Manual: b is after variables
        content = content.replaceAll(";", "").trim().toUpperCase();
        content = content.replaceAll("\\(", "").replaceAll("\\)", "");
        String[] values;
        if (content.contains(",")) {
            values = content.split(",", Integer.MIN_VALUE);
        } else {
            values = content.trim().split("\\s+");
        }

        for (int i = 0; i < values.length; i++) {
            if (values[i].contains("F")) {
                double value = Double.parseDouble(values[i].replaceAll("F", "").trim());
                spec.getRegArimaSpecification().getRegression().setFixedCoefficients(ITsVariable.shortName(fixedRegressors.get(i)), new double[]{value});
            } else {
                warnings.add(partName + ": Initial value for " + fixedRegressors.get(i) + " is not possible.");
            }
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
                messages.add(partName + ": No support for value " + content + " in argument BALANCED" + " (Code:1805)");
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
                messages.add(partName + ": No support for value " + content + "in argument CALENDARSIGMA. Calendarsigma will be set to None." + " (Code:1806)");
            case "NONE":
                x11.setCalendarSigma(CalendarSigma.None);
                break;
        }
    }

    private void read_centeruser(SpecificationPart partName, String content) {

//        content = content.replaceAll(";", "").trim().toUpperCase();
//        if (content.equals("SEASONAL")) {
        warnings.add(partName + ": No suppport for argument CENTERUSER. Please use a centred regression variable." + " (Code:1301)");
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
                messages.add(partName + ": No support for value " + content + " in argument CHECKMU" + " (Code:1807)");
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
                        LOGGER.error(e.toString());
                        messages.add(partName + ": No support for value " + content + " in argument CRITICAL" + " (Code:1808)");
                    }
                }
            } else {
                try {
                    spec.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(Double.parseDouble(s[0]));
                    if (s.length > 1) {
                        messages.add(partName + ": It isn't possible to set more than one critical value. The global critical value will be set to " + s[0] + " (Code:1842)");
                    }
                } catch (NumberFormatException e) {
                    LOGGER.error(e.toString());
                    messages.add(partName + ": No support for value " + content + " in argument CRITICAL" + " (Code:1809)");
                }
            }
//        errors.add(partName + ": No support for more than one critical value , critical value is set to " + s[0]);
        } else {
            messages.add(partName + ": Argument TYPES have to be defined before argument CRITICAL" + " (Code:1810)");
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
                regressionLoader.setRegressorDesc("data");
                if (regressionLoader.isStartDefault()) {
                    regressionLoader.setStart(dataLoader.getStart());
                }
                if (regressionTyp == null) {
                    regressionTyp = new String[]{"USER"};
                }
                break;
            default: //Fehler
                messages.add(partName + ": No support for argument DATA in " + partName.name().toUpperCase() + " (Code:1101)");
                break;
        }
    }

    private void read_excludefcst(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim().toUpperCase();

        if (content.equals("YES")) {
            spec.getX11Specification().setExcludefcst(true);
        } else {
            if (content.equals("NO")) {
                spec.getX11Specification().setExcludefcst(false);
            } else {
                //Fehler
                messages.add(partName + ": No support  for value " + content + " in argument EXCLUDEFCST." + " (Code:1841)");
            }
        }

    }

    private void read_format(SpecificationPart partName, String content) {

        content = content.replaceAll("[;'\"]", "").trim();

        switch (partName) {
            case SERIES:
                dataLoader.setFormat(content.toUpperCase());
                break;
            case REGRESSION:
                regressionLoader.setFormat(content.toUpperCase());
                break;
            default:
                messages.add(partName + ": No support for argument FORMAT in " + partName + " (Code:1102)");
                break;
        }

//        if(content.toUpperCase().equals("FREE") || content.toUpperCase().equals("DATEVALUE")){
//
//        }else{
//            errors.add(partName+": Format "+content.toUpperCase() + " is not supported. Data will be not loaded. ");
//        }
//        messages.add(partName + ": Argument FORMAT is ignored. Please have a look in SpecParser UserGuide");
    }

    private void read_file(SpecificationPart partName, String content) {
        /*
         Load data from a file
         possible endings are .dat or .ser or .rgr
         */

        //delete this letters: ; and ' and "
        content = content.replaceAll("[;'\"]", "").trim();
        File file;
        if (!content.contains(File.separator)) {
//            if (!content.contains("\\")) {
            file = new File((path + content));
        } else {
            file = new File(content);
        }
        //tests.add("PATH: "+path);
        //tests.add("FILE: "+file);

        switch (partName) {
            case SERIES:
                dataLoader.load(file);
                /*                if (!dataLoader.getMessages().isEmpty()) {
                 errors.add(partName + ": " + dataLoader.getMessages());
                 dataLoader.setMessage();
                 }*/
                break;
            case REGRESSION:
                regressionSpec = true;
                regressionLoader.load(file);
                if (regressionLoader.isStartDefault()) {
                    regressionLoader.setStart(dataLoader.getStart());
                }
                regressionLoader.setRegressorDesc(content);
                /*if (!regressionLoader.getMessages().isEmpty()) {
                 errors.add(partName + ": " + regressionLoader.getMessages());
                 regressionLoader.setMessage();
                 }*/
                break;
            default:
                messages.add(partName + ": No support for argument FILE in " + partName.name().toUpperCase() + " (Code:1103)");
                break;
        }
    }

    private void read_final(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toLowerCase();
        content = content.replaceAll("\\(", "").replaceAll("\\)", "").trim();

        if (content.equals("user")) {
            finalIsUser = true;
        } else {
            warnings.add(partName + ": No support for value " + content.toUpperCase() + " in argument FINAL." + " (Code:1302)");
        }
    }

    private void read_function(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        switch (content.toUpperCase()) {
            case "LOG":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
                spec.getX11Specification().setMode(DecompositionMode.Multiplicative);

                spec.getRegArimaSpecification().getRegression().getTradingDays().setAutoAdjust(false);

                transformLog = true;
                break;
            case "NONE":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
                spec.getX11Specification().setMode(DecompositionMode.Additive);

                spec.getRegArimaSpecification().getRegression().getTradingDays().setAutoAdjust(false);

                transformNone = true;
                break;
            case "AUTO":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Auto);
                spec.getX11Specification().setMode(DecompositionMode.Undefined);
                transformAuto = true;
                break;
            default:
                warnings.add(partName + ": No support for value " + content + " in argument FUNCTION. Value is changed to NONE" + " (Code:1811)");
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
                messages.add(partName + ": No support for value " + content + " in argument for HRINITIAL" + " (Code:1812)");
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
            LOGGER.error(ex.toString());
            messages.add(partName + ": No support for value " + content + " in argument LJUNGBOXLIMIT" + " (Code:1813)");
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
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument LSRUN" + " (Code:1814)");
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
        if (assigned >= required) {

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
            messages.add(partName + ": The number of values for argument MA is not conform to the number of values in argument MODEL. (Code:1505)");
        }
    }

    private void read_maxback(SpecificationPart partName, String content) {

        content = content.trim();

        try {
            int backcast = Integer.parseInt(content);
            spec.getX11Specification().setBackcastHorizon(backcast);
        } catch (NumberFormatException e) {
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument MAXBACK" + " (Code:1816)");
        }
    }

    private void read_maxlead(SpecificationPart partName, String content) {

        content = content.trim();

        try {
            int forecast = Integer.parseInt(content);
            spec.getX11Specification().setForecastHorizon(forecast);
        } catch (NumberFormatException e) {
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument MAXLEAD" + " (Code:1815)");
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
//                warnings.add(partName + ": No support for value " + content.toUpperCase() + " in argument METHOD" + " (Code:1816)");
                spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddAll);
//                spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddAll);
                break;
            default:
                messages.add(partName + ": No support for value " + content + " in argument METHOD." + " (Code:1817)");
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
                messages.add(partName + ": No support for value " + content + " in argument MIXED" + " (Code:1818)");
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
                        warnings.add(SpecificationPart.TRANSFORM + ": Decompostion mode = add is not possible for transform function = log. Value changed to mult" + " (Code:1819)");
                        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                    }
                    break;
                case "mult":
                    if (!transformNone) {
                        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                    } else {
                        warnings.add(partName + ": For transform function = none is only mode=add in JD+ possible. Value is changed to add" + " (Code:1820)");
                        spec.getX11Specification().setMode(DecompositionMode.Additive);
                    }
                    break;
                case "logadd":
                    if (!transformNone) {
                        spec.getX11Specification().setMode(DecompositionMode.LogAdditive);
                    } else {
                        warnings.add(partName + ": For transform function = none is only mode=add in JD+ possible, Value is changed to mult" + " (Code:1821)");
                        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                    }
                    break;
                case "pseudoadd":
                    if (!transformNone) {
                        warnings.add(partName + ": No support for value " + content.toUpperCase() + " in argument MODE" + " (Code:1822)");
                    } else {
                        warnings.add(partName + ": For transform function = none is only mode=add in JD+ possible. Value changed in add" + " (Code:1823)");
                        spec.getX11Specification().setMode(DecompositionMode.Additive);
                    }
                    break;
                default:
                    messages.add(partName + ": No support for value " + content.toUpperCase() + " in argument MODE" + " (Code:1824)");
                    break;
            }
        } else {
            warnings.add(SpecificationPart.TRANSFORM + ": For transform function = auto it is not possible to set a decomposition mode" + " (Code:1825)");
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
            warnings.add(partName + ": No support for value " + content + " in argument MODEL" + " (Code:1501)");
        } else {

            String[] sep = content.split("\\s*\\)\\s*\\(\\s*");

            boolean sarima;
            String p_string, d_string, q_string;
            String[] p_array, q_array;
            Parameter[] p_para, q_para;
            int start, end;
            String s;

//        2. After split there is one or two strings to analyze
            for (int i = 0; i < sep.length; i++) {
                s = sep[i].trim();
                p_string = null;
                q_string = null;
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
                            messages.add(partName + ": Period are not conform to the period of the data. Model period is changed into the period of data." + " (Code:1502)");
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
                    p_string = s.substring(1, end).trim();
                    p_string = p_string.trim();
                    p_string = p_string.replaceAll("\\s+", ",");
                    s = s.substring(end + 1).trim();
                }
                if (s.endsWith("]")) {
                    start = s.indexOf("[");
                    q_string = s.substring(start).trim();
                    q_string = q_string.replaceAll("\\s+", ",");
                    s = s.substring(0, start).trim();
                }

//           b) Between two arguments has to be ","
                if (s.contains(",")) {
                    s = s.replaceAll("\\s+", "");
                } else {
                    s = s.replaceAll("\\s+", ",");
                }

//            c) Check cases of p,q null or not
                if (p_string == null) {
//                i) extract p from string
                    end = s.indexOf(",");
                    p_string = s.substring(0, end);
                    s = s.substring(end + 1);

//                ii) set default parameter
                    p_para = new Parameter[Integer.parseInt(p_string)];
                    for (int j = 0; j < p_para.length; j++) {
                        p_para[j] = new Parameter(-0.1, ParameterType.Undefined);
                    }

                } else {
//                i) extract arguments in [...]
                    p_string = p_string.replaceAll("\\[", "").replaceAll("\\]", "");
                    p_array = p_string.split(",");

//              ii) set parameter, when not use 0.0 else default
                    p_string = p_array[p_array.length - 1];
                    p_para = new Parameter[Integer.parseInt(p_string)];
                    for (int j = 0; j < p_para.length; j++) {
                        p_para[j] = new Parameter(0.0, ParameterType.Fixed);
                    }
                    for (String a : p_array) {
                        p_para[Integer.parseInt(a) - 1] = new Parameter(-0.1, ParameterType.Undefined);
                    }
                }
                if (q_string == null) {
//                i) extract q
                    if (s.startsWith(",")) {
                        start = s.indexOf(",");
                        s = s.substring(start).trim();
                    }
                    start = s.indexOf(",");
                    q_string = s.substring(start + 1);
                    s = s.substring(0, start);

//                ii) set default parameter
                    q_para = new Parameter[Integer.parseInt(q_string)];
                    for (int j = 0; j < q_para.length; j++) {
                        q_para[j] = new Parameter(-0.1, ParameterType.Undefined);
                    }

                } else {
//                i) extract arguments in [...]
                    q_string = q_string.replaceAll("\\[", "").replaceAll("\\]", "");
                    q_array = q_string.split(",");
                    q_string = q_array[q_array.length - 1];

//                ii) set parameter
                    q_para = new Parameter[Integer.parseInt(q_string)];
                    for (int j = 0; j < q_para.length; j++) {
                        q_para[j] = new Parameter(0.0, ParameterType.Fixed);
                    }
                    for (String a : q_array) {
                        q_para[Integer.parseInt(a) - 1] = new Parameter(-0.1, ParameterType.Undefined);
                    }
                }

//            c) extract d
                d_string = s.replaceAll(",", "").replaceAll(" ", "");

//            d) set parameters in X13Specification
                int p = 0, d = 0, q = 0;
                try {
                    p = Integer.parseInt(p_string);
                    d = Integer.parseInt(d_string);
                    q = Integer.parseInt(q_string);
                } catch (NumberFormatException e) {
                    LOGGER.error(e.toString());
                    messages.add(partName + ": No support for value " + content + " in argument MODEL" + " (Code:1503)");
                }
                try {
                    if (sarima == false) {
//                    i) ARIMA part
                        spec.getRegArimaSpecification().getArima().setP(p);
                        spec.getRegArimaSpecification().getArima().setD(q);
                        spec.getRegArimaSpecification().getArima().setQ(d);
//
                        spec.getRegArimaSpecification().getArima().setPhi(p_para);
                        spec.getRegArimaSpecification().getArima().setTheta(q_para);
                    } else {
//                    ii) SARIMA part
                        if (p <= 1) {
                            spec.getRegArimaSpecification().getArima().setBP(p);
                            spec.getRegArimaSpecification().getArima().setBPhi(p_para);
                        } else {
                            spec.getRegArimaSpecification().getArima().setBP(1);
                            // was passiert hier? p_para 
                            ArrayList<Parameter> tmp1 = new ArrayList<>();
                            tmp1.add(p_para[0]);
                            spec.getRegArimaSpecification().getArima().setBTheta(tmp1.toArray(new Parameter[0]));
                            warnings.add(partName + ": Number of seasonal AR parameters is set to 1. " + " (Code:1504)");
                        }
                        if (d <= 1) {
                            spec.getRegArimaSpecification().getArima().setBD(d);
                        } else {
                            spec.getRegArimaSpecification().getArima().setBD(1);
                            warnings.add(partName + ": Number of seasonal differencing is set to 1. " + " (Code:1504)");
                        }
                        if (q <= 1) {
                            spec.getRegArimaSpecification().getArima().setBQ(q);
                            spec.getRegArimaSpecification().getArima().setBTheta(q_para);
                        }else{
                            spec.getRegArimaSpecification().getArima().setBQ(1);
                            ArrayList<Parameter> tmp = new ArrayList<>();
                            tmp.add(q_para[0]);
                            spec.getRegArimaSpecification().getArima().setBTheta(tmp.toArray(new Parameter[0]));
                            warnings.add(partName + ": Number of seasonal MA parameters is set to 1. " + " (Code:1504)");
                        }                        
                    }
                } catch (X13Exception e) {
                    LOGGER.error(e.toString());
                    warnings.add(partName + ": No support for parameters in argument MODEL" + " (Code:1504)");
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
                    warnings.add(partName + ": No support for value " + content + " in argument PERIOD. Set to default 12" + " (Code:1405)");
                    break;
            }
        } catch (NumberFormatException e) {
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument PERIOD." + " (Code:1406)");
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
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument POWER" + " (Code:1826)");
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
            LOGGER.error(ex.toString());
            messages.add(partName + ": No support for value " + content + " in argument REDUCECV" + " (Code:1827)");
        } catch (X13Exception e) {
            LOGGER.error(e.toString());
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
                        messages.add(partName + ": No Support for value " + content + " in argument SEASONALMA" + " (Code:1828)");
                        tmp.add(null);
                        break;
                }
            }
        }
        if (tmp.size() == 1 || tmp.size() == dataLoader.getPeriod().intValue()) {
            spec.getX11Specification().setSeasonalFilters(tmp.toArray(new SeasonalFilterOption[tmp.size()]));
        } else {
            messages.add(partName + ": Period is not conform to the period of the data. Seasonal filter is set to " + tmp.get(0) + " (Code:1829)");
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
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument SIGMALIM" + " (Code:1830)");
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
                messages.add(partName + ": For argument SIGMACVEC group 1 contains no element." + " (Code:1831)");
            }

        } else {
            messages.add(partName + ": Argument SIGMAVEC needs argument CALENDARSIGMA = select" + " (Code:1832)");
        }
    }

    private void read_span(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        String s = content.replaceAll("\\(", " ");
        s = s.replaceAll("\\)", " ");
        String[] split = s.split(",");

        if (split.length == 2) {

            TsPeriodSelector dummy = new TsPeriodSelector();
            if (split[0].replaceAll("\\s+", "").isEmpty()) {
                if (split[1].replaceAll("\\s+", "").isEmpty()) {
                    // beides leer, sowas in der art ( , )
                    messages.add(partName + ": No support for value " + content + " in argument SPAN" + " (Code:1401)");
                } else {
                    //TO
//                    dummy.setType(PeriodSelectorType.To);
//                    dummy.to(DateConverter.toJD(split[1], dataLoader.getPeriod()));
//                    p.to(calcDay(partName, split[1]));
                    //modify to between
                    dummy.setType(PeriodSelectorType.Between);
                    dummy.setD0(spec.getRegArimaSpecification().getBasic().getSpan().getD0());
                    dummy.setD1(DateConverter.toJD(split[1], dataLoader.getPeriod(), false));
//                    dummy.to(DateConverter.toJD(split[1], dataLoader.getPeriod()));
                }
            } else {
                if (split[1].replaceAll("\\s+", "").isEmpty()) {
                    //FROM
                    dummy.setType(PeriodSelectorType.From);
                    dummy.from(DateConverter.toJD(split[0], dataLoader.getPeriod(), true));
//                    p.from(calcDay(partName, split[0]));

                } else {
                    //BETWEEN
                    dummy.setType(PeriodSelectorType.Between);
                    dummy.between(DateConverter.toJD(split[0], dataLoader.getPeriod(), true), DateConverter.toJD(split[1], dataLoader.getPeriod(), false));
//                    p.between(calcDay(partName, split[0]), calcDay(partName, split[1]));
                }
            }

            //dummy modifizieren falls ALL ist, da wir nicht mehr mit all arbeiten stattdessen mit from, da start argument missbraucht wird
            //andere FÃƒÂ¤lle werden nicht abgeprÃƒÂ¼ft, da von korrekten Spec files ausgegangen wird
            TsPeriodSelector span_current = spec.getRegArimaSpecification().getBasic().getSpan();
            if (dummy.getType().equals(PeriodSelectorType.All)) {
                dummy.setType(PeriodSelectorType.From);
                dummy.setD0(span_current.getD0());
            }

            switch (partName.toString().toUpperCase()) {
                case "OUTLIER":
                    if (!outlierDefaults) {
                        setOUTLIERDefaults();
                    }
                    spec.getRegArimaSpecification().getOutliers().setSpan(dummy);
                    break;
                case "ESTIMATE":
                    //modelspan
                    spec.getRegArimaSpecification().getEstimate().setSpan(dummy);
                    break;
                case "SERIES":
                    spec.getRegArimaSpecification().getBasic().setSpan(dummy);
                    if (span_current.equals(spec.getRegArimaSpecification().getEstimate().getSpan())) {
                        //alter span aus Series war gleich mit modelspan, daher beide aendern um analog zu halten
                        spec.getRegArimaSpecification().getEstimate().setSpan(dummy);
                    }
                    //anderenfalls war modelspan schon geaendert
                    break;
                default:
                    messages.add(partName + ": No support for argument SPAN" + " (Code:1402)");
                    break;
            }
        } else {
            //Fehler
            //drueber nachdenken
            messages.add((String) (partName == SpecificationPart.ESTIMATE ? SpecificationPart.SERIES : partName + ": No support for value " + content + " in argument SPAN" + " (Code:1403)"));
        }
    }

    private void read_start(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        switch (partName) {
            case SERIES:
                Day start = DateConverter.toJD(content, dataLoader.getPeriod(), true);

                if (dataLoader.isStartDefault()) {
                    dataLoader.setStart(start);
                }

                if (start.isNotBefore(dataLoader.getStart())) {
                    TsPeriodSelector p = spec.getRegArimaSpecification().getBasic().getSpan();
//                if(spec.getRegArimaSpecification().getBasic().getSpan().)
                    switch (p.getType()) {
                        case All:
                            p.setType(PeriodSelectorType.From);
                            p.setD0(start);
                            break;
                        case To:
                            p.setType(PeriodSelectorType.Between);
                            p.setD0(start);
                            break;
                        case Between:
                        case From:
                            if (p.getD0().isBefore(start)) {
                                //d0<start
                                //=> Fehler da d0 vor start
                                p.setD0(start);
                                warnings.add(partName + ": Start and span are not correct. (Code:1407)");
                            }//d0>=start
                            break;
                        default://
                            warnings.add(partName + ": Something is wrong at start or span argument. (Code:1408)");
                            //einfach mal auf from und start
                            p.setType(PeriodSelectorType.From);
                            p.setD0(start);
                            break;
                    }
                    //                spec.getRegArimaSpecification().getBasic().setSpan(p);
                    spec.getRegArimaSpecification().getEstimate().setSpan(p);

                } else {
                    warnings.add(partName + ": Start date is before start of timeseries. (Code: 1410)");
                }
                break;
            case REGRESSION:
                content = content.replaceAll("\\(", "").replaceAll("\\)", "").trim();
                regressionLoader.setStart(DateConverter.toJD(content, regressionLoader.getPeriod(), true));
                regressionLoader.changeStartDefault();

                break;
            default:
                messages.add(partName + ": No support for argument START" + " (Code:1404)");
                break;
        }
    }

    private void read_tcrate(SpecificationPart partName, String content) {

        String s = content.replaceAll(";", "").trim();
        try {
            double value = Double.parseDouble(s);

            spec.getRegArimaSpecification().getOutliers().setMonthlyTCRate(value);
        } catch (NumberFormatException e) {
            LOGGER.error(e.toString());
            messages.add(partName + ": No support for value " + content + " in argument TCRATE" + " (Code:1833)");
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
            LOGGER.error(ex.toString());
            messages.add(partName + ": No support for value " + content + " in argument TOL" + " (Code:1834)");
        } catch (X13Exception e) {
            LOGGER.error(e.toString());
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
            LOGGER.error(ex.toString());
            messages.add(partName + ": No support for value " + content + " in argument TRENDMA" + " (Code:1835)");
        }
    }

    private void read_type(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        if (content.toUpperCase().equals("SUMMARY")) {
            spec.getX11Specification().setSeasonal(false);
        } else {
            messages.add(partName + ": No support for value " + content.toUpperCase() + " in argument TYPE." + " (Code:1836)");
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
//                    warnings.add(partName + ": It is possible WinX13 and JD+ have different results for the value LS in argument TYPES." + " (Code:1837)");

//                        value.add(new SingleOutlierSpec(OutlierType.LS));
                    break;
                case "TC":
                    spec.getRegArimaSpecification().getOutliers().add(OutlierType.TC);
//                        value.add(new SingleOutlierSpec(OutlierType.TC));
//                    warnings.add(partName + ": It is possible WinX13 and JD+ have different results for the value TC in argument TYPES." + " (Code:1838)");

                    break;
                default:
                    messages.add(partName + ": No support for value " + t.toUpperCase() + " in argument TYPES" + " (Code:1839)");
                    break;
            }
        }
        warnings.add(partName + ": It is possible WinX13 and JD+ have different results argument TYPES." + " (Code:1837)");
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
            LOGGER.error(ex.toString());
            messages.add(partName + ": No support for value " + content + " in argument URFINAL" + " (Code:1840)");
        } catch (X13Exception e) {
            LOGGER.error(e.toString());
            messages.add(partName + ": " + e.getMessage());
        }
    }

    private void read_user(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim().toUpperCase();
        String[] regressors = content.split("\\s+");

        for (String s : regressors) {
            fixedRegressors.add("reg_SpecParser@" + s.trim());
        }

        regressionLoader.setRegressorName(regressors);

        if (regressionLoader.isStartDefault()) {
            regressionLoader.setStart(dataLoader.getStart());
        }
        regressionLoader.setPeriod(dataLoader.getPeriod());

        if (regressionTyp == null) {
            regressionTyp = new String[regressors.length];
            for (int i = 0; i < regressors.length; i++) {
                //default usertype
                regressionTyp[i] = "USER";
            }
        }
//            else {
//            if (regressionTyp.length != regressors.length) {
//                regressionTyp = new String[regressors.length];
//            }
//        }

        regressionSpec = true;
    }

    private void read_usertype(SpecificationPart partName, String content) {
        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();
        String[] regressors = content.split("\\s+");

        if (regressionTyp == null) {
            regressionTyp = new String[regressors.length];
        }

        /*Beide Variablen sind fÃ¼r den Fall, dass der Regressortyp nicht unterstuetzt wird und mehr als einer angegeben ist. 
         Das Ziel ist es die Meldung im default case nur einmal auszugeben. Z.B. usertype = tdstock*/
        boolean stretched = false, alreday = false;

        String[] reg = new String[regressionTyp.length];
//        String[] reg = new String[regressors.length];
        for (int j = 0; j < reg.length; j++) {
            if (j < regressors.length) { // nachdenken
                reg[j] = regressors[j];
            } else {
                reg[j] = regressors[0];
                stretched = true;
            }
        }

        //regressionTyp.lwngth == reg.length
        for (int i = 0; i < regressionTyp.length; i++) {

            switch (reg[i].toUpperCase().trim()) {
                case "TD":
                    regressionTyp[i] = "TD";
                    break;
                case "USER":
                    regressionTyp[i] = "USER";
                    break;
                case "SEASONAL":
                    regressionTyp[i] = "SEASONAL";
                    break;
                case "LS":
                    regressionTyp[i] = "LS";
                    break;
                //Version 1.5.6 Sylwias mail vom 12.10.
                case "HOLIDAY":
                    regressionTyp[i] = "HOLIDAY";
                    break;
                default: // easter, tdstock etc.
                    if (!stretched) {
                        warnings.add(partName + ": No support for value " + regressors[i].toUpperCase() + " in argument USERTYPE. Values changed to value USER" + " (Code:1303)");
                    } else {
                        if (!alreday) {
                            warnings.add(partName + ": No support for value " + reg[i].toUpperCase() + " in argument USERTYPE. Values changed to value USER" + " (Code:1303)");
                            alreday = true;
                        }
                    }

                    regressionTyp[i] = "USER";
                    break;
            }
        }
        regressionSpec = true;
    }

    private void read_variables(SpecificationPart partName, String content) {

        warnings.add(partName + ": It is possible WinX13 and JD+ have different results for argument VARIABLES." + " (Code:1602)");

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim().toLowerCase();

        if (content.contains("sincos")) {
            //hier die kommas tauschen durch semikolon
            //beachten wenn sincos mal genutzt werden sollte
            int beginIndex = content.indexOf("sincos");
            int endIndex = content.indexOf("]", beginIndex) + 1;
            String sincos = content.substring(beginIndex, endIndex);
            content = content.replace(sincos, "");
//            content = content.replaceAll(sincos, sincos.replaceAll(",", ";")); Komma ersetzen
            warnings.add(partName + ": Value SINCOS in argument VARIABLES is not supported (Code:1604).");
        }

        // create some separator
        content = content.replaceAll(",", " ");
        String[] variables = content.split("\\s+");

//        if (content.contains(",")) {
//            variables = content.split(",");
//        } else {
//            variables = 
//        }
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
                Method m = this.getClass().getDeclaredMethod(method.toLowerCase(), SpecificationPart.class, String.class);
                m.setAccessible(true);
                m.invoke(this, partName, assign);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                LOGGER.error(ex.toString());
                messages.add(partName.name() + ": " + (assign == null ? "Argument VARIABLES is empty" : "Value " + content.toUpperCase() + " in VARIABLES is not supported" + " (Code:1600)"));
            }

            //dynamisch mit method und string, exception handling
        }
    }

    private void read_zewil(SpecificationPart partName, String content) {

        content = content.replaceAll("\\(", "").replaceAll("\\)", "").trim();
        if (content.endsWith(";")) {
            content = content.substring(0, content.lastIndexOf(";"));
        }
        String[] zewil_reihen = content.split(";");
        for (String reihe : zewil_reihen) {
            read_zisl(partName, reihe);
        }
    }

    private void read_zisl(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();
        Izisl zisl = Lookup.getDefault().lookup(Izisl.class);
        if (zisl != null) {
            zisl.setId(content.toUpperCase(), name);
            TsData dataFromWebServive = zisl.getData();
            TsMoniker moniker = zisl.getMoniker();

            if (dataFromWebServive != null) {
                switch (partName) {
                    case SERIES:
                        if (dataLoader.getZislId() == null) {
                            if (!dataLoader.isDataFromWebserviceSet()) {
                                dataLoader.setDataFromWebService(dataFromWebServive);
                                dataLoader.setMoniker(moniker);
                                dataLoader.setPeriod(dataFromWebServive.getFrequency());
                                dataLoader.setZislId(content);
                                if (!dataLoader.getMessages().isEmpty()) {
                                    errors.add(partName + ": " + dataLoader.getMessages());
                                }
                            }
                        } else {
                            // 2. zisl befehl fÃ¼r alte d10
                            StringBuilder sb = new StringBuilder("prodebene");
                            sb.append(InformationSet.STRSEP).append("seasonalfactor").append(InformationSet.STRSEP).append("loadid");
                        }
                        break;
                    case REGRESSION:
                        regressionLoader.addRegFromWebServive(dataFromWebServive);
                        regressionLoader.setMoniker(moniker);
                        regressionLoader.setPeriod(dataFromWebServive.getFrequency());
//                    regressionLoader.setRegressorDesc(content);
                        regressionLoader.setRegressorZisl(content);
                        if (!regressionLoader.getMessages().isEmpty()) {
                            errors.add(partName + ": " + regressionLoader.getMessages());
                        }
                        regressionSpec = true;
                        break;
                    default:
                        messages.add(partName + ": Hier wird zisl nicht unterstÃƒÂ¼tzt!" + " (Code:1201)");
                        break;
                }
            } else {
                errors.add(partName + ": Keine Werte vom Webservice verfÃƒÂ¼gbar" + " (Code:1202)");
            }
        } else {
            errors.add(partName + ": WebService-Plugin nicht vorhanden" + " (Code:1203)");
        }
    }

    private void read_zisd(SpecificationPart partName, String content) {

        content = content.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(";", "").trim();
        String[] zisd = content.split("-");

        String tmp = "";
        switch (zisd[0].toUpperCase()) {
            case "USR":
                tmp = "calendarfactor";
                break;
            case "D10":
                tmp = "seasonalfactor";
                break;
            case "FCT":
                tmp = "forecast";
                break;
            default:
                //nicht unterstÃƒÂ¼tzt
                break;
        }

        StringBuilder sb = new StringBuilder("zebene");
        sb.append(InformationSet.STRSEP).append(tmp).append(InformationSet.STRSEP).append("updateid");

        meta.put(sb.toString(), zisd[1]);
    }

    /*methods for variables*/
    private void do_const(SpecificationPart partName, String content) {

        spec.getRegArimaSpecification().getArima().setMean(true);
    }

    private void do_td(SpecificationPart partName, String content) {

        TradingDaysSpec td = spec.getRegArimaSpecification().getRegression().getTradingDays();
//        td.setAutoAdjust(false);
        td.setTradingDaysType(TradingDaysType.TradingDays);
        td.setTest(RegressionTestSpec.None);
        td.setHolidays(null);
        td.setUserVariables(null);
    }

    private void do_seasonal(SpecificationPart partName, String content) {
        warnings.add(partName + ": No support for value SEASONAL in argument VARIABLES. Please, increase MA parameter and set closed to 1." + " (Code:1601)");
    }

    private void do_easter(SpecificationPart partName, String content) {

        MovingHolidaySpec easter = new MovingHolidaySpec();
        int w = 7;
        if (!content.isEmpty()) {
            try {
                w = Integer.parseInt(content);//abfangen
            } catch (NumberFormatException e) {
                LOGGER.error(e.toString());
                warnings.add(partName + ": No support for variables = ( easter[ " + content + " ] )");
            }
        }
        easter.setType(MovingHolidaySpec.Type.Easter);
        easter.setTest(RegressionTestSpec.None);
        easter.setW(w);
        spec.getRegArimaSpecification().getRegression().add(easter);
    }

    private void do_ao(SpecificationPart partName, String content) {

        Day day = DateConverter.toJD(content, dataLoader.getPeriod(), true);
        OutlierDefinition o = new OutlierDefinition(day, OutlierType.AO);
        spec.getRegArimaSpecification().getRegression().add(o);

        fixedRegressors.add("AO (" + day.toString() + ")");
    }

    private void do_ls(SpecificationPart partName, String content) {

        Day day = DateConverter.toJD(content, dataLoader.getPeriod(), true);
        OutlierDefinition o = new OutlierDefinition(day, OutlierType.LS);
        spec.getRegArimaSpecification().getRegression().add(o);

        fixedRegressors.add("LS (" + day.toString() + ")");
    }

    private void do_tc(SpecificationPart partName, String content) {
        Day day = DateConverter.toJD(content, dataLoader.getPeriod(), true);
        OutlierDefinition o = new OutlierDefinition(day, OutlierType.TC);
        spec.getRegArimaSpecification().getRegression().add(o);
        fixedRegressors.add("TC (" + day.toString() + ")");
    }

    private void do_tdstock(SpecificationPart partName, String content) {

        int w = 0;
        TradingDaysSpec td = spec.getRegArimaSpecification().getRegression().getTradingDays();
//        td.setAutoAdjust(false);
        if (content != null) {
            try {
                w = Integer.parseInt(content);
            } catch (NumberFormatException e) {
                LOGGER.error(e.toString());
                warnings.add(partName + ": No support for variables = ( tdstock[ " + content + " ] )");
            }
        }
        td.setStockTradingDays(w);
        spec.getRegArimaSpecification().getRegression().setTradingDays(td);
    }

    private void do_so(SpecificationPart partName, String content) {
        Day day = DateConverter.toJD(content, dataLoader.getPeriod(), true);
        OutlierDefinition o = new OutlierDefinition(day, OutlierType.SO);
        spec.getRegArimaSpecification().getRegression().add(o);

        fixedRegressors.add("SO (" + day.toString() + ")");
    }

    private void do_rp(SpecificationPart partName, String content) {

        String[] ramps = content.split("-");
        Day start = DateConverter.toJD(ramps[0].trim(), dataLoader.getPeriod(), true);
        Day end = DateConverter.toJD(ramps[1].trim(), dataLoader.getPeriod(), false);
//        Day start = calcDay(partName, ramps[0].trim());
//        Day end = calcDay(partName, ramps[1].trim());

        spec.getRegArimaSpecification().getRegression().add(new Ramp(start, end));

        fixedRegressors.add("rp$" + start.toString() + "$" + end.toString());
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

    private void read_outofsample(SpecificationPart partName, String content) {
    }

    private void read_precision(SpecificationPart partName, String content) {
    }

    private void read_print(SpecificationPart partName, String content) {
    }

    private void read_save(SpecificationPart partName, String content) {
    }

    private void read_savelog(SpecificationPart partName, String content) {
    }

    private void read_spectrumtype(SpecificationPart partName, String content) {
    }
}
