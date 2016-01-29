package cern.c2mon.web.manager.statistics.daqlog.charts;

import java.sql.SQLException;
import java.util.List;

import cern.c2mon.web.manager.statistics.daqlog.values.IChartCollectionValue;

public class JFreeBarChartCollection extends JFreeWebChartCollection {

    /**
     * Set the chart class at initialisation.
     */
    public JFreeBarChartCollection() {
        super();
        chartClass = JFreeBarChart.class;
    }


    @Override
    public List<IChartCollectionValue> getValuesFromDatabase(String tableName) throws SQLException {
        //retrieve the chart values from the database
        return mapper.getBarChartCollectionData(tableName);
    }
}
