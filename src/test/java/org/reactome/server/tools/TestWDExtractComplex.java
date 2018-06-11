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
 * Unit test for simple WikiDataComplexExtractor.
 */
public class TestWDExtractComplex {

    private static  Complex complex;
    private static WikiDataComplexExtractor wdextract;
    private static String expected = "HSA,COMP,R-HSA-2220975,FBXW7 WD mutants:SKP1:CUL1:RBX1,[EWAS P63208 1 R-HSA-187538;EWAS P62877 1 R-HSA-1234142;EWAS Q13616 1 R-HSA-187551;DS null 1 R-HSA-1602340],,None";

    @BeforeClass
    public static void setup() throws JSAPException {
        DatabaseObjectService databaseObjectService = ReactomeGraphCore.getService(DatabaseObjectService.class);
        String dbid = "R-HSA-2220975";
        complex = (Complex) databaseObjectService.findById(dbid);

        wdextract = new WikiDataComplexExtractor(complex);
    }

    @org.junit.Test
    public void testConstructor()
    {
        Assert.assertTrue( "WikiDataComplexExtractor constructor failed", wdextract != null );
    }

    @org.junit.Test
    public void testEntry()
    {
        wdextract.createWikidataEntry();
        String entry = wdextract.getWikidataEntry();

        Assert.assertTrue( "WikiDataComplexExtractor createEntry failed", entry != null );
        Assert.assertEquals(entry, expected);
    }

}