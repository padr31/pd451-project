package uk.ac.cam.pd451.dissertation.pipeline.run;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.cam.pd451.dissertation.datalog.*;
import uk.ac.cam.pd451.dissertation.pipeline.Step;
import uk.ac.cam.pd451.dissertation.pipeline.io.EmptyIO;
import uk.ac.cam.pd451.dissertation.utils.Props;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This Step imports the provenance graph outputted by Datalog
 * from the Datalog output directory.
 *
 * Provenance is leaded as a list of clauses which are usually optimised afterwards.
 */
public class ProvenanceImportStep implements Step<EmptyIO, List<Clause>> {

        @Override
        public List<Clause> process(EmptyIO input) throws PipeException {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                String provenanceOutputFolder = Props.get("provenanceOutputFolder");
                File file = new File(provenanceOutputFolder + "/provenance.json");
                List<GroundClausePOJO> provenance = objectMapper.readValue(file, new TypeReference<List<GroundClausePOJO>>(){});
                //System.out.println(objectMapper.writeValueAsString(provenance));

                return clausify(provenance);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private List<Clause> clausify(List<GroundClausePOJO> provenance) {
            List<Clause> groundClauses = new ArrayList<>();
            for(GroundClausePOJO groundClause : provenance) {
                groundClauses.add(
                        new Clause(
                                new Predicate(groundClause.getPredicate(), groundClause.getTerms()),
                                provenanceItemToPredicateList(groundClause.getProvenance())
                        )
                );
            }
            return groundClauses;
        }

        private List<Predicate> provenanceItemToPredicateList(GroundClauseProvenancePOJO provenanceItem) {
            return provenanceItem.getBody().stream().map(clPOJO -> {
                return new Predicate(
                        clPOJO.getPredicate(),
                        clPOJO.getTerms().stream().map(ClauseTermPOJO::getContents).collect(Collectors.toList()));
            }).collect(Collectors.toList());
        }
}

