/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.nbdemetra.specParser.Miscellaneous;

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.*;

/**
 *
 * @author Nina Gonschorreck
 */
public class DateConverter {
    
    
    public static Day toJD(String day){
        return toJD(day, Monthly);
    }
    
    public static Day toJD(String day, TsFrequency period){
                
       String[] split = day.split("\\.");

        int year = Integer.parseInt(split[0].trim());
        Day erg = null;

        switch (period) {
            case Monthly:
                DateConverterMonth m = DateConverterMonth.getMonth(split[1].trim());
                switch (m) {
                     
                    case JAN:
                        erg = new Day(year, Month.January, 0);
                        break;
                    case FEB:
                        erg = new Day(year, Month.February, 0);
                        break;
                    case MAR:
                        erg = new Day(year, Month.March, 0);
                        break;
                    case APR:
                        erg = new Day(year, Month.April, 0);
                        break;
                    case MAY:
                        erg = new Day(year, Month.May, 0);
                        break;
                    case JUN:
                        erg = new Day(year, Month.June, 0);
                        break;
                    case JUL:
                        erg = new Day(year, Month.July, 0);
                        break;
                    case AUG:
                        erg = new Day(year, Month.August, 0);
                        break;
                    case SEP:
                        erg = new Day(year, Month.September, 0);
                        break;
                    case OCT:
                        erg = new Day(year, Month.October, 0);
                        break;
                    case NOV:
                        erg = new Day(year, Month.November, 0);
                        break;
                    case DEC:
                        erg = new Day(year, Month.December, 0);
                        break;
                    default:
//                        errors.add(partName + ": Date format is not correct");
                        break;
                }
                break;
            case Quarterly:
                DateConverterQuarter q = DateConverterQuarter.getQuarter(split[1].trim());
                switch (q) {
                    case Q1:
                        erg = new Day(year, Month.January, 0);
                        break;
                    case Q2:
                        erg = new Day(year, Month.April, 0);
                        break;
                    case Q3:
                        erg = new Day(year, Month.July, 0);
                        break;
                    case Q4:
                        erg = new Day(year, Month.October, 0);
                        break;
                    default:
//                        errors.add(partName + ": Date format is not correct");
                        break;
                }
        } 
        return erg;
    }
    
    public static Day changeToQuarter(Day month){
        
        int quarter = month.getMonth();
        Day rslt;

        switch (quarter) {
            //Recalculation from month to quarter
            //i.e. the third quarter is defined, calculate to march and than to the beginning of the third quarter
            //1970.3 -> 1970-03-01 -> month=2 -> 1970-07-01
            case 0:
                //yyyy.1
                rslt = new Day(month.getYear(), Month.January, 0);
                break;
            case 1:
                //yyyy.2
                rslt = new Day(month.getYear(), Month.April, 0);
                break;
            case 2:
                //yyyy.3
                rslt = new Day(month.getYear(), Month.July, 0);
                break;
            case 3:
                //yyyy.4
                rslt = new Day(month.getYear(), Month.October, 0);
                break;
            default:
                //Fehler
                rslt = null;
                break;
        }
        return rslt;
    }
}
