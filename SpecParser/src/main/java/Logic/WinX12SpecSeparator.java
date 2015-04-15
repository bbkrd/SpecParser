/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import eu.nbdemetra.specParser.Miscellaneous.SpecificationPart;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x13.X13Specification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.X13Exception;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

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

    //no equivalence in a JD+ spec item, this information is given in Ts
    private TsFrequency period = TsFrequency.Monthly;
    private Day tsStart = new Day(1970, Month.January, 0);
    private TsData tsData = null;
    private String tsName = null;

    public WinX12SpecSeparator() {
        setDefaults();
    }

    public String[] getErrorList() {
        return errors.toArray(new String[errors.size()]);
    }
    
    public String[] getMessageList() {
        return messages.toArray(new String[messages.size()]);
    }

    public X13Document getResult() {
        X13Document x13 = new X13Document();
        x13.setSpecification(spec);
        return x13;
    }

    public Ts getTs() {
        return TsFactory.instance.createTs(tsName, null, tsData);
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
            if (!line.replaceAll("\\s", "").isEmpty()) {
                sb = new StringBuilder(";");
                line = sb.append(line).toString();
            }
            if (linebreak == true) {
                if (line.contains(")")) {
                    linebreak = false;
                    line = line.replaceAll(";", " ");
                }
                sb = new StringBuilder();
                line = sb.append(line).toString();
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

        //2. for each specification part split on "{" to separate name and content
        for (String item : specParts) {
            specPartSplitted = item.split("\\{");
            //seperate the specification part name
            specPartSplitted[0] = specPartSplitted[0].replaceAll(";", "");
            specPartSplitted[0] = specPartSplitted[0].replaceAll("\\s", "");
            try {
                specPartName = SpecificationPart.valueOf(specPartSplitted[0].toUpperCase());
                //all values for one argument in one line
                specPartSplitted[1] = specPartSplitted[1].replaceAll("\n", " ");
                //3. split on line breaks signed by ;
                lines = specPartSplitted[1].split(";");

//                //4. for each line split on "=" to separate arguments and values
                for (String tmp : lines) {
                    if (tmp.contains("=")) {
                        lineSplitted = tmp.split("=");
                        lineSplitted[0] = lineSplitted[0].replaceAll("\\s", "");
                        method = new StringBuilder("read_");
                        method.append(lineSplitted[0].toLowerCase());

                        try {
                            //5. try to invoke the method for the argument
                            m = this.getClass().getMethod(method.toString().toLowerCase(), SpecificationPart.class, String.class);
                            m.invoke(this, specPartName, lineSplitted[1]);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                            errors.add(specPartName.name() + ": No support for " + lineSplitted[0].toUpperCase());
                        }
                    }
                }
            } catch (IllegalArgumentException ex) {
                errors.add("No support for " + specPartSplitted[0].toUpperCase());
            }
        }
    }

    private void setDefaults() {

        //arima
        spec.getRegArimaSpecification().getArima().setP(0);
        spec.getRegArimaSpecification().getArima().setD(0);
        spec.getRegArimaSpecification().getArima().setQ(0);
        spec.getRegArimaSpecification().getArima().setBP(0);
        spec.getRegArimaSpecification().getArima().setBD(0);
        spec.getRegArimaSpecification().getArima().setBQ(0);

        //automdl
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

        //estimate
        spec.getRegArimaSpecification().getEstimate().setTol(1.0e-5);

        //outliers immer an?
        //critical value
        spec.getRegArimaSpecification().getOutliers().setLSRun(0);
        spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddOne);
        spec.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
        spec.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
        spec.getRegArimaSpecification().getOutliers().setMonthlyTCRate(0.7);

        //transform
        spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
        spec.getRegArimaSpecification().getTransform().setAICDiff(-2.0);
        //?
        spec.getRegArimaSpecification().getTransform().setAdjust(LengthOfPeriodType.None);

        //x11
        spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
        spec.getX11Specification().setSeasonal(false);
        spec.getX11Specification().setSigma(1.5, 2.5);
    }

    private Day calcDay(SpecificationPart partName, String day) {

        String[] split = day.split("\\.");

        int year = Integer.parseInt(split[0].trim());
        Day erg = new Day(1970, Month.January, 0);

        switch (period) {
            case Monthly:
                switch (split[1].trim().toUpperCase()) {
                    case "JAN":
                    case "01":
                    case "1":
                        erg = new Day(year, Month.January, 0);
                        break;
                    case "FEB":
                    case "02":
                    case "2":
                        erg = new Day(year, Month.February, 0);
                        break;
                    case "MAR":
                    case "03":
                    case "3":
                        erg = new Day(year, Month.March, 0);
                        break;
                    case "APR":
                    case "04":
                    case "4":
                        erg = new Day(year, Month.April, 0);
                        break;
                    case "MAY":
                    case "05":
                    case "5":
                        erg = new Day(year, Month.May, 0);
                        break;
                    case "JUN":
                    case "06":
                    case "6":
                        erg = new Day(year, Month.June, 0);
                        break;
                    case "JUL":
                    case "07":
                    case "7":
                        erg = new Day(year, Month.July, 0);
                        break;
                    case "AUG":
                    case "08":
                    case "8":
                        erg = new Day(year, Month.August, 0);
                        break;
                    case "SEP":
                    case "09":
                    case "9":
                        erg = new Day(year, Month.September, 0);
                        break;
                    case "OCT":
                    case "10":
                        erg = new Day(year, Month.October, 0);
                        break;
                    case "NOV":
                    case "11":
                        erg = new Day(year, Month.November, 0);
                        break;
                    case "DEC":
                    case "12":
                        erg = new Day(year, Month.December, 0);
                        break;
                    default:
                        errors.add(partName + ": Date format is not correct");
                        break;
                }
                break;
            case Quarterly:
                switch (split[1].trim().toUpperCase()) {
                    case "01":
                    case "1":
                        erg = new Day(year, Month.January, 0);
                        break;
                    case "02":
                    case "2":
                        erg = new Day(year, Month.April, 0);
                        break;
                    case "03":
                    case "3":
                        erg = new Day(year, Month.July, 0);
                        break;
                    case "04":
                    case "4":
                        erg = new Day(year, Month.October, 0);
                        break;
                    default:
                        errors.add(partName + ": Date format is not correct");
                        break;
                }
        }
        return erg;
    }

    private Day toQuarterDay(Day month, String part, String detail) {

        int quarter = month.getMonth();
        Day erg;

        switch (quarter) {
            //Recalculation from month to quarter
            //i.e. the third quarter is defined, calculate to march and than to the beginning of the third quarter
            //1970.3 -> 1970-03-01 -> month=2 -> 1970-07-01
            case 0:
                //yyyy.1
                erg = new Day(month.getYear(), Month.January, 0);
                break;
            case 1:
                //yyyy.2
                erg = new Day(month.getYear(), Month.April, 0);
                break;
            case 2:
                //yyyy.3
                erg = new Day(month.getYear(), Month.July, 0);
                break;
            case 3:
                //yyyy.4
                erg = new Day(month.getYear(), Month.October, 0);
                break;
            default:
                errors.add(part + ": " + detail + " format is not valid");
                erg = month;
                break;
        }
        return erg;
    }

    /* The following methods have to be public, 
     because the invoke procedure doesn't work with private. */
    /*methods for each argument*/
    /**
     * ***************************************************************************
     */
    public void read_acceptdefault(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setAcceptDefault(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setAcceptDefault(false);
                break;
            default:
                errors.add(partName + ": Wrong value for acceptdefault");
                break;
        }
    }

    public void read_adjust(SpecificationPart partName, String content) {

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
                errors.add(partName + ": No support for " + content + " in adjust");
                spec.getRegArimaSpecification().getTransform().setAdjust(LengthOfPeriodType.None);
                break;
        }
    }

    public void read_aicdiff(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getTransform().setAICDiff(value);
        } catch (NumberFormatException e) {
            errors.add(partName + ": Wrong Format for " + content + " in aicdiff");
        }
    }

    public void read_ar(SpecificationPart partName, String content) {

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
            errors.add(partName + ": The number of values for AR is not conform to the model.");
        }
    }

    public void read_armalimit(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setArmaSignificance(value);
        } catch (NumberFormatException ex) {
            errors.add(partName + ": Wrong format for armalimit");
        } catch (X13Exception e) {
            errors.add(e.toString());
        }
    }

    public void read_balanced(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setBalanced(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setBalanced(false);
                break;
            default:
                errors.add(partName + ": Wrong value for balanced");
                break;
        }
    }

    public void read_checkmu(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setCheckMu(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setCheckMu(false);
                break;
            default:
                errors.add(partName + ": Wrong value for checkmu");
                break;
        }
    }

    public void read_critical(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        String[] s = content.replaceAll("\\(", "").replaceAll("\\)", "").split(",");

        if (s.length > 1) {
            errors.add(partName + ": No support for more than one critical value , critical value is set to " + s[0]);
        }
        try {
            spec.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(Double.parseDouble(s[0]));
        } catch (NumberFormatException e) {
            errors.add(partName + ": Wrong format for critical value");
        }
    }

    public void read_data(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").replaceAll("\\(", "").replaceAll("\\)", "").trim();
        String[] split = content.split("\\s+");

        double[] values = new double[split.length];
        for (int i = 0; i < split.length; i++) {
            values[i] = Double.parseDouble(split[i]);
        }

        switch (partName) {
            case SERIES:
                tsData = new TsData(new TsPeriod(period, tsStart), values, false);
                break;
            case REGRESSION:
                errors.add(partName + ": To load data for regression is not possible at the moment");
                //noch nix, regression braucht auch start
                break;
            default: //Fehler
                errors.add(partName + ": To load data is not implemented");
                break;
        }
    }

    public void read_format(SpecificationPart partName, String content) {
        errors.add(partName + ": Format isn't supported");
    }

    public void read_file(SpecificationPart partName, String content) {
        /*
         Load data from a file
         possible endings are .dat or .ser
         */

        content = content.replaceAll("[;'\"]", "").trim();
        ArrayList<String> v = new ArrayList();
        double[] values;

        if (content.toLowerCase().endsWith(".ser")) {
            /*SER*/
            File file = new File(content);

            try {
                FileReader f = new FileReader(file);

                try (BufferedReader br = new BufferedReader(f)) {
                    String zeile;
                    while ((zeile = br.readLine()) != null) {
                        v.add(zeile);
                    }
                    values = new double[v.size()];

                    for (int i = 0; i < v.size(); i++) {
                        values[i] = Double.parseDouble(v.get(i));
                    }
                    if (partName == SpecificationPart.SERIES) {
                        tsData = new TsData(new TsPeriod(period, tsStart), values, false);
                    } else {
                        errors.add(partName + ": To load data is not possible");
                    }
                }

            } catch (FileNotFoundException ex) {
                errors.add(partName + ": File " + content + " not found");
            } catch (IOException ex) {
                errors.add(partName + ": Error loading file");
            }

        } else if (content.toLowerCase().endsWith(".dat")) {
            /*DAT*/
            File file = new File(content);
            try {
                FileReader f = new FileReader(file);

                try (BufferedReader br = new BufferedReader(f)) {
                    String zeile;
                    String[] split;
                    int periode = 0;
                    int year = 10000; //

                    while ((zeile = br.readLine()) != null) {
                        split = zeile.split("\\s+");
                        if (year > Integer.parseInt(split[0])) {
                            year = Integer.parseInt(split[0]);
                            tsStart = calcDay(partName, split[0] + "." + split[1]);
                        }
                        //calculate max period
                        if (periode < Integer.parseInt(split[1])) {
                            periode = Integer.parseInt(split[1]);
                        }
                        //collect values
                        v.add(split[2]);
                    }
                    //set period, when it is not Monthly data
                    if (periode <= 4) {
                        period = TsFrequency.Quarterly;
                    }

                    //calculate values
                    values = new double[v.size()];
                    for (int i = 0; i < v.size(); i++) {
                        values[i] = Double.parseDouble(v.get(i));
                    }

                    if (partName == SpecificationPart.SERIES) {
                        tsData = new TsData(new TsPeriod(period, tsStart), values, false);
                    } else {
                        errors.add(partName + ": To load data is not possible");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    errors.add(partName + ": Format of dat-file is not correct ");
                }

            } catch (FileNotFoundException ex) {
                errors.add(partName + ": File " + content + " not found");
            } catch (IOException ex) {
                errors.add(partName + ": Error loading file");
            }

        } else {
            errors.add(partName + ": Loading data from file " + content + " is not possible");
        }
    }

    public void read_function(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        switch (content.toUpperCase()) {
            case "LOG":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
                break;
            case "NONE":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
                break;
            case "AUTO":
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Auto);
                break;
            default:
                errors.add(partName + ": No support for " + content + " in function");
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
                break;
        }
    }

    public void read_hrinitial(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setHannanRissanen(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setHannanRissanen(false);
                break;
            default:
                errors.add(partName + ": Wrong value for balanced");
                break;
        }
    }

    public void read_ljungboxlimit(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setLjungBoxLimit(value);
        } catch (NumberFormatException ex) {
            errors.add(partName + ": Wrong format for ljungboxlimit");
        }
    }

    public void read_lsrun(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        try {
            int value = Integer.parseInt(content);
            spec.getRegArimaSpecification().getOutliers().setLSRun(value);
        } catch (NumberFormatException e) {
            errors.add(partName + ": Wrong format for lsrun");
        }
    }

    public void read_ma(SpecificationPart partName, String content) {
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
            errors.add(partName + ": The number of values for MA is not conform to the model.");
        }
    }

    public void read_maxlead(SpecificationPart partName, String content) {

        content = content.trim();

        try {
            int forecast = Integer.parseInt(content);
            spec.getX11Specification().setForecastHorizon(forecast);

//            if (forecast % period.value != 0) {
//                errors.add(partName + ": In JD+ only integers for forecasts horizon allowed. Please correct this manually in the JD+ specification");
//            }
//
//            //rounded down when result is no integer, parse in integer
//            int div = forecast / period.value * (-1);
//            spec.getX11Specification().setForecastHorizon(div);
        } catch (NumberFormatException e) {
            errors.add(partName + ": Wrong format for MAXLEAD");
        }

    }

    public void read_method(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        switch (content.toUpperCase()) {
            case "ADDONE":
                spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddOne);
                break;
            case "ADDALL":
                errors.add(partName + ": No support for value " + content.toUpperCase());
//                spec.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddAll);
                break;
            default:
                errors.add(partName + ": Wrong value for method ");
                break;
        }
    }

    public void read_mixed(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim().toUpperCase();

        switch (content) {
            case "YES":
                spec.getRegArimaSpecification().getAutoModel().setMixed(true);
                break;
            case "NO":
                spec.getRegArimaSpecification().getAutoModel().setMixed(false);
                break;
            default:
                errors.add(partName + ": Wrong value for mixed");
                break;
        }
    }

    public void read_mode(SpecificationPart partName, String content) {

        /*
         *   Select the correct DecompositionMode for JD+
         */
        content = content.replaceAll(";", "").trim();
        content = content.replaceAll(" ", "");
        switch (content.toLowerCase()) {
            case "add":
                spec.getX11Specification().setMode(DecompositionMode.Additive);
                break;
            case "mult":
                spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                break;
            case "logadd":
                spec.getX11Specification().setMode(DecompositionMode.LogAdditive);
                break;
            case "pseudoadd":
                errors.add(partName + ": No Support for value " + content.toUpperCase() + " in mode");
                break;
            default:
                errors.add(partName + ": No Support for value " + content.toUpperCase() + " in mode ");
                break;
        }
    }

    public void read_model(SpecificationPart partName, String content) {

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

        //1. Split on ")(" with or without spaces
        content = content.replaceAll(";", "").trim();
        //invalid format for ARIMA model: (...)(...)3(...)
        String[] match = content.split("\\d+\\(");
        if (match.length > 1) {
            errors.add(partName + ": No support for an ARIMA model like " + content);
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
                    if (period != null) {
                        if (!match[1].trim().equals(period.intValue() + "")) {
                            errors.add(partName + ": Periods are not conform.");
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
                    errors.add(partName + ": Model is not correct");
                } catch (X13Exception e) {
                    errors.add(partName + ": Parameters for model are not correct");
                }
            }
        }
    }

    public void read_modelspan(SpecificationPart partName, String content) {

        read_span(SpecificationPart.ESTIMATE, content);

    }

    public void read_name(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        switch (partName) {
            case SERIES:
                tsName = content;
                break;
            case REGRESSION:
            default:
                errors.add(partName + ": not possible to name the data");
                break;
        }
    }

    public void read_period(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        try {
            int p = Integer.parseInt(content);
            switch (p) {
                case 12:
                    if (period != TsFrequency.Monthly) {
                        period = TsFrequency.Monthly;
                    }
                    break;
                case 4:
                    //change period
                    period = TsFrequency.Quarterly;

                    //change start date
                    tsStart = toQuarterDay(tsStart, "SERIES", "start date");

                    //change data object
                    if (tsData != null) {
                        tsData = new TsData(new TsPeriod(period, tsStart), tsData.getValues().internalStorage(), false);
                    }

                    //change span
                    TsPeriodSelector sel;
                    if (spec.getRegArimaSpecification().getBasic() != null && spec.getRegArimaSpecification().getBasic().getSpan() != null) {
                        sel = spec.getRegArimaSpecification().getBasic().getSpan();
                        if (sel.getD0() != null) {
                            sel.setD0(toQuarterDay(sel.getD0(), "SERIES", "span start date"));
                        }
                        if (sel.getD1() != null) {
                            sel.setD1(toQuarterDay(sel.getD1(), "SERIES", "span ending date"));
                        }
                        spec.getRegArimaSpecification().getBasic().setSpan(sel);
                    }
                    //change modelspan
                    if (spec.getRegArimaSpecification().getEstimate() != null && spec.getRegArimaSpecification().getEstimate().getSpan() != null) {
                        sel = spec.getRegArimaSpecification().getEstimate().getSpan();
                        if (sel.getD0() != null) {
                            sel.setD0(toQuarterDay(sel.getD0(), "SERIES", "modelspan start date"));
                        }
                        if (sel.getD1() != null) {
                            sel.setD1(toQuarterDay(sel.getD1(), "SERIES", "model span ending date"));
                        }
                        spec.getRegArimaSpecification().getEstimate().setSpan(sel);
                    }

                    break;
                default:
                    errors.add(partName + ": Period " + p + " are not possible. Set to default 12");
                    break;
            }
        } catch (NumberFormatException e) {
            errors.add(partName + ": Wrong format for PERIOD");
        }
    }

    public void read_power(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            if (value == 0.0) {
                spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
            }
        } catch (NumberFormatException e) {
            errors.add(partName + ": No support for power = " + content);
        }
    }

    public void read_reducecv(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setPercentReductionCV(value);
        } catch (NumberFormatException ex) {
            errors.add(partName + ": Wrong format for reducecv");
        } catch (X13Exception e) {
            errors.add(e.getMessage());
        }
    }

    public void read_seasonalma(SpecificationPart partName, String content) {

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
                        errors.add(partName + ": No Support for value " + content);
                        tmp.add(null);
                        break;
                }
            }
        }
        if (tmp.size() == 1 || tmp.size() == period.intValue()) {
            spec.getX11Specification().setSeasonalFilters((SeasonalFilterOption[]) tmp.toArray(new SeasonalFilterOption[tmp.size()]));
        } else {
            errors.add(partName + ": Periods are not conform. Sesonal filter is set to " + tmp.get(0));
            spec.getX11Specification().setSeasonalFilter(tmp.get(0));
        }
    }

    public void read_sigmalim(SpecificationPart partName, String content) {

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
            errors.add(partName + ": " + content + " is no correct format for the sigma argument");
        }
    }

    public void read_span(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();
        String s = content.replaceAll("\\(", " ");
        s = s.replaceAll("\\)", " ");
        String[] split = s.split(",");

        if (split.length == 2) {

            TsPeriodSelector p = new TsPeriodSelector();
            if (split[0].replaceAll("\\s+", "").isEmpty()) {
                if (split[1].replaceAll("\\s+", "").isEmpty()) {
                    //Fehler im Format
                    errors.add(partName + ": In span are two missing values");
                } else {
                    //to
                    p.setType(PeriodSelectorType.To);
                    p.to(calcDay(partName, split[1]));
                }
            } else {
                if (split[1].replaceAll("\\s+", "").isEmpty()) {
                    //from
                    p.setType(PeriodSelectorType.From);
                    p.from(calcDay(partName, split[0]));

                } else {
                    //between
                    p.setType(PeriodSelectorType.Between);
                    p.between(calcDay(partName, split[0]), calcDay(partName, split[1]));
                }
            }

            switch (partName.toString().toUpperCase()) {
                case "OUTLIERS":
                    spec.getRegArimaSpecification().getOutliers().setSpan(p);
                    break;
                case "ESTIMATE":
                    spec.getRegArimaSpecification().getEstimate().setSpan(p);
                    break;
                case "SERIES":
                    spec.getRegArimaSpecification().getBasic().setSpan(p);
                    break;
                default:
                    errors.add(partName + ": No support for span");
                    break;
            }
        } else {
            //Fehler
            errors.add((String) (partName == SpecificationPart.ESTIMATE ? SpecificationPart.SERIES : partName + ": Wrong format for span"));
        }
    }

    public void read_start(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        switch (partName) {
            case SERIES:
                tsStart = calcDay(partName, content);
                break;
            case REGRESSION: //auch eine variable anlegen
            // break;
            default:
                errors.add(partName + ": start argument is not implemented");
                break;
        }

    }

    public void read_tcrate(SpecificationPart partName, String content) {

        String s = content.replaceAll(";", "").trim();
        try {
            double value = Double.parseDouble(s);
            spec.getRegArimaSpecification().getOutliers().setMonthlyTCRate(value);
        } catch (NumberFormatException e) {
            errors.add(partName + ": Wrong format for tcrate");
        }
    }

    public void read_tol(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getEstimate().setTol(value);
        } catch (NumberFormatException ex) {
            errors.add(partName + ": Wrong format for tol");
        } catch (X13Exception e) {
            errors.add(e.getMessage());
        }
    }

    public void read_trendma(SpecificationPart partName, String content) {

        /*
         *   Set the correct length of Hendersonfilter for JD+
         */
        content = content.replaceAll(";", "").trim();
        content = content.replaceAll(" ", "");
        try {
            int t = Integer.parseInt(content);
            spec.getX11Specification().setHendersonFilterLength(t);
        } catch (NumberFormatException ex) {
            errors.add(partName + ": " + content + " isn't a correct argument for HendersonFilter");
        }
    }

    public void read_types(SpecificationPart partName, String content) {

        spec.getRegArimaSpecification().getOutliers().clearTypes();

        String s = content.replaceAll(";", "").trim();

        s = s.replaceAll("\\(", "").replaceAll("\\)", "").trim();
        String[] split = s.split("\\s+");

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
                    errors.add(partName + ": No support for " + t + "in types");
                    break;
            }
        }
//            spec.getRegArimaSpecification().getOutliers().setTypes((SingleOutlierSpec[]) value.toArray());

    }

    public void read_urfinal(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setUnitRootLimit(value);
        } catch (NumberFormatException ex) {
            errors.add(partName + ": Wrong format for urfinal");
        } catch (X13Exception e) {
            errors.add(e.getMessage());
        }
    }

    /*
     * empty methods
     *
     *   argument is not supported, but it is not an error
     */
    public void read_decimals(SpecificationPart partName, String content) {
    }

    public void read_precision(SpecificationPart partName, String content) {
    }

    public void read_print(SpecificationPart partName, String content) {
    }

    public void read_save(SpecificationPart partName, String content) {
    }

    public void read_savelog(SpecificationPart partName, String content) {
    }

    public void read_title(SpecificationPart partName, String content) {
    }

    public void read_type(SpecificationPart partName, String content) {
    }
}
