/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import WriteAndRead.DataLoader;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x13.X13Specification;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;


/**
 *
 * @author o35041m
 */
public class WinX12SpecSeparatorTest_X11 {

    public WinX12SpecSeparatorTest_X11() {
    }

        public X13Specification createTestFile(String values) {

        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(values);
        return x12.getCurrentSpec();
    }

    public X13Specification createX13Default() {
        
        
        X13Specification expected = new X13Specification();
        expected.getRegArimaSpecification().getBasic().setPreprocessing(false);
        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);       // mode
        expected.getX11Specification().setSeasonal(true);
        expected.getX11Specification().setSeasonalFilter(SeasonalFilterOption.Msr);     // seasonalma
        expected.getX11Specification().setSigma(1.5, 2.5);                              // sigmalim
        expected.getX11Specification().setForecastHorizon(0);

        return expected;
    }
    
    
    @Test
    public void test_read_trendma_valid() {
        String spc = "X11{"
                + "trendma = 17"
                + "}";

        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();

        expected.getX11Specification().setHendersonFilterLength(17);                    // trendma

        assertEquals(expected, spec);
    }
    
    @Test
    public void test_read_trendma_invalid_toBig() {
        String spc = "X11{"
                + "trendma = 103"
                + "}";

        X13Specification spec = createTestFile(spc);
        assertEquals(0, spec.getX11Specification().getHendersonFilterLength());
    }
    
    @Test
    public void test_read_trendma_invalid_toSmall() {
        String spc = "X11{"
                + "trendma = -1"
                + "}";

        X13Specification spec = createTestFile(spc);
        assertEquals(0, spec.getX11Specification().getHendersonFilterLength());
    }
    
    @Test
    public void test_read_trendma_invalid_even() {
        String spc = "X11{"
                + "trendma = 8"
                + "}";

        X13Specification spec = createTestFile(spc);
        assertEquals(0, spec.getX11Specification().getHendersonFilterLength());
    }
    
    @Test
    public void test_read_trendma_invalid_notANumber() {
        String spc = "X11{"
                + "trendma = blafasel"
                + "}";

        X13Specification spec = createTestFile(spc);
        assertEquals(0, spec.getX11Specification().getHendersonFilterLength());
    }
    
    @Test
    public void test_read_trendma_noValue() {
        String spc = "X11{"
                + "trendma = "
                + "}";

        X13Specification spec = createTestFile(spc);
        assertEquals(0, spec.getX11Specification().getHendersonFilterLength());
    }
    
    

    @Test
    public void test_mode_add() {
        String spc = "X11{"
                + "mode = add"
                + "}";

        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();

        expected.getX11Specification().setMode(DecompositionMode.Additive);

        assertEquals(expected, spec);
    }
    
    @Test
    public void test_mode_mult() {
        String spc = "X11{"
                + "mode = mult"
                + "}";

        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();

        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);

        assertEquals(expected, spec);
    }
    
    @Test
    public void test_mode_noInput() {
        String spc = "X11{"
                + "mode ="
                + "}";

        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();

        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);

        assertEquals(expected, spec);
    }

    @Test
    public void test_mode_logadd() {
        String spc = "X11{"
                + "mode = logadd"
                + "}";

        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();

        expected.getX11Specification().setMode(DecompositionMode.LogAdditive);

        assertEquals(expected, spec);
    }
    
    @Test
    public void test_mode_number() {
        String spc = "X11{"
                + "mode = 5"
                + "}";

        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();

        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);

        assertEquals(expected, spec);
    }

    @Ignore
    @Test
    public void test_seasonalma_quarter(){
        String spc = "X11{"
                + "seasonalma = S3X3  S3X5  S3X9  S3X1"
                + "}";
        
        SeasonalFilterOption[] filter = new SeasonalFilterOption[]
            {
                SeasonalFilterOption.S3X3, 
                SeasonalFilterOption.S3X5, 
                SeasonalFilterOption.S3X9, 
                SeasonalFilterOption.S3X1,
            };
        
        X13Specification expected = createX13Default();
        X13Specification spec = createTestFile(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        assertEquals(expected, spec);
    }
    
    @Test
    public void test_seasonalma_year(){
        String spc = "X11{"
                + "seasonalma = S3X3"
                + "}";
        
        SeasonalFilterOption[] filter = new SeasonalFilterOption[]
            {
                SeasonalFilterOption.S3X3 
            };
        
        X13Specification expected = createX13Default();
        X13Specification spec = createTestFile(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        assertEquals(expected, spec);
    }
    
    @Test
    public void test_seasonalma_month(){
        String spc = "X11{"
                + "seasonalma = S3X3 S3X3 S3X3 S3X5 S3X5 S3X5 S3X1 S3X1 S3X1 S3X9 S3X9 S3X9"
                + "}";
        
        SeasonalFilterOption[] filter = new SeasonalFilterOption[]
            {
                SeasonalFilterOption.S3X3,
                SeasonalFilterOption.S3X3,
                SeasonalFilterOption.S3X3,
                SeasonalFilterOption.S3X5,
                SeasonalFilterOption.S3X5,
                SeasonalFilterOption.S3X5,
                SeasonalFilterOption.S3X1,
                SeasonalFilterOption.S3X1,
                SeasonalFilterOption.S3X1,
                SeasonalFilterOption.S3X9,
                SeasonalFilterOption.S3X9,
                SeasonalFilterOption.S3X9,
            };
        
        X13Specification expected = createX13Default();
        X13Specification spec = createTestFile(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        assertEquals(expected, spec);
    }
    
    @Test
    public void test_seasonalma_nothing(){
        String spc = "X11{"
                + "seasonalma = "
                + "}";
        
        SeasonalFilterOption[] filter = new SeasonalFilterOption[]
            {
                SeasonalFilterOption.Msr 
            };
        
        X13Specification expected = createX13Default();
        X13Specification spec = createTestFile(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        assertEquals(expected, spec);
    }
    
    @Test
    public void test_seasonalma_invalid(){
        String spc = "X11{"
                + "seasonalma = blafasel"
                + "}";
        
        X13Specification spec = createTestFile(spc);

        Assert.assertNull(spec.getX11Specification().getSeasonalFilters()[0]);
    }
    
    @Test
    public void test_sigmalim_valid() {
        String spc = "X11{"
                + "sigmalim = ( 2.0,2.5 )"
                + "}";
        
        X13Specification spec = createTestFile(spc);
        
        assertEquals(2.0, spec.getX11Specification().getLowerSigma(), 0);
        assertEquals(2.5, spec.getX11Specification().getUpperSigma(), 0);
    }
    
    @Test
    public void test_sigmalim_invalid_lBiggerThanU() {
        String spc = "X11{"
                + "sigmalim = ( 2.9,2.6 )"
                + "}";
        
        X13Specification spec = createTestFile(spc);
        
        assertEquals(1.5, spec.getX11Specification().getLowerSigma(), 0);
        assertEquals(2.5, spec.getX11Specification().getUpperSigma(), 0);
    }
    
    @Test
    public void test_sigmalim_valid_missingValues() {
        String spc = "X11{"
                + "sigmalim = ()"
                + "}";
        
        X13Specification spec = createTestFile(spc);
        
        assertEquals(1.5, spec.getX11Specification().getLowerSigma(), 0);
        assertEquals(2.5, spec.getX11Specification().getUpperSigma(), 0);
    }
    
    @Test
    public void test_sigmalim_valid_onlyUSigma() {
        String spc = "X11{"
                + "sigmalim = (,3.7)"
                + "}";
        
        X13Specification spec = createTestFile(spc);
        
        assertEquals(1.5, spec.getX11Specification().getLowerSigma(), 0);
        assertEquals(3.7, spec.getX11Specification().getUpperSigma(), 0);
    }
    
    @Test
    public void test_sigmalim_invalid_notANumber() {
        String spc = "X11{"
                + "sigmalim = (bla, fasel)"
                + "}";
        
        X13Specification spec = createTestFile(spc);
        
        assertEquals(1.5, spec.getX11Specification().getLowerSigma(), 0);
        assertEquals(2.5, spec.getX11Specification().getUpperSigma(), 0);
    }
    
    @Test
    public void test_sigmavec() {
        String spc = "x11{"
                     +"calendarsigma=select\n"
                     +"sigmavec=(jan feb)\n"
                     +"}";
        
        SigmavecOption[] sigmavec = new SigmavecOption[]{
            SigmavecOption.Group1, 
            SigmavecOption.Group1,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2,
            SigmavecOption.Group2
        };
        
        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        expected.getX11Specification().setSigmavec(sigmavec);
        
        assertEquals(expected, spec);
    }
    
    @Test
    public void test_sigmavec_empty() {
        String spc = "x11{"
                     +"calendarsigma=select\n"
                     +"sigmavec=\n"
                     +"}";
        
        SigmavecOption[] sigmavec = new SigmavecOption[]{
            
        };
        
        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        //expected.getX11Specification().setSigmavec(sigmavec);
        
        assertEquals(expected, spec);
    }
    
    @Test
    public void test_sigmavec_calendarsigmaNotSelected() {
        String spc = "x11{"
                     +"calendarsigma=all\n"
                     +"sigmavec=\n"
                     +"}";
        
        SigmavecOption[] sigmavec = new SigmavecOption[]{
            
        };
        
        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        //expected.getX11Specification().setSigmavec(sigmavec);
        
        assertEquals(expected, spec);
    }
    
    @Test
    public void test_sigmavec_all() {
        String spc = "x11{"
                     +"calendarsigma=selected\n"
                     +"sigmavec=(jan feb mar apr may jun jul )\n"
                     +"}";
        
        SigmavecOption[] sigmavec = new SigmavecOption[]{
            SigmavecOption.Group1, 
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1,
            SigmavecOption.Group1
        };
        
        X13Specification spec = createTestFile(spc);
        X13Specification expected = createX13Default();
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        expected.getX11Specification().setSigmavec(sigmavec);
        
        assertEquals(expected, spec);
    }
}
