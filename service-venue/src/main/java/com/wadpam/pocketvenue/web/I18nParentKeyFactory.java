package com.wadpam.pocketvenue.web;

import com.google.appengine.api.datastore.Key;

/**
 * Interface used when mapping translation parent types into ancestors.
 * @mattiaslevin
 */
public interface I18nParentKeyFactory {

    /**
     * Create a key with a specific id and ancestor
     * @param id the id of the parent
     * @return a parent ket
     */
    public Key createKey(Object id);
}
