package com.wadpam.pocketvenue.json;

import com.wadpam.open.json.JBaseObject;

/**
 * Json representation of a i18n translation.
 * @author mattiaslevin
 */
public class Ji18nTranslation extends JBaseObject {

    /** The locale of the localized entity */
    private String            locale;

    /** The parent key this localization belongs to. E.g. a tag or category */
    private String            parent;

    /** Localized string value */
    private String            localizedString;

    /** Localized image */
    private String            localizedImage;

    /** Localized url */
    private String            localizedUrl;


    @Override
    protected String subString() {
        return String.format("locale:%s string:%s", locale, localizedString);
    }


    // Setters and getters
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocalizedImage() {
        return localizedImage;
    }

    public void setLocalizedImage(String localizedImage) {
        this.localizedImage = localizedImage;
    }

    public String getLocalizedString() {
        return localizedString;
    }

    public void setLocalizedString(String localizedString) {
        this.localizedString = localizedString;
    }

    public String getLocalizedUrl() {
        return localizedUrl;
    }

    public void setLocalizedUrl(String localizedUrl) {
        this.localizedUrl = localizedUrl;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
