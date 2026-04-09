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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.service.TemplateMessageService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.sys.domain.entity.SysMessage;
import com.forgex.sys.domain.entity.SysMessageTemplate;
import com.forgex.sys.domain.entity.SysMessageTemplateContent;
import com.forgex.sys.domain.entity.SysMessageTemplateReceiver;
import com.forgex.sys.domain.vo.SysMessageVO;
import com.forgex.sys.mapper.SysMessageMapper;
import com.forgex.sys.mapper.SysMessageTemplateContentMapper;
import com.forgex.sys.mapper.SysMessageTemplateMapper;
import com.forgex.sys.mapper.SysMessageTemplateReceiverMapper;
import com.forgex.sys.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗犻弻娑樷枎閹邦剛浼岄梺閫炲苯鍘搁柣鎺炵畱閿曘垽宕堕鈧粈澶愭煃閵夈儳锛嶉柡澶夌矙閺岋絽顭ㄩ崟顐闁荤偞鍑归崑濠冧繆?
 * <p>
 * 闂備礁婀辩划顖炲礉閹烘梹顐介柣銏㈩焾閺勩儵寮堕悙鏉戭棆闁煎湱鍋撶换娑㈠箣濠靛牆顤€婵炲鍘ч幊搴ょ亽闂佹枼鏅涢崯顖滄閺屻儲鐓熼柕濞垮劚椤忣參鏌嶈閸撴艾顫濋妸鈺佹瀬闁靛牆鎳夊Σ鍫ユ煕椤愩倕鏋嶇紒杈ㄧ墵閺屾稑鈻庨幇顒備紝闂侀€炲苯鍘哥紒鈧担瑙勫弿婵炴垯鍨洪崵鍕煛閸屾ê鈧绮堟径鎰厸鐎广儱娴傞悞鍓х磼閻戞ê鍔ら棁澶娒归敐鍫涒偓鈧柡灞诲€濋弻?
 * <ol>
 *   <li>闂備礁鎼粔鐑斤綖婢跺﹦鏆?templateCode 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂佽埖褰冮埥澶愬箻鐎涙﹩娼￠梺?/li>
 *   <li>闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂佽埖鐓￠弻娑㈠箛椤掍礁娅ｅ銈嗘⒐閸旀瑩寮绘繝鍌ゅ悑闁搞儮鏅滈悗顓㈡⒑閹稿海鈽夐柣妤€锕顐︻敋閳ь剟鐛幇顓熷闁告挸寮堕埢鐓幬旈悩闈涗粶闁哥喕鍎婚妵鎰板箻椤旇姤娅?/li>
 *   <li>闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呮噹缁狅綁鏌″搴″姰闁圭儤顨呴崙鐘绘煙闁箑骞栧璺虹Ч閺岋繝鍩€椤掑嫬瀚夐柛顭戝亞濠у嫰姊洪崨濠傜瑲妞ゃ劌顦垫俊?/li>
 *   <li>濠电娀娼чˇ浠嬪磻閸曨垰鍌ㄩ柡宥庡幖绾偓闂佺粯顭堟禍顒傜矓鐎靛摜纾?/li>
 *   <li>濠电儑绲藉ú锔炬崲閸岀偞鍋ら柕濞垮劗濡插牓鏌涢銈呮瀺缂佽鲸鐗楅〃銉╂倷閺夋垵濮搁悶姘箞閺?SSE 闂備浇顫夋禍浠嬪礉閹达箑鐒?/li>
 * </ol>
 * </p>
 * <p><strong>闂備礁鎲￠〃鍫熸叏瀹曞洨绀婇柛娑卞灣缁犳棃鏌ㄩ弮鍌涙珪濠㈣鍔楅埀顒侇問閸犳牗顨ヨ箛鏇燁潟?/strong> ${闂備礁鎲￠悷锕傛晝閵忋倕闂繛宸簻鐟欙妇鈧懓瀚锟犲极瀹ュ洣娌柤娴嬫櫃缁憋絾绻?${userName}闂?{taskName}</p>
 * <p><strong>濠电姰鍨奸崺鏍ㄧ┍濞差亶鏁嬮柕蹇嬪灪閸ゅ﹪鏌嶈閸撶喎顕ｉ妸鈺婃晬婵炴垶顭囧Σ鐑芥⒑?/strong> 闂備礁鎼粔鐑斤綖婢跺﹦鏆?Accept-Language 闂佽崵濮村ú顓㈠绩闁秵鍎戦柣妤€鐗嗙欢鐐烘倵濞戞顏堟倶?LangContext 闂傚倷绶￠崑鍕囬幍顔瑰亾濮樸儱濡块柍褜鍓涢弫鎼佸箠閹炬儼濮抽柤纰卞墯鐎氭碍銇勯幘顖涱€嗛柡瀣嚇閺岋綁濡搁妷銉患闂佸摜鍋涢顓㈠焵?/p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 * @see TemplateMessageService
 * @see SysMessageTemplate
 * @see SysMessageTemplateContent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateMessageServiceImpl implements TemplateMessageService {

    /**
     * 闂備礁鎲￠〃鍫熸叏瀹曞洨绀婇柛娑卞灣缁犳棃鏌ㄩ弮鍌滄憘闁告柡鍋撻梻浣告啞鐢鎮烽妸鈺佄ラ柛鎰ㄦ櫆缂嶅洭鏌熼鍡楁噽椤㈠懘姊?{闂備礁鎲￠悷锕傛晝閵忋倕闂繛宸簻鐟欙妇鈧?
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 缂傚倷鐒﹀褰掓偡閵夆晛鑸归柡澶嬪灍濡插牓鏌涢銈呮瀺缂佽鲸鐗楅〃銉╂倷閹绘帗姣愰悷婊呭閹稿啿顕ｆ禒瀣倞妞ゅ繐妫欓～?
     */
    private static final String PLATFORM_INTERNAL = "INTERNAL";

    /**
     * 濠殿喗甯楃粙鎺椻€﹂崼銉晣閻犲洦绁村Σ鍫ユ煕椤愩倕鏋嶇紒杈ㄧ墱缁辨帡顢曢妷顔兼闂?
     */
    private static final String DEFAULT_MESSAGE_TYPE = "NOTICE";

    /**
     * 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆吹閳绘棃鏌￠崟顐ょ疄闁?Mapper
     */
    private final SysMessageTemplateMapper templateMapper;

    /**
     * 婵犵妲呴崹顏堝礈濠靛棭鐔嗘慨妞诲亾鐎规洘锕㈠畷銊╊敇瑜嶉弲?Mapper
     */
    private final SysMessageTemplateContentMapper contentMapper;

    /**
     * 婵犵妲呴崹顏堝礈濠靛棭鐔嗘慨妞诲亾妤犵偞甯℃俊鐑芥晜閼恒儲鐦戝┑?Mapper
     */
    private final SysMessageTemplateReceiverMapper receiverMapper;

    /**
     * 婵犵數鍋為崹鐢告偋婵犲啫顕?Mapper
     */
    private final SysMessageMapper messageMapper;

    /**
     * SSE 闂備浇顫夋禍浠嬪礉閹达箑鐒垫い鎴ｆ硶閸斿秵绻濋埀顒勫炊椤掆偓缁€?
     */
    private final SseEmitterService sseEmitterService;

    /**
     * JSON 闂佺懓鍚嬪娆戞崲閹版澘鍨傛い蹇撶墕缁€宀勬煛瀹擃喖鍊婚惌妤呮⒑?
     */
    private final ObjectMapper objectMapper;

    /**
     * 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡堕崨鍌滃枛閸╁嫰宕橀埡鍐ㄥ殥闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤犵偘绶氶、娑㈠Χ閸モ晩妲遍梻浣告惈鐎氼參宕曞畷鍥潟闁跨喓濮寸粻浼存煕閵夋垵鎳忓В搴ㄦ⒑鐟欏嫪鑸柛搴㈢叀瀵偆鎲撮崟顓ф锤闂佺粯鑹鹃顓犵矆閸℃稒鐓?
     * <p>
     * 闂傚倷绶￠崑鍕磹婵犳艾鏋侀柕鍫濇椤╂煡骞栫划瑙勵潑闁告埃鍋撻梻浣哥秺濞佳呯礊婵犲洤鐒垫い鎺嶇劍閻ㄦ垿鏌ｉ幙鍕？缂侇喖鐏氬鍕箛椤撴繄甯涢梺鑽ゅ枑濞叉垹绮堟担瑙勫弿闁冲搫鎳庣粻鎴澝归敐鍥ㄥ殌闁伙箑缍婇幃妤冩喆閸曨収鏆￠柣銏╁灡閹稿啿顕ｉ妸鈺佸珘闁割煈鍋嗗┃鍕⒑濮瑰洤濡奸悗姘煎墮閳瑰啴鍩€椤掑嫭鐓涢柛婊€绀侀悘娑㈡煃?
     * </p>
     *
     * @param templateCode    婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆亹绾句粙鏌″搴′簽闁搞劍濞婇弻銊モ槈濡警娈紓浣诡殔椤︾敻骞婂Δ鍛闁圭儤娲滈ˇ鎵磽?
     * @param receiverUserIds 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╂煡鏌ц箛姘兼綈闁哄棗绻橀弻鐔衡偓鍨暭閻涘爼姊洪崨濠傜瑲妞ゃ劌顦垫俊鎾礃椤旇姤娅栭柣蹇曞仩閸嬫劙鎮峰┑瀣厾闁惧浚鍋勯悘濠囨偣閹邦喖鏋旂紒?
     * @param dataMap         闂備礁鎲￠〃鍫熸叏瀹曞洨绀婇柛娑卞灣缁犳棃鏌ㄩ弮鍥撴繛鍫濈埣閺岀喖宕烽鐔告殢ap闂備焦瀵х粙鎴︽嚐椤栨縿浜归柛娆忣槺閳绘棃鏌ц箛鎾冲辅闁?
     * @param bizType         濠电偞鍨堕幐濠氭嚌閻愵剚鍙忛柣鏃囨鐏忕敻鎮归崶顏勭毢闁逞屽墰閸忔﹢寮鍥︽勃闁芥ê顦抽澶嬬箾閹寸偞宕勬繛鍜冪秮閸?
     * @return 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秹鏌涢妸锕€鍝虹€规洘绻堥幃銏ゆ倻濡吋娈告繝鐢靛仦閸ㄧ敻鎮ф繝鍐嚤闁告稒娼欓弸浣该归崗鍏肩稇婵炲懌鍊濋弻銊モ槈濡灝顏梺闈涙处閸ㄥ綊骞忚ぐ鎺懳ㄩ柕濞у拋鍞归梻?
     * @throws IllegalArgumentException 闁?templateCode 闂?receiverUserIds 濠电偞鍨堕幑浣糕枍閿濆鐓橀柡宥庡幖缁秹鏌ら幖浣规锭妞ゎ偅妫冮弻?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendByTemplate(String templateCode, List<Long> receiverUserIds, Map<String, Object> dataMap, String bizType) {
        // 闂備礁鎲￠悷銉╁磹瑜版帒姹查柣鏃傚帶閸愨偓闂佽法鍠撴慨宄扮暦?
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("Invalid request parameter");
        }
        if (receiverUserIds == null || receiverUserIds.isEmpty()) {
            throw new IllegalArgumentException("Receiver list cannot be empty");
        }

        // 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂?
        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆吹閳绘梻鈧箍鍎遍幊鎰板箺閻樼粯鐓? templateCode={}", templateCode);
            return 0;
        }
        if (!Boolean.TRUE.equals(template.getStatus())) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖欒閸熷懘鏌ょ喊鍗炲⒒婵炶缍侀弻? templateCode={}", templateCode);
            return 0;
        }

        // 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂佽埖鐓￠弻娑㈠箛椤掍礁娅ｅ銈嗘⒐閸旀瑩寮绘繝鍌ゅ悑闁搞儮鏅滈悗?
        List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
        if (contents.isEmpty()) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘慨妞诲亾鐎规洘锕㈠畷銊╊敇瑜嶉弲锝夋⒒娴ｈ绶茬紒澶婄埣閹潡宕卞Ο灏栨灃闂佹儳绻愰崢婊堝极? templateCode={}", templateCode);
            return 0;
        }

        // 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉簵娴滃綊鏌熼幆褍鏆辨い銈呮噽缁辨帗寰勭仦鑺ョ€鹃梺绯曟櫆閺夌枍
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            log.warn("Current tenantId is null, cannot send message");
            return 0;
        }

        // 闂備礁鎲￠敋妞ゎ厾鍏樺畷鎶藉箹娴ｅ摜顔婇梺闈涱檧闂勫嫰寮抽鍔?
        Set<Long> distinctUserIds = new LinkedHashSet<>(receiverUserIds);

        // 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤?
        int sentCount = 0;
        for (Long receiverUserId : distinctUserIds) {
            if (receiverUserId == null) {
                continue;
            }

            for (SysMessageTemplateContent content : contents) {
                // 濠电偛顕慨鎾箠鎼淬剫鍥煛閸涱喖浠╅梺绯曞墲閻熝嗐亹閺屻儲鐓曢柟閭﹀墯閸ｈ櫣绱掓径灞藉幋妤?
                if (!PLATFORM_INTERNAL.equals(content.getPlatform())) {
                    continue;
                }

                // 濠电娀娼чˇ浠嬪磻閸曨垰鍌ㄩ柡宥庡幖绾偓闂佺粯顭堟禍顒傜矓鐎靛摜纾?
                String title = fillPlaceholders(content.getContentTitle(), content.getContentTitleI18nJson(), dataMap);
                String body = fillPlaceholders(content.getContentBody(), content.getContentBodyI18nJson(), dataMap);

                // 闂備礁鎲＄敮妤冪矙閹寸姷纾介柟鐐灱濡插牓鏌涢銈呮瀺缂?
                SysMessage message = createMessage(tenantId, receiverUserId, template, content, title, body, bizType);

                // 濠电儑绲藉ú锔炬崲閸岀偞鍋ら柕濞垮劗濡插牓鏌涢銈呮瀺缂?
                messageMapper.insert(message);

                // SSE 闂備浇顫夋禍浠嬪礉閹达箑鐒?
                pushMessage(tenantId, receiverUserId, message);

                sentCount++;
            }
        }

        log.info("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗犻弻娑樷枎閹邦剛浼岄梺閫炲苯鍘哥紒鈧笟鈧幃楣冾敆閸曨偆顓? templateCode={}, receiverCount={}, sentCount={}",
                templateCode, distinctUserIds.size(), sentCount);
        return sentCount;
    }

    /**
     * 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡堕崨鍌滃枛閸╁嫰宕橀埡鍐ㄥ殥闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤犵偘绶氶、娑㈠Χ閸モ晩妲遍梻浣告惈缁夌兘锝炴径濠勬殼濞达綀鍊介悢鐓庣労闁告劏鏅濋崙锟犳⒒娴ｈ绶茬紒澶婄埣閹潡宕卞☉娆忔疁濡炪倕绻愮€氼剝顤勯梺鑽ゅ枑閻熻京寰婃ィ鍐╁€靛ù鐘差儏缁犳娊鏌曟径娑㈡闁哄棭浜滈湁婵犲﹤鎳庨惃鎴犵磼鏉堛劌鍝哄┑?
     * <p>
     * 闂傚倷绶￠崑鍕磹婵犳艾鏋侀柕鍫濇椤╂煡骞栨潏鍓х？闁哄棔鍗抽弻锟犲礃閵娧冪厽闂侀€炲苯澧慨妯稿姂閹繝鏁嶉崟顓⑩攺婵犮垼娉涢鍛緞瀹ュ鐓涢柍褜鍓熷鐢稿垂椤愩倖绨為梻浣规偠閸庨亶骞栭锔藉仼濡わ絽鍟崕宥夋煕閺囥劌浜濈紒銊﹀哺閺岋繝宕担鍝ヤ粶闂?
     * </p>
     *
     * @param templateCode 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆亹绾句粙鏌″搴′簽闁搞劍濞婇弻銊モ槈濡警娈紓浣诡殔椤︾敻骞婂Δ鍛闁圭儤娲滈ˇ鎵磽?
     * @param dataMap      闂備礁鎲￠〃鍫熸叏瀹曞洨绀婇柛娑卞灣缁犳棃鏌ㄩ弮鍥撴繛鍫濈埣閺岀喖宕烽鐔告殢ap闂備焦瀵х粙鎴︽嚐椤栨縿浜归柛娆忣槺閳绘棃鏌ц箛鎾冲辅闁?
     * @return 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秹鏌涢妸锕€鍝虹€规洘绻堥幃銏ゆ倻濡吋娈告繝鐢靛仦閸ㄧ敻鎮ф繝鍐嚤闁告稒娼欓弸浣该归崗鍏肩稇婵炲懌鍊濋弻銊モ槈濡灝顏梺闈涙处閸ㄥ綊骞忚ぐ鎺懳ㄩ柕濞у拋鍞归梻?
     */
    @Override
    public int sendByTemplate(String templateCode, Map<String, Object> dataMap) {
        // 闂備礁鎲￠悷銉╁磹瑜版帒姹查柣鏃傚帶閸愨偓闂佽法鍠撴慨宄扮暦?
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("Invalid request parameter");
        }

        // 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂?
        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆吹閳绘梻鈧箍鍎遍幊鎰板箺閻樼粯鐓? templateCode={}", templateCode);
            return 0;
        }

        // 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呮噹缁犳娊鏌曟径娑㈡闁哄棭浜滈湁婵犲﹤瀚妵婵嬫煕濡鈧妲?
        List<SysMessageTemplateReceiver> receivers = findTemplateReceivers(template.getId());
        if (receivers.isEmpty()) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘慨妞诲亾妤犵偞甯℃俊鐑芥晜閼恒儲鐦戝┑鐐茬摠缁秴顪冩禒瀣偍闁靛牆娲ㄧ壕鐐亜閺冣偓濡叉帞绮堢€ｎ剛纾? templateCode={}", templateCode);
            return 0;
        }

        // 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏缁犳娊鏌曟径娑㈡闁哄棭浜滈湁婵犲﹤鎳愬Σ鐧夐梻浣告啞鐢銆冩径鎰?
        List<Long> receiverUserIds = resolveReceiverUserIds(receivers);
        if (receiverUserIds.isEmpty()) {
            log.warn("闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏鐟欙箓骞栨潏鍓хɑ闁伙綁浜堕弻鐔煎箒閹烘垵濮曢梺杞版祰椤绮嬪澶婁紶闁告洦鍋嗘禒姘舵煟閻愬鈼ら柛鏂款儑閹峰綊鎮㈤搹鍦厰? templateCode={}", templateCode);
            return 0;
        }

        // 闂佽崵濮撮鍛村疮娴兼潙鏋侀柕鍫濐槸閸欏﹪鏌熼鍡楁噽椤㈠懘姊洪崷顓х劸婵炲眰鍊濋幃鐐偅閸愩劎顔婇梺闈涱檧闂勫嫰寮抽鍔界懓顭ㄩ崱姗嗕哗濠电偛鐗婇崹鍨嚕椤掑倹鍠嗛柛鏇ㄥ幑閳?
        return sendByTemplate(templateCode, receiverUserIds, dataMap, null);
    }

    /**
     * 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡堕崨鍌滃枛閸╁嫰宕橀埡鍐ㄥ殥闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤犵偘绶氶、娑橆煥閸涙澘鐓戦梻浣告啞椤ㄥ棗煤閹达附鐓傛繝濠傜墛閸嬨劑鏌曟繝蹇曠暠闁绘挻娲熼弻?
     *
     * @param templateCode   婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆亹绾句粙鏌″搴′簽闁搞劍濞婇弻銊モ槈濡警娈紓浣诡殔椤︾敻骞婂Δ鍛闁圭儤娲滈ˇ鎵磽?
     * @param receiverUserId 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╂煡鏌ц箛姘兼綈闁哄棗绻橀弻鐔衡偓鍨暭閻涘爼姊洪幐搴ｂ槈濠靛倹姊荤划顓熷緞閹邦厼娈戦梺鐟扮摠鐢喓绮堢€ｎ剛纾?
     * @param dataMap        闂備礁鎲￠〃鍫熸叏瀹曞洨绀婇柛娑卞灣缁犳棃鏌ㄩ弮鍥撴繛鍫濈埣閺岀喖宕烽鐔告殢ap闂備焦瀵х粙鎴︽嚐椤栨縿浜归柛娆忣槺閳绘棃鏌ц箛鎾冲辅闁?
     * @param bizType        濠电偞鍨堕幐濠氭嚌閻愵剚鍙忛柣鏃囨鐏忕敻鎮归崶顏勭毢闁逞屽墰閸忔﹢寮鍥︽勃闁芥ê顦抽澶嬬箾閹寸偞宕勬繛鍜冪秮閸?
     * @return 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴ｆ硶閸斿秹鏌涢妸锕€鍝虹€规洘绻堥幃銏ゆ嚒閵堝浂鍞归梻浣规偠閸庢娊宕戦悙鐢电煋闁绘垼妫勭粻鎺楁煟閵堝棎鍋嬮梻浣瑰缁嬫垿鎳熼鐐参﹂柟瀵稿У鐎氬鏌曟径鍫濆缂佺姵鐓￠弻娑㈠Ψ瑜夐崑鎾愁潖閻氱棭
     */
    @Override
    public Long sendToUser(String templateCode, Long receiverUserId, Map<String, Object> dataMap, String bizType) {
        // 闂備礁鎲￠悷銉╁磹瑜版帒姹查柣鏃傚帶閸愨偓闂佽法鍠撴慨宄扮暦?
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("Invalid request parameter");
        }
        if (receiverUserId == null) {
            throw new IllegalArgumentException("Invalid request parameter");
        }

        // 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂?
        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null || !Boolean.TRUE.equals(template.getStatus())) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆吹閳绘梻鈧箍鍎遍幊鎰板箺閻樼粯鐓曢柨鏂挎惈婵′粙鏌涢妸锝呭妞ゆ柨绻橀幊鐘活敆婢跺鐓戦梻? templateCode={}", templateCode);
            return null;
        }

        // 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂佽埖鐓￠弻娑㈠箛椤掍礁娅ｅ銈嗘⒐閸旀瑩寮绘繝鍌ゅ悑闁搞儮鏅滈悗?
        List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
        if (contents.isEmpty()) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘慨妞诲亾鐎规洘锕㈠畷銊╊敇瑜嶉弲锝夋⒒娴ｈ绶茬紒澶婄埣閹潡宕卞Ο灏栨灃闂佹儳绻愰崢婊堝极? templateCode={}", templateCode);
            return null;
        }

        // 闂備礁鍚嬮崕鎶藉床閼艰翰浜归柛銉簵娴滃綊鏌熼幆褍鏆辨い銈呮噽缁辨帗寰勭仦鑺ョ€鹃梺绯曟櫆閺夌枍
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            log.warn("Current tenantId is null, cannot send message");
            return null;
        }

        // 闂備礁鎼悮顐﹀磿閸欏鐝舵俊顖濆亹閸楁碍绻涢崱妤冪闁轰浇鍩栫换娑㈠箣濠靛牆顤€婵炲鍘ч幊妯虹暦濮椻偓瀹曘劑顢樿閺?
        SysMessageTemplateContent internalContent = contents.stream()
                .filter(c -> PLATFORM_INTERNAL.equals(c.getPlatform()))
                .findFirst()
                .orElse(null);

        if (internalContent == null) {
            log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘慨妞诲亾鐎殿喚鏁婚、鏃堝醇濠靛棜绁寸紓鍌氬€搁崰姘跺窗濮樿埖鍋熸い鏍仜缁€鍐煕濞戝崬鏋ら崯鎼佹⒑鐠団€冲季闁搞劋鍗冲畷顒傗偓鐢电《閸? templateCode={}", templateCode);
            return null;
        }

        // 濠电娀娼чˇ浠嬪磻閸曨垰鍌ㄩ柡宥庡幖绾偓闂佺粯顭堟禍顒傜矓鐎靛摜纾?
        String title = fillPlaceholders(internalContent.getContentTitle(), internalContent.getContentTitleI18nJson(), dataMap);
        String body = fillPlaceholders(internalContent.getContentBody(), internalContent.getContentBodyI18nJson(), dataMap);

        // 闂備礁鎲＄敮妤冪矙閹寸姷纾介柟鐐灱濡插牓鏌涢銈呮瀺缂?
        SysMessage message = createMessage(tenantId, receiverUserId, template, internalContent, title, body, bizType);

        // 濠电儑绲藉ú锔炬崲閸岀偞鍋ら柕濞垮劗濡插牓鏌涢銈呮瀺缂?
        messageMapper.insert(message);

        // SSE 闂備浇顫夋禍浠嬪礉閹达箑鐒?
        pushMessage(tenantId, receiverUserId, message);

        log.info("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗犻弻娑樷枎閹邦剛浼岄梺閫炲苯鍘搁柣鎺炵畵瀹曠懓顫濈捄铏诡槷? templateCode={}, receiverUserId={}, messageId={}",
                templateCode, receiverUserId, message.getId());

        return message.getId();
    }

    /**
     * 婵犵妲呴崑鈧柛瀣崌閺岋紕浠︾拠鎻掑闁煎灈鏅犻弻鈩冨緞鐎ｎ亶鍤嬪┑鈥冲级閹倸鐣烽妷鈺傛櫇闁逞屽墴閹椽濡搁埡浣瑰祶闂侀潧顭堥崐妤呮偡椤掑嫭鐓曟慨妯煎帶閻忥綁鏌℃担闈涒偓婵囦繆?
     *
     * @param templateCode 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆亹绾句粙鏌″搴′簽闁?
     * @return true闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵﹩鍎甸悢鐓庣労闁告劏鏅濋崙锟犳煟鎼淬垻鈯曢柨姘節閳ь剟顢旈崨顖楁灃闂佸搫鍊圭€笛囧磿濞嗘挻鐓熼柕蹇婃櫇閸樻粎绱掓潏銊у惞alse闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵﹩鍘鹃埢鏃傗偓骞垮劚閹虫劙骞楅悩缁樼厱闁挎柨鎼俊浠嬫煕閵婏絽濡兼い鏂跨箻閹崇娀顢楁径瀣厬闂?
     */
    @Override
    public boolean isTemplateAvailable(String templateCode) {
        if (!StringUtils.hasText(templateCode)) {
            return false;
        }
        SysMessageTemplate template = findTemplateByCode(templateCode);
        return template != null && Boolean.TRUE.equals(template.getStatus());
    }

    /**
     * 闂備礁鎼粔鐑斤綖婢跺﹦鏆ゅù锝堝€介悢鐓庣労闁告劏鏅濋崙锛勭磽閸屾艾鈧綊鎮у鍫熷亯婵犲﹤鐗嗛拑鐔兼煏婢舵鍘涢柛銈呭娣囧﹪顢涘Δ鈧晶鍙夌節閵忊埄鎴犵矙婢舵劕鍙婇煫鍥ь儌閸嬫捇宕橀鍢?
     *
     * @param templateCode 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆亹绾句粙鏌″搴′簽闁?
     * @return 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖氱毞閸嬫捇宕烽鐐扮钵缂傚倸绉撮澶愬极瀹ュ洣娌柤娴嬫櫇閹插綊鏌ｆ惔銏⑩姇闁挎碍绻濋埀顒勵敂閸涱喕姘﹂梺鍝勫€圭€笛呯矆閳х惢ull
     */
    private SysMessageTemplate findTemplateByCode(String templateCode) {
        return templateMapper.selectOne(new LambdaQueryWrapper<SysMessageTemplate>()
                .eq(SysMessageTemplate::getTemplateCode, templateCode)
                .eq(SysMessageTemplate::getDeleted, false)
                .last("LIMIT 1"));
    }

    /**
     * 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂佽埖鐓￠弻娑㈠箛椤掍礁娅ｅ銈嗘⒐閸旀瑩寮绘繝鍌ゅ悑闁搞儮鏅滈悗顓㈡⒑閸涘﹤绗氭い銊ヮ樀婵℃挳宕橀鍢?
     *
     * @param templateId 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆吹閳绘棃鏌￠崟顐ょ疄闁逞屽墮閸庢€?
     * @return 闂備礁鎲￠崝鏇㈠箠鎼搭煈鏁婇柟鎵閻撯偓閻庡箍鍎卞ú銊╁几閸岀偞鐓曢柟鐑樻惄濞堟瑩鏌?
     */
    private List<SysMessageTemplateContent> findTemplateContents(Long templateId) {
        return contentMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateContent>()
                .eq(SysMessageTemplateContent::getTemplateId, templateId)
                .eq(SysMessageTemplateContent::getDeleted, false));
    }

    /**
     * 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈傚亾閾伙綁鏌嶉埡浣告殲缂佽埖鐓￠弻鐔煎箒閹烘垵濮曢梺杞版祰椤绮嬪鍚ゆ椽顢旈崟顐ョゴ缂傚倸鍊搁崰姘跺窗閺嶎厼鍨傛い蹇撴閸嬫﹢鏌曟繝蹇曞矝闁?
     *
     * @param templateId 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆吹閳绘棃鏌￠崟顐ょ疄闁逞屽墮閸庢€?
     * @return 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╁弶绻涘顔荤盎閻㈩垰纾槐鎾诲礃閹勭亪闂佸壊鐓堟禍鐐哄箖?
     */
    private List<SysMessageTemplateReceiver> findTemplateReceivers(Long templateId) {
        return receiverMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateReceiver>()
                .eq(SysMessageTemplateReceiver::getTemplateId, templateId)
                .eq(SysMessageTemplateReceiver::getDeleted, false));
    }

    /**
     * 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏缁犳娊鏌曟径娑㈡闁哄棭浜滈湁婵犲﹤妫欓鐘绘煛娴ｉ潧鈧繈鐛€ｎ剛鐭欓柕鍡愬劦閺屾盯骞掗幙鍐╃暯闂侀潧妫楅崯瀛樹繆?
     * <p>
     * 闂備礁鎼粔鐑斤綖婢跺﹦鏆?receiverType 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儐閺?
     * <ul>
     *   <li>USER: 闂備胶鍎甸弲娑㈡偤閵娧勬殰閻庢稒蓱婵挳鎮归幁鎺戝闁?receiverIds</li>
     *   <li>ROLE: 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呭暞閸犲棝鏌熼悜妯荤妞ゃ儱绻愰埥澶愬箻閹颁礁鍓冲┑鐐茬墛閸ㄥ潡骞冩禒瀣╅柨鏇楀亾闁?/li>
     *   <li>DEPT: 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呮噺閻掗箖鏌曟繛鐐珕闁挎稑鑻埥澶愬箻閹颁礁鍓冲┑鐐茬墛閸ㄥ潡骞冩禒瀣╅柨鏇楀亾闁?/li>
     *   <li>POSITION: 闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呮噺閸ゅ倿鎮橀悙璺轰汗缂佸娼欓埥澶愬箻閹颁礁鍓冲┑鐐茬墛閸ㄥ潡骞冩禒瀣╅柨鏇楀亾闁?/li>
     * </ul>
     * </p>
     * <p>
     * TODO: 闁荤喐绮庢晶妤呭箰閸涘﹥娅犻柣妯虹－椤╃兘鏌涘☉鍗炴灍闁哄棙绮撻弻?USER 缂傚倷绶￠崑澶愵敋瑜旈幃妤呮倻閼恒儲娅栭柣蹇曞仩濡嫬顕ラ幘瓒佸綊鎮╁畷鍥р拡闁荤偞鍑归崑濠傜暦閸︻厸鍋撻敐搴℃灍濞寸姵锕㈤幃鐑藉即濮樺崬濡介梺杞拌兌閸嬨倕鐣峰Δ鍛ㄧ憸搴ｇ矈閺屻儲鐓?Mapper
     * </p>
     *
     * @param receivers 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╁弶绻涘顔荤盎閻㈩垰纾槐鎾诲礃閹勭亪闂佸壊鐓堟禍鐐哄箖?
     * @return 闂備焦妞垮鍧楀礉瀹ュ鏄ユ慨鍦嫃闂備礁鎲＄敮妤呫€冩径鎰?
     */
    private List<Long> resolveReceiverUserIds(List<SysMessageTemplateReceiver> receivers) {
        Set<Long> userIds = new LinkedHashSet<>();

        for (SysMessageTemplateReceiver receiver : receivers) {
            String receiverType = receiver.getReceiverType();
            List<Long> ids = parseReceiverIds(receiver.getReceiverIds());

            if ("USER".equals(receiverType)) {
                // 闂備礁婀遍…鍫澝洪妶澶嬪仼闁绘劦鍓涢々鏌ユ煣韫囨洦鏀版い顐畵閺屾盯濡搁妷顔煎妧缂備浇椴哥换鍫ュ箚閸曨厸鍋撳☉娅亝寰勫鍡欑闁挎繂鐗滈崵娆忊攽?
                userIds.addAll(ids);
            } else if ("ROLE".equals(receiverType)) {
                // TODO: 闂備礁鎼粔鐑斤綖婢跺﹦鏆ゅù锝囧劋閸犲棝鏌熼悜妯荤妞ゃ儱绮〥闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呮噺閸嬨劑鏌曟繝蹇曠暠闁绘挻妞孌闂備礁鎲＄敮妤呫€冩径鎰?
                // 闂傚倸鍊稿ú鐘诲磻閹剧粯鍋￠柡鍥ㄦ皑閸斿秹鏌℃担鍝勪槐鐎?SysUserRoleMapper
                log.warn("ROLE 缂傚倷绶￠崑澶愵敋瑜旈幃妤呮倻閽樺顔婇梺闈涱檧闂勫嫰寮抽鍔界懓顭ㄩ崼銏㈢獥閻炴碍鐟╅弻锛勨偓锝庡亜婵牊绻涢崼婵堢鐎殿喚鏁婚、鏃堝礋椤撶喐绶伴梻? receiverIds={}", ids);
            } else if ("DEPT".equals(receiverType)) {
                // TODO: 闂備礁鎼粔鐑斤綖婢跺﹦鏆ゅ〒姘ｅ亾闁哄苯鐗撴俊鎼佸煛閸岋妇绀塈D闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呮噺閸嬨劑鏌曟繝蹇曠暠闁绘挻妞孌闂備礁鎲＄敮妤呫€冩径鎰?
                // 闂傚倸鍊稿ú鐘诲磻閹剧粯鍋￠柡鍥ㄦ皑閸斿秹鏌℃担鍝勪槐鐎?SysUserMapper
                log.warn("DEPT 缂傚倷绶￠崑澶愵敋瑜旈幃妤呮倻閽樺顔婇梺闈涱檧闂勫嫰寮抽鍔界懓顭ㄩ崼銏㈢獥閻炴碍鐟╅弻锛勨偓锝庡亜婵牊绻涢崼婵堢鐎殿喚鏁婚、鏃堝礋椤撶喐绶伴梻? receiverIds={}", ids);
            } else if ("POSITION".equals(receiverType)) {
                // TODO: 闂備礁鎼粔鐑斤綖婢跺﹦鏆ゅ〒姘ｅ亾闁硅櫕娲滄禒锕傛嚃閳哄唭顢疍闂備礁鎼悮顐﹀磿閹绢噮鏁嬫俊銈呮噺閸嬨劑鏌曟繝蹇曠暠闁绘挻妞孌闂備礁鎲＄敮妤呫€冩径鎰?
                // 闂傚倸鍊稿ú鐘诲磻閹剧粯鍋￠柡鍥ㄦ皑閸斿秹鏌℃担鍝勪槐鐎?SysUserMapper
                log.warn("POSITION 缂傚倷绶￠崑澶愵敋瑜旈幃妤呮倻閽樺顔婇梺闈涱檧闂勫嫰寮抽鍔界懓顭ㄩ崼銏㈢獥閻炴碍鐟╅弻锛勨偓锝庡亜婵牊绻涢崼婵堢鐎殿喚鏁婚、鏃堝礋椤撶喐绶伴梻? receiverIds={}", ids);
            } else if ("CUSTOM".equals(receiverType)) {
                // CUSTOM 缂傚倷绶￠崑澶愵敋瑜旈幃妤呮倻閼恒儱浠洪梺鍛婄懄椤洭鎮疯ぐ鎺撶厱闁哄倸鎳岄崐銈夊吹閻愬瓨鍙忛柣鐔告緲閳ь剙鎲￠幈銊╁煛閸涱厾鐣辨繛杈剧悼閹虫捇鎯佽ぐ鎺撳€甸悷娆忓閸熻尙绱掓潏銊х畺婵炶壈顕ч埢搴ㄥ箻閾忣偒浠﹀┑鐐村灦閹稿摜绮旈幘顔㈠洭鍩￠崨顔间哗闂佺硶鍓濈粙鎺擃殰椤栫偞鐓?receiverIds
                log.debug("CUSTOM 缂傚倷绶￠崑澶愵敋瑜旈幃妤呮倻閽樺顔婇梺闈涱檧闂勫嫰寮抽鍔界懓顭ㄩ崱姗嗕哗闂佽桨娴囨ご鍝ョ矙婢跺娼╂い鎺嗗亾妞ゎ偁鍊濋幃褰掑炊椤掍焦鏆犻梺娲讳簷閸楀啿鐣峰┑瀣╅柨鏃傛嚀娴滄儳霉閿濆妫戦柣鎰躬閺? templateReceiverId={}", receiver.getId());
            }
        }

        return new ArrayList<>(userIds);
    }

    /**
     * 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏缁犳娊鏌曟径娑㈡闁哄棭浜滈湁婵犲﹤鎳愬Σ鐧?JSON闂佽瀛╃粙鎺椼€冮崼銉晞濞达絽婀遍埢鏃堟倶閻愭彃鈧敻宕?
     *
     * @param receiverIdsJson JSON闂佽瀛╃粙鎺椼€冮崼銉晞濞达絽婀遍埢鏃堟偣閸ュ洤鎳愰ˇ顔界箾閹剧澹樻い鎴濇嚇閵?"[1, 2, 3]"
     * @return ID闂備礁鎲＄敮妤呫€冩径鎰?
     */
    private List<Long> parseReceiverIds(String receiverIdsJson) {
        if (!StringUtils.hasText(receiverIdsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(receiverIdsJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏缁犳娊鏌曟径娑㈡闁哄棭浜滈湁婵犲﹤鎳愬Σ鐧夐梻浣告啞鐢銆冩径鎰ラ柛鎰ㄦ櫅缁剁偤寮堕崼顐函鐞? {}", receiverIdsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 濠电娀娼чˇ浠嬪磻閸曨垰鍌ㄩ柡宥庡幖绾偓闂佺粯顭堟禍顒傜矓鐎靛摜纾兼俊銈咁儓缁€瀣煃?
     * <p>
     * 闂備浇銆€閸嬫挻銇勯弽銊р槈闁?${闂備礁鎲￠悷锕傛晝閵忋倕闂繛宸簻鐟欙妇鈧?闂備礁鎼粔鍫曞储瑜忓Σ鎰版晸閻樺啿鍓梺鍛婃处閸嬪嫮绮ｅΔ鈧湁闁挎繂鎳愯倴濡炪倖鍨崇欢姘跺极瀹ュ洣娌柤娴嬫櫇閻?dataMap 濠电偞鍨堕幖鈺呭矗閳ь剚顨ラ悙鑼鐎规洩缍佸鍊燁槷闁稿鎹囬幊婵嬪箥椤旂虎鍟嬮梺鑽ゅ仦缁嬫垿寮甸鈧湁婵炴垯鍨圭粻鍙夈亜椤愵偄鍘撮柛?
     * </p>
     *
     * @param template     婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖氱毞閸嬫挸鈽夊▎妯荤暭濡炪倖鍨抽悞锔剧矙?
     * @param i18nJson     濠电姰鍨奸崺鏍ㄧ┍濞差亶鏁嬮柕蹇嬪灪閸ゅ﹪鏌?JSON
     * @param dataMap      闂備礁鎲￠〃鍫熸叏瀹曞洨绀婇柛娑卞灣缁犳棃鏌ㄩ弮鍥撴繛鍫濈埣閺?
     * @return 濠电娀娼чˇ浠嬪磻閸曨垰鍌ㄩ柡宥庡幖鐟欙箓骞栨潏鍓хɑ闁伙綁浜堕幃妤€鈽夊▎妯荤暭濡炪倖鍨抽悞锔剧矙?
     */
    private String fillPlaceholders(String template, String i18nJson, Map<String, Object> dataMap) {
        // 濠电偞娼欓崥瀣晪闂佸憡蓱缁嬫帡骞忛崨顖涘磯闁靛闄勫▓銏犫攽閳藉棗鐏欓柛鎾寸箓椤灝螣鐏忔牕浜炬繛鎴炵懐濞堟ɑ銇勯幋婊呭妽缂佸顦遍幏鐘侯槾缂佲偓婢跺ň鏀介柍銉ュ暱閳ь剙缍婇幃妯诲緞鐎ｎ兘鏋栭梺鎯х箰閸樻粓寮崟顖涚厱闁圭儤鎸鹃幊浣虹磼椤旀儳校鐎垫澘瀚～婵嬪箛娴ｅ憡顓鹃梺鑽ゅ枎閻ㄦ繈宕?JSON 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊?
        String content = template;
        if (!StringUtils.hasText(content) && StringUtils.hasText(i18nJson)) {
            content = resolveI18nContent(i18nJson);
        }

        if (!StringUtils.hasText(content)) {
            return "";
        }

        if (dataMap == null || dataMap.isEmpty()) {
            return content;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = dataMap.get(key);
            String replacement = value != null ? Matcher.quoteReplacement(String.valueOf(value)) : "";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 濠电偛顕慨瀛橆殽閹间絸鍥╂崉閵婏箑鐝板銈嗘尨濡狙囧几?JSON 濠电偞鍨堕幖鈺呭矗閳ь剛鎮▎鎾寸厸閻庯綆鍋勬慨鍥煕閻愬樊鍤熼柍褜鍓涢幊鎾剁不瀹ュ鐒?
     * <p>
     * 濠电偞娼欓崥瀣晪闂佸憡蓱缁嬫帡骞忛崨顖涘磯闁靛闄勫▓?zh-CN闂備焦瀵х粙鎴︽嚐椤栫偛鐭楅柟顖嗏偓閺嬪酣鏌嶉妷锕€绲婚柍璇茬箻閺岋綁锝為鈧俊鎸庛亜閹存繃澶勭紒瀣樀閸┾偓妞ゆ帊鑳堕埢鏃€銇勮箛鎾跺濠殿喗绮庣槐鎺斾沪閼恒儲鍎撻梺閫炲苯澧版い鎴炴礋閸┾偓?
     * </p>
     *
     * @param i18nJson 濠电姰鍨奸崺鏍ㄧ┍濞差亶鏁嬮柕蹇嬪灪閸ゅ﹪鏌?JSON
     * @return 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏鐟欙箓骞栨潏鍓хɑ闁伙綁浜堕弻娑㈠箛椤掍礁娅ｅ?
     */
    private String resolveI18nContent(String i18nJson) {
        if (!StringUtils.hasText(i18nJson)) {
            return null;
        }
        try {
            Map<String, String> i18nMap = objectMapper.readValue(i18nJson, new TypeReference<Map<String, String>>() {});
            // 濠电偞娼欓崥瀣晪闂佸憡蓱缁嬫帡骞忛崨顖涘磯闁靛闄勫▓銏＄箾閹寸偞鎯勯柛妯圭矙瀵?
            if (i18nMap.containsKey("zh-CN") && StringUtils.hasText(i18nMap.get("zh-CN"))) {
                return i18nMap.get("zh-CN");
            }
            // 闂備胶顭堢换妤呭春閺嶎収鏁冮柛蹇撳悑婵挳鎮归幁鎺戝闁哄棗绻橀弻銈夊及韫囨稑鎽甸梺?
            if (i18nMap.containsKey("en-US") && StringUtils.hasText(i18nMap.get("en-US"))) {
                return i18nMap.get("en-US");
            }
            // 闂備礁鎼悧鍐磻閹剧粯鐓曟慨姗嗗墰閸戠懓鈹戦悙鍙夊枠闁诡喕绮欐俊姝岊檨闁稿孩鍨甸埥澶愬箻椤栨矮澹曞┑鐐村灦閹尖晜绂嶉鍕电劷妞ゅ繐妫涢惌姘舵煕濠靛棗顏ら柛?
            for (String value : i18nMap.values()) {
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        } catch (Exception e) {
            log.warn("闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊甸柤鎭掑劚缁剁偞鎱ㄥΟ璇插闁搞倖妫冮幃椋庝焊娴ｉ晲澹?JSON 濠电姰鍨洪崕鑲╁垝閸撗勫枂? {}", i18nJson, e);
        }
        return null;
    }

    /**
     * 闂備礁鎲＄敮妤冪矙閹寸姷纾介柟鐐灱濡插牓鏌涢銈呮瀺缂佽鲸鐗犻幃妤呭捶椤撶偘绮х紓鍌氱Т椤戝淇?
     *
     * @param tenantId      缂傚倷绀侀ˇ閬嶅窗閹版澘鏄ユ慨鍦嫃
     * @param receiverUserId 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╄尙绱掗埄鍐潷
     * @param template      婵犵妲呴崹顏堝礈濠靛棭鐔?
     * @param content       闂備礁鎲￠崝鏇㈠箠鎼搭煈鏁婇柟鎵閻撯偓閻庡箍鍎卞ú銊╁几?
     * @param title         婵犵數鍋為崹鐢告偋婵犲啫顕遍柛娑欐綑閸愨偓闂佹悶鍎洪崜锕傚汲?
     * @param body          婵犵數鍋為崹鐢告偋婵犲啫顕遍柛娑欐綑缁€鍐煕濞戝崬寮鹃柛?
     * @param bizType       濠电偞鍨堕幐濠氭嚌閻愵剚鍙忛柣鏃囨鐏忕敻鎮归崶顏勭毢闁?
     * @return 婵犵數鍋為崹鐢告偋婵犲啫顕遍柛娑卞灙閸嬫捇宕烽鐐扮钵缂?
     */
    private SysMessage createMessage(Long tenantId, Long receiverUserId,
                                      SysMessageTemplate template,
                                      SysMessageTemplateContent content,
                                      String title, String body, String bizType) {
        Long senderUserId = UserContext.get();

        SysMessage message = new SysMessage();
        message.setTenantId(tenantId);
        message.setSenderTenantId(tenantId);
        message.setSenderUserId(senderUserId);
        message.setSenderName(resolveSenderName(senderUserId));
        message.setReceiverUserId(receiverUserId);
        message.setScope("INTERNAL");
        message.setTemplateCode(template.getTemplateCode());
        message.setMessageType(template.getMessageType() != null ? template.getMessageType() : DEFAULT_MESSAGE_TYPE);
        message.setPlatform(content.getPlatform());
        message.setTitle(title);
        message.setContent(body);
        message.setLinkUrl(content.getLinkUrl());
        message.setBizType(StringUtils.hasText(bizType) ? bizType : resolveBizType(template));
        message.setStatus(0);
        message.setDeleted(false);

        return message;
    }

    /**
     * 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏閻鏌熸潏鎹愬悅闁稿鎸婚幏鍛存煥鐎ｎ剚绨為梻浣告啞閼瑰墽绮旈幆鎵冲徔闂?
     *
     * @param senderUserId 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴炲椤﹂攱鎱ㄩ悷閭︽█闁诡喕绮欐俊鎼佹晝閳ь剟鎮￠弮缍?
     * @return 闂備礁鎲￠悷锕傚垂閸ф鐒垫い鎴炲椤﹂攱鎱ㄩ悷閭︽█鐎规洏鍎遍濂稿炊?
     */
    private String resolveSenderName(Long senderUserId) {
        if (senderUserId != null) {
            return "User(" + senderUserId + ")";
        }
        return "System";
    }

    /**
     * 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊甸柤鎭掑劤閳绘梹鎱ㄥ鍡楀妞ゎ偁鍊楃槐鎺楊敃閵夘喖娈梺璇叉捣閸忔ɑ淇?
     * <p>
     * 濠电偞娼欓崥瀣晪闂佸憡蓱缁嬫帡骞忛崨顖涘磯闁靛闄勫▓銏ゆ⒑閸濆嫬鈧粙锝炴径灞惧厹闁割偅娲栭惌妤呮煕鐏炲墽鈯曟繛鍫ｆ硾闇夐柤娴嬫櫅瀛濋梺鍛婄懃濡繈骞?bizType闂備焦瀵х粙鎴︽嚐椤栫偛绠氬〒姘ｅ亾鐎规洘鑹鹃埢搴㈡償閳锯偓閺嬪繘姊哄ú缁樺▏闁告柨绉烽崯顖炴⒑閸濆嫷妲搁柣蹇旂箞楠炲啴寮撮悩鐢电厠婵☆偊顣﹂懗鍓佺矙婵犲倵妲堥柟鎯х摠閵囨繄绱掗鍛仯闁瑰嘲顑夋俊鍫曞礋椤撶姵鍤岄梻浣告啞濡垹妲愰弴鐘冲仏妞ゆ劧绠戦崹鍌炴倵閿濆啫濡烽柛?
     * </p>
     *
     * @param template 婵犵妲呴崹顏堝礈濠靛棭鐔?
     * @return 濠电偞鍨堕幐濠氭嚌閻愵剚鍙忛柣鏃囨鐏忕敻鎮归崶顏勭毢闁?
     */
    private String resolveBizType(SysMessageTemplate template) {
        if (template == null) {
            return null;
        }
        if (StringUtils.hasText(template.getBizType())) {
            return template.getBizType().trim();
        }
        // 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡堕崨鍌滃枛閸╁嫰宕橀埡鍐ㄥ殥缂傚倸鍊搁崐褰掓偋濠婂牊鍋夋繝濠傚婵瓨绻濇繛鎯т壕闁荤姵鍔楅崰鎰矙婢跺娼╂い鎺嗗亾妞ゎ偁鍊楃槐鎺楊敃閵夘喖娈梺?
        return template.getTemplateCode();
    }

    /**
     * SSE 闂備浇顫夋禍浠嬪礉閹达箑鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤犵偘绶氶、娑樜熷畡棰佸?
     *
     * @param tenantId      缂傚倷绀侀ˇ閬嶅窗閹版澘鏄ユ慨鍦嫃
     * @param receiverUserId 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╄尙绱掗埄鍐潷
     * @param message       婵犵數鍋為崹鐢告偋婵犲啫顕遍柛娑卞灙閸嬫捇宕烽鐐扮钵缂?
     */
    private void pushMessage(Long tenantId, Long receiverUserId, SysMessage message) {
        try {
            SysMessageVO vo = toVO(message, tenantId);
            sseEmitterService.sendToUser(tenantId, receiverUserId, "message", vo);
        } catch (Exception e) {
            log.error("SSE 闂備浇顫夋禍浠嬪礉閹达箑鐒垫い鎴ｆ硶閸斿秶绱掓径灞藉幋妤犵偘绶氶、娑橆潩閵夈倖婢€闂? tenantId={}, receiverUserId={}", tenantId, receiverUserId, e);
        }
    }

    /**
     * 闂佽绻愮换鎰涘▎鎴犵煋闁绘垼妫勭粻鎺撱亜閺嶃劍鐨戦柡澶庢闇夐柨婵嗘瀹曞嫭绻涢弶鎴烆棦妤犵偞鍨块敐鐐侯敇閿涘嫷妲?VO闂?
     *
     * @param msg               婵犵數鍋為崹鐢告偋婵犲啫顕遍柛娑卞灙閸嬫捇宕烽鐐扮钵缂?
     * @param receiverTenantId  闂備浇顫夋禍浠嬪磿閺屻儱鏋侀柛锔诲幘閻捇鏌ｉ弮鍌ょ劸闁绘挻妞孌
     * @return 婵犵數鍋為崹鐢告偋婵犲啫顕?VO
     */
    private SysMessageVO toVO(SysMessage msg, Long receiverTenantId) {
        SysMessageVO vo = new SysMessageVO();
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
        return vo;
    }

    /**
     * 濠电姰鍨煎▔娑氣偓姘煎櫍楠?MQ 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂?
     * <p>
     * RocketMQ 婵犵數鍋為崹鐢告偋閹版澘鍨傞柛宀€鍋涚粈鍌炴煏婢跺牆鍔氱紓宥嗘崌閺屻劌鈽夊Ο缁樻嫳闁诲骸鐏氱划搴ㄥ箯閻樼粯鐓ラ悗锝庡墯閸曠偓绻涢幋鐐存儎闁告ɑ鍎抽埢鎾诲箣閻樼數鐓嬮梺缁樻⒒椤牓鎮￠弴鐐╂闁瑰墽顒插妤冪磼濡も偓閹虫ê顕ｉ鍕倞鐟滄粌危闁秵鐓熼柣鎰级椤ョ偤鎳氶埡鍛厸濠㈣泛顑呴婊呯磼婢跺苯鍘存鐐扮窔椤㈡稑螣瀹勯澹?     * </p>
     *
     * @param request MQ 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗犻幃褰掑炊椤掍焦鏆犻梺?
     */
    @Transactional(rollbackFor = Exception.class)
    public void processTemplateMessageFromMq(com.forgex.common.mq.message.TemplateMessageRequest request) {
        if (request == null || !StringUtils.hasText(request.getTemplateCode())) {
            log.warn("MQ 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗犻幃褰掑炊椤掍焦鏆犻梺娲讳簽婢ф绮欐径濠庡悑闁告侗鍠栭埀顑惧€栫换? request={}", request);
            return;
        }
        if (request.getTenantId() == null) {
            log.warn("MQ 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗滅槐鎾诲磼濮橆厽鍎撻梺娲荤厛閸ㄥ磭鍒掗弮鍫熷亜缂佸娉曢崢鐩淒: templateCode={}", request.getTemplateCode());
            return;
        }

        Long oldTenantId = TenantContext.get();
        Long oldUserId = UserContext.get();
        try {
            TenantContext.set(request.getTenantId());
            UserContext.set(request.getSenderUserId());

            SysMessageTemplate template = findTemplateByCode(request.getTemplateCode());
            if (template == null) {
                log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濆吹閳绘梻鈧箍鍎遍幊鎰板箺閻樼粯鐓? templateCode={}", request.getTemplateCode());
                return;
            }
            if (!Boolean.TRUE.equals(template.getStatus())) {
                log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖欒閸熷懘鏌ょ喊鍗炲⒒婵炶缍侀弻? templateCode={}", request.getTemplateCode());
                return;
            }

            List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
            if (contents.isEmpty()) {
                log.warn("婵犵妲呴崹顏堝礈濠靛棭鐔嗘慨妞诲亾鐎规洘锕㈠畷銊╊敇瑜嶉弲锝夋⒒娴ｈ绶茬紒澶婄埣閹潡宕卞Ο灏栨灃闂佹儳绻愰崢婊堝极? templateCode={}", request.getTemplateCode());
                return;
            }

            List<Long> receiverUserIds = resolveReceivers(template, request.getReceiverUserIds());
            if (receiverUserIds.isEmpty()) {
                log.warn("闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╂煡鏌涘┑鍡楊仼闁诲繑鐟╅幃璺衡槈閺嵮冾瀱闁荤姵鍔楅崰鎾跺垝? templateCode={}", request.getTemplateCode());
                return;
            }

            int sentCount = 0;
            for (SysMessageTemplateContent content : contents) {
                if (!PLATFORM_INTERNAL.equals(content.getPlatform())) {
                    continue;
                }

                String title = fillPlaceholders(content.getContentTitle(), content.getContentTitleI18nJson(), request.getDataMap());
                String body = fillPlaceholders(content.getContentBody(), content.getContentBodyI18nJson(), request.getDataMap());

                for (Long receiverUserId : receiverUserIds) {
                    if (receiverUserId == null) {
                        continue;
                    }
                    SysMessage message = createMessage(request.getTenantId(), receiverUserId, template, content, title, body, request.getBizType());
                    if (StringUtils.hasText(request.getSenderName())) {
                        message.setSenderName(request.getSenderName());
                    }
                    messageMapper.insert(message);
                    pushMessage(request.getTenantId(), receiverUserId, message);
                    sentCount++;
                }
            }

            log.info("MQ 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗曢…璺ㄦ崉閸濆嫷浼€闂佽鍠栭敃锕傚焵椤掑倹鍤€闁哄牜鍓熷畷? templateCode={}, tenantId={}, receiverCount={}, sentCount={}",
                    request.getTemplateCode(), request.getTenantId(), receiverUserIds.size(), sentCount);
        } catch (Exception e) {
            log.error("MQ 婵犵妲呴崹顏堝礈濠靛棭鐔嗘俊顖濄€€濡插牓鏌涢銈呮瀺缂佽鲸鐗曢…璺ㄦ崉閸濆嫷浼€闂佽鍠栭敃銉ヮ嚗閸曨剚缍囨い鎰╁剾? templateCode={}, tenantId={}",
                    request.getTemplateCode(), request.getTenantId(), e);
            throw e;
        } finally {
            if (oldTenantId != null) {
                TenantContext.set(oldTenantId);
            } else {
                TenantContext.clear();
            }
            if (oldUserId != null) {
                UserContext.set(oldUserId);
            } else {
                UserContext.clear();
            }
        }
    }

    /**
     * 闂佽崵鍠愰悷杈╁緤妤ｅ啯鍊靛ù鐘差儏缁犳娊鏌曟径娑㈡闁哄棭浜滈湁?ID 闂備礁鎲＄敮妤呫€冩径鎰?
     * <p>
     * 濠电偞娼欓崥瀣晪闂佸憡蓱缁嬫帒顕ラ崟顖氱妞ゆ挾鍠庨埀顒傚仦娣囧﹪顢涘Δ鈧晶鍙夌節閵忊埄鎴犵矙婢舵劦鏁囬柣妯虹－閺?CUSTOM 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤╁弶绻涘顔荤盎閻㈩垰纾槐鎾诲礃閹勭彲缂?
     * 闂備礁鍚嬮惇褰掑磿閼艰泛鏆為梻浣告惈椤︻垶鎮樺┑瀣畾?CUSTOM 濠电偞鍨堕幐璇参涙笟鈧、姘额敆閳ь剚鏅ラ梺绋挎湰閻熝囨倷婵犲洦鐓曢煫鍥ь儏閸旀瑧绱掗…鎺旂瘈妤犵偞甯℃俊鐑芥晜閼恒儲鐦戝┑鐐茬摠缁海浜搁妸褎顫曟繝闈涱儏缁€鍡樼箾閸℃顎楅柍璇茬箻閺岋綁锝為鈧慨鈧銈嗘处閸撴瑦鏅ラ梺绋挎湰缁嬫垶寰勫澶嬬厸闁逞屽墴瀵敻宕归銈嗙盀闂?     * 闂備礁鎲￠悢顒傜不閹达箑鍨傛い鏍仜閻愬﹪鏌ｉ幇顖樷偓鈧柛瀣崌閸┾偓妞ゆ帒瀚粈鍡涙煟濡も偓閻擃剚绗熼埀顒€顕ｉ崼鏇為敜婵°倓绶氶妶顖炴⒑鐠団€冲幐闁绘帪濡囬弫顕€骞橀鑲╁摋闂佸搫鐗嗛悘姘舵儑娴犲鈷戦柡澶庢硶鑲栭梺姹囧€曞Λ婵囦繆?     * </p>
     *
     * @param template 婵犵妲呴崹顏堝礈濠靛棭鐔?
     * @param requestReceivers 闂佽崵濮村ú顓㈠绩闁秵鍎戦柣妤€鐗忛埢鏃€銇勯幘璺盒ｉ柣锝変憾閺岀喖骞侀幒鎴濆闂佽桨娴囬～澶岀矉瀹ュ浼犻柛鏇ㄥ亞娴犳岸鏌?     * @return 闂備浇顫夋禍浠嬪磿閺屻儱鏋侀悷娆忓椤?ID 闂備礁鎲＄敮妤呫€冩径鎰?
     */
    private List<Long> resolveReceivers(SysMessageTemplate template, List<Long> requestReceivers) {
        List<SysMessageTemplateReceiver> receivers = findTemplateReceivers(template.getId());
        boolean hasCustomReceiver = receivers.stream()
                .anyMatch(r -> "CUSTOM".equalsIgnoreCase(r.getReceiverType()));

        if (hasCustomReceiver && requestReceivers != null && !requestReceivers.isEmpty()) {
            Set<Long> distinctUserIds = new LinkedHashSet<>();
            for (Long requestReceiver : requestReceivers) {
                if (requestReceiver != null) {
                    distinctUserIds.add(requestReceiver);
                }
            }
            return new ArrayList<>(distinctUserIds);
        }

        List<Long> configuredReceivers = resolveReceiverUserIds(receivers);
        if (!configuredReceivers.isEmpty()) {
            return configuredReceivers;
        }

        if (requestReceivers != null && !requestReceivers.isEmpty()) {
            Set<Long> distinctUserIds = new LinkedHashSet<>();
            for (Long requestReceiver : requestReceivers) {
                if (requestReceiver != null) {
                    distinctUserIds.add(requestReceiver);
                }
            }
            return new ArrayList<>(distinctUserIds);
        }
        return Collections.emptyList();
    }
}

