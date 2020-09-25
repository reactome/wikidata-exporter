package org.reactome.server.tools;

import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.DatabaseIdentifier;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.service.helper.StoichiometryObject;

import java.util.ArrayList;
import java.util.List;

public class WDComplex extends WDPhysicalEntity {

    private String complexPortalRef;

    public WDComplex(Complex complex) {
        super(complex);
        // TODO: we needa define this in a constants file
        super.setType("COMP");
        this.complexPortalRef = populateComplexPortalRef(complex);
    }

    private String populateComplexPortalRef(Complex complex) {
        List<DatabaseIdentifier> xrefs = complex.getCrossReference();
        if (xrefs != null) {
            for (DatabaseIdentifier db: xrefs) {
                if (db.getDatabaseName().equals("ComplexPortal")) {
                    return db.getIdentifier();
                }
            }
        }
        return null;
    }

    public String getComplexPortalRef() { return complexPortalRef; }
}
