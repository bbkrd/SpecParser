/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WriteAndRead;

import ec.tss.DynamicTsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Nina Gonschorreck
 */
public class DataLoaderRegression extends DataLoader {

    private ArrayList<String> regressorName = new ArrayList();
    private ArrayList<String> regressorZislId = new ArrayList();
//    private double[] values;

    private ArrayList<TsData> regressorsFromWebService = new ArrayList();

//    @Override
//    public void load(String data) {
//        super.load(data);
//        values = super.getValues();
//    }
    public void addRegFromWebServive(TsData data) {
        regressorsFromWebService.add(data);
    }

    public void setRegressorName(String regressorName) {
        this.regressorName.add(regressorName);
    }

    public void setRegressorName(String[] regressorNames) {
        regressorName.addAll(Arrays.asList(regressorNames));
    }

    public String[] getRegressorName() {
        return regressorName.toArray(new String[0]);
    }

    public void setRegressorZisl(String zisl) {
        this.regressorZislId.add(zisl);
    }

//     public String[] getRegressorZislIds() {
//        return regressorZislId.toArray(new String[0]);
//    }
//    @Override
//    public void load(File file) {
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            String extension = Files.getFileExtension(file.toString());
//
//            switch (extension.toLowerCase()) {
//                case "rgr":
//                    loadRgr(br);
//                    break;
//                case "dat":
//                case "txt":
//                    values = super.loadDat(br);
//                    break;
//                case "ser":
//                    values = super.loadSer(br);
//                    break;
//                default:
//                    //File extension are not supported
//                    messages = "File extension are not supported";
//                    break;
//            }
//        } catch (FileNotFoundException ex) {
//            //fehler
//            messages = "File not found";
//        } catch (IOException ex) {
////            Exceptions.printStackTrace(ex);
//            messages = "File is not readable";
//        }
//    }
//
//    private void loadRgr(BufferedReader br) throws IOException {
//
//        /*Idea from WinX12:
//         load all values successiv
//         split values to their regressor in method getRegressors()
//         */
//        ArrayList<String> rslt = new ArrayList();
//
//        //evt. ist erste zeile Namen von regressoren
//        String line;
//        String[] single;
//        while ((line = br.readLine()) != null) {
//            line = line.replaceAll("D", "E").trim();
//            if (!line.isEmpty()) {
//                single = line.split("\\s+");
//                rslt.addAll(Arrays.asList(single));
//            }
//        }
//        values = new double[rslt.size()];
//        for (int i = 0; i < rslt.size(); i++) {
//            values[i] = Double.parseDouble(rslt.get(i));
//        }
//    }
    public TsVariable[] getRegressors() {

        /**/
        if (regressorsFromWebService.isEmpty()) {
            if (super.getValues() == null) {
                super.generateValues();
            }

            if (super.getValues() != null) {
                ArrayList<TsVariable> r = new ArrayList();
                if (regressorName.isEmpty()) {
                    regressorName.add("NoName");
                }
                int lengthOfRegressors = super.getValues().length / regressorName.size();
                double[] tmp = new double[lengthOfRegressors];
                TsData data;

                for (int regressor = 0; regressor < regressorName.size(); regressor++) {
                    for (int i = 0; i < lengthOfRegressors; i++) {
                        tmp[i] = super.getValues()[i * regressorName.size() + regressor];
                    }
//month geht nicht wenn größer als 3
                    int p = getStart().getMonth();
                    if (getPeriod().equals(TsFrequency.Quarterly)) {
                        p = (p / 3);
                    }
                    data = new TsData(getPeriod(), getStart().getYear(), p, tmp, true);
                    //Name und startwert pruefen
//                r.add(new DynamicTsVariable(regressorName.get(regressor), TsMoniker.createDynamicMoniker(), data));
//                    r.add(new DynamicTsVariable(regressorName.get(regressor),getMoniker(), data));
                    r.add(new TsVariable(regressorName.get(regressor), data));

                }
                return r.toArray(new TsVariable[0]);
            }
            //Fehlermeldung: Keine Werte vorhanden
//            messages = "Keine Werte vorhanden";
            return null;
        } else {
            if (regressorName != null) {
                if (regressorName.size() == regressorsFromWebService.size()) {
                    ArrayList<TsVariable> r = new ArrayList();
                    for (int regressor = 0; regressor < regressorName.size(); regressor++) {
//                    r.add(new TsVariable(regressorName.get(regressor), regressorsFromWebService.get(regressor)));
                        r.add(new DynamicTsVariable(regressorZislId.get(regressor), getMoniker(), regressorsFromWebService.get(regressor)));

                    }
                    return r.toArray(new TsVariable[0]);
                } else {
                    //Fehlermeldung: unterschiedliche Längen bei Namen und Daten
                    messages = "Unterschiedliche Anzahl an Regressornamen und -daten " + " (Code:2101).";
                    return null;
                }
            }
        }
        return null;
    }
}
