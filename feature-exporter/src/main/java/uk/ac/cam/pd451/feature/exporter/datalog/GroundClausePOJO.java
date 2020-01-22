package uk.ac.cam.pd451.feature.exporter.datalog;

import java.util.List;

/**
 * GroundCalusePOJO groundClauses {
 *         [String terms],
 *         String predicate,
 *         GroundClauseProvenancePOJO provenance {
 *             [ClausePOJO body {
 *                 [ClauseTermPOJO terms {
 *                      String tag,
 *                      String contents
 *                 }],
 *                 String predicate,
 *                 String polarity
 *             }],
 *             ClausePOJO head {
 *                    [ClauseTermPOJO terms {
 *                        String tag,
 *                        String contents
 *                    }],
 *                    String predicate,
 *                    String polarity
 *             }
 *         }
 *     }
 */
public class GroundClausePOJO {
    List<String> terms;
    String predicate;
    GroundClauseProvenancePOJO provenance;

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public GroundClauseProvenancePOJO getProvenance() {
        return provenance;
    }

    public void setProvenance(GroundClauseProvenancePOJO provenance) {
        this.provenance = provenance;
    }
}
