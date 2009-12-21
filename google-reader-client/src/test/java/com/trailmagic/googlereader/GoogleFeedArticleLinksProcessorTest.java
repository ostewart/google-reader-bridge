package com.trailmagic.googlereader;

import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by: oliver on Date: Dec 20, 2009 Time: 4:34:45 PM
 */
public class GoogleFeedArticleLinksProcessorTest {
    private static final String TEST_FEED = "classpath:com/trailmagic/googlereader/testfeed.xml";

    @Test
    public void testExtractsIdsFromGoogleFeed() throws Exception {
        String stringContent = loadContentFromResource(TEST_FEED);

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new StringReader(stringContent));

        GoogleFeedArticleLinksProcessor processor = new GoogleFeedArticleLinksProcessor();
        Map<String, String> mappings = processor.processWithDocument(document);
        XPath xPath = XPath.newInstance("//a:feed/a:entry");
        xPath.addNamespace("a", "http://www.w3.org/2005/Atom");
        int numEntries = xPath.selectNodes(document).size();
        assertEquals(numEntries, mappings.size());

//        assertEquals("tag:google.com,2005:reader/item/e5636a3f87610bd2", client.googleUrl("http://online.wsj.com/article/SB10001424052748703757404574592530591075444.html"));
    }

    private String loadContentFromResource(String location) throws IOException {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        InputStream contentInputStream = resource.getInputStream();
        contentInputStream.mark(Integer.MAX_VALUE);
        String stringContent = IOUtils.toString(contentInputStream);
        contentInputStream.reset();
        return stringContent;
    }

}
