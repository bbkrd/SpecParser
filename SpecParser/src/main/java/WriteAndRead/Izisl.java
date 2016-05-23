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
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Nina Gonschorreck
 */
@ServiceDefinition
public interface Izisl {
    
    public void setId(String id);
    public TsData getData();
    public TsMoniker getMoniker();
}
