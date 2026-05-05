package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * Server folder node used by file upload configuration.
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class FileUploadFolderNodeDTO {

    /**
     * Display name.
     */
    private String name;

    /**
     * Absolute folder path on the backend server.
     */
    private String path;

    /**
     * Whether the backend process can write to the folder.
     */
    private boolean writable;

    /**
     * Whether the node may have child folders.
     */
    private boolean leaf;
}
