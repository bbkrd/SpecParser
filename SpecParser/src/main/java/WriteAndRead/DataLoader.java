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
package WriteAndRead;

import ec.tss.TsMoniker;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import eu.nbdemetra.specParser.Miscellaneous.DateConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Nina Gonschorreck
 */
public class DataLoader {

    protected String messages = "";
    protected String format = "FREE";
    protected String fileInput = "";

    private TsFrequency period = TsFrequency.Monthly;
    private Day start = new Day(1970, Month.January, 0);
    private boolean startDefault = true;
    private TsData dataFromWebService;

    private TsMoniker moniker = null;
    private String zislId = null;

    private double[] values;

//    private TsData rslt;
    public TsFrequency getPeriod() {
        return period;
    }

    public TsMoniker getMoniker() {
        return moniker;
    }

    public void setZislId(String id) {
        zislId = id;
    }

    public String getZislId() {
        return zislId;
    }

    public void setMoniker(TsMoniker moniker) {
        this.moniker = moniker;
    }

    public int getNumberValues() {
        return values.length;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessage() {
        messages = "";
    }

    public boolean isStartDefault() {
        return startDefault;
    }

    public void changeStartDefault() {
        startDefault = false;
    }

    public Day getStart() {
        return start;
    }

    public void setStart(Day d) {
        changeStartDefault();
        start = d;
    }

    public void setPeriod(TsFrequency p) {
        period = p;
    }

    public void setFormat(String s) {
        format = s;
    }

    public void load(String data) {

        data = data.replaceAll("D", "E");
        String[] split = data.split("\\s+");

        values = new double[split.length];
        for (int i = 0; i < split.length; i++) {
            values[i] = Double.parseDouble(split[i]);
        }
    }

    public void setDataFromWebService(TsData data) {
        dataFromWebService = data;
        changeStartDefault();
        start = dataFromWebService.getStart().firstday();
    }

    public void load(File file) {

        try {
            String line;
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    sb.append(line).append(";");
                }
            }

            if (sb.length() != 0) {
                fileInput = sb.toString();
                //because regression variables
                fileInput = fileInput.replaceAll("D", "E");
            } else {
                messages = "File is empty" + " (Code:2001)";
                JOptionPane.showMessageDialog(null, messages);
            }

        } catch (FileNotFoundException ex) {
            messages = "File not found (Code:2007)";
            JOptionPane.showMessageDialog(null, messages);
            //eig error
        } catch (IOException ex) {
            //error
            messages = "File is not readable" + " (Code:2002)";
            JOptionPane.showMessageDialog(null, messages);
        }
    }

    protected double[] loadFreeFormat() {
        double[] val = null;
        try {

            if (!fileInput.isEmpty()) {
                String[] split = fileInput.split(";");

                val = new double[split.length];
                for (int i = 0; i < val.length; i++) {
                    val[i] = Double.parseDouble(split[i].trim());
                }

            } else {
                val = null;
            }

        } catch (NumberFormatException ex) {
            messages = "Format is not correct." + " (Code:2003)";

        }
        return val;
    }

    protected double[] loadDatevalueFormat() {

        //splin on linebreak
        String[] lineSplit = fileInput.split(";");
        String[] split;

        int periode = 0;
        int year = 10000;
        ArrayList<String> v = new ArrayList();

        double[] val = null;

        try {
            // each line will analyzed
            for (String line : lineSplit) {
                line = line.trim();
                if (!line.isEmpty()) {
                    split = line.split("\\s+");
                    if (year > Integer.parseInt(split[0])) {
                        year = Integer.parseInt(split[0]);
                        start = DateConverter.toJD(split[0] + "." + split[1], true);
                        changeStartDefault();
                    }
                    //calculate max period
                    if (periode < Integer.parseInt(split[1])) {
                        periode = Integer.parseInt(split[1]);
                    }
                    //collect values
                    for (int j = 2; j < split.length; j++) {
                        v.add(split[j]);
                    }
                }
            }
            //calculate values
            val = new double[v.size()];
            for (int i = 0; i < v.size(); i++) {
                val[i] = Double.parseDouble(v.get(i));
            }

            if (periode <= 4) {
                period = TsFrequency.Quarterly;
            }
        } catch (NumberFormatException ex) {
            messages = "Format is not correct. Try with free format" + " (Code:2004)";
            JOptionPane.showMessageDialog(null, messages);
            loadFreeFormat();
        }
        return val;
    }

    protected double[] loadX12SaveFormat() {
        double[] val = null;
        int year = 10000;
        int periode = 0;

        try {
            if (!fileInput.isEmpty()) {
                String[] lines = fileInput.split("\\n");

                val = new double[lines.length - 2];

                //first and second line will be ignored, because comments
                for (int i = 2; i > lines.length; i++) {
                    String[] split = lines[i].split("\\s+");

                    //first input is date
                    String y = split[0].substring(3);
                    String p = split[0].substring(4, 5);

                    if (year > Integer.parseInt(y)) {
                        year = Integer.parseInt(y);
                        start = DateConverter.toJD(y + "." + p, true);
                        changeStartDefault();
                    }
                    //calculate max period
                    if (periode < Integer.parseInt(p)) {
                        periode = Integer.parseInt(p);
                    }

                    //second input is value
                    val[i - 2] = Double.parseDouble(split[1].trim());
                }
            }
            if (periode <= 4) {
                period = TsFrequency.Quarterly;
            }
        } catch (NumberFormatException ex) {

            messages = "Format is not correct" + " (Code:2005)";
        }

        return val;
    }

    public TsData getData() {

        if (values == null) {
            generateValues();
        }

        if (dataFromWebService == null) {
            if (values != null) {
                switch (period) {
                    case Quarterly:
                        return new TsData(new TsPeriod(period, DateConverter.changeToQuarter(getStart())), values, false);
                    case Monthly:
                        return new TsData(new TsPeriod(period, start), values, false);
                    default:
                        return null;
                }
            } else {
                return null;
            }
        } else {
            return dataFromWebService;
        }
    }

    public double[] getValues() {
        if (values == null) {
            generateValues();
        }
        return values;
    }

    public boolean isDataFromWebserviceSet() {
        return dataFromWebService != null;
    }

    protected void generateValues() {

        if (!fileInput.isEmpty()) {
            switch (format.toUpperCase()) {
                case "FREE":
                    values = loadFreeFormat();
                    break;
                case "DATEVALUE":
                    values = loadDatevalueFormat();
                    break;

                case "X12Save":
                case "X13Save":
                    values = loadX12SaveFormat();
                    break;
                default:
                    messages = "No support for format " + format.toUpperCase() + " (Code:2006)";
                    values = null;
                    break;
            }
        }
    }
}
