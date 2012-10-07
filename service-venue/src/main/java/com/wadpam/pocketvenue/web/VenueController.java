package com.wadpam.pocketvenue.web;

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

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;

/**
 * The venue controller implements all REST methods related to venues.
 * @author mattiaslevin
 */
@Controller
@RequestMapping(value="{domain}/venue")
public class VenueController extends AbstractRestController {
    static final int ERROR_CODE_NOT_FOUND = 10000;
    static final int ERROR_CODE_BAD_REQUEST = 10100;
    static final int ERROR_CODE_SEVER_ERROR = 10200;

    static final Logger LOG = LoggerFactory.getLogger(VenueController.class);

    static final Converter CONVERTER = new Converter();

    private VenueService venueService;

    /**
     * Create a venue.
     * @param name the name of the venue
     * @param parentId Optional. The id of the parent venue this venue belong to
     * @param shortDescription Optional. A short description of the venue
     * @param description Optional. A description of the venue
     * @param openingHours Optional. A list of string describing the opening hours.
     *                     Suggest one string for each weekday. Indicate closed with "CLOSED" or ""
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
     * @return the new venue
     */
    @RestReturn(value= JVenue.class, entity=JVenue.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly created venue")
    })
    @RequestMapping(value="", method= RequestMethod.POST)
    public RedirectView addVenue(HttpServletRequest request,
                                 @ModelAttribute("jVenue") JVenue jVenue,
                                 @ModelAttribute("jLocation") JLocation jLocation,
                                 BindingResult result) {

        // Check for binding errors
        if (result.hasErrors()) {
            LOG.debug(String.format("Data binding to venue failed with reason:%s"), result.toString());
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 1, String.format("Data binding to venue failed with reason:%s", result.toString()), null, "Bad request");
        }

        // Set the location outside the binding
        jVenue.setLocation(jLocation);

        // Check that we have a minimum set of parameters
        if (null == jVenue.getName() || jVenue.getName().isEmpty())
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 2, "Client must provide a venue name", null, "Bad request, missing venue name");

        // Create the venue
        final DPlace body = venueService.addPlace(jVenue);

        if (null == body)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 1, String.format("Failed to create new venue:%s", jVenue.getName()), null, "Create new venue failed");

        return new RedirectView(request.getRequestURI() + "/" + body.getId().toString());
    }

    /**
     * Update venue by id.
     * @param id the id of the venue.
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
                                    @PathVariable Long id,
                                    @ModelAttribute("jVenue") JVenue jVenue,
                                    @ModelAttribute("jLocation") JLocation jLocation,
                                    BindingResult result) {

        if (result.hasErrors()) {
            LOG.debug(String.format("Data binding to venue failed with reason:%s"), result.toString());
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 3, String.format("Data binding to venue failed with reason:%s", result.toString()), null, "Bad request");
        }

        // Check that we have a minimum set of parameters
        if (null == jVenue.getName() || jVenue.getName().isEmpty())
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 4, "Client must provide a venue name", null, "Bad request, missing venue name");

        // Add the id to the binding
        jVenue.setId(Long.toString(id));
        // Set the location outside the binding
        jVenue.setLocation(jLocation);

        final DPlace body = venueService.updatePlace(jVenue);

        if (null == body)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 2, String.format("Failed to update venue with id:%s", jVenue.getId()), null, "Update venue failed");

        return new RedirectView(request.getRequestURI());
    }

    /**
     * Get a venue by id.
     * @param id the id of the venue.
     * @return the venue
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=200, message="OK", description="Venue found"),
            @RestCode(code=404, message="NOK", description="Venue not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.GET)
    public ResponseEntity<JVenue> getVenue(HttpServletRequest request,
                                           @PathVariable String domain,
                                           @PathVariable Long id) {

        final DPlace body = venueService.getPlace(id);

        if (null == body)
            throw new NotFoundException(ERROR_CODE_NOT_FOUND + 1, String.format("Place with id:%s not found", id), null, "Venue not found");

        return new ResponseEntity<JVenue>(CONVERTER.convert(body), HttpStatus.OK);
    }

    /**
     * Delete a venue by id.
     * @param id the id of the venue.
     * @return the and http response code indicating the outcome of the operation
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=200, message="OK", description="Venue found"),
            @RestCode(code=404, message="NOK", description="Venue not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.DELETE)
    public ResponseEntity<JVenue> deleteVenue(HttpServletRequest request,
                                              @PathVariable String domain,
                                              @PathVariable Long id) {

        final DPlace result = venueService.deletePlace(id);

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
    public ResponseEntity<JCursorPage<JVenue>> getAllVenues(HttpServletRequest request,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor) {

        final CursorPage<DPlace, Long> cursorPage = venueService.getAllPlaces(cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 3, "Not possible to get all venues", null, "Search failed");

        JCursorPage venuePage = new JCursorPage();
        if (null != cursorPage.getCursorKey())
            venuePage.setCursor(cursorPage.getCursorKey().toString());
        venuePage.setItems(CONVERTER.convert(cursorPage.getItems()));
        venuePage.setPageSize((long)pagesize);

        return new ResponseEntity<JCursorPage<JVenue>>(venuePage, HttpStatus.OK);
    }

    /**
     * Get all venues for a parent.
     * @param id the id of the parent venue
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
    @RequestMapping(value="parent/{id}", method= RequestMethod.GET)
    public ResponseEntity<JCursorPage<JVenue>> getAllVenuesForParent(HttpServletRequest request,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor,
                                                            @PathVariable Long id) {

        final CursorPage<DPlace, Long> cursorPage = venueService.getAllPlacesForParent(id, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 4, String.format("Not possible to get all venue for parent:%s", id), null, "Search failed");

        JCursorPage venuePage = new JCursorPage();
        if (null != cursorPage.getCursorKey())
            venuePage.setCursor(cursorPage.getCursorKey().toString());
        venuePage.setItems(CONVERTER.convert(cursorPage.getItems()));
        venuePage.setPageSize((long)pagesize);

        return new ResponseEntity<JCursorPage<JVenue>>(venuePage, HttpStatus.OK);
    }



    // TODO Get root venues


    /**
     * Search for venues by text.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @param text the search text
     * @param tagIds Optional. Only venues containing the list of tag ids will be searched
     * @return a list of venues matching the search text
     */
    @RestReturn(value=JCursorPage.class, entity=JCursorPage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
    })
    @RequestMapping(value="search", method= RequestMethod.GET, params="text")
    public ResponseEntity<JCursorPage<JVenue>> searchForVenue(HttpServletRequest request,
                                                     @PathVariable String domain,
                                                     @RequestParam(required = true) String text,
                                                     @RequestParam(required = false) Long[] tagIds,
                                                     @RequestParam(defaultValue = "10") int pagesize,
                                                     @RequestParam(required = false) String cursor) {

        final CursorPage<DPlace, Long> cursorPage = venueService.textSearchForPlaces(text, tagIds, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 5, String.format("Not possible to search with text:%s", text), null, "Search failed");

        JCursorPage venuePage = new JCursorPage();
        if (null != cursorPage.getCursorKey())
            venuePage.setCursor(cursorPage.getCursorKey().toString());
        venuePage.setItems(CONVERTER.convert(cursorPage.getItems()));
        venuePage.setPageSize((long)pagesize);

        return new ResponseEntity<JCursorPage<JVenue>>(venuePage, HttpStatus.OK);
    }


    /**
     * Get all venues with matching tag ids.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @param tagIds optional. A list a tag ids. Only places with matching tags will be considered
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JCursorPage.class, entity=JCursorPage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
    })
    @RequestMapping(value="tags", method= RequestMethod.GET, params = "tagIds")
    public ResponseEntity<JCursorPage<JVenue>> getAllVenuesForTagIds(HttpServletRequest request,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor,
                                                            @RequestParam(required = true) Long[] tagIds) {

        // Check that we have at least one tag
        if (tagIds.length < 1)
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 3, "At least one tag must be provided", null, "Bad request");

        final CursorPage<DPlace, Long> cursorPage = venueService.getAllPlacesForTags(tagIds, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 6, String.format("Not possible to search with tags:%s", tagIds), null, "Search failed");

        JCursorPage venuePage = new JCursorPage();
        if (null != cursorPage.getCursorKey())
            venuePage.setCursor(cursorPage.getCursorKey().toString());
        venuePage.setItems(CONVERTER.convert(cursorPage.getItems()));
        venuePage.setPageSize((long)pagesize);

        return new ResponseEntity<JCursorPage<JVenue>>(venuePage, HttpStatus.OK);
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
     * @param tagIds optional. A list a tag ids. Only places with matching tags will be considered
     * @return a list of products
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
    })
    @RequestMapping(value="nearby", method= RequestMethod.GET)
    public ResponseEntity<Collection<JVenue>> getNearbyVenues(HttpServletRequest request,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor,
                                                            @RequestParam(required = true) Float latitude,
                                                            @RequestParam(required = true) Float longitude,
                                                            @RequestParam(defaultValue = "2500") int radius,
                                                            @RequestParam(required = true) Long[] tagIds) {

        final CursorPage<DPlace, Long> cursorPage = venueService.getNearbyPlaces(latitude, longitude, radius, tagIds, cursor, pagesize);

        if (null == cursorPage)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 7, String.format("Not possible to search nearby with lat:%s and lon:%s", latitude, longitude), null, "Search failed");

        return new ResponseEntity<Collection<JVenue>>((Collection<JVenue>)CONVERTER.convert(cursorPage.getItems()), HttpStatus.OK);
    }


    // Setters
    public void setVenueService(VenueService venueService) {
        this.venueService = venueService;
    }
}
