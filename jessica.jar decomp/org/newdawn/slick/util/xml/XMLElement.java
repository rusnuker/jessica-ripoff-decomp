/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.util.xml;

import org.newdawn.slick.util.xml.SlickXMLException;
import org.newdawn.slick.util.xml.XMLElementList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLElement {
    private Element dom;
    private XMLElementList children;
    private String name;

    XMLElement(Element xmlElement) {
        this.dom = xmlElement;
        this.name = this.dom.getTagName();
    }

    public String[] getAttributeNames() {
        NamedNodeMap map = this.dom.getAttributes();
        String[] names = new String[map.getLength()];
        int i = 0;
        while (i < names.length) {
            names[i] = map.item(i).getNodeName();
            ++i;
        }
        return names;
    }

    public String getName() {
        return this.name;
    }

    public String getAttribute(String name) {
        return this.dom.getAttribute(name);
    }

    public String getAttribute(String name, String def) {
        String value = this.dom.getAttribute(name);
        if (value == null || value.length() == 0) {
            return def;
        }
        return value;
    }

    public int getIntAttribute(String name) throws SlickXMLException {
        try {
            return Integer.parseInt(this.getAttribute(name));
        }
        catch (NumberFormatException e) {
            throw new SlickXMLException("Value read: '" + this.getAttribute(name) + "' is not an integer", e);
        }
    }

    public int getIntAttribute(String name, int def) throws SlickXMLException {
        try {
            return Integer.parseInt(this.getAttribute(name, "" + def));
        }
        catch (NumberFormatException e) {
            throw new SlickXMLException("Value read: '" + this.getAttribute(name, "" + def) + "' is not an integer", e);
        }
    }

    public double getDoubleAttribute(String name) throws SlickXMLException {
        try {
            return Double.parseDouble(this.getAttribute(name));
        }
        catch (NumberFormatException e) {
            throw new SlickXMLException("Value read: '" + this.getAttribute(name) + "' is not a double", e);
        }
    }

    public double getDoubleAttribute(String name, double def) throws SlickXMLException {
        try {
            return Double.parseDouble(this.getAttribute(name, "" + def));
        }
        catch (NumberFormatException e) {
            throw new SlickXMLException("Value read: '" + this.getAttribute(name, "" + def) + "' is not a double", e);
        }
    }

    public boolean getBooleanAttribute(String name) throws SlickXMLException {
        String value = this.getAttribute(name);
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new SlickXMLException("Value read: '" + this.getAttribute(name) + "' is not a boolean");
    }

    public boolean getBooleanAttribute(String name, boolean def) throws SlickXMLException {
        String value = this.getAttribute(name, "" + def);
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new SlickXMLException("Value read: '" + this.getAttribute(name, "" + def) + "' is not a boolean");
    }

    public String getContent() {
        String content = "";
        NodeList list = this.dom.getChildNodes();
        int i = 0;
        while (i < list.getLength()) {
            if (list.item(i) instanceof Text) {
                content = String.valueOf(content) + list.item(i).getNodeValue();
            }
            ++i;
        }
        return content;
    }

    public XMLElementList getChildren() {
        if (this.children != null) {
            return this.children;
        }
        NodeList list = this.dom.getChildNodes();
        this.children = new XMLElementList();
        int i = 0;
        while (i < list.getLength()) {
            if (list.item(i) instanceof Element) {
                this.children.add(new XMLElement((Element)list.item(i)));
            }
            ++i;
        }
        return this.children;
    }

    public XMLElementList getChildrenByName(String name) {
        XMLElementList selected = new XMLElementList();
        XMLElementList children = this.getChildren();
        int i = 0;
        while (i < children.size()) {
            if (children.get(i).getName().equals(name)) {
                selected.add(children.get(i));
            }
            ++i;
        }
        return selected;
    }

    public String toString() {
        String value = "[XML " + this.getName();
        String[] attrs = this.getAttributeNames();
        int i = 0;
        while (i < attrs.length) {
            value = String.valueOf(value) + " " + attrs[i] + "=" + this.getAttribute(attrs[i]);
            ++i;
        }
        value = String.valueOf(value) + "]";
        return value;
    }
}

