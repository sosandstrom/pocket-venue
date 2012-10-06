package com.wadpam.pocketvenue.web;

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
    static final int ERROR_CODE_NOT_FOUND = 20000;
    static final int ERROR_CODE_SEVER_ERROR = 20200;

    static final String TAG_CACHE_KEY = "tagKey:";

    static final Converter CONVERTER = new Converter();

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
                               @RequestParam(required = false) Long parentId,
                               @RequestParam(required = true) String type,
                               @RequestParam(required = true) String name) {

        final DTag body = venueService.addTag(type, parentId, name);

        if (null == body)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 1, String.format("Not possible to create new tag:%s", name), null, "Create tag failed");

        // Invalidate the memcache
        MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
        memCache.delete(tagCacheKey(body));

        return new RedirectView(request.getRequestURI() + "/" + body.getId().toString());
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
                                  @PathVariable Long id,
                                  @RequestParam(required = false) Long parentId,
                                  @RequestParam(required = true) String type,
                                  @RequestParam(required = true) String name) {

        final DTag body = venueService.updateTag(id, type, parentId, name);

        if (null == body)
            throw new NotFoundException(ERROR_CODE_NOT_FOUND + 1, String.format("Tag:%s not found during update", name), null, "Update tag failed");

        // Invalidate the memcache
        MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();
        memCache.delete(tagCacheKey(body));

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

        final DTag body = venueService.getTag(id);

        if (null == body)
            throw new NotFoundException(ERROR_CODE_NOT_FOUND + 2, String.format("Tag with id:%s not found", id), null, "Tag not found");

        return new ResponseEntity<JTag>(CONVERTER.convert(body), HttpStatus.OK);
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
                                          @PathVariable Long id) {

        final DTag body = venueService.deleteTag(id);

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
            Map<Long, Collection<JTag>> remainingTags = new HashMap<Long, Collection<JTag>>();

            // Split in root and non-root tags
            for (DTag dTag : dTagIterable) {
                // Convert to JTag before we do anything
                JTag jTag = Converter.convert(dTag);

                if (null == dTag.getParentKey())
                    jTags.add(jTag);
                else {
                    Collection<JTag> children = remainingTags.get(jTag.getParentId());
                    if (null == children) {
                        children = new ArrayList<JTag>();
                        remainingTags.put(jTag.getParentId(), children);
                    }
                    children.add(jTag);
                }
            }

            for (JTag parentTag : jTags)
                addChildren(parentTag, remainingTags);

            // Update memcache
            memCache.put(TAG_CACHE_KEY + type, jTags);
        }

        return new ResponseEntity<Collection<JTag>>(jTags, HttpStatus.OK);
    }

    // Build a hierarchy of tags
    private void addChildren(JTag parentTag, Map<Long, Collection<JTag>> tags) {
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
                                                 @PathVariable Long id) {

        final Iterable<DTag> body = venueService.getTagsForParent(id);

        if (null == body)
            throw new NotFoundException(ERROR_CODE_NOT_FOUND + 3, String.format("Parent:%s not found", id), null, "Tags not found");

        return new ResponseEntity<Collection<JTag>>((Collection<JTag>)CONVERTER.convert(body), HttpStatus.OK);
    }


    // Setters
    public void setVenueService(VenueService venueService) {
        this.venueService = venueService;
    }
}
