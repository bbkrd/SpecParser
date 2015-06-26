/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.x11.*;
import ec.satoolkit.x13.X13Specification;
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.TsVariableDescriptor.UserComponentType;
import ec.tstoolkit.modelling.arima.x13.*;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec.Type;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import eu.nbdemetra.specParser.Miscellaneous.SpecificationPart;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class JDSpecSeparator {

    /*
     result      -   collect all arguments with values in a map with a specification part key, sort to ABC
     spec        -   the specification to translate
     ts          -   further information, which not in spec
     errors      -   collect all errors, which appear where is no translation possible
     messages    -   collect messages, which are not errors by translating
     */
    private X13Specification spec;
    private Ts ts;
    private ProcessingContext context;
    private LinkedHashMap<SpecificationPart, LinkedHashMap<String, String>> result = new LinkedHashMap();
    private ArrayList<String> errors = new ArrayList();
    private ArrayList<String> messages = new ArrayList<>();

    private boolean transformAuto = false;
    private boolean transformLog = false;
    private boolean transformNone = false;


    /*Constructor*/
    public JDSpecSeparator(X13Specification x13, Ts ts, ProcessingContext context) {
//      get all information for translating 
        this.ts = ts;
        this.spec = x13;
        this.context = context;
    }

    public void build() {

        if (ts != null) {
            if (!ts.getTsData().getValues().hasMissingValues()) {
                generateTs();

                //because of the order of the methods calling x11 and benchmark two times
                if (spec.getRegArimaSpecification().equals(RegArimaSpecification.RGDISABLED)) {
                    generateX11();
                    generateBenchmark();

                } else {
                    generateBasicSpec();
                    generateEstimateSpec();
                    generateTransform();
                    generateRegression();

                    if (spec.getRegArimaSpecification().getAutoModel().isEnabled()) {
                        generateAutomdl();
                    } else {
                        generateArima();
                    }
                    generateOutlier();
                    generateX11();
                    generateForecast();
                    generateBenchmark();
                }

            } else {
                errors.add("MISSING VALUES ARE NOT ALLOWED IN WINX12");
            }
        }
    }

    private void generateArima() {

        ArimaSpec reg = spec.getRegArimaSpecification().getArima();
        LinkedHashMap<String, String> arima;

        if (result.containsKey(SpecificationPart.ARIMA)) {
            arima = result.get(SpecificationPart.ARIMA);
        } else {
            arima = new LinkedHashMap<>();
        }

//        1)model
        String model = "( " + reg.getP() + " " + reg.getD() + " " + reg.getQ() + " )( " + reg.getBP() + " " + reg.getBD() + " " + reg.getBQ() + " )";
        arima.put("model", model);

//        2)AR
        StringBuilder ar = null;
        double value;
        if (reg.getP() > 0) {
            ar = new StringBuilder("( ");
            Parameter p;
            for (int i = 0; i < reg.getP(); i++) {
                p = reg.getPhi()[i];
                value = p.getValue();
                if (value != 0.0) {
                    value = value * -1.0;
                }
                ar.append(value);
                switch (p.getType()) {
                    case Fixed:
                        ar.append("f");
                        break;
                    default:
                        break;
                }
                if (i < (reg.getP() - 1) || reg.getBP() > 0) {
                    ar.append(" , ");
                }
            }
        }
        if (reg.getBP() > 0) {
            if (ar == null) {
                ar = new StringBuilder("( ");
            }
            Parameter p;
            for (int i = 0; i < reg.getBP(); i++) {
                p = reg.getBPhi()[i];
                value = p.getValue();
                if (value != 0.0) {
                    value = value * -1.0;
                }
                ar.append(value);
                switch (p.getType()) {
                    case Fixed:
                        ar.append("f");
                        break;
                    default:
                        break;
                }
                if (i < (reg.getBP() - 1)) {
                    ar.append(" , ");
                }
            }
        }
        if (ar != null) {
            ar.append(" )");
            arima.put("ar", ar.toString());
        }

//        3)MA
        StringBuilder ma = null;
        if (reg.getQ() > 0) {
            ma = new StringBuilder("( ");
            Parameter p;
            for (int i = 0; i < reg.getQ(); i++) {
                p = reg.getTheta()[i];
                value = p.getValue();
                if (value != 0.0) {
                    value = value * -1.0;
                }
                ma.append(value);
                switch (p.getType()) {
                    case Fixed:
                        ma.append("f");
                        break;
                    default:
                        break;
                }
                if (i < (reg.getQ() - 1) || reg.getBQ() > 0) {
                    ma.append(" , ");
                }
            }
        }
        if (reg.getBQ() > 0) {
            if (ma == null) {
                ma = new StringBuilder("( ");
            }
            Parameter p;
            for (int i = 0; i < reg.getBQ(); i++) {
                p = reg.getBTheta()[i];
                value = p.getValue();
                if (value != 0.0) {
                    value = value * -1.0;
                }
                ma.append(value);
                switch (p.getType()) {
                    case Fixed:
                        ma.append("f");
                        break;
                    default:
                        break;
                }
                if (i < (reg.getBQ() - 1)) {
                    ma.append(" , ");
                }
            }
        }
        if (ma != null) {
            ma.append(" )");
            arima.put("ma", ma.toString());
        }

//        4)Mean
        if (reg.isMean()) {
            errors.add(SpecificationPart.ARIMA + ": Mean is not supported in WinX12");
        }

        result.put(SpecificationPart.ARIMA, arima);
    }

    private void generateAutomdl() {

        AutoModelSpec reg = spec.getRegArimaSpecification().getAutoModel();
        LinkedHashMap<String, String> automdl;

        if (result.containsKey(SpecificationPart.AUTOMDL)) {
            automdl = result.get(SpecificationPart.AUTOMDL);
        } else {
            automdl = new LinkedHashMap<>();
        }

//        1)accept default
        String acceptdef;
        if (reg.isAcceptDefault()) {
            acceptdef = "yes";
        } else {
            acceptdef = "no";
        }
        automdl.put("acceptdefault", acceptdef);

//        2)Balanced
        String balanced;
        if (reg.isBalanced()) {
            balanced = "yes";
        } else {
            balanced = "no";
        }
        automdl.put("balanced", balanced);

//        3)cancelation limit
        errors.add(SpecificationPart.AUTOMDL + ": No Translation for Cancelation limit");

        //4)checkmu
        String checkmu;
        if (reg.isCheckMu()) {
            checkmu = "yes";
        } else {
            checkmu = "no";
        }
        automdl.put("checkmu", checkmu);

//        5)ljungboxlimit
        automdl.put("ljungboxlimit", reg.getLjungBoxLimit() + "");

//        6)mixed
        String mixed;
        if (reg.isMixed()) {
            mixed = "yes";
        } else {
            mixed = "no";
        }
        automdl.put("mixed", mixed);

//        7)reducecv
        automdl.put("reducecv", reg.getPercentReductionCV() + "");

//        8)unit root limit
        automdl.put("urfinal", reg.getUnitRootLimit() + "");

//        9)final + initial unit root limit
        errors.add(SpecificationPart.AUTOMDL + ": There is no translation for final and initial unit root limit");

//        10)hrinitial
        String hr;
        if (reg.isHannanRissannen()) {
            hr = "yes";
        } else {
            hr = "no";
        }
        automdl.put("hrinitial", hr);

        result.put(SpecificationPart.AUTOMDL, automdl);
    }

    private void generateBasicSpec() {

        //1) span
        RegArimaSpecification reg = spec.getRegArimaSpecification();
        LinkedHashMap<String, String> series;

        if (result.containsKey(SpecificationPart.SERIES)) {
            series = result.get(SpecificationPart.SERIES);
        } else {
            series = new LinkedHashMap<>();
        }

        String span = separateSpan(reg.getBasic().getSpan(), "SERIES");
        if (span != null) {
            series.put("span", span);
        }
        result.put(SpecificationPart.SERIES, series);
    }

    private void generateBenchmark() {

        if (spec.getBenchmarkingSpecification().isEnabled()) {
            messages.add("BENCHMARKING is not supported");
        }
    }

    private void generateEstimateSpec() {

        RegArimaSpecification reg = spec.getRegArimaSpecification();

//        1)modelspan
        LinkedHashMap<String, String> series;
        if (result.containsKey(SpecificationPart.SERIES)) {
            series = result.get(SpecificationPart.SERIES);
        } else {
            series = new LinkedHashMap<>();
        }

        String span = separateSpan(reg.getEstimate().getSpan(), "ESTIMATE");
        if (span != null) {
            series.put("modelspan", span);
        }

        result.put(SpecificationPart.SERIES, series);

//        2)tol
        LinkedHashMap<String, String> estimate;
        if (result.containsKey(SpecificationPart.ESTIMATE)) {
            estimate = result.get(SpecificationPart.ESTIMATE);
        } else {
            estimate = new LinkedHashMap<>();
        }
        estimate.put("tol", reg.getEstimate().getTol() + "");

//        3)outofsample
        estimate.put("outofsample", "yes");

//        4)maxiter
        estimate.put("maxiter", "1500");

        result.put(SpecificationPart.ESTIMATE, estimate);
    }

    private void generateForecast() {

        LinkedHashMap<String, String> forecast;
        if (result.containsKey(SpecificationPart.FORECAST)) {
            forecast = result.get(SpecificationPart.FORECAST);
        } else {
            forecast = new LinkedHashMap<>();
        }

//        1) maxlead/forecast horizon
        int maxlead = spec.getX11Specification().getForecastHorizon();

        if (maxlead < 0) {
            if (ts != null) {
                maxlead = -1 * maxlead * ts.getTsData().getFrequency().intValue();
            } else {
                maxlead = -1 * maxlead * 12;
            }
        }
        forecast.put("maxlead", maxlead + "");

        result.put(SpecificationPart.FORECAST, forecast);
    }

    private void generateOutlier() {

        OutlierSpec outSpec = spec.getRegArimaSpecification().getOutliers();

        if (outSpec.isUsed()) {
            LinkedHashMap<String, String> outlier;
            if (result.containsKey(SpecificationPart.OUTLIER)) {
                outlier = result.get(SpecificationPart.OUTLIER);
            } else {
                outlier = new LinkedHashMap<>();
            }

//        1)span
            String span = separateSpan(outSpec.getSpan(), "OUTLIER");
            if (span != null) {
                outlier.put("span", span);
            }

//        2)LSRun
            outlier.put("lsrun", outSpec.getLSRun() + "");

//        3)types
            StringBuilder types;
            if (outSpec.getTypes() == null) {
                types = new StringBuilder("none");
            } else {
                types = new StringBuilder("( ");
                for (SingleOutlierSpec s : outSpec.getTypes()) {
                    switch (s.getType()) {
                        case AO:
                        case LS:
                        case TC:
                            types.append(s.getType()).append(" ");
                            break;
                        default:
                            errors.add(SpecificationPart.OUTLIER + ": No Translation for " + s.getType().name());
                            break;
                    }
                }
                types.append(")");
            }
            outlier.put("types", types.toString());

//        4)critical value
            if (outSpec.getDefaultCriticalValue() != 0.0) {
                outlier.put("critical", outSpec.getDefaultCriticalValue() + "");
            }

//        5)TC rate
            outlier.put("tcrate", outSpec.getMonthlyTCRate() + "");

//        6)Method
            outlier.put("method", outSpec.getMethod().toString());

            result.put(SpecificationPart.OUTLIER, outlier);
        }
    }

    private void generateRegression() {

        RegressionSpec reg = spec.getRegArimaSpecification().getRegression();
        if (reg.isUsed()) {

            LinkedHashMap<String, String> regression;
            if (result.containsKey(SpecificationPart.REGRESSION)) {
                regression = result.get(SpecificationPart.REGRESSION);
            } else {
                regression = new LinkedHashMap<>();
            }

            //collect all values for argument variables
            StringBuilder variables = new StringBuilder();

            //const
            if (!spec.getRegArimaSpecification().getAutoModel().isEnabled() && spec.getRegArimaSpecification().getArima().isMean()) {
                variables.append("const\n");
            }

//            A)TradingDays
            TradingDaysSpec tdSpec = reg.getTradingDays();
            /* Possibilities fpr click in Calender -> TradingDays -> option
             *   - None          if tdSpec = null
             *   - Default       if tdSpec!= null + UserVariables = null + Holidays = null
             *   - Stock         not implemented
             *   - Holidays      if tdSpec!= null + UserVariables = null + Holidays != null
             *   - UserDefined   if tdSpec!= null + UserVariables != null
             */
            if (tdSpec.isUsed()) {
                if (tdSpec.getUserVariables() != null) {
                    //GUI option: UserDefined
                    String regName = tdSpec.getUserVariables()[0];
                    TsVariables var = context.getTsVariableManagers().get(regName.split("\\.")[0]);

//                    1)data
                    TsData regData = extractData(((DynamicTsVariable) var.variables().toArray()[0]));
                    StringBuilder data = new StringBuilder("(");
                    int counter = regData.getStart().getPosition() + 1;
                    for (int i = 0; i < regData.getValues().getLength(); i++) {
                        if (i == 0) {
                            for (int j = 0; j < counter; j++) {
                                data.append("\t");
                            }
                        }
                        data.append(regData.getValues().get(i));
//            
                        int period = Integer.parseInt(result.get(SpecificationPart.SERIES).get("period"));
                        if ((counter) % period == 0) {
                            data.append("\n\t\t");
                        } else {
                            data.append("\t");
                        }
                        counter++;
                    }
                    data.append(")");
                    regression.put("data", data.toString());

//                    2)usertype (WinX need this information)
                    regression.put("usertype", "td");

//                    3)start
                    regression.put("start", regData.getStart().getYear() + "." + regData.getStart().getPosition() + 1);

                } else {
                    if (tdSpec.getHolidays() != null) {
                        //GUI option: Holidays
                        errors.add("REGRESSION: Holidays are not supported in WinX12");

                    } else {
                        //GUI option: Default
                        switch (tdSpec.getTradingDaysType()) {
                            case WorkingDays:
                                errors.add("REGRESSION: WorkingDays are not supported in WinX12");
                                break;
                            case TradingDays: {
                                if (tdSpec.isAutoAdjust()) {
                                    variables.append("td\n\t\t");
                                } else {
                                    errors.add("REGRESSION: autoadjusted has to be enabled");
                                }
                                break;
                            }
                            default: //None -> switched to GUI option None
                                break;
                        }
                    }
                }

//               i) test
                switch (tdSpec.getTest()) {
                    case None:
                        break;
                    case Add:
                    case Remove:
                    default:
                        messages.add("REGRESSION: Test doesn't exist in WINX12. Please set it to None.");
                }

            }//GUI option: None

//            B)Easter
            MovingHolidaySpec mov = reg.getEaster();
            if (mov != null) {
                if (mov.getType().equals(Type.Easter)) {
                    if (!mov.getTest().equals(RegressionTestSpec.None)) {
                        errors.add("REGRESSION: Pre-Test for easter are not supported in WinX12");
                    } else {
                        variables.append("easter[");
                        variables.append(mov.getW()).append("]\n\t\t");
                    }
                } else {
                    //other types will not be supported
                }
            }

//            C) Pre-Specified outliers
            if (reg.getOutliersCount() != 0) {
                StringBuilder outlier;
                for (OutlierDefinition o : reg.getOutliers()) {

                    outlier = new StringBuilder();
                    outlier.append(o.type);
                    outlier.append(o.position.getYear()).append(".");

                    if (ts.getTsData().getFrequency().equals(TsFrequency.Monthly)) {
                        outlier.append(o.position.getMonth() + 1);
                    } else {
                        //Quaterly
                        if (o.position.getMonth() <= 2) {
                            //1.Quarter
                            outlier.append("1");
                        } else if (o.position.getMonth() <= 5) {
                            //2.quarter
                            outlier.append("2");
                        } else if (o.position.getMonth() <= 8) {
                            //3. Quarter
                            outlier.append("3");
                        } else {
                            //4.Quarter
                            outlier.append("4");
                        }
                    }

                    variables.append(outlier.toString()).append(" ");
                }
                variables.append("\n\t\t");
            }
//            D) Intervention variables
            if (reg.getInterventionVariablesCount() != 0) {
                errors.add("REGRESSION: Intervention variables are not possible in WinX");
            }
//            E) Ramps
            if (reg.getRampsCount() != 0) {
                StringBuilder ramp;
                for (Ramp r : reg.getRamps()) {

                    ramp = new StringBuilder("rp");

                    ramp.append(r.getStart().getYear()).append(".");

                    if (ts.getTsData().getFrequency().equals(TsFrequency.Monthly)) {
                        ramp.append(r.getStart().getMonth() + 1).append("-");
                    } else {
                        //Quaterly
                        if (r.getStart().getMonth() <= 2) {
                            //1.Quarter
                            ramp.append("1").append("-");
                        } else if (r.getStart().getMonth() <= 5) {
                            //2.quarter
                            ramp.append("2").append("-");
                        } else if (r.getStart().getMonth() <= 8) {
                            //3. Quarter
                            ramp.append("3").append("-");
                        } else {
                            //4.Quarter
                            ramp.append("4").append("-");
                        }
                    }
                    ramp.append(r.getEnd().getYear()).append(".");
                    if (ts.getTsData().getFrequency().equals(TsFrequency.Monthly)) {
                        ramp.append(r.getEnd().getMonth() + 1);
                    } else {
                        //Quaterly
                        if (r.getEnd().getMonth() <= 2) {
                            //1.Quarter
                            ramp.append("1");
                        } else if (r.getEnd().getMonth() <= 5) {
                            //2.quarter
                            ramp.append("2");
                        } else if (r.getEnd().getMonth() <= 8) {
                            //3. Quarter
                            ramp.append("3");
                        } else {
                            //4.Quarter
                            ramp.append("4");
                        }
                    }
                    variables.append(ramp.toString()).append(" ");
                }
                variables.append("\n\t\t");
            }

            //final step for variables argument
            if (variables.length() != 0) {
                regression.put("variables", "( " + variables.toString() + "\t\t)");
            }

            //F) user defined variables
            TsVariableDescriptor[] userDef = reg.getUserDefinedVariables();
            if (userDef.length != 0) {
                StringBuilder userName = new StringBuilder("( ");
                StringBuilder userData = new StringBuilder();

                for (TsVariableDescriptor t : userDef) {
//                    1)Name
                    userName.append(t.getName()).append(" ");

//                    2)start
                    TsData data = extractData(t.toTsVariable(context));
                    regression.put("start", data.getStart().getYear() + "." + data.getStart().getPosition() + 1);

//                    3)data
                    StringBuilder dataString = new StringBuilder("(\t");
                    for (int i = 0; i < data.getValues().getLength(); i++) {
                        dataString.append(data.getValues().get(i));
                        if (((i + 1) % data.getFrequency().intValue()) == 0) {
                            dataString.append("\n\t\t");
                        } else {
                            dataString.append("\t");
                        }
                    }
                    dataString.append(")");
                    userData.append(dataString.toString());

                    //4)Firstlag
                    if (t.getFirstLag() != 0) {
                        errors.add("REGRESSION: Firstlag in user-defined variables is not supported");
                    }
//                    5)Lastlag
                    if (t.getLastLag() != 0) {
                        errors.add("REGRESSION: Lastlag in user-defined variables is not supported");
                    }
//                    6)Component type
                    if (!t.getEffect().equals(UserComponentType.Series)) {
                        errors.add("REGRESSION: Component type in user-defined variables is not supported");
                    }
                }
//                1)Name
                regression.put("user", userName.append(")").toString());
//                2)Data
                regression.put("data", userData.toString());
//                3)usertype
//                if (tdSpec.isUsed() && tdSpec.getTradingDaysType().equals(TradingDaysType.None)) {
//                    tdSpec.getUserVariables()
                regression.put("usertype", "user");
//                }
            }

            result.put(SpecificationPart.REGRESSION, regression);
        }
    }

    private void generateTransform() {

        TransformSpec t = spec.getRegArimaSpecification().getTransform();
        LinkedHashMap<String, String> transform;
        if (result.containsKey(SpecificationPart.TRANSFORM)) {
            transform = result.get(SpecificationPart.TRANSFORM);
        } else {
            transform = new LinkedHashMap<>();
        }
        LinkedHashMap<String, String> x11;

        switch (t.getFunction()) {
            case None:
                transformNone = true;
                transform.put("function", "none");
                if (result.containsKey(SpecificationPart.X11)) {
                    x11 = result.get(SpecificationPart.X11);
                } else {
                    x11 = new LinkedHashMap<>();
                }
                x11.put("mode", "add");
                messages.add(SpecificationPart.TRANSFORM + ": Only decompostion mode = add is in X11 part possible");
                result.put(SpecificationPart.X11, x11);
                break;
            case Auto:
                transformAuto = true;
                transform.put("function", "auto");
                transform.put("aicdiff", t.getAICDiff() + "");
                break;
            case Log:
                transformLog = true;
                transform.put("function", "log");

                if (result.containsKey(SpecificationPart.X11)) {
                    x11 = result.get(SpecificationPart.X11);
                } else {
                    x11 = new LinkedHashMap<>();
                }
                x11.put("mode", "mult");
                result.put(SpecificationPart.X11, x11);

                String adjust;
                switch (t.getAdjust()) {
                    case None:
                        adjust = "none";
                        break;
                    case LeapYear:
                        adjust = "lpyear";
                        break;
                    case LengthOfPeriod:
                        if (ts.getTsData().getFrequency().intValue() == 12) {
                            adjust = "lom";
                        } else {
                            adjust = "loq";
                        }
                        break;
                    default:
                        errors.add(SpecificationPart.TRANSFORM + ": No translation for " + t.getAdjust());
                        adjust = null;
                        break;
                }

                if (adjust != null) {
                    transform.put("adjust", adjust);
                }
            default:
                break;
        }

        result.put(SpecificationPart.TRANSFORM, transform);
    }

    private void generateTs() {

        LinkedHashMap<String, String> series;
        if (result.containsKey(SpecificationPart.SERIES)) {
            series = result.get(SpecificationPart.SERIES);
        } else {
            series = new LinkedHashMap<>();
        }

//        1) title
        String name;
        if (ts.getRawName() != null) {
            name = ts.getRawName();
        } else {
            if (ts.getName() != null) {
                name = ts.getName();
            } else {
                name = ts.toString();
            }
        }
        name = name.replaceAll("'", "");
        if (name.isEmpty()) {
            name = "no title";
        }
        series.put("name", "'" + name + "'");

        //2) start
        StringBuilder start = new StringBuilder(ts.getTsData().getStart().getYear() + ".");
        start.append(ts.getTsData().getStart().getPosition() + 1);
        series.put("start", start.toString());

//        3) period
        int period = ts.getTsData().getFrequency().intValue();
        series.put("period", period + "");

//        4) data
        int lineCounter = 8; // "data = (" -> #8
        StringBuilder data = new StringBuilder("(");
        int counter = ts.getTsData().getStart().getPosition() + 1;
        for (int i = 0; i < ts.getTsData().getValues().getLength(); i++) {
            if (i == 0) {
                for (int j = 0; j < counter; j++) {
                    if (lineCounter < 133) {
                        data.append(" \t");
                        lineCounter = lineCounter + "\t".length();
                    } else {
                        data.append("\n\t\t");
                        lineCounter = 2 * "\t".length();
                    }
                }
            }

            if ((lineCounter + (ts.getTsData().getValues().get(i) + "").length()) >= 133) {
                data.append("\n\t\t");
                lineCounter = 2 * "\t".length();
            }
            data.append(ts.getTsData().getValues().get(i));
            lineCounter = lineCounter + (ts.getTsData().getValues().get(i) + "").length();

//            if ((i + 1) % 12 == 0) {
            if ((counter) % period == 0) {
                data.append("\n\t\t");
                lineCounter = 2 * "\t".length();
            } else {
                if (lineCounter < 133) {
                    data.append("     ");
                    lineCounter = lineCounter + 5;
                } else {
                    data.append("\n\t\t");
                    lineCounter = 2 * "\t".length();
                }
            }
            counter++;
        }
        data.append(")");
        series.put("data", data.toString());

        result.put(SpecificationPart.SERIES, series);

    }

    private void generateX11() {

//        A)X11
        X11Specification x11 = spec.getX11Specification();
        LinkedHashMap<String, String> x11Result;

        if (result.containsKey(SpecificationPart.X11)) {
            x11Result = result.get(SpecificationPart.X11);
        } else {
            x11Result = new LinkedHashMap<>();
        }

        if (!transformAuto) {
            //1) Mode
            String mode = null;
            switch (x11.getMode()) {
                case Additive:
                    if (transformLog) {
                        errors.add(SpecificationPart.TRANSFORM + ": For transform function = log is decompostion mode add not possible");
                    } else {
                        mode = "add";
                        x11Result.put("mode", mode);
                    }
                    break;
                case LogAdditive:
                    if (!transformNone) {
                        mode = "logadd";
                        x11Result.put("mode", mode);
                    } else {
                        errors.add(SpecificationPart.TRANSFORM + ": For transform function = none is only decomposition mode = add possible");
                    }
                    break;
                case Multiplicative:
                    if (!transformNone) {
                        mode = "mult";
                        x11Result.put("mode", mode);
                    } else {
                        errors.add(SpecificationPart.TRANSFORM + ": For transform function = none is only decomposition mode = add possible");
                    }
                    break;
                case Undefined:
                    messages.add(SpecificationPart.X11 + ": No decomposition mode is choosen");
                    break;
                default:
                    messages.add(SpecificationPart.X11 + ": No translation for mode = " + x11.getMode());
//                mode = "mult";
                    break;
            }
        } else {
            messages.add(SpecificationPart.TRANSFORM + ": For transform function = auto is no decompostion mode possible");
        }

        //2) Seasonalma
        if (x11.isSeasonal()) {

            StringBuilder seasonalma = new StringBuilder("( ");
            if (x11.getSeasonalFilters() != null) {
                if (x11.getSeasonalFilters().length != 1) {
                    for (SeasonalFilterOption s : x11.getSeasonalFilters()) {
                        seasonalma.append(s.toString()).append("\t");
                    }
                } else {
                    for (int i = 0; i < ts.getTsData().getFrequency().intValue(); i++) {
                        seasonalma.append(x11.getSeasonalFilters()[0].toString()).append("\t");
                    }
                }
            } else {
                for (int i = 0; i < ts.getTsData().getFrequency().intValue(); i++) {
                    seasonalma.append(SeasonalFilterOption.Msr).append("\t");
                }
            }
            seasonalma.deleteCharAt(seasonalma.length() - 1);
            seasonalma.append(" )");

            x11Result.put("seasonalma", seasonalma.toString());
        }

        //3) trendma
        if (!x11.isAutoHenderson()) {
            x11Result.put("trendma", x11.getHendersonFilterLength() + "");
        }

        //4) sigmalim
        x11Result.put("sigmalim", "(" + x11.getLowerSigma() + " , " + x11.getUpperSigma() + " )");

        //5) Calendarsigma
        switch (x11.getCalendarSigma()) {
            case All:
                x11Result.put("calendarsigma", "all");
                break;
            case Select:
                x11Result.put("calendarsigma", "select");
                x11Result.put("sigmavec", "( " + do_sigmavec() + ")");
                break;
            case Signif:
                x11Result.put("calendarsigma", "signif");
                break;
            case None:
                x11Result.put("calendarsigma", "none");
            default:
                break;
        }
        result.put(SpecificationPart.X11, x11Result);
    }

    private String do_sigmavec() {
        
        StringBuilder sb = new StringBuilder("");
        SigmavecOption[] s = spec.getX11Specification().getSigmavec();
        
        if (s == null) {
            //defaut setting: Group1 for all 
            s = new SigmavecOption[ts.getTsData().getFrequency().intValue()];
            for (int i = 0; i < ts.getTsData().getFrequency().intValue(); i++) {
                s[i]=SigmavecOption.Group1;
            }
        }
        for (int i = 0; i < s.length; i++) {
            switch (s[i]) {
                case Group1:

                    switch (i) {
                        case 0:
                            if (s.length == 4) {
                                sb.append("q1 ");
                            } else {
                                sb.append("jan ");
                            }
                            break;
                        case 1:
                            if (s.length == 4) {
                                sb.append("q2 ");
                            } else {
                                sb.append("feb ");
                            }
                            break;
                        case 2:
                            if (s.length == 4) {
                                sb.append("q3 ");
                            } else {
                                sb.append("mar ");
                            }
                            break;
                        case 3:
                            if (s.length == 4) {
                                sb.append("q4 ");
                            } else {
                                sb.append("apr ");
                            }
                            break;
                        case 4:
                            sb.append("may ");
                            break;
                        case 5:
                            sb.append("jun ");

                            break;
                        case 6:
                            sb.append("jul ");
                            break;
                        case 7:
                            sb.append("aug ");
                            break;
                        case 8:
                            sb.append("sep ");
                            break;
                        case 9:
                            sb.append("oct ");
                            break;
                        case 10:
                            sb.append("nov ");
                            break;
                        case 11:
                            sb.append("dec ");
                            break;
                        default: //iwie Fehler aufgetrten
                        }

                    break;
                case Group2:
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }

    public String getResult() {

        StringBuilder text = new StringBuilder();

        for (SpecificationPart spec : result.keySet()) {
            text.append(spec).append("\n{\n");

            LinkedHashMap<String, String> content = result.get(spec);

            for (String argument : content.keySet()) {
                text.append("\t").append(argument).append(" = ").append(content.get(argument)).append("\n");
            }

            text.append("}\n\n");
        }

        return text.toString();
    }

    public String[] getErrorList() {
        return errors.toArray(new String[errors.size()]);
    }

    public String[] getMessageList() {
        return messages.toArray(new String[messages.size()]);
    }

    private String separateSpan(TsPeriodSelector p, String specPart) {

        String result;

        switch (p.getType()) {
            case All:
                result = null;
                break;
            case From:
                result = "(" + p.getD0().getYear() + "." + (p.getD0().getMonth() + 1) + ", )";
                break;
            case To:
                result = "( ," + p.getD1().getYear() + "." + (p.getD1().getMonth() + 1) + ")";
                break;
            case Between:
                result = "(" + p.getD0().getYear() + "." + (p.getD0().getMonth() + 1) + " , " + p.getD1().getYear() + "." + p.getD1().getMonth() + ")";
                break;
            case Excluding:
            case First:
            case Last:
                result = null;
                errors.add(specPart + ": There is no transformation for the span type");
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    private TsData extractData(ITsVariable input) {

        ArrayList<DataBlock> data = new ArrayList();
        data.add(new DataBlock(input.getDefinitionDomain().getLength()));
        input.data(input.getDefinitionDomain(), data);
        return new TsData(input.getDefinitionDomain().getStart(), data.get(0));
    }
}
