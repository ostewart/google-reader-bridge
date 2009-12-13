package com.trailmagic.googlereader;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by: oliver on Date: Dec 13, 2009 Time: 2:58:03 PM
 */
public class GoogleReaderClientTest {
    private GoogleReaderClient client;
    private Reader content;
    @Mock
    private DefaultHttpClient httpClient;
    @Mock
    private GoogleClientLogin clientLogin;
    private Document document;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:com/trailmagic/googlereader/testfeed.xml");
        InputStream inputStream = resource.getInputStream();
        String stringContent = IOUtils.toString(inputStream);
        content = new StringReader(stringContent);

        SAXBuilder builder = new SAXBuilder();
        document = builder.build(new StringReader(stringContent));

        client = new GoogleReaderClient(httpClient, clientLogin);
    }

    @Test
    public void testExtractsIdsFromGoogleFeed() throws JDOMException, IOException {
        Map<String, String> mappings = client.extractIdMapping(content);
        XPath xPath = XPath.newInstance("//a:feed/a:entry");
        xPath.addNamespace("a", "http://www.w3.org/2005/Atom");
        int numEntries = xPath.selectNodes(document).size();
        assertEquals(numEntries, mappings.size());
    }
}
