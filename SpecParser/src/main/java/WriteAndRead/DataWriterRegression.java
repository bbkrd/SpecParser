/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WriteAndRead;

import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;

/**
 *
 * @author Nina Gonschorreck
 */
public class DataWriterRegression {

    private ArrayList<String> regressorNames = new ArrayList();
    private ArrayList<String[]> regressors = new ArrayList();

    private ArrayList<TsPeriod> starts = new ArrayList();
    private ArrayList<TsPeriod> ends = new ArrayList();

    private TsFrequency period = TsFrequency.Monthly;
    private TsPeriod tsStart;

    public void setTsStart(TsPeriod s) {
        tsStart = s;
    }

    public void setPeriod(TsFrequency p) {
        period = p;
    }

    public boolean addRegressorName(String s) {
        if (!regressorNames.contains(s)) {
            regressorNames.add(s);
            return true;
        }
        return false;
    }

    public void addRegValues(String[] values) {
        regressors.add(values);
    }

    public void addStart(TsPeriod start) {
        starts.add(start);
    }

    public void addEnd(TsPeriod end) {
        ends.add(end);
    }

    /**/
    public String getRegString() {
        //unbedingt testen mit verschieden langen regressoren

        if (!starts.isEmpty() || !ends.isEmpty()) {
            //ermittelt den am frühsten endenden Regressor
            TsPeriod minRegLength = ends.get(0);
            for (int i = 1; i < ends.size(); i++) {
                if (ends.get(i).isBefore(minRegLength)) {
                    minRegLength = ends.get(i);
                }
            }

            //sammelt die Indizies der Regressoren, bei denen die ZR beginnt
            ArrayList<Integer> indexOfStartTs = new ArrayList<>();
            for (TsPeriod p : starts) {
                indexOfStartTs.add(tsStart.minus(p));
            }
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            //#Werte auf die die Regressoren verkürzt werden
            int lengthForReg = minRegLength.minus(tsStart);
            //
            for (int i = 0; i < lengthForReg; i++) {
                for (String[] values : regressors) {
                    sb.append(values[indexOfStartTs.get(counter) + i]);
                    sb.append("\t\t");
                    counter++;
                }
                counter = 0;
                sb.append("\n");
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}
