/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.Ramp;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Nina Gonschorreck
 */
public class WinX12SpecSeperator_REGRESSION_Test {

    private X13Specification expected;

    public WinX12SpecSeperator_REGRESSION_Test() {
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
    public void test_default() {
        String testname = "test_default";
        Logger.logTestStart(testname);

        String spc
                = "REGRESSION\n"
                + "{\n"
                + "}";

        X13Specification actual = createActual(spc);

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_const() {
        String testname = "test_variables_const";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = const"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getRegArimaSpecification().getArima().setMean(true);

        assertEquals(expected.getRegArimaSpecification().getArima().isMean(),
                actual.getRegArimaSpecification().getArima().isMean());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_seasonal() {
        String testname = "test_variables_seasonal";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = seasonal"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getRegArimaSpecification().getArima().setBP(0);
        expected.getRegArimaSpecification().getArima().setBD(1);
        expected.getRegArimaSpecification().getArima().setBQ(1);

        assertEquals(expected.getRegArimaSpecification().getArima().getBP(),
                actual.getRegArimaSpecification().getArima().getBP());

        assertEquals(expected.getRegArimaSpecification().getArima().getBD(),
                actual.getRegArimaSpecification().getArima().getBD());

        assertEquals(expected.getRegArimaSpecification().getArima().getBQ(),
                actual.getRegArimaSpecification().getArima().getBQ());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_td() {
        String testname = "test_variables_td";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = td"
                + "}";

        X13Specification actual = createActual(spc);

        TradingDaysSpec td = expected.getRegArimaSpecification().getRegression().getTradingDays();
        td.setTradingDaysType(TradingDaysType.TradingDays);
        td.setTest(RegressionTestSpec.None);
        td.setHolidays(null);
        td.setUserVariables(null);

        assertEquals(expected.getRegArimaSpecification().getRegression().getTradingDays().getTradingDaysType(),
                actual.getRegArimaSpecification().getRegression().getTradingDays().getTradingDaysType());

        assertEquals(expected.getRegArimaSpecification().getRegression().getTradingDays().getTest(),
                actual.getRegArimaSpecification().getRegression().getTradingDays().getTest());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_td1coeff() {
        String testname = "test_variables_td1coeff";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = td1coeff"
                + "}";

        X13Specification actual = createActual(spc);

        TradingDaysSpec td = expected.getRegArimaSpecification().getRegression().getTradingDays();
        td.setTradingDaysType(TradingDaysType.WorkingDays);
        td.setTest(RegressionTestSpec.None);
        td.setHolidays(null);
        td.setUserVariables(null);

        assertEquals(expected.getRegArimaSpecification().getRegression().getTradingDays().getTradingDaysType(),
                actual.getRegArimaSpecification().getRegression().getTradingDays().getTradingDaysType());

        assertEquals(expected.getRegArimaSpecification().getRegression().getTradingDays().getTest(),
                actual.getRegArimaSpecification().getRegression().getTradingDays().getTest());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_easter() {
        String testname = "test_variables_easter";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = easter[8]"
                + "}";

        X13Specification actual = createActual(spc);

        MovingHolidaySpec easter = new MovingHolidaySpec();
        easter.setType(MovingHolidaySpec.Type.Easter);
        easter.setTest(RegressionTestSpec.None);
        easter.setW(8);
        expected.getRegArimaSpecification().getRegression().add(easter);

        assertEquals(expected.getRegArimaSpecification().getRegression().getEaster().getType(),
                actual.getRegArimaSpecification().getRegression().getEaster().getType());

        assertEquals(expected.getRegArimaSpecification().getRegression().getEaster().getTest(),
                actual.getRegArimaSpecification().getRegression().getEaster().getTest());

        assertEquals(expected.getRegArimaSpecification().getRegression().getEaster().getW(),
                actual.getRegArimaSpecification().getRegression().getEaster().getW());
        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_tdstock() {
        String testname = "test_variables_tdstock";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = tdstock[31]"
                + "}";

        X13Specification actual = createActual(spc);

        TradingDaysSpec td = expected.getRegArimaSpecification().getRegression().getTradingDays();
        td.setStockTradingDays(31);

        assertEquals(expected.getRegArimaSpecification().getRegression().getTradingDays().getStockTradingDays(),
                actual.getRegArimaSpecification().getRegression().getTradingDays().getStockTradingDays());

        assertEquals(expected.getRegArimaSpecification().getRegression().getTradingDays().getTradingDaysType(),
                actual.getRegArimaSpecification().getRegression().getTradingDays().getTradingDaysType());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_rp() {
        String testname = "test_variables_rp";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = rp2019.1-2019.4"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getRegArimaSpecification().getRegression().add(
                new Ramp(new Day(2019, Month.January, 0), new Day(2019, Month.April, 29))
        );

        assertArrayEquals(expected.getRegArimaSpecification().getRegression().getRamps(),
                actual.getRegArimaSpecification().getRegression().getRamps());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_ao() {
        String testname = "test_variables_ao";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = ao2019.1"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getRegArimaSpecification().getRegression().add(
                new OutlierDefinition(new Day(2019, Month.January, 0), OutlierType.AO)
        );

        assertArrayEquals(expected.getRegArimaSpecification().getRegression().getOutliers(),
                actual.getRegArimaSpecification().getRegression().getOutliers());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_ls() {
        String testname = "test_variables_ls";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = ls2019.1"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getRegArimaSpecification().getRegression().add(
                new OutlierDefinition(new Day(2019, Month.January, 0), OutlierType.LS)
        );

        assertArrayEquals(expected.getRegArimaSpecification().getRegression().getOutliers(),
                actual.getRegArimaSpecification().getRegression().getOutliers());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_so() {
        String testname = "test_variables_so";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = so2019.1"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getRegArimaSpecification().getRegression().add(
                new OutlierDefinition(new Day(2019, Month.January, 0), OutlierType.SO)
        );

        assertArrayEquals(expected.getRegArimaSpecification().getRegression().getOutliers(),
                actual.getRegArimaSpecification().getRegression().getOutliers());

        Logger.logTestEnd(testname);
    }

    @Test
    public void test_variables_tc() {
        String testname = "test_variables_tc";
        Logger.logTestStart(testname);

        String spc = "REGRESSION{"
                + "variables = tc2019.1"
                + "}";

        X13Specification actual = createActual(spc);

        expected.getRegArimaSpecification().getRegression().add(
                new OutlierDefinition(new Day(2019, Month.January, 0), OutlierType.TC)
        );

        assertArrayEquals(expected.getRegArimaSpecification().getRegression().getOutliers(),
                actual.getRegArimaSpecification().getRegression().getOutliers());

        Logger.logTestEnd(testname);
    }
}
