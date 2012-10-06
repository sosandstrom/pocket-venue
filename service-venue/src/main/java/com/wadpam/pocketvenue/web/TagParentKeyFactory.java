package com.wadpam.pocketvenue.web;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Create a key of DTag ancestor kind.
 * @mattiaslevin
 */
public class TagParentKeyFactory implements I18nParentKeyFactory {

    @Override
    public Key createKey(Object id) {
        return KeyFactory.createKey("DTag", (Long)id);
    }
}
