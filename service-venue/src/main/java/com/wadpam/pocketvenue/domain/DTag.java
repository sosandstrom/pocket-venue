package com.wadpam.pocketvenue.domain;

import net.sf.mardao.api.domain.AEDLongEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Collection;

/**
 * Represent and hierarchical tag
 * @author mattiaslevin
 */
@Entity
public class DTag extends AEDLongEntity  {


    /** Generated primary key */
    @Id
    private Long               id;

    /** The type of the tags, e.g. "location", "category" */
    @Basic
    private String             type;

    /** The parent tag id */
    @Basic
    private Long               parentId;

    /** The tag name */
    @Basic
    private String             name;


    @Override
    public Long getSimpleKey() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("{id:%d, name:%s}", id, name);
    }


    // Setters and getters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
