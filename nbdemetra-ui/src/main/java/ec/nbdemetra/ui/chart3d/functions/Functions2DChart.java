/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.nbdemetra.ui.chart3d.functions;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IParametersDomain;
import ec.ui.ATsView;
import ec.ui.chart.BasicXYDataset;
import ec.ui.chart.TsCharts;
import ec.ui.view.JChartPanel;
import ec.util.chart.ColorScheme;
import ec.util.chart.ColorScheme.KnownColor;
import ec.util.chart.swing.Charts;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * 2D Chart panel used when the likelihood function contains only 1 parameter
 *
 * @author Mats Maggi
 */
public class Functions2DChart extends ATsView {

    private JChartPanel panel;
    private JFreeChart chart;
    private IFunction function;
    private IFunctionInstance maxFunction;
    private float epsilon = 0.2f;
    private int steps;
    private final XYLineAndShapeRenderer functionRenderer;
    private final XYLineAndShapeRenderer optimumRenderer;

    public Functions2DChart(IFunction f, IFunctionInstance maxF, int steps) {
        super();
        setLayout(new BorderLayout());

        function = f;
        maxFunction = maxF;
        
        if (steps < ConfigurationToolBar.MIN_STEPS || steps > ConfigurationToolBar.MAX_STEPS) {
            throw new IllegalArgumentException("Number of steps must be between " 
                    + ConfigurationToolBar.MIN_STEPS + " and " + ConfigurationToolBar.MAX_STEPS + " !");
        }
        
        this.steps = steps;

        functionRenderer = new XYLineAndShapeRenderer(true, false);
        functionRenderer.setAutoPopulateSeriesPaint(false);
        functionRenderer.setBasePaint(themeSupport.getLineColor(KnownColor.BLUE));

        optimumRenderer = new XYLineAndShapeRenderer(false, true);
        optimumRenderer.setAutoPopulateSeriesPaint(false);
        optimumRenderer.setAutoPopulateSeriesShape(false);
        optimumRenderer.setBaseShape(new Ellipse2D.Double(-2, -2, 4, 4));
        optimumRenderer.setBasePaint(themeSupport.getLineColor(KnownColor.RED));
        optimumRenderer.setBaseShapesFilled(true);

        panel = new JChartPanel(null);
        chart = createChart();
    }

    /**
     * Generates the data and displays it in the chart. Calculates the points
     * ("steps" total points) and the optimum of the function
     */
    public void generateData() {
        if (function == null || maxFunction == null) {
            throw new IllegalArgumentException("The given functions can't be null !");
        }
        
        BasicXYDataset dataset = new BasicXYDataset();
        BasicXYDataset optimumDataset = new BasicXYDataset();
        double[] dataX = new double[steps];
        double[] dataY = new double[steps];

        final IReadDataBlock parameters = maxFunction.getParameters();
        DataBlock p = new DataBlock(parameters);
        final IParametersDomain d = function.getDomain();

        float xMin = ((float) p.get(0) - epsilon);
        float xMax = ((float) p.get(0) + epsilon);
        float stepX = (xMax - xMin) / steps;    // Calculates the "distance" between each point

        // Optimum point of the max likelihood function
        double optiX = parameters.get(0);
        double optiY = maxFunction.getValue();

        for (int i = 0; i < steps; i++) {
            // Value on the x axis (min X value + index* (distance between points)
            float x = xMin + i * stepX;
            float y = Float.NaN;
            p.set(0, x);    // Setting new value of the 1st param (X)

            // Calculating the Y value
            try {
                if (d.checkBoundaries(p)) {
                    y = (float) function.evaluate(p).getValue();
                }
            } catch (Exception err) {
                y = Float.NaN;
            }

            if (Float.isInfinite(y)) {
                y = Float.NaN;
            }

            dataX[i] = x;
            dataY[i] = y;
        }

        // Creates the 2 datasets (function + optimum point)
        BasicXYDataset.Series serie = BasicXYDataset.Series.of("f(" + d.getDescription(0) + ")", dataX, dataY);
        BasicXYDataset.Series optimum = BasicXYDataset.Series.of("Optimum", new double[]{optiX}, new double[]{optiY});
        dataset.addSeries(serie);
        optimumDataset.addSeries(optimum);

        XYPlot plot = chart.getXYPlot();
        configureAxis(plot);
        plot.setDataset(0, dataset);
        plot.setDataset(1, optimumDataset);

        panel.setChart(chart);
        add(panel, BorderLayout.CENTER);

        onColorSchemeChange();
    }

    private void configureAxis(XYPlot plot) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRange(true);
        xAxis.setAutoRangeIncludesZero(false);
        plot.setDomainAxis(xAxis);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(yAxis);
    }

    private JFreeChart createChart() {
        XYPlot plot = new XYPlot();

        plot.setDataset(0, Charts.emptyXYDataset());
        plot.setRenderer(0, functionRenderer);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, Charts.emptyXYDataset());
        plot.setRenderer(1, optimumRenderer);
        plot.mapDatasetToDomainAxis(1, 0);
        plot.mapDatasetToRangeAxis(1, 0);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        JFreeChart result = new JFreeChart("", TsCharts.CHART_TITLE_FONT, plot, false);

        LegendTitle legend = new LegendTitle(result.getPlot());
        legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
        legend.setFrame(new LineBorder());
        legend.setBackgroundPaint(Color.white);
        legend.setPosition(RectangleEdge.BOTTOM);
        result.addLegend(legend);

        result.setPadding(TsCharts.CHART_PADDING);
        return result;
    }

    /**
     * Sets the value of the epsilon parameter. Epsilon = range of the function
     * (X values from max-eps to max+eps)
     *
     * @param eps
     */
    public void setEpsilon(float eps) {
        if (eps < ConfigurationToolBar.MIN_EPS || eps > ConfigurationToolBar.MAX_EPS) {
            throw new IllegalArgumentException("Epsilon must be between " 
                    + ConfigurationToolBar.MIN_EPS + " and " + ConfigurationToolBar.MAX_EPS + " !");
        }
        epsilon = eps;
        generateData();
    }

    /**
     * Sets the number of points to calculate and display the function
     *
     * @param steps
     */
    public void setSteps(int steps) {
        if (steps < ConfigurationToolBar.MIN_STEPS || steps > ConfigurationToolBar.MAX_STEPS) {
            throw new IllegalArgumentException("Number of steps must be between " 
                    + ConfigurationToolBar.MIN_STEPS + " and " + ConfigurationToolBar.MAX_STEPS + " !");
        }
        this.steps = steps;
        generateData();
    }

    @Override
    protected void onTsChange() {
        // Do nothing
    }

    @Override
    protected void onDataFormatChange() {
        // Do nothing
    }

    @Override
    protected void onColorSchemeChange() {
        functionRenderer.setBasePaint(themeSupport.getLineColor(ColorScheme.KnownColor.BLUE));
        optimumRenderer.setBasePaint(themeSupport.getLineColor(KnownColor.RED));

        XYPlot mainPlot = chart.getXYPlot();
        mainPlot.setBackgroundPaint(themeSupport.getPlotColor());
        mainPlot.setDomainGridlinePaint(themeSupport.getGridColor());
        mainPlot.setRangeGridlinePaint(themeSupport.getGridColor());
        chart.setBackgroundPaint(themeSupport.getBackColor());
    }
}
