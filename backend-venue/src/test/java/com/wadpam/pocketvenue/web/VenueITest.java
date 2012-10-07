package com.wadpam.pocketvenue.web;

import com.wadpam.open.integrationtest.DoNothingResponseErrorHandler;
import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.server.json.JRestError;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;

/**
 * Integration tests for venue service.
 * @author mattiaslevin
 */
public class VenueITest {
    static final Logger LOG = LoggerFactory.getLogger(VenueITest.class);


    static final String                  BASE_URL       = "http://localhost:8234/api/test/";

    RestTemplate template;

    public VenueITest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        template = new RestTemplate();

        // Configure an error handler that does not throw exceptions
        // All http codes are handled and tested using asserts
        template.setErrorHandler(new DoNothingResponseErrorHandler());
    }

    @After
    public void tearDown() {
    }


    @Test
    public void createVenueWithNameOnly() {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue1");

        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Get venue
        entity = template.getForEntity(BASE_URL + "venue/1", JVenue.class);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
        assertTrue("Name", entity.getBody().getName().equals("Venue1"));
    }

    @Test
    public void updateVenueName() {

        // Update venue name
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue1 updated");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue/1", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Get venue
        entity = template.getForEntity(BASE_URL + "venue/1", JVenue.class);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
        assertTrue("Name", entity.getBody().getName().equals("Venue1 updated"));
    }


    @Test
    public void createVenueWithAllProperties() {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue2");
        map.set("parentId", "1");
        setDefaultVenueValues(map);
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Get venue
        entity = template.getForEntity(BASE_URL + "venue/2", JVenue.class);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
        assertTrue("Name", entity.getBody().getName().equals("Venue2"));
        //assertTrue("parentId", entity.getBody().getParentId() == 1L);
        assertTrue("Short description", entity.getBody().getShortDescription().equals("Short description"));
        assertTrue("Description", entity.getBody().getDescription().equals("Description"));
    }

    @Test
    public void deleteVenue() {

        // Create
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue3");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Get
        entity = template.getForEntity(BASE_URL + "venue/3", JVenue.class);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
        assertTrue("Name", entity.getBody().getName().equals("Venue3"));

        // Delete
        template.delete(BASE_URL + "venue/3");

        // Check that it is deleted
        ResponseEntity<JRestError> restError = template.getForEntity(BASE_URL + "venue/3", JRestError.class);
        assertEquals("Http response 404", HttpStatus.NOT_FOUND, restError.getStatusCode());
    }

    @Test
    public void getAllVenues() throws InterruptedException {

        // Create a 3rd venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue3");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Create a 4th venue
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue4");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        //Thread.sleep(1000);

        // Get all venues (pagesize 10)
        ResponseEntity<JVenueCursorPage> cursorEntity = template.getForEntity(BASE_URL + "venue", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 4);
    }

    @Test
    public void getAllVenuesWithPagination() {

        // Get first 2 venues
        ResponseEntity<JVenueCursorPage> cursorEntity = template.getForEntity(BASE_URL + "venue?pagesize=2", JVenueCursorPage.class);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 2);

        String cursor = cursorEntity.getBody().getCursor();
        assertNotNull("Cursor null", cursor);
        assertFalse("Cursor empty", cursor.isEmpty());

        // Get the next 2 venues
        cursorEntity = template.getForEntity(BASE_URL + "venue?pagesize=2&cursor={cursor}", JVenueCursorPage.class, cursor);
        assertEquals("Http response 200", HttpStatus.OK, cursorEntity.getStatusCode());
        assertTrue("Number of venues returned", cursorEntity.getBody().getItems().size() == 2);
    }

    @Test
    public void getVenuesForParent() {

        // Create a venue with a parent
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue5");
        map.set("parentId", "1");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Get venue
        entity = template.getForEntity(BASE_URL + "venue/6", JVenue.class);
        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
        assertTrue("Name", entity.getBody().getName().equals("Venue5"));
        //assertTrue("Parent id", entity.getBody().getParentId() == 1L);

        // TODO Test parent venue
//        entity = template.getForEntity(BASE_URL + "venue/parent/1", JVenue.class);
//        assertEquals("Http response 200", HttpStatus.OK, entity.getStatusCode());
//        assertTrue("Name", entity.getBody().getName().equals("Venue5"));
    }

    @Test
    public void textSearch() {

        // Create venue
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue6 text search 1");
        map.set("city", "City1");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Create venue
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue7 text search 2");
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
        map.set("name", "Venue8 tags 1 and 2");
        map.set("city", "City2");
        map.add("tags", "1");
        map.add("tags", "2");
        ResponseEntity<JVenue> entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue9 tags 2");
        map.set("city", "City3");
        map.add("tags", "2");
        entity = template.postForEntity(BASE_URL + "venue", map, JVenue.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Venue10 tags 3");
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




    // TODO Test lat and long





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

    private void assertDefaulVenueValues(JVenue venue) {
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
