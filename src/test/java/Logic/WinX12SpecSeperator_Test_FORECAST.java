/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.x13.X13Specification;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author o35041m
 */
public class WinX12SpecSeperator_Test_FORECAST {

    private X13Specification expected;

    public WinX12SpecSeperator_Test_FORECAST() {
    }

    public X13Specification createActual(String values) {

        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(values);
        return x12.getCurrentSpec();
    }

    @Before
    public void setUp() {
        expected = new X13Specification();

        expected.getX11Specification().setBackcastHorizon(0);
        expected.getX11Specification().setForecastHorizon(-1);
    }

    @Test
    public void test_default() {
        String testname = "test_default";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "}";

        X13Specification actual = createActual(spc);

        assertEquals(expected.getX11Specification().getBackcastHorizon(), actual.getX11Specification().getBackcastHorizon());
        assertEquals(expected.getX11Specification().getForecastHorizon(), actual.getX11Specification().getForecastHorizon());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_attributes_empty() {
        String testname = "test_attributes_empty";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "maxback = "
                + "maxlead = "
                + "}";

        X13Specification actual = createActual(spc);

        assertEquals(expected.getX11Specification().getBackcastHorizon(), actual.getX11Specification().getBackcastHorizon());
        assertEquals(expected.getX11Specification().getForecastHorizon(), actual.getX11Specification().getForecastHorizon());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_attributes_invalid() {
        String testname = "test_attributes_invalid";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "maxback = invalid"
                + "maxlead = invalid"
                + "}";

        X13Specification actual = createActual(spc);

        assertEquals(expected.getX11Specification().getBackcastHorizon(), actual.getX11Specification().getBackcastHorizon());
        assertEquals(expected.getX11Specification().getForecastHorizon(), actual.getX11Specification().getForecastHorizon());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_maxlead_valid() {
        String testname = "test_maxlead_valid";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "maxlead = 30"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getX11Specification().setForecastHorizon(30);

        assertEquals(expected.getX11Specification().getForecastHorizon(), actual.getX11Specification().getForecastHorizon());

        Logger.logTestEnd(testname);
    }
/*
    @Test
    public void test_maxlead_tooBig() {
        String testname = "test_maxlead_tooBig";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "maxlead = 121"
                + "}";

        X13Specification actual = createActual(spc);

        assertEquals(expected.getX11Specification().getForecastHorizon(), actual.getX11Specification().getForecastHorizon());

        Logger.logTestEnd(testname);
    }
    */

    /*
    @Test
    public void test_maxlead_tooLow() {
        String testname = "test_maxlead_tooLow";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "maxlead = -5"
                + "}";

        X13Specification actual = createActual(spc);

        assertEquals(expected.getX11Specification().getForecastHorizon(), actual.getX11Specification().getForecastHorizon());

        Logger.logTestEnd(testname);
    }
*/
    @Test
    public void test_maxback_valid() {
        String testname = "test_maxback_valid";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "maxback = 30"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getX11Specification().setBackcastHorizon(30);

        assertEquals(expected.getX11Specification().getBackcastHorizon(), actual.getX11Specification().getBackcastHorizon());

        Logger.logTestEnd(testname);
    }

    // big values are allowed in JD+
    /* 
    @Test
    public void test_maxback_tooBig(){
        String testname = "test_maxback_tooBig";
        Logger.logTestStart(testname);
        
        String spc = 
                "FORECAST    \n"
                + "{      \n"
                + "maxback = 121"
                + "}";
        
        X13Specification actual = createActual(spc);
        
        assertEquals(expected.getX11Specification().getBackcastHorizon(), actual.getX11Specification().getBackcastHorizon());
        
        Logger.logTestEnd(testname);
    }
     */
    
    // negative values are allowed in JD+
    /*
    @Test
    public void test_maxback_tooLow() {
        String testname = "test_maxback_tooLow";
        Logger.logTestStart(testname);

        String spc
                = "FORECAST    \n"
                + "{      \n"
                + "maxback = -5"
                + "}";

        X13Specification actual = createActual(spc);

        assertEquals(expected.getX11Specification().getBackcastHorizon(), actual.getX11Specification().getBackcastHorizon());

        Logger.logTestEnd(testname);
    }*/
}
