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
 * Unit test for simple WikiDataSetExtractor.
 */
public class TestWDExtractSet {

    private static  EntitySet complex;
    private static WikiDataSetExtractor wdextract;
    private static String expected = "HSA,DS,R-HSA-1602340,[CS null 1 R-HSA-1602341;CS null 1 R-HSA-1602334;" +
    "CS null 1 R-HSA-1602324;CS null 1 R-HSA-1602342;CS null 1 R-HSA-1602442],None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-1602340";
        complex = (EntitySet) databaseObjectService.findById(dbid);

        wdextract = new WikiDataSetExtractor(complex);
    }

    @org.junit.Test
    public void testConstructor()
    {
        Assert.assertTrue( "WikiDataSetExtractor constructor failed", wdextract != null );
    }

    @org.junit.Test
    public void testEntry()
    {
        wdextract.createWikidataEntry();
        String entry = wdextract.getWikidataEntry();

        Assert.assertTrue( "WikiDataSetExtractor createEntry failed", entry != null );
        Assert.assertEquals(entry, expected);
    }

}