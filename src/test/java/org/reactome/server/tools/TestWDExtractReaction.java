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
public class TestWDExtractReaction {

    private static  ReactionLikeEvent reaction;
    private static WikiDataReactionExtractor wdextract;

    private static String expected = "HSA,R-HSA-168285,R,Clathrin-Mediated Pit Formation And Endocytosis Of The "
            +"Influenza Virion,An instance of the biological reaction Clathrin-Mediated Pit Formation And Endocytosis Of The Influenza Virion in "
            +"Homo sapiens,[],GO:0019065,[COMP null 1 R-FLU-188954],[SE 26667 1 R-ALL-189161;COMP null 1 R-FLU-189171],[COMP null 1 R-HSA-177482],[R-HSA-168275],None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-168285";
        reaction = (ReactionLikeEvent) databaseObjectService.findById(dbid);

        wdextract = new WikiDataReactionExtractor(reaction);
        wdextract.setParent("R-HSA-168275");
    }

    @org.junit.Test
    public void testConstructor()
    {
        Assert.assertTrue( "WikiDataReactionExtractor constructor failed", wdextract != null );
    }

    @org.junit.Test
    public void testEntry()
    {
        wdextract.createWikidataEntry();
        String entry = wdextract.getWikidataEntry();

        Assert.assertTrue( "WikiDataReactionExtractor createEntry failed", entry != null );
        Assert.assertEquals(entry, expected);
    }

}