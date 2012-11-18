package com.wadpam.pocketvenue.service;

import com.google.appengine.api.datastore.*;
import com.wadpam.open.transaction.Idempotent;
import com.wadpam.pocketvenue.dao.DPlaceDao;
import com.wadpam.pocketvenue.dao.DTagDao;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.server.exceptions.BadRequestException;
import com.wadpam.server.exceptions.NotFoundException;
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
    private static final Logger LOG = LoggerFactory.getLogger(VenueService.class);

    // Base error code values for each class
    public static final int ERR_VENUE_SERVICE = 1000;
    public static final int ERR_TRANSLATION_SERVICE = 2000;

    private static final int ERR_NOT_FOUND = ERR_VENUE_SERVICE + 1;
    private static final int ERR_BAD_REQUEST = ERR_VENUE_SERVICE + 2;

    private DPlaceDao placeDao;
    private DTagDao tagDao;


    /* Brand related methods */

    // Create a new place with a certain hierarchy
    @Transactional
    @Idempotent
    public DPlace addPlace(JVenue jVenue) {
        LOG.debug("Create new place with name:{}", jVenue.getName());

        // Convert to domain object
        DPlace dPlace = convertJVenueToDomain(jVenue);

        if (null == dPlace)
            throw new BadRequestException(ERR_BAD_REQUEST, String.format("Not possible to convert input parameters to domain object"));

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

        if (null != from.getParent()) {
            LOG.debug("Check that parent exist with id:{}", from.getParent());
            Key parentKey = KeyFactory.stringToKey(from.getParent());
            DPlace parent = placeDao.findByPrimaryKey(parentKey);
            if (null == parent) {
                throw new BadRequestException(ERR_BAD_REQUEST, String.format("Parent does not exist:%s", from.getParent()));
            } else
                to.setParent(parentKey);
        }

        to.setName(from.getName());
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
            to.setLocation(new GeoPt(from.getLocation().getLatitude(), from.getLocation().getLongitude()));
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
        LOG.debug("Update existing place with id:{}", jVenue.getId());

        // Convert to domain object
        DPlace dPlace = convertJVenueToDomain(jVenue);

        if (null == dPlace)
            throw new BadRequestException(ERR_BAD_REQUEST , String.format("Not possible to convert input parameters to domain object"));

        // Check that the place exist
        DPlace existingPlace = getPlace(KeyFactory.stringToKey(jVenue.getId()));
        if (null == existingPlace)
            return null;

        // Store and index some fields
        dPlace.setId(existingPlace.getId());
        placeDao.persist(dPlace);

        return dPlace;
    }

    // Get place by key
    public DPlace getPlace(Key key) {
        LOG.debug("Get place with key:{}", key);

        DPlace dPlace = placeDao.findByPrimaryKey(key);

        return dPlace;
    }

    // Delete a place by key
    @Transactional
    @Idempotent
    public DPlace deletePlace(Key key) {
        LOG.debug("Delete place by key:{}", key);

        DPlace dPlace = placeDao.findByPrimaryKey(key);

        if (null == dPlace)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Place with key:%s not found during delete", key));

        // Remove from datastore and update index
        placeDao.delete(dPlace);

        return dPlace;
    }

    // Return all places
    public CursorPage<DPlace, Long> getAllPlaces(String cursor, int pagesize) {
        LOG.debug("Get all places");

        return placeDao.queryPage(pagesize, cursor);
    }

    // Return all places for a parentKey
    public CursorPage<DPlace, Long> getAllPlacesForParent(Key parentKey, String cursor, int pagesize) {
        LOG.debug("Get all places for parentKey:{}", parentKey);

        return placeDao.queryPageByParentKey(cursor, pagesize, parentKey);
    }

    // Return all place that does not have a parent
    public CursorPage<DPlace,Long> getAllPlacesWithNoParent(String cursor, int pagesize) {
        LOG.debug("Get all places without parent");

        return placeDao.queryPageByParentKey(cursor, pagesize, null);
    }

    // Return all places with matching tags
    public CursorPage<DPlace, Long> getAllPlacesForTags(String[] tags, String cursor, int pagesize) {
        LOG.debug("Get places for category tag ids:{}", tags);

        Collection<String> tagCollection = null;
        if (null != tags)
            tagCollection = new ArrayList<String>(Arrays.asList(tags));

        return placeDao.searchInIndexForPlaces(cursor, pagesize, null, tagCollection);
    }

    // Search for a place using free text and optional tags
    public CursorPage<DPlace, Long> textSearchForPlaces(String text, String[] tags, String cursor, int pageSize) {
        LOG.debug("Search for places with name:{} and tag ids:{}", text, tags);

        Collection<String> tagCollection = null;
        if (null != tags)
            tagCollection = new ArrayList<String>(Arrays.asList(tags));

        // User Google search
        return placeDao.searchInIndexForPlaces(cursor, pageSize, text, tagCollection);
    }

    // Search for nearby places
    public CursorPage<DPlace, Long> getNearbyPlaces(Float latitude, Float longitude, int radius, String[] tags, String cursor, int pageSize) {
        LOG.debug("Search for nearby places with lat:{} and lon:{}", latitude, longitude);

        Collection<String> tagCollection = null;
        if (null != tags)
            tagCollection = new ArrayList<String>(Arrays.asList(tags));

        // User Google search
        return placeDao.searchInIndexForNearby(cursor, pageSize, latitude, longitude, radius, tagCollection);
    }

    /* Tag related methods */

    // Create a new tag
    @Transactional
    @Idempotent()
    public DTag addTag(String type, Key parentKey, String name) {
        LOG.debug("Create new tag with type:{} name:{}", type, name);

        // Check if the parentKey exists
        DTag dParentTag = null;
        if (null != parentKey) {
            LOG.debug("Check that parentKey exists for id:{}", parentKey);
            dParentTag = tagDao.findByPrimaryKey(parentKey);
            if (null == dParentTag)
                throw new NotFoundException(ERR_BAD_REQUEST, String.format("Parent tag:%s does not exist when create", parentKey));
        }

        // Create the new tag
        DTag dTag = new DTag();
        dTag.setType(type);
        dTag.setName(name);
        if (null != dParentTag) {
            LOG.debug("Parent key:{}", tagDao.getPrimaryKey(dParentTag));
            dTag.setParent(parentKey);
        }

        // Store the new tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Update a tag
    @Transactional
    @Idempotent()
    public DTag updateTag(Key key, String type, Key parentKey, String name) {
        LOG.debug("Update tag with id:{}", key);

        // Find
        DTag dTag = tagDao.findByPrimaryKey(key);
        if (null == dTag)
            return null;

        // Check if the parentKey exists
        DTag dParentTag = null;
        if (null != parentKey) {
            dParentTag = tagDao.findByPrimaryKey(parentKey);
            if (null == dParentTag) {
                throw new NotFoundException(ERR_BAD_REQUEST, String.format("Parent tag:%s does not exist during update", parentKey));
            }
        }

        // Update
        dTag.setType(type);
        dTag.setName(name);
        if (null != dParentTag)
            dTag.setParent(parentKey);

        // Store the update tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Get tag by key
    public DTag getTag(Key key) {
        LOG.debug("Get tag with key:{}", key);

        return tagDao.findByPrimaryKey(key);
    }

    // Delete a tag by key
    @Transactional
    @Idempotent()
    public DTag deleteTag(Key key) {
        LOG.debug("Delete tag with key:{}", key);

        // Find
        DTag dTag = tagDao.findByPrimaryKey(key);

        if (null == dTag)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Tag with key:%s not found during delete", key));

        // Delete tag
        tagDao.delete(dTag);

        // Delete tag from venues
        placeDao.deleteTag(KeyFactory.keyToString((Key) tagDao.getPrimaryKey(dTag)));

        // Find and delete all children
        Iterable<DTag> dTagIterable = tagDao.queryAll(tagDao.getPrimaryKey(dTag));
        deleteTags(dTagIterable);

        return dTag;
    }

    // Recursively
    private void deleteTags(Iterable<DTag> dTags) {
        if (null != dTags && dTags.iterator().hasNext()) {

            for (DTag dTag : dTags) {
                LOG.debug("Delete child tag:{}", dTag);
                Iterable<DTag> dTagIterable = tagDao.queryAll(tagDao.getPrimaryKey(dTag));
                deleteTags(dTagIterable);

                // Delete tag from venues
                placeDao.deleteTag(KeyFactory.keyToString((Key) tagDao.getPrimaryKey(dTag)));
            }

            tagDao.deleteIterable(dTags);
        }
    }

    // Get all tags of a certain type
    public Iterable<DTag> getTagsForType(String type) {
        LOG.debug("Get all tags for type:{}", type);

        // Find tags
        return tagDao.queryByType(type);
    }

    // Get all tags for a parentKey
    public Iterable<DTag> getTagsForParent(Key parentKey) {
        LOG.debug("Get all tags for parentKey:{}", parentKey);

        // Find tags
        return tagDao.queryAll(parentKey);
    }


    // Setters
    public void setPlaceDao(DPlaceDao placeDao) {
        this.placeDao = placeDao;
    }

    public void setTagDao(DTagDao tagDao) {
        this.tagDao = tagDao;
    }

}
