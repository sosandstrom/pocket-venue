package com.wadpam.pocketvenue.web;

import com.wadpam.open.json.JMonitor;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the monitor controller.
 * @author sosandstrom
 */
public class MonitorITest extends AbstractITest {

    @Override
    protected String getBaseUrl() {
        return "http://localhost:8234/api/test/";
    }


    @Test
    public void testMonitor() {
        ResponseEntity<JMonitor> entity = template.getForEntity(BASE_URL + "monitor/v10", JMonitor.class);
        assertEquals("getMonitor", HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    public void testMonitorNamespace() {
        ResponseEntity<JMonitor> entity = template.getForEntity(BASE_URL + "monitor/v10", JMonitor.class);
        assertEquals("getMonitor", HttpStatus.OK, entity.getStatusCode());
        assertEquals("getMonitor namespace", "test", entity.getBody().getNamespace());
    }

    @Test
    public void testMonitorJsonp() {
        ResponseEntity<String> entity = template.getForEntity(BASE_URL + "monitor/v10?callback=itest", String.class);
        assertEquals("getMonitorJsonp", HttpStatus.OK, entity.getStatusCode());
        assertTrue("getMonitorJsonp callback", entity.getBody().startsWith("itest({"));
    }
}
