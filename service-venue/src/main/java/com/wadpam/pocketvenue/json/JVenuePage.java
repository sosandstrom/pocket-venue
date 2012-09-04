package com.wadpam.pocketvenue.json;

import java.util.Collection;

/**
 * The Json object for a page of venues.
 * @author os
 */
public class JVenuePage {

    /** The cursor used to return the next page of venues */
    private String cursor;

    /**
     * The number of venues to return.
     * If the number of venues actually returned are less then the requested page size, end of pagination have been reached.
     */
    private long pageSize;

    /** The venues */
    private Collection<JVenue> venues;


    @Override
    public String toString() {
        return String.format("cursor:%s page size:%d venues:%s", cursor, pageSize, getVenues());
    }


    // Setters and Getters
    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public Collection<JVenue> getVenues() {
        return venues;
    }

    public void setVenues(Collection<JVenue> venues) {
        this.venues = venues;
    }
}
