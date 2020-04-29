package com.xing.qa.selenium.grid.node;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;

/**
* NodeReporter
*
* @author Jens Hausherr (jens.hausherr@xing.com)
*/
class NodeReporter extends BaseSeleniumReporter {

    private final MonitoringWebProxy proxy;

    public NodeReporter(String remoteHostName, InfluxDB influxdb, String database, MonitoringWebProxy monitoringWebProxy) {
        super(remoteHostName, influxdb, database);
        this.proxy = monitoringWebProxy;
    }

    @Override
    protected void report() {
        log.log(Level.INFO, String.format("Reporting: node.%s.measure", SerieNames.utilization));

        Point point = Point.measurement(String.format("node.%s.measure", SerieNames.utilization)).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .field("host", remoteHostName).field("used", proxy.getTotalUsed()).field("total", proxy.getMaxNumberOfConcurrentTestSessions())
                .field("normalized", proxy.getResourceUsageInPercent()).build();
        write(point);
    }
}