package org.reactome.server.tools;

import com.martiansoftware.jsap.JSAPException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

/**
 * Unit test for simple WikiDataExtractor.
 */
public class TestWDExtractMultiplePathways {

    private static String dbid = "R-HSA-168255";
    private static  Pathway pathway, childPathway;
    private static WikiDataExtractor wdextract;
    private static String expected = "HSA,R-HSA-168255,Influenza Life Cycle,An instance of Influenza Life Cycle "
    +"in Homo sapiens,[],GO:0016032,"
            +"[R-HSA-168275;R-HSA-168274;R-HSA-168272;R-HSA-168273;R-HSA-168270;R-HSA-168271;R-HSA-168268],[],None";

    private static String expected1 = "HSA,R-HSA-168275,Entry of Influenza Virion into Host Cell via Endocytosis,"
            +"An instance of Entry of Influenza Virion into Host Cell via Endocytosis in Homo sapiens,[],GO:0019065,"
            +"[R-HSA-168285],[R-HSA-168255],None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        pathway = (Pathway) databaseObjectService.findById(dbid);
        childPathway = (Pathway) databaseObjectService.findById("R-HSA-168275");

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
    public void testChildEntry()
    {
        wdextract = new WikiDataExtractor(childPathway, 61, dbid);
        wdextract.createWikidataEntry();
        String entry = wdextract.getWikidataEntry();

        Assert.assertTrue( "WikiDataExtractor createEntry failed", entry != null );
        Assert.assertEquals(entry, expected1);
    }

}