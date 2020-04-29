package com.xing.qa.selenium.grid.node;

import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.openqa.grid.internal.ExternalSessionKey;
import org.openqa.grid.internal.TestSession;

/**
 * CommandReporter
 *
 * @author Jens Hausherr (jens.hausherr@xing.com)
 */
class CommandReporter extends BaseSeleniumReporter {

    protected final TestSession session;
    protected final ContentSnoopingRequest request;
    protected final HttpServletResponse response;
    protected final ReportType type;

    public CommandReporter(String remoteHostName, InfluxDB influxdb, String database, TestSession session,
            ContentSnoopingRequest request, HttpServletResponse response, ReportType type) {
        super(remoteHostName, influxdb, database);
        this.type = type;
        this.request = request;
        this.session = session;
        this.response = response;
    }

    protected void report() {
        ExternalSessionKey esk = session.getExternalKey();
        String sessionKey = null;
        if (esk != null) {
            sessionKey = esk.getKey();
        }
        log.log(Level.INFO, String.format((String.format("session.cmd.%s.measure",type)), SerieNames.command));        
        Point point = Point.measurement(String.format("session.cmd.%s.measure",type)).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .field("host", Objects.toString(remoteHostName,"")).field("ext_key",Objects.toString(sessionKey, "")).field("int_key", Objects.toString(session.getInternalKey(),""))
                .field("forwarding", Objects.toString(session.isForwardingRequest(),"")).field("orphaned", Objects.toString(session.isOrphaned(),""))
                .field("inactivity", Objects.toString(session.getInactivityTime(),"")).field("cmd_method", Objects.toString(request.getMethod(),""))
                .field("cmd_action", Objects.toString(request.getPathInfo(),"")).field("cmd", Objects.toString(request.getContent(), "")).build();

        write(point);
    }
}