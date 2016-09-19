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

    public static Day toJD(String day, boolean begin) {
        return toJD(day, Monthly, begin);
    }

    public static Day toJD(String day, TsFrequency period, boolean begin) {

        String[] split = day.split("\\.");

        int year = Integer.parseInt(split[0].trim());
        Day erg = null;

        switch (period) {
            case Monthly:
                DateConverterMonth m = DateConverterMonth.getMonth(split[1].trim());
                switch (m) {

                    case JAN:
                        if (begin) {
                            erg = new Day(year, Month.January, 0);
                        } else {
                            erg = new Day(year, Month.January, 30);
                        }
                        break;
                    case FEB:
                        if (begin) {
                            erg = new Day(year, Month.February, 0);
                        } else {
                            erg = new Day(year, Month.February, 27);
                        }
                        break;
                    case MAR:
                        if (begin) {
                            erg = new Day(year, Month.March, 0);
                        } else {
                            erg = new Day(year, Month.March, 30);
                        }
                        break;
                    case APR:
                        if (begin) {
                            erg = new Day(year, Month.April, 0);
                        } else {
                            erg = new Day(year, Month.April, 29);
                        }
                        break;
                    case MAY:
                        if (begin) {
                            erg = new Day(year, Month.May, 0);
                        }else{
                            erg = new Day(year, Month.May, 30);
                        }
                        break;
                    case JUN:
                        if (begin) {
                            erg = new Day(year, Month.June, 0);
                        }else{
                            erg = new Day(year, Month.June, 29);
                        }
                        break;
                    case JUL:
                        if (begin) {
                            erg = new Day(year, Month.July, 0);
                        }else{
                            erg = new Day(year, Month.July, 30);
                        }
                        break;
                    case AUG:
                        if (begin) {
                            erg = new Day(year, Month.August, 0);
                        }else{
                            erg = new Day(year, Month.August, 30);
                        }
                        break;
                    case SEP:
                        if (begin) {
                            erg = new Day(year, Month.September, 0);
                        }else{
                            erg = new Day(year, Month.September, 29);
                        }
                        break;
                    case OCT:
                        if (begin) {
                            erg = new Day(year, Month.October, 0);
                        }else{
                            erg = new Day(year, Month.October, 30);
                        }
                        break;
                    case NOV:
                        if (begin) {
                            erg = new Day(year, Month.November, 0);
                        }else{
                            erg = new Day(year, Month.November, 29);
                        }
                        break;
                    case DEC:
                        if (begin) {
                            erg = new Day(year, Month.December, 0);
                        }else{
                            erg = new Day(year, Month.December, 30);
                        }
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

    public static Day changeToQuarter(Day month) {

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
