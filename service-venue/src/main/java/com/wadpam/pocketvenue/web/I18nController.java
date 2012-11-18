package com.wadpam.pocketvenue.web;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.wadpam.docrest.domain.RestCode;
import com.wadpam.docrest.domain.RestReturn;
import com.wadpam.pocketvenue.domain.Di18nTranslation;
import com.wadpam.pocketvenue.json.JTag;
import com.wadpam.pocketvenue.json.Ji18nTranslation;
import com.wadpam.pocketvenue.service.TranslationService;
import com.wadpam.pocketvenue.service.VenueService;
import com.wadpam.server.exceptions.NotFoundException;
import com.wadpam.server.exceptions.ServerErrorException;
import com.wadpam.server.web.AbstractRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The i18n controller implements all REST methods related to translations.
 * There methods should preferably only be user by some kind of backoffice interface
 * to manage the translations.
 * Any app side integration should be in the Controller och Service accessed by the
 * app (the app should not access the REST interface directly).
 * @author mattiaslevin
 */
@RequestMapping(value="{domain}/i18n")
public class I18nController extends AbstractRestController {
    private static final Logger LOG = LoggerFactory.getLogger(I18nController.class);

    private static final int ERR_NOT_FOUND = VenueService.ERR_TRANSLATION_SERVICE + 100;
    private static final int ERR_BAD_REQUEST = VenueService.ERR_TRANSLATION_SERVICE + 102;
    private static final int ERR_SERVER_ERROR = VenueService.ERR_TRANSLATION_SERVICE + 103;

    private static final Converter CONVERTER = new Converter();

    private TranslationService translationService;

    /**
     * Add a localized translation.
     * @param parent the parent resource being translated
     * @param locale the locale
     * @param string localized string value
     * @param imageUrl localized image
     * @param linkUrl localized url
     * @return redirect to the newly created translation
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=302, message="OK", description="Redirect to newly created translation")
    })
    @RequestMapping(value="", method= RequestMethod.POST)
    public RedirectView addTranslation(HttpServletRequest request,
                                       UriComponentsBuilder uriBuilder,
                                       @PathVariable String domain,
                                       @RequestParam(required = true) String parent,
                                       @RequestParam(required = true) String locale,
                                       @RequestParam(required = false) String string,
                                       @RequestParam(required = false) String imageUrl,
                                       @RequestParam(required = false) String linkUrl) {

        Key parentKey = KeyFactory.stringToKey(parent);
        final Di18nTranslation body = translationService.addTranslation(parentKey, locale, string, imageUrl, linkUrl);

        if (null == body)
            throw new ServerErrorException(ERR_SERVER_ERROR, String.format("Failed to create new translation for locale:%s", locale));

        Ji18nTranslation ji18nTranslation = CONVERTER.convert(body);
        return new RedirectView(uriBuilder.path("/{domain}/i18n/{id}")
                .buildAndExpand(domain, ji18nTranslation.getId()).toUriString());
    }

    /**
     * Get a localized translation by id
     * @param id the id of the translation
     * @return the translation
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation found"),
            @RestCode(code=404, message="NOK", description="Translations not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.GET)
    public ResponseEntity<Ji18nTranslation> getTranslation(HttpServletRequest request,
                                                           @PathVariable String id) {

        Key key = KeyFactory.stringToKey(id);
        final Di18nTranslation body = translationService.getTranslation(key);

        if (null == body)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("translation not found for id:%s", id));

        return new ResponseEntity<Ji18nTranslation>(CONVERTER.convert(body), HttpStatus.OK);
    }


    /**
     * Get a localized translation for a specific locale and parent.
     * @param parent the parent resource being translated
     * @param locale the locale
     * @return the translation
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation found"),
            @RestCode(code=404, message="NOK", description="Translations not found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = {"parent", "locale"})
    public ResponseEntity<Ji18nTranslation> getTranslation(HttpServletRequest request,
                                            @RequestParam(required = true) String parent,
                                            @RequestParam(required = true) String locale) {

        Key parentKey = KeyFactory.stringToKey(parent);
        final Di18nTranslation body = translationService.getTranslation(parentKey, locale);

        if (null == body)
            throw new NotFoundException(ERR_NOT_FOUND, String.format("Locale:%s for parent:%s not found", parentKey, locale));

        return new ResponseEntity<Ji18nTranslation>(CONVERTER.convert(body), HttpStatus.OK);
    }

    /**
     * Get all localized translation for a specific parent.
     * @param parent the parent resource being translated
     * @return a list of translations
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = "parent")
    public ResponseEntity<Collection<Ji18nTranslation>> getTranslationsForParent(
            HttpServletRequest request,
            @RequestParam(required = true) String parent) {

        Key parentKey = KeyFactory.stringToKey(parent);
        final Iterable<Di18nTranslation> di18nTranslations = translationService.getTranslations(parentKey);

        return new ResponseEntity<Collection<Ji18nTranslation>>((Collection<Ji18nTranslation>)CONVERTER.convert(di18nTranslations), HttpStatus.OK);
    }


    /**
     * Get all localized translation for a list fo parents.
     * @param parents a list of parents
     * @return a map of translations
     */
    @RestReturn(value= Map.class, entity=Map.class, code={
            @RestCode(code=200, message="OK", description="Translation found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = "parents")
    public ResponseEntity<Map<String, Collection<Ji18nTranslation>>> getTranslationsForParents(
            HttpServletRequest request,
            @RequestParam(required = true) String[] parents) {

        Collection<Key> parentKeys = new ArrayList<Key>(parents.length);
        for (String parentId : parents)
            parentKeys.add(KeyFactory.stringToKey(parentId));
        final Map<Key, Iterable<Di18nTranslation>> di18nTranslations = translationService.getTranslations(parentKeys);

        // Build response json
        Map<String, Collection<Ji18nTranslation>> result = new HashMap<String, Collection<Ji18nTranslation>>();
        for (Map.Entry<Key, Iterable<Di18nTranslation>> entry : di18nTranslations.entrySet())
            result.put(KeyFactory.keyToString(entry.getKey()), (Collection<Ji18nTranslation>)CONVERTER.convert(entry.getValue()));

        return new ResponseEntity<Map<String, Collection<Ji18nTranslation>>>(result, HttpStatus.OK);
    }


    /**
     * Get localized translation in a specific locale for a list fo parents.
     * @param parents a list
     * @param locale the locale
     * @return a list of translations
     */
    @RestReturn(value= Di18nTranslation.class, entity=Di18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = {"parents", "locale"})
    public ResponseEntity<Map<String, Ji18nTranslation>> getTranslationsForLocaleForParents(
            HttpServletRequest request,
            @RequestParam(required = true) String[] parents,
            @RequestParam(required = true) String locale) {

        Collection<Key> parentKeys = new ArrayList<Key>(parents.length);
        for (String parentId : parents)
            parentKeys.add(KeyFactory.stringToKey(parentId));
        final Map<Key, Di18nTranslation> di18nTranslations = translationService.getTranslations(parentKeys, locale);

        // Build response json
        Map<String, Ji18nTranslation> result = new HashMap<String, Ji18nTranslation>();
        for (Map.Entry<Key, Di18nTranslation> entry : di18nTranslations.entrySet())
            result.put(KeyFactory.keyToString(entry.getKey()), CONVERTER.convert(entry.getValue()));

        return new ResponseEntity<Map<String, Ji18nTranslation>>(result, HttpStatus.OK);
    }

    /**
     * Delete a localized translation by id
     * @param id the id of the translation
     * @return the translation
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation deleted"),
            @RestCode(code=404, message="NOK", description="Translation not found")
    })
    @RequestMapping(value="{id}", method= RequestMethod.DELETE)
    public ResponseEntity<Ji18nTranslation> deleteTranslation(HttpServletRequest request,
                                                              @PathVariable String id) {

        Key key = KeyFactory.stringToKey(id);
        final Di18nTranslation body = translationService.deleteTranslation(key);

        return new ResponseEntity<Ji18nTranslation>(HttpStatus.OK);
    }

    /**
     * Delete a localized translation for a specific local and parent
     * @param parent the parent resource being translated
     * @param locale the locale
     * @return the translation
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation deleted"),
            @RestCode(code=404, message="NOK", description="Translation not found")
    })
    @RequestMapping(value="", method= RequestMethod.DELETE, params = "locale")
    public ResponseEntity<Ji18nTranslation> deleteTranslation(HttpServletRequest request,
                                                           @RequestParam(required = true) String parent,
                                                           @RequestParam(required = true) String locale) {

        Key parentKey = KeyFactory.stringToKey(parent);
        final Di18nTranslation body = translationService.deleteTranslation(parentKey, locale);

        return new ResponseEntity<Ji18nTranslation>(HttpStatus.OK);
    }


    /**
     * Delete all localized translation for a specific parent
     * @param parent the parent resource being deleted
     * @return a list of translations
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation deleted"),
            @RestCode(code=404, message="NOK", description="Translations not found")
    })
    @RequestMapping(value="", method= RequestMethod.DELETE, params = "parent")
    public ResponseEntity<Collection<Ji18nTranslation>> deleteTranslationsForParent(
            HttpServletRequest request,
            @RequestParam(required = true) String parent) {

        Key parentKey = KeyFactory.stringToKey(parent);
        final int result = translationService.deleteTranslationForParent(parentKey);

        return new ResponseEntity<Collection<Ji18nTranslation>>(HttpStatus.OK);
    }


    // Setters
    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

}
