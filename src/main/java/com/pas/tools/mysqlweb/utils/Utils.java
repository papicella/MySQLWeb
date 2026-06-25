package com.pas.tools.mysqlweb.utils;

import java.sql.Connection;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class Utils
{
    public static final String SESSION_CONNECTION_ID = "sessionId";

    public static String getConnectionSessionId(HttpSession session)
    {
        return (String) session.getAttribute(SESSION_CONNECTION_ID);
    }

    public static String applicationIndex ()
    {
        String instanceIndex = "N/A";

        try
        {
            instanceIndex = getVcapApplicationMap().getOrDefault("instance_index", "N/A").toString();
        }
        catch (Exception ex)
        {

        }

        return instanceIndex;
    }

    static private Map getVcapApplicationMap() throws Exception {
        return getEnvMap("VCAP_APPLICATION");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map getEnvMap(String vcap) throws Exception {
        String vcapEnv = System.getenv(vcap);
        ObjectMapper mapper = new ObjectMapper();

        if (vcapEnv != null) {
            Map<String, ?> vcapMap = mapper.readValue(vcapEnv, Map.class);
            return vcapMap;
        }

        return new HashMap<String, String>();
    }

    public static Map<String, String> jvmPropertyMap ()
    {
        Properties props = System.getProperties();
        Map<String, String> map = new HashMap<String, String>((Map) props);

        return map;
    }

    public static boolean verifyConnection(HttpServletResponse response, HttpSession session) throws Exception
    {
        String sessionId = getConnectionSessionId(session);
        if (sessionId == null)
        {
            response.sendRedirect("/");
            return true;
        }

        Connection conn = AdminUtil.getConnection(sessionId);
        if (conn.isClosed() || !conn.isValid(5))
        {
            log.info("Connection is closed or no longer valid for session {}", sessionId);
            response.sendRedirect("/");
            return true;
        }

        return false;
    }

    static public Map<String, Long> getSchemaMap ()
    {
        Map<String, Long> schemaMap = new HashMap<String, Long>();

        schemaMap.put("Table", 0L);
        schemaMap.put("View", 0L);
        schemaMap.put("Index", 0L);
        schemaMap.put("Constraint", 0L);

        return schemaMap;
    }
}
