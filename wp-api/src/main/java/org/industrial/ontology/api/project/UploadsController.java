package org.industrial.ontology.api.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.project.UploadService;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Uploads of ontology documents to create projects from (docs/02 §3), replacing the legacy {@code /submitfile}
 * servlet.
 */
@RestController
@RequestMapping("/api/v1/uploads")
@Tag(name = "Uploads", description = "Ontology documents to create projects from")
public class UploadsController {

    private final UploadService uploadService;

    public UploadsController(UploadService uploadService) {
        this.uploadService = checkNotNull(uploadService);
    }

    @Operation(summary = "Stores an ontology document or a zip archive of documents (permission UploadProject)",
               description = "The multipart field is 'file'. Pass the returned documentId as sourceDocumentId to "
                       + "POST /api/v1/projects; the document is deleted once a project has been created from it.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadDto> upload(@Caller UserId caller,
                                            @RequestParam("file") MultipartFile file) throws IOException {
        try (var content = file.getInputStream()) {
            var uploaded = uploadService.upload(caller, file.getOriginalFilename(), content);
            return ResponseEntity.status(HttpStatus.CREATED)
                                 .body(new UploadDto(uploaded.documentId().getDocumentId(),
                                                     uploaded.fileName(),
                                                     uploaded.size()));
        }
    }

    /**
     * An uploaded file; {@code size} is in bytes.
     */
    public record UploadDto(String documentId, String fileName, long size) {
    }
}
