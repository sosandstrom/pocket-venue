package com.wadpam.pocketvenue.web;

import com.wadpam.docrest.domain.RestCode;
import com.wadpam.docrest.domain.RestReturn;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.pocketvenue.json.JTag;
import com.wadpam.pocketvenue.service.VenueService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * The tag controller implements all REST methods related to tags.
 * @author mattiaslevin
 */
@Controller
@RequestMapping(value="{domain}/tag")
public class TagController {

    static final Logger LOG = LoggerFactory.getLogger(TagController.class);

    private VenueService venueService;

    /**
     * Add a tag.
     * @param type the type of the tag, e.g. "location" or "category". D
     * @param name the name of the tag
     * @param parentId Optional. The id of the parent tag this tag belong to
     *                  E.g. "country", "city" in case of location tags
     * @return the new tag
     */
    @RestReturn(value= JTag.class, entity=JTag.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly created tag")
    })
    @RequestMapping(value="", method= RequestMethod.POST)
    public RedirectView addTag(HttpServletRequest request,
                               Principal principal,
                               @PathVariable String domain,
                               @RequestParam(required = false) Long parentId,
                               @RequestParam(required = true) String type,
                               @RequestParam(required = true) String name) {

        final DTag body = venueService.addTag(domain, type, parentId, name);

        return new RedirectView(request.getRequestURI() + "/" + body.getId().toString());
    }

    /**
     * Update a tag by id.
     * @param type the type of the tag, e.g. "location" or "category"
     * @param name the name of the tag
     * @param parentId Optional. The id of the parent tag this tag belong to
     * @return the updated tag
     */
    @RestReturn(value= JTag.class, entity=JTag.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly updated tag")
    })
    @RequestMapping(value="{id}", method= RequestMethod.POST)
    public RedirectView updateTag(HttpServletRequest request,
                                  Principal principal,
                                  @PathVariable String domain,
                                  @PathVariable Long id,
                                  @RequestParam(required = false) Long parentId,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String name) {

        final DTag body = venueService.updateTag(domain, id, type, parentId, name);

        return new RedirectView(request.getRequestURI());
    }

    /**
     * Get a tag by id.
     * @param id the id of the tag.
     * @return the tag
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tag found"),
            @RestCode(code=404, message="NOK", description="Tag not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.GET)
    public ResponseEntity<JTag> getTag(HttpServletRequest request,
                                       Principal principal,
                                       @PathVariable String domain,
                                       @PathVariable Long id) {

        final DTag body = venueService.getTag(domain, id);

        return new ResponseEntity<JTag>(Converter.convert(body, request), HttpStatus.OK);
    }

    /**
     * Delete a tag by id.
     * @param id the id of the tag.
     * @return the and http response code indicating the outcome of the operation
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tag found"),
            @RestCode(code=404, message="NOK", description="Tag not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.DELETE)
    public ResponseEntity<JTag> deleteTag(HttpServletRequest request,
                                          Principal principal,
                                          @PathVariable String domain,
                                          @PathVariable Long id) {

        final DTag body = venueService.deleteTag(domain, id);

        return new ResponseEntity<JTag>(HttpStatus.OK);
    }

    /**
     * Get full hierarchy of tags of a certain type.
     * @param name the type of the tag, e.g. "location" or "category". D
     * @return a full tag hierarchy
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tags found"),
            @RestCode(code=404, message="NOK", description="Tags not found")
    })
    @RequestMapping(value="type/{name}", method= RequestMethod.GET)
    public ResponseEntity<Collection<JTag>> getTagHierarchyForType(HttpServletRequest request,
                                                       Principal principal,
                                                       @PathVariable String domain,
                                                       @PathVariable String name) {
        // TODO: Need to support caching of the JSON


        final Collection<DTag> dTags = venueService.getTagsForType(domain, name);

        // Arrange in a hierarchy
        Collection<JTag> rootTags = new ArrayList<JTag>();
        Map<String, Collection<JTag>> remainingTags = new HashMap<String, Collection<JTag>>();

        // Get root and non-root tags
        for (DTag dTag : dTags) {
            // Convert to JTag before we do anything
            JTag jTag = Converter.convert(dTag, request);

            if (null == dTag.getParentId())
                rootTags.add(jTag);
            else {
                Collection<JTag> children = remainingTags.get(jTag.getParentId());
                if (null == children) {
                    children = new ArrayList<JTag>();
                    remainingTags.put(jTag.getParentId(), children);
                }
                children.add(jTag);
            }
        }

        for (JTag parentTag : rootTags)
            addChildren(parentTag, remainingTags);

        return new ResponseEntity<Collection<JTag>>(rootTags, HttpStatus.OK);
    }

    // Build a hierarchy of tags
    private void addChildren(JTag parentTag, Map<String, Collection<JTag>> tags) {
        //LOG.debug(String.format("Add children for parent:%s, remaining tags:%s", parentTag, tags));
        Collection<JTag> childTags = tags.get(parentTag.getId());

        if (null == childTags)
            // No children. Reached a leaf, do nothing
            return;

        // Add children
        parentTag.setChildren(childTags);

        // Recursively build the hierarchy
        for (JTag tag : childTags)
            addChildren(tag, tags);
    }

    /**
     * Get all tags for a parent.
     * @param id the parent
     * @return all list of child tags
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tags found"),
            @RestCode(code=404, message="NOK", description="Tags not found")
    })
    @RequestMapping(value="parent/{id}", method= RequestMethod.GET)
    public ResponseEntity<Collection<JTag>> getTagsForParent(HttpServletRequest request,
                                                 Principal principal,
                                                 @PathVariable String domain,
                                                 @PathVariable Long id) {

        final Collection<DTag> body = venueService.getTagsForParent(domain, id);

        return new ResponseEntity<Collection<JTag>>((Collection<JTag>)Converter.convert(body, request), HttpStatus.OK);
    }



    // Setters
    public void setVenueService(VenueService venueService) {
        this.venueService = venueService;
    }
}
