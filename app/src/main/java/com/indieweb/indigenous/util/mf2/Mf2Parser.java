package com.indieweb.indigenous.util.mf2;

import com.indieweb.indigenous.model.HCard;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parse a Microformats2 formatted HTML document.
 * @author kmahan
 *
 */
public class Mf2Parser {

    private ArrayList<HCard> hcards = new ArrayList<HCard>();

    /**
     * Constructor
     */
    public Mf2Parser() { }

    private URI findBaseUri(Document doc, URI baseUri) {
        Element base = doc.getElementsByTag("base").first();
        if (base != null && base.hasAttr("href")) {
            baseUri = baseUri.resolve(base.attr("href"));
        }
        // normalize URIs with missing path
        String path = baseUri.getPath();
        if (path == null || path.isEmpty())
        {
            try
            {
                baseUri = new URI(baseUri.getScheme(), baseUri.getAuthority(), "/", null, null);
            } catch (URISyntaxException e) {}
        }
        return baseUri;
    }

    /**
     * Parse an existing document for microformats2.
     * @param doc the Jsoup document to parse
     * @param baseUri the URI where the document exists, used for normalization
     * @return a well-defined JSON structure containing the parsed microformats2 data.
     */    
    public ArrayList<HCard> parse(Document doc, URI baseUri) {
        baseUri = findBaseUri(doc, baseUri);

        AtomicReference<JsonDict> dict = new AtomicReference<>(new JsonDict());
        JsonList items = dict.get().getOrCreateList("items");
        parseMicroformats(doc, baseUri, items);

        return hcards;
    }

    private void parseMicroformats(Element elem, URI baseUri, JsonList items) {
        if (hasRootClass(elem)) {
            JsonDict itemDict = parseMicroformat(elem, baseUri);
            items.add(itemDict);
        }
        else {
            for (Element child : elem.children()) {
                parseMicroformats(child, baseUri, items);
            }
        }
    }

    private JsonDict parseMicroformat(Element elem, URI baseUri) {
        JsonDict itemDict = new JsonDict();
        itemDict.put("type", getRootClasses(elem));
        JsonDict properties = itemDict.getOrCreateDict("properties");

        for (Element child : elem.children()) {
            parseProperties(child, baseUri, itemDict);
        }

        boolean saveCard = false;
        HCard card = new HCard();

        if (properties.containsKey("name")) {
            JsonList names = (JsonList) properties.get("name");
            if (names.size() > 0) {
                saveCard = true;
                card.setName(names.get(0).toString());
            }

        }
        else {
            String impliedName = parseImpliedName(elem);
            if (impliedName != null) {
                saveCard = true;
                card.setName(impliedName);
            }
        }

        if (properties.containsKey("url")) {
            JsonList urls = (JsonList) properties.get("url");
            if (urls.size() > 0) {
                saveCard = true;
                card.setUrl(urls.get(0).toString());
            }
        }
        else {
            String impliedUrl = parseImpliedUrl(elem, baseUri);
            if (impliedUrl != null) {
                saveCard = true;
                card.setUrl(impliedUrl);
            }
        }

        if (properties.containsKey("photo")) {
            JsonList avatars = (JsonList) properties.get("photo");
            if (avatars.size() > 0) {
                saveCard = true;
                card.setAvatar(avatars.get(0).toString());
            }
        }
        else {
            String impliedPhoto = parseImpliedPhoto(elem, baseUri);
            if (impliedPhoto != null) {
                saveCard = true;
                card.setAvatar(impliedPhoto);
            }
        }

        if (saveCard) {
            hcards.add(card);
        }

        return itemDict;
    }

    private Object parseChildValue(String className, JsonDict valueObj, Object value) {
        JsonDict properties = (JsonDict) valueObj.get("properties");
        //if it's a p-* property element, use the first p-name of the h-* child
        if (className.startsWith("p-") && properties.containsKey("name")) {
            JsonList names = (JsonList) properties.get("name");
            if (names.size() > 0)
                return names.get(0);
        }
        // else if it's an e-* property element, re-use its { } structure with existing value: inside.
        // not-sure: implement this or find out if its handled by default case below
        // else if it's a u-* property element and the h-* child has a u-url, use the first such u-url
        if (className.startsWith("u-") && properties.containsKey("url")) {
            JsonList urls = (JsonList) properties.get("url");
            if (urls.size() > 0)
                return urls.get(0);
        }
        //else use the parsed property value per p-*,u-*,dt-* parsing respectively
        return value;
    }

    private void parseProperties(Element elem, URI baseUri, JsonDict itemDict) {
        boolean isProperty = false, isMicroformat = false;

        JsonDict valueObj = null;
        if (hasRootClass(elem)) {
            valueObj = parseMicroformat(elem, baseUri);
            isMicroformat = true;
        }

        for (String className : elem.classNames()) {
            String propName = null;
            Object value = null;
            if (className.startsWith("p-")) {
                propName = className.substring(2);
                value = parseTextProperty(elem);
                isProperty = true;
            }
            else if (className.startsWith("u-")) {
                propName = className.substring(2);
                value = parseUrlProperty(elem, baseUri);
                isProperty = true;
            }

            if (propName != null) {
                if (isMicroformat && valueObj != null) {
                    valueObj.put("value", parseChildValue(className, valueObj, value));
                    value = valueObj;
                }
                itemDict.getDict("properties").getOrCreateList(propName).add(value);
            }
        }

        if (!isProperty && isMicroformat) {
            itemDict.getOrCreateList("children").add(valueObj);
        }

        if (!isMicroformat) {
            for (Element child : elem.children()) {
                parseProperties(child, baseUri, itemDict);
            }
        }
    }

    private String parseTextProperty(Element elem) {
        /// not-sure: value-class-pattern
        if ("abbr".equals(elem.tagName()) && elem.hasAttr("title")) {
            return elem.attr("title");
        }
        if (("data".equals(elem.tagName()) || "input".equals(elem.tagName())) && elem.hasAttr("value")) {
            return elem.attr("value");
        }
        if (("img".equals(elem.tagName()) || "area".equals(elem.tagName())) && elem.hasAttr("alt")) {
            return elem.attr("alt");
        }
        // not-sure: replace nested <img> with alt or src
        return elem.text().trim();
    }

    private String parseUrlProperty(Element elem, URI baseUri) {
        String url = null;
        if (("a".equals(elem.tagName()) || "area".equals(elem.tagName())) && elem.hasAttr("href")) {
            url = elem.attr("href");
        }
        if (("img".equals(elem.tagName()) || "audio".equals(elem.tagName())
                || "video".equals(elem.tagName()) || "source".equals(elem.tagName())) && elem.hasAttr("src")) {
            url = elem.attr("src");
        }
        if ("object".equals(elem.tagName()) && elem.hasAttr("data")) {
            url = elem.attr("data");
        }
        if (url != null) {
            return baseUri.resolve(url).toString();
        }
        // not-sure: value-class-pattern
        if ("abbr".equals(elem.tagName()) && elem.hasAttr("title")) {
            return elem.attr("title");
        }
        if (("data".equals(elem.tagName()) || "input".equals(elem.tagName())) && elem.hasAttr("value")) {
            return elem.attr("value");
        }
        return elem.text().trim();
    }

    private String parseImpliedPhoto(Element elem, URI baseUri) {
        String href = parseImpliedPhotoRelative(elem);
        if (href != null) {
            return baseUri.resolve(href).toString();
        }
        return null;
    }

    private String parseImpliedPhotoRelative(Element elem) {
        String[][] tagAttrs = {
                {"img", "src"},
                {"object", "data"},
        };

        for (String[] tagAttr : tagAttrs) {
            String tag = tagAttr[0], attr = tagAttr[1];
            if (tag.equals(elem.tagName()) && elem.hasAttr(attr)) {
                return elem.attr(attr);
            }
        }

        for (String[] tagAttr : tagAttrs) {
            String tag = tagAttr[0], attr = tagAttr[1];
            Elements children = filterByTag(elem.children(), tag);
            if (children.size() == 1) {
                Element child = children.first();
                if (!hasRootClass(child) && child.hasAttr(attr)) {
                    return child.attr(attr);
                }
            }
        }

        Elements children = elem.children();
        if (children.size() == 1) {
            Element child = children.first();
            for (String[] tagAttr : tagAttrs) {
                String tag = tagAttr[0], attr = tagAttr[1];
                Elements grandChildren = filterByTag(child.children(), tag);
                if (grandChildren.size() == 1) {
                    Element grandChild = grandChildren.first();
                    if (!hasRootClass(grandChild) && grandChild.hasAttr(attr)) {
                        return grandChild.attr(attr);
                    }
                }
            }
        }

        return null;
    }

    private String parseImpliedUrl(Element elem, URI baseUri) {
        String href = parseImpliedUrlRelative(elem);
        if (href != null) {
            return baseUri.resolve(href).toString();
        }
        return null;
    }

    private String parseImpliedUrlRelative(Element elem) {
        //     if a.h-x[href] or area.h-x[href] then use that [href] for url
        if (("a".equals(elem.tagName()) || "area".equals(elem.tagName()))
                && elem.hasAttr("href")) {
            return elem.attr("href");
        }
        //else if .h-x>a[href]:only-of-type:not[.h-*] then use that [href] for url
        //else if .h-x>area[href]:only-of-type:not[.h-*] then use that [href] for url
        for (String childTag : Arrays.asList("a", "area")) {
            Elements children = filterByTag(elem.children(), childTag);
            if(children.size() == 1) {
                Element child = children.first();
                if (!hasRootClass(child) && child.hasAttr("href")) {
                    return child.attr("href");
                }
            }
        }

        return null;
    }


    private String parseImpliedName(Element elem) {
        if (("img".equals(elem.tagName()) || ("area".equals(elem.tagName())) && elem.hasAttr("alt"))) {
            return elem.attr("alt");
        }
        if ("abbr".equals(elem.tagName()) && elem.hasAttr("title")) {
            return elem.attr("title");
        }

        Elements children = elem.children();
        if (children.size() == 1) {
            Element child = children.first();
            // else if .h-x>img:only-child[alt]:not[.h-*] then use that img alt for name
            // else if .h-x>area:only-child[alt]:not[.h-*] then use that area alt for name
            if (!hasRootClass(child)
                    && ("img".equals(child.tagName()) || "area".equals(child.tagName()))
                    && child.hasAttr("alt")) {
                return child.attr("alt");
            }
            // else if .h-x>abbr:only-child[title] then use that abbr title for name
            if ("abbr".equals(child.tagName()) && child.hasAttr("title")) {
                return child.attr("title");
            }

            Elements grandChildren = child.children();
            if (grandChildren.size() == 1) {
                Element grandChild = grandChildren.first();
                // else if .h-x>:only-child>img:only-child[alt]:not[.h-*] then use that img alt for name
                // else if .h-x>:only-child>area:only-child[alt]:not[.h-*] then use that area alt for name
                if (!hasRootClass(grandChild)
                        && ("img".equals(grandChild.tagName()) || "area".equals(grandChild.tagName()))
                        && grandChild.hasAttr("alt")) {
                    return grandChild.attr("alt");
                }
                // else if .h-x>:only-child>abbr:only-child[title] use that abbr title for name
                if ("abbr".equals(grandChild.tagName()) && grandChild.hasAttr("c")) {
                    return grandChild.attr("title");
                }
            }
        }

        // else use the textContent of the .h-x for name
        // drop leading & trailing white-space from name, including nbsp
        return elem.text().trim();
    }

    private Elements filterByTag(Elements elems, String tag) {
        Elements filtered = new Elements();
        for (Element child : elems) {
            if (tag.equals(child.tagName())) {
                filtered.add(child);
            }
        }
        return filtered;
    }

    private JsonList getRootClasses(Element elem) {
        JsonList rootClasses = null;
        for (String className : elem.classNames()) {
            if (isRootClass(className)) {
                if (rootClasses == null) {
                    rootClasses = new JsonList();
                }
                rootClasses.add(className);
            }
        }
        return rootClasses;
    }

    private boolean hasRootClass(Element elem) {
        for (String className : elem.classNames()) {
            if (isRootClass(className)) {
                return true;
            }
        }
        return false;
    }

    // We only care for h-card (now).
    private boolean isRootClass(String className) {
        return className.equals("h-card");
    }
}
