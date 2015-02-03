/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.arima.x13.X13Exception;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 *
 * @author Nina Gonschorreck
 */
public class WinX13SpecSeparator {

    private X13Specification spec = new X13Specification();
    private ArrayList<String> errors = new ArrayList();

    public String[] getErrorList() {
        return errors.toArray(new String[errors.size()]);
    }

    public X13Document getResult() {
        X13Document x13 = new X13Document();
        x13.setSpecification(spec);
        return x13;
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
                sb=new StringBuilder(";");
                line = sb.append(line).toString();
            }
            if(linebreak==true){
                if(line.contains(")")){
                    linebreak=false;
                    line=line.replaceAll(";", " ");
                }
                sb=new StringBuilder();
                line = sb.append(line).toString();
            }
            if(line.contains("(")){
                if(!line.contains(")")){
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
                specPartSplitted[1]=specPartSplitted[1].replaceAll("\n", " ");
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
                            errors.add("No support for " + lineSplitted[0] + " in " + specPartName.name());
                        }
                    }
                }
            } catch (IllegalArgumentException ex) {
                errors.add("No support for " + specPartSplitted[0]);
            }
        }

    }

    /* The following methods have to be public, 
     because the invoke procedure doesn't work with private. */

    /*methods for each argument*/
    /**
     * ***************************************************************************
     */
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
                errors.add("No Support for value " + content + " for mode in " + partName.name());
                break;
            default:
                errors.add("No Support for value " + content + " for mode in " + partName.name());
                break;
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
                        errors.add("No Support for value " + content + " in " + partName.name());
                        tmp.add(null);
                        break;
                }
            }
        }
        spec.getX11Specification().setSeasonalFilters((SeasonalFilterOption[]) tmp.toArray(new SeasonalFilterOption[tmp.size()]));
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
            errors.add(content + " isn't a correct argument for HendersonFilter in " + partName.name());
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
            errors.add(content + " is no correct format for the sigma argument in " + partName.name());
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

//        4. Set assigned parameters on the correct position in the vector
        if (phi != null) {
            for (Parameter p : phi) {
                if (p.getValue() == 0.1) {
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
                if (p.getValue() == 0.1) {
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

//        4. Set assigned parameters on the correct position in the vector
        if (theta != null) {
            for (Parameter q : theta) {
                if (q.getValue() == 0.1) {
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
                if (q.getValue() == 0.1) {
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
//        0. set no model
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
            errors.add("No support for an ARIMA model like "+content);
        } else{

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
                if ((i + 1) == 2 || match.length == 2) {
                    sarima = true;
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
                        p_para[j] = new Parameter(0.1, ParameterType.Undefined);
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
                        p_para[Integer.parseInt(a) - 1] = new Parameter(0.1, ParameterType.Undefined);
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
                        q_para[j] = new Parameter(0.1, ParameterType.Undefined);
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
                        q_para[Integer.parseInt(a) - 1] = new Parameter(0.1, ParameterType.Undefined);
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
                    errors.add("Model in ARIMA is not correct");
                } catch (X13Exception e) {
                    errors.add("Parameters for model in ARIMA are not correct");
                }
            }
        }
    }

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
                errors.add("Wrong value for acceptdefault in " + partName);
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
                errors.add("Wrong value for checkmu in " + partName);
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
                errors.add("Wrong value for checkmu in " + partName);
                break;
        }
    }

    public void read_ljungboxlimit(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setLjungBoxLimit(value);
        } catch (NumberFormatException ex) {
            errors.add("Wrong format for ljungboxlimit in " + partName);
        }
    }

    public void read_armalimit(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setArmaSignificance(value);
        } catch (NumberFormatException ex) {
            errors.add("Wrong format for armalimit in " + partName);
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
                errors.add("Wrong value for balanced in " + partName);
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
                errors.add("Wrong value for balanced in " + partName);
                break;
        }
    }

    public void read_reducecv(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setPercentReductionCV(value);
        } catch (NumberFormatException ex) {
            errors.add("Wrong format for reducecv in " + partName);
        } catch (X13Exception e) {
            errors.add(e.getMessage());
        }
    }

    public void read_urfinal(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getAutoModel().setUnitRootLimit(value);
        } catch (NumberFormatException ex) {
            errors.add("Wrong format for urfinal in " + partName);
        } catch (X13Exception e) {
            errors.add(e.getMessage());
        }
    }

    public void read_tol(SpecificationPart partName, String content) {

        content = content.replaceAll(";", "").trim();

        try {
            double value = Double.parseDouble(content);
            spec.getRegArimaSpecification().getEstimate().setTol(value);
        } catch (NumberFormatException ex) {
            errors.add("Wrong format for tol in " + partName);
        } catch (X13Exception e) {
            errors.add(e.getMessage());
        }
    }

    /*
     * empty methods
     *
     *   argument is not supported, but it is not an error
     */
    public void read_title(SpecificationPart partName, String content) {
    }

    public void read_save(SpecificationPart partName, String content) {
    }

    public void read_savelog(SpecificationPart partName, String content) {
    }

    public void read_print(SpecificationPart partName, String content) {
    }

    public void read_type(SpecificationPart partName, String content) {
    }

    //This method print the x11 specification to controll your results.
    public void x11ToString() {

        X11Specification x11 = spec.getX11Specification();

        StringBuilder sb = new StringBuilder("X11\n"
                + "###\n");

        sb.append("Mode = ").append(x11.getMode()).append("\n");

        sb.append("Seasonal = ( ");
        if (x11.getSeasonalFilters() != null) {

            for (SeasonalFilterOption item : x11.getSeasonalFilters()) {
                sb.append(item).append(" ");
            }

        }
        sb.append(")\n");

        sb.append("Trend = ").append(x11.getHendersonFilterLength()).append("\n");

        sb.append("Sigma = (").append(x11.getLowerSigma()).append(" ").append(x11.getUpperSigma()).append(")");

        System.out.println(sb.toString());

    }
}
