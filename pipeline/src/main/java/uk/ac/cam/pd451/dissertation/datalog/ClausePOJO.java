package uk.ac.cam.pd451.dissertation.datalog;

import java.util.List;

/*    ClausePOJO body {
 *         [ClauseTermPOJO terms {
 *              String tag,
 *              String contents
 *         }],
 *         String predicate,
 *         String polarity
 *    }
 */
public class ClausePOJO {
    List<ClauseTermPOJO> terms;
    String predicate;
    String polarity;

    public List<ClauseTermPOJO> getTerms() {
        return terms;
    }

    public void setTerms(List<ClauseTermPOJO> terms) {
        this.terms = terms;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getPolarity() {
        return polarity;
    }

    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }
}
