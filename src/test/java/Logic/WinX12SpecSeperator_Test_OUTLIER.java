/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.timeseries.regression.OutlierType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author o35041m
 */
public class WinX12SpecSeperator_Test_OUTLIER {
    
    private X13Specification expected;
    
    public WinX12SpecSeperator_Test_OUTLIER() {
    }    
    
    /*
     * Erstellt eine X13Specification über build-Methode der zu testenden Klasse WinX12SpecSeperator
     * und gibt diese zurück
     */
    public X13Specification createActual(String values) {

        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(values);
        return x12.getCurrentSpec();
    }
    
    @Before
    public void setUp() {
        expected = new X13Specification();
        
        expected.getRegArimaSpecification().getOutliers().setSpan(expected.getRegArimaSpecification().getBasic().getSpan());
        expected.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(0);
        expected.getRegArimaSpecification().getOutliers().setLSRun(0);
        expected.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddOne);
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
        expected.getRegArimaSpecification().getOutliers().setMonthlyTCRate(0.7);
    }

    @Test
    public void test_default(){
        String testname = "test_default";
        Logger.logTestStart(testname);
        // String, der vom Parser eingelesen wird
        String spc = "OUTLIER   \n" +
                        "  {    \n" +                       
                        "  }      ";
        
        X13Specification actual = createActual(spc);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getLSRun(), actual.getRegArimaSpecification().getOutliers().getLSRun());
        assertEquals(expected.getRegArimaSpecification().getOutliers().getMethod(), actual.getRegArimaSpecification().getOutliers().getMethod());
        assertEquals(expected.getRegArimaSpecification().getOutliers().getMonthlyTCRate(), actual.getRegArimaSpecification().getOutliers().getMonthlyTCRate(), 0.000001);
        assertEquals(expected.getRegArimaSpecification().getOutliers().getSpan(), actual.getRegArimaSpecification().getOutliers().getSpan());
        Assert.assertArrayEquals(expected.getRegArimaSpecification().getOutliers().getTypes(), actual.getRegArimaSpecification().getOutliers().getTypes());
        
        Logger.logTestEnd(testname);
    }
      
    /*
    @Test
    public void test_attributes_empty(){
        String testname = "test_attributes_empty";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = \n"
                + "critical = \n"
                + "lsrun = \n"
                + "method = \n"
                + "span = \n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getLSRun(), actual.getRegArimaSpecification().getOutliers().getLSRun());
        assertEquals(expected.getRegArimaSpecification().getOutliers().getMethod(), actual.getRegArimaSpecification().getOutliers().getMethod());
        assertEquals(expected.getRegArimaSpecification().getOutliers().getMonthlyTCRate(), actual.getRegArimaSpecification().getOutliers().getMonthlyTCRate(), 0.000001);
        assertEquals(expected.getRegArimaSpecification().getOutliers().getSpan(), actual.getRegArimaSpecification().getOutliers().getSpan());
        Assert.assertArrayEquals(expected.getRegArimaSpecification().getOutliers().getTypes(), actual.getRegArimaSpecification().getOutliers().getTypes());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }*/
    
    /*
    @Test
    public void test_attributes_invalid(){
        String testname = "test_attributes_invalid";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = invalid\n"
                + "critical = invalid\n"
                + "lsrun = invalid\n"
                + "method = invalid\n"
                + "span = invalid\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getLSRun(), actual.getRegArimaSpecification().getOutliers().getLSRun());
        assertEquals(expected.getRegArimaSpecification().getOutliers().getMethod(), actual.getRegArimaSpecification().getOutliers().getMethod());
        assertEquals(expected.getRegArimaSpecification().getOutliers().getMonthlyTCRate(), actual.getRegArimaSpecification().getOutliers().getMonthlyTCRate(), 0.000001);
        assertEquals(expected.getRegArimaSpecification().getOutliers().getSpan(), actual.getRegArimaSpecification().getOutliers().getSpan());
        Assert.assertArrayEquals(expected.getRegArimaSpecification().getOutliers().getTypes(), actual.getRegArimaSpecification().getOutliers().getTypes());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    */
    
    @Test
    public void test_types_all(){
        String testname = "test_attributes_types_all";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = all\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.TC);
        
        Assert.assertArrayEquals(expected.getRegArimaSpecification().getOutliers().getTypes(), actual.getRegArimaSpecification().getOutliers().getTypes());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_types_ao(){
        String testname = "test_attributes_types_ao";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = ao\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getOutliers().clearTypes();
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
        
        Assert.assertArrayEquals(expected.getRegArimaSpecification().getOutliers().getTypes(), actual.getRegArimaSpecification().getOutliers().getTypes());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_types_ls(){
        String testname = "test_attributes_types_ls";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = ls\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getOutliers().clearTypes();
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
        
        Assert.assertArrayEquals(expected.getRegArimaSpecification().getOutliers().getTypes(), actual.getRegArimaSpecification().getOutliers().getTypes());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_types_tc(){
        String testname = "test_attributes_types_tc";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = tc\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getOutliers().clearTypes();
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.TC);
        
        Assert.assertArrayEquals(expected.getRegArimaSpecification().getOutliers().getTypes(), actual.getRegArimaSpecification().getOutliers().getTypes());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_method_addOne(){
        String testname = "test_attributes_method_addOne";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
            + "{\n"
                + "method = addone\n"
            + "}\n";
        
        X13Specification actual = createActual(spc);
        
        expected.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddOne);

        assertEquals(expected.getRegArimaSpecification().getOutliers().getMethod(), actual.getRegArimaSpecification().getOutliers().getMethod());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_method_addAll(){
        String testname = "test_attributes_method_addAll";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
            + "{\n"
                + "method = addall\n"
            + "}\n";
        
        X13Specification actual = createActual(spc);
        
        expected.getRegArimaSpecification().getOutliers().setMethod(OutlierSpec.Method.AddAll);

        assertEquals(expected.getRegArimaSpecification().getOutliers().getMethod(), actual.getRegArimaSpecification().getOutliers().getMethod());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    // big value in JD* allowed
    /*@Test
    public void test_lsrun_tooBig(){
        String testname = "test_lsrun_tooBig";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "lsrun = 6\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getLSRun(), actual.getRegArimaSpecification().getOutliers().getLSRun());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }*/
    
    /*
    @Test
    public void test_lsrun_tooLow(){
        String testname = "test_lsrun_tooLow";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "lsrun = -1\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getLSRun(), actual.getRegArimaSpecification().getOutliers().getLSRun());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    */
    
    @Test
    public void test_lsrun_valid(){
        String testname = "test_lsrun_valid";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "lsrun = 3\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        expected.getRegArimaSpecification().getOutliers().setLSRun(3);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getLSRun(), actual.getRegArimaSpecification().getOutliers().getLSRun());
        
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_critical_oneValue(){
        String testname = "test_critical_oneValue";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "critical = 3.75\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        expected.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(3.75);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), actual.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), 0.00001);;
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_critical_twoValues(){
        String testname = "test_critical_twoValues";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "critical = (3.75,,4.77)\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        expected.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(3.75);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), actual.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), 0.00001);;
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_critical_threeValues_ao(){
        String testname = "test_critical_threeValues_ao";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = ao\n"
                + "critical = (3.75,4.22,4.77)\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getOutliers().clearTypes();
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.AO);
        expected.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(3.75);
        
        OutlierSpec act = actual.getRegArimaSpecification().getOutliers();
        OutlierSpec exp = expected.getRegArimaSpecification().getOutliers();
         
        assertEquals(expected.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), actual.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), 0.00001);;
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_critical_threeValues_ls(){
        String testname = "test_critical_threeValues_ls";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = ls\n"
                + "critical = (3.75,4.22,4.77)\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        expected.getRegArimaSpecification().getOutliers().clearTypes();
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.LS);
        expected.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(3.75);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), actual.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), 0.00001);;
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_critical_threeValues_tc(){
        String testname = "test_critical_threeValues_tc";
        Logger.logTestStart(testname);
        
        String spc = "OUTLIER\n"
                + "{\n"
                + "types = tc\n"
                + "critical = (3.75,4.22,4.77)\n"
                + "}\n";
        
        X13Specification actual = createActual(spc);
        
        expected.getRegArimaSpecification().getOutliers().clearTypes();
        expected.getRegArimaSpecification().getOutliers().add(OutlierType.TC);
        expected.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(3.75);
        
        assertEquals(expected.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), actual.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), 0.00001);;
        assertEquals(expected.getRegArimaSpecification().getOutliers(), actual.getRegArimaSpecification().getOutliers());
        
        Logger.logTestEnd(testname);
    }
    
}
