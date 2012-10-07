package com.wadpam.pocketvenue.dao;

import com.google.appengine.api.datastore.Key;
import com.wadpam.pocketvenue.domain.DPlace;
import net.sf.mardao.core.CursorPage;

import java.util.Collection;
import java.util.List;

/**
 * Business Methods interface for entity DPlace.
 * This interface is generated by mardao, but edited by developers.
 * It is not overwritten by the generator once it exists.
 *
 * Generated on 2012-09-02T11:34:32.312+0700.
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
public interface DPlaceDao extends GeneratedDPlaceDao {


    /**
     * Search for a place based on a text string.
     * @param cursor The cursor returned from the previous call to this method. If this is the first call, use null.
     * @param pageSize The number of places to return
     * @param text the search text
     * @param tagIds an optional list of tag id
     * @return a page of places
     */
    public CursorPage<DPlace, Long> searchInIndexForPlaces(String cursor, int pageSize, String text, Collection<Long> tagIds);

    /**
     * Get places nearby
     * @param cursor the cursor returned from the previous call to this method. If this is the first call, use null.
     * @param pageSize the number of places to return
     * @param latitude the latitude to search around
     * @param longitude the longitude to search around
     * @param radius the radius to search within
     * @param tagIds A list of tags ids to match against
     * @return a new cursor that can be used to get the next products.
     */
    public CursorPage<DPlace, Long> searchInIndexForNearby(String cursor, int pageSize, Float latitude,
                                                           Float longitude, int radius, Collection<Long> tagIds);

    /**
     * Delete the tag id from all venues
     * @param tagId the tag id to delete
     */
    public void deleteTagId(Long tagId);

    /**
     * Create a datastore key.
     * @param id the unique place id.
     * @return a detastore key
     */
    public Key createKey(Long id);


    /**
     * Get place for parent key.
     * @param cursor the cursor returned from the previous call to this method. If this is the first call, use null.
     * @param pageSize the number of place to return
     * @param parentKey the parent key
     * @return a page of places
     */
    public CursorPage<DPlace, Long> queryPageByParentKey(String cursor, int pageSize, Key parentKey);
}
