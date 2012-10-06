package com.wadpam.pocketvenue.json;

import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.wadpam.open.json.JBaseObject;
import com.wadpam.open.json.JLocation;
import net.sf.mardao.api.Parent;

import java.util.Collection;

/**
 * Json representation of a place.
 * @author mattiaslevin
 */
public class JVenue extends JBaseObject {

    // Id is inherited from from parent class

    /** The brand id */
    private Long               parentId;

    /** The venue name */
    private String             name;

    /** The venue short description */
    private String             shortDescription;

    /** The venue description */
    private String             description;

    /** Opening hours */
    private Collection<String> openingHours;


    // Tag groups

    /** App specific tag group 1 */
    private Collection<Long>   tags;


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
    private JLocation location;


    // Contact details

    /** The venue phone number */
    private String              phoneNumber;

    /** The venue email number */
    private String              email;

    /** The venue web url */
    private String              webUrl;


    // Social

    /** The venue facebook url */
    private String              facebookUrl;

    /** The venue twitter url */
    private String              twitterUrl;


    // Images

    /** The venue logo url */
    private String               logoUrl;

    /** A list of venue related image urls */
    private Collection<String>   imageUrls;



    @Override
    protected String subString() {
        return String.format("name:%s parent:%s", name, parentId);
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public Collection<Long> getTags() {
        return tags;
    }

    public void setTags(Collection<Long> tags) {
        this.tags = tags;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public Collection<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(Collection<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Collection<String> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(Collection<String> openingHours) {
        this.openingHours = openingHours;
    }
}
