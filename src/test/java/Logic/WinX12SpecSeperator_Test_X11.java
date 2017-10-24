/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x13.X13Specification;
import org.junit.Assert;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author o35041m
 */
public class WinX12SpecSeperator_Test_X11 {
    
    private X13Specification expected;
    
    public X13Specification createActual(String values) {

        WinX12SpecSeparator x12 = new WinX12SpecSeparator();
        x12.buildSpec(values);
        return x12.getCurrentSpec();
    }
    
    @Before
    public void setUp(){
        expected = new X13Specification();
        
        expected.getRegArimaSpecification().getBasic().setPreprocessing(false);
        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);
        expected.getX11Specification().setSeasonal(true);
        expected.getX11Specification().setSeasonalFilter(SeasonalFilterOption.Msr);
        expected.getX11Specification().setSigma(1.5, 2.5);
        expected.getX11Specification().setForecastHorizon(0);
    }
    
    @Test
    public void test_default(){
	String testname = "test_default";
	Logger.logTestStart(testname);
        
        String spc = 
                "X11    \n" +
                "{      \n"+
                "}";
        
        X13Specification actual = createActual(spc);
        
        assertEquals(expected.getX11Specification().getMode(), actual.getX11Specification().getMode());
        Assert.assertArrayEquals(expected.getX11Specification().getSeasonalFilters(), actual.getX11Specification().getSeasonalFilters());
        assertEquals(0.0001, expected.getX11Specification().getLowerSigma(), actual.getX11Specification().getLowerSigma());
        assertEquals(0.0001, expected.getX11Specification().getUpperSigma(), actual.getX11Specification().getUpperSigma());
        assertEquals(expected.getX11Specification().isSeasonal(), actual.getX11Specification().isSeasonal());
        assertEquals(expected.getX11Specification().isAutoHenderson(), actual.getX11Specification().isAutoHenderson());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
        
    }
    
    @Test
    public void test_read_trendma_valid(){
	String testname = "test_read_trendma_valid";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "trendma = 17"
                + "}";

        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setHendersonFilterLength(17);

        assertEquals(   
                        expected.getX11Specification().getHendersonFilterLength(), 
                        actual.getX11Specification().getHendersonFilterLength()
                    );
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_read_trendma_invalid_tooBig(){
	String testname = "test_read_trendma_invalid_tooBig";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "trendma = 103"
                + "}";

        X13Specification actual = createActual(spc);
        assertEquals(expected.getX11Specification().getHendersonFilterLength(), actual.getX11Specification().getHendersonFilterLength());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_read_trendma_invalid_tooSmall(){
	String testname = "test_read_trendma_invalid_tooSmall";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "trendma = -1"
                + "}";

        X13Specification actual = createActual(spc);
        assertEquals(expected.getX11Specification().getHendersonFilterLength(), actual.getX11Specification().getHendersonFilterLength());
        
        //assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_read_trendma_invalid_even(){
	String testname = "test_read_trendma_invalid_even";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "trendma = 8"
                + "}";

        X13Specification actual = createActual(spc);
        assertEquals(expected.getX11Specification().getHendersonFilterLength(), actual.getX11Specification().getHendersonFilterLength());
        
        //assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_read_trendma_invalid_notANumber(){
	String testname = "test_read_trendma_invalid_notANumber";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "trendma = invalid"
                + "}";

        X13Specification actual = createActual(spc);
        assertEquals(expected.getX11Specification().getHendersonFilterLength(), actual.getX11Specification().getHendersonFilterLength());
        
        //assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_read_trendma_noValue(){
	String testname = "test_read_trendma_noValue";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "trendma = "
                + "}";

        X13Specification actual = createActual(spc);
        assertEquals(expected.getX11Specification().getHendersonFilterLength(), actual.getX11Specification().getHendersonFilterLength());
        
        //assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }

    @Test
    public void test_mode_add(){
	String testname = "test_mode_add";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "mode = add"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getX11Specification().setMode(DecompositionMode.Additive);

        assertEquals(expected.getX11Specification().getMode(), actual.getX11Specification().getMode());
        
        //assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_mode_mult(){
	String testname = "test_mode_mult";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "mode = mult"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);

        assertEquals(expected.getX11Specification().getMode(), actual.getX11Specification().getMode());
        
        assertEquals(expected.getX11Specification(), actual.getX11Specification());
        
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_mode_noInput(){
	String testname = "test_mode_noInput";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "mode ="
                + "}";

        X13Specification actual = createActual(spc);

        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);

        assertEquals(expected.getX11Specification().getMode(), actual.getX11Specification().getMode());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }

    @Test
    public void test_mode_logadd(){
	String testname = "test_mode_logadd";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "mode = logadd"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getX11Specification().setMode(DecompositionMode.LogAdditive);

        assertEquals(expected.getX11Specification().getMode(), actual.getX11Specification().getMode());
        
        assertEquals(expected, actual);
        Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_mode_number(){
	String testname = "test_mode_number";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "mode = 5"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getX11Specification().setMode(DecompositionMode.Multiplicative);

        assertEquals(expected.getX11Specification().getMode(), actual.getX11Specification().getMode());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }

    @Test
    public void test_seasonalma_quarter(){
	String testname = "test_seasonalma_quarter";
	Logger.logTestStart(testname);
        
        String spc = ""
                + "SERIES\n"
                + "{\n"
                + "PERIOD = 4\n"
                + "}\n"
                + "\n"
                + "X11\n"
                + "{\n"
                + "seasonalma = S3X3  S3X5  S3X9  S3X1\n"
                + "}";
        
        SeasonalFilterOption[] filter = new SeasonalFilterOption[]
            {
                SeasonalFilterOption.S3X3, 
                SeasonalFilterOption.S3X5, 
                SeasonalFilterOption.S3X9, 
                SeasonalFilterOption.S3X1,
            };
        
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        Assert.assertArrayEquals(expected.getX11Specification().getSeasonalFilters(), actual.getX11Specification().getSeasonalFilters());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_seasonalma_year(){
	String testname = "test_seasonalma_year";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "seasonalma = S3X3"
                + "}";
        
        SeasonalFilterOption[] filter = new SeasonalFilterOption[]
            {
                SeasonalFilterOption.S3X3 
            };
        
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        Assert.assertArrayEquals(expected.getX11Specification().getSeasonalFilters(), actual.getX11Specification().getSeasonalFilters());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_seasonalma_month(){
	String testname = "test_seasonalma_month";
	Logger.logTestStart(testname);

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
        
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        Assert.assertArrayEquals(expected.getX11Specification().getSeasonalFilters(), actual.getX11Specification().getSeasonalFilters());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_seasonalma_nothing(){
	String testname = "test_seasonalma_nothing";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "seasonalma = "
                + "}";
        
        SeasonalFilterOption[] filter = new SeasonalFilterOption[]
            {
                SeasonalFilterOption.Msr 
            };
        
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setSeasonalFilters(filter);
        
        Assert.assertArrayEquals(expected.getX11Specification().getSeasonalFilters(), actual.getX11Specification().getSeasonalFilters());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    /*
    @Test
    public void test_seasonalma_invalid(){
	String testname = "test_seasonalma_invalid";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "seasonalma = invalidValue"
                + "}";
        
        X13Specification actual = createActual(spc);

        Assert.assertArrayEquals(expected.getX11Specification().getSeasonalFilters(), actual.getX11Specification().getSeasonalFilters());
        //Assert.assertNull(actual.getX11Specification().getSeasonalFilters()[0]);
       
        assertEquals(expected.getX11Specification(), actual.getX11Specification());
        Logger.logTestEnd(testname);
    }
    */
    
    @Test
    public void test_sigmalim_valid(){
	String testname = "test_sigmalim_valid";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "sigmalim = ( 2.0,2.5 )"
                + "}";
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setLowerSigma(2.0);
        expected.getX11Specification().setUpperSigma(2.5);
        
        assertEquals(expected.getX11Specification().getLowerSigma(), actual.getX11Specification().getLowerSigma(), 0);
        assertEquals(expected.getX11Specification().getUpperSigma(), actual.getX11Specification().getUpperSigma(), 0);
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmalim_invalid_lBiggerThanU(){
	String testname = "test_sigmalim_invalid_lBiggerThanU";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "sigmalim = ( 2.9,2.6 )"
                + "}";
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setLowerSigma(1.5);
        expected.getX11Specification().setUpperSigma(2.5);
        
        assertEquals(expected.getX11Specification().getLowerSigma(), actual.getX11Specification().getLowerSigma(), 0);
        assertEquals(expected.getX11Specification().getUpperSigma(), actual.getX11Specification().getUpperSigma(), 0);
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmalim_valid_missingValues(){
	String testname = "test_sigmalim_valid_missingValues";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "sigmalim = ()"
                + "}";
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setLowerSigma(1.5);
        expected.getX11Specification().setUpperSigma(2.5);
        
        assertEquals(expected.getX11Specification().getLowerSigma(), actual.getX11Specification().getLowerSigma(), 0);
        assertEquals(expected.getX11Specification().getUpperSigma(), actual.getX11Specification().getUpperSigma(), 0);
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmalim_valid_onlyUSigma(){
	String testname = "test_sigmalim_valid_onlyUSigma";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "sigmalim = (,3.7)"
                + "}";
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setLowerSigma(1.5);
        expected.getX11Specification().setUpperSigma(3.7);
        
        assertEquals(expected.getX11Specification().getLowerSigma(), actual.getX11Specification().getLowerSigma(), 0);
        assertEquals(expected.getX11Specification().getUpperSigma(), actual.getX11Specification().getUpperSigma(), 0);
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmalim_invalid_notANumber(){
	String testname = "test_sigmalim_invalid_notANumber";
	Logger.logTestStart(testname);

        String spc = "X11{"
                + "sigmalim = (bla, fasel)"
                + "}";
        
        X13Specification actual = createActual(spc);
        
        expected.getX11Specification().setLowerSigma(1.5);
        expected.getX11Specification().setUpperSigma(2.5);
        
        assertEquals(expected.getX11Specification().getLowerSigma(), actual.getX11Specification().getLowerSigma(), 0);
        assertEquals(expected.getX11Specification().getUpperSigma(), actual.getX11Specification().getUpperSigma(), 0);
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmavec(){
	String testname = "test_sigmavec";
	Logger.logTestStart(testname);

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
        
        X13Specification actual = createActual(spc);
        
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        expected.getX11Specification().setSigmavec(sigmavec);
        
        Assert.assertArrayEquals(expected.getX11Specification().getSigmavec(), actual.getX11Specification().getSigmavec());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmavec_empty(){
	String testname = "test_sigmavec_empty";
	Logger.logTestStart(testname);

        String spc = "x11{"
                     +"calendarsigma=select\n"
                     +"sigmavec=\n"
                     +"}";
        
        SigmavecOption[] sigmavec = new SigmavecOption[]{
            
        };
        
        X13Specification actual = createActual(spc);
        
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        //expected.getX11Specification().setSigmavec(sigmavec);
        
        assertEquals(expected.getX11Specification().getCalendarSigma(), actual.getX11Specification().getCalendarSigma());
        Assert.assertArrayEquals(expected.getX11Specification().getSigmavec(), actual.getX11Specification().getSigmavec());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmavec_calendarsigmaNotSelected(){
	String testname = "test_sigmavec_calendarsigmaNotSelected";
	Logger.logTestStart(testname);

        String spc = "x11{"
                     +"calendarsigma=all\n"
                     +"sigmavec=\n"
                     +"}";
        
        SigmavecOption[] sigmavec = new SigmavecOption[]{
            
        };
        
        X13Specification actual = createActual(spc);
        
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.All);
        //expected.getX11Specification().setSigmavec(sigmavec);
        
        assertEquals(expected.getX11Specification().getCalendarSigma(), actual.getX11Specification().getCalendarSigma());
        Assert.assertArrayEquals(expected.getX11Specification().getSigmavec(), actual.getX11Specification().getSigmavec());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
    @Test
    public void test_sigmavec_all(){
	String testname = "test_sigmavec_all";
	Logger.logTestStart(testname);

        String spc = "x11{"
                     +"calendarsigma=select\n"
                     +"sigmavec=(jan feb mar apr may jun jul aug sep oct nov dec)\n"
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
        
        X13Specification actual = createActual(spc);
        
        
        expected.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        expected.getX11Specification().setSigmavec(sigmavec);
        
        assertEquals(expected.getX11Specification().getCalendarSigma(), actual.getX11Specification().getCalendarSigma());
        Assert.assertArrayEquals(expected.getX11Specification().getSigmavec(), actual.getX11Specification().getSigmavec());
        
        assertEquals(expected, actual);
	Logger.logTestEnd(testname);
    }
    
}
