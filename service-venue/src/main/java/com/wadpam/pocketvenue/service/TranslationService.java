package com.wadpam.pocketvenue.service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.wadpam.open.transaction.Idempotent;
import com.wadpam.pocketvenue.dao.Di18nTranslationDao;
import com.wadpam.pocketvenue.domain.Di18nTranslation;
import com.wadpam.server.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle localization of resources (strings, images and urls)
 * @mattiaslevin
 */
public class TranslationService {
    private static final Logger LOG = LoggerFactory.getLogger(TranslationService.class);

    private static final int ERR_NOT_FOUND = VenueService.ERR_TRANSLATION_SERVICE + 1;

    private Di18nTranslationDao i18nDao;

    // Add or update a translation
    @Transactional
    @Idempotent
    public Di18nTranslation addTranslation(Key parentKey, String locale, String localizedString,
                                           String localizedImageUrl, String localizedLinkUrl) {
        LOG.debug("Add new translation:{} for local:{}", localizedString, locale);

        Di18nTranslation di18n = new Di18nTranslation();
        di18n.setParent(parentKey);
        di18n.setLocale(locale);
        di18n.setLocalizedString(localizedString);
        if (null != localizedImageUrl)
            di18n.setLocalizedImage(new Link(localizedImageUrl));
        if (null != localizedLinkUrl)
            di18n.setLocalizedUrl(new Link(localizedLinkUrl));

        // Save in datastore
        i18nDao.persist(di18n);

        return di18n;
    }

    // Get translation with id
    public Di18nTranslation getTranslation(Key key) {
        LOG.debug("Get translation with id:{}", key);

        return i18nDao.findByPrimaryKey(key);
    }

    // Get a translation for a specific parent and locale
    public Di18nTranslation getTranslation(Key parentKey, String locale) {
        LOG.debug("Get translation for locale:{} and parent:{}", locale, parentKey);

        return i18nDao.findByPrimaryKey(parentKey, locale);
    }

    // Get translations for a specific parent
    public Iterable<Di18nTranslation> getTranslations(Key parentKey) {
        LOG.debug("Get translations for parent:{}", parentKey);

        return i18nDao.queryAll(parentKey);
    }

    // Get translations for a list of parents
    public Map<Key, Iterable<Di18nTranslation>> getTranslations(Collection<Key> parentKeys) {
        LOG.debug("Get translations for parents:{}", parentKeys);

        Map<Key, Iterable<Di18nTranslation>> resultMap = new HashMap<Key, Iterable<Di18nTranslation>>();
        for (Key parentKey : parentKeys) {
            Iterable<Di18nTranslation> di18nIterable = this.getTranslations(parentKey);
            if (null != di18nIterable)
                resultMap.put(parentKey, di18nIterable);
        }

        return resultMap;
    }


    // Get translations for a list of parents in a specific locale
    public Map<Key, Di18nTranslation> getTranslations(Collection<Key> parentKeys, String locale) {
        LOG.debug("Get translations for parents:{}", parentKeys);

        Map<Key, Di18nTranslation> resultMap = new HashMap<Key, Di18nTranslation>();
        for (Key parentKey : parentKeys) {
            Di18nTranslation di18nIterable = this.getTranslation(parentKey, locale);
            if (null != di18nIterable)
                resultMap.put(parentKey, di18nIterable);
        }

        return resultMap;
    }

    // Delete a translation with id
    @Transactional
    @Idempotent
    public Di18nTranslation deleteTranslation(Key key) {
        LOG.debug("Delete translation with id:{}", key);

        Di18nTranslation di18nTranslation = i18nDao.findByPrimaryKey(key);

        if (null == di18nTranslation)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Localisation with key:%s not found during delete", key));

        i18nDao.delete(di18nTranslation);

        return di18nTranslation;
    }

    // Delete a translation for a specific parent and locale
    @Transactional
    @Idempotent
    public Di18nTranslation deleteTranslation(Key parentKey, String locale) {
        LOG.debug("Delete translation for locale:{} and parent:{}", locale, parentKey);

        Di18nTranslation di18nTranslation = this.getTranslation(parentKey, locale);

        if (null == di18nTranslation)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Locale:%s for parent:%s not found during delete", parentKey, locale));

        i18nDao.delete(di18nTranslation);

        return di18nTranslation;
    }


    // Delete all translations for a parent resource
    @Transactional
    @Idempotent
    public int deleteTranslationForParent(Key parentKey) {
        LOG.debug("Delete all translations for parent:{}", parentKey);

        Iterable<Di18nTranslation> di18nIterable = this.getTranslations(parentKey);

        if (null == di18nIterable)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Parent:%s not found during delete", parentKey));

        return i18nDao.delete(di18nIterable);
    }


    // Setters
    public void setI18nDao(Di18nTranslationDao i18nDao) {
        this.i18nDao = i18nDao;
    }
}
