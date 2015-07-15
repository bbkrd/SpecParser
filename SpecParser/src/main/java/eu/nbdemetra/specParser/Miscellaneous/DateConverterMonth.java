/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.nbdemetra.specParser.Miscellaneous;

/**
 *
 * @author Nina Gonschorreck
 */
public enum DateConverterMonth {

    JAN(new String[]{"jan", "january", "01", "1"}),
    FEB(new String[]{"feb", "february", "02", "2"}),
    MAR(new String[]{"mar", "march", "03", "3"}),
    APR(new String[]{"apr", "april", "04", "4"}),
    MAY(new String[]{"may", "05", "5"}),
    JUN(new String[]{"jun", "juny", "06", "6"}),
    JUL(new String[]{"jul", "july", "07", "7"}),
    AUG(new String[]{"aug", "august", "08", "8"}),
    SEP(new String[]{"sep", "september", "09", "9"}),
    OCT(new String[]{"oct", "october", "10"}),
    NOV(new String[]{"nov", "november","11"}),
    DEC(new String[]{"dec", "december", "12"});

    private DateConverterMonth(String[] array) {
        value=array;
    }

    private final String[] value;

    public String[] getValue() {
        return value;
    }

    public static DateConverterMonth getMonth(String input) {
        for (DateConverterMonth m : values()) {
            for (String string : m.getValue()) {
                if (string.equalsIgnoreCase(input)) {
                    return m;
                }
            }
        }
        throw new IllegalArgumentException();
    }
}
