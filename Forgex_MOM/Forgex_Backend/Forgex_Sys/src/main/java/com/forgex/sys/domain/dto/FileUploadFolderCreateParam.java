package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * Request body for creating a server folder.
 */
@Data
public class FileUploadFolderCreateParam {

    /**
     * Parent folder path.
     */
    private String parentPath;

    /**
     * Folder name to create under parentPath.
     */
    private String folderName;
}
