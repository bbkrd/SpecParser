/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WriteAndRead;

import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.timeseries.simplets.TsDomain;
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
    
    public void addStart(TsPeriod start){
        starts.add(start);
    }

    public void addEnd(TsPeriod end){
        ends.add(end);
    }
//    public void getRegressors(TsDomain period, DecompositionMode mode) {
//        //hier nach Treffen mit Susanne weiterarbeiten
//
//        if (regressorNames.size() > 0) {
//            TsPeriod min = starts.get(0);
//            TsPeriod max = ends.get(0);
//
//            for (int i = 1; i < starts.size(); i++) {
//                if (min.isAfter(starts.get(i))) {
//                    min=starts.get(i);
//                }
//                if(max.isBefore(ends.get(i))){
//                    max = ends.get(i);
//                }
//            }
//            
//            int length = max.minus(min);
//            double[][] reg = new double[regressorNames.size()][length];
//            
//            double defaultValue = 0.0;
//            switch(mode){
//                case Additive:
//                case LogAdditive:
//                    //mit 0er füllen
//                    defaultValue = 0.0;
//                    break;
//                case Multiplicative:
//                    //mit 1er füllen
//                    defaultValue = 1.0;
//                    break;
//                case Undefined: 
//                    //Problem
//                    break;
//                default: 
//                    //kein decomposition mode
//                    break;
//            }
//            
//            for(int r =0; r<regressorNames.size(); r++ ){
//                for(int i = 0; i<length; i++){
//                    reg[r][i]=defaultValue;
//                }
//            }
//            
//            
//            
//        }
//    }
}
