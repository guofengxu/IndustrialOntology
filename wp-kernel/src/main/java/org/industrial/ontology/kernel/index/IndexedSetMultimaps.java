package org.industrial.ontology.kernel.index;



import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Collection;
import java.util.HashMap;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.IndexedSetMultimaps}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-18
 */
public class IndexedSetMultimaps {

    public static <K, V> Multimap<Key<K>, V> create() {
        var backingMap = new HashMap<Key<K>, Collection<V>>();
        return Multimaps.newSetMultimap(backingMap, IndexedSet::new);
    }
}
