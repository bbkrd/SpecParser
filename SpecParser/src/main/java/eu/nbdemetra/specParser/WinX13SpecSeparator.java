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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 *
 * @author Nina Gonschorreck
 */
public class WinX13SpecSeparator {

//    unnuetze Zeilen? #
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
        StringBuilder sb = new StringBuilder();
        String[] allLines = winX13Text.split("\n");
        for (String line : allLines) {
            if (line.contains("#")) {
                line = line.substring(0, line.indexOf("#") - 1);
            }
            if (!line.replaceAll("\\s", "").isEmpty()) {
                sb.append(";").append(line);
            }
        }
        winX13Text = sb.toString();

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
                //3. split on line breaks
                lines = specPartSplitted[1].split(";");
//
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
    /*methods for each specification part*/
    /*methods for each argument*/
    /**
     * ***************************************************************************
     */
    public void read_mode(SpecificationPart partName, String content) {

        /*
         *   Select the correct DecompositionMode for JD+
         */
        content = content.replaceAll(" ", "");
        switch (content) {
            case "add":
                spec.getX11Specification().setMode(DecompositionMode.Additive);
                break;
            case "mult":
                spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
                break;
            case "loggadd":
                spec.getX11Specification().setMode(DecompositionMode.LogAdditive);
                break;
            case "pseudoadd":
                break;
            default:
                break;
        }
    }

    public void read_seasonalma(SpecificationPart partName, String content) {

        /*
         *   Select the correct seasonal filters for JD+
         */
        //Delete the brackets
        content = content.replaceAll("\\(", "");
        content = content.replaceAll("\\)", "");

        String[] filter = content.split(" ");

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
        content = content.replaceAll(" ", "");
        int t = Integer.parseInt(content);
        spec.getX11Specification().setHendersonFilterLength(t);
    }

    public void read_sigmalim(SpecificationPart partName, String content) {

        /*
         *   
         */
        content = content.replaceAll("\\(", "");
        content = content.replaceAll("\\)", "");
        content = content.trim();

        String[] tmp;
        if (content.contains(",")) {
            tmp = content.split(",");
        } else if (content.contains("\t")) {
            tmp = content.split("\t");
        } else {
            tmp = content.split(" ");
        }

        if (tmp[0].isEmpty()) {
            spec.getX11Specification().setSigma(1.5, Double.parseDouble(tmp[1]));
        } else {
            spec.getX11Specification().setSigma(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]));
        }
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
