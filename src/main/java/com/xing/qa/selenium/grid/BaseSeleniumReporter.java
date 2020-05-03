package com.xing.qa.selenium.grid;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

/**
 * BaseSeleniumReporter
 *
 * @author Jens Hausherr (jens.hausherr@xing.com)
 */
public abstract class BaseSeleniumReporter implements Runnable {

    protected final String remoteHostName;
    protected final Logger log = Logger.getLogger(getClass().getName());
    private final String database;
    private final InfluxDB influxdb;
   

    public BaseSeleniumReporter(String remoteHostName, InfluxDB influxdb, String database) {
        this.remoteHostName = remoteHostName;
        this.influxdb = influxdb;
        this.database = database;
    }

    @Override
    public final void run() {
        try {
            report();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    protected abstract void report();

    protected void write(Point... points) {
        BatchPoints batchPoints = BatchPoints.database(database).points(points).retentionPolicy("autogen")
                .consistency(ConsistencyLevel.ANY).build();
        influxdb.write(batchPoints);
    }
}