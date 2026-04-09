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
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.sys.domain.param.SysMessageTemplateBatchDeleteParam;
import com.forgex.sys.domain.param.SysMessageTemplateIdParam;
import com.forgex.sys.domain.param.SysMessageTemplateParam;
import com.forgex.sys.domain.vo.SysMessageTemplateVO;
import com.forgex.sys.service.SysMessageTemplateService;
import com.forgex.sys.validator.MessageTemplateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 娑堟伅妯℃澘鎺у埗鍣?
 * 
 * 鑱岃矗锛?
 * - 鎺ユ敹 HTTP 璇锋眰
 * - 鍙傛暟鏍￠獙锛堣皟鐢?MessageTemplateValidator锛?
 * - 璋冪敤 Service 灞傛柟娉?
 * - 杩斿洖鍝嶅簲缁撴灉
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @see SysMessageTemplateService
 * @see MessageTemplateValidator
 */
@RestController
@RequestMapping("/sys/message-template")
@RequiredArgsConstructor
public class SysMessageTemplateController {
    
    private final SysMessageTemplateService messageTemplateService;
    private final MessageTemplateValidator messageTemplateValidator;
    
    /**
     * 鍒嗛〉鏌ヨ娑堟伅妯℃澘
     * <p>
     * 鎺ュ彛璺緞锛歅OST /sys/message-template/page
     * 闇€瑕佹潈闄愶細sys:messageTemplate:query
     * </p>
     * <p>鎵ц姝ラ锛?/p>
     * <ol>
     *   <li>鎺ユ敹鏌ヨ鍙傛暟锛堝寘鍚?pageNum銆乸ageSize 鍜岀瓫閫夋潯浠讹級</li>
     *   <li>璋冪敤 Service 灞傚垎椤垫煡璇㈡秷鎭ā鏉?VO 鍒楄〃</li>
     *   <li>杩斿洖鍒嗛〉缁撴灉</li>
     * </ol>
     *
     * @param param 鏌ヨ鍙傛暟
     *              - pageNum: 椤电爜锛堝繀濉紝浠?1 寮€濮嬶級
     *              - pageSize: 姣忛〉澶у皬锛堝繀濉級
     *              - templateName: 妯℃澘鍚嶇О锛堝彲閫夛紝妯＄硦鏌ヨ锛?
     *              - templateCode: 妯℃澘缂栫爜锛堝彲閫夛紝绮剧‘鏌ヨ锛?
     *              - templateType: 妯℃澘绫诲瀷锛堝彲閫夛級
     * @return {@link R} 鍖呭惈娑堟伅妯℃澘鍒嗛〉鍒楄〃鐨勭粺涓€杩斿洖缁撴瀯
     *         - code: 鐘舵€佺爜锛?00=鎴愬姛锛?
     *         - data: 鍒嗛〉缁撴灉锛圥age&lt;SysMessageTemplateVO&gt;锛?
     *         - message: 鎻愮ず淇℃伅
     * @see SysMessageTemplateParam
     * @see SysMessageTemplateVO
     */
    @PostMapping("/page")
    public R<Page<SysMessageTemplateVO>> page(@RequestBody SysMessageTemplateParam param) {
        // 濮旀淳缁?Service 灞傚垎椤垫煡璇㈡秷鎭ā鏉?
        return R.ok(messageTemplateService.page(param));
    }
    
    /**
     * 鏍规嵁 ID 鏌ヨ娑堟伅妯℃澘璇︽儏
     * <p>
     * 鎺ュ彛璺緞锛歅OST /sys/message-template/get
     * 闇€瑕佹潈闄愶細sys:messageTemplate:query
     * </p>
     * <p>鎵ц姝ラ锛?/p>
     * <ol>
     *   <li>鎺ユ敹鍙傛暟锛堝寘鍚ā鏉?ID锛?/li>
     *   <li>璋冪敤 Validator 鏍￠獙 ID 鍚堟硶鎬?/li>
     *   <li>璋冪敤 Service 灞傝幏鍙栨ā鏉胯鎯?/li>
     *   <li>杩斿洖妯℃澘璇︽儏</li>
     * </ol>
     *
     * @param param 璇锋眰鍙傛暟
     *              - id: 妯℃澘 ID锛堝繀濉級
     * @return {@link R} 鍖呭惈娑堟伅妯℃澘璇︽儏鐨勭粺涓€杩斿洖缁撴瀯
     *         - code: 鐘舵€佺爜锛?00=鎴愬姛锛?
     *         - data: 妯℃澘璇︽儏锛圫ysMessageTemplateVO锛?
     *         - message: 鎻愮ず淇℃伅
     * @throws BusinessException 鍙傛暟鏍￠獙澶辫触鏃舵姏鍑?
     * @see SysMessageTemplateIdParam
     * @see MessageTemplateValidator#validateId(Long)
     */
    @PostMapping("/get")
    public R<SysMessageTemplateVO> getById(@RequestBody @Validated SysMessageTemplateIdParam param) {
        // 1. 璋冪敤 Validator 鏍￠獙 ID 鍚堟硶鎬?
        messageTemplateValidator.validateId(param.getId());
        // 2. 濮旀淳缁?Service 灞傝幏鍙栨ā鏉胯鎯?
        return R.ok(messageTemplateService.getById(param.getId(), param.getPublicConfig()));
    }
    
    /**
     * 淇濆瓨娑堟伅妯℃澘锛堟柊澧炴垨淇敼锛?
     * <p>
     * 鎺ュ彛璺緞锛歅OST /sys/message-template/save
     * 闇€瑕佹潈闄愶細sys:messageTemplate:add锛堟柊澧烇級鎴?sys:messageTemplate:edit锛堜慨鏀癸級
     * </p>
     * <p>鎵ц姝ラ锛?/p>
     * <ol>
     *   <li>鎺ユ敹妯℃澘淇濆瓨鍙傛暟</li>
     *   <li>璋冪敤 Validator 鏍￠獙鏁版嵁锛堟ā鏉跨紪鐮佸敮涓€鎬с€佸繀濉瓧娈电瓑锛?/li>
     *   <li>璋冪敤 Service 灞備繚瀛樻ā鏉?/li>
     *   <li>杩斿洖淇濆瓨鎴愬姛鐨勬ā鏉?ID</li>
     * </ol>
     *
     * @param dto 妯℃澘淇濆瓨鍙傛暟
     *            - id: 妯℃澘 ID锛堜慨鏀规椂蹇呭～锛屾柊澧炴椂涓嶅～锛?
     *            - templateName: 妯℃澘鍚嶇О锛堝繀濉級
     *            - templateCode: 妯℃澘缂栫爜锛堝繀濉級
     *            - templateType: 妯℃澘绫诲瀷锛堝繀濉級
     *            - templateContent: 妯℃澘鍐呭锛堝繀濉級
     *            - paramExample: 鍙傛暟绀轰緥锛堝彲閫夛級
     *            - remark: 澶囨敞锛堝彲閫夛級
     * @return {@link R} 鎿嶄綔缁撴灉
     *         - code: 鐘舵€佺爜锛?00=鎴愬姛锛?
     *         - data: 淇濆瓨鎴愬姛鐨勬ā鏉?ID
     *         - message: 鎻愮ず淇℃伅
     * @throws BusinessException 鍙傛暟鏍￠獙澶辫触鎴栨ā鏉跨紪鐮佸凡瀛樺湪鏃舵姏鍑?
     * @see SysMessageTemplateSaveDTO
     * @see MessageTemplateValidator#validateForSave(SysMessageTemplateSaveDTO)
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody @Validated SysMessageTemplateSaveDTO dto) {
        // 1. 璋冪敤 Validator 鏍￠獙鏁版嵁
        messageTemplateValidator.validateForSave(dto);
        // 2. 濮旀淳缁?Service 灞備繚瀛樻ā鏉?
        return R.ok(messageTemplateService.save(dto));
    }
    
    /**
     * 鍒犻櫎娑堟伅妯℃澘
     * <p>
     * 鎺ュ彛璺緞锛歅OST /sys/message-template/delete
     * 闇€瑕佹潈闄愶細sys:messageTemplate:delete
     * </p>
     * <p>鎵ц姝ラ锛?/p>
     * <ol>
     *   <li>鎺ユ敹鍙傛暟锛堝寘鍚ā鏉?ID锛?/li>
     *   <li>璋冪敤 Validator 鏍￠獙 ID 鍚堟硶鎬?/li>
     *   <li>璋冪敤 Service 灞傚垹闄ゆā鏉?/li>
     *   <li>杩斿洖鍒犻櫎缁撴灉</li>
     * </ol>
     *
     * @param param 璇锋眰鍙傛暟
     *              - id: 妯℃澘 ID锛堝繀濉級
     * @return {@link R} 鎿嶄綔缁撴灉
     *         - code: 鐘舵€佺爜锛?00=鎴愬姛锛?
     *         - data: 鍒犻櫎缁撴灉锛坱rue=鎴愬姛锛宖alse=澶辫触锛?
     *         - message: 鎻愮ず淇℃伅
     * @throws BusinessException 鍙傛暟鏍￠獙澶辫触鏃舵姏鍑?
     * @see SysMessageTemplateIdParam
     * @see MessageTemplateValidator#validateId(Long)
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody @Validated SysMessageTemplateIdParam param) {
        // 1. 璋冪敤 Validator 鏍￠獙 ID 鍚堟硶鎬?
        messageTemplateValidator.validateId(param.getId());
        // 2. 濮旀淳缁?Service 灞傚垹闄ゆā鏉?
        return R.ok(messageTemplateService.delete(param.getId(), param.getPublicConfig()));
    }
    
    /**
     * 鎵归噺鍒犻櫎娑堟伅妯℃澘
     * <p>
     * 鎺ュ彛璺緞锛歅OST /sys/message-template/delete-batch
     * 闇€瑕佹潈闄愶細sys:messageTemplate:delete
     * </p>
     * <p>鎵ц姝ラ锛?/p>
     * <ol>
     *   <li>鎺ユ敹鍙傛暟锛堝寘鍚ā鏉?ID 鍒楄〃锛?/li>
     *   <li>璋冪敤 Validator 鏍￠獙 ID 鍒楄〃鍚堟硶鎬?/li>
     *   <li>璋冪敤 Service 灞傛壒閲忓垹闄ゆā鏉?/li>
     *   <li>杩斿洖鍒犻櫎缁撴灉</li>
     * </ol>
     *
     * @param param 璇锋眰鍙傛暟
     *              - ids: 妯℃澘 ID 鍒楄〃锛堝繀濉紝涓嶈兘涓虹┖锛?
     * @return {@link R} 鎿嶄綔缁撴灉
     *         - code: 鐘舵€佺爜锛?00=鎴愬姛锛?
     *         - data: 鍒犻櫎缁撴灉锛坱rue=鎴愬姛锛宖alse=澶辫触锛?
     *         - message: 鎻愮ず淇℃伅
     * @throws BusinessException 鍙傛暟鏍￠獙澶辫触鏃舵姏鍑?
     * @see SysMessageTemplateBatchDeleteParam
     * @see MessageTemplateValidator#validateBatchIds(java.util.List)
     */
    @PostMapping("/delete-batch")
    public R<Boolean> deleteBatch(@RequestBody @Validated SysMessageTemplateBatchDeleteParam param) {
        // 1. 璋冪敤 Validator 鏍￠獙 ID 鍒楄〃鍚堟硶鎬?
        messageTemplateValidator.validateBatchIds(param.getIds());
        // 2. 濮旀淳缁?Service 灞傛壒閲忓垹闄ゆā鏉?
        return R.ok(messageTemplateService.deleteBatch(param.getIds(), param.getPublicConfig()));
    }

    @PostMapping("/pull-public")
    public R<Integer> pullPublicConfig() {
        return R.ok(messageTemplateService.pullPublicConfig());
    }
}
