package uk.ac.cam.pd451.feature.exporter.datalog;

import java.util.List;

/**
 * GroundClauseProvenancePOJO provenance {
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
 */
public class GroundClauseProvenancePOJO {
    List<ClausePOJO> body;
    ClausePOJO head;

    public List<ClausePOJO> getBody() {
        return body;
    }

    public void setBody(List<ClausePOJO> body) {
        this.body = body;
    }

    public ClausePOJO getHead() {
        return head;
    }

    public void setHead(ClausePOJO head) {
        this.head = head;
    }
}
