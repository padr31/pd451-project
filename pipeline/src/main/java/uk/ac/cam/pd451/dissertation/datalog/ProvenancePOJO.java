package uk.ac.cam.pd451.dissertation.datalog;

import java.util.List;

/**
 * Represent a a Provenance object as outputted by the Vanillalog Datalog frontend.
 * The following is the skeleton of Provenance objects:
 * ProvenancePOJO {
 *     [GroundCalusePOJO groundClauses {
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
 *     }]
 * }
 */
public class ProvenancePOJO {

    List<GroundClausePOJO> groundClauses;

    public List<GroundClausePOJO> getGroundClauses() {
        return groundClauses;
    }

    public void setGroundClauses(List<GroundClausePOJO> groundClauses) {
        this.groundClauses = groundClauses;
    }
}
