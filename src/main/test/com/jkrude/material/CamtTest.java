package com.jkrude.material;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import org.junit.Test;

public class CamtTest {

    @Test
    public void testGenerateLineChart(){
        List<Data<Number, Number>> lineChartData = TestData.getLineChartData();
    }

}