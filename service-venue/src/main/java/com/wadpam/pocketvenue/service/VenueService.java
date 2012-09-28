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
            throw new BadRequestException(400, String.format("Bad request, not possible to convert to domain object"));

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

        to.setId(Long.parseLong(from.getId()));
        to.setName(from.getName());
        to.setParentKey(placeDao.createKey(from.getParentId()));
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
        if (null != from.getLocation())
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
        LOG.debug(String.format("Update existing place with id %s", jVenue.getId()));

        // Convert to domain object
        DPlace dPlace = convertJVenueToDomain(jVenue);
        if (null == dPlace)
            throw new BadRequestException(400, String.format("Bad request, not possible to convert to domain object"));

        // Check that the place exist already
        DPlace existingPlace = getPlace(dPlace.getId());
        if (null == existingPlace)
            throw new NotFoundException(404, String.format("Place with id:%s not found", dPlace.getId()));

        // Store and index some fields
        placeDao.persist(dPlace);

        return dPlace;
    }

    // Get place by id
    public DPlace getPlace(Long id) {
        LOG.debug(String.format("Get place with id %s", id));

        DPlace dPlace = placeDao.findByPrimaryKey(id);
        if (null == dPlace)
            throw new NotFoundException(404, String.format("Place with id:%s not found", id));

        return dPlace;
    }

    // Delete a place by id
    @Transactional
    @Idempotent
    public DPlace deletePlace(Long id) {
        LOG.debug(String.format("Delete place by id %s", id));

        DPlace dPlace = placeDao.findByPrimaryKey(id);
        if (null == dPlace)
            throw new NotFoundException(404, String.format("Place with id:%s not found", id));

        // Remove from datastore and update index
        placeDao.delete(dPlace);

        return dPlace;
    }

    // Return all places
    public String getAllPlaces(String cursor, int pagesize, Collection<DPlace> dPlaces) {
        LOG.debug("Get all places for parent");

        String newCursor = placeDao.getAllPlaces(cursor, pagesize, dPlaces);

        return newCursor;
    }

    // Return all places for a parent
    public String getAllPlacesForParent(Long parentId, String cursor, int pagesize, Collection<DPlace> dPlaces) {
        LOG.debug("Get all places for parent");

        String newCursor = placeDao.getPlacesForParent(cursor, pagesize, parentId, dPlaces);

        return newCursor;
    }

    // Return all places with matching tags
    public String getAllPlacesForTags(Long[] tagIds, String cursor, int pagesize, Collection<DPlace> dPlaces) {
        LOG.debug(String.format("Get places for category tag ids:%s", tagIds));

        String newCursor = placeDao.searchInIndexForPlaces(cursor, pagesize, null, Arrays.asList(tagIds), dPlaces);

        return newCursor;
    }

    // Search for a place using free text
    public String textSearchForPlaces(String text, Long[] tagIds, String cursor, int pageSize, Collection<DPlace> result) {
        LOG.debug(String.format("Search for place with name %s and tag ids:%s", text, tagIds));

        // User Google search
        String newCursor = placeDao.searchInIndexForPlaces(cursor, pageSize, text, Arrays.asList(tagIds), result);

        return newCursor;
    }

    // Search for nearby places
    public Collection<DPlace> getNearbyPlaces(Float latitude, Float longitude, int radius, Long[] tagIds, int limit) {

        // User Google search
        Collection<DPlace> result = new ArrayList<DPlace>();
        String newCursor = placeDao.searchInIndexForNearby(null, limit, latitude, longitude, radius, Arrays.asList(tagIds), result);

        return result;
    }

    /* Tag related methods */

    // Create a new tag
    @Idempotent()
    public DTag addTag(String type, Long parentId, String name) {
        LOG.debug(String.format("Create new tag with type:%s name:%s", type, name));

        // Check if the parent exists
        if (null != parentId) {
            DTag dParentTag = tagDao.findByPrimaryKey(parentId);
            if (null == dParentTag) {
                throw new NotFoundException(404, String.format("Parent tag:%s does not exist", parentId));
            }
        }

        // Create the new tag
        DTag dTag = new DTag();
        dTag.setType(type);
        if (null != parentId)
            dTag.setParentKey(placeDao.createKey(parentId));
        dTag.setName(name);

        // Store the new tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Update a tag
    @Idempotent()
    public DTag updateTag(Long id, String type, Long parentId, String name) {
        LOG.debug(String.format("Update tag with id:%s", id));

        // Find
        DTag dTag = tagDao.findByPrimaryKey(id);
        if (null == dTag)
            throw new NotFoundException(404, String.format("Tag with id:%s not found", id));

        // Check if the parent exists
        if (null != parentId) {
            DTag dParentTag = tagDao.findByPrimaryKey(parentId);
            if (null == dParentTag) {
                throw new NotFoundException(404, String.format("Parent tag:%s does not exist", parentId));
            }
        }

        // Update
        if (null != type)
            dTag.setType(type);
        if (null != parentId)
            dTag.setParentKey(placeDao.createKey(parentId));
        if (null != name)
            dTag.setName(name);

        // Store the update tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Get tag by id
    public DTag getTag(Long id) {
        LOG.debug(String.format("Get tag with id:%s", id));

        // Find
        DTag dTag = tagDao.findByPrimaryKey(id);

        if (null == dTag)
            throw new NotFoundException(404, String.format("Tag with id:%s not found", id));

        return dTag;
    }

    // Delete a tag by id
    @Idempotent()
    public DTag deleteTag(Long id) {
        LOG.debug(String.format("Delete tag with id:%s", id));

        // Find
        DTag dTag = tagDao.findByPrimaryKey(id);

        if (null == dTag)
            throw new NotFoundException(404, String.format("Tag with id:%s not found", id));

        // Delete
        tagDao.delete(dTag);

        // Delete tag from venues
        placeDao.deleteTagId(dTag.getId());

        // Find and delete all children
        Collection<DTag> childTags = tagDao.findByParentKey(tagDao.createKey(dTag.getId()));
        deleteTags(childTags);

        return dTag;
    }

    // Recursively
    private void deleteTags(Collection<DTag> dTags) {
        if (null != dTags) {
            for (DTag dTag : dTags) {
                Collection<DTag> childTags = tagDao.findByParentKey(tagDao.createKey(dTag.getId()));
                deleteTags(childTags);

                // Delete tag from venues
                placeDao.deleteTagId(dTag.getId());
            }
            tagDao.delete(dTags);
        }
    }

    // Get all tags of a certain type
    public Collection<DTag> getTagsForType(String type) {
        LOG.debug(String.format("Get all tags for type:%s", type));

        // Find
        Collection<DTag> tags = tagDao.findByType(type);

        if (tags.size() == 0)
            throw new NotFoundException(404, String.format("No tag of type:%s found", type));

        return  tags;
    }

    // Get all tags for a parent
    public Collection<DTag> getTagsForParent(Long parentId) {
        LOG.debug(String.format("Get all tags for parent:%s", parentId));

        // Find
        Collection<DTag> tags = tagDao.findByParentKey(tagDao.createKey(parentId));
        if (tags.size() == 0)
            throw new NotFoundException(404, String.format("No tags for parent:%s found", parentId));

        return  tags;
    }

    // Setters
    public void setPlaceDao(DPlaceDao placeDao) {
        this.placeDao = placeDao;
    }

    public void setTagDao(DTagDao tagDao) {
        this.tagDao = tagDao;
    }
}
