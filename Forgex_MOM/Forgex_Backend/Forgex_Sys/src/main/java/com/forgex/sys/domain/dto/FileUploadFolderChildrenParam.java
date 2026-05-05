package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * Request body for loading server folder children.
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class FileUploadFolderChildrenParam {

    /**
     * Parent folder path.
     */
    private String path;
}
