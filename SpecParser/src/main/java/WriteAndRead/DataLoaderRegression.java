/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WriteAndRead;

import com.google.common.io.Files;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.openide.util.Exceptions;

/**
 *
 * @author Nina Gonschorreck
 */
public class DataLoaderRegression extends DataLoader {

    private ArrayList<String> regressorName = new ArrayList();
    private double[] values;

    private ArrayList<TsData> regressorsFromWebService = new ArrayList();

    @Override
    public void load(String data) {
        super.load(data);
        values = super.getValues();
    }

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

    @Override
    public void load(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String extension = Files.getFileExtension(file.toString());

            switch (extension.toLowerCase()) {
                case "rgr":
                    loadRgr(br);
                    break;
                case "dat":
                    values = super.loadDat(br);
                    break;
                case "ser":
                    values = super.loadSer(br);
                    break;
                default:
                    //File extension are not supported
                    messages = ("Dateiendung wird nicht unterstützt");
                    break;
            }
        } catch (FileNotFoundException ex) {
            //fehler
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadRgr(BufferedReader br) throws IOException {

        /*Idea from WinX12:
         load all values successiv
         split values to their regressor in method getRegressors()
         */
        ArrayList<String> rslt = new ArrayList();

        //evt. ist erste zeile Namen von regressoren
        String line;
        String[] single;
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("D", "E").trim();
            if (!line.isEmpty()) {
                single = line.split("\\s+");
                rslt.addAll(Arrays.asList(single));
            }
        }
        values = new double[rslt.size()];
        for (int i = 0; i < rslt.size(); i++) {
            values[i] = Double.parseDouble(rslt.get(i));
        }
    }

    public TsVariable[] getRegressors() {

        /**/
        if (regressorsFromWebService.isEmpty()) {
            if (values != null) {
                ArrayList<TsVariable> r = new ArrayList();
                if (regressorName.isEmpty()) {
                    regressorName.add("NoName");
                }
                int lengthOfRegressors = values.length / regressorName.size();
                double[] tmp = new double[lengthOfRegressors];
                TsData data;

                for (int regressor = 0; regressor < regressorName.size(); regressor++) {
                    for (int i = 0; i < lengthOfRegressors; i++) {
                        tmp[i] = values[i * regressorName.size() + regressor];
                    }

                    data = new TsData(getPeriod(), getStart().getYear(), getStart().getMonth(), tmp, true);
                    //Name und startwert pruefen
//                r.add(new DynamicTsVariable(regressorName.get(regressor), TsMoniker.createDynamicMoniker(), data));
                    r.add(new TsVariable(regressorName.get(regressor), data));
                }
                return r.toArray(new TsVariable[0]);
            }
            //Fehlermeldung: Keine Werte vorhanden
//            messages = "Keine Werte vorhanden";
            return null;
        } else {
            if (regressorName.size() == regressorsFromWebService.size()) {
                ArrayList<TsVariable> r = new ArrayList();
                for (int regressor = 0; regressor < regressorName.size(); regressor++) {
                    r.add(new TsVariable(regressorName.get(regressor), regressorsFromWebService.get(regressor)));
                }
                return r.toArray(new TsVariable[0]);
            } else {
                //Fehlermeldung: unterschiedliche Längen bei Namen und Daten
                messages = "Unterschiedliche Anzahl an Regressornamen und -daten";
                return null;
            }
        }
    }
}
