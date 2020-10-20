package org.reactome.server.tools;

import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.DatabaseIdentifier;

import java.util.List;

/**
 * @author Yusra Haider (yhaider@ebi.ac.uk)
 **/

public class WDComplex extends WDPhysicalEntity {

    private String complexPortalRef;

    public WDComplex(Complex complex) {
        super(complex);
        super.setType("COMP");
        this.complexPortalRef = populateComplexPortalRef(complex);
    }

    private String populateComplexPortalRef(Complex complex) {
        List<DatabaseIdentifier> xrefs = complex.getCrossReference();
        if (xrefs != null) {
            for (DatabaseIdentifier db : xrefs) {
                if (db.getDatabaseName().equals("ComplexPortal")) {
                    return db.getIdentifier();
                }
            }
        }
        return null;
    }

    public String getComplexPortalRef() {
        return complexPortalRef;
    }
}
