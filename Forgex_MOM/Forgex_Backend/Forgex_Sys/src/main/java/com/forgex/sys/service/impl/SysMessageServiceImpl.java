package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.dto.SysMessageSendDTO;
import com.forgex.sys.domain.entity.SysMessage;
import com.forgex.sys.domain.entity.SysTenantMessageWhitelist;
import com.forgex.sys.domain.param.SysMessageParam;
import com.forgex.sys.domain.vo.SysMessageVO;
import com.forgex.sys.mapper.SysMessageMapper;
import com.forgex.sys.mapper.SysTenantMessageWhitelistMapper;
import com.forgex.sys.service.SseEmitterService;
import com.forgex.sys.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 绯荤粺娑堟伅鏈嶅姟瀹炵幇绫?
 * <p>
 * 鎻愪緵绯荤粺娑堟伅鐨勫彂閫併€佹煡璇㈠拰鏍囪宸茶鍔熻兘銆?
 * 鏀寔鍐呴儴娑堟伅锛堝悓涓€绉熸埛锛夊拰澶栭儴娑堟伅锛堜笉鍚岀鎴凤級銆?
 * </p>
 * <p><strong>涓昏鍔熻兘锛?/strong></p>
 * <ul>
 *   <li>鍙戦€佺郴缁熸秷鎭紝鏀寔鍐呴儴鍜屽閮ㄦ秷鎭?/li>
 *   <li>閫氳繃SSE瀹炴椂鎺ㄩ€佹秷鎭粰鎺ユ敹鐢ㄦ埛</li>
 *   <li>鏌ヨ鏈娑堟伅鍒楄〃</li>
 *   <li>鏍囪娑堟伅涓哄凡璇荤姸鎬?/li>
 *   <li>璺ㄧ鎴锋秷鎭潈闄愭牎楠?/li>
 * </ul>
 * <p><strong>娑堟伅绫诲瀷锛?/strong></p>
 * <ul>
 *   <li>{@code INTERNAL} - 鍐呴儴娑堟伅锛屽彂閫佽€呭拰鎺ユ敹鑰呭湪鍚屼竴绉熸埛</li>
 *   <li>{@code EXTERNAL} - 澶栭儴娑堟伅锛屽彂閫佽€呭拰鎺ユ敹鑰呭湪涓嶅悓绉熸埛锛堥渶瑕佺櫧鍚嶅崟鎺堟潈锛?/li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysMessageService
 * @see SysMessage
 * @see SysMessageVO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl implements SysMessageService {

    /**
     * 绔欏唴浜哄伐鍙戦€佹秷鎭椂锛岃〃瀛楁 {@code message_type} 闈炵┖涓旀棤搴撻粯璁ゅ€兼椂鐨勯粯璁ょ被鍨嬶細閫氱煡銆?
     *
     * @see com.forgex.sys.domain.entity.SysMessage#getMessageType()
     */
    private static final String DEFAULT_MESSAGE_TYPE = "NOTICE";

    /**
     * 绔欏唴娑堟伅榛樿娓犻亾锛氱珯鍐咃紙涓?{@code scope=INTERNAL} 璇箟涓€鑷达紝鍧囦负绔欏唴鍦烘櫙锛夈€?
     *
     * @see com.forgex.sys.domain.entity.SysMessage#getPlatform()
     */
    private static final String DEFAULT_PLATFORM = "INTERNAL";

    /**
     * 娑堟伅Mapper
     */
    private final SysMessageMapper messageMapper;

    /**
     * SSE鎺ㄩ€佹湇鍔?
     */
    private final SseEmitterService sseEmitterService;
    
    /**
     * 绉熸埛娑堟伅鐧藉悕鍗昅apper
     */
    private final SysTenantMessageWhitelistMapper whitelistMapper;

    /**
     * 鍙戦€佺郴缁熸秷鎭?
     * <p>
     * 鍒涘缓绯荤粺娑堟伅骞堕€氳繃SSE瀹炴椂鎺ㄩ€佺粰鎺ユ敹鐢ㄦ埛銆?
     * 鏀寔鍐呴儴娑堟伅锛堝悓涓€绉熸埛锛夊拰澶栭儴娑堟伅锛堜笉鍚岀鎴凤級銆?
     * </p>
     * <p><strong>鎵ц娴佺▼锛?/strong></p>
     * <ol>
     *   <li>鍙傛暟鏍￠獙锛欴TO銆佺鎴稩D銆佺敤鎴稩D銆佹帴鏀剁敤鎴稩D銆佹爣棰樹笉鑳戒负绌?/li>
     *   <li>纭畾鎺ユ敹绉熸埛ID锛氬鏋滄湭鎸囧畾锛屼娇鐢ㄥ彂閫佺鎴稩D</li>
     *   <li>纭畾娑堟伅鑼冨洿锛氬鏋滄湭鎸囧畾锛屾牴鎹鎴峰叧绯昏嚜鍔ㄥ垽鏂?/li>
     *   <li>濡傛灉鏄唴閮ㄦ秷鎭紝寮哄埗浣跨敤鍙戦€佺鎴稩D浣滀负鎺ユ敹绉熸埛ID</li>
     *   <li>鏋勫缓娑堟伅瀹炰綋锛岃缃墍鏈夊瓧娈?/li>
     *   <li>鍒囨崲绉熸埛涓婁笅鏂囷紝鎻掑叆娑堟伅鍒版暟鎹簱</li>
     *   <li>鎭㈠鍘熷绉熸埛涓婁笅鏂?/li>
     *   <li>鏋勫缓娑堟伅VO锛岄€氳繃SSE鎺ㄩ€佺粰鎺ユ敹鐢ㄦ埛</li>
     *   <li>杩斿洖娑堟伅ID</li>
     * </ol>
     * 
     * @param dto 娑堟伅鍙戦€丏TO
     * @return 娑堟伅ID锛屽弬鏁版棤鏁堟垨鍙戦€佸け璐ヨ繑鍥瀗ull
     */
    @Override
    public Long send(SysMessageSendDTO dto) {
        // 鍙傛暟鏍￠獙锛欴TO涓嶈兘涓虹┖
        if (dto == null) {
            return null;
        }
        
        // 鑾峰彇鍙戦€佽€呯鎴稩D
        Long senderTenantId = TenantContext.get();
        
        // 鑾峰彇鍙戦€佽€呯敤鎴稩D
        Long senderUserId = UserContext.get();
        
        // 鍙戦€佽€呬俊鎭负绌猴紝鐩存帴杩斿洖
        if (senderTenantId == null || senderUserId == null) {
            return null;
        }
        
        // 鎺ユ敹鐢ㄦ埛ID涓虹┖锛岀洿鎺ヨ繑鍥?
        if (dto.getReceiverUserId() == null) {
            return null;
        }
        
        // 鏍囬涓虹┖锛岀洿鎺ヨ繑鍥?
        if (!StringUtils.hasText(dto.getTitle())) {
            return null;
        }

        // 鑾峰彇鎺ユ敹绉熸埛ID
        Long receiverTenantId = dto.getReceiverTenantId();
        
        // 鎺ユ敹绉熸埛ID鏈寚瀹氾紝浣跨敤鍙戦€佺鎴稩D
        if (receiverTenantId == null) {
            receiverTenantId = senderTenantId;
        }

        // 纭畾娑堟伅鑼冨洿
        String scope = StringUtils.hasText(dto.getScope()) ? dto.getScope().trim().toUpperCase() : null;
        
        // 娑堟伅鑼冨洿鏈寚瀹氾紝鏍规嵁绉熸埛鍏崇郴鑷姩鍒ゆ柇
        if (!StringUtils.hasText(scope)) {
            scope = receiverTenantId.equals(senderTenantId) ? "INTERNAL" : "EXTERNAL";
        }
        
        // 濡傛灉鏄唴閮ㄦ秷鎭紝寮哄埗浣跨敤鍙戦€佺鎴稩D浣滀负鎺ユ敹绉熸埛ID
        if ("INTERNAL".equals(scope)) {
            receiverTenantId = senderTenantId;
        } else if ("EXTERNAL".equals(scope)) {
            // 澶栭儴娑堟伅闇€瑕佹牎楠岃法绉熸埛鏉冮檺
            if (!receiverTenantId.equals(senderTenantId)) {
                // 妫€鏌ユ槸鍚︽湁璺ㄧ鎴锋秷鎭彂閫佹潈闄?
                if (!checkCrossTenantPermission(senderTenantId, receiverTenantId)) {
                    log.warn("璺ㄧ鎴锋秷鎭彂閫佽鎷掔粷: senderTenantId={}, receiverTenantId={}", 
                            senderTenantId, receiverTenantId);
                    throw new BusinessException("No permission to send cross-tenant message. Please configure tenant message whitelist.");
                }
            }
        }

        // 鏋勫缓娑堟伅瀹炰綋
        SysMessage msg = new SysMessage();
        msg.setTenantId(receiverTenantId);
        msg.setSenderTenantId(senderTenantId);
        msg.setSenderUserId(senderUserId);
        // sender_name 琛ㄥ瓧娈甸潪绌轰笖鏃犻粯璁ゅ€硷紝鎻掑叆鏃跺繀椤昏祴鍊硷紙瑙佸疄浣?SysMessage#senderName 璇存槑锛?
        msg.setSenderName(resolveSenderName(senderUserId));
        msg.setReceiverUserId(dto.getReceiverUserId());
        msg.setScope(scope);
        // message_type銆乸latform 琛ㄥ瓧娈甸潪绌轰笖鏃犻粯璁ゅ€硷紝椤绘樉寮忓啓鍏ワ紱鏈紶鏃朵娇鐢ㄧ珯鍐呴€氱煡榛樿鍊?
        msg.setMessageType(resolveMessageType(dto.getMessageType()));
        msg.setPlatform(resolvePlatform(dto.getPlatform()));
        msg.setTitle(dto.getTitle());
        msg.setContent(dto.getContent());
        msg.setLinkUrl(dto.getLinkUrl());
        msg.setBizType(dto.getBizType());
        msg.setStatus(0);
        msg.setDeleted(false);

        // 淇濆瓨鍘熷绉熸埛ID
        Long originTenantId = TenantContext.get();
        
        try {
            // 鍒囨崲绉熸埛涓婁笅鏂囦负鎺ユ敹绉熸埛
            TenantContext.set(receiverTenantId);
            
            // 鎻掑叆娑堟伅鍒版暟鎹簱
            messageMapper.insert(msg);
        } finally {
            // 鎭㈠鍘熷绉熸埛涓婁笅鏂?
            TenantContext.set(originTenantId);
        }

        // 鏋勫缓娑堟伅VO
        SysMessageVO push = toVO(msg, receiverTenantId);
        
        // 閫氳繃SSE鎺ㄩ€佺粰鎺ユ敹鐢ㄦ埛
        sseEmitterService.sendToUser(receiverTenantId, msg.getReceiverUserId(), "message", push);

        // 杩斿洖娑堟伅ID
        return msg.getId();
    }

    /**
     * 鏌ヨ鏈娑堟伅鍒楄〃
     * <p>
     * 鏌ヨ褰撳墠鐢ㄦ埛鐨勬湭璇绘秷鎭紝鎸夊垱寤烘椂闂村€掑簭鎺掑垪銆?
     * </p>
     * <p><strong>鎵ц娴佺▼锛?/strong></p>
     * <ol>
     *   <li>鑾峰彇褰撳墠绉熸埛ID鍜岀敤鎴稩D</li>
     *   <li>鍙傛暟鏃犳晥鏃惰繑鍥炵┖鍒楄〃</li>
     *   <li>闄愬埗鏌ヨ鏁伴噺锛氶粯璁?0锛屾渶澶?00</li>
     *   <li>鏌ヨ鏈娑堟伅锛坰tatus=0锛夛紝鎸夊垱寤烘椂闂村€掑簭</li>
     *   <li>灏嗘秷鎭疄浣撹浆鎹负VO</li>
     *   <li>杩斿洖VO鍒楄〃</li>
     * </ol>
     * 
     * @param limit 鏈€澶ц繑鍥炴暟閲忥紝null鎴?=0鏃朵娇鐢ㄩ粯璁ゅ€?0
     * @return 鏈娑堟伅鍒楄〃
     */
    @Override
    public List<SysMessageVO> listUnread(Integer limit) {
        // 鑾峰彇褰撳墠绉熸埛ID
        Long tenantId = TenantContext.get();
        
        // 鑾峰彇褰撳墠鐢ㄦ埛ID
        Long userId = UserContext.get();
        
        // 鍙傛暟鏃犳晥锛岃繑鍥炵┖鍒楄〃
        if (tenantId == null || userId == null) {
            return List.of();
        }
        
        // 闄愬埗鏌ヨ鏁伴噺锛氶粯璁?0锛屾渶澶?00
        int l = limit == null || limit <= 0 ? 20 : Math.min(limit, 200);
        
        // 鏌ヨ鏈娑堟伅锛坰tatus=0锛夛紝鎸夊垱寤烘椂闂村€掑簭
        List<SysMessage> list = messageMapper.selectList(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverUserId, userId)
                .eq(SysMessage::getStatus, 0)
                .orderByDesc(SysMessage::getCreateTime)
                .last("limit " + l));
        
        // 鏌ヨ缁撴灉涓虹┖锛岃繑鍥炵┖鍒楄〃
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        
        // 灏嗘秷鎭疄浣撹浆鎹负VO
        List<SysMessageVO> out = new ArrayList<>(list.size());
        for (SysMessage m : list) {
            out.add(toVO(m, tenantId));
        }
        
        // 杩斿洖VO鍒楄〃
        return out;
    }

    /**
     * 鑾峰彇鏈娑堟伅鏁伴噺
     * 
     * @return 鏈娑堟伅鏁伴噺
     */
    @Override
    public Long getUnreadCount() {
        // 鑾峰彇褰撳墠绉熸埛ID
        Long tenantId = TenantContext.get();
        
        // 鑾峰彇褰撳墠鐢ㄦ埛ID
        Long userId = UserContext.get();
        
        // 鍙傛暟鏃犳晥锛岃繑鍥?
        if (tenantId == null || userId == null) {
            return 0L;
        }
        
        // 鏌ヨ鏈娑堟伅鏁伴噺
        return messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverUserId, userId)
                .eq(SysMessage::getStatus, 0));
    }

    /**
     * 鏍囪娑堟伅宸茶
     * <p>
     * 灏嗘寚瀹氭秷鎭爣璁颁负宸茶鐘舵€侊紝骞惰褰曢槄璇绘椂闂淬€?
     * </p>
     * <p><strong>鎵ц娴佺▼锛?/strong></p>
     * <ol>
     *   <li>鍙傛暟鏍￠獙锛氭秷鎭疘D涓嶈兘涓虹┖</li>
     *   <li>鑾峰彇褰撳墠绉熸埛ID鍜岀敤鎴稩D</li>
     *   <li>鍙傛暟鏃犳晥鏃惰繑鍥瀎alse</li>
     *   <li>鏇存柊娑堟伅鐘舵€佷负宸茶锛坰tatus=1锛?/li>
     *   <li>璁剧疆闃呰鏃堕棿涓哄綋鍓嶆椂闂?/li>
     *   <li>杩斿洖鏇存柊缁撴灉锛堝奖鍝嶈鏁?0琛ㄧず鎴愬姛锛?/li>
     * </ol>
     * 
     * @param id 娑堟伅ID
     * @return true琛ㄧず鏍囪鎴愬姛锛宖alse琛ㄧず鏍囪澶辫触
     */
    @Override
    public boolean markRead(Long id) {
        // 鍙傛暟鏍￠獙锛氭秷鎭疘D涓嶈兘涓虹┖
        if (id == null) {
            return false;
        }
        
        // 鑾峰彇褰撳墠绉熸埛ID
        Long tenantId = TenantContext.get();
        
        // 鑾峰彇褰撳墠鐢ㄦ埛ID
        Long userId = UserContext.get();
        
        // 鍙傛暟鏃犳晥锛岃繑鍥瀎alse
        if (tenantId == null || userId == null) {
            return false;
        }
        
        // 鏇存柊娑堟伅鐘舵€佷负宸茶锛坰tatus=1锛夛紝璁剧疆闃呰鏃堕棿
        int rows = messageMapper.update(
                null,
                new LambdaUpdateWrapper<SysMessage>()
                        .eq(SysMessage::getId, id)
                        .eq(SysMessage::getReceiverUserId, userId)
                        .eq(SysMessage::getStatus, 0)
                        .set(SysMessage::getStatus, 1)
                        .set(SysMessage::getReadTime, LocalDateTime.now())
        );
        
        // 杩斿洖鏇存柊缁撴灉锛堝奖鍝嶈鏁?0琛ㄧず鎴愬姛锛?
        return rows > 0;
    }
    
    /**
     * 鏍囪鎵€鏈夋秷鎭凡璇?
     * 
     * @return true琛ㄧず鏍囪鎴愬姛锛宖alse琛ㄧず鏍囪澶辫触
     */
    @Override
    public boolean markAllRead() {
        // 鑾峰彇褰撳墠绉熸埛ID
        Long tenantId = TenantContext.get();
        
        // 鑾峰彇褰撳墠鐢ㄦ埛ID
        Long userId = UserContext.get();
        
        // 鍙傛暟鏃犳晥锛岃繑鍥瀎alse
        if (tenantId == null || userId == null) {
            return false;
        }
        
        // 鏇存柊鎵€鏈夋湭璇绘秷鎭负宸茶
        messageMapper.update(
                null,
                new LambdaUpdateWrapper<SysMessage>()
                        .eq(SysMessage::getReceiverUserId, userId)
                        .eq(SysMessage::getStatus, 0)
                        .set(SysMessage::getStatus, 1)
                        .set(SysMessage::getReadTime, LocalDateTime.now())
        );
        
        return true;
    }
    
    /**
     * 鍒嗛〉鏌ヨ娑堟伅鍒楄〃
     * 
     * @param param 鏌ヨ鍙傛暟
     * @return 鍒嗛〉缁撴灉
     */
    @Override
    public Page<SysMessageVO> page(SysMessageParam param) {
        // 鑾峰彇褰撳墠绉熸埛ID
        Long tenantId = TenantContext.get();
        
        // 鑾峰彇褰撳墠鐢ㄦ埛ID
        Long userId = UserContext.get();
        
        // 鍙傛暟鏃犳晥锛岃繑鍥炵┖鍒嗛〉
        if (tenantId == null || userId == null) {
            return new Page<>(param.getPageNum(), param.getPageSize(), 0);
        }
        
        // 鏋勫缓鏌ヨ鏉′欢
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverUserId, userId)
               .eq(StringUtils.hasText(param.getMessageType()), 
                   SysMessage::getMessageType, param.getMessageType())
               .eq(StringUtils.hasText(param.getPlatform()), 
                   SysMessage::getPlatform, param.getPlatform())
               .eq(param.getStatus() != null, 
                   SysMessage::getStatus, param.getStatus())
               .like(StringUtils.hasText(param.getTitle()), 
                     SysMessage::getTitle, param.getTitle())
               .orderByDesc(SysMessage::getCreateTime);
        
        // 鍒嗛〉鏌ヨ
        Page<SysMessage> page = new Page<>(param.getPageNum(), param.getPageSize());
        Page<SysMessage> result = messageMapper.selectPage(page, wrapper);
        
        // 杞崲涓篤O
        Page<SysMessageVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SysMessageVO> voList = result.getRecords().stream()
                .map(m -> toVO(m, tenantId))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 灏嗘秷鎭疄浣撹浆鎹负VO
     * <p>
     * 澶嶅埗娑堟伅瀹炰綋鐨勬墍鏈夊瓧娈靛埌VO瀵硅薄銆?
     * </p>
     * 
     * @param msg 娑堟伅瀹炰綋
     * @param receiverTenantId 鎺ユ敹绉熸埛ID
     * @return 娑堟伅VO
     */
    /**
     * 瑙ｆ瀽鍙戦€佷汉灞曠ず鍚嶇О锛屼緵鍐欏叆 {@link SysMessage#setSenderName(String)}銆?
     * <p>
     * 涓?{@link com.forgex.sys.domain.entity.SysMessage#getSenderName()} 绾﹀畾涓€鑷达細
     * 浼樺厛浣跨敤褰撳墠鐧诲綍璐﹀彿锛圫ession {@code LOGIN_ACCOUNT}锛岃 {@link CurrentUserUtils#getAccount()}锛夛紱
     * 鑻ヤ笉鍙敤鍒欎娇鐢?{@code 鐢ㄦ埛({userId})} 鍏滃簳锛涙瀬绔儏鍐典笅鍥為€€涓?{@code 绯荤粺}銆?
     * </p>
     *
     * @param senderUserId 鍙戦€佹柟鐢ㄦ埛 ID锛岀敤浜庤处鍙风己澶辨椂鐨勫睍绀烘枃鏈?
     * @return 闈炵┖瀛楃涓诧紝鍙洿鎺ュ啓鍏ユ暟鎹簱 {@code sender_name} 鍒?
     */
    private String resolveSenderName(Long senderUserId) {
        String account = CurrentUserUtils.getAccount();
        if (StringUtils.hasText(account)) {
            return account.trim();
        }
        if (senderUserId != null) {
            return "鐢ㄦ埛(" + senderUserId + ")";
        }
        return "绯荤粺";
    }

    /**
     * 瑙ｆ瀽娑堟伅绫诲瀷锛屽啓鍏?{@link SysMessage#setMessageType(String)}銆?
     *
     * @param raw 璋冪敤鏂逛紶鍏ュ€硷紝鍏佽涓虹┖
     * @return 澶у啓闈炵┖涓诧紝榛樿 {@link #DEFAULT_MESSAGE_TYPE}
     */
    private String resolveMessageType(String raw) {
        if (StringUtils.hasText(raw)) {
            return raw.trim().toUpperCase();
        }
        return DEFAULT_MESSAGE_TYPE;
    }

    /**
     * 瑙ｆ瀽娑堟伅娓犻亾锛屽啓鍏?{@link SysMessage#setPlatform(String)}銆?
     *
     * @param raw 璋冪敤鏂逛紶鍏ュ€硷紝鍏佽涓虹┖
     * @return 澶у啓闈炵┖涓诧紝榛樿 {@link #DEFAULT_PLATFORM}
     */
    private String resolvePlatform(String raw) {
        if (StringUtils.hasText(raw)) {
            return raw.trim().toUpperCase();
        }
        return DEFAULT_PLATFORM;
    }

    private SysMessageVO toVO(SysMessage msg, Long receiverTenantId) {
        // 鍒涘缓VO瀵硅薄
        SysMessageVO vo = new SysMessageVO();
        
        // 澶嶅埗鎵€鏈夊瓧娈?
        vo.setId(msg.getId());
        vo.setSenderTenantId(msg.getSenderTenantId());
        vo.setSenderUserId(msg.getSenderUserId());
        vo.setReceiverTenantId(receiverTenantId);
        vo.setReceiverUserId(msg.getReceiverUserId());
        vo.setScope(msg.getScope());
        vo.setMessageType(msg.getMessageType());
        vo.setType(msg.getMessageType());
        vo.setPlatform(msg.getPlatform());
        vo.setSenderName(msg.getSenderName());
        vo.setTitle(msg.getTitle());
        vo.setContent(msg.getContent());
        vo.setLinkUrl(msg.getLinkUrl());
        vo.setBizType(msg.getBizType());
        vo.setStatus(msg.getStatus());
        vo.setCreateTime(msg.getCreateTime());
        vo.setReadTime(msg.getReadTime());
        
        // 杩斿洖VO瀵硅薄
        return vo;
    }
    
    /**
     * 妫€鏌ヨ法绉熸埛娑堟伅鍙戦€佹潈闄?
     * <p>
     * 閫氳繃鏌ヨ绉熸埛娑堟伅鐧藉悕鍗曡〃锛屽垽鏂彂閫佹柟绉熸埛鏄惁鏈夋潈闄愬悜鎺ユ敹鏂圭鎴峰彂閫佹秷鎭€?
     * </p>
     * <p><strong>鏉冮檺瑙勫垯锛?/strong></p>
     * <ul>
     *   <li>鍚屼竴绉熸埛鍐呴儴娑堟伅锛氭棤闇€鏍￠獙锛岀洿鎺ュ厑璁?/li>
     *   <li>璺ㄧ鎴锋秷鎭細蹇呴』鍦ㄧ櫧鍚嶅崟涓笖鐘舵€佷负鍚敤</li>
     *   <li>瓒呯骇绠＄悊鍛樼鎴凤紙ID=1锛夛細榛樿鎷ユ湁鍚戞墍鏈夌鎴峰彂閫佹秷鎭殑鏉冮檺</li>
     * </ul>
     * 
     * @param senderTenantId 鍙戦€佹柟绉熸埛ID
     * @param receiverTenantId 鎺ユ敹鏂圭鎴稩D
     * @return true琛ㄧず鏈夋潈闄愶紝false琛ㄧず鏃犳潈闄?
     */
    private boolean checkCrossTenantPermission(Long senderTenantId, Long receiverTenantId) {
        // 鍙傛暟鏍￠獙
        if (senderTenantId == null || receiverTenantId == null) {
            return false;
        }
        
        // 鍚屼竴绉熸埛锛岀洿鎺ュ厑璁?
        if (senderTenantId.equals(receiverTenantId)) {
            return true;
        }
        
        // 瓒呯骇绠＄悊鍛樼鎴凤紙ID=1锛夐粯璁ゆ嫢鏈夋墍鏈夋潈闄?
        if (senderTenantId == 0L) {
            return true;
        }
        
        try {
            // 鏌ヨ鐧藉悕鍗曢厤缃?
            Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<SysTenantMessageWhitelist>()
                    .eq(SysTenantMessageWhitelist::getSenderTenantId, senderTenantId)
                    .eq(SysTenantMessageWhitelist::getReceiverTenantId, receiverTenantId)
                    .eq(SysTenantMessageWhitelist::getEnabled, true)
                    .eq(SysTenantMessageWhitelist::getDeleted, false));
            
            // 瀛樺湪鍚敤鐨勭櫧鍚嶅崟璁板綍锛屽垯鍏佽鍙戦€?
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("妫€鏌ヨ法绉熸埛娑堟伅鏉冮檺澶辫触: senderTenantId={}, receiverTenantId={}", 
                    senderTenantId, receiverTenantId, e);
            // 寮傚父鎯呭喌涓嬶紝涓轰簡瀹夊叏璧疯锛屾嫆缁濆彂閫?
            return false;
        }
    }
}


