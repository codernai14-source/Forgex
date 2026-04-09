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
package com.forgex.sys.domain.param;

import lombok.Data;

import java.util.List;

/**
 * 娑堟伅妯℃澘鎵归噺鍒犻櫎鍙傛暟銆?
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.controller.SysMessageTemplateController
 */
@Data
public class SysMessageTemplateBatchDeleteParam {

    /**
     * 寰呭垹闄ょ殑娑堟伅妯℃澘涓婚敭 ID 鍒楄〃
     */
    private List<Long> ids;

    /** 閺勵垰鎯侀崗顒€鍙￠柊宥囩枂閿涘rue=tenantId=0 */
    private Boolean publicConfig;
}
