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
import eu.nbdemetra.specParser.Miscellaneous.TranslationInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Nina Gonschorreck
 */
public class DataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    protected HashMap<String, TranslationInfo> infos;
    protected String format = "FREE";
    protected String fileInput = "";

    private TsFrequency period = TsFrequency.Monthly;
    private Day start = new Day(1970, Month.January, 0);
    private boolean startDefault = true;
    private TsData dataFromWebService;

    private TsMoniker moniker = null;
    private String zislId = null;

    private double[] values;

    public DataLoader(HashMap<String, TranslationInfo> map) {
        if (infos == null) {
            infos = map;
        }
    }

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

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder sb = new StringBuilder();
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
                infos.put("File empty"
                        + ". (Code:2001)", 
                        TranslationInfo.ERROR);
            }

        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.toString());
            infos.put("File not found"
                    + ". (Code:2007)", 
                    TranslationInfo.ERROR);
        } catch (IOException ex) {
            LOGGER.error(ex.toString());
            infos.put("File not readable"
                    + ". (Code:2002)", 
                    TranslationInfo.ERROR);
        }
    }

    protected double[] loadFreeFormat() {
        ArrayList<Double> val = new ArrayList<>();

        if (!fileInput.isEmpty()) {
            String[] split = fileInput.split(";");

            for (String line : split) {
                line = line.trim();
                String[] singleValue = line.split("\\s+");
                for (String s : singleValue) {
                    try {
                        val.add(Double.parseDouble(s));
                    } catch (NumberFormatException e) {
                        LOGGER.error(e.toString());
                        infos.put("Incorrect data format"
                                + ". (Code:2003)", 
                                TranslationInfo.ERROR);
                    }
                }
            }
        }

        // kleiner double
        double[] val_double = new double[val.size()];
        for (int i = 0; i < val.size(); i++) {
            val_double[i] = val.get(i);
        }
        return val_double;
//        return ArrayUtils.toPrimitive();
    }

    protected double[] loadDatevalueFormat() {

        //splin on linebreak
        String[] lineSplit = fileInput.split(";");
        String[] split;

        int periode = 0;
        int year = Integer.MAX_VALUE;//10000;
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
                        setStart(DateConverter.toJD(split[0] + "." + split[1], true));
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
            LOGGER.error(ex.toString());
            infos.put("Incorrect data format. "
                    + "Free format is used instead"
                    + ". (Code:2004)", 
                    TranslationInfo.WARNING2);
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
            LOGGER.error(ex.toString());
            infos.put("Incorrect data format"
                    + ". (Code:2005)", 
                    TranslationInfo.ERROR);
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
                default:
                    infos.put("Value " + format.toUpperCase() + " in argument FORMAT not supported. (Code:2006)", TranslationInfo.ERROR);
                    values = null;
                    break;
            }
        }
    }
}
