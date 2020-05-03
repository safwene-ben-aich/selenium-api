package com.xing.qa.selenium.grid.influxdb;

import static java.lang.String.format;

import java.util.logging.Logger;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

public class InfluxDBConnector {
    
    public static final Logger LOG = Logger.getLogger(InfluxDBConnector.class.getName());
    
    public static final String DATABASE = envOr("IFXDB_DB", "selenium");

    public static final String URL = format("http://%s:%s", envOr("IFXDB_HOST", "10.200.222.60"), envOr("IFXDB_PORT", "8086"));
    public static final InfluxDB INFLUX_DB = InfluxDBFactory.connect(
            URL,
            envOr("IFXDB_USER", "root"),
            envOr("IFXDB_PASSWD", "root"));

    static {
        LOG.info(String.format("Reporting to: %s/db/%s", URL, DATABASE));
        INFLUX_DB.setLogLevel(InfluxDB.LogLevel.NONE);
    }

    public static String envOr(String envVar, String defaultVal) {
        String val = System.getenv(envVar);
        if (val == null) return defaultVal;
        return val;
    }

}
