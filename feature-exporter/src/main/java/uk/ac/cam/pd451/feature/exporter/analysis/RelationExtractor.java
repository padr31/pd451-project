package uk.ac.cam.pd451.feature.exporter.analysis;

/**
 * Interface that is able to produce an analysis relation,
 * for example, the extractRelation method can connect to
 * a graph database containing the Java AST
 * and query it for allocation points.
 */
public interface RelationExtractor {
    Relation extractRelation();
}
