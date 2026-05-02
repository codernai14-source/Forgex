package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * Request body for loading server folder children.
 */
@Data
public class FileUploadFolderChildrenParam {

    /**
     * Parent folder path.
     */
    private String path;
}
