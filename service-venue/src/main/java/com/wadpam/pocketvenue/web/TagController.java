package com.wadpam.pocketvenue.web;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.wadpam.docrest.domain.RestCode;
import com.wadpam.docrest.domain.RestReturn;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.pocketvenue.json.JTag;
import com.wadpam.pocketvenue.service.VenueService;
import com.wadpam.server.exceptions.NotFoundException;
import com.wadpam.server.exceptions.ServerErrorException;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
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
    private static final Logger LOG = LoggerFactory.getLogger(TagController.class);

    private static final int ERR_NOT_FOUND = VenueService.ERR_VENUE_SERVICE + 100;
    private static final int ERR_SERVER_ERROR = VenueService.ERR_VENUE_SERVICE + 101;

    private static final String TAG_CACHE_KEY = "tagKey:";

    private static final Converter CONVERTER = new Converter();

    private VenueService venueService;

    /**
     * Add a tag.
     * @param type the type of the tag, e.g. "location" or "category". D
     * @param name the name of the tag
     * @param parent Optional. The parent tag this tag belong to
     * @return the new tag
     */
    @RestReturn(value= JTag.class, entity=JTag.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly created tag")
    })
    @RequestMapping(value="", method= RequestMethod.POST)
    public RedirectView addTag(HttpServletRequest request,
                               UriComponentsBuilder uriBuilder,
                               @PathVariable String domain,
                               @RequestParam(required = false) String parent,
                               @RequestParam(required = true) String type,
                               @RequestParam(required = true) String name) {

        Key parentKey = parent != null ? KeyFactory.stringToKey(parent) : null;
        final DTag body = venueService.addTag(type, parentKey, name);

        if (null == body)
            throw new ServerErrorException(ERR_SERVER_ERROR, String.format("Not possible to create new tag:%s", name));

        // Invalidate the memcache
        MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
        memCache.delete(tagCacheKey(body));

        JTag jTag = CONVERTER.convert(body);
        return new RedirectView(uriBuilder.path("/{domain}/tag/{id}")
                .buildAndExpand(domain, jTag.getId()).toUriString());
    }

    // Build the cache key for memcache
    private String tagCacheKey(DTag dTag) {
        if (null == dTag || null == dTag.getType() || dTag.getType().isEmpty())
            return TAG_CACHE_KEY + "DEFAULT";
        else
            return TAG_CACHE_KEY + dTag.getType();
    }

    /**
     * Update a tag by id.
     * @param id the id of the tag
     * @param type the type of the tag, e.g. "location" or "category"
     * @param name the name of the tag
     * @param parent Optional. The id of the parent tag this tag belong to
     * @return the updated tag
     */
    @RestReturn(value= JTag.class, entity=JTag.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly updated tag")
    })
    @RequestMapping(value="{id}", method= RequestMethod.POST)
    public RedirectView updateTag(HttpServletRequest request,
                                  @PathVariable String id,
                                  @RequestParam(required = false) String parent,
                                  @RequestParam(required = true) String type,
                                  @RequestParam(required = true) String name) {

        Key key = KeyFactory.stringToKey(id);
        Key parentKey = parent != null ? KeyFactory.stringToKey(parent) : null;
        final DTag body = venueService.updateTag(key, type, parentKey, name);

        if (null == body)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Tag:%s not found during update", name));

        // Invalidate the memcache
        MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
        memCache.delete(tagCacheKey(body));

        return new RedirectView(request.getRequestURI());
    }

    /**
     * Get a tag by id.
     * @param id the id of the tag
     * @return the tag
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tag found"),
            @RestCode(code=404, message="NOK", description="Tag not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.GET)
    public ResponseEntity<JTag> getTag(HttpServletRequest request,
                                       @PathVariable String id) {

        Key key = KeyFactory.stringToKey(id);
        final DTag body = venueService.getTag(key);

        if (null == body)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Tag with id:%s not found", id));

        return new ResponseEntity<JTag>(CONVERTER.convert(body), HttpStatus.OK);
    }

    /**
     * Delete a tag by id.
     * @param id the id of the tag
     * @return the and http response code indicating the outcome of the operation
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tag found"),
            @RestCode(code=404, message="NOK", description="Tag not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.DELETE)
    public ResponseEntity<JTag> deleteTag(HttpServletRequest request,
                                          @PathVariable String id) {

        Key key = KeyFactory.stringToKey(id);
        final DTag body = venueService.deleteTag(key);

        // Invalidate the memcache
        MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
        memCache.delete(tagCacheKey(body));

        return new ResponseEntity<JTag>(HttpStatus.OK);
    }

    /**
     * Get full hierarchy of tags of a certain type.
     * @param type the type of the tag, e.g. "location" or "category". D
     * @return a full tag hierarchy
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tags found"),
            @RestCode(code=404, message="NOK", description="Tags not found")
    })
    @RequestMapping(value="type/{type}", method= RequestMethod.GET)
    public ResponseEntity<Collection<JTag>> getTagHierarchyForType(HttpServletRequest request,
                                                                   @PathVariable String type) {

        // Check the cache first
        MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
        Collection<JTag> jTags = (Collection<JTag>)memCache.get(TAG_CACHE_KEY + type);

        if (null == jTags) {

            final Iterable<DTag> dTagIterable = venueService.getTagsForType(type);

            // Arrange in a hierarchy
            jTags = new ArrayList<JTag>();
            Map<String, Collection<JTag>> remainingTags = new HashMap<String, Collection<JTag>>();

            // Split in root and non-root tags
            for (DTag dTag : dTagIterable) {
                // Convert to JTag before we do anything
                JTag jTag = CONVERTER.convert(dTag);

                if (null == dTag.getParent())
                    jTags.add(jTag);
                else {
                    Collection<JTag> children = remainingTags.get(jTag.getParent());
                    if (null == children) {
                        children = new ArrayList<JTag>();
                        remainingTags.put(jTag.getParent(), children);
                    }
                    children.add(jTag);
                }
            }

            for (JTag parentTag : jTags)
                addChildren(parentTag, remainingTags);

            // Update memcache
            //memCache.put(TAG_CACHE_KEY + type, jTags); // TODO
        }

        return new ResponseEntity<Collection<JTag>>(jTags, HttpStatus.OK);
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
     * @param parent the parent
     * @return all list of child tags
     */
    @RestReturn(value=JTag.class, entity=JTag.class, code={
            @RestCode(code=200, message="OK", description="Tags found"),
            @RestCode(code=404, message="NOK", description="Tags not found")
    })
    @RequestMapping(value="parent/{parent}", method= RequestMethod.GET)
    public ResponseEntity<Collection<JTag>> getTagsForParent(HttpServletRequest request,
                                                 @PathVariable String parent) {

        Key parentKey = KeyFactory.stringToKey(parent);
        final Iterable<DTag> body = venueService.getTagsForParent(parentKey);

        if (null == body)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Parent:%s not found", parent));

        return new ResponseEntity<Collection<JTag>>((Collection<JTag>)CONVERTER.convert(body), HttpStatus.OK);
    }


    // Setters
    public void setVenueService(VenueService venueService) {
        this.venueService = venueService;
    }
}
