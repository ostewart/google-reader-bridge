package com.trailmagic.googlereader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by: oliver on Date: Dec 20, 2009 Time: 4:52:48 PM
 */
public class XmlUtils {
    public static XPath atomXPath(String path) throws JDOMException {
        XPath xPath = XPath.newInstance(path);
        xPath.addNamespace("a", "http://www.w3.org/2005/Atom");
        xPath.addNamespace("gr", "http://www.google.com/schemas/reader/atom/");
        return xPath;
    }

    public int countXPath(Document document, String xPathString) throws JDOMException {
        XPath xPath = XPath.newInstance(xPathString);
        xPath.addNamespace("a", "http://www.w3.org/2005/Atom");
        return xPath.selectNodes(document).size();
    }

    public Document parseDocument(Reader reader) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(reader);

    }

}
