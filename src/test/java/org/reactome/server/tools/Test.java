package org.reactome.server.tools;

import com.martiansoftware.jsap.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.config.GraphNeo4jConfig;


/**
 * @author Sarah Keating <skeating@ebi.ac.uk>
 */
@RunWith(value=Suite.class)

//@Suite.SuiteClasses(value={TestComplexGetHasComponent.class})

@Suite.SuiteClasses(value={
        TestWDExtractPathway.class, TestWDExtractGOTerm.class,
        TestWDExtractMultiplePathways.class, TestWDErrors.class,
        TestWDExtractGOTerm2.class
})


public class Test {

    @BeforeClass
    public static void setup()  throws JSAPException {
        SimpleJSAP jsap = new SimpleJSAP(WikidataExport.class.getName(), "A tool for generating SBML files",
                new Parameter[]{
                        new FlaggedOption("host", JSAP.STRING_PARSER, "localhost", JSAP.REQUIRED, 'h', "host", "The neo4j host"),
                        new FlaggedOption("port", JSAP.STRING_PARSER, "7474", JSAP.REQUIRED, 'b', "port", "The neo4j port"),
                        new FlaggedOption("user", JSAP.STRING_PARSER, null, JSAP.REQUIRED, 'u', "user", "The neo4j user"),
                        new FlaggedOption("password", JSAP.STRING_PARSER, null, JSAP.REQUIRED, 'p', "password", "The neo4j password")
                }
        );
        String[] args = {"-h", "localhost", "-b", "7474", "-u", "neo4j", "-p", "j16a3s27"};
        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);
        ReactomeGraphCore.initialise(config.getString("host"), config.getString("port"), config.getString("user"), config.getString("password"), GraphNeo4jConfig.class);
    }

}
