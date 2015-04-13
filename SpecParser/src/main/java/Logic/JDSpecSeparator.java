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
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import eu.nbdemetra.specParser.Miscellaneous.SpecificationPart;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class JDSpecSeparator {

    private TreeMap<SpecificationPart, TreeMap<String, String>> result = new TreeMap();
    private ArrayList<String> errors = new ArrayList();
    private ArrayList<String> messages = new ArrayList<String>();
    private Ts ts;
    private X13Specification spec;

    public JDSpecSeparator(X13Specification x13, Ts ts) {

        this.ts = ts;
        this.spec = x13;

        buildX11Spec();
        buildForecastSpec();
        buildSERIES();
//        RegArimaSpecification reg = x13.getRegArimaSpecification();

    }

    private void buildX11Spec() {

        X11Specification x11 = spec.getX11Specification();
        TreeMap<String, String> x11Result = new TreeMap();

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
    }

    private void buildSERIES() {

        TreeMap<String, String> series = new TreeMap();
        RegArimaSpecification reg = spec.getRegArimaSpecification();

        //1) start
        StringBuilder start = new StringBuilder(ts.getTsData().getStart().getYear() + ".");
        start.append(ts.getTsData().getStart().getPosition() + 1);
        series.put("start", start.toString());

        //2) span
        String span = separateSpan(reg.getBasic().getSpan(), "SERIES");
        if (span != null) {
            series.put("span", span);
        }

//        3) modelspan
        span = separateSpan(reg.getEstimate().getSpan(), "ESTIMATE");
        if (span != null) {
            series.put("modelspan", span);
        }
        
//        4) period
        series.put("period", ts.getTsData().getFrequency().intValue()+"");
        
//        5) data
        series.put("data","("+ ts.getTsData().getValues().toString()+")");

        result.put(SpecificationPart.SERIES, series);

    }

    private void buildForecastSpec() {

        X11Specification x11 = spec.getX11Specification();
        TreeMap<String, String> forecast = new TreeMap();
        //1) Maxlead
        int maxlead = x11.getForecastHorizon();
        if (maxlead < 0) {
            maxlead = -1 * maxlead * ts.getTsData().getFrequency().intValue();
        }
        forecast.put("maxlead", maxlead + "");
        result.put(SpecificationPart.FORECAST, forecast);

    }

    private void buildRegArima() {

        RegArimaSpecification reg = spec.getRegArimaSpecification();
        if (reg.isUsingAutoModel()) {
            //Automdl
        } else {
            //Arima
        }

        //ansonsten den rest aufrufen
    }

    public String getResult() {

        StringBuilder text = new StringBuilder();

        for (SpecificationPart spec : result.keySet()) {
            text.append(spec).append("{\n");

            TreeMap<String, String> content = result.get(spec);

            for (String argument : content.keySet()) {
                text.append("\t").append(argument).append(" = ").append(content.get(argument)).append("\n");
            }

            text.append("}\n");
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
                result = "(" + p.getD0().getYear() + "." + (p.getD0().getMonth()+1) + ", )";
                break;
            case To:
                result = "( ," + p.getD1().getYear() + "." + (p.getD1().getMonth()+1) + ")";
                break;
            case Between:
                result = "(" + p.getD0().getYear() + "." + (p.getD0().getMonth()+1) + " , " + p.getD1().getYear() + "." + p.getD1().getMonth() + ")";
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
