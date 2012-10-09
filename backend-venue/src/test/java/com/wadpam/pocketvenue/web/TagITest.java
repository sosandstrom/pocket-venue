package com.wadpam.pocketvenue.web;

import com.wadpam.pocketvenue.json.JTag;
import com.wadpam.server.json.JRestError;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration tests for tags.
 * @author mattiaslevin
 */
public class TagITest extends AbstractITest {
    static final Logger LOG = LoggerFactory.getLogger(TagITest.class);

    @Override
    protected String getBaseUrl() {
        return "http://localhost:8234/api/test/";
    }

    @Test
    public void createTag() throws MalformedURLException {

        // Create new tag
        final MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag1");
        map.set("type", "TypeA");

        ResponseEntity<JTag> entity = postAndFollowRedirect(BASE_URL + "tag", map, JTag.class);
        assertTrue("Name", entity.getBody().getName().equals("Tag1"));
        assertTrue("Type", entity.getBody().getType().equals("TypeA"));
    }

    @Test
    public void updateTag() throws MalformedURLException {

        // Create new tag
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag2");
        map.set("type", "TypeA");
        ResponseEntity<JTag> entity = postAndFollowRedirect(BASE_URL + "tag", map, JTag.class);
        assertTrue("Name", entity.getBody().getName().equals("Tag2"));
        assertTrue("Type", entity.getBody().getType().equals("TypeA"));

        assertNotNull("Tag id", entity.getBody().getId());
        String urlString = BASE_URL + "tag/" + entity.getBody().getId();

        // Update tag
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag2 updated");
        map.set("type", "TypeA");

        entity = postAndFollowRedirect(urlString, map, JTag.class);
        assertTrue("Name", entity.getBody().getName().equals("Tag2 updated"));
        assertTrue("Type", entity.getBody().getType().equals("TypeA"));
    }

    @Test
    public void deleteTag() throws MalformedURLException {

        // Create new tag
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag3");
        map.set("type", "TypeA");

        ResponseEntity<JTag> entity = postAndFollowRedirect(BASE_URL + "tag", map, JTag.class);
        assertTrue("Name", entity.getBody().getName().equals("Tag3"));
        assertTrue("Type", entity.getBody().getType().equals("TypeA"));

        // Delete tag
        String urlString = BASE_URL + "tag/" + entity.getBody().getId();
        template.delete(urlString);

        // Check that it is deleted
        ResponseEntity<JRestError> restError = template.getForEntity(urlString, JRestError.class);
        assertEquals("Http response 404", HttpStatus.NOT_FOUND, restError.getStatusCode());
    }

    @Test
    public void getTagsOfType() throws MalformedURLException {

        // Create new tag
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag4 level 1");
        map.set("type", "TypeB");
        ResponseEntity<JTag> entity = postAndFollowRedirect(BASE_URL + "tag", map, JTag.class);
        assertTrue("Name", entity.getBody().getName().equals("Tag4 level 1"));

        String parentId = entity.getBody().getId();
        assertNotNull(parentId);

        // Create new tag
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag5 level 2");
        map.set("type", "TypeB");
        map.set("parentId", parentId);
        entity = template.postForEntity(BASE_URL + "tag", map, JTag.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Create new tag
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag6 level 2");
        map.set("type", "TypeB");
        map.set("parentId", parentId);
        entity = template.postForEntity(BASE_URL + "tag", map, JTag.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Create new tag
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag7 level 2");
        map.set("type", "TypeB");
        map.set("parentId", parentId);
        entity = postAndFollowRedirect(BASE_URL + "tag", map, JTag.class);
        assertTrue("Name", entity.getBody().getName().equals("Tag7 level 2"));

        parentId = entity.getBody().getId();
        assertNotNull(parentId);

        // Create new tag
        map = new LinkedMultiValueMap<String, Object>();
        map.set("name", "Tag8 level 3");
        map.set("type", "TypeB");
        map.set("parentId", parentId);
        entity = template.postForEntity(BASE_URL + "tag", map, JTag.class);
        assertEquals("Http response 302", HttpStatus.FOUND, entity.getStatusCode());

        // Get hierarchy
        entity = template.getForEntity(BASE_URL + "tag/type/TypeB", JTag.class);
        JTag rootTag = entity.getBody();
        assertTrue("Name", rootTag.getName().equals("Tag4 level 1"));
        assertTrue("Type", rootTag.getType().equals("TypeB"));
        assertTrue("Number level 2 tags", rootTag.getChildren().size() == 3);

        for (JTag child : rootTag.getChildren()) {
            if (child.getName().equals("Tag6 level 2"))
                assertTrue("One child", child.getChildren().size() == 1);
            else
                assertNull("No children", child.getChildren());
        }
    }
}
