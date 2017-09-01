/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.x13.X13Specification;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author o35041m
 */
public class WinX12SpecSeperator_Test_TRANSFORM {
    
    private X13Specification expected;
    
    public WinX12SpecSeperator_Test_TRANSFORM() {
    }

    public X13Specification createActual(String values) {

        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(values);
        return x12.getCurrentSpec();
    }
    
    @Before
    public void setUp() {
        expected = new X13Specification();
    }
    
    @Test
    public void test_default(){
        String testname = "test_default";
        Logger.logTestStart(testname);
        
        String spc = 
                "TRANSFORM\n"
                + "{\n"
                + "}";
        
        X13Specification actual = createActual(spc);
        
        Logger.logTestEnd(testname);
    }
        
}
