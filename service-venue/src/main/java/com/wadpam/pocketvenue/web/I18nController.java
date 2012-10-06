package com.wadpam.pocketvenue.web;

import com.google.appengine.api.datastore.Key;
import com.wadpam.docrest.domain.RestCode;
import com.wadpam.docrest.domain.RestReturn;
import com.wadpam.pocketvenue.domain.Di18nTranslation;
import com.wadpam.pocketvenue.json.Ji18nTranslation;
import com.wadpam.pocketvenue.service.TranslationService;
import com.wadpam.server.exceptions.BadRequestException;
import com.wadpam.server.exceptions.NotFoundException;
import com.wadpam.server.exceptions.RestException;
import com.wadpam.server.exceptions.ServerErrorException;
import com.wadpam.server.web.AbstractRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

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
    static final Logger LOG = LoggerFactory.getLogger(I18nController.class);

    static final int ERROR_CODE_NOT_FOUND = 30000;
    static final int ERROR_CODE_BAD_REQUEST = 30100;
    static final int ERROR_CODE_SEVER_ERROR = 30200;

    static final Converter CONVERTER = new Converter();

    private TranslationService translationService;
    private Map<String, I18nParentKeyFactory> keyFactoryMap;

    /**
     * Add a localized translation.
     * @param parentId the parent resource being translated
     * @param type the type of the parent
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
                                       @RequestParam(required = true) Object parentId,
                                       @RequestParam(required = true) String type,
                                       @RequestParam(required = true) String locale,
                                       @RequestParam(required = false) String string,
                                       @RequestParam(required = false) String imageUrl,
                                       @RequestParam(required = false) String linkUrl) {

        Key parentKey = createParentKey(parentId, type);
        final Di18nTranslation body = translationService.addTranslation(parentKey, locale, string, imageUrl, linkUrl);

        if (null == body)
            throw new ServerErrorException(ERROR_CODE_SEVER_ERROR + 1, String.format("Failed to create new translation for locale:%s", locale), null, "Create new translation failed");

        return new RedirectView(request.getRequestURI() + "?parentId=" + body.getParentKey().getId() + "&locale=" + body.getLocale());
    }

    // Create a parent key based on id and type
    private Key createParentKey(Object parentId, String type) {

        I18nParentKeyFactory keyFactory = keyFactoryMap.get(type);

        if (null == keyFactory)
            throw new BadRequestException(ERROR_CODE_BAD_REQUEST + 1, String.format("Bad requests. Unsupported parent type:%s", type), null, "Bad request");

        return keyFactory.createKey(parentId);
    }

    /**
     * Get a localized translation for a specific local and parent.
     * @param parentId the parent resource being translated
     * @param type the type of the parent
     * @param locale the locale
     * @return the translation
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation found"),
            @RestCode(code=404, message="NOK", description="Translations not found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = "locale")
    public ResponseEntity<Ji18nTranslation> getTranslation(HttpServletRequest request,
                                            @RequestParam(required = true) Object parentId,
                                            @RequestParam(required = true) String type,
                                            @RequestParam(required = true) String locale) {

        Key parentKey = createParentKey(parentId, type);
        final Di18nTranslation body = translationService.getTranslation(parentKey, locale);

        if (null == body)
            throw new NotFoundException(ERROR_CODE_NOT_FOUND + 1, String.format("Locale:%s for parent:%s not found", parentKey, locale), null, "Translation not found");

        return new ResponseEntity<Ji18nTranslation>(CONVERTER.convert(body), HttpStatus.OK);
    }

    /**
     * Get all localized translation for a specific parent.
     * @param parentId the parent resource being translated
     * @param type the type of the parent
     * @return a list of translations
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = "parentId")
    public ResponseEntity<Collection<Ji18nTranslation>> getTranslationsForParent(HttpServletRequest request,
                                                                                 @RequestParam(required = true) Object parentId,
                                                                                 @RequestParam(required = true) String type) {

        Key parentKey = createParentKey(parentId, type);
        final Iterable<Di18nTranslation> di18nTranslations = translationService.getTranslations(parentKey);

        return new ResponseEntity<Collection<Ji18nTranslation>>((Collection<Ji18nTranslation>)CONVERTER.convert(di18nTranslations), HttpStatus.OK);
    }


    /**
     * Get all localized translation for a list fo parents.
     * @param parentIds a lite of parent resources being translated
     * @param type the type of the parent
     * @return a map of translations
     */
    @RestReturn(value= Map.class, entity=Map.class, code={
            @RestCode(code=200, message="OK", description="Translation found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = "parentIds")
    public ResponseEntity<Map<String, Collection<Ji18nTranslation>>> getTranslationsForParents(HttpServletRequest request,
                                                                                            @RequestParam(required = true) Object[] parentIds,
                                                                                            @RequestParam(required = true) String type) {

        Collection<Key> parentKeys = new ArrayList<Key>(parentIds.length);
        for (Object parentId : parentIds)
            parentKeys.add(createParentKey(parentId, type));
        final Map<Key, Iterable<Di18nTranslation>> di18nTranslations = translationService.getTranslations(parentKeys);

        // Build response json
        Map<String, Collection<Ji18nTranslation>> result = new HashMap<String, Collection<Ji18nTranslation>>();
        for (Map.Entry<Key, Iterable<Di18nTranslation>> entry : di18nTranslations.entrySet())
            result.put(entry.getKey().toString(), (Collection<Ji18nTranslation>)CONVERTER.convert(entry.getValue()));

        return new ResponseEntity<Map<String, Collection<Ji18nTranslation>>>(result, HttpStatus.OK);
    }


    /**
     * Get localized translation in a specific locale for a list fo parents.
     * @param parentIds a lite of parent resources being translated
     * @param type the type of the parent
     * @param locale the locale
     * @return a list of translations
     */
    @RestReturn(value= Di18nTranslation.class, entity=Di18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation found")
    })
    @RequestMapping(value="", method= RequestMethod.GET, params = {"parentIds", "locale"})
    public ResponseEntity<Map<String, Ji18nTranslation>> getTranslationsForLocaleForParents(HttpServletRequest request,
                                                                                               @RequestParam(required = true) Object[] parentIds,
                                                                                               @RequestParam(required = true) String type,
                                                                                               @RequestParam(required = true) String locale) {

        Collection<Key> parentKeys = new ArrayList<Key>(parentIds.length);
        for (Object parentId : parentIds)
            parentKeys.add(createParentKey(parentId, type));
        final Map<Key, Di18nTranslation> di18nTranslations = translationService.getTranslations(parentKeys, locale);

        // Build response json
        Map<String, Ji18nTranslation> result = new HashMap<String, Ji18nTranslation>();
        for (Map.Entry<Key, Di18nTranslation> entry : di18nTranslations.entrySet())
            result.put(entry.getKey().toString(), CONVERTER.convert(entry.getValue()));

        return new ResponseEntity<Map<String, Ji18nTranslation>>(result, HttpStatus.OK);
    }

    /**
     * Delete a localized translation for a specific local and parent
     * @param parentId the parent resource being translated
     * @param type the type of the parent
     * @param locale the locale
     * @return the translation
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation deleted"),
            @RestCode(code=404, message="NOK", description="Translations not found")
    })
    @RequestMapping(value="", method= RequestMethod.DELETE, params = "locale")
    public ResponseEntity<Ji18nTranslation> deleteTranslation(HttpServletRequest request,
                                                           @RequestParam(required = true) Object parentId,
                                                           @RequestParam(required = true) String type,
                                                           @RequestParam(required = true) String locale) {

        Key parentKey = createParentKey(parentId, type);
        final Di18nTranslation body = translationService.deleteTranslation(parentKey, locale);

        return new ResponseEntity<Ji18nTranslation>(HttpStatus.OK);
    }


    /**
     * Delete all localized translation for a specific parent
     * @param parentId the parent resource being deleted
     * @param type the type of the parent
     * @return a list of translations
     */
    @RestReturn(value= Ji18nTranslation.class, entity=Ji18nTranslation.class, code={
            @RestCode(code=200, message="OK", description="Translation deleted"),
            @RestCode(code=404, message="NOK", description="Translations not found")
    })
    @RequestMapping(value="", method= RequestMethod.DELETE, params = "parentId")
    public ResponseEntity<Collection<Ji18nTranslation>> deleteTranslationsForParent(HttpServletRequest request,
                                                                                    @RequestParam(required = true) Long parentId,
                                                                                    @RequestParam(required = true) String type) {

        Key parentKey = createParentKey(parentId, type);
        final int result = translationService.deleteTranslations(parentKey);

        return new ResponseEntity<Collection<Ji18nTranslation>>(HttpStatus.OK);
    }


    // Setters
    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void setKeyFactoryMap(Map<String, I18nParentKeyFactory> keyFactoryMap) {
        this.keyFactoryMap = keyFactoryMap;
    }
}
