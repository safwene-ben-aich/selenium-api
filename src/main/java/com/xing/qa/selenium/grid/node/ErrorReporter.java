package com.xing.qa.selenium.grid.node;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openqa.grid.common.exception.RemoteException;

/**
* ErrorReporter
*
* @author Jens Hausherr (jens.hausherr@xing.com)
*/
class ErrorReporter extends BaseSeleniumReporter {

    private final RemoteException exception;

    public ErrorReporter(String remoteHostName, InfluxDB influxdb, String database, RemoteException ex) {
        super(remoteHostName, influxdb, database);
        this.exception = ex;
    }

    @Override
    protected void report() {
    	log.log(Level.INFO, String.format("node.errors", SerieNames.node_errors));
        Point point = Point.measurement("node.errors").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .field("host", remoteHostName).field("error", exception.getClass().getName()).field("message", exception.getMessage()).build();
        write(point);
    }

}