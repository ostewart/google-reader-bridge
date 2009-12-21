package com.trailmagic.googlereader;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.trailmagic.googlereader.XmlUtils.atomXPath;


/**
* Created by: oliver on Date: Dec 20, 2009 Time: 4:32:55 PM
*/
@Service
public class GoogleFeedArticleLinksProcessor extends XPathEntityContentProcessor<Map<String, String>> {
    @SuppressWarnings({"unchecked"})
    @Override
    public Map<String, String> processWithDocument(Document doc) throws Exception {
        Map<String, String> mappings = new HashMap<String, String>();

        XPath xPath = atomXPath("//a:feed/a:entry");
        for (Element entry : (List<Element>) xPath.selectNodes(doc)) {
            Attribute linkAttr = (Attribute) atomXPath("a:link[@rel='alternate']/@href").selectSingleNode(entry);
            Text googleId = (Text) atomXPath("a:id/text()").selectSingleNode(entry);
            if (linkAttr != null && googleId != null) {
                mappings.put(linkAttr.getValue(), googleId.getText());
            }
        }
        return mappings;

    }
}
