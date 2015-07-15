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
public enum DateConverterQuarter {
    
    Q1(new String[]{"1", "01", "q1"}),
    Q2(new String[]{"2", "02", "q2"}),
    Q3(new String[]{"3", "03", "q3"}),
    Q4(new String[]{"4", "04", "q4"});

    private DateConverterQuarter(String[] array) {
        value = array;
    }

    private String[] value;

    public String[] getValue() {
        return value;
    }

    public static DateConverterQuarter getQuarter(String input) {
        for (DateConverterQuarter q : values()) {
            for (String string : q.getValue()) {
                if (string.equalsIgnoreCase(input)) {
                    return q;
                }
            }
        }
        throw new IllegalArgumentException();
    }
}
