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
 * Unit test for simple WikiDataPathwayExtractor.
 */
public class TestWDExtractGOTerm {

    private static  Pathway pathway;
    private static WikiDataPathwayExtractor wdextract;
    private static String expected = "HSA,R-HSA-73894,P,DNA Repair,"
            +"An instance of the biological pathway DNA Repair in Homo sapiens,"
            +"[https://identifiers.org/pubmed/10583946;https://identifiers.org/pubmed/23175119],GO:0006281,"
            +"[R-HSA-73884;R-HSA-73893;R-HSA-73942;R-HSA-5693532;R-HSA-5696398;R-HSA-5358508;R-HSA-6783310],[],None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-73894"; // pathway with a single child reaction
        pathway = (Pathway) databaseObjectService.findById(dbid);

        wdextract = new WikiDataPathwayExtractor(pathway);
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