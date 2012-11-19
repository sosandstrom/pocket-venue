package com.wadpam.pocketvenue.web;

import au.com.bytecode.opencsv.CSVReader;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.docrest.domain.RestCode;
import com.wadpam.docrest.domain.RestReturn;
import com.wadpam.open.json.JCursorPage;
import com.wadpam.open.json.JLocation;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.pocketvenue.service.VenueService;
import com.wadpam.server.exceptions.BadRequestException;
import com.wadpam.server.exceptions.NotFoundException;
import com.wadpam.server.exceptions.ServerErrorException;
import com.wadpam.server.web.AbstractRestController;
import net.sf.mardao.core.CursorPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The venue controller implements all REST methods related to venues.
 * @author mattiaslevin
 */
@Controller
@RequestMapping(value="{domain}/venue")
public class VenueController extends AbstractRestController {
    private static final Logger LOG = LoggerFactory.getLogger(VenueController.class);

    private static final int ERR_NOT_FOUND = VenueService.ERR_VENUE_SERVICE + 201;
    private final int ERR_BAD_REQUEST = VenueService.ERR_VENUE_SERVICE + 202;
    private final int ERR_SERVER_ERROR = VenueService.ERR_VENUE_SERVICE + 203;

    private static final Converter CONVERTER = new Converter();
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    private VenueService venueService;

    /**
     * Create a venue.
     * @param name the name of the venue
     * @param parent Optional. The id of the parent venue this venue belong to
     * @param shortDescription Optional. A short description of the venue
     * @param description Optional. A description of the venue
     * @param openingHours Optional. A list of string describing the opening hours.
     *                     Suggest one string for each weekday. Indicate closed with "CLOSED" or ""
     * @param tags Optional. A list of app specific tag ids
     * @param street Optional. Street name
     * @param cityArea Optional. City area
     * @param city Optional. City
     * @param county Optional. County
     * @param postalCode Optional. Postal code
     * @param country Optional. Country
     * @param latitude Optional. The latitude
     * @param longitude Optional. The longitude
     * @param phoneNumber Optional. Phone number
     * @param email Optional. Email address
     * @param webUrl Optional. The venue home page if any
     * @param facebookUrl Optional. The venue Facebook page
     * @param twitterUrl Optional. The Twitter url
     * @param logoUrl Optional. A logo url that is suitable to show in list views
     * @param imageUrls Optional. Image urls that are suitable to show in a details view
     * @return the new venue
     */
    @RestReturn(value= JVenue.class, entity=JVenue.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly created venue")
    })
    @RequestMapping(value="", method= RequestMethod.POST)
    public RedirectView addVenue(HttpServletRequest request,
                                 UriComponentsBuilder uriBuilder,
                                 @PathVariable String domain,
                                 @ModelAttribute("jVenue") JVenue jVenue,
                                 @ModelAttribute("jLocation") JLocation jLocation,
                                 BindingResult result) {

        // Check for binding errors
        if (result.hasErrors()) {
            LOG.debug("Data binding to venue failed with reason:{}", result.toString());
            throw new BadRequestException(ERR_BAD_REQUEST, String.format("Data binding to venue failed with reason:%s", result.toString()));
        }

        // Set the location outside the binding
        jVenue.setLocation(jLocation);

        // Check that we have a minimum set of parameters
        if (null == jVenue.getName() || jVenue.getName().isEmpty())
            throw new BadRequestException(ERR_BAD_REQUEST, "Client must provide a venue name");

        // Create the venue
        final DPlace body = venueService.addPlace(jVenue);

        if (null == body)
            throw new ServerErrorException(ERR_SERVER_ERROR, String.format("Failed to create new venue:%s", jVenue.getName()));

        JVenue createdVenue = CONVERTER.convert(body);
        return new RedirectView(uriBuilder.path("/{domain}/venue/{id}").
                buildAndExpand(domain, createdVenue.getId()).toUriString());
    }

    /**
     * Update venue by id.
     * @param id the id of the venue
     * @param name the name of the venue
     * @param parentId Optional. The id of the parent venue this venue belong to
     * @param shortDescription Optional. A short description of the venue
     * @param description Optional. A description of the venue
     * @param openingHours Optional. A list of string describing the opening hours
     * @param tagIds Optional. A list of app specific tag ids
     * @param street Optional. Street name
     * @param cityArea Optional. City area
     * @param city Optional. City
     * @param county Optional. County
     * @param postalCode Optional. Postal code
     * @param country Optional. Country
     * @param latitude Optional. The latitude
     * @param longitude Optional. The longitude
     * @param phoneNumber Optional. Phone number
     * @param email Optional. Email address
     * @param webUrl Optional. The venue home page if any
     * @param facebookUrl Optional. The venue Facebook page
     * @param twitterUrl Optional. The Twitter url
     * @param logoUrl Optional. A logo url that is suitable to show in list views
     * @param imageUrls Optional. Image urls that are suitable to show in a details view
     * @return the updated venue
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly updated venue")
    })
    @RequestMapping(value="{id}", method= RequestMethod.POST)
    public RedirectView updateVenue(HttpServletRequest request,
                                    @PathVariable String id,
                                    @ModelAttribute("jVenue") JVenue jVenue,
                                    @ModelAttribute("jLocation") JLocation jLocation,
                                    BindingResult result) {

        if (result.hasErrors()) {
            LOG.debug("Data binding to venue failed with reason:{}", result.toString());
            throw new BadRequestException(ERR_BAD_REQUEST, String.format("Data binding to venue failed with reason:%s", result.toString()));
        }

        // Check that we have a minimum set of parameters
        if (null == jVenue.getName() || jVenue.getName().isEmpty())
            throw new BadRequestException(ERR_BAD_REQUEST, "Client must provide a venue name");

        // Add the id to the binding
        jVenue.setId(id);
        // Set the location outside the binding
        jVenue.setLocation(jLocation);

        final DPlace body = venueService.updatePlace(jVenue);

        if (null == body)
            throw new ServerErrorException(ERR_SERVER_ERROR, String.format("Failed to update venue with id:%s", jVenue.getId()));

        return new RedirectView(request.getRequestURI());
    }

    /**
     * Get a venue by id.
     * @param id the id of the venue
     * @return the venue
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=200, message="OK", description="Venue found"),
            @RestCode(code=404, message="NOK", description="Venue not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.GET)
    public ResponseEntity<JVenue> getVenue(HttpServletRequest request,
                                           @PathVariable String domain,
                                           @PathVariable String id) {

        Key key = KeyFactory.stringToKey(id);
        final DPlace body = venueService.getPlace(key);

        if (null == body)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Place with id:%s not found", id));

        return new ResponseEntity<JVenue>(CONVERTER.convert(body), HttpStatus.OK);
    }

    /**
     * Delete a venue by id.
     * @param id the id of the venue
     * @return the and http response code indicating the outcome of the operation
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=200, message="OK", description="Venue found"),
            @RestCode(code=404, message="NOK", description="Venue not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.DELETE)
    public ResponseEntity<JVenue> deleteVenue(HttpServletRequest request,
                                              @PathVariable String domain,
                                              @PathVariable String id) {

        Key key = KeyFactory.stringToKey(id);
        final DPlace result = venueService.deletePlace(key);

        return new ResponseEntity<JVenue>(HttpStatus.OK);
    }

    /**
     * Get all venues.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JCursorPage.class, entity=JCursorPage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
            @RestCode(code=404, message="NOK", description="No venues found")
    })
    @RequestMapping(value="", method= RequestMethod.GET)
    public ResponseEntity<JCursorPage<JVenue>> getAllVenues(
            HttpServletRequest request,
            @PathVariable String domain,
            @RequestParam(defaultValue = "10") int pagesize,
            @RequestParam(required = false) String cursor) {

        final CursorPage<DPlace, Long> cursorPage = venueService.getAllPlaces(cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERR_SERVER_ERROR, "Not possible to get all venues");

        return new ResponseEntity<JCursorPage<JVenue>>((JCursorPage<JVenue>)CONVERTER.convertPage(cursorPage), HttpStatus.OK);
    }

    /**
     * Get all venues for a parent.
     * @param parent the id of the parent venue
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JCursorPage.class, entity=JCursorPage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
            @RestCode(code=404, message="NOK", description="No venues found")
    })
    @RequestMapping(value="parent/{parent}", method= RequestMethod.GET)
    public ResponseEntity<JCursorPage<JVenue>> getAllVenuesForParent(
            HttpServletRequest request,
            @PathVariable String domain,
            @RequestParam(defaultValue = "10") int pagesize,
            @RequestParam(required = false) String cursor,
            @PathVariable String parent) {

        Key parentKey = KeyFactory.stringToKey(parent);
        final CursorPage<DPlace, Long> cursorPage = venueService.getAllPlacesForParent(parentKey, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERR_SERVER_ERROR , String.format("Not possible to get all venue for parent:%s", parent));

        return new ResponseEntity<JCursorPage<JVenue>>((JCursorPage<JVenue>)CONVERTER.convertPage(cursorPage), HttpStatus.OK);
    }

    /**
     * Get all venues with no parent.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JCursorPage.class, entity=JCursorPage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
            @RestCode(code=404, message="NOK", description="No venues found")
    })
    @RequestMapping(value="root", method= RequestMethod.GET)
    public ResponseEntity<JCursorPage<JVenue>> getAllVenuesWithNoParent(
            HttpServletRequest request,
            @PathVariable String domain,
            @RequestParam(defaultValue = "10") int pagesize,
            @RequestParam(required = false) String cursor) {

        final CursorPage<DPlace, Long> cursorPage = venueService.getAllPlacesWithNoParent(cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERR_SERVER_ERROR , "Not possible to get venus with no parent");

        return new ResponseEntity<JCursorPage<JVenue>>((JCursorPage<JVenue>)CONVERTER.convertPage(cursorPage), HttpStatus.OK);
    }

    /**
     * Search for venues by text.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @param text the search text
     * @param tags Optional. Only venues containing the list of tag ids will be searched
     * @return a list of venues matching the search text
     */
    @RestReturn(value=JCursorPage.class, entity=JCursorPage.class, code={
            @RestCode(code=200, message="OK", description="Venues found")
    })
    @RequestMapping(value="search", method= RequestMethod.GET)
    public ResponseEntity<JCursorPage<JVenue>> searchForVenue(
            HttpServletRequest request,
            @PathVariable String domain,
            @RequestParam(required = true) String text,
            @RequestParam(required = false) String[] tags,
            @RequestParam(defaultValue = "10") int pagesize,
            @RequestParam(required = false) String cursor) {

        final CursorPage<DPlace, Long> cursorPage = venueService.textSearchForPlaces(text, tags, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERR_SERVER_ERROR, String.format("Not possible to search with text:%s", text));

        return new ResponseEntity<JCursorPage<JVenue>>((JCursorPage<JVenue>)CONVERTER.convertPage(cursorPage), HttpStatus.OK);
    }


    /**
     * Get all venues with matching tag ids.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @param tags optional. A list a tag ids. Only places with matching tags will be considered
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JCursorPage.class, entity=JCursorPage.class, code={
            @RestCode(code=200, message="OK", description="Venues found")
    })
    @RequestMapping(value="tags", method= RequestMethod.GET)
    public ResponseEntity<JCursorPage<JVenue>> getAllVenuesForTags(
            HttpServletRequest request,
            @PathVariable String domain,
            @RequestParam(defaultValue = "10") int pagesize,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = true) String[] tags) {

        // Check that we have at least one tag
        if (tags.length < 1)
            throw new BadRequestException(ERR_BAD_REQUEST, "At least one tag must be provided");

        final CursorPage<DPlace, Long> cursorPage = venueService.getAllPlacesForTags(tags, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERR_SERVER_ERROR, String.format("Not possible to search with tags:%s", tags));

        return new ResponseEntity<JCursorPage<JVenue>>((JCursorPage<JVenue>)CONVERTER.convertPage(cursorPage), HttpStatus.OK);
    }


    /**
     * Find nearby venues.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @param latitude optional, the latitude to search around
     * @param longitude optional, the longitude to search around
     * @param radius optional, the radius i meter. Default 1500m
     * @param tags optional. A list a tag ids. Only places with matching tags will be considered
     * @return a list of products
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=200, message="OK", description="Venues found")
    })
    @RequestMapping(value="nearby", method= RequestMethod.GET)
    public ResponseEntity<JCursorPage<JVenue>> getNearbyVenues(
            HttpServletRequest request,
            @PathVariable String domain,
            @RequestParam(defaultValue = "10") int pagesize,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = true) Float latitude,
            @RequestParam(required = true) Float longitude,
            @RequestParam(defaultValue = "3000") int radius,
            @RequestParam(required = true) String[] tags) {

        final CursorPage<DPlace, Long> cursorPage = venueService.getNearbyPlaces(latitude, longitude, radius, tags, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERR_SERVER_ERROR, String.format("Not possible to search nearby with lat:%s and lon:%s", latitude, longitude));

        return new ResponseEntity<JCursorPage<JVenue>>((JCursorPage<JVenue>)CONVERTER.convertPage(cursorPage), HttpStatus.OK);
    }


    /**
     * Get a url where you can upload a CVS file with venue data.
     * @return a url where a CSV file can be uploaded
     */
    @RestReturn(value=Map.class, entity=Map.class, code={
            @RestCode(code=200, message="OK", description="Venue CSV file upload url create")
    })
    @RequestMapping(value="csv", method= RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getCSVUploadUrl(HttpServletRequest request) {

        String callbackUrl = request.getRequestURI();

        Map<String, String> response = new HashMap<String, String>();
        response.put("url", blobstoreService.createUploadUrl(callbackUrl));

        return response;
    }

    /**
     * Upload a CSV file to blobstore.
     * Expected format
     * name;shortDescription;description;openingHours;tags;street;cityArea;city;county;postalCode;country;latitude;longitude;phoneNumber;email;webUrl;facebookUrl;twitterUrl;logoUrl;imageUrls
     * In columns that contain a list of values, e.g. opening hours, individual elements will be separated by #
     * First each row is checked for errors. If errors are found the operation in aborted (all rows
     * must be validated before they are stored to the datastore).
     * @return The number of venues that was created
     */
    @RestReturn(value=Map.class, entity=Map.class, code={
            @RestCode(code=200, message="OK", description="Venue CSV file uploaded")
    })
    @RequestMapping(value="csv", method= RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> csvUploadCallback(
            HttpServletRequest request,
            @RequestParam(required = false) String parent) throws IOException {
        LOG.debug("Venue CSV upload callback");

        Map<String, List<BlobKey>> blobKeys = blobstoreService.getUploads(request);

        // Get the blobkey
        BlobKey blobKey = null;
        Iterable<BlobKey> iterable = null;
        if (blobKeys.values().iterator().hasNext()) {
            blobKey = blobKeys.values().iterator().next().get(0);
        }

        if (null == blobKey) {
            // Not possible to get the blob key
            throw new ServerErrorException(ERR_SERVER_ERROR, "Not possible to get the blob key for the CSV file");
        }

        Map<String, Integer> response = new HashMap<String, Integer>();
        try {
            InputStream inputStream = new BlobstoreInputStream(blobKey);
            Key parentKey = parent != null ? KeyFactory.stringToKey(parent) : null;
            int rows = this.venueService.addVenueFromCSV(inputStream, parentKey);
            response.put("rows", rows);
        } finally {
            // Make sure we always delete the blob
            blobstoreService.delete(blobKey);
        }

        return response;
    }



    // Setters
    public void setVenueService(VenueService venueService) {
        this.venueService = venueService;
    }
}
