package com.wadpam.pocketvenue.web;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.wadpam.open.json.JBaseObject;
import com.wadpam.pocketvenue.domain.DPlace;
import com.wadpam.pocketvenue.domain.DTag;
import com.wadpam.pocketvenue.json.JLocation;
import com.wadpam.pocketvenue.json.JTag;
import com.wadpam.pocketvenue.json.JVenue;
import com.wadpam.pocketvenue.json.JVenuePage;
import net.sf.mardao.api.domain.AEDPrimaryKeyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * This class implement various methods for converting from domain object to JSON objects
 * @author mattiaslevin
 */
public class Converter {
    static final Logger LOG = LoggerFactory.getLogger(Converter.class);


    protected static JVenuePage convert(String cursor, int pageSize, Collection<DPlace> dPlaces) {

        JVenuePage jVenuePage = new JVenuePage();
        jVenuePage.setCursor(cursor);
        jVenuePage.setPageSize(pageSize);
        jVenuePage.setVenues((Collection<JVenue>)convert(dPlaces));

        return jVenuePage;
    }

    protected static JVenue convert(DPlace from) {

        if (null == from) {
            return null;
        }

        JVenue to = new JVenue();

        to.setId(Long.toString(from.getId()));
        to.setName(from.getName());
        if (null != from.getParentKey())
            to.setParentId(from.getParentKey().getId());
        to.setHierarchy(from.getHierarchy());
        to.setShortDescription(from.getShortDescription());
        to.setDescription(from.getDescription());
        to.setOpeningHours(from.getOpeningHours());
        to.setAppTags1(from.getAppTags1());
        to.setAppTags2(from.getAppTags2());
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

    protected static JTag convert(DTag from) {

        if (null == from) {
            return null;
        }

        JTag to = new JTag();
        to.setId(Long.toString(from.getId()));
        to.setType(from.getType());
        if (null != from.getParentKey())
            to.setParentId(from.getParentKey().getId());
        to.setName(from.getName());

        return to;
    }

    protected static JLocation convert(GeoPt from) {
        if (null == from) {
            return null;
        }

        JLocation to = new JLocation(from.getLatitude(), from.getLongitude());

        return to;
    }

    protected static <T extends AEDPrimaryKeyEntity> JBaseObject convert(T from) {
        if (null == from) {
            return null;
        }

        JBaseObject returnValue;
        if (from instanceof DPlace)
            returnValue = convert((DPlace)from);
        else if (from instanceof DTag)
            returnValue = convert((DTag)from);
        else
            throw new UnsupportedOperationException("No converter for " + from.getKind());

        return returnValue;
    }

    protected static <T extends AEDPrimaryKeyEntity> Collection<?> convert(Collection<T> from) {
        if (null == from) {
            return null;
        }

        final Collection<Object> to = new ArrayList<Object>(from.size());

        for(T wf : from) {
            to.add(convert(wf));
        }

        return to;
    }

    protected static <T extends AEDPrimaryKeyEntity> void convert(T from, JBaseObject to) {
        if (null == from || null == to) {
            return;
        }

        to.setId(null != from.getSimpleKey() ? from.getSimpleKey().toString() : null);
        to.setCreatedDate(toLong(from.getCreatedDate()));
        to.setUpdatedDate(toLong(from.getUpdatedDate()));
    }

    private static Long toLong(Key from) {
        if (null == from) {
            return null;
        }
        return from.getId();
    }

    private static Long toLong(Date from) {
        if (null == from) {
            return null;
        }
        return from.getTime();
    }

    private static Collection<Long> toLongs(Collection<String> from) {
        if (null == from) {
            return null;
        }

        final Collection<Long> to = new ArrayList<Long>();

        for(String s : from) {
            try {
                to.add(Long.parseLong(s));
            }
            catch (NumberFormatException sometimes) {
                LOG.warn("Trying to convert non-numeric id: {}", s);
            }
        }

        return to;
    }

    private static String toString(Key from) {
        if (null == from) {
            return null;
        }
        return Long.toString(from.getId());
    }

    private static Collection<String> toString(Collection<Long> from) {
        if (null == from) {
            return null;
        }

        final Collection<String> to = new ArrayList<String>(from.size());

        for(Long l : from) {
            to.add(l.toString());
        }

        return to;
    }

}
