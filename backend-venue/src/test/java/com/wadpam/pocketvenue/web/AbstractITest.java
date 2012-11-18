package com.wadpam.pocketvenue.web;

import org.junit.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Base class for GAE, Spring, RestTemplate integration tests.
 * Contains convenience methods and help classes.
 * @author mattiaslevin
 */
public abstract class AbstractITest {

    // Spring rest template
    RestTemplate template;

    protected final String BASE_URL = getBaseUrl();

    // All sub classes must implement this method
    protected abstract String getBaseUrl();

    @Before
    public void setUp() {
        template = new RestTemplate();

        // Configure an error handler that does not throw exceptions
        // All http codes are handled and tested using asserts
        template.setErrorHandler(new DoNothingResponseErrorHandler());
    }


    // A error handler for Spring RestTemplate that ignores all http error codes.
    protected class DoNothingResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        protected boolean hasError(HttpStatus statusCode) {
            return false;
        }
    }


    // Extract the headers from from the response in a RestTemplate execute method.
    protected class HeaderResponseExtractor implements ResponseExtractor<HttpHeaders> {
        @Override
        public HttpHeaders extractData(ClientHttpResponse clientHttpResponse) throws IOException {

            // Check that it is a redirect
            assertTrue("Redirect",clientHttpResponse.getStatusCode() == HttpStatus.FOUND);

            return clientHttpResponse.getHeaders();
        }
    }


    // This class writes a map to the request body in a RestTemplate execute method.
    protected class RequestCallBackBodyWriter implements RequestCallback {

        MultiValueMap<String, Object> map;

        protected RequestCallBackBodyWriter(MultiValueMap<String, Object> map) {
            this.map = map;
        }

        @Override
        public void doWithRequest(ClientHttpRequest clientHttpRequest) throws IOException {
            StringBuilder builder = new StringBuilder();

            for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
                for (Object value : entry.getValue())
                    builder.append(entry.getKey()).append("=").append(value).append("&");
            }

            PrintWriter out = new PrintWriter(clientHttpRequest.getBody());
            out.write(builder.toString()); // TODO add url encoding
            out.flush();
            out.close();
        }
    }

    // Make a post and follow the redirect
    protected  <T> ResponseEntity<T> postAndFollowRedirect(String url, MultiValueMap<String, Object> map, Class<T> clazz) throws MalformedURLException {

        HttpHeaders headers = template.execute(url, HttpMethod.POST,
                new RequestCallBackBodyWriter(map),
                new HeaderResponseExtractor());

        assertNotNull("Header", headers);
        assertNotNull(headers.getLocation().toURL());

        ResponseEntity<T> entity = template.getForEntity(headers.getLocation().toURL().toExternalForm(), clazz);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());

        return entity;
    }

    // TODO Create builder

}
