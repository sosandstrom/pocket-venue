package com.wadpam.pocketvenue.service;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.wadpam.open.transaction.Idempotent;
import com.wadpam.pocketvenue.dao.DPlaceDao;
import com.wadpam.pocketvenue.dao.DTagDao;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.server.exceptions.BadRequestException;
import com.wadpam.server.exceptions.NotFoundException;
import com.wadpam.server.exceptions.ServerErrorException;
import net.sf.mardao.core.CursorPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Implementation of the venue service.
 * @author mattiaslevin
 */
public class VenueService {
    static final Logger LOG = LoggerFactory.getLogger(VenueService.class);
    static final int ERROR_CODE_NOT_FOUND = 40000;
    static final int ERROR_CODE_BAD_REQUEST = 40100;


    private DPlaceDao placeDao;
    private DTagDao tagDao;


    /* Brand related methods */

    // Create a new place with a certain hierarchy
    @Transactional
    @Idempotent
    public DPlace addPlace(JVenue jVenue) {
        LOG.debug(String.format("Create new place with name %s", jVenue.getName()));

        // Convert to domain object
        DPlace dPlace = convertJVenueToDomain(jVenue);

        if (null == dPlace)
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 2, String.format("Not possible to convert input parameters to domain object"), null, "Bad request");

        // Store and index fields
        placeDao.persist(dPlace);

        return dPlace;
    }

    // Convert from json object to domain object
    private DPlace convertJVenueToDomain(JVenue from) {

         if (null == from)
             return null;

        // Make the conversion
        DPlace to = new DPlace();

        if (null != from.getId())
            to.setId(Long.parseLong(from.getId()));
        to.setName(from.getName());

        if (null != from.getParentId()) {
            LOG.debug("Check that parent exist with id:{}", from.getParentId());
            DPlace parent = placeDao.findByPrimaryKey(from.getParentId());
            if (null == parent) {
                throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 1, String.format("Parent does not exist:%s", from.getParentId()), null, "Bad request");
            } else
                to.setParentKey((Key)placeDao.getPrimaryKey(parent));
        }

        to.setShortDescription(from.getShortDescription());
        to.setDescription(from.getDescription());
        to.setOpeningHours(from.getOpeningHours());
        to.setTags(from.getTags());
        to.setStreet(from.getStreet());
        to.setCityArea(from.getCityArea());
        to.setCity(from.getCity());
        to.setCounty(from.getCounty());
        to.setPostalCode(from.getPostalCode());
        to.setCountry(from.getCountry());
        if (null != from.getLocation() && null != from.getLocation().getLatitude() && null != from.getLocation().getLongitude())
            to.setLocation(new GeoPt(from.getLocation().getLatitude(), from.getLocation().getLatitude()));
        to.setPhoneNumber(from.getPhoneNumber());
        if (null != from.getEmail())
            to.setEmail(new Email(from.getEmail()));
        if (null != from.getWebUrl())
            to.setWebUrl(new Link(from.getWebUrl()));
        if (null != from.getFacebookUrl())
            to.setFacebookUrl(new Link(from.getFacebookUrl()));
        if (null != from.getTwitterUrl())
            to.setTwitterUrl(new Link(from.getTwitterUrl()));
        if (null != from.getLogoUrl())
            to.setLogoUrl(new Link(from.getLogoUrl()));
        if (null != from.getImageUrls()) {
            Collection<Link> links = new ArrayList<Link>(from.getImageUrls().size());
            for (String link : from.getImageUrls())
                links.add(new Link(link));
            to.setImageUrls(links);
        }

        return to;
    }

    // Update a place
    @Transactional
    @Idempotent
    public DPlace updatePlace(JVenue jVenue) {
        LOG.debug(String.format("Update existing place with id %s", jVenue.getId()));

        // Convert to domain object
        DPlace dPlace = convertJVenueToDomain(jVenue);

        if (null == dPlace)
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 3, String.format("Not possible to convert input parameters to domain object"), null, "Bad request");

        // Check that the place exist
        DPlace existingPlace = getPlace(dPlace.getId());
        if (null == existingPlace)
            return null;

        // Store and index some fields
        placeDao.persist(dPlace);

        return dPlace;
    }

    // Get place by id
    public DPlace getPlace(Long id) {
        LOG.debug(String.format("Get place with id %s", id));

        DPlace dPlace = placeDao.findByPrimaryKey(id);

        return dPlace;
    }

    // Delete a place by id
    @Transactional
    @Idempotent
    public DPlace deletePlace(Long id) {
        LOG.debug(String.format("Delete place by id %s", id));

        DPlace dPlace = placeDao.findByPrimaryKey(id);

        if (null == dPlace)
            throw new NotFoundException(ERROR_CODE_NOT_FOUND + 1, String.format("Place with id:%s not found during delete", id), null, "Delete failed");

        // Remove from datastore and update index
        placeDao.delete(dPlace);

        return dPlace;
    }

    // Return all places
    public CursorPage<DPlace, Long> getAllPlaces(String cursor, int pagesize) {
        LOG.debug("Get all places");

        return placeDao.queryPage(pagesize, cursor);
    }

    // Return all places for a parent
    public CursorPage<DPlace, Long> getAllPlacesForParent(Long parentId, String cursor, int pagesize) {
        LOG.debug("Get all places for parent:%s", parentId);

        return placeDao.queryPageByParentKey(cursor, pagesize, placeDao.createKey(parentId));
    }

    // Return all places with matching tags
    public CursorPage<DPlace, Long> getAllPlacesForTags(Long[] tagIds, String cursor, int pagesize) {
        LOG.debug(String.format("Get places for category tag ids:%s", tagIds));

        Collection<Long> tags = null;
        if (null != tagIds)
            tags = new ArrayList<Long>(Arrays.asList(tagIds));

        return placeDao.searchInIndexForPlaces(cursor, pagesize, null, tags);
    }

    // Search for a place using free text and optional tags
    public CursorPage<DPlace, Long> textSearchForPlaces(String text, Long[] tagIds, String cursor, int pageSize) {
        LOG.debug(String.format("Search for places with name:%s and tag ids:%s", text, tagIds));

        Collection<Long> tags = null;
        if (null != tagIds)
                tags = new ArrayList<Long>(Arrays.asList(tagIds));

        // User Google search
        return placeDao.searchInIndexForPlaces(cursor, pageSize, text, tags);
    }

    // Search for nearby places
    public CursorPage<DPlace, Long> getNearbyPlaces(Float latitude, Float longitude, int radius, Long[] tagIds, String cursor, int pageSize) {
        LOG.debug(String.format("Search for nearby places with lat:%s and lon:%s", latitude, longitude));

        Collection<Long> tags = null;
        if (null != tagIds)
            tags = new ArrayList<Long>(Arrays.asList(tagIds));

        // User Google search
        return placeDao.searchInIndexForNearby(cursor, pageSize, latitude, longitude, radius, tags);
    }

    /* Tag related methods */

    // Create a new tag
    @Transactional
    @Idempotent()
    public DTag addTag(String type, Long parentId, String name) {
        LOG.debug(String.format("Create new tag with type:%s name:%s", type, name));

        // Check if the parent exists
        if (null != parentId) {
            DTag dParentTag = tagDao.findByPrimaryKey(parentId);
            if (null == dParentTag) {
                throw new NotFoundException(ERROR_CODE_BAD_REQUEST + 3, String.format("Parent tag:%s does not exist when create", parentId), null, "Failed creating new tag");
            }
        }

        // Create the new tag
        DTag dTag = new DTag();
        dTag.setType(type);
        dTag.setName(name);
        if (null != parentId)
            dTag.setParentKey(placeDao.createKey(parentId));

        // Store the new tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Update a tag
    @Transactional
    @Idempotent()
    public DTag updateTag(Long id, String type, Long parentId, String name) {
        LOG.debug(String.format("Update tag with id:%s", id));

        // Find
        DTag dTag = tagDao.findByPrimaryKey(id);
        if (null == dTag)
            return null;

        // Check if the parent exists
        if (null != parentId) {
            DTag dParentTag = tagDao.findByPrimaryKey(parentId);
            if (null == dParentTag) {
                throw new NotFoundException(ERROR_CODE_BAD_REQUEST + 4, String.format("Parent tag:%s does not exist during update", parentId), null, "Update tag failed");
            }
        }

        // Update
        dTag.setType(type);
        dTag.setName(name);
        if (null != parentId)
            dTag.setParentKey(placeDao.createKey(parentId));

        // Store the update tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Get tag by id
    public DTag getTag(Long id) {
        LOG.debug(String.format("Get tag with id:%s", id));

        return tagDao.findByPrimaryKey(id);
    }

    // Delete a tag by id
    @Transactional
    @Idempotent()
    public DTag deleteTag(Long id) {
        LOG.debug(String.format("Delete tag with id:%s", id));

        // Find
        DTag dTag = tagDao.findByPrimaryKey(id);

        if (null == dTag)
            throw new NotFoundException(ERROR_CODE_NOT_FOUND + 2, String.format("Tag with id:%s not found during delete", id), null, "Delete tag failed");

        // Delete tag
        tagDao.delete(dTag);

        // Delete tag from venues
        placeDao.deleteTagId(dTag.getId());

        // Find and delete all children
        Iterable<DTag> dTagIterable = tagDao.queryAll(tagDao.createKey(dTag.getId()));
        deleteTags(dTagIterable);

        return dTag;
    }

    // Recursively
    private void deleteTags(Iterable<DTag> dTags) {
        if (null != dTags) {
            for (DTag dTag : dTags) {
                Iterable<DTag> dTagIterable = tagDao.queryAll(tagDao.createKey(dTag.getId()));
                deleteTags(dTagIterable);

                // Delete tag from venues
                placeDao.deleteTagId(dTag.getId());
            }

            tagDao.deleteIterable(dTags);
        }
    }

    // Get all tags of a certain type
    public Iterable<DTag> getTagsForType(String type) {
        LOG.debug(String.format("Get all tags for type:%s", type));

        // Find tags
        return tagDao.queryByType(type);
    }

    // Get all tags for a parent
    public Iterable<DTag> getTagsForParent(Long parentId) {
        LOG.debug(String.format("Get all tags for parent:%s", parentId));

        // Find tags
        return tagDao.queryAll(tagDao.createKey(parentId));
    }


    // Setters
    public void setPlaceDao(DPlaceDao placeDao) {
        this.placeDao = placeDao;
    }

    public void setTagDao(DTagDao tagDao) {
        this.tagDao = tagDao;
    }
}
