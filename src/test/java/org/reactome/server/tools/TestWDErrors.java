package org.reactome.server.tools;

import com.martiansoftware.jsap.JSAPException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

/**
 * Unit test for simple WikiDataPathwayExtractor.
 */
public class TestWDErrors {

    private static  Pathway pathway;
    private static WikiDataPathwayExtractor wdextract;
    private static String expected = "invalid pathway";

    @BeforeClass
    public static void setup() throws JSAPException {
        wdextract = new WikiDataPathwayExtractor((Pathway)(null));
    }

    @org.junit.Test
    public void testConstructor()
    {
        Assert.assertTrue( "WikiDataPathwayExtractor constructor failed", wdextract != null );
    }

    @org.junit.Test
    public void testEntry()
    {
        wdextract.createWikidataEntry();
        String entry = wdextract.getWikidataEntry();

        Assert.assertTrue( "WikiDataPathwayExtractor createEntry failed", entry != null );
        Assert.assertEquals(entry, expected);
    }


}