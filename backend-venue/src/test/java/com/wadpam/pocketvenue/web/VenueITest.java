package com.wadpam.pocketvenue.web;

import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.server.json.JRestError;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.MalformedURLException;

import static org.junit.Assert.*;

/**
 * Integration tests for venues.
 * @author mattiaslevin
 */
public class VenueITest extends AbstractITest {
    static final Logger LOG = LoggerFactory.getLogger(VenueITest.class);


    @Override
    protected String getBaseUrl() {
        return "http://localhost:8234/api/test/";
    }


    @Test
    public void createVenueWithNameOnly() throws MalformedURLException {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue1");
        ResponseEntity<JVenue> entity = postAndFollowRedirect(BASE_URL + "venue", map, JVenue.class);
        assertTrue("Name", entity.getBody().getName().equals("Venue1"));
    }

    @Test
    public void updateVenueName() throws MalformedURLException {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue2");
        ResponseEntity<JVenue> entity = postAndFollowRedirect(BASE_URL + "venue", map, JVenue.class);
        assertTrue("Name", entity.getBody().getName().equals("Venue2"));

        // Update venue name
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue2 updated");
        entity = postAndFollowRedirect(BASE_URL + "venue", map, JVenue.class);
        assertTrue("Name", entity.getBody().getName().equals("Venue2 updated"));
    }


    @Test
    public void createVenueWithAllProperties() throws MalformedURLException {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue3");
        setDefaultVenueValues(map);
        ResponseEntity<JVenue>entity = postAndFollowRedirect(BASE_URL + "venue", map, JVenue.class);
        assertTrue("Name", entity.getBody().getName().equals("Venue3"));
        assertDefaultVenueValues(entity.getBody());
    }

    @Test
    public void deleteVenue() throws MalformedURLException {

        // Create
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue4");
        ResponseEntity<JVenue> entity = postAndFollowRedirect(BASE_URL + "venue", map, JVenue.class);
        assertTrue("Name", entity.getBody().getName().equals("Venue4"));

        // Delete venue
        String urlString = BASE_URL + "venue/" + entity.getBody().getId();
        template.delete(urlString);

        // Check that it is deleted
        ResponseEntity<JRestError> restError = template.getForEntity(urlString, JRestError.class);
        assertEquals("Http response 404", HttpStatus.NOT_FOUND, restError.getStatusCode());
    }

    @Test
    public void getAllVenues() throws InterruptedException {

        // Create a 5rd venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue5");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Create a 6th venue
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue6");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        Thread.sleep(2000);

        // Get all venues (pagesize 10)
        ResponseEntity<JVenueCursorPage> cursorEntity = template.getForEntity(BASE_URL + "venue", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 5);
    }

    @Test
    public void getAllVenuesWithPagination() {

        // Get first 2 venues
        ResponseEntity<JVenueCursorPage> entity = template.getForEntity(BASE_URL + "venue?pagesize=2", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
        assertTrue("Number of venues returned", entity.getBody().getItems().size() == 2);

        String cursor = entity.getBody().getCursor();
        assertNotNull("Cursor null", cursor);
        assertFalse("Cursor empty", cursor.isEmpty());

        // Get the next 2 venues
        entity = template.getForEntity(BASE_URL + "venue?pagesize=2&cursor={cursor}", JVenueCursorPage.class, cursor);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
        assertTrue("Number of venues returned", entity.getBody().getItems().size() == 2);

        cursor = entity.getBody().getCursor();
        assertNotNull("Cursor null", cursor);
        assertFalse("Cursor empty", cursor.isEmpty());
    }

    @Test
    public void getVenuesForParent() throws MalformedURLException {

        // Create parent venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue7");
        ResponseEntity<JVenue> entity = postAndFollowRedirect(BASE_URL + "venue", map, JVenue.class);
        assertTrue("Name", entity.getBody().getName().equals("Venue7"));

        String parentId = entity.getBody().getId();
        assertNotNull("Parent id", entity.getBody().getId());

        // Create a venue with a parent
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue8 with parent");
        map.set("parentId", parentId);
        entity = postAndFollowRedirect(BASE_URL + "venue", map, JVenue.class);
        assertTrue("Name", entity.getBody().getName().equals("Venue8 with parent"));
//        assertTrue("Parent id", entity.getBody().getParent().equals(parentId));
//
//        // TODO Test parent venue
//        entity = template.getForEntity(BASE_URL + "venue/parent/{parent}", JVenue.class, parentId);
//        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
//        assertTrue("Name", entity.getBody().getName().equals("Venue8 with parent"));
    }

    @Test
    public void textSearch() {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue9 text search 1");
        map.set("city", "City1");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Create venue
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue10 text search 2");
        map.set("city", "City1");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Search for venues
        ResponseEntity<JVenueCursorPage> cursorEntity = template.getForEntity(BASE_URL + "venue/search?text=search 1", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 1);

        // Search using pagination
        cursorEntity = template.getForEntity(BASE_URL + "venue/search?text=City1&pagesize=1", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 1);
        assertNotNull("Cursor", cursorEntity.getBody().getCursor());

        cursorEntity = template.getForEntity(BASE_URL + "venue/search?text=City1&cursor={cursor}", JVenueCursorPage.class, cursorEntity.getBody().getCursor());
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 1);
    }

    @Test
    public void tagSearch() {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue11 tags 1 and 2");
        map.set("city", "City2");
        map.add("tags", "1");
        map.add("tags", "2");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue12 tags 2");
        map.set("city", "City3");
        map.add("tags", "2");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue13 tags 3");
        map.set("city", "City3");
        map.add("tags", "2");
        map.add("tags", "3");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Search for venues
        ResponseEntity<JVenueCursorPage> cursorEntity = template.getForEntity(BASE_URL + "venue/tags?tagIds=1", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 1);

        cursorEntity = template.getForEntity(BASE_URL + "venue/tags?tagIds=2", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 3);

        cursorEntity = template.getForEntity(BASE_URL + "venue/tags?tagIds=2&tagIds=3", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 1);

        cursorEntity = template.getForEntity(BASE_URL + "venue/search?text=city3&tagIds=2", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 2);
    }

    // TODO Test lat and long and nearby search


    @Test
    public void nearbySearch() {
        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue14 location");
        map.add("tags", "10");
        map.add("tags", "11");
        // street 302
        map.set("latitude", "11.553236");
        map.set("longitude", "104.927663");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue15 location");
        map.add("tags", "11");
        // street 51 (200m from venue 11)
        map.set("latitude", "11.554061");
        map.set("longitude", "104.926429");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue16 location");
        map.add("tags", "11");
        map.add("tags", "12");
        // street 106 (3km from venue 11)
        map.set("latitude", "11.574332");
        map.set("longitude", "104.926767");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue17 location");
        map.add("tags", "13");
        // Temple Udong (8km from venue 11)
        map.set("latitude", "11.819469");
        map.set("longitude", "104.743231");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue18 location");
        map.add("tags", "14");
        // Ministry of Interior (2,5km from venue 11)
        map.set("latitude", "11.537395");
        map.set("longitude", "104.927368");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Search for venues
        // Geo points are not implemented when running locally
        // Must be deployed to test
    }

    // Helper methods
    private void setDefaultVenueValues(MultiValueMap<String, Object> map) {

        map.set("shortDescription", "Short description");
        map.set("description", "Description");

        map.add("openingHours", "8-11");
        map.add("openingHours", "8-12");
        map.add("openingHours", "8-13");
        map.add("openingHours", "8-14");
        map.add("openingHours", "8-15");
        map.add("openingHours", "8-16");
        map.add("openingHours", "8-17");

        map.set("street", "Street");
        map.set("cityArea", "City area");
        map.set("city", "City");
        map.set("county", "County");
        map.set("postalCode", "Postal code");
        map.set("country", "Country");
        map.set("phoneNumber", "123456789");
        map.set("email", "test@test.com");
        map.set("webUrl", "www.test.se");
        map.set("facebookUrl", "www.facebook.com/test");
        map.set("twitterUrl", "www.twitter.com/test");
        map.set("logoUrl", "www.imageRepo.com/logoUrl");

        map.add("imageUrls", "www.imageRepo.com/image1");
        map.add("imageUrls", "www.imageRepo.com/image2");
        map.add("imageUrls", "www.imageRepo.com/image3");
    }

    private void assertDefaultVenueValues(JVenue venue) {
        assertTrue("Short description", venue.getShortDescription().equals("Short description"));
        assertTrue("Description", venue.getDescription().equals("Description"));
        assertTrue("Opening Hours", venue.getOpeningHours().size() == 7);
        assertTrue("Street", venue.getStreet().equals("Street"));
        assertTrue("City area", venue.getCityArea().equals("City area"));
        assertTrue("City", venue.getCity().equals("City"));
        assertTrue("County", venue.getCounty().equals("County"));
        assertTrue("Postal code", venue.getPostalCode().equals("Postal code"));
        assertTrue("Country", venue.getCountry().equals("Country"));
        assertTrue("Phone Number", venue.getPhoneNumber().equals("123456789"));
        assertTrue("Email", venue.getEmail().equals("test@test.com"));
        assertTrue("webUrl", venue.getWebUrl().equals("www.test.se"));
        assertTrue("facebookUrl", venue.getFacebookUrl().equals("www.facebook.com/test"));
        assertTrue("twitterUrl", venue.getTwitterUrl().equals("www.twitter.com/test"));
        assertTrue("logoUrl", venue.getLogoUrl().equals("www.imageRepo.com/logoUrl"));
        assertTrue("imageUrls", venue.getImageUrls().size() == 3);
    }


}
