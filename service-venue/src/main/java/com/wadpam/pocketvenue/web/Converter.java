package com.wadpam.pocketvenue.web;


import com.google.appengine.api.datastore.Link;
import com.wadpam.open.json.JBaseObject;
import com.wadpam.open.web.BaseConverter;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.pocketvenue.domain.Di18nTranslation;
import com.wadpam.pocketvenue.json.JTag;
import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.pocketvenue.json.Ji18nTranslation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class implement various methods for converting from domain object to JSON objects
 * @author mattiaslevin
 */
public class Converter extends BaseConverter {
    static final Logger LOG = LoggerFactory.getLogger(Converter.class);

    // Convert venues
    public JVenue convert(DPlace from) {

        if (null == from) {
            return null;
        }

        JVenue to = new JVenue();

        to.setId(Long.toString(from.getId()));
        to.setName(from.getName());
        if (null != from.getParentKey())
            to.setParentId(from.getParentKey().getId());
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
            to.setLocation(convert(from.getLocation()));
        to.setPhoneNumber(from.getPhoneNumber());
        if (null != from.getEmail())
            to.setEmail(from.getEmail().getEmail());
        if (null != from.getWebUrl())
            to.setWebUrl(from.getWebUrl().getValue());
        if (null != from.getFacebookUrl())
            to.setFacebookUrl(from.getFacebookUrl().getValue());
        if (null != from.getTwitterUrl())
            to.setTwitterUrl(from.getTwitterUrl().getValue());
        if (null != from.getLogoUrl())
            to.setLogoUrl(from.getLogoUrl().getValue());
        if (null != from.getImageUrls()) {
            Collection<String> links = new ArrayList<String>(from.getImageUrls().size());
            for (Link link : from.getImageUrls())
                links.add(link.getValue());
            to.setImageUrls(links);
        }

        return to;
    }

    // Convert tags
    public static JTag convert(DTag from) {

        if (null == from) {
            return null;
        }

        JTag to = new JTag();
        to.setId(Long.toString(from.getId()));
        to.setType(from.getType());
        if (null != from.getParentKey())
            to.setParentId(from.getParentKey().getId());
        to.setName(from.getName());
        to.setImageUrl(from.getImageUrl().getValue());

        return to;
    }

    // Convert translations
    public static Ji18nTranslation convert(Di18nTranslation from) {

        if (null == from) {
            return null;
        }

        Ji18nTranslation to = new Ji18nTranslation();
        to.setParentKey(toString(from.getParentKey().getId()));
        to.setLocale(from.getLocale());
        to.setLocalizedString(from.getLocalizedString());
        to.setLocalizedImage(from.getLocalizedImage().getValue());
        to.setLocalizedUrl(from.getLocalizedImage().getValue());

        return to;
    }

    @Override
    public JBaseObject convertBase(Object from) {
        if (null == from) {
            return null;
        }

        JBaseObject to;
        if (from instanceof DPlace) {
            to = convert((DPlace) from);
        }
        else if (from instanceof DTag) {
            to = convert((DTag) from);
        }
        else if (from instanceof Di18nTranslation) {
            to = convert((Di18nTranslation) from);
        }
        else {
            throw new UnsupportedOperationException(String.format("No converter for:%s" + from.getClass().getSimpleName()));
        }

        return to;
    }
}
