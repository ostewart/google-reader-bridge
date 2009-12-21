package com.trailmagic.googlereader;

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
 * Created by: oliver on Date: Dec 20, 2009 Time: 8:00:41 PM
 */
@Service
public class GoogleReadStatusProcessor extends XPathEntityContentProcessor<Map<String, Boolean>> {
    @SuppressWarnings({"unchecked"})
    @Override
    public Map<String, Boolean> processWithDocument(Document doc) throws Exception {
        Map<String, Boolean> statuses = new HashMap<String, Boolean>();
        XPath xPath = atomXPath("//a:feed/a:entry");
        for (Element entry : (List<Element>) xPath.selectNodes(doc)) {
            Text googleId = (Text) atomXPath("a:id/text()").selectSingleNode(entry);
            if (googleId != null) {
                statuses.put(googleId.getValue(), Boolean.TRUE);
            }
        }
        return statuses;
    }
}
