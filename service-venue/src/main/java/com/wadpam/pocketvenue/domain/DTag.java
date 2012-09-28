package com.wadpam.pocketvenue.domain;

import com.google.appengine.api.datastore.Key;
import net.sf.mardao.api.Parent;
import net.sf.mardao.api.domain.AEDLongEntity;
import net.sf.mardao.core.domain.AbstractLongEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Represent and hierarchical tag
 * @author mattiaslevin
 */
@Entity
public class DTag extends AbstractLongEntity {

    /** The parent tag id */
    @Parent(kind = "DTag")
    private Key                parentKey;

    /** The type of the tags, e.g. "location", "category" */
    @Basic
    private String             type;

    /** The tag name */
    @Basic
    private String             name;


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

    public Key getParentKey() {
        return parentKey;
    }

    public void setParentKey(Key parentKey) {
        this.parentKey = parentKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
