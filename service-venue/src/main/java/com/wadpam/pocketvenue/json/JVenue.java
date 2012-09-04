package com.wadpam.pocketvenue.json;

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.wadpam.open.json.JBaseObject;

import java.util.Collection;

/**
 * Json representation of a place.
 * @author mattiaslevin
 */
public class JVenue extends JBaseObject {

    // Id is inherited from from parent class

    /** The brand id */
    private String             parentId;

    /** The hierarchy name */
    private String             hierarchy;

    /** The venue name */
    private String             name;

    /** The venue short description */
    private String             shortDescription;

    /** The venue description */
    private String             description;

    /** Opening hours */
    // TODO add opening hours


    // Tag groups

    /** App specific tag group 1 */
    private Collection<Long>   appTags1;

    /** App specific tag group 2 */
    private Collection<Long>   appTags2;


    // Address

    /** The venue street */
    private String             street;

    /** The venue city area/district */
    private String             cityArea;

    /** The venue city */
    private String             city;

    /** The venue county */
    private String             county;

    /** The venue postal code */
    private String             postalCode;

    /** The venue country */
    private String             country;

    /** The venue position in latitude and longitude */
    private JLocation           location;


    // Contact details

    /** The venue phone number */
    private String              phoneNumber;

    /** The venue email number */
    private Email               email;

    /** The venue web url */
    private Link                webUrl;


    // Social

    /** The venue facebook url */
    private Link                facebookUrl;

    /** The venue twitter url */
    private Link                twitterUrl;


    // Images

    /** The venue logo url */
    private Link                logoUrl;

    /** A list of venue related image urls */
    private Collection<Link>    imageUrls;



    @Override
    protected String subString() {
        return String.format("name:%s parent:%s hierarchy:%s", name, parentId, hierarchy);
    }

    // Setters and getters
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
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

    public JLocation getLocation() {
        return location;
    }

    public void setLocation(JLocation location) {
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
