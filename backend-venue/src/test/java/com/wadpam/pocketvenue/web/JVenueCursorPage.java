package com.wadpam.pocketvenue.web;

import com.wadpam.open.json.JCursorPage;
import com.wadpam.pocketvenue.json.JVenue;

import java.util.Collection;

/**
 * Wrapper class for JCursorPage.
 * RestTemplate does not work with generics
 * @author mattiaslevin
 */
public class JVenueCursorPage {

    private JCursorPage<JVenue> jCursorPage = new JCursorPage<JVenue>();


    public String getCursor() {
        return (String)jCursorPage.getCursorKey();
    }

    public void setCursor(String cursor) {
        this.jCursorPage.setCursorKey(cursor);
    }

    public Collection<JVenue> getItems() {
        return jCursorPage.getItems();
    }

    public void setItems(Collection<JVenue> items) {
        this.jCursorPage.setItems(items);
    }

    public Long getPageSize() {
        return (long)jCursorPage.getPageSize();
    }

    public void setPageSize(Long pageSize) {
        this.jCursorPage.setPageSize(pageSize.intValue());
    }
}
