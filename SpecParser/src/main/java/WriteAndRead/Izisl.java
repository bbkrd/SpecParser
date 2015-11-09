/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
