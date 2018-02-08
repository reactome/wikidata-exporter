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
public class TestWDExtractPathway {

    private static  Pathway pathway;
    private static WikiDataPathwayExtractor wdextract;
    private static String expected = "HSA,R-HSA-168275,P,Entry of Influenza Virion into Host Cell via Endocytosis,"
        +"An instance of the biological pathway Entry of Influenza Virion into Host Cell via Endocytosis in Homo sapiens,[],GO:0019065,"
    +"[R-HSA-168285],[],None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-168275";
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