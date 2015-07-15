/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WriteAndRead;

import com.google.common.io.Files;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.*;
import eu.nbdemetra.specParser.Miscellaneous.DateConverter;
import java.util.ArrayList;

/**
 *
 * @author Nina Gonschorreck
 */
public class DataLoader {

    private String errormessage = "";

    private TsFrequency period = TsFrequency.Monthly;
    private Day start = new Day(1970, Month.January, 0);
    private boolean startDefault = true;

    private double[] values;

//    private TsData rslt;
    public TsFrequency getPeriod() {
        return period;
    }
    
    public boolean isStartDefault(){
        return startDefault;
    }

    public Day getStart() {
        return start;
    }

    public void setStart(Day d) {
        start = d;
        startDefault=false;
    }

    public void setPeriod(TsFrequency p) {
        period = p;
    }

    public void load(String data) {

        String[] split = data.split("\\s+");

        values = new double[split.length];
        for (int i = 0; i < split.length; i++) {
            values[i] = Double.parseDouble(split[i]);
        }
//        rslt = ;
    }

    public void load(File file) {

        try {
            String extension = Files.getFileExtension(file.toString());

            switch (extension.toLowerCase()) {
                case "dat":
                    values = loadDat(new BufferedReader(new FileReader(file)));
//                    rslt = new TsData(new TsPeriod(period, start), loadDat(new BufferedReader(new FileReader(file))), false);
                    break;
                case "ser":
                    values = loadSer(new BufferedReader(new FileReader(file)));
//                    rslt = new TsData(new TsPeriod(period, start), loadSer(new BufferedReader(new FileReader(file))), false);
                    break;
                default:
                    errormessage = "File extension are not supported";
                    break;
            }

        } catch (FileNotFoundException ex) {
            errormessage = "File not found";
        } catch (IOException ex) {
            errormessage = "File is not readable";
        }
    }

    protected double[] loadDat(BufferedReader br) throws IOException {
        String line;
        ArrayList<String> v = new ArrayList();
        double[] values;
        String[] split;
        int periode = 0;
        int year = 10000; //

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {

                split = line.split("\\s+");
                if (year > Integer.parseInt(split[0])) {
                    year = Integer.parseInt(split[0]);
                    start = DateConverter.toJD(split[0] + "." + split[1]);
                    startDefault=false;
                }
                //calculate max period
                if (periode < Integer.parseInt(split[1])) {
                    periode = Integer.parseInt(split[1]);
                }
                //collect values
                v.add(split[2]);
            }
        }

        //calculate values
        values = new double[v.size()];
        for (int i = 0; i < v.size(); i++) {
            values[i] = Double.parseDouble(v.get(i));
        }

        if (periode <= 4) {
            period = TsFrequency.Quarterly;
        }
        return values;
    }

    protected double[] loadSer(BufferedReader br) throws IOException {
        String line;
        ArrayList<String> v = new ArrayList();
        double[] values;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                v.add(line);
            }
        }
        values = new double[v.size()];

        for (int i = 0; i < v.size(); i++) {
            values[i] = Double.parseDouble(v.get(i));
        }
        return values;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public TsData getData() {
        return new TsData(new TsPeriod(period, start), values, false);
    }
    
    public double[] getValues(){
        return values;
    }
}
