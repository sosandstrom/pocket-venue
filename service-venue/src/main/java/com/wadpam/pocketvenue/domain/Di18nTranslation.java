package com.wadpam.pocketvenue.domain;

import com.google.appengine.api.datastore.Link;
import net.sf.mardao.core.Parent;
import net.sf.mardao.core.domain.AbstractCreatedUpdatedEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Handle localization of strings and other resources.
 * @mattiaslevin
 */
@Entity
public class Di18nTranslation extends AbstractCreatedUpdatedEntity {

    /** The locale of the localized entity */
    @Id
    private String          locale;

    /** The parent key this localization belongs to. E.g. a tag or category */
    @Parent(kind = "Any")
    private Object          parent;

    /** Localized string value */
    @Basic
    private String          localizedString;

    /** Localized image */
    @Basic
    private Link            localizedImage;

    /** Localized url */
    @Basic
    private Link            localizedUrl;


    // Setters and getters
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Link getLocalizedImage() {
        return localizedImage;
    }

    public void setLocalizedImage(Link localizedImage) {
        this.localizedImage = localizedImage;
    }

    public String getLocalizedString() {
        return localizedString;
    }

    public void setLocalizedString(String localizedString) {
        this.localizedString = localizedString;
    }

    public Link getLocalizedUrl() {
        return localizedUrl;
    }

    public void setLocalizedUrl(Link localizedUrl) {
        this.localizedUrl = localizedUrl;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }
}
