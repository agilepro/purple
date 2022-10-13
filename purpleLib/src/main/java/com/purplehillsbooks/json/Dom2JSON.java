package com.purplehillsbooks.json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * <p> This class contains routines to convert a standard XML DOM
 * into a JSON structure.  Useful for the kind of XML is that which has
 * been used to store DATA.  What I mean by that is that data is always
 * name-value pair, can be stored in an attribute OR can be stored in
 * scalar form (one element with a name and contents in the value).
 * If you have a set of values then you use a set of elements all
 * the same name.</p>
 * 
 * <h1>ALGORITHM EXPLANATION</h1>
 *
 * <p> * An element can come in two forms:  scalar value and object
 * The Object has members, and two types of member: scalar or vector</p>
 * 
 * <h2>
 * A: &lt;elementA&gt;value&lt;/elementA&gt;
 * </h2>
 * 
 * <p> The simple scalar form can ONLY have text within it, nothing else.
 * No attributes, no sub elements.  This then gets translated
 * to the following JSON: </p>
 * 
 * <pre>
 * {
 *     "elementA": value
 * }
 * </pre>
 * 
 * <h2>
 * B: &lt;elementB attrib="aval"&gt;&lt;sub&gt;xxx&lt;/sub&gt;&lt;/elemenetB&gt;
 * </h2>
 * 
 * <p>In case B, we we have an object, and must structure this as a
 * JSON object.  Each of the attributes becomes a single named
 * string element.  Each sub-element is converted depending upon
 * whether it is simple or complex, and whether there is one
 * or multiple.   In this first case, we have one attribute and
 * one simple sub-element</p>
 *
 * <pre>
 * {
 *     "elementB": {
 *         "attrib": "aval",
 *         "sub":  "xxx"
 *     }
 * }
 * </pre>
 *
 * <p>Attributes always map to simple values, and are treated the
 * same as simple subelements. The order is assumed to be unimportant.</p>
 *
 * <p>The next case to consider is multiple simple subelements.
 * If multiple tags with the same name appear
 * in the child, then the output need to map to an array value.</p>
 *
 * <h2>
 * C:  &lt;elementC&gt;&lt;sub&gt;xxx&lt;/sub&gt;&lt;sub&gt;yyy&lt;/sub&gt;&lt;sub&gt;zzz&lt;/sub&gt;&lt;/elemenetC&gt;
 * </h2>
 *
 * <p>Then the result needs to be:</p>
 *
 * <pre>
 * {
 *     "elementC": {
 *         "sub":  ["xxx", "yyy", "zzz"]
 *     }
 * }
 * </pre>
 *
 * <p>The next case to consider is a single complex sub element:</p>
 *
 * <h2>
 * D: &lt;elementD&gt;&lt;sub x="23" y="48"/&gt;&lt;/elemenetD&gt;
 * </h2>
 *
 * <p>Because the sub element is complex, the result must be an
 * object notation:</p>
 *
 * <pre>
 * {
 *     "elementD": {
 *         "sub":  {
 *             x: 23,
 *             y: 48
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>The final case to consider is multiple complex sub elements
 * and you get an array of json objects:</p>
 *
 * <h2>
 * E: &lt;elementE&gt;&lt;sub x="1" y="2"/&gt;&lt;sub x="3" y="4"/&gt;&lt;/elemenetE&gt;
 * </h2>
 * <pre>
 * {
 *     "elementD": {
 *         "sub":  [
 *             {
 *                 x: 23,
 *                 y: 48
 *             },
 *             {
 *                 x: 23,
 *                 y: 48
 *             }
 *         ]
 *     }
 * }
 * </pre>
 *
 * <h1>LIMITATIONS</h1>
 *
 * <p>IF an element is supposed to be an array, but the source document
 * has only a single item, then there is no way for the introspection
 * approach to determine that is it supposed to be an array or not.
 * If there is only one (or zero) element, it can not know that it
 * is supposed to be an array.</p>
 *
 * <p>So Hints is a map from element name to an integer</p>
 *
 * <ul>
 * <li>  0 (HINT_SIMPLE) means it is a simple value, just get the string value out</li>
 * <li>  1 (HINT_SIMPLE_ARRAY) means array of simple values, that is, this element defines
 *         a simple value and it should be one of multiple such values</li>
 * <li>  2 (HINT_OBJECT) means it is an object -- even if no attributes or sub-elements appear
 *         still always treat this as an object</li>
 * <li>  3 (HINT_OBJECT_ARRAY) means multiple objects -- this should be put in an array and
 *         treated as an object regardless of whether it looks like one</li>
 * </ul>
 * 
 * <p>In this example:</p>
 *
 * <pre>
 * &lt;books&gt;&lt;book/&gt;&lt;/books&gt;
 * </pre>
 *
 * <p>The hint of 3 is placed on "book" so that book will be interpreted
 * as an object, and also to say that there can be multiple books potentially.
 * Unfortunately, if the XML has zero book elements, there is no way to
 * know about the hint, and then no empty array can be generated.  To solve
 * this we would need a more complete schema definition of all the things
 * that could be within any given element.</p>
 *
 *
 * <h1>STRIPPING AND IGNORING TEXT</h1>
 *
 * <p>An important thing to remember is that when an element is determined
 * to be non-simple, then all of the text within it is ignored.  THat is,
 * all the text between the start and end tag that is NOT within a
 * sub element.   For data usage in XML, the text around the sub elements
 * is usually text for indenting the tags for display.  This transformation
 * strips that out.  (There is no way in XML to distinguish content text from
 * text that is used for layout, and there is a weak mechanism to consider
 * all white space collapsable into a single character -- or even no characters.
 * This is a weakness of XML and why you should generally prefer JSON for data.)</p>
 *
 * <p>In the example below, all of the 'x' will be ignored.  IN a real XML this
 * might be white space, or it might be other characters, but in this conversion
 * they will always be ignored.</p>
 *
 * <pre>
 * &lt;elementF&gt;xx
 * xxxxx&lt;sub&gt;Hello&lt;/sub&gt;xx
 * xxxxx&lt;dub&gt;
 * xxxxxxxx&lt;dubdub&gt;World&lt;/dubdub&gt;xxx
 * xxxxx&lt;/dub&gt;xxx
 * &lt;/elementF&gt;
 * </pre>
 *
 * <p>The implication of this is that this is NOT useful for converting HTML
 * to JSON.  In HTML, you can have text, and within that text a word might
 * be marked BOLD or something.  In this conversion, all the text except
 * for the small block marked bold would be ignored.  Another way of thinking
 * about this is that text for data can only exist as leaves at the end of
 * the tree.  Text that is not at a leaf will be ignored.</p>
 */

public class Dom2JSON {
    
/**
 * HINT_SIMPLE (0) means it is a simple value, just take whatever string is found in the XML, and associate it with the attribute name as a string.
 * Used in the hint hashtable parameter of convertDomToJSON and convertElementToJSON.
 */
    public static final int HINT_SIMPLE       = 0;

    /**
 * HINT_SIMPLE_ARRAY (1) means this element will appear multiple times in the current context and make an array of simple values, 
 * that is, and associate this name with an array that is then filled with simple string values
 * Used in the hint hashtable parameter of convertDomToJSON and convertElementToJSON.
 */    
    public static final int HINT_SIMPLE_ARRAY = 1;

/**
 * HINT_OBJECT (2) means it is an object even if no attributes or sub-elements appear,
 * still always treat this as an object and return an object whether empty or not.
 * Used in the hint hashtable parameter of convertDomToJSON and convertElementToJSON.
 */    
    public static final int HINT_OBJECT       = 2;

/**
 * HINT_OBJECT_ARRAY (3) means that this can can appear multiple times in the context, and that each time it is expected to have something complicated in it,
 * so assocait the name with an array, and put the contents into object in that array.
 * Used in the hint hashtable parameter of convertDomToJSON and convertElementToJSON.
 */    
    public static final int HINT_OBJECT_ARRAY = 3;

    
/**
 * if UseAtSignForAttributeName is set false, then attribute names will be 
 * used directly in the JSONObject member without modification.
 * 
 * if UseAtSignForAttributeName is true, then attribute names will have 
 * an at-sign (@) prepended to the name for the JSONOjbect member.
 */
    public static boolean UseAtSignForAttributeName = false;

/**
 * if includeContentsWithAttributes affects what happens with XML tags that have
 * attributes and also have direct string contents.  This is set to true,
 * the contents of an element which has attributes, no children, but contains text
 * will have the text places in a member calles "@"
 * 
 * Previously the converted did not do this, and would simply throw the text away
 * of the element had attributes.  Get the old behavior by setting this false.
 */
    public static boolean includeContentsWithAttributes = true;
        
    /**
     * Pass in a DOM Document, and you get a JSON object that represents
     * the entire contents.
     */
    public static JSONObject convertDomToJSON(Document doc) throws Exception {
        Element rootEle = doc.getDocumentElement();
        return convertElementToJSON(rootEle);
    }

    /**
     * Pass in a DOM Document, and you get a JSON object that represents
     * the entire contents.
     */
    public static JSONObject convertDomToJSON(Document doc, Hashtable<String,Integer> hints) throws Exception {
        Element rootEle = doc.getDocumentElement();
        return convertElementToJSON(rootEle, hints);
    }

    /**
     * Pass in an element and make a stand alone JSON structure for this one
     * element.   Please note that this method can not be used if the element
     * has siblings.  It only works for a single, standing alone element like
     * the root element of a document or the root of any tree.
     */
    private static JSONObject convertElementToJSON(Element rootEle) throws Exception {
        return convertElementToJSON(rootEle, new Hashtable<String,Integer>());
    }

    /**
     * Pass in an element and make a stand alone JSON structure for this one
     * element.   Please note that this method can not be used if the element
     * has siblings.  It only works for a single, standing alone element like
     * the root element of a document or the root of any tree.
     */
    private static JSONObject convertElementToJSON(Element rootEle,
            Hashtable<String,Integer> hints) throws Exception {

        JSONObject jo = new JSONObject();
        String tagName = rootEle.getTagName();
        boolean isSimple = elementIsSimple(rootEle, hints.get(tagName));
        if (isSimple) {
            String value = getElementText(rootEle);
            jo.put(tagName, value);
        }
        else {
            JSONObject eleObj = getElementObj(rootEle, hints);
            jo.put(tagName, eleObj);
        }
        return jo;
    }


    /**
     * determines whether this element is simple or not.
     * Simple elements can be converted to a string value.
     * Non-simple elements need to be converted to a JSONObject
     */
    private static boolean elementIsSimple(Element ele, Integer hintValue) {
        if (hintValue!=null) {
            int val = hintValue.intValue();
            return (val==HINT_SIMPLE || val==HINT_SIMPLE_ARRAY);
        }
        return inspectWhetherSimple(ele);
    }
    
    private static boolean inspectWhetherSimple(Element ele) {
        //try to figure it out by inspecting the data.  It is simple
        //if there are no attributes and no child nodes
        NodeList nl = ele.getChildNodes();
        int last = nl.getLength();
        NamedNodeMap nnm = ele.getAttributes();
        int attributeCount = nnm.getLength();
        int elementCount = 0;
        for (int i=0; i<last; i++) {
            Node node = nl.item(i);
            short type = node.getNodeType();
            if (Node.ELEMENT_NODE==type) {
                elementCount++;
            }
        }
        return (attributeCount==0 && elementCount==0);
    }


    /**
     * Gets the content of the element as an object that includes members
     * for each of the attributes, and for each of the sub elements.
     * Text within the element and outside of the subelements will be
     * thrown away.
     *
     * This does NOT use the element name, it returns everything except
     * for the name.
     */
    private static JSONObject getElementObj(Element ele, Hashtable<String,Integer> hints) throws Exception {
        JSONObject jo = new JSONObject();
        NodeList nl = ele.getChildNodes();
        int last = nl.getLength();
        
        Set<String> isObject = new HashSet<String>();
        Set<String> isArray = new HashSet<String>();
        Set<String> wasSeen = new HashSet<String>();
        boolean hasChildren = false;
        for (int i=0; i<last; i++) {
            Node node = nl.item(i);
            short type = node.getNodeType();
            if (Node.ATTRIBUTE_NODE==type) {
                String attName = node.getNodeName();
                if (wasSeen.contains(attName)) {
                    isArray.add(attName);
                }
                wasSeen.add(attName);
                Integer hintType = hints.get(attName);
                if (hintType!=null) {
                    if (hintType >= HINT_OBJECT) {
                        isObject.add(attName);
                    }
                    if (hintType == HINT_SIMPLE_ARRAY || hintType == HINT_OBJECT_ARRAY) {
                        isArray.add(attName);
                    }
                }
            }
            else if (Node.ELEMENT_NODE==type) {
                hasChildren = true;
                Element ele2 = (Element)node;
                String tagName = ele2.getTagName();
                if (wasSeen.contains(tagName)) {
                    isArray.add(tagName);
                }
                wasSeen.add(tagName);
                Integer hintType = hints.get(tagName); 
                if (hintType!=null) {
                    if (hintType >= HINT_OBJECT) {
                        isObject.add(tagName);
                    }
                    if (hintType == HINT_SIMPLE_ARRAY || hintType == HINT_OBJECT_ARRAY) {
                        isArray.add(tagName);
                    }
                }
                if (!isObject.contains(tagName)) {
                    //if nothing above tells us it is an object, look now to see if it has any children
                    if (!inspectWhetherSimple(ele2)) {
                        isObject.add(tagName);
                    }
                }
            }
        }
        
        Hashtable<String,ArrayList<String>> stringMap = new Hashtable<String,ArrayList<String>>();
        Hashtable<String,ArrayList<JSONObject>> objectMap = new Hashtable<String,ArrayList<JSONObject>>();
        for (int i=0; i<last; i++) {
            Node node = nl.item(i);
            short type = node.getNodeType();
            if (Node.ATTRIBUTE_NODE==type) {
                String attName = node.getNodeName();
                if (UseAtSignForAttributeName) {
                    attName = "@"+attName;
                }
                String attValue = node.getNodeValue();
                ArrayList<String> stringList = stringMap.get(attName);
                if (stringList==null) {
                    stringList = new ArrayList<String>();
                    stringMap.put(attName, stringList);
                }
                stringList.add(attValue);
            }
            else if (Node.ELEMENT_NODE==type) {
                hasChildren = true;
                Element ele2 = (Element)node;
                String tagName = ele2.getTagName();
                if (!isObject.contains(tagName)) {
                    ArrayList<String> stringList = stringMap.get(tagName);
                    if (stringList==null) {
                        stringList = new ArrayList<String>();
                        stringMap.put(tagName, stringList);
                    }
                    stringList.add(getElementText(ele2));
                }
                else {
                    ArrayList<JSONObject> objList = objectMap.get(tagName);
                    if (objList==null) {
                        objList = new ArrayList<JSONObject>();
                        objectMap.put(tagName, objList);
                    }
                    objList.add(getElementObj(ele2, hints));
                }
            }
            else if (Node.TEXT_NODE!=type && Node.CDATA_SECTION_NODE!=type) {
                //ignore these
            }
        }
        
        for (String key : stringMap.keySet()) {
            ArrayList<String> stringList = stringMap.get(key);
            if (stringList.size()==1 && !isArray.contains(key)) {
                jo.put(key, stringList.get(0));
            }
            else {
                JSONArray ja = new JSONArray();
                for (String val : stringList) {
                    ja.put(val);
                }
                jo.put(key, ja);
            }
        }
        for (String key : objectMap.keySet()) {
            ArrayList<JSONObject> objList = objectMap.get(key);
            if (objList.size()==1 && !isArray.contains(key)) {
                jo.put(key, objList.get(0));
            }
            else {
                JSONArray ja = new JSONArray();
                for (JSONObject jj : objList) {
                    ja.put(jj);
                }
                jo.put(key, ja);
            }
        }
        if (!hasChildren && includeContentsWithAttributes) {
            //if there are no children elements  AND if the text is 
            //non trivial, then make a new member with '@' by itself.
            //This covers cases where an element has both attributes
            //and contents.  Note, this is an at-sign by itself, and 
            //so it can not conflict with attributes that have at-sign
            //at the beginning of the name.
            String contents = getElementText(ele).trim();
            if (contents.length()>0) {
                jo.put("@", contents);
            }
        }

        //are attributes separate?  Don't understand why they did not appear in the children above
        NamedNodeMap nnm = ele.getAttributes();
        last = nnm.getLength();
        for (int i=0; i<last; i++) {
            Node node = nnm.item(i);
            if (Node.ATTRIBUTE_NODE==node.getNodeType()) {
                String attName = node.getNodeName();
                String attValue = node.getNodeValue();
                jo.put(attName, attValue);
            }
        }
        
        return jo;
    }

    /**
     * Returns the string contents of a simple element.
     * All attributes and sub-elements will be ignored.
     * If there are multiple spans of text, all the text
     * is appended together.
     */
    private static String getElementText(Element ele) throws Exception {
        NodeList nl = ele.getChildNodes();
        StringBuffer sb = new StringBuffer();
        int last = nl.getLength();
        for (int i=0; i<last; i++) {
            Node node = nl.item(i);
            if (Node.TEXT_NODE==node.getNodeType()) {
                sb.append(node.getNodeValue());
            }
            else if (Node.CDATA_SECTION_NODE==node.getNodeType()) {
                sb.append(node.getNodeValue());
            }
        }
        return sb.toString();
    }
    
    /**
     * XML has tags that have namespace prefixes on them, and since those prefixes are not
     * necessarily the same from every source, this method will simply strip the prefix off
     * of keys and leave you will only the base key name on the presumption that no XML
     * key is duplicated -- that is, the XML you are working with does not actually need 
     * the prefix to distinguish the keys at a given level.  This is usually the case,
     * but certainly not guaranteed.
     */
    public static void recusiveStripNameSpace(JSONObject jo) throws Exception {
        ArrayList<String> keys = new ArrayList<String>();
        for (String key : jo.keySet()) {
            keys.add(key);
        }
        for (String key : keys) {
            int colonPos = key.indexOf(":");
            if (colonPos>0) {
                Object value = jo.get(key);
                jo.remove(key);
                String bareKey = key.substring(colonPos+1);
                jo.put(bareKey, value);
            }
        }
        for (String key : jo.keySet()) {
            Object value = jo.get(key);
            if (value instanceof JSONObject) {
                recusiveStripNameSpace((JSONObject)value);
            }
            else if (value instanceof JSONArray) {
                listStripNameSpace((JSONArray)value);
            }
        }
    }
    
    /**
     * XML has tags that have namespace prefixes on them, and since those prefixes are not
     * necessarily the same from every source, this method will simply strip the prefix off
     * of keys and leave you will only the base key name on the presumption that no XML
     * key is duplicated -- that is, the XML you are working with does not actually need 
     * the prefix to distinguish the keys at a given level.  This is usually the case,
     * but certainly not guaranteed.
     */
    public static void listStripNameSpace(JSONArray ja) throws Exception {
        for (int i=0; i<ja.length(); i++) {
            Object value = ja.get(i);
            if (value instanceof JSONObject) {
                recusiveStripNameSpace((JSONObject)value);
            }
            else if (value instanceof JSONArray) {
                listStripNameSpace((JSONArray)value);
            }
        }
    }

}
