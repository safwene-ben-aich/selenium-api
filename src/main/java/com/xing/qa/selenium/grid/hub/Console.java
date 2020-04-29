package com.xing.qa.selenium.grid.hub;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.web.Hub;
import org.openqa.grid.web.servlet.RegistryBasedServlet;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.server.ActiveSessions;

/**
 * Console information nad more as JSON
 *
 * @author Jens Hausherr (jens.hausherr@xing.com)
 */
public class Console extends RegistryBasedServlet {
    static final long serialVersionUID = -1;

    private final Logger log = Logger.getLogger(getClass().getName());
    private String coreVersion;

    public Console() {
        this(null);
    }

    public Console(GridRegistry registry) {
        super(registry);

        coreVersion = new BuildInfo().getReleaseLabel();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/requests".equals(req.getPathInfo())) {
                sendJson(pendingRequests(), req, resp);
            } else {
                sendJson(status(), req, resp);
            }
        } catch (JSONException je) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(500);
            JSONObject error = new JSONObject();

            try {
                error.put("message", je.getMessage());
                error.put("location", je.getStackTrace());
                error.write(resp.getWriter());
            } catch (JSONException e1) {
              log.log(Level.WARNING, "Failed to write error response", e1);
            }

        }

    }

    protected void sendJson(JSONObject jo, HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(200);
        Writer w = null;
        try {
            w = resp.getWriter();
            jo.write(w);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error writing response", e);
        } catch (JSONException e) {
            log.log(Level.WARNING, "Failed to serialize JSON response", e);
        }
    }

    protected JSONObject pendingRequests() throws JSONException {
        JSONObject pending = new JSONObject();

        int activeSessions = getRegistry().getActiveSessions().size();

        ArrayList<Map<String, Object>>  requestedCapabilities = new ArrayList<>();

        for (TestSession testSession : getRegistry().getActiveSessions()) {
            try {
                requestedCapabilities.add(testSession.getRequestedCapabilities());
            } catch (Exception e) {}
        }

        pending.put("active", activeSessions);
        pending.put("requested_capabilities", requestedCapabilities);



        return pending;
    }

    protected JSONObject status()
            throws JSONException {
            JSONObject status = new JSONObject();

            Hub h = getRegistry().getHub();

            List<JSONObject> nodes = new ArrayList<JSONObject>();

            for (RemoteProxy proxy : getRegistry().getAllProxies()) {
                try {
                    JSONRenderer beta = new WebProxyJsonRenderer(proxy);
                    nodes.add(beta.render());
                } catch (Exception e) {}
            }



            status.put("version", coreVersion);
            JSONObject configuration = new JSONObject(getRegistry().getHub().getConfiguration().toJson());
            status.put("configuration", configuration);
            status.put("host", h.getConfiguration().host);
            status.put("port", h.getConfiguration().port);
            status.put("registration_url", h.getRegistrationURL());
            status.put("nodes", nodes);
            status.put("requests", pendingRequests());

            return status;
    }
}
