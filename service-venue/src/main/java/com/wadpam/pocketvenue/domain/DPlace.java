package com.wadpam.pocketvenue.domain;

import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import net.sf.mardao.api.domain.AEDLongEntity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Collection;

/**
 * A place domain object.
 * @author mattiaslevin
 */
@Entity
public class DPlace extends AEDLongEntity {

    /** Generated primary key */
    @Id
    private Long               id;

    /** The parent place id */
    @Basic
    private Long               parentId;

    /** The hierarchy name */
    @Basic
    private String             hierarchy;

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
    // TODO add opening hours


    // Tag groups

    /** App specific tag group 1 */
    @Basic
    private Collection<Long>   appTags1;

    /** App specific tag group 2 */
    @Basic
    private Collection<Long>   appTags2;


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
    public Long getSimpleKey() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("{id:%d, name:%s}", id, name);
    }


    // Setter and getters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
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

    public Collection<Long> getAppTags1() {
        return appTags1;
    }

    public void setAppTags1(Collection<Long> appTags1) {
        this.appTags1 = appTags1;
    }

    public Collection<Long> getAppTags2() {
        return appTags2;
    }

    public void setAppTags2(Collection<Long> appTags2) {
        this.appTags2 = appTags2;
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
}
