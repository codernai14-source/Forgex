package com.forgex.sys.domain.param;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserImportParam {

    private MultipartFile file;
}
