package org.industrial.ontology.app.admin.persistence;

import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code ApplicationPreferences} collection; ported from the legacy {@code ApplicationPreferencesStore}. The
 * preferences are read once and cached; an empty collection is given the defaults.
 */
public class ApplicationPreferencesRepository {

    private final MongoOperations mongo;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    @Nullable
    private volatile ApplicationPreferencesDocument cachedPreferences;

    public ApplicationPreferencesRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    @Nonnull
    public ApplicationPreferencesDocument getApplicationPreferences() {
        var cached = cachedPreferences;
        if (cached != null) {
            return cached;
        }
        readLock.lock();
        try {
            var preferences = mongo.findById(ApplicationPreferencesDocument.ID, ApplicationPreferencesDocument.class);
            if (preferences == null) {
                preferences = ApplicationPreferencesDocument.defaults();
                mongo.save(preferences);
            }
            cachedPreferences = preferences;
            return preferences;
        } finally {
            readLock.unlock();
        }
    }

    public void setApplicationPreferences(@Nonnull ApplicationPreferencesDocument preferences) {
        writeLock.lock();
        try {
            mongo.save(checkNotNull(preferences));
            cachedPreferences = preferences;
        } finally {
            writeLock.unlock();
        }
    }
}
