package com.wadpam.pocketvenue.domain;

import com.google.appengine.api.datastore.Link;
import net.sf.mardao.api.Parent;
import net.sf.mardao.core.domain.AbstractLongEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;

/**
 * Represent and hierarchical tag
 * @author mattiaslevin
 */
@Entity
public class DTag extends AbstractLongEntity {

    /** The parent tag id */
    @Parent(kind = "DTag")
    private Object             parent;

    /** The type of the tags, e.g. "location", "category" */
    @Basic
    private String             type;

    /** The tag name */
    @Basic
    private String             name;

    /** An optional image url */
    @Basic
    private Link               imageUrl;


    @Override
    public String toString() {
        return String.format("{id:%d, name:%s}", getId(), name);
    }


    // Setters and getters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Link getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Link imageUrl) {
        this.imageUrl = imageUrl;
    }
}
