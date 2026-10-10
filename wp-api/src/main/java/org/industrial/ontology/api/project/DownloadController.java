package org.industrial.ontology.api.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectDownload;
import org.industrial.ontology.app.project.ProjectDownloadService;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.download.ProjectDownloadConstants;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.kernel.io.download.DownloadFormat;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Project downloads (docs/02 §3): a zip archive of the project's ontologies at a revision, in one of the five
 * formats ({@code owl}, {@code ttl}, {@code owx}, {@code omn}, {@code ofn}).
 * <p>
 * {@code /download} keeps the legacy servlet's parameters and their leniency: a missing or malformed revision means
 * the head revision and an unknown format means RDF/XML. It is the only path that also accepts {@code ?apiKey=}
 * (docs/01 §6), for download links. The new path is strict about both.
 */
@RestController
@Tag(name = "Downloads", description = "Project downloads")
public class DownloadController {

    private static final MediaType ZIP = MediaType.parseMediaType("application/zip");

    private final ProjectDownloadService downloadService;

    public DownloadController(ProjectDownloadService downloadService) {
        this.downloadService = checkNotNull(downloadService);
    }

    @Operation(summary = "Downloads a revision of a project (permission DownloadProject), legacy parameters",
               description = "?project=<id>&revision=<n>&format=owl|ttl|owx|omn|ofn")
    @GetMapping("/download")
    public ResponseEntity<Resource> legacyDownload(
            @Caller UserId caller,
            @RequestParam(name = ProjectDownloadConstants.PROJECT, required = false) String project,
            @RequestParam(name = ProjectDownloadConstants.REVISION, required = false) String revision,
            @RequestParam(name = ProjectDownloadConstants.FORMAT, required = false) String format) {
        if (project == null || !ProjectId.isWelFormedProjectId(project)) {
            throw WpException.invalidRequest("project needs a project id");
        }
        return send(downloadService.download(caller,
                                             ProjectId.get(project),
                                             parseLegacyRevision(revision),
                                             DownloadFormat.getDownloadFormatFromParameterName(format)));
    }

    @Operation(summary = "Downloads a revision of a project (permission DownloadProject)",
               description = "revision is a revision number or HEAD (the default); format is owl (the default), "
                       + "ttl, owx, omn or ofn.")
    @GetMapping("/api/v1/projects/{projectId}/download")
    public ResponseEntity<Resource> download(@Caller UserId caller,
                                             @PathVariable String projectId,
                                             @RequestParam(required = false) String revision,
                                             @RequestParam(defaultValue = "owl") String format) {
        return send(downloadService.download(caller,
                                             ProjectIds.parse(projectId),
                                             parseRevision(revision),
                                             parseFormat(format)));
    }

    private static ResponseEntity<Resource> send(ProjectDownload download) {
        long size;
        try {
            size = Files.size(download.file());
        } catch (IOException e) {
            throw new IllegalStateException("The created download " + download.file() + " cannot be read", e);
        }
        var disposition = ContentDisposition.attachment()
                                            .filename(download.fileName(), StandardCharsets.UTF_8)
                                            .build();
        return ResponseEntity.ok()
                             .contentType(ZIP)
                             .contentLength(size)
                             .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                             .body(new FileSystemResource(download.file()));
    }

    /**
     * As the legacy {@code FileDownloadParameters.getRequestedRevision}.
     */
    private static RevisionNumber parseLegacyRevision(@Nullable String revision) {
        if (revision == null) {
            return RevisionNumber.getHeadRevisionNumber();
        }
        try {
            return RevisionNumber.getRevisionNumber(Long.parseLong(revision));
        } catch (NumberFormatException e) {
            return RevisionNumber.getHeadRevisionNumber();
        }
    }

    private static RevisionNumber parseRevision(@Nullable String revision) {
        if (revision == null || revision.isBlank()) {
            return RevisionNumber.getHeadRevisionNumber();
        }
        try {
            return RevisionNumber.valueOf(revision);
        } catch (NumberFormatException e) {
            throw WpException.invalidRequest("revision is a revision number or HEAD");
        }
    }

    private static DownloadFormat parseFormat(String format) {
        return Arrays.stream(DownloadFormat.values())
                     .filter(candidate -> candidate.getExtension().equals(format))
                     .findFirst()
                     .orElseThrow(() -> WpException.invalidRequest("format is one of owl, ttl, owx, omn and ofn"));
    }
}
