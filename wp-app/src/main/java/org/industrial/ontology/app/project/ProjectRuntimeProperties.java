package org.industrial.ontology.app.project;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code webprotege.*} settings of the project runtime: how long loaded projects and their events are kept, and
 * the sizes of the {@link KernelExecutors} pools. Other {@code webprotege.*} keys are bound elsewhere.
 *
 * @param project projects in memory
 * @param events  project events
 * @param kernel  the shared kernel thread pools
 */
@ConfigurationProperties("webprotege")
public record ProjectRuntimeProperties(@DefaultValue Project project,
                                       @DefaultValue Events events,
                                       @DefaultValue Kernel kernel) {

    /**
     * @param dormantTime how long a loaded project stays in memory after its last use (legacy
     *                    {@code project.dormant.time})
     */
    public record Project(@DefaultValue("PT1H") Duration dormantTime) {

        public Project {
            checkNotNull(dormantTime, "dormantTime");
            checkArgument(dormantTime.compareTo(Duration.ZERO) > 0,
                          "webprotege.project.dormant-time must be positive: %s", dormantTime);
        }
    }

    /**
     * @param retention how long a project's events can be read back by event tag (legacy: 60 seconds)
     */
    public record Events(@DefaultValue("PT10M") Duration retention) {

        public Events {
            checkNotNull(retention, "retention");
            checkArgument(retention.compareTo(Duration.ZERO) > 0,
                          "webprotege.events.retention must be positive: %s", retention);
        }
    }

    /**
     * @param indexUpdateThreads   threads that update a project's indexes in parallel (legacy: 10)
     * @param revisionWriteThreads threads that append revisions to the change history files of all projects
     */
    public record Kernel(@DefaultValue("10") int indexUpdateThreads,
                         @DefaultValue("4") int revisionWriteThreads) {

        public Kernel {
            checkArgument(indexUpdateThreads > 0,
                          "webprotege.kernel.index-update-threads must be positive: %s", indexUpdateThreads);
            checkArgument(revisionWriteThreads > 0,
                          "webprotege.kernel.revision-write-threads must be positive: %s", revisionWriteThreads);
        }
    }
}
