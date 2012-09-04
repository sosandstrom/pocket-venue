package com.wadpam.pocketvenue.web;

import com.wadpam.docrest.domain.RestCode;
import com.wadpam.docrest.domain.RestReturn;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.pocketvenue.json.JVenuePage;
import com.wadpam.pocketvenue.service.VenueService;
import com.wadpam.server.web.AbstractRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The venue controller implements all REST methods related to venues.
 * @author mattiaslevin
 */
@Controller
@RequestMapping(value="{domain}/venue")
public class VenueController extends AbstractRestController {
    // TODO: Exception handlers not working

    static final Logger LOG = LoggerFactory.getLogger(VenueController.class);

    private VenueService venueService;

    /**
     * Add a venue.
     * @param name the name of the venue
     * @param parentId Optional. The id of the parent venue this venue belong to
     * @param hierarchy Optional. The hierarchy level this venue belong to
     * @param shortDescription Optional. A short description of the venue
     * @param description Optional. A description of the venue
     * @param appTags1 Optional. A list of app specific tags group 1
     * @param appTags2 Optional. A list of app specific tags group 2
     * @return the new venue
     */
    @RestReturn(value= JVenue.class, entity=JVenue.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly created venue")
    })
    @RequestMapping(value="", method= RequestMethod.POST)
    public RedirectView addVenue(HttpServletRequest request,
                                 Principal principal,
                                 @PathVariable String domain,
                                 @RequestParam(required = false) Long parentId,
                                 @RequestParam(required = false) String hierarchy,
                                 @RequestParam(required = true) String name,
                                 @RequestParam(required = false) String shortDescription,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) Long[] appTags1,
                                 @RequestParam(required = false) Long[] appTags2) {

        final DPlace body = venueService.addPlace(domain, parentId, hierarchy, name);

        return new RedirectView(request.getRequestURI() + "/" + body.getId().toString());
    }

    /**
     * Update venue by id.
     * @param id the id of the venue.
     * @param parentId Optional. The id of the parent venue this venue belong to
     * @param hierarchy Optional. The hierarchy level this venue belong to
     * @param name the name of the venue
     * @return the updated venue
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly updated venue")
    })
    @RequestMapping(value="{id}", method= RequestMethod.POST)
    public RedirectView updateVenue(HttpServletRequest request,
                                    Principal principal,
                                    @PathVariable String domain,
                                    @PathVariable Long id,
                                    @RequestParam(required = false) Long parentId,
                                    @RequestParam(required = false) String hierarchy,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String shortDescription,
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) Long[] appTags1,
                                    @RequestParam(required = false) Long[] appTags2) {

        final DPlace body = venueService.updatePlace(domain, id, parentId, hierarchy, name);

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
                                           Principal principal,
                                           @PathVariable String domain,
                                           @PathVariable Long id) {

        final DPlace body = venueService.getPlace(domain, id);

        return new ResponseEntity<JVenue>(Converter.convert(body, request), HttpStatus.OK);
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
                                              Principal principal,
                                              @PathVariable String domain,
                                              @PathVariable Long id) {

        final DPlace body = venueService.deletePlace(domain, id);

        return new ResponseEntity<JVenue>(HttpStatus.OK);
    }

    /**
     * Free text search for venues.
     * @param searchText the search text
     * @return a list of venues matching the search text
     */
    @RestReturn(value=JVenue.class, entity=JVenue.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
    })
    @RequestMapping(value="", method= RequestMethod.GET, params="searchText")
    public ResponseEntity<Collection<JVenue>> searchForVenue(HttpServletRequest request,
                                                             Principal principal,
                                                             @PathVariable String domain,
                                                             @RequestParam(required = true) String searchText) {

        final Collection<DPlace> body = venueService.searchForPlace(domain, searchText);

        return new ResponseEntity<Collection<JVenue>>((Collection<JVenue>)Converter.convert(body, request), HttpStatus.OK);
    }

    /**
     * Get all venues.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JVenuePage.class, entity=JVenuePage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
            @RestCode(code=404, message="NOK", description="No venues found")
    })
    @RequestMapping(value="", method= RequestMethod.GET)
    public ResponseEntity<JVenuePage> getAllVenuesForParent(HttpServletRequest request,
                                                            Principal principal,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor) {

        Collection<DPlace> dPlaces = new ArrayList<DPlace>(pagesize);
        final String newCursor = venueService.getAllPlaces(domain, cursor, pagesize, dPlaces);

        JVenuePage venuePage = new JVenuePage();
        venuePage.setCursor(newCursor);
        venuePage.setPageSize(pagesize);
        venuePage.setVenues((Collection<JVenue>) Converter.convert(dPlaces, request));

        return new ResponseEntity<JVenuePage>(venuePage, HttpStatus.OK);
    }

    /**
     * Get all venues for a parent.
     * @param id the id of the parent venue this venue belong to
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JVenuePage.class, entity=JVenuePage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
            @RestCode(code=404, message="NOK", description="No venues found")
    })
    @RequestMapping(value="parent/{id}", method= RequestMethod.GET)
    public ResponseEntity<JVenuePage> getAllVenuesForParent(HttpServletRequest request,
                                                            Principal principal,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor,
                                                            @PathVariable Long id) {

        Collection<DPlace> dPlaces = new ArrayList<DPlace>(pagesize);
        final String newCursor = venueService.getAllPlacesForParent(domain, id, cursor, pagesize, dPlaces);

        JVenuePage venuePage = new JVenuePage();
        venuePage.setCursor(newCursor);
        venuePage.setPageSize(pagesize);
        venuePage.setVenues((Collection<JVenue>) Converter.convert(dPlaces, request));

        return new ResponseEntity<JVenuePage>(venuePage, HttpStatus.OK);
    }

    /**
     * Get all venues for a hierarchy.
     * @param name the name of the hierarchy
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JVenuePage.class, entity=JVenuePage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
            @RestCode(code=404, message="NOK", description="No venues found")
    })
    @RequestMapping(value="hierarchy/{name}", method= RequestMethod.GET)
    public ResponseEntity<JVenuePage> getAllVenuesForHierarchy(HttpServletRequest request,
                                                            Principal principal,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor,
                                                            @PathVariable String name) {

        Collection<DPlace> dPlaces = new ArrayList<DPlace>(pagesize);
        final String newCursor = venueService.getAllPlacesForHierarchy(domain, name, cursor, pagesize, dPlaces);

        JVenuePage venuePage = new JVenuePage();
        venuePage.setCursor(newCursor);
        venuePage.setPageSize(pagesize);
        venuePage.setVenues((Collection<JVenue>) Converter.convert(dPlaces, request));

        return new ResponseEntity<JVenuePage>(venuePage, HttpStatus.OK);
    }


    /**
     * Get all venues with matching tag ids.
     *
     * Both a category and location tag can be provided and will when act and an AND operation.
     * Minimum one category or location tag must be provided.
     * @param pagesize Optional. The number of venues to return in this page. Default value is 10.
     * @param cursor Optional. The current cursor position during pagination.
     *               The next page will be return from this position.
     *               If asking for the first page, not cursor should be provided.
     * @param appTag1 the tag id will be matched against tag group 1
     * @param appTag2 the tag id will be matched against tag group 2
     * @return a list of venues and a new cursor.
     */
    @RestReturn(value=JVenuePage.class, entity=JVenuePage.class, code={
            @RestCode(code=200, message="OK", description="Venues found"),
            @RestCode(code=404, message="NOK", description="No venues found")
    })
    @RequestMapping(value="tags", method= RequestMethod.GET)
    public ResponseEntity<JVenuePage> getAllVenuesForTagIds(HttpServletRequest request,
                                                            Principal principal,
                                                            @PathVariable String domain,
                                                            @RequestParam(defaultValue = "10") int pagesize,
                                                            @RequestParam(required = false) String cursor,
                                                            @RequestParam(required = false) Long appTag1,
                                                            @RequestParam(required = false) Long appTag2) {

        // Check that at least one tag is provided
        if (null == appTag1 && null == appTag2)
            return new ResponseEntity<JVenuePage>(HttpStatus.BAD_REQUEST);

        Collection<DPlace> dPlaces = new ArrayList<DPlace>(pagesize);
        final String newCursor = venueService.getAllPlacesForTags(domain, appTag1, appTag2, cursor, pagesize, dPlaces);

        JVenuePage venuePage = new JVenuePage();
        venuePage.setCursor(newCursor);
        venuePage.setPageSize(pagesize);
        venuePage.setVenues((Collection<JVenue>) Converter.convert(dPlaces, request));

        return new ResponseEntity<JVenuePage>(venuePage, HttpStatus.OK);
    }



    // Setters
    public void setVenueService(VenueService venueService) {
        this.venueService = venueService;
    }
}
