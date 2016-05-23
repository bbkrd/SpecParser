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
