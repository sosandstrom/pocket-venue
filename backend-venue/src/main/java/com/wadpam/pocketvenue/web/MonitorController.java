package com.wadpam.pocketvenue.web;

import com.google.appengine.api.NamespaceManager;
import com.wadpam.pocketvenue.json.JMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author os
 */
@Controller
@RequestMapping(value={"monitor", "{domain}/monitor"})
public class MonitorController {
    static final Logger LOG = LoggerFactory.getLogger(MonitorController.class);
    
    static final String PREFIX = "Current IP Address: ";
    
    @RequestMapping(value={"", "v10"}, method=RequestMethod.GET)
    public ResponseEntity<JMonitor> getV10() {
        final JMonitor body = new JMonitor();
        body.setCurrentTimeMillis(System.currentTimeMillis());
        body.setNamespace(NamespaceManager.get());
        body.setVersion(System.getProperty("maven.application.version", "N/A"));
        return new ResponseEntity<JMonitor>(body, HttpStatus.OK);
    }
    
    @RequestMapping(value="v11", method=RequestMethod.GET)
    public ResponseEntity<JMonitor> getV11() {
        final ResponseEntity<JMonitor> returnValue = getV10();
        RestTemplate template = new RestTemplate();
        final ResponseEntity<String> responseEntity = template.getForEntity("http://dns.loopia.se/checkip/checkip.php", String.class);
        if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            String response = responseEntity.getBody();
            if (null != response && -1 < response.lastIndexOf(PREFIX)) {
                int beginIndex = response.lastIndexOf(PREFIX) + PREFIX.length();
                int endIndex = response.indexOf("</body>", beginIndex);
                final JMonitor body = returnValue.getBody();
                body.setIpAddress(response.substring(beginIndex, endIndex));
            }
        }
        return returnValue;
    }
    
}
