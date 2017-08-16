package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.config.GraphNeo4jConfig;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Unit test for simple WikiDataExtractor.
 */
public class TestWDExtractGOTerm2 {

    private static  ReactionLikeEvent pathway;
    private static WikiDataExtractor wdextract;
    private static String expected = "HSA,R-HSA-8848215,R,ACAT2 condenses 2 Ac-CoA to form ACA-CoA," +
            "An instance of the biological reaction ACAT2 condenses 2 Ac-CoA to form ACA-CoA in Homo sapiens," +
    "[http://identifiers.org/pubmed/9380443;http://identifiers.org/pubmed/7911016],,[],[],None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-8848215"; // pathway with a single child reaction
        pathway = (ReactionLikeEvent) databaseObjectService.findById(dbid);

        wdextract = new WikiDataExtractor(pathway);
    }

    @org.junit.Test
    public void testConstructor()
    {
        Assert.assertTrue( "WikiDataExtractor constructor failed", wdextract != null );
    }

    @org.junit.Test
    public void testEntry()
    {
        wdextract.createWikidataEntry();
        String entry = wdextract.getWikidataEntry();

        Assert.assertTrue( "WikiDataExtractor createEntry failed", entry != null );
        Assert.assertEquals(entry, expected);
    }


}