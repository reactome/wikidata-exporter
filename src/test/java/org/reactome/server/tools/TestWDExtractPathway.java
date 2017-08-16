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
    private static String expected = "HSA,R-HSA-168275,P,Entry of Influenza Virion into Host Cell via Endocytosis,"
        +"An instance of the biological pathway Entry of Influenza Virion into Host Cell via Endocytosis in Homo sapiens,[],GO:0019065,"
    +"[R-HSA-168285],[],None";

    private static String expected_rn = "HSA,R-HSA-168285,R,Clathrin-Mediated Pit Formation And Endocytosis Of The "
    +"Influenza Virion,An instance of the biological reaction Clathrin-Mediated Pit Formation And Endocytosis Of The Influenza Virion in "
    +"Homo sapiens,[],GO:0019065,[],[R-HSA-168275],None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-168275";
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

    @org.junit.Test
    public void testReactionEntry()
    {
        ReactionLikeEvent rn = (ReactionLikeEvent)(pathway.getHasEvent().get(0));
        wdextract = new WikiDataExtractor(rn, 1, "R-HSA-168275");
        wdextract.createWikidataEntry();
        String entry = wdextract.getWikidataEntry();

        Assert.assertTrue( "WikiDataExtractor createReactionEntry failed", entry != null );
        Assert.assertEquals(entry, expected_rn);
    }


}