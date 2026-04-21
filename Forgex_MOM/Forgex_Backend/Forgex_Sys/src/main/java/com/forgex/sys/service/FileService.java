/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * File service interface.
 */
public interface FileService {

    /**
     * Upload file and return its public access URL.
     */
    String upload(MultipartFile file, String moduleCode, String moduleName) throws IOException;

    /**
     * Resolve file resource by relative path.
     */
    Resource getFile(String filename) throws IOException;

    /**
     * Resolve base upload directory.
     */
    Path getBaseDir();

    /**
     * Resolve media type for a file.
     */
    String getMediaType(String filename) throws IOException;
}
