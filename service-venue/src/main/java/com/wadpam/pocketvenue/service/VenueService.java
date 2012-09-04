package com.wadpam.pocketvenue.service;

import com.wadpam.pocketvenue.dao.DPlaceDao;
import com.wadpam.pocketvenue.dao.DTagDao;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.pocketvenue.json.JTag;
import com.wadpam.server.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    public DPlace addPlace(String domain, Long parentId, String hierarchy, String name) {
        LOG.debug(String.format("Create new place with name %s", name));

        DPlace dPlace = new DPlace();
        if (null != parentId)
            dPlace.setParentId(parentId);
        if (null != hierarchy)
            dPlace.setHierarchy(hierarchy);
        dPlace.setName(name);

        // TODO use datastore parent

        placeDao.persistAndIndex(dPlace);

        return dPlace;
    }

    // Update a place
    public DPlace updatePlace(String domain, Long placeId, Long parentId, String hierarchy, String name) {
        LOG.debug(String.format("Update existing place with id %s", placeId));

        DPlace dPlace = getPlace(domain, placeId);

        if (null == dPlace)
            throw new NotFoundException(404, String.format("Place with id:%s not found", placeId));

        // Update and persist
        if (null != name)
            dPlace.setName(name);
        if (null != parentId)
            dPlace.setParentId(parentId);
        if (null != hierarchy)
            dPlace.setHierarchy(hierarchy);
        placeDao.persistAndIndex(dPlace);

        return dPlace;
    }

    // Get place by id
    public DPlace getPlace(String domain, Long id) {
        LOG.debug(String.format("Get place with id %s", id));

        DPlace dPlace = placeDao.findByPrimaryKey(id);

        if (null == dPlace)
            throw new NotFoundException(404, String.format("Place with id:%s not found", id));

        return dPlace;
    }

    // Delete a place by id
    public DPlace deletePlace(String domain, Long id) {
        LOG.debug(String.format("Delete place by id %s", id));

        DPlace dPlace = placeDao.findByPrimaryKey(id);

        if (null == dPlace)
            throw new NotFoundException(404, String.format("Place with id:%s not found", id));

        // Remove from datastore and update index
        placeDao.deleteAndUpdateIndex(dPlace);

        return dPlace;
    }

    // Return all places
    public String getAllPlaces(String domain, String cursor, int pagesize, Collection<DPlace> dPlaces) {
        LOG.debug("Get all places for parent");

        String newCursor = placeDao.getPlaces(cursor, pagesize, dPlaces);

        if (dPlaces.size() == 0)
            throw new NotFoundException(404, "No places found"); // TODO Maybe not throw exception

        return newCursor;
    }

    // Return all places for a parent
    public String getAllPlacesForParent(String domain, Long parentId, String cursor, int pagesize, Collection<DPlace> dPlaces) {
        LOG.debug("Get all places for parent");

        String newCursor = placeDao.getPlacesForParent(cursor, pagesize, parentId, dPlaces);

        if (dPlaces.size() == 0)
            throw new NotFoundException(404, "No places found");

        return newCursor;
    }

    // Return all places for a certain hierarchy
    public String getAllPlacesForHierarchy(String domain, String hierarchy, String cursor, int pagesize, Collection<DPlace> dPlaces) {
        LOG.debug("Get all places for hierarchy");

        String newCursor = placeDao.getPlacesForHierarchy(cursor, pagesize, hierarchy, dPlaces);

        if (dPlaces.size() == 0)
            throw new NotFoundException(404, "No places found");

        return newCursor;
    }

    // Return all places with matching tags
    public String getAllPlacesForTags(String domain, Long appTag1, Long appTag2, String cursor, int pagesize, Collection<DPlace> dPlaces) {
        LOG.debug(String.format("Get places for category tagId:%s and location tagId:%s", appTag1, appTag1));

        String newCursor = placeDao.getPlacesForTags(cursor, pagesize, appTag1, appTag1, dPlaces);

        if (dPlaces.size() == 0)
            throw new NotFoundException(404, "No places found");

        return null;
    }

    // Search for a place by name
    public Collection<DPlace> searchForPlace(String domain, String text) {
        LOG.debug(String.format("Search for place with name %s", text));

        // User Google search
        Collection<DPlace> dPlaces = placeDao.searchInIndexForPlaces(text);

        return dPlaces;
    }


    /* Tag related methods */

    // TODO: User datastore parent when creating tags

    // Create a new tag
    public DTag addTag(String domain, String type, Long parentId, String name) {
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
            dTag.setParentId(parentId);
        dTag.setName(name);

        // Store the new tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Update a tag
    public DTag updateTag(String domain, Long id, String type, Long parentId, String name) {
        LOG.debug(String.format("Create new tag with id:%s", id));

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
            dTag.setParentId(parentId);
        if (null != name)
            dTag.setName(name);

        // Store the update tag
        tagDao.persist(dTag);

        return dTag;
    }

    // Get tag by id
    public DTag getTag(String domain, Long id) {
        LOG.debug(String.format("Get tag with id:%s", id));

        // Find
        DTag dTag = tagDao.findByPrimaryKey(id);

        if (null == dTag)
            throw new NotFoundException(404, String.format("Tag with id:%s not found", id));

        return dTag;
    }

    // Delete a tag by id
    public DTag deleteTag(String domain, Long id) {
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
        Collection<DTag> childTags = tagDao.findByParentId(dTag.getId());
        deleteTags(childTags);



        return dTag;
    }

    // Recursively
    private void deleteTags(Collection<DTag> dTags) {
        if (null != dTags) {
            for (DTag dTag : dTags) {
                Collection<DTag> childTags = tagDao.findByParentId(dTag.getId());
                deleteTags(childTags);

                // Delete tag from venues
                placeDao.deleteTagId(dTag.getId());
            }
            tagDao.delete(dTags);
        }
    }

    // Get all tags of a certain type
    public Collection<DTag> getTagsForType(String domain, String type) {
        LOG.debug(String.format("Get all tags for type:%s", type));

        // Find
        Collection<DTag> tags = tagDao.findByType(type);

        if (tags.size() == 0)
            throw new NotFoundException(404, String.format("No tag of type:%s found", type));

        return  tags;
    }

    // Get all tags for a parent
    public Collection<DTag> getTagsForParent(String domain, Long parentId) {
        LOG.debug(String.format("Get all tags for parent:%s", parentId));

        // Find
        Collection<DTag> tags = tagDao.findByParentId(parentId);

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
