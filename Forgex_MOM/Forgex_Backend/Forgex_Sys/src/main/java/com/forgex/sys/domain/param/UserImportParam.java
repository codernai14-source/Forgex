package com.forgex.sys.domain.param;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户导入请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class UserImportParam {

    /**
     * 文件。
     */
    private MultipartFile file;
}
