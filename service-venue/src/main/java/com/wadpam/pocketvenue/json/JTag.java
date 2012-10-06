package com.wadpam.pocketvenue.json;

import com.google.appengine.api.datastore.Link;
import com.wadpam.open.json.JBaseObject;

import java.util.Collection;


/**
 * Json representation of a tag.
 * @author mattiaslevin
 */
public class JTag extends JBaseObject {

    // Id is inherited from from parent class

    /** The type of the tags, e.g. "location", "category" */
    private String             type;

    /** The parent tag id */
    private Long               parentId;

    /** The tag name */
    private String             name;

    /** Image url */
    private String             imageUrl;


    /** The tag name */
    private Collection<JTag>   children;

    @Override
    protected String subString() {
        return String.format("name:%s parent:%s", name, parentId);
    }

    // Setters and getters

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

    public Collection<JTag> getChildren() {
        return children;
    }

    public void setChildren(Collection<JTag> children) {
        this.children = children;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}