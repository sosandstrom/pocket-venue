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
        return jCursorPage.getCursor();
    }

    public void setCursor(String cursor) {
        this.jCursorPage.setCursor(cursor);
    }

    public Collection<JVenue> getItems() {
        return jCursorPage.getItems();
    }

    public void setItems(Collection<JVenue> items) {
        this.jCursorPage.setItems(items);
    }

    public Long getPageSize() {
        return jCursorPage.getPageSize();
    }

    public void setPageSize(Long pageSize) {
        this.jCursorPage.setPageSize(pageSize);
    }
}
