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

import ec.tss.DynamicTsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import eu.nbdemetra.specParser.Miscellaneous.TranslationInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Nina Gonschorreck
 */
public class DataLoaderRegression extends DataLoader {

    private ArrayList<String> regressorName = new ArrayList();
    private ArrayList<String> regressorDesc = new ArrayList<>();
    private ArrayList<String> regressorZislId = new ArrayList();

    private ArrayList<TsData> regressorsFromWebService = new ArrayList();

    public DataLoaderRegression(HashMap<String, TranslationInfo> map) {
        super(map);
    }

    public void addRegFromWebServive(TsData data) {
        regressorsFromWebService.add(data);
    }

    public void setRegressorName(String regressorName) {
        this.regressorName.add(regressorName);
    }

    public void setRegressorName(String[] regressorNames) {
        regressorName.addAll(Arrays.asList(regressorNames));
    }

    public String[] getRegressorName() {
        return regressorName.toArray(new String[0]);
    }

    public void setRegressorZisl(String zisl) {
        this.regressorZislId.add(zisl);
    }

    public String[] getRegressorDesc() {
        return regressorDesc.toArray(new String[0]);
    }

    public void setRegressorDesc(ArrayList<String> regressorDesc) {
        this.regressorDesc = regressorDesc;
    }

    public void setRegressorDesc(String regressorDesc) {
        this.regressorDesc.add(regressorDesc);
    }

    public TsVariable[] getRegressors() {

        if (regressorsFromWebService.isEmpty()) {
            if (super.getValues() == null) {
                super.generateValues();
            }

            if (super.getValues() != null) {
                ArrayList<TsVariable> r = new ArrayList();
                if (regressorDesc.isEmpty()) {
                    regressorDesc.add("...");
                }
                int lengthOfRegressors = super.getValues().length / regressorName.size();
                double[] tmp = new double[lengthOfRegressors];
                TsData data;

                for (int regressor = 0; regressor < regressorName.size(); regressor++) {
                    for (int i = 0; i < lengthOfRegressors; i++) {
                        tmp[i] = super.getValues()[i * regressorName.size() + regressor];
                    }
                    //month geht nicht wenn grÃ¶ÃŸer als 3
                    int p = getStart().getMonth();
                    if (getPeriod().equals(TsFrequency.Quarterly)) {
                        p = (p / 3);
                    }
                    data = new TsData(getPeriod(), getStart().getYear(), p, tmp, true);
                    //Name und startwert pruefen
                    r.add(new TsVariable(regressorDesc.get(0), data));
                }
                return r.toArray(new TsVariable[0]);
            }
            //Fehlermeldung: Keine Werte vorhanden
//            messages = "Keine Werte vorhanden";
            return null;
        } else {
            if (regressorName != null) {
                if (regressorName.size() == regressorsFromWebService.size()) {
                    ArrayList<TsVariable> r = new ArrayList();
                    for (int regressor = 0; regressor < regressorName.size(); regressor++) {
                        r.add(new DynamicTsVariable(regressorZislId.get(regressor), getMoniker(), regressorsFromWebService.get(regressor)));
                    }
                    return r.toArray(new TsVariable[0]);
                } else {
                    //Fehlermeldung: unterschiedliche LÃ¤ngen bei Namen und Daten
                    super.infos.put("REGRESSION"
                            + ": Number of regressor names not equal to number of regressors"
                            + ". (Code:2101).",
                            TranslationInfo.ERROR);
                    return null;
                }
            }
        }
        return null;
    }
}
