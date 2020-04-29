package com.xing.qa.selenium.grid.node;

import static java.lang.String.format;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.openqa.grid.internal.ExternalSessionKey;
import org.openqa.grid.internal.TestSession;
import org.yaml.snakeyaml.emitter.ScalarAnalysis;

/**
 * Session Reporter
 *
 * @author Jens Hausherr (jens.hausherr@xing.com)
 */
class SessionReporter extends BaseSeleniumReporter {
    private final TestSession session;
    private final ReportType type;

    public SessionReporter(String remoteHostName, InfluxDB influxdb, String database, TestSession session,
            ReportType type) {
        super(remoteHostName, influxdb, database);
        this.session = session;
        this.type = type;
    }

    @Override
    protected void report() {
        ExternalSessionKey esk = session.getExternalKey();
        String sessionKey = null;

        if (esk != null) {
            sessionKey = esk.getKey();
        }

        Builder srep = Point.measurement("session.event.measure");

        final Boolean forwardingRequest = session.isForwardingRequest();
        final Boolean orphaned = session.isOrphaned();
        final Long inactivityTime = session.getInactivityTime();
        final long time = System.currentTimeMillis();
        if (ReportType.timeout != type) {
            srep.time(time, TimeUnit.MILLISECONDS)
            		.field("host", 	  Objects.toString(remoteHostName, ""))
            		.field("type", 	  Objects.toString(type.toString(), ""))
                    .field("ext_key", Objects.toString(sessionKey, ""))
                    .field("int_key", Objects.toString(session.getInternalKey(),""))
                    .field("forwarding", forwardingRequest)
                    .field("orphaned",   orphaned)
                    .field("inactivity", inactivityTime);
        } else {
            srep.time(time, TimeUnit.MILLISECONDS)
            		.field("host", 	  Objects.toString(remoteHostName, ""))
            		.field("type", 	  Objects.toString(type.toString(), ""))
                    .field("ext_key", Objects.toString(sessionKey, ""))
                    .field("int_key", Objects.toString(session.getInternalKey(),""))
                    .field("forwarding", forwardingRequest)
                    .field("orphaned",   orphaned)
                    .field("inactivity", inactivityTime)
                    .field("browser_starting", String.valueOf(session.getInternalKey() == null));
        }

        Builder req = Point.measurement(format("session.cap.requested.%s.measure", type));

        for (Map.Entry<String, Object> rcap : session.getRequestedCapabilities().entrySet()) {
            req.time(time, TimeUnit.MILLISECONDS).field("host", Objects.toString(remoteHostName, "")).field("ext_key", Objects.toString(sessionKey, ""))
                    .field("int_key", Objects.toString(session.getInternalKey(),"")).field("forwarding", forwardingRequest)
                    .field("orphaned", orphaned).field("inactivity", Objects.toString(inactivityTime,"")).field("capability", Objects.toString(rcap.getKey(), ""))
                    .field("val", Objects.toString(rcap.getValue(), ""));
        }

        Builder prov = Point.measurement(format("session.cap.provided.%s.measure", type));

        for (Map.Entry<String, Object> scap : session.getSlot().getCapabilities().entrySet()) {
            prov.time(time, TimeUnit.MILLISECONDS).field("host", Objects.toString(remoteHostName,"")).field("ext_key", Objects.toString(sessionKey,""))
                    .field("int_key", Objects.toString(session.getInternalKey(), "")).field("forwarding", forwardingRequest)
                    .field("orphaned", orphaned).field("inactivity", inactivityTime).field("capability", Objects.toString(scap.getKey(), ""))
                    .field("val", Objects.toString(scap.getValue(), ""));
        }
        
        log.log(Level.INFO, String.format("session.event.measure", type), SerieNames.session);
        log.log(Level.INFO, String.format("session.cap.requested.%s.measure", type), SerieNames.session);
        log.log(Level.INFO, String.format("session.cap.provided.%s.measure", type), SerieNames.session);
        
        
        
        
        
        write(srep.build(), req.build(), prov.build());

    }

}