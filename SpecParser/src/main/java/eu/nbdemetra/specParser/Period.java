/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.nbdemetra.specParser;

/**
 *
 * @author Nina Gonschorreck
 */
public enum Period {
    
    MONTH(12), QUARTER(4);
    
    int value;
    
    private Period(int v){
        value=v;
    }
}
