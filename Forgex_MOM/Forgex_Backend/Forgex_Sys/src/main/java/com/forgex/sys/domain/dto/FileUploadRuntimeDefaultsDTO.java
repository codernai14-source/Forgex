package com.forgex.sys.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime defaults for local file upload public access.
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class FileUploadRuntimeDefaultsDTO {

    /**
     * Backend server IP candidates.
     */
    private List<String> ipCandidates = new ArrayList<>();

    /**
     * Recommended public base URL, including gateway port and /api prefix.
     */
    private String recommendedPublicBaseUrl = "";

    /**
     * Recommended local access prefix.
     */
    private String accessPrefix = "/files";

    /**
     * Full URL example for previewing an uploaded file.
     */
    private String previewExample = "";
}
