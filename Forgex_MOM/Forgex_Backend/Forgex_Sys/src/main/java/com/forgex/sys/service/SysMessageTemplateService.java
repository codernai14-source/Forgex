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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.sys.domain.param.SysMessageTemplateParam;
import com.forgex.sys.domain.vo.SysMessageTemplateVO;

/**
 * 娑堟伅妯℃澘鏈嶅姟鎺ュ彛
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public interface SysMessageTemplateService {
    
    /**
     * 鍒嗛〉鏌ヨ娑堟伅妯℃澘
     * 
     * @param param 鏌ヨ鍙傛暟
     * @return 鍒嗛〉缁撴灉
     */
    Page<SysMessageTemplateVO> page(SysMessageTemplateParam param);
    
    /**
     * 鏍规嵁ID鏌ヨ娑堟伅妯℃澘璇︽儏
     * 
     * @param id 妯℃澘ID
     * @return 妯℃澘璇︽儏
     */
    SysMessageTemplateVO getById(Long id, Boolean publicConfig);
    
    /**
     * 淇濆瓨娑堟伅妯℃澘(鏂板鎴栦慨鏀?
     * 
     * @param dto 淇濆瓨鍙傛暟
     * @return 妯℃澘ID
     */
    Long save(SysMessageTemplateSaveDTO dto);
    
    /**
     * 鍒犻櫎娑堟伅妯℃澘
     * 
     * @param id 妯℃澘ID
     * @return 鏄惁鎴愬姛
     */
    boolean delete(Long id, Boolean publicConfig);
    
    /**
     * 鎵归噺鍒犻櫎娑堟伅妯℃澘
     * 
     * @param ids 妯℃澘ID鍒楄〃
     * @return 鏄惁鎴愬姛
     */
    boolean deleteBatch(java.util.List<Long> ids, Boolean publicConfig);
    
    /**
     * 妫€鏌ユā鏉跨紪鐮佹槸鍚﹀瓨鍦?
     * 
     * @param code 妯℃澘缂栫爜
     * @return true=瀛樺湪锛宖alse=涓嶅瓨鍦?
     */
    boolean existsByCode(String code, Boolean publicConfig);
    
    /**
     * 妫€鏌ユā鏉跨紪鐮佹槸鍚﹀瓨鍦紙鎺掗櫎鎸囧畾 ID锛?
     * 
     * @param code 妯℃澘缂栫爜
     * @param id 瑕佹帓闄ょ殑妯℃澘 ID
     * @return true=瀛樺湪锛宖alse=涓嶅瓨鍦?
     */
    boolean existsByCodeExcludeId(String code, Long id, Boolean publicConfig);

    /**
     * 娴犲骸鍙曢崗閬嶅帳缂冾喗濯洪崣鏍ㄧХ閹垱膩閺夊灝鍩岃ぐ鎾冲缁夌喐鍩?     *
     * @return 閺囧瓨鏌婇幋鏍ㄦ煀婢х偟娈戝Ο鈩冩緲閺佷即鍣?     */
    int pullPublicConfig();
}



