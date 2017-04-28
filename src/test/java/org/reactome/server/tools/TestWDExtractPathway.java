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
public class TestWDExtractPathway {

    private static  Pathway pathway;
    private static WikiDataExtractor wdextract;
    private static String expected = "HSA,R-HSA-168275,Entry of Influenza Virion into Host Cell via Endocytosis,"
        +"An instance of Entry of Influenza Virion into Host Cell via Endocytosis in Homo sapiens,[],GO:0019065,None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-168275"; // pathway with a single child reaction
        pathway = (Pathway) databaseObjectService.findById(dbid);

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