package org.industrial.ontology.kernel.crud;



import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;

import java.util.List;
import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityCrudKitPluginManager}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class EntityCrudKitPluginManager implements HasPlugins<EntityCrudKitPlugin<?,?,?>> {

    private final ImmutableList<EntityCrudKitPlugin<?,?,?>> plugins;

    public EntityCrudKitPluginManager(@Nonnull Set<EntityCrudKitPlugin<?,?,?>> plugins) {
        this.plugins = ImmutableList.copyOf(plugins);
    }

    public List<EntityCrudKitPlugin<?,?,?>> getPlugins() {
        return plugins;
    }
}
