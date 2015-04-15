/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.Ts;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.AutoModelSpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import eu.nbdemetra.specParser.Miscellaneous.SpecificationPart;
import java.util.ArrayList;
import java.util.TreeMap;

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
    private TreeMap<SpecificationPart, TreeMap<String, String>> result = new TreeMap();
    private ArrayList<String> errors = new ArrayList();
    private ArrayList<String> messages = new ArrayList<String>();

    /*Constructor*/
    public JDSpecSeparator(X13Specification x13, Ts ts) {
//      get all information for translating 
        this.ts = ts;
        this.spec = x13;
    }

    public void build() {

        generateTs();

        generateBasicSpec();
        generateEstimateSpec();
        generateTransformSpec();

        if (spec.getRegArimaSpecification().getAutoModel().isEnabled()) {
            generateAutomdl();
        } else {
            generateArima();
        }
        generateOutlier();
        generateX11Spec();
    }

    private void generateArima() {

        ArimaSpec reg = spec.getRegArimaSpecification().getArima();
        TreeMap<String, String> arima;

        if (result.containsKey(SpecificationPart.ARIMA)) {
            arima = result.get(SpecificationPart.ARIMA);
        } else {
            arima = new TreeMap<>();
        }

//        1)model
        String model = "( " + reg.getP() + " " + reg.getD() + " " + reg.getQ() + " )( " + reg.getBP() + " " + reg.getBD() + " " + reg.getBQ() + " )";
        arima.put("model", model);

//        2)AR
        StringBuilder ar = null;
        if (reg.getP() > 0) {
            ar = new StringBuilder("( ");
            Parameter p;
            for (int i = 0; i < reg.getP(); i++) {
                p = reg.getPhi()[i];
                ar.append(p.getValue() * -1);
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
                ar.append(p.getValue() * -1);
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
                ma.append(p.getValue() * -1);
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
                ma.append(p.getValue() * -1);
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
        TreeMap<String, String> automdl;

        if (result.containsKey(SpecificationPart.AUTOMDL)) {
            automdl = result.get(SpecificationPart.AUTOMDL);
        } else {
            automdl = new TreeMap<>();
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
        TreeMap<String, String> series;

        if (result.containsKey(SpecificationPart.SERIES)) {
            series = result.get(SpecificationPart.SERIES);
        } else {
            series = new TreeMap<>();
        }

        String span = separateSpan(reg.getBasic().getSpan(), "SERIES");
        if (span != null) {
            series.put("span", span);
        }
        result.put(SpecificationPart.SERIES, series);
    }

    private void generateEstimateSpec() {

        RegArimaSpecification reg = spec.getRegArimaSpecification();

//        1)modelspan
        TreeMap<String, String> series;
        if (result.containsKey(SpecificationPart.SERIES)) {
            series = result.get(SpecificationPart.SERIES);
        } else {
            series = new TreeMap<>();
        }

        String span = separateSpan(reg.getEstimate().getSpan(), "ESTIMATE");
        if (span != null) {
            series.put("modelspan", span);
        }

        result.put(SpecificationPart.SERIES, series);

//        2)tol
        TreeMap<String, String> estimate;
        if (result.containsKey(SpecificationPart.ESTIMATE)) {
            estimate = result.get(SpecificationPart.ESTIMATE);
        } else {
            estimate = new TreeMap<>();
        }
        estimate.put("tol", reg.getEstimate().getTol() + "");

        result.put(SpecificationPart.ESTIMATE, estimate);
    }

    private void generateOutlier() {

        OutlierSpec reg = spec.getRegArimaSpecification().getOutliers();
        TreeMap<String, String> outlier;
        if (result.containsKey(SpecificationPart.OUTLIER)) {
            outlier = result.get(SpecificationPart.OUTLIER);
        } else {
            outlier = new TreeMap<>();
        }

//        1)span
        String span = separateSpan(reg.getSpan(), "OUTLIER");
        if (span != null) {
            outlier.put("span", span);
        }

//        2)LSRun
        outlier.put("lsrun", reg.getLSRun() + "");

        if (reg.isUsed()) {

//            1)critical value
            outlier.put("critical", reg.getDefaultCriticalValue() + "");

//            2)TC rate
            outlier.put("tcrate", reg.getMonthlyTCRate() + "");

//            3)Method
            outlier.put("method", reg.getMethod().toString());

//            4)types
            StringBuilder types;
            if (reg.getTypes() == null) {
                types = new StringBuilder("none");
            } else {
                types = new StringBuilder("( ");
                for (SingleOutlierSpec s : reg.getTypes()) {
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
        }

        result.put(SpecificationPart.OUTLIER, outlier);
    }

    private void generateTransformSpec() {

        TransformSpec t = spec.getRegArimaSpecification().getTransform();
        TreeMap<String, String> transform;
        if (result.containsKey(SpecificationPart.TRANSFORM)) {
            transform = result.get(SpecificationPart.TRANSFORM);
        } else {
            transform = new TreeMap<>();
        }

//        1)function
        transform.put("function", t.getFunction().name());

//        2)aic diff
        if (t.getFunction().equals(DefaultTransformationType.Auto)) {
            transform.put("aicdiff", t.getAICDiff() + "");
        }

//        3)adjust
        if (t.getFunction().equals(DefaultTransformationType.Log)) {
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
        }

        result.put(SpecificationPart.TRANSFORM, transform);
    }

    private void generateTs() {

        TreeMap<String, String> series;
        if (result.containsKey(SpecificationPart.SERIES)) {
            series = result.get(SpecificationPart.SERIES);
        } else {
            series = new TreeMap<>();
        }

        //1) start
        StringBuilder start = new StringBuilder(ts.getTsData().getStart().getYear() + ".");
        start.append(ts.getTsData().getStart().getPosition() + 1);
        series.put("start", start.toString());

//        2) period
        series.put("period", ts.getTsData().getFrequency().intValue() + "");

//        3) data
        series.put("data", "(" + ts.getTsData().getValues().toString() + ")");

//        4) title
        series.put("title", ts.getRawName());

        result.put(SpecificationPart.SERIES, series);

    }

    private void generateX11Spec() {

//        A)X11
        X11Specification x11 = spec.getX11Specification();
        TreeMap<String, String> x11Result;

        if (result.containsKey(SpecificationPart.X11)) {
            x11Result = result.get(SpecificationPart.X11);
        } else {
            x11Result = new TreeMap<>();
        }

        //1) Mode
        String mode;
        switch (x11.getMode()) {
            case Additive:
                mode = "add";
                break;
            case LogAdditive:
                mode = "logadd";
                break;
            case Multiplicative:
                mode = "mult";
                break;
            default:
                messages.add(SpecificationPart.X11 + ": No translation for mode. It will be set to default 'mode = mult'");
                mode = "mult";
                break;
        }

        x11Result.put("mode", mode);

        //2) Seasonalma
        if (x11.isSeasonal()) {

            StringBuilder seasonalma = new StringBuilder("( ");
            if (x11.getSeasonalFilters() != null) {
                for (SeasonalFilterOption s : x11.getSeasonalFilters()) {
                    seasonalma.append(s.toString()).append(" ");
                }
            } else {
                seasonalma.append(SeasonalFilterOption.Msr);
            }
            seasonalma.append(")");

            x11Result.put("seasonalma", seasonalma.toString());
        }

        //3) trendma
        if (!x11.isAutoHenderson()) {
            x11Result.put("trendma", x11.getHendersonFilterLength() + "");
        }

        //4) sigmalim
        x11Result.put("sigmalim", "(" + x11.getLowerSigma() + " , " + x11.getUpperSigma() + " )");

        result.put(SpecificationPart.X11, x11Result);

//        B) FORECAST
        TreeMap<String, String> forecast;
        if (result.containsKey(SpecificationPart.FORECAST)) {
            forecast = result.get(SpecificationPart.FORECAST);
        } else {
            forecast = new TreeMap<>();
        }
//        1) maxlead/forecast horizon
        int maxlead = x11.getForecastHorizon();
        if (maxlead < 0) {
            maxlead = -1 * maxlead * ts.getTsData().getFrequency().intValue();
        }
        forecast.put("maxlead", maxlead + "");

        result.put(SpecificationPart.FORECAST, forecast);
    }

    public String getResult() {

        StringBuilder text = new StringBuilder();

        for (SpecificationPart spec : result.keySet()) {
            text.append(spec).append("\n{\n");

            TreeMap<String, String> content = result.get(spec);

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

}
