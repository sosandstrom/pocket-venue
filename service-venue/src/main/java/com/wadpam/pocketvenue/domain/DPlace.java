package com.wadpam.pocketvenue.domain;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import net.sf.mardao.core.Parent;
import net.sf.mardao.core.domain.AbstractLongEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import java.util.Collection;

/**
 * A place domain object.
 * @author mattiaslevin
 */
@Entity
public class DPlace extends AbstractLongEntity {

    /** The parent place id */
    @Parent(kind = "DPlace")
    private Object             parent;

    /** The place name */
    @Basic
    private String             name;

    /** The place short description */
    @Basic
    private String             shortDescription;

    /** The place description */
    @Basic
    private String             description;

    /** Opening hours */
    @Basic
    private Collection<String> openingHours;


    // Tag groups

    /** Tags assigned to this place */
    @Basic
    private Collection<String>   tags;


    // Address

    /** The place street */
    @Basic
    private String             street;

    /** The place city area/district */
    @Basic
    private String             cityArea;

    /** The place city */
    @Basic
    private String             city;

    /** The place county */
    @Basic
    private String             county;

    /** The place postal code */
    @Basic
    private String             postalCode;

    /** The place country */
    @Basic
    private String             country;

    /** The place position in latitude and longitude */
    @Basic
    private GeoPt              location;


    // Contact details

    /** The place phone number */
    @Basic
    private String              phoneNumber;

    /** The place email number */
    @Basic
    private Email               email;

    /** The place web url */
    @Basic
    private Link                webUrl;


    // Social

    /** The place facebook url */
    @Basic
    private Link                facebookUrl;

    /** The place twitter url */
    @Basic
    private Link                twitterUrl;


    // Images

    /** The place logo url */
    @Basic
    private Link                logoUrl;

    /** A list of place related image urls */
    @Basic
    private Collection<Link>    imageUrls;


    // TODO: What other attributes should exist


    @Override
    public String toString() {
        return String.format("{id:%d, name:%s}", getId(), name);
    }


    // Setter and getters
    public float getLatitude() {
        return null != location ? location.getLatitude() : -200;
    }

    public float getLongitude() {
        return null != location ? location.getLongitude() : -200;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCityArea() {
        return cityArea;
    }

    public void setCityArea(String cityArea) {
        this.cityArea = cityArea;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Link getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(Link webUrl) {
        this.webUrl = webUrl;
    }

    public Link getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(Link facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public Link getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(Link twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public Link getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(Link logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Collection<Link> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(Collection<Link> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Collection<String> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(Collection<String> openingHours) {
        this.openingHours = openingHours;
    }
}
