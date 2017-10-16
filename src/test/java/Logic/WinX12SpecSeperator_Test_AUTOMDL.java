/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.x13.X13Specification;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author o35041m
 */
public class WinX12SpecSeperator_Test_AUTOMDL {
    
    private X13Specification expected;
    
    public WinX12SpecSeperator_Test_AUTOMDL() {
    }

    public X13Specification createActual(String values) {

        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(values);
        return x12.getCurrentSpec();
    }
    
    @Before
    public void setUp() {
        expected = new X13Specification();
        
        expected.getRegArimaSpecification().getAutoModel().setEnabled(true);
        
        expected.getRegArimaSpecification().getAutoModel().setAcceptDefault(false);
        expected.getRegArimaSpecification().getAutoModel().setCheckMu(true);
        expected.getRegArimaSpecification().getAutoModel().setLjungBoxLimit(0.95);
        expected.getRegArimaSpecification().getAutoModel().setMixed(true);
        expected.getRegArimaSpecification().getAutoModel().setArmaSignificance(1.0);
        expected.getRegArimaSpecification().getAutoModel().setBalanced(false);
        expected.getRegArimaSpecification().getAutoModel().setHannanRissanen(false);
        expected.getRegArimaSpecification().getAutoModel().setPercentReductionCV(0.14268);
        expected.getRegArimaSpecification().getAutoModel().setUnitRootLimit(1.05);
    }
    
    @Test
    public void test_default(){
        String testname = "test_default";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" +
                        "  {\n" +
                
                        "  }";
        
        X13Specification actual = createActual(spc);
                
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getArmaSignificance(), actual.getRegArimaSpecification().getAutoModel().getArmaSignificance(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isAcceptDefault(), actual.getRegArimaSpecification().getAutoModel().isAcceptDefault());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isCheckMu(), actual.getRegArimaSpecification().getAutoModel().isCheckMu());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getLjungBoxLimit(), actual.getRegArimaSpecification().getAutoModel().getLjungBoxLimit(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isMixed(), actual.getRegArimaSpecification().getAutoModel().isMixed());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isEnabled(), actual.getRegArimaSpecification().getAutoModel().isEnabled());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isBalanced(), actual.getRegArimaSpecification().getAutoModel().isBalanced());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isHannanRissannen(), actual.getRegArimaSpecification().getAutoModel().isHannanRissannen());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getPercentReductionCV(), actual.getRegArimaSpecification().getAutoModel().getPercentReductionCV(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getUnitRootLimit(), actual.getRegArimaSpecification().getAutoModel().getUnitRootLimit(), 0.0001);
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        
        Logger.logTestEnd(testname);
        
    }
    
    @Test
    public void test_attribute_empty(){
        String testname = "test_attribute_empty";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" 
                +"{\n"
                + "acceptdefault = \n"
                + "checkmu = \n"
                + "ljungboxlimit = \n"
                + "mixed = \n"
                + "armalimit = \n"
                + "balanced = \n"
                + "hrinitial = \n"
                + "reducecv = \n"
                + "urfinal = \n" 
                + "}";
        
        X13Specification actual = createActual(spc);
                
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getArmaSignificance(), actual.getRegArimaSpecification().getAutoModel().getArmaSignificance(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isAcceptDefault(), actual.getRegArimaSpecification().getAutoModel().isAcceptDefault());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isCheckMu(), actual.getRegArimaSpecification().getAutoModel().isCheckMu());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getLjungBoxLimit(), actual.getRegArimaSpecification().getAutoModel().getLjungBoxLimit(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isMixed(), actual.getRegArimaSpecification().getAutoModel().isMixed());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isEnabled(), actual.getRegArimaSpecification().getAutoModel().isEnabled());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isBalanced(), actual.getRegArimaSpecification().getAutoModel().isBalanced());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isHannanRissannen(), actual.getRegArimaSpecification().getAutoModel().isHannanRissannen());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getPercentReductionCV(), actual.getRegArimaSpecification().getAutoModel().getPercentReductionCV(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getUnitRootLimit(), actual.getRegArimaSpecification().getAutoModel().getUnitRootLimit(), 0.0001);
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        
        Logger.logTestEnd(testname);
        
    } 
    
    @Test
    public void test_attribute_invalid(){
        String testname = "test_attribute_invalid";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" 
                +"{\n"
                + "acceptdefault = invalid\n"
                + "checkmu = invalid\n"
                + "ljungboxlimit = invalid\n"
                + "mixed = invalid\n"
                + "armalimit = invalid\n"
                + "balanced = invalid\n"
                + "hrinitial = invalid\n"
                + "reducecv = invalid\n"
                + "urfinal = invalid\n" 
                + "}";
        
        X13Specification actual = createActual(spc);
                
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getArmaSignificance(), actual.getRegArimaSpecification().getAutoModel().getArmaSignificance(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isAcceptDefault(), actual.getRegArimaSpecification().getAutoModel().isAcceptDefault());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isCheckMu(), actual.getRegArimaSpecification().getAutoModel().isCheckMu());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getLjungBoxLimit(), actual.getRegArimaSpecification().getAutoModel().getLjungBoxLimit(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isMixed(), actual.getRegArimaSpecification().getAutoModel().isMixed());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isEnabled(), actual.getRegArimaSpecification().getAutoModel().isEnabled());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isBalanced(), actual.getRegArimaSpecification().getAutoModel().isBalanced());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isHannanRissannen(), actual.getRegArimaSpecification().getAutoModel().isHannanRissannen());
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getPercentReductionCV(), actual.getRegArimaSpecification().getAutoModel().getPercentReductionCV(), 0.0001);
        assertEquals(expected.getRegArimaSpecification().getAutoModel().getUnitRootLimit(), actual.getRegArimaSpecification().getAutoModel().getUnitRootLimit(), 0.0001);
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        
        Logger.logTestEnd(testname);
        
    }
    
    @Test
    public void test_acceptdefault_yes(){
        String testname = "test_acceptdefault_yes";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" 
                +"{\n"
                + "acceptdefault = yes\n"
                + "}";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getAutoModel().setAcceptDefault(true);
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isAcceptDefault(), actual.getRegArimaSpecification().getAutoModel().isAcceptDefault());
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        
        Logger.logTestEnd(testname);
        
    }
    
    @Test
    public void test_checkmu_no(){
        String testname = "test_checkmu_no";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" 
                +"{\n"
                + "checkmu = no\n"
                + "}";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getAutoModel().setCheckMu(false);   
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isCheckMu(), actual.getRegArimaSpecification().getAutoModel().isCheckMu());
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        Logger.logTestEnd(testname);
        
    }
    
    @Test
    public void test_mixed_no(){
        String testname = "test_checkmu_no";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" 
                +"{\n"
                + "mixed = no\n"
                + "}";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getAutoModel().setMixed(false);   
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isMixed(), actual.getRegArimaSpecification().getAutoModel().isMixed());
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        Logger.logTestEnd(testname);
        
    }
    
    @Test
    public void test_hrinitial_yes(){
        String testname = "test_hrinitial_yes";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" 
                +"{\n"
                + "hrinitial = yes\n"
                + "}";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getAutoModel().setHannanRissanen(true);   
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isHannanRissannen(), actual.getRegArimaSpecification().getAutoModel().isHannanRissannen());
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        Logger.logTestEnd(testname);
        
    }
    
    @Test
    public void test_balanced_yes(){
        String testname = "test_balanced_yes";
        Logger.logTestStart(testname);
        
        String spc = "AUTOMDL\n" 
                +"{\n"
                + "balanced = yes\n"
                + "}";
        
        X13Specification actual = createActual(spc);
        expected.getRegArimaSpecification().getAutoModel().setBalanced(true);   
        
        assertEquals(expected.getRegArimaSpecification().getAutoModel().isBalanced(), actual.getRegArimaSpecification().getAutoModel().isBalanced());
        assertEquals(expected.getRegArimaSpecification().getAutoModel(), actual.getRegArimaSpecification().getAutoModel());
        Logger.logTestEnd(testname);
        
    }
    
    
}
