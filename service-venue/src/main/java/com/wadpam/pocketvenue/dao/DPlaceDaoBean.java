package com.wadpam.pocketvenue.dao;


import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.search.*;
import com.google.appengine.api.search.Index;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.server.exceptions.BadRequestException;
import net.sf.mardao.core.CursorPage;
import net.sf.mardao.core.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of Business Methods related to entity DPlace.
 * This (empty) class is generated by mardao, but edited by developers.
 * It is not overwritten by the generator once it exists.
 *
 * Generated on 2012-09-02T11:34:32.312+0700.
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
public class DPlaceDaoBean 
	extends GeneratedDPlaceDaoImpl
		implements DPlaceDao
{

    static final Logger LOG = LoggerFactory.getLogger(DPlaceDao.class);

    static final String SEARCH_INDEX = "searchIndex";

    // Default constructor to enable caching by Mardao
    public DPlaceDaoBean() {
        this.memCacheEntities = true;
        this.memCacheAll = false;
    }


    // Persist a place and update the index
    @Override
    public Long persist(DPlace dPlace) {

        // Persist
        Long placeId = super.persist(dPlace);

        // Update the index
        updateIndex(dPlace);

        return placeId;
    }

    // Update the index
    private void updateIndex(DPlace dPlace) {

        try {
            // Index the text search
            Document.Builder searchBuilder = Document.newBuilder()
                    .setId(Long.toString(dPlace.getId()));

            // Name
            if (null != dPlace.getName() && dPlace.getName().isEmpty() == false)
                searchBuilder.addField(Field.newBuilder().setName("name").setText(dPlace.getName()));

            // City
            if (null != dPlace.getCity() && dPlace.getCity().isEmpty() == false)
                searchBuilder.addField(Field.newBuilder().setName("city").setText(dPlace.getCity()));

            // Tag ids
            if (null != dPlace.getTags() && dPlace.getTags().size() > 0) {
                String tagsString = buildTagsString(dPlace.getTags());
                searchBuilder.addField(Field.newBuilder().setName("tags").setText(tagsString));
            }

            // Index the geo location
            if (null != dPlace.getLocation()) {
                GeoPoint geoPoint = new GeoPoint(dPlace.getLocation().getLatitude(), dPlace.getLocation().getLongitude());
                LOG.info("GeoPoint:{}", geoPoint);
                searchBuilder.addField(Field.newBuilder().setName("location").setGeoPoint(geoPoint));
            }

            getSearchIndex().add(searchBuilder.build());

        } catch (AddException e) {
            if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
                LOG.error("Not possible to add document to index");
                // TODO: Error handling missing
            }
        }
    }

    // Concatenate all tag ids into a string with " " (blank) in between
    private String buildTagsString (Collection<Long> tags) {
        StringBuilder result = new StringBuilder();
        for(Long tag : tags) {
            result.append(Long.toString(tag));
            result.append(" ");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    // Build search index
    private Index getSearchIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder()
                .setName(SEARCH_INDEX)
                .setConsistency(Consistency.PER_DOCUMENT)
                .build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    // Delete and update index
    public boolean delete(DPlace dPlace) {

        // Delete from data store
        boolean result =  super.delete(dPlace);

        // Remove from index
        getSearchIndex().remove(Long.toString(dPlace.getId()));

        return result;
    }


    // Search in the index for matching places
    @Override
    public CursorPage<DPlace, Long> searchInIndexForPlaces(String cursor, int pageSize, String text, Collection<Long> tagIds) {

        // Build the query string
        StringBuilder queryString = new StringBuilder();
        if (null != tagIds && tagIds.size() > 0) {

            Iterator<Long> iterator = tagIds.iterator();

            while (iterator.hasNext()) {
                queryString.append("tags:").append(iterator.next());
                if (iterator.hasNext())
                    queryString.append(" AND ");
            }

            if (null != text && text.isEmpty() == false) {
                queryString.append(" AND ").append("(")
                        .append("name:").append(text)
                        .append(" OR ")
                        .append("city:").append(text)
                        .append(")");
            }

        }
        else {
            if (null != text && text.isEmpty() == false)  {
                queryString.append("name:").append(text)
                        .append(" OR ")
                        .append("city:").append(text);
            }
        }

        // Should not happen but guard to avoid null pointer exceptions later on
        if (queryString.length() == 0)
            return null;

        // Options
        QueryOptions.Builder builder = QueryOptions.newBuilder()
                .setLimit(pageSize);

        if (null == cursor)
            builder.setCursor(com.google.appengine.api.search.Cursor.newBuilder().build());
        else
            builder.setCursor(com.google.appengine.api.search.Cursor.newBuilder().build(cursor));

        // Build query
        com.google.appengine.api.search.Query query = com.google.appengine.api.search.Query.newBuilder()
                    .setOptions(builder.build())
                    .build(queryString.toString());

        return searchInIndexWithQuery(query, getSearchIndex());
    }

    // Search for nearby places
    @Override
    public CursorPage<DPlace, Long> searchInIndexForNearby(String cursor, int pageSize, Float latitude,
                                         Float longitude, int radius, Collection<Long> tagIds) {

        StringBuilder queryString = new StringBuilder();

        // Add tags if provided
        if (null != tagIds && tagIds.size() > 0) {
            Iterator<Long> iterator = tagIds.iterator();

            while (iterator.hasNext()) {
                queryString.append("tags:").append(iterator.next());
                if (iterator.hasNext())
                    queryString.append(" AND ");
            }
        }

        // Build the query string
        if (null != latitude && null != longitude) {
            if (queryString.length() > 0)
                queryString.append(" AND ");

            queryString.append(String.format("distance(location, geopoint(%f, %f)) < %d", latitude, longitude, radius));
        }

        // Should not happen but guard to avoid null pointer exceptions later on
        if (queryString.length() == 0)
            return null;

        // Sort expression
        String sortString = String.format("distance(location, geopoint(%f, %f))", latitude, longitude);
        SortExpression sortExpression = SortExpression.newBuilder()
                .setExpression(sortString)
                .setDirection(SortExpression.SortDirection.ASCENDING)
                .setDefaultValueNumeric(radius + 1)
                .build();

        // Options
        QueryOptions.Builder optionBuilder = QueryOptions.newBuilder()
                .setSortOptions(SortOptions.newBuilder().addSortExpression(sortExpression))
                .setLimit(pageSize);

        if (null == cursor)
            optionBuilder.setCursor(com.google.appengine.api.search.Cursor.newBuilder().build());
        else
            optionBuilder.setCursor(com.google.appengine.api.search.Cursor.newBuilder().build(cursor));

        // Build query
        com.google.appengine.api.search.Query query = com.google.appengine.api.search.Query.newBuilder()
                .setOptions(optionBuilder.build())
                .build(queryString.toString());

        return searchInIndexWithQuery(query, getSearchIndex());
    }

    // Search in index for a query
    private CursorPage<DPlace, Long> searchInIndexWithQuery(com.google.appengine.api.search.Query query, Index index) {

        try {
            // Query the index.
            Results<ScoredDocument> results = index.search(query);
            LOG.debug("Found {} hits in the index", results.getNumberFound());
            LOG.debug("Returned {} hits", results.getResults().size());

            Collection<Long> ids = new ArrayList<Long>();
            for (ScoredDocument document : results) {
                // Collect all the primary keys
                ids.add(Long.parseLong(document.getId()));
            }

            if (ids.size() > 0) {
                // We got results, get the places from datastore
                Iterable<DPlace> dPlaceIterable = queryByPrimaryKeys(null, ids);

                CursorPage<DPlace, Long> cursorPage = new CursorPage<DPlace, Long>();

                // Get a cursor
                if (null != results.getCursor())  {
                    LOG.debug("New cursor:{}", results.getCursor().toWebSafeString());
                    cursorPage.setCursorKey(results.getCursor().toWebSafeString());
                }

                Collection<DPlace> dPlaces = new ArrayList<DPlace>(ids.size());
                cursorPage.setItems(dPlaces);

                Iterator<DPlace> iterator = dPlaceIterable.iterator();
                while (iterator.hasNext())
                    dPlaces.add(iterator.next());

                return cursorPage;
            } else {
                // No results
                return new CursorPage<DPlace, Long>();
            }
        } catch (SearchException e) {
            if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
                LOG.error("Search index failed");
                // TODO: Error handling missing
            }
            throw new SearchException("Searching index failed");
        }
    }

    // Delete a tag id from all venues
    public void deleteTagId(Long tagId) {
        LOG.debug(String.format("Delete tag id:%s from all places", tagId));

        // Find all places with tag

        Iterable<DPlace> dPlaceIterable = null;
        final Filter filter = createEqualsFilter(COLUMN_NAME_TAGS, tagId);
        dPlaceIterable = queryIterable(false, 0, -1, null, null, null, false, null, false, filter);

        Collection<DPlace> updatedTags = new ArrayList<DPlace>();
        Iterator<DPlace> iterator = dPlaceIterable.iterator();
        while (iterator.hasNext()) {
            DPlace dPlace = iterator.next();

            Collection<Long> existingTagIds = dPlace.getTags();
            if (null != existingTagIds) {
                existingTagIds.remove(tagId);
                updatedTags.add(dPlace);
            }
        }

        // Save to datastore
        update(updatedTags);

        // Update the index
        for (DPlace dPlace : updatedTags)
            updateIndex(dPlace);
    }

    // Create datastore key
    @Override
    public Key createKey(Long id) {
        return super.createCoreKey(null, id);
    }

    // get datastore key
    @Override
    public Key getKey(DPlace dPlace) {
        return (Key)this.getPrimaryKey(dPlace);
    }

    // Get places for parent key
    @Override
    public CursorPage<DPlace, Long> queryPageByParentKey(String cursor, int pageSize, Key parentKey) {

        return super.queryPage(false, pageSize, parentKey, null, null, false, null, false, cursor, null);
    }


}
