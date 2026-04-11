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
package com.forgex.auth.service.impl;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysTenant;
import com.forgex.auth.domain.entity.SysUser;
import com.forgex.auth.domain.entity.SysUserTenant;
import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.dto.SysUserDTO;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.mapper.SysTenantMapper;
import com.forgex.auth.mapper.SysUserMapper;
import com.forgex.auth.mapper.SysUserTenantMapper;
import com.forgex.auth.mapper.SysInviteCodeMapper;
import com.forgex.auth.mapper.SysInviteRegisterRecordMapper;
import com.forgex.auth.service.AuthService;
import com.forgex.auth.service.LoginLogService;
import com.forgex.auth.strategy.AuthTerminalConstants;
import com.forgex.auth.strategy.LoginTypeConstants;
import com.forgex.auth.strategy.captcha.CaptchaStrategyFactory;
import com.forgex.auth.strategy.captcha.CaptchaValidationResult;
import com.forgex.auth.strategy.login.LoginStrategyFactory;
import com.forgex.auth.strategy.tenant.ChooseTenantStrategyFactory;
import com.forgex.common.config.ConfigService;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.dynamic.datasource.annotation.DS;

import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.json.JSONUtil;
import java.nio.charset.StandardCharsets;
import com.forgex.common.domain.config.CaptchaConfig;
import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.LoginSecurityConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.Duration;
import java.time.LocalDateTime;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.common.crypto.CryptoProviders;

/**
 * 闂備浇宕垫慨鎶芥⒔瀹ュ纾规繛鎴欏灪閸庡﹤顭块懜闈涘閻庢艾顦遍埀顒€绠嶉崕閬嶅箠鎼搭煉缍栭柕鍫濐槹閸婄敻鏌涢敂璇插箹濞寸姍鍕垫闁绘劕寮堕崰妯衡攽?
 * <p>
 * 闂備浇宕垫慨鐢稿礉濡ゅ懎绐楅柡鍥ュ灪閸庢淇婇妶鍛櫤闁稿骸鐭傞幃褰掑炊椤忓嫮姣㈢紓浣割槹鐎笛呮崲濞戞氨绀勯柣妯烘惈閸橈繝姊虹€圭媭鍤欓柛姘儑缁瑦寰勭€ｎ偄鍔呴梺鎸庣箓鐎涒晠宕犻悩缁樷拺闁荤喖鍋婇崵鐔兼煕韫囨棑鑰块柛鈹惧亾濡炪倖宸婚崑鎾淬亜閿旇棄顥嬮柍褜鍓氶崫搴ㄥ礉閹寸偘绻嗛悗娑欘焽閻熷綊鏌嶈閸撶喖骞冮弶搴撴瀻闁瑰鍋涢悵鐗堢箾鏉堝墽绉繛鍜冪秮瀹曞綊骞庨懞銉у幐閻庡箍鍎辩换鎺斿姬閳ь剟姊虹紒妯尖姇闁荤啿鏅滄穱濠勨偓娑櫳戞刊鎾煕濠靛嫬鍔滄い顐ｅ哺濮婂搫煤鐠囧弶鐏嗛梺缁樺釜缁犳捇骞冭閹棃濮€閳哄偆鍟嶉柣搴″帨閸嬫捇鏌嶈閸撶喖骞冨ú顏勭闁挎梻鏅崝鐢告⒑閸濆嫬顏╃紒缁樺笧閺侇噣宕卞☉娆戝弳濠电偞鍨惰摫婵犫偓娴煎瓨鐓涢柛鈩冪懃閺嬫稒顨ラ悙鏉戠伌妞ゃ垺鐟╅幃鎯х暆閳ь剟宕哄鍡欑?admin 闂備礁婀遍崢褔鎮洪妸鈺佺闁割偅娲栭悞?
 * </p>
 * <p>婵犵數鍋為崹鍫曞箰鐠囧弬锝夊箳濡炲皷鍋撻崨瀛樻櫇闁稿本姘ㄩˇ顕€姊虹紒妯活梿妞ゆ泦鍥у偍闁圭虎鍠楅悡?/p>
 * <ul>
 *   <li>{@link #login(LoginParam)} - 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹存瑥鐏柛搴＄焸閹綊宕堕鍕缂備礁顦锟犲蓟閵娿儮妲堟俊顖濆亹閸旑喚绱撴担铏瑰笡闁搞劌鐏濋悾鐑藉醇閺囩喎鈧兘鏌ｉ姀鐘典粵閻庢皜鍥ㄧ厽閹兼番鍊ゅ鎰叏濡濡奸柍缁樻崌楠炲鎮╅悽鐢靛帬闂備胶绮濠氣€﹂崼銉﹀亗闁哄洢鍨洪悡鏇㈡煙閻戞ɑ鎯勬繛鍫熺懇閺?/li>
 *   <li>{@link #chooseTenant(TenantChoiceParam)} - 闂傚倸鍊风欢锟犲磻閸曨垁鍥箥椤旂懓浜炬慨妯稿劚婵¤法绱掗鑺ヮ棃闁诡喓鍨荤划娆戞崉閵娿儱绠為梻鍌欑劍鐎笛呯矙閹达附鍎楀〒姘ｅ亾妞ゃ垺鐟╁鎾偐閻㈢绱存俊鐐€栧Λ渚€宕戦幇顔剧煋濠靛倸鎲￠悡鏇㈡煙閹咃紞閼叉牕顪冮妶搴′簴闁稿﹥绻堥悰顔锯偓锝庡枟閹层儵鏌涚仦鍓х煂闁瑰樊浜濈换娑㈠箣閻愭潙纰嶅銈嗗灥濡繂顕?/li>
 *   <li>{@link #secureAdmin()} - 缂傚倸鍊烽懗鑸靛垔鐎靛憡顫曢柡鍥ュ灩缁犳牕鈹戦悩鍙夋悙鐎瑰憡绻冩穱濠囶敍濠婂懎绗＄紓鍌氱М閸嬫捇姊婚崒姘偓鍝モ偓姘煎弮瀹曟繈寮介鐔蜂罕闁瑰吋鐣崺鍕枔?/li>
 *   <li>{@link #resetPasswordById(Long)} - 闂傚倸鍊烽悞锕併亹閸愵亞鐭撻柛顐ｆ礃閸嬵亪鏌涢埄鍐姇闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濞茬喖骞冨Δ鍛棃婵炴垶鐟Λ銈夋⒑?/li>
 *   <li>{@link #logout()} - 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹存瑥鐏柛搴＄焸閹綊宕堕妸銉хシ濠?/li>
 *   <li>{@link #updateTenantPreferences(String, List, Long)} - 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樻彃顏柣顓熸崌閺岋綁寮崒銈囧姼闂佺粯鎸诲ú鐔煎蓟濞戞埃鍋撻敐搴濈敖缂佺姳鍗抽弻鏇㈠醇閵忊剝鐝梺鐟板槻椤戝鐣峰鍡╂Ь闂?/li>
 * </ul>
 * <p>婵犵數鍋犻幓顏嗙礊閳ь剚绻涙径瀣鐎殿噮鍋婃俊鑸靛緞鐎ｎ偅鐝?/p>
 * <ul>
 *   <li>{@link #login(LoginParam)} 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓瑜版帗鍋樻い鏇楀亾妤犵偛妫濋獮宥夘敊缂併垹鏁奸梻鍌欑劍閹爼宕曢幎钘夌；婵炴垶鍤庢禍褰掓煛鐏炶鍔滈柣鎺曨嚙椤法鎹勯搹鍦姼濠电偛鍚嬫繛濠囧蓟閻旂厧绾ч柟瀛樼箖浜涢梻浣稿閻撳牊绂嶉悙鍝勭畺濞磋偐琛ラ埀顒婄畵楠炴帡寮懗顖楀亾?/li>
 *   <li>{@link #chooseTenant(TenantChoiceParam)} 闂傚倸鍊风欢锟犲磻閸曨垁鍥箥椤旂懓浜炬慨妯稿劚婵¤姤銇勯浣镐壕婵犵妲呴崹閬嶎敄濞嗘挸瑙﹂柛銉㈡櫇绾剧厧顭跨捄楦垮闁哄鍨块弻娑㈠煛閸屾粍鍒涢梺?/li>
 *   <li>闂傚倷绀佸﹢閬嶁€﹂崼銉嬪洭鎮界粙鍨亶闂侀潧鐗嗗ú銊у閻愵兙浜滈柡鍐ｅ亾妞ゆ垶鐟╁畷鐢稿閵堝棛鍘?{@code security.crypto.transport} 闂傚倷绀佸﹢閬嶆偡閹惰棄骞㈤柍鍝勫€归弶?SM2 婵犵數鍋熼ˉ鎰板磻閹邦厽鍙忛柛鎾楀嫷鍋ㄥ銈嗘尵婵敻宕伴崱娑欑叆婵犻潧妫欓崳鑺ヤ繆閸欏绀嬮柡灞诲妼閳藉螣閸噮浼冮梻浣告憸閸熷潡宕戦幘缁樷拺?{@code security.password.policy.store} 闂備礁鎼ˇ顐﹀疾濠婂懐鐭欓柡宥庡幑閳ь兛绶氶獮瀣偐椤愵澀澹曞┑鐐村灦椤忣亪顢旈崼婵堬紱閻庡箍鍎遍ˇ顖炲礃閳ь剟姊虹捄銊ユ灁濠殿喖鍢查悾鐑芥晲婢跺鍘?/li>
 * </ul>
 * <p>闂傚倷绀侀幉锟犳偡椤栫偛鍨傞弶鍫涘妽濞呯娀鎮归幁鎺戝婵炲矈浜弻锟犲炊閳轰焦姣愰梺绋款儐閹逛線篓閸屾粎纾?/p>
 * <ul>
 *   <li>闂傚倷娴囬妴鈧柛瀣尰閵囧嫰寮介妸褉妲堥梺浼欏瘜閸ｏ綁寮诲☉銏℃櫆闁告繂瀚～鍥⒑閸涘婊堝磹濡ゅ啯顫曢柡鍐ㄧ墱閺佸﹪鏌ゅù瀣珕闁汇劍鍨垮娲捶椤撗勬瘜闂佸搫鎳忛惄顖炲箚閸繍鍚嬮柛鈾€鏅滈鏃堟倵閸忓浜鹃梺鍛婂姦娴滄繈锝炲澶嬧拺婵炶尪顕ч弸娆撴煟閹虹偟鐣抽柛鐕佸灦閹鈻撻崹顔界亪缂備浇顕ч鍥╁垝椤撶偟鏆嗛柍褜鍓熼崺鈧い鎺戝濞懷勪繆椤愶絿娲撮柟顔哄劚椤劑宕ㄩ鐐╂敽闂備礁鎼粙渚€宕戦崨顓涙灁闁挎繂顦伴悡?/li>
 *   <li>闂傚倷绀侀幖顐﹀磹閻熼偊鐔嗘慨妞诲亾鐠侯垶鏌涢幇鍏哥按闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掓惔锝呭灊閻犲洤寮弮鈧幏鍛村箒閹哄棗浜惧┑鐘叉处閻撴盯鏌涢幇闈涘箺缁绢厾鍋撶换婵嬪焵?common 濠电姷顣藉Σ鍛村垂椤忓牆鐒垫い鎺戝暞閻濐亪鏌涚€ｃ劌鍔﹂柡?Provider 婵犵數鍋為崹鍫曞箹閳哄懎鍌ㄩ柤鎭掑劜濞呯娀鎮归幁鎺戝婵炲矈浜弻锟犲炊閵婂洦宀稿畷?/li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.auth.service.AuthService
 * @see com.forgex.common.crypto.CryptoPasswordProvider
 * @see com.forgex.common.domain.config.CryptoTransportConfig
 * @see com.forgex.common.domain.config.PasswordPolicyConfig
 */
@Service
@DS("admin")
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String KEY_SECURITY_PASSWORD_POLICY = "security.password.policy";
    private static final String KEY_SECURITY_LOGIN_FAILURE = "security.login.failure";
    private static final String LOGIN_FAIL_COUNT_KEY_PREFIX = "fx:auth:login:fail:";
    private static final String LOGIN_LOCK_KEY_PREFIX = "fx:auth:login:lock:";
    @Autowired
    private SysTenantMapper tenantMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysUserTenantMapper userTenantMapper;
    @Autowired
    private ConfigService configService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    private com.forgex.common.util.IpLocationService ipLocationService;
    @Autowired
    private LoginStrategyFactory loginStrategyFactory;
    @Autowired
    private ChooseTenantStrategyFactory chooseTenantStrategyFactory;
    @Autowired
    private CaptchaStrategyFactory captchaStrategyFactory;


    /**
     * 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓瑜版帗鍋樻い鏇楀亾妤犵偛妫濋獮宥夘敊缂併垹鏁奸梻鍌欑劍閹爼宕曢幎钘夌；闁绘梻銆嬬紞鏍煏閸繍妲哥紒鐙呯悼閳ь剛鎳撶€氼厼顭垮鈧幃妯衡枎閹炬潙浠╁┑鐐村灦閻楁洘淇婇幖浣圭厓?
     * <p>
     * 闂備浇顕уù鐑藉箠閹捐瀚夋い鎺戝濮规煡鏌ㄥ┑鍡╂Ч闁稿骸鐭傞幃褰掑炊椤忓嫮姣㈢紓浣割槹鐎笛呮崲濞戞氨绀勯柣妯烘惈閸橈繝姊虹€圭媭鍤欓柕鍫熸倐瀵?
     * </p>
     * <ol>
     *   <li>闂傚倷绀侀幉锟犳偡閵夆晛纾圭憸鐗堝笒濮规煡鏌ｉ弮鍌氬付闁告劏鍋撻梻浣芥硶閸犳挻鎱ㄥ畡鎵殾闁挎繂顦伴悡銉︾箾閹寸偟鎳呮い锝呭级閵囧嫰濡烽妷锕€娈楅悗娈垮枛椤攱淇婇悜钘壩╅柕澶樺枟琚╅梻鍌欑閹诧繝鎮烽妷鈺佸簥闁哄被鍎辩粻顖炴煙濞堝灝鏋撻柛瀣崌濡啫鈽夊▎妯伙骏闂備胶绮悧妤冨垝閹炬剚鍤曞ù鍏兼儗閺佸秵鎱ㄥ鍡楀箹闁告挶鍔嶇换娑㈠箣閻愭潙纾╁┑鐐叉嫅缂嶄線宕?/li>
     *   <li>闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃澒闁稿鎹囬悰顕€宕归鍙ョ棯闂備胶纭堕弲婊呯矙閹捐泛鍨濋柍鍝勬媼閺佸秵绻涢崱妤佹崳缂佺姵宀稿铏规嫚閳ュ啿瀛ｉ梺鎼炲妿閺咁偄危閹扮増鍎岄柟?闂傚倷绶氬濠氭⒔閸曨偒鐔嗘俊顖欒閻掍粙鏌涢幇闈涙灈鏉╂繃绻涢幋鐐寸叄濠㈠墎鍓硆-Agent闂傚倷绶氬褍螞閺冨倻鐭嗗ù锝夋交閼板潡鏌熺紒銏犳灍闁稿濞€閺屾盯鍩勯崘鈺冾槶缂備礁顦顓犳?/li>
     *   <li>闂備浇顕ч柊锝咁焽瑜嶈灋婵炴垯鍨洪崑澶嬬節婵犲倹鍣洪柛鐘叉閺屻劌鈹戦崱妯烘濠碘€冲级濞茬喖寮婚妸銉㈡婵炲棙甯掗ˉ婵嗩渻閵堝倹鏆橀柛銉戝啫娈ゆ繝鐢靛Т閿曘倝宕鐐茬？婵炲棙鎸婚悡鐔搞亜椤愵偄澧┑陇娅ｇ槐?SM2 婵犵數鍋熼ˉ鎰板磻閹邦厽鍙忛柛鎾楀嫷鍋ㄥ銈嗘尪閸ㄥ湱绮堟径鎰厽婵☆垰鐏濋惃鐑樹繆閸欏绀嬮柡灞诲妼閳藉螣閼测晛濮伴柣鐐寸閻熲晠骞冨Δ鍛棃婵炴垶鐟Λ鐐烘⒑娴兼瑩妾柛銊ユ健楠?/li>
     *   <li>闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹存瑥鐏╅柦鍐枛閺屾洘寰勫Ο鐓庡弗闂佹悶鍊曠€氫即寮婚妸銉㈡婵炲棙鍩堥弳锟犳⒑缂佹﹩娈曢柟鍛婂▕閻涱噣骞嬮敂缁樻櫓闂佸壊鐓堥崳顔嘉ｉ柆宥嗏拺闁告稑锕﹂幊鍥煛閳ь剟鏌嗗鍡椾簵闂佹寧绋戠€氼厾鈧碍宀搁弻鏇＄疀鐎ｎ亞浠鹃梺鍝勬缁绘﹢寮婚悢鍝勵棜閻庯綆鍋夐崥鍌涚箾閿濆懏鎼愰柨鏇ㄤ邯閻?/li>
     *   <li>闂備浇顕ч柊锝咁焽瑜嶈灋婵炴垯鍨洪崑澶嬬節婵犲倸顏存繛鍫㈡櫕閳ь剙鍘滈崑鎾绘煕閹板吀绨兼い顐㈢Ч濮婃椽骞愭惔锝傚缂佺偓婢樼粔褰掑箖瑜旈獮姗€顢欓懖鈺婂敼婵＄偑鍊栧Λ渚€宕戦幇顑╋綁寮撮姀锛勫幗濡炪倖姊瑰Σ鎺楀礂瀹€鍕厽閹烘娊宕曢悽绋跨畺濡わ絽鍟敮濡炪倖鐗楃划搴ㄋ囬妶娓價ypt/SM2闂傚倷鐒︾€笛呯矙閹次诲洭鎮介崨濠備粧濠电偛妫欓崹鍓佲偓姘壘闇夐柨婵嗘噺閹插憡淇婇崣澶嬬闁?/li>
     *   <li>婵犲痉鏉库偓妤佹叏閹绢喗鍎楀〒姘ｅ亾闁诡垯鐒﹀鍕箛椤撶偛澹撳┑鐐舵彧缁茶棄锕㈡潏銊︽珷闁炽儲绶為弮鍫熷亹闁肩⒈鍓涢崝鍦磽娴ｇ缍侀柛妤€鍟块锝夋偩鐏炴儳鏋傞梺鍛婃处閸嬪棝濡堕敃鍌涒拻濞达綀顫夌欢鑼磼婢跺﹦鍩ｉ柟顔兼健瀹曞崬鈽夊Ο鍝勬暏闂備浇娉曢崰鎾存叏瀹勬壆鏆﹂柨婵嗩槹閻撴洟鏌曟径鍫濆姎闁抽攱鏌ㄩ…鑳槼闁绘顨婇妴鍐Ψ閵壯勬畷闂佸憡鍔︽禍婵嬶綖瀹ュ鈷戞繛鑼额嚙閺嬫瑩鏌ｉ幒鐐电暤鐎规洖缍婃俊鎼佸Ψ椤旀儳寮抽梻浣烘嚀閻°劎鎹㈤崘顔肩；闁靛鍎欓弮鍫熷亹闁肩⒈鍓涢崝顕€姊烘导娆戠暠妞わ妇鏁婚獮?/li>
     *   <li>缂傚倸鍊风粈渚€藝闁秴绐楅柟鐗堟緲閺勩儲绻涢幋娆忕仼闁藉啰鍠栭弻鏇熷緞濡厧甯ラ梺鎼炲€曠€氫即寮婚妸銉㈡婵炲棙鍩堥弳锟犳⒑缂佹ɑ鎯堢紒杈ㄦ礋楠炲繘鎮╃拠宸綂闂侀潧绻嗛崜婵嗏枔閵忋倖鈷戦悷娆忓鐏忥附銇勯妷锔藉暗闁告帗甯￠弫鍌炴煥椤栨矮澹曢柣鐔哥懃鐎氼參鎯冮幋鐐电闁割偆鍠庨悘瀛橆殽閻愯尙效闁糕斁鍋撳銈嗗笒鐎氼剛鈧艾顦…璺ㄦ崉閾忓湱鍔稿┑鐐插悑婵炲﹪寮?/li>
     *   <li>闂備礁鎼ˇ顐﹀疾濠婂牆钃熼柕濞垮剭濞差亜鍐€妞ゆ劑鍊楅悡瀣⒑缂佹ɑ鈷掓い顓炵墦閹繝寮撮姀锛勫幈闂佸湱鍎ら幖鈺佲枔濞嗘挻鐓忛柛鈩冾殔閺嗭綁鏌℃担鍝バ㈡い顐ｇ箞椤㈡宕掑☉鍗炴櫏缂傚倸鍊风粈渚€藝闁秴绐楅柟鐗堟緲閺勩儲绻涢幋鐐茬劰闁稿鎹囧畷鐑筋敇閻愭壆閽电紓鍌氬€哥粔鎾敄閸℃鐒芥い蹇撶墱閺佸棗霉閿濆浂鐒剧紒銊ｅ劜缁?VO 闂備礁鎼ˇ顐﹀疾濠婂牆钃熼柕濞垮剭?/li>
     * </ol>
     * <p>
     * 闂備浇顕уù鐑藉箠閹惧嚢鍥敍閻愯尙鐓戦棅顐㈡处缁嬫帡宕戠€ｎ喗鍋℃繛鍡楃箰椤忋倝鏌涚€ｎ偅宕勬い褍鍊荤槐?
     * </p>
     * <ul>
     *   <li>闂傚倷娴囬妴鈧柛瀣尰閵囧嫰寮介妸褉妲堥梺?SM2 闂傚倷鐒﹂幃鍫曞磿閺夋嚦娑㈠礋椤栨稑鍓ㄩ梺鐟邦嚟婵攱绋夊鍡曠箚闁靛牆瀚崗宀勬煏閸℃韬柡灞剧洴瀵噣宕掑В瑁ゅ灲閺岋綁鏁傞挊澶屼桓闁诲酣娼ч妶鎼佸箖閳哄懏鎯為柛锔诲幘閸婃娊姊虹拠鏌ュ弰婵炰匠鍏炬稑鈽夐姀鈥充函?/li>
     *   <li>闂傚倷娴囬妴鈧柛瀣尰閵囧嫰寮介妸褉妲堥梺?bcrypt 闂?SM2 闂傚倷绀侀幉鈥愁潖缂佹ɑ鍙忛柟缁㈠枟閸庡﹪鏌熺€电袥闁稿鎸搁埥澶娾枍椤撗傞偗鐎规洘鍨甸濂稿炊閿濆懍澹曢梻鍌氱墛缁嬫帡藟閻樼粯鐓?/li>
     *   <li>闂傚倷娴囬妴鈧柛瀣尰閵囧嫰寮介妸褉妲堥梺浼欏瘜閸ｏ綁寮诲☉銏犖ㄩ柕澶堝劗閹稿啯绻濋埛鈧崟顓烆潷濡炪們鍔婇崕閬嶆偩濠靛绀冩い蹇撴媼濞兼棃姊绘担瑙勩仧闁哥姵娲滅划鍫熺瑹閳ь剟鐛箛娑樼骇婵炲棗澧介崣鍡涙⒑閻熸壆鎽犵紒璇插暣瀹曟垿濡烽妷搴㈡閹晠鎳犻鍌氬О闂備椒绱粻鎴λ囬悽绋跨畾鐎广儱妫欐刊鎾煟閵堝骸鐏欓柟鍑ょ磿缁辨挻鎷呴崜鍙壭︾紓浣割槷缁插宕鐐村€?/li>
     *   <li>闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹥鍋ラ柡宀€鍠撻埀顒佺⊕閿氬┑顔兼搐閳?婵犵數濮伴崹娲磿閼测晛鍨濋柛鎾楀嫬鏋傞梺鎸庢礀閸婂摜鐚惧澶嬬厪濠㈣泛鐗嗛崝銈囩磼?/li>
     * </ul>
     *
     * @param param 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閻熸壆鏆﹂柨婵嗩槸绾惧吋绻涢幋鐐垫噭妞ゆ柨绉瑰娲箰鎼达絺妲堥梺鍏兼た閸ㄦ娊鍩€椤掆偓閻忔岸鏁冮姀鐘垫殾闁靛鏅╅弫鍐煏婢跺牆鍔存俊鏌ヤ憾濮婃椽宕ㄦ繝鍕櫗闂佽绻戝畝鎼佸春閳ь剚銇勯幋锝嗙《妞わ讣绠撻弻锝夋晜閽樺浼岄梺璇″灠缁夌敻骞忛崨顖氭瀳婵☆垵妗ㄦ竟鏇熺箾閹炬潙鐒归柛瀣崌閺岋絽鈽夊▎鎴炲櫘闂佸綊顥撴慨鐢稿箯閸涘瓨鎯為柣鐔稿娴狀垳绱撻崒娆戝妽缂佸鍨胯棟妞ゆ梻鏅埞宥呪攽閻樺弶鎼愮紒?
     * @return {@link R} 闂傚倷绀侀幉锟犳偋閺囥垹绠犻幖娣妼缁犳岸鏌涢鐘茬伄闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺?VO 闂傚倷绀侀幉锛勬暜濡ゅ懌鈧啯寰勯幇顑┿儵鏌涢幇闈涙灍闁稿骸绉归弻娑㈠即閵娿儱绠婚梺绯曟櫇婵挳鍩ユ径鎰妞ゆ牗鐭竟鏇㈡⒑閸濆嫷妲撮柡鍛矒瀵濡搁妷鍐╂そ瀵粙顢曢妶鍥风床闂備浇顫夐崕鎶筋敋椤撱垹鐒?
     * @throws BusinessException 闂佽崵鍠愮划搴㈡櫠濡ゅ懎绠扮紒瀣儥閸ゆ洘銇勯幒鎴濐仼闁哄绶氶弻锝呂旈埀顒勬偋韫囨梹娅犻柍銉︾窞閺冨牊鍋愰柤鑹版硾椤忣參姊洪棃娑欏闁搞劌缍婇獮蹇氥亹閹烘嚦銊╂煥濠靛棗鈧憡绂嶉幆顬″綊鏁愰崶褍濡洪梺鍝勬缁绘﹢寮婚悢鍝勵棜閻庯綆鍋夐崥鍌滅磽娴ｈ娈旀い锕備憾閸┾偓妞ゆ帊鑳堕埊鏇㈡煥濮橆厾绡€闁逞屽墴椤㈡棃宕煎顏傚劚闇夐柨婵嗘噺閹插憡淇婇崣澶嬬闁哄矉缍侀、娆撴嚒閵堝洨宕查梻浣芥〃缁€渚€鏁冮敃鍌氱闁绘ɑ妞块弫宥嗕繆椤栨艾妲诲ù婊勫劤閵嗘帒顫濋敐鍛闂備胶绮粙鎺楁偡瑜旈獮蹇涙偐閾忣偄顎撻梺鎯х箳閹虫捇鎮橀敐澶嬧拻濞达絿鎳撻婊堟煟閻旀潙濮傛い銏″哺婵¤埖寰勬繝鍕綁闂備礁鍚嬮幃鍌氼焽瑜旈敐鐐哄籍閸喓鍘?
     * @see com.forgex.auth.domain.param.LoginParam
     * @see com.forgex.auth.domain.vo.TenantVO
     * @see com.forgex.common.domain.config.CryptoTransportConfig
     * @see com.forgex.common.domain.config.PasswordPolicyConfig
     * @see com.forgex.common.domain.config.CaptchaConfig
     */
    @Override
    public R<List<TenantVO>> login(LoginParam param) {
        String loginTerminal = resolveLoginTerminal(param == null ? null : param.getLoginTerminal());
        String loginType = resolveLoginType(param == null ? null : param.getLoginType());
        if (param != null) {
            param.setLoginTerminal(loginTerminal);
            param.setLoginType(loginType);
        }
        return loginStrategyFactory.getStrategy(loginTerminal, loginType).login(param);
    }

    public R<List<TenantVO>> doAccountPasswordLogin(LoginParam param) {
        // 婵犵數鍋涢顓熸叏鐎涙﹩娈界紒瀣儥閸ゆ洘銇勯幒鎴濐仼闁哄绀佽灋濡鐒︾粊浼存煕濮楀棗澧撮柡宀€鍠撶槐鎺懳熼崫鍕垫綋闁荤喐绮嶅妯好洪妸鈺佺鐟滅増甯楅弲婵嬫煃瑜滈崜婵嬪Φ?
        String account = param == null ? null : param.getAccount();
        // 婵犵數鍋涢顓熸叏鐎涙﹩娈界紒瀣儥閸ゆ洘銇勯幒鎴濐仼闁哄绀佽灋濡鐒︾粊浼存煕濮楀棗澧撮柡宀€鍠撶槐鎺懳熼崫鍕垫綋闁荤喐绮嶅妯好洪妸鈺佺劦妞ゆ帒瀚☉褎淇婇锝囨创闁?
        String password = param == null ? null : param.getPassword();
        
        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃澒闁稿鎹囬悰顕€宕归鍙ョ棯闂備胶纭堕弲婊呯矙閹捐泛鍨濋柍鍝勬噺閸嬵亝淇婇妶鍛煟闁哄本绋戦…銊╁礃閹冾潙ser-Agent
        String clientIp = getClientIp();
        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢姀鈺傜９er-Agent婵犵數鍎戠徊钘壝洪悩璇茬婵犻潧娲ら?
        String userAgent = getUserAgent();
        // 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺嗐倕鈽夐幙鍐╂啝闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁圭懓鐖奸弻鐔封枔閸喗鐏嶉梺绋匡功閸嬫盯婀侀梺鎸庣箓閹虫劘鍊撮梻浣圭湽閸婃洖螞濞嗘垹鐭氶梺顒€绉撮悞娲煕閹板苯鎳愰ˇ?
        String region = ipLocationService.getLocationByIp(clientIp);
        
        // 闂傚倷绀侀幉锟犳偡閵夆晛纾圭憸鐗堝笒濮规煡鏌ｉ弮鍌氬付闁告劏鍋撻梻浣芥硶閸犳挻鎱ㄥ畡鎵殾闁挎繂顦伴悡銉︾箾閹寸偟鎳佸ù婧垮灩椤法鎹勯崫鍕典純閻庤娲╃徊浠嬫偩閿熺姵鍋嬮柛顐ｇ箥娴煎懘姊虹拠鏌ュ弰婵炰匠鍏炬稑鈽夐姀鈥充函濠电姴锕ょ€氼噣鍩㈤弮鍌楀亾楠炲灝鍔氶柣妤€妫濆畷銏ゅ级閹存梹顫嶉梺瑙勫礃椤顢旈悩缁樼厓?
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            log.warn("Login failed: account or password is empty");
            // 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹦浠㈡い顓炴健楠炲棝骞嶉鍓у嫎闂佽崵鍠愰悷杈╃不閹炬剚鍤曟い鏇楀亾濠碉紕鍏樻俊鐤槾妞?
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "account or password is empty");
            return R.fail(CommonPrompt.ACCOUNT_OR_PASSWORD_EMPTY);
        }

        LoginSecurityConfig loginSecurityConfig = getLoginSecurityConfig();
        R<List<TenantVO>> lockResult = checkLoginLocked(account, loginSecurityConfig);
        if (lockResult != null) {
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "account is locked");
            return lockResult;
        }
        
        // 婵犵數濮烽。浠嬪焵椤掆偓閸熷潡鍩€椤掆偓缂嶅﹪骞冨Ο璇茬窞闁归偊鍓濊婵＄偑鍊栭悧妤冨垝瀹ュ姹查煫鍥ㄦ媼閻熼偊鐓ラ柛鎰ㄦ櫓濡稓绱撴担鐣岀翱闁糕晜鐗曢…鍥疀濞戞顔掗梺鍛婂姇濡﹤顭囬柆宥嗙厽閹兼番鍔嶅☉褎淇婇銏╁殶婵″弶鍔曢～婊堟儓?闂傚倷鐒︾€笛呯矙閹次层劑鍩€椤掑倻纾奸弶鍫涘妿缁犳牠鎮楅悽闈涘付閾伙綁鏌涘┑鍡楊仼鐎殿喗顨婇弻锝堢疀閹惧墎顔夌紓浣筋嚙濡繈骞嗘笟鈧、娑橆煥閸曨偅鐎梻浣瑰缁诲倿骞婇幇顑╋綁寮撮姀锛勫幈闂婎偄娲ら鍛村礉濮樻墎鍋撳▓鍨珮闁告挻鐩崺鈧い鎺戝濞懷勪繆椤愶絿娲撮柟?
        CryptoTransportConfig cryptoCfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        if (cryptoCfg != null && StringUtils.hasText(cryptoCfg.getPrivateKey()) && "SM2".equalsIgnoreCase(cryptoCfg.getAlgorithm())) {
            try {
                // 闂傚倷绀侀幉锛勬暜濡ゅ啰鐭欓柟瀵稿Х绾句粙鏌熺紒妯块浌2闂傚倷绀侀幉鈥愁潖缂佹ɑ鍙忛柟缁㈠枟閸庡﹪鏌熺€电袥闁稿鎹囬幃钘夆枔閹稿孩鏆為梻?
                SM2 sm2 = new SM2(cryptoCfg.getPrivateKey(), cryptoCfg.getPublicKey());
                // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃澒闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锛侀梻浣告惈椤戝懘宕弶鎴殨濞寸姴顑嗛崑鍌炲箹鏉堝墽鎮兼い?
                String cipherFmt = cryptoCfg.getCipher();
                // 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺嗐倕霉閿濆牄鈧偓闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锛侀梻浣告惈椤戝懘宕弶鎴殨濞寸姴顑嗛崑鍌炲箹鏉堝墽鎮兼い銏犳嚇閺岋綁鎮㈤崫銉﹀殏缂備浇寮撶划娆撳箚?
                if ("BCD".equalsIgnoreCase(cipherFmt)) {
                    // BCD闂傚倷绀侀幖顐ょ矓閸洖鍌ㄧ憸蹇撐ｉ幇鐗堟櫖闁告洦鍓欓弸鍌炴⒑閹稿海绠撻柟铏姇鍗?
                    byte[] plain = sm2.decryptFromBcd(password, KeyType.PrivateKey);
                    password = new String(plain, StandardCharsets.UTF_8);
                } else {
                    // 婵犳鍠楃敮妤冪矙閹烘せ鈧箓宕奸妷顔芥櫍婵犵數濮甸懝楣冨礃閳ь剟姊虹紒妯诲碍婵炴挳顥撳☉鐢稿焵椤掑嫭鐓熼柣鏂挎憸閹虫洜绱掗煫顓犵煓闁?
                    password = sm2.decryptStr(password, KeyType.PrivateKey);
                }
            } catch (Exception ignored) {
                // 闂備浇宕甸崰鎰版偡鏉堚晛绶ゅù鐘差儐閸庡﹪鏌熺€电浠滅紒鍓佸仱瀵爼宕奸顫嚱閻炴碍绻堝娲箰鎼达絺妲堟繝闈涘€瑰鍦崲濞戙垹鐐婃い鎺嶇娴犳椽姊洪棃娑辩劸闁稿酣浜跺顒冾槻闁宠鍨块、娆戝枈閸楃偛澹堥梻浣风串闂勫嫰宕归崸妤€绠?
            }
        }
        
        // 闂傚倷绀侀幖顐ゆ偖椤愶箑纾块柟缁㈠櫘閺佸淇婇妶鍛櫤闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濞叉牞褰侀梺鎼炲劵缁茶姤鏅堕幓鎹?
        String idKey = account;
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId, SysUser::getAccount, SysUser::getUsername, SysUser::getPassword, SysUser::getEmail, SysUser::getPhone, SysUser::getStatus)
                .eq(SysUser::getAccount, idKey));
        // 闂傚倷绀侀幖顐ょ矙閸曨厽宕叉繝闈涱儐閸嬫ɑ绻涢崱妯诲鞍闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濞茬喖寮婚敓鐘茬闂傚牊绋撴禒鈺呮⒑鐠団€崇仧闁煎啿鐖奸崺鈧い鎺嶈兌閳洟鏌ㄥ顓犵瘈闁?
        if (user == null) {
            log.warn("闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓闂堟侗鍤楅柛鏇ㄥ墯缂嶅洦銇勯幇鈺佸壘? 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹寸偟鎳呴柍缁樻⒒閳ь剙绠嶉崕閬嶅箠閹版澘绠洪柣妯肩帛閻? account={}", idKey);
            // 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹦浠㈡い顓炴健楠炲棝骞嶉鍓у嫎闂佽崵鍠愰悷杈╃不閹炬剚鍤曟い鏇楀亾濠碉紕鍏樻俊鐤槾妞?
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "user not found");
            recordLoginFailureState(account, loginSecurityConfig);
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        
        // 婵犲痉鏉库偓妤佹叏閹绢喗鍎楀〒姘ｅ亾闁诡垯鐒﹀鍕沪缁嬪じ澹曢梻鍌氱墛缁嬫帡藟閻樼粯鐓熼柡宥冨妿閻帡鏌℃担鍝バ㈡い顐ｇ箞椤㈡﹢鎮╁顔筋棓闂傚倷鑳堕、濠囶敋閺嶎厼绐楁慨妯垮煐閸嬨倝鏌曟径鍡樻珕闁稿鏅犻弻鏇熷緞濞戞氨鏆犻梺鎶芥敱濡啴骞冪捄琛℃婵☆垵鍋愰崝鍦磽娴ｄ粙鍝洪柣娑掓殨2 闂傚倷绀侀幉锟犳偡椤栫偛鍨傚ù锝呭枤閺嗕即姊虹拠鏌ュ弰婵炰匠鍏炬稑螖閸涱喖浠遍梺闈浥堥弲娑氬?/ bcrypt 闂傚倷绀侀幉锛勬崲閸岀偞鍋嬮柛鈩冪懅缁犳棃鏌涢妷顔煎闁?
        PasswordPolicyConfig policy = getPasswordPolicyConfig();
        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃澒闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掓惔銊ョ劦妞ゆ帊鑳堕埊鏇㈡煥濮樿埖鐓曢悗锝庝簽鑲栫紓渚囧枤閺佽顕ｆ禒瀣垫晝闁挎繂妫崥鍛存⒒娴ｇ懓顕滅紒瀣灴閹矂宕掑鍐ㄧ亰濡炪倖鐗楃划宥嗗垔婵傚憡鐓忛柛顐ｇ箓椤忣剛绱掗埀顒佸垔鐎规唵ypt
        String store = resolvePasswordStore(policy);
        // 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺嗐倕霉閿濆牊顏犲瑙勫▕閺屸€愁吋鎼粹€茬凹濠电偛鐗炵紞渚€寮婚妶澶婄畾鐟滃秴危閼姐倖鍠愰柡澶嬪瀹告繈鏌嶈閸撶喎顭囪铻炴繛鎴欏灪閸嬪绻濇繝鍌滃缂佺姷鏁婚弻鐔兼焽閿旇法鏁栭柡澶嗘櫆濡啴寮?
        CryptoPasswordProvider provider = CryptoProviders.resolve(store, configService);
        // 婵犲痉鏉库偓妤佹叏閹绢喗鍎楀〒姘ｅ亾闁诡垯鐒﹀鍕沪缁嬪じ澹曢梻鍌氱墛缁嬫帡藟閻樼粯鐓熼柡宥冨妿閻帞鈧鍠曢崡鎶姐€佸☉姗嗘僵妞ゆ挾鍋涙晶鐐節濞堝灝鏋熺紒缁樺姉瀵板﹪宕稿Δ鈧悘?
        boolean passOk = provider.verify(password, user.getPassword());
        if (!passOk) {
            log.warn("闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓闂堟侗鍤楅柛鏇ㄥ墯缂嶅洦銇勯幇鈺佸壘? 闂備浇顕ч柊锝咁焽瑜嶈灋婵炴垯鍨洪崑澶嬬節婵犲倸顏柍缁樻⒒閳ь剙绠嶉崕鍗炍涢銏犵闁逞屽墰缁? account={}", account);
            // 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹦浠㈡い顓炴健楠炲棝骞嶉鍓у嫎闂佽崵鍠愰悷杈╃不閹炬剚鍤曟い鏇楀亾濠碉紕鍏樻俊鐤槾妞?
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, "password incorrect");
            recordLoginFailureState(account, loginSecurityConfig);
            return R.fail(CommonPrompt.PASSWORD_INCORRECT);
        }
        
        // 婵犲痉鏉库偓妤佹叏閹绢喗鍎楀〒姘ｅ亾闁诡垯鐒﹀鍕箛椤撶偛澹撳┑鐐舵彧缁茶棄锕㈡潏銊︽珷闁炽儲绶為弮鍫熷亹闁肩⒈鍓涢崝鍦磽娴ｄ粙鍝洪柣鐔叉櫊楠炲啯绂掔€ｎ偆鍔堕悗骞垮劚閹冲海鎸х€ｎ剛纾介柛灞捐壘閺嬨倝鏌涢悩宕囧⒌鐎规洑鍗冲畷銊╊敍濡も偓娴滈箖鎮峰▎蹇擃伀妞わ絾鐓￠弻鈥崇暆閳ь剟宕版惔顭戞晪闁挎繂鐗忛悿鈧梺闈涳紡閸滀焦孝
        // 濠德板€楁慨鎾儗娓氣偓閹焦寰勯幇顓炲壓婵炶揪绲藉﹢杈ㄦ櫠閳ユ緞鏃堟偐閼碱剛鍔圭紓浣介哺閻熲晠骞冩禒瀣劶鐎广儱鎳庣挧瀣磽閸屾艾鏋ら柛鐘崇墵瀹曚即宕ㄩ妤€浜鹃悷娆忓椤ｆ煡鏌″畝鈧崰搴敋閿濆牏鐤€闁靛／鍜佹Т
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        String mode = cfg == null ? null : cfg.getMode();
        CaptchaValidationResult captchaValidationResult = captchaStrategyFactory.getStrategy(mode).validate(param);
        if (!captchaValidationResult.isSuccess()) {
            String logMessage = captchaValidationResult.getLogMessage();
            log.warn("闂備浇鐨崱鈺佹缂傚倸绉寸粔闈涱嚗閸曨剚缍囨い鎰╁剾? {} account={}", logMessage, account);
            loginLogService.recordLoginFailure(account, 0L, clientIp, region, userAgent, logMessage);
            recordLoginFailureState(account, loginSecurityConfig);
            return R.fail(captchaValidationResult.getPrompt());
        }
        
        // 闂傚倷绀侀幖顐ゆ偖椤愶箑纾块柟缁㈠櫘閺佸淇婇妶鍛櫤闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濞叉粎妲愰幘瀛樺闁荤喐澹嗘禒鎾⒑娴兼瑧绉紒鐘崇墵楠炲棝宕橀鑲╊槹濡炪倖鎸嗛崟顐㈢殶闂傚倷鑳堕幊鎾活敋椤撶偐鍙緿闂傚倷绀侀幉锛勬暜濡ゅ懌鈧啯寰勯幇顑?
        clearLoginFailureState(account);

        List<SysUserTenant> binds = userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .orderByDesc(SysUserTenant::getPrefOrder)
                .orderByDesc(SysUserTenant::getLastUsed));
        // 闂傚倷绀侀幉锛勬暜濡ゅ啯宕查柛宀€鍎戠紞鏍煙閻楀牊绶茬紒鈧畝鍕厸鐎广儱楠告禍鑺ョ箾閸忕厧鈻曢柡宀€鍠撻埀顒€鐏氶弫濂告偘閸洘鈷戦柛娑橈工缁楁碍銇勯妸銉█濠?
        List<Long> tenantIds = new ArrayList<>();
        // 闂傚倷绀佸﹢杈╁垝椤栫偛绀夐柡鍤堕姹楅梺鎼炲劗閺呮繈鎯岄幘缁樼厽闁哄啫鍋嗛悞楣冩煟閹惧瓨顦盌
        for (SysUserTenant b : binds) tenantIds.add(b.getTenantId());
        // 闂傚倷绀侀幖顐ょ矙閸曨厽宕叉繝闈涱儐閸嬫ɑ绻涢崱妯诲碍闁告瑥锕ラ妵鍕冀閵娧屾殹闂佺楠搁敃锔炬閹惧瓨濯撮柣鐔稿娴犳挳姊烘导娆戝埌濠⒀呮櫕閸掓帡寮崼鐔蜂簻缂備礁顑堝▔鏇㈠储?
        if (tenantIds.isEmpty()) {
            log.warn("闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓瑜版帞宓侀柟鎹愵嚙缁犳稒銇勯弮鍥ㄣ€冪紒? 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹存瑥鐏╅悗姘槹閵囧嫯绠涢幘鎼￥闂佽鐏氶崝鎴﹀箖濡ゅ啯鍠嗛柛鏇ㄥ幘閸欐繈姊洪悷鎵暛闁搞劍妞介獮蹇涘川閺夋垵宓嗛梺缁樼憿閸嬫挻绻涢崗鐓庘枙闁? account={}", account);
            return R.ok(Collections.emptyList());
        }
        // 闂傚倷绀侀幖顐ゆ偖椤愶箑纾块柟缁㈠櫘閺佸淇婇妶鍛殜闁稿鎹囬幃浠嬪垂椤愩垺鐣紓鍌欓檷閸斿繒浜稿▎鎴濆灊闁哄啫鐗婇崑鍕磼鐎ｎ厽纭堕柛妯块哺缁绘盯鏁愰崨顔绢槺闂佸憡鎸荤换鍐偓?
        List<SysTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                .in(SysTenant::getId, tenantIds));
        // 闂傚倷绀侀幉锛勬暜濡ゅ啯宕查柛宀€鍎戠紞鏍煙閻楀牊绶茬紒鈧畝鍕厸鐎广儱楠告禍鑺ョ箾閸忕厧鈻曢柡宀€鍠撻埀顒傛暩濞插﹤菐閵夆晜鈷戦柛娑橈工缁楁碍銇勯妸銉█濠?
        List<TenantVO> vos = new ArrayList<>();
        // 闂備浇顕х换鎰崲閹邦儵娑樷槈椤喚绋忛梺闈涚墕椤︻垳绮婚敐鍥ｅ亾閻熸澘顏鐟版瀵爼鎸婃竟婵嗙秺閺佹劙宕卞Ο铏癸級婵犵數鍋炲娆徫涘┑瀣祦闁硅揪璁ｇ紞鏍ㄣ亜閹垮嫮纾挎い锔炬珛O
        for (SysTenant t : tenants) {
            // 闂傚倷绀侀幉锛勬暜濡ゅ啰鐭欓柟瀵稿Х绾句粙鏌熼幆褍顣抽柣顓熸崌閺岋綁寮崒銈囧姼闂佺粯鎸诲Σ姝勯梻浣筋嚙濞寸兘寮幖浣碘偓浣圭節閸ャ劌鐦?
            TenantVO vo = new TenantVO();
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濡炲瓕
            vo.setId(t.getId() == null ? null : String.valueOf(t.getId()));
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濞茬喖寮诲☉姗嗘僵妞ゆ帒鍊愰妶鍥ㄥ仏?
            vo.setName(t.getTenantName());
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濞茬喖寮婚悢鐑樺珰闁肩⒈鍓涢鎴︽⒑?
            vo.setIntro(t.getDescription());
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濡插兌go
            vo.setLogo(t.getLogo());
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濞叉粎妲愰幒妤婃晝闁靛鍠栧▓顓㈡⒑?
            vo.setTenantType(t.getTenantType() == null ? null : t.getTenantType().getCode());
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐夸緵婵炴捁鍩栨穱濠囶敍濞戝崬鍔岄梺鍛婅壘濞差參寮婚垾鎰佸悑闁告侗鍠氶埞娑橆渻?
            for (SysUserTenant b : binds) { if (b.getTenantId().equals(t.getId())) { vo.setIsDefault(b.getIsDefault()); break; } }
            // 濠电姷鏁搁崕鎴犵礊閳ь剚銇勯弴鍡楀閸欏繘鏌ｉ幇顒佹儓缂佲偓閸℃稒鐓涘璺猴功娴犮垽鏌ｈ箛鏃傛噰闁?
            vos.add(vo);
        }
        log.info("婵犵妲呴崑鍛熆濡皷鍋撳鐓庢灈闁宠绉规俊鐑藉Ω閿濆嫭鐫忛梻浣告惈濞层劑宕版惔銊﹀€堕柛銉墯閻撴洟鏌￠崘銊モ偓绋库枍閹邦喚纾奸弶鍫涘妿閹冲洭鏌ｉ幙鍐ㄥ⒋妞ゃ垺娲熼崹楣冩惞椤愶絾鐣堕梻鍌欑閹碱偆绮欓崟顓熷床婵犻潧顑嗛崑妯荤箾閸℃ɑ灏伴柣鎺旂帛閹便劌螣閾忕櫢绱炵紓浣哄У閻楃娀寮? account={}, tenants={}", idKey, vos.size());
        return R.ok(vos);
    }

    /**
     * 闂傚倸鍊风欢锟犲磻閸曨垁鍥箥椤旂懓浜炬慨妯稿劚婵¤法绱掗鑺ヮ棃闁诡喓鍨荤划娆戞崉閵娿儱绠為梻鍌欑劍鐎笛呯矙閹达附鍎楀〒姘ｅ亾妞ゃ垺鐟╁鎾偐閻㈢绱存俊鐐€栧Λ浣哥暦閻㈠憡鍋￠柟閭﹀厴濡插牓鏌熼悙顒€澧柛搴㈠姍閺岋紕鈧綆浜滈弳锝呪攽?
     * <p>
     * 闂備浇顕уù鐑藉箠閹捐瀚夋い鎺戝濮规煡鏌ㄥ┑鍡樺仾閻熸瑥瀚刊鎾偠濞戞帒澧查柣鎾跺█濮?
     * </p>
     * <ol>
     *   <li>闂傚倷绀侀幉锟犳偡閵夆晛纾圭憸鐗堝笒濮规煡鏌ｉ弮鍌氬付缂佺姷鏁婚弻鐔兼焽閿曗偓婢ь喚绱掗幓鎺撳仴闁哄被鍔岄埥澶娾枎閹寸姴褰庣紓鍌欑贰閸犳碍绻涢埀顒傗偓瑙勬穿缂嶄礁鐣烽幒鎴旀婵炲棙鍔﹂崯鈧繝鐢靛仦閸ㄥ爼骞栭埡鍛偍闁稿﹦鍠嗘禍鐟般€掑锝呬壕閻庤娲╃紞浣割嚕娴犲鏁冩い鎰剁到鐏忓啴姊?ID 闂傚倷绀侀幉锛勫垝瀹€鍕剹濞达絿鎳撶欢鐐烘煕閺囥劌鐏犻柣?/li>
     *   <li>闂傚倷绀侀幉锟犳偡閵夆晛纾圭憸鐗堝笒濮规煡鏌ｉ弮鍌氬付闁告劏鍋撻梻浣芥硶閸犳挻鎱ㄥ畡鎵殾闁挎繂顦伴悡銉︾箾閹寸偟鎳呮い锝呭级閵囧嫰濡烽妷锕€娈楅悗娈垮枛椤攱淇婇悿顖ｆ濠电偛鍚嬫繛濠囧蓟?ID 闂傚倷绀侀幉锛勫垝瀹€鍕剹濞达絿鎳撶欢鐐烘煕閺囥劌鐏犻柣顓燁殜濮婃椽宕归鍛壉缂佹鍨垮娲川婵犲嫬鏆＄紓鍌氬€瑰銊╁箯瑜版帗鍋勯柧蹇撴贡閻?/li>
     *   <li>闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹存瑥鐏╅柦鍐枛閺屾洘寰勫Ο鐓庡弗闂佹悶鍊曠€氫即寮婚妸銉㈡婵炲棙鍩堥弳锟犳⒑缂佹﹩娈曢柟鍛婂▕閻涱噣骞嬮敂缁樻櫓闂佸壊鐓堥崳顔嘉ｉ柆宥嗏拺闁告稑锕﹂幊鍥煛閳ь剟鏌嗗鍡椾簵闂佹寧绋戠€氼厾鈧碍宀搁弻鏇＄疀鐎ｎ亞浠鹃梺鍝勬缁绘﹢寮婚悢鍝勵棜閻庯綆鍋夐崥鍌涚箾閿濆懏鎼愰柨鏇ㄤ邯閻?/li>
     *   <li>缂傚倸鍊搁崐鐑芥倿閿曞倸鍨傞柣銏犳啞閸嬧晛螖閿濆懎鏆欑紒鈧崒婧惧亾鐟欏嫭绀€婵炲樊鍙冨畷妤呭炊椤掍胶鍘搁悗骞垮劚缁绘帞寮ч埀顒勬⒑缂佹鈯曢柣鐔叉櫊瀵偄顓奸崪浣瑰兊濡炪倖鎸嗛崟銊﹀瘲闂傚倷绀侀幖顐ゆ偖椤愶箑纾块柟鎯版閺嬩線鏌曢崼婵愭Ц缂佺媴缍侀弻褍顫濋鈧埀顒€顭烽幃鐑藉閵忋垻锛滄繝銏ｆ硾閼活垶寮搁幋锔界厱闁冲搫鍊诲ú鎾煙椤栨艾鏆ｇ€规洜鍠栭、姗€鎮╅崘宸槐闂備浇顕у锕傦綖婢舵劖鍤屽Δ锝呭暙閻鎲歌箛娑樼?/li>
     *   <li>SaToken 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓瑜版帒鏋佺€广儱鎷嬪鈺冣偓瑙勬礀濞诧箓顢欏澶嬧拺?StpUtil.login() 闂備礁鎼ˇ顐﹀疾濠婂懐鐭欓柡宥庡幑閳ь兛绶氶獮瀣晜閽樺澹冮梺鑽ゅТ濞测晝浜稿▎鎴犵煋?/li>
     *   <li>婵犵數鍋炲娆撳触鐎ｎ偆鈹嶆繛宸簼閸庡﹪鏌ｉ敐鍛拱濠殿垰銈搁弻娑㈠箻濡も偓閹冲繘鎮楅銏♀拺闁圭娴烽埊鏇炃庨崶銊︺仢妞ゃ垺鐟╁鎾偐閻㈢绱存俊鐐€栧濠氬磻閹捐姹查煫鍥ㄧ⊕閻?ID闂傚倷绶氬褍螞閺冨牊鍤勯柛顐ｆ穿缂嶆牠鏌曢崼婵愭Ц缂?ID闂傚倷绶氬褍螞閺傛５褰掑炊閵娿儳绐為梺鍛婃处閸ㄤ即鎯屽Δ鍐ｅ亾閻熸澘顏繝銏★耿閹?SaSession</li>
     *   <li>Token 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁哄拋鍓氶幈銊ノ熼崫鍕瘣缂備胶濯崹鍫曞蓟?StpUtil 闂傚倷鐒﹂惇褰掑垂婵犳艾绐楅柟鐗堟緲閸ㄥ倹鎱ㄥΟ鍨厫闁?Token</li>
     *   <li>Redis 闂備浇顕х€涒晝绮欓幒妤佹櫔闂備胶顭堥鍛矓瑜版帒鏋佺€广儱鎷嬪鈺傘亜閹烘垵鈧綊顢欓幋锔解拺閻犲楠稿Λ顓㈠吹濞嗘垹纾界€广儱鎳愭牎缂備礁顑呴ˇ鎵崲濠靛洦濯撮柧蹇氼潐閸熸挳姊绘担鍛婃儓闁稿﹦顭堢叅闁靛牆顦伴崑銈夋煏婵炵偓娅呯紒鈧?Redis闂傚倷鐒︾€笛呯矙閹寸偟闄勯柡鍐ㄥ€荤粻?Token 闂備礁鎼ˇ顐﹀疾濞戞◤娲晝閳ь剟鏁冮姀銈嗘櫢闁绘灏欓惈鍕⒑閸撴彃浜濇繛鍙夛耿閺佸秹鎮㈢亸浣诡潔闂佸湱铏庨崹鐗堢妤ｅ啯鈷?/li>
     *   <li>缂傚倸鍊风粈渚€藝闁秴绐楅柟鐗堟緲閺勩儲绻涢幋鐐垫噮闁崇粯鏌ㄩ埞鎴︽偐鏉堫偄鍘￠梺鑽ゅ枑閹瑰洭寮婚敓鐘茬＜婵﹩鍘鹃埞娑橆渻閵堝棙鈷愭繛鍙夌矌濡叉劙寮崼顐ｆ櫔闂佺硶鍓濋悷褔鎮伴妷鈺傜厽闁绘ê妯婇崕蹇涙煕閻旇泛宓嗙€?TenantContext.set() 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐ㄦ惛濞存粌缍婇弻鐔煎箚瑜嶉弳杈ㄣ亜閵堝懏鍣界紒杈ㄥ笚瀵板嫮浠﹂懞銉р偓楣冩⒑?/li>
     *   <li>闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樻彃顏痪鎯с偢閺岀喓鈧稒顭囩粻姗€鏌￠崱鏇炲祮闁哄本绋栫粻娑㈠箻鐠佸湱绀婇梻鍌欑贰閸犳骞愰幎钘夋瀬鐎广儱鎷嬪鈺傘亜閹捐泛鏋旈柛鎴滅矙濮婃椽宕崟顐熷亾鐟欏嫮鐝堕柛鈩冪☉閺嬩線鏌曢崼婵愭Ц缂佺媴绲介—鍐偓锝庝簻閻︺劍绻涢崗鐓庘枙闁哄瞼鍠栭獮姗€顢氶崨顕呮缂傚倷绶￠崳顖涗繆閸ヮ剙鐒垫い鎺戝枤濞兼劖鎱ㄥΟ绋垮闁崇粯鎹囬獮瀣晝閳ь剛鈧艾顦甸弻宥堫檨闁告挾鍠庨悾鐑藉Ψ閿旇棄鍔呭┑鐘绘涧濡矂寮歌箛娑欌拺婵炶尙绮繛鍥煕閺傝法孝妞ゎ亜鍟村畷鎺楁倷缁瀚介梻浣告惈椤︿即宕归崜浣插亾濮橆厾鍙€闁哄本绋撻埀顒婄秵娴滅偟绮绘导瀛樼厪闁割偓绲借闂侀潧娲ょ€氭澘鐣烽锕€绀嬫い鎾跺閺?/li>
     *   <li>闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹥鍋ラ柡宀嬬節瀹曟﹢鏁冮埀顒勫礉濮樿京纾肩紓浣股戦ˉ鍫ユ煛娴ｅ摜孝妞ゎ偅绻堥獮鍥ㄦ媴閸涘﹥姣嗛梺鑽ゅ枑缁孩鏅跺Δ鈧叅婵犲﹤鍟犻弸宥夋煏婢跺牆濡稿ù婊冪秺閺岋繝宕堕埡浣圭€婚梺璇茬箰濞差參寮诲☉銏犵睄闁稿本纰嶉悗鎯р攽閳藉棗浜愰柡鍛仧缁?/li>
     *   <li>闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樺弶澶勯柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂佺粯鎸诲ú鏍絹闂佹悶鍎荤徊鑺ユ櫠閹绘崡褰掓偂鎼淬垹娈楅梺杞扮缁夋挳锝炲┑瀣垫晣闁绘柨鎼崵閬嶆⒒娴ｅ憡鎯堥柛濠咁潐缁旂喖宕卞☉妯荤€梺闈涚墕椤︻垳绮婚敐澶嬧拺闁割煈鍣崕鎰归悩绛硅€块柡灞剧☉椤繈顢楁担鐟伴棷婵犵數鍋涢幊宀勫磿閹绢喖桅?IP闂傚倷绶氬褍螞閺冨牊鍊块柨鏇楀亾妞ゎ亜鍟村畷鎺楁倷缁瀚介梻浣告惈椤︿即宕归崜浣插亾濮橆厾鍙€闁哄本鐩鍊燁槼闁诡垰鐗撻弻?/li>
     *   <li>闂傚倷绶氬鑽ゆ嫻閻旂厧绀夐幖娣妼閸氬綊骞栧ǎ顒€濡介柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂佺粯鎸诲ú婊呮閹捐纾兼繛鎴炵懃绾板秹姊虹€癸附婢樻慨鍌炴煛娴ｅ摜孝妞ゎ偅绻堥、姗€鎮欑€靛摜效闂傚倷绀侀幉鈥愁潖缂佹ɑ鍙忛柡澶嬪煀閼版寧銇勮箛鎾搭棤婵☆偒鍨堕幃褰掑箒閹烘垵顬夐梺鍝勬缁绘﹢寮婚悢鐓庣妞ゆ洖鎳忛ˉ鏍磽娴ｇ懓鏁惧┑鈥虫喘閸┾偓妞ゆ帊鑳堕埊鏇㈡煥濮樿埖鐓?Redis</li>
     *   <li>闂備礁鎼ˇ顐﹀疾濠婂牆钃熼柕濞垮剭濞差亜鍐€妞ゆ挾鍠庢禒娲⒑闂堟侗鐓紒鐘冲灴閹繝寮撮悙鍐ㄩ叄瀹曞爼鏁愰崨顓涙嫟濠电偛顕崢褔骞婂鈧顐㈩吋閸滀焦鍍靛銈嗘尵婵參宕戦幘娲绘晬婵鍘у▓姗€姊洪崨濠勭畵閻庢凹鍨抽埀顒佺閻熲晛顕ｉ崼鏇炵厸闁稿本绋撻崣鍡椻攽閻愭彃绾ч柣妤冨█楠炲啯绂掔€ｎ€晠鏌ㄩ弴妤€浜鹃梺?DTO</li>
     * </ol>
     * <p>
     * 闂傚倷娴囧銊╂嚄閼稿灚娅犳俊銈傚亾闁伙絽鐏氶幏鍛偘閳ュ厖澹曞┑鐐村灦椤忣亪顢旈崼婵堬紱閻庡箍鍎遍ˇ浼村疾?
     * </p>
     * <ul>
     *   <li>SaSession闂傚倷鐒︾€笛呯矙閹烘鍤屽Δ锝呭暞閸嬨倝鏌曟繛鐐珔缁?LOGIN_USER_ID闂傚倷绶氬褍螞閺冣偓缁岄亶鎮块—宀籒_TENANT_ID闂傚倷绶氬褍螞閺冣偓缁岄亶鎮块—宀籒_ACCOUNT</li>
     *   <li>濠电姷鏁搁崑鐐差焽濞嗘垶宕叉俊銈呮嫅缂嶆牜鈧箍鍎遍ˇ顖炴⒒椤栫偞鐓忓┑鐐戝啫顏村ù鐙€鍣ｉ幃宄邦煥閸涱収鏆┑鐐插级閿曘垽宕洪埀顒併亜閹达絽鍔甸柛蹇撶灱缁辨帡顢欓幆褍濡虹紓浣割儏椤︾敻宕洪埀顒併亜閹烘垵顏柣鎺旂帛閹便劌螣閾忕櫢绱炵紓?Sa-Token Cookie 婵犵數鍋熼ˉ鎰板磻閹邦厾绠鹃柍褜鍓熼弻?/li>
     *   <li>Redis fx:online:user:{tenantId}:{userId}闂傚倷鐒︾€笛呯矙閹烘鍎楁い鏃€鍎抽崹婵嬪箹鏉堝墽宀涢柛瀣尭閳藉鈻嶉褌閭€规洘鍨甸鍏煎緞婵犲啫绁堕梻渚€娼ц噹闁告劏鏅濋弸鍐⒑鐠囪尙绠抽柛瀣仜鐓ゆ繝濠傜墕妗呭┑顔斤供閸橀箖鍩炲澶嬬厓闁宠桨绀侀弳鏇犵磼?/li>
     * </ul>
     *
     * @param param 闂傚倸鍊风欢锟犲磻閸曨垁鍥箥椤旂懓浜炬慨妯稿劚婵¤法绱掗鑺ヮ棃闁诡喓鍨荤划娆戞崉閵娿儱绠為梻鍌欑閹诧繝鎮烽妷鈺佺９鐟滅増甯掑Ч鏌ユ煟閺傚灝鎮戦柡鍜佸墴閹﹢鎮欓懜娈挎闂佷紮绠戦悥濂稿蓟濞戞﹩娼╅柣鎾崇凹閸戣姤绻濋姀锝嗙【閻庢碍婢橀悾鐑芥晲閸涱垱娈濋梺缁橆殔閻楀棝顢旈婊呯＝濞达絽澹婂Σ娲煕閻樺啿濮嶇€?ID
     * @return {@link R} 闂傚倷绀侀幉锟犳偋閺囥垹绠犻幖娣妼缁犳岸鏌涢顒傜シ濞存粌缍婇弻鐔煎箚瑜嶉弳杈ㄣ亜閵堝懏鍤囬柡宀嬬秬椤﹁埖銇勯弴鍡楁噽缁€濠勨偓骞垮劚椤︿即宕戦妸鈺傜厪濠电姴绻掗悾閬嶆煟閹惧瓨绀€閼挎劙鏌涢妷顖滃埌濠⒀勫絻闇夐柣鎾冲閸ゅ洭鏌熼姘殻鐎规洜鍠栭、姗€鎮╅懠顒€骞忔繝鐢靛仦閸ㄥ爼骞愰幘顔肩；闁规崘顕х痪褔鏌嶉崫鍕偓褰掓偂閵夛妇绠鹃柛娆忣槺婢ц京绱掓潏銊ユ诞妤犵偞顭囬埀顒傛暩绾爼宕?
     * @throws BusinessException 闂佽崵鍠愮划搴㈡櫠濡ゅ懎绠扮紒瀣儥閸ゆ洘銇勯幒鎴濐仼闁哄绶氶弻锝呂旈埀顒勬偋韫囨梹娅犻柍銉︾窞閺冨牊鍋愰柤鑹版硾椤忣參姊洪棃娑欏闁搞劌缍婇獮蹇氥亹閹烘嚦銊╂煥濠靛棗鈧憡绂嶉幆顬″綊鏁愰崶褍濡洪梺鍝勬缁绘﹢寮婚悢鍝勵棜閻庯綆鍋夐崥鍌滅磽娴ｈ娈旀い锕備憾閸┾偓妞ゆ帊鑳堕埊鏇㈡煥濮橆厾绡€闁逞屽墴椤㈡棃宕煎顏傚劚闇夐柨婵嗗椤掔喐绻涢崗鐓庘枙闁哄瞼鍠栭獮姗€顢欓懞銉︽婵犵數濮崑鎾淬亜閹捐泛啸缁炬儳銈搁弻鐔衡偓娑欘焽缁犳﹢鏌￠崱鏇炲祮闁哄矉绻濆畷姗€顢橀悙闈涘缚濠电姵顔栭崰鏍崲濡櫣鏆?
     * @see com.forgex.auth.domain.param.TenantChoiceParam
     * @see com.forgex.auth.domain.dto.SysUserDTO
     * @see com.forgex.common.tenant.TenantContext
     * @see cn.dev33.satoken.stp.StpUtil
     */
    @Override
    public R<SysUserDTO> chooseTenant(TenantChoiceParam param) {
        String loginTerminal = resolveLoginTerminal(param == null ? null : param.getLoginTerminal());
        if (param != null) {
            param.setLoginTerminal(loginTerminal);
        }
        return chooseTenantStrategyFactory.getStrategy(loginTerminal).chooseTenant(param);
    }

    public R<SysUserDTO> doChooseTenant(TenantChoiceParam param) {
        // 婵犵數鍋涢顓熸叏鐎涙﹩娈界紒瀣儥閸ゆ洘銇勯幒鎴濐仼闁哄绀佽灋濡鐒︾粊浼存煕濮楀棗澧撮柡宀€鍠撶槐鎺懳熼崫鍕垫綋闁荤喐绮嶅妯好洪妸褍鍨濋柡鍐ㄧ墛閸嬪嫮绱掔€ｎ厽纭堕柛妯兼磾D
        Long tenantId = param == null ? null : param.getTenantId();
        String account = param == null ? null : param.getAccount();
        if (tenantId == null) {
            return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        }
        if (!StringUtils.hasText(account)) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.ACCOUNT_CANNOT_BE_EMPTY);
        String idKey = account;
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, idKey));
        if (user == null) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.USER_NOT_FOUND);
        SysUserTenant bind = userTenantMapper.selectOne(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId)
                .last("limit 1"));
        if (bind == null) return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.USER_NOT_BOUND_TO_TENANT);
        // 闂備浇宕垫慨鎾敄閸涙潙鐤ù鍏兼綑閺嬩焦銇勯妷褜鍔員oken闂備礁鎼ˇ顐﹀疾濠婂懐鐭欓柡宥庡幑閳ь兛绶氶獮瀣晜閽樺澹冮梺鑽ゅТ濞测晝浜稿▎鎴犵煋?
        StpUtil.login(idKey);
        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢姀鈺傛珝Session闂備浇顕уù鐑藉极閹间降鈧焦绻濋崶銊ョ樁?
        SaSession session = StpUtil.getSession();
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍏狀亪顢氭潏銊﹀弿婵☆垵娅ｉ敍宥嗕繆椤栨氨澧曢懣鎰版煕閵夘垳鍒板褎褰冮湁?
        if (session != null) {
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿骸鐭傞幃褰掑炊椤忓嫮姣㈢紓浣割槸濞硷繝寮婚敐澶涚稏妞ゆ巻鍋撳┑鈥茬矙閺屾盯鍩￠崒娑欑窚D
            session.set("LOGIN_USER_ID", user.getId());
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿骸鐭傞幃褰掑炊椤忓嫮姣㈢紓浣割槼濞夋洜妲愰幒鎾崇窞閻忕偠濮ら悗楣冩⒑缁洘娅嗛柡澶屾瀺
            session.set("LOGIN_TENANT_ID", tenantId);
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿骸鐭傞幃褰掑炊椤忓嫮姣㈢紓浣割槸濞硷繝骞冭ぐ鎺戠疀闁告挷鑳堕弳鐘绘偡?
            session.set("LOGIN_ACCOUNT", idKey);
        }
        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢姀鈺傜彾ken闂?
        String token = StpUtil.getTokenValue();
        // Sa-Token 婵犵數鍋炲娆撳触鐎ｎ偆鈹嶆繛宸簼閸庡﹪鏌﹀Ο渚▓闁哥喎鎳橀弻娑樷枎韫囷絾笑濡炪們鍎抽崑銈咁嚕閸洘鍊烽柛顭戝亝閻濐亜鈹戦悙鍙夊櫡闁稿孩鎸虫俊鐢稿箛閺夊灝宓嗗┑顔筋焾娴滎剟骞夐鍛闁瑰鍋炵亸顓熴亜閹存繃顥㈢€殿喗鎮傚畷姗€顢欓懞銉︾彨闁诲骸鍘滈崑鎾绘煕閹板吀绨芥い鏃€鍔欏缁樻媴閻熸壆绁锋繝闈涘€瑰鑽ゅ垝椤撶喎绶為柟閭﹀墰椤︺劑鎮楅獮鍨姎婵炶绠戣灒濠电姴娲﹂悡娆撴椤掍礁绲婚柍褜鍓氬ú鐔煎箠閻斿吋鍋勯柣鎴灻禒?Redis 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閻㈢數鐭欏璺侯儑缁♀偓婵炶揪缍€濞咃綁骞夐鈧娲传閸曨偀鍋撶粙妫垫椽濮€閵堝懐鍔?
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濞叉牠鍩ユ径鎰缂佹稖顫夐。鑲╃磽娴ｈ娈旈柟铏?
        TenantContext.set(tenantId);
        // 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樺弶澶勯柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂佺粯鎸诲ú婊呮閹烘挸绶為悘鐐跺Г閻庨箖姊虹化鏇熸珕缂佸鎸惧Σ鎰板箳濡も偓缁狅絾绻濋棃娑氬婵絽閰ｅ铏圭矙鐠恒劎顔囩紒鍓ц檸閸欏啴寮?
        userTenantMapper.update(null, new LambdaUpdateWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId)
                .set(SysUserTenant::getLastUsed, LocalDateTime.now())
                .setSql("pref_order = pref_order + 1"));
        
        // 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹥鍋ラ柡宀€鍠撻埀顒佺⊕閿氬┑顔兼搐閳规垿顢欓悾灞惧垱閻庢鍣崳锝嗕繆閻戣棄唯鐟滃繘顢?
        String clientIp = getClientIp();
        String userAgent = getUserAgent();
        String region = ipLocationService.getLocationByIp(clientIp);
        loginLogService.recordLoginSuccess(user.getId(), account, tenantId, clientIp, region, userAgent, token);
        
        // 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樺弶澶勯柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂佺粯鎸诲ú鐔煎蓟閿熺姴閱囨慨姗嗗厸婢规洟姊绘担鍛婂暈闁艰鍎抽湁鐎瑰嫭澹嬮弸宥夋煏婢跺牆濡稿ù婊冪秺閺岋繝宕堕妸褏鍙濈紓浣筋嚙濡繈寮?
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLastLoginIp, clientIp)
                .set(SysUser::getLastLoginRegion, region)
                .set(SysUser::getLastLoginTime, LocalDateTime.now()));
        
        // 濠电姷鏁搁崕鎴犵礊閳ь剚銇勯弴鍡楀閸欏繘鏌ｉ幇顒佹儓闁圭懓鐖奸弻鏇熺箾瑜嶉崯顖炴偪閸ヮ剚鈷戞繛鑼帛婵炲洭鏌涢弬璺ㄐх€规洖鎼埥澶婎潩鏉堚晪绱抽梻浣筋潐閸庡磭澹曢銏犵闁绘绮悡銉︾箾閹寸儐鐒藉褎鏌ㄩ埞鎴︽倷閸欏鏋犻梺璇″枙缁瑩宕洪敓鐘冲剳婵帗顒琁d+tenantId婵犵數鍋犻幓顏嗗緤閸撗冨灊闁割偁鍨虹€氬鏌ｉ幒鎾跺垱y闂?
        String onlineKey = "fx:online:user:" + tenantId + ":" + user.getId();
        try {
            // 闂傚倷绀侀幉锛勬暜濡ゅ啰鐭欓柟瀵稿Х绾句粙鏌熼幑鎰靛殭闁圭懓鐖奸弻鏇熺箾瑜嶉崯顖炴偪閸ヮ剚鈷戞繛鑼帛婵炲洭鏌涢弬璺ㄐх€规洖鎼埥澶娾枎閹存梹鏁甸梻浣规灱閺呮盯宕埡鍐╊潟闁哄牊鐡玴
            Map<String, Object> onlineInfo = new HashMap<>();
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濡炲瓕
            onlineInfo.put("userId", user.getId());
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濡炲瓕
            onlineInfo.put("tenantId", tenantId);
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噭閻庢艾顭烽弻銊╁即濡も偓娴滈箖鎮?
            onlineInfo.put("account", idKey);
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢悙鎼瀫ken
            onlineInfo.put("token", token);
            onlineInfo.put("loginTerminal", resolveLoginTerminal(param == null ? null : param.getLoginTerminal()));
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿骸鐭傞幃褰掑炊椤忓嫮姣㈢紓浣割槸濞硷繝寮婚敓鐘茬闁靛ě鍐幗婵?
            onlineInfo.put("loginTime", LocalDateTime.now().toString());
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐喛闁稿鎹囬悰顕€宕归鍙ョ棯闂備胶纭堕弲婊呯矙閹捐泛鍨濋柍鍝勬噺閸嬵亝淇?
            onlineInfo.put("clientIp", clientIp);
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涘┑鎰畬er-Agent
            onlineInfo.put("userAgent", userAgent);
            // 闂備礁鎼ˇ閬嶅磿閹版澘绀堟慨姗嗗墰閺嗭箓鏌涘▎蹇ｆШ闁崇粯姊荤槐鎺楀焺閸愨晜鍣窸N闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
            String onlineJson = toJson(onlineInfo);
            // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢姀鈺傜彾ken闂備胶鍎甸崜婵堟暜閹烘绠犻煫鍥ㄦ惄濞撳鏌涘畝鈧崑娑氱尵瀹ュ鐓曟い鎰剁稻缁€鍐煥?
            Duration onlineTtl = resolveCurrentTokenTtl(token);
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涘┑鍥╃厷dis闂傚倸鍊烽悞锕傤敄濞嗘挸绐楅柡宥庡幖閻掑灚銇勯幒鎴濃偓鍝ユ暜閼哥偣浜滈柡鍥ュ妼濞呭秹鏌℃担鍝バゅù鐙呯畵閹虫牠鍩￠崘鈺佹灕token闂備礁鎼ˇ顐﹀疾濞戞◤娲晝閳ь剟鏁冮姀銈嗘櫢闁绘灏欓惈鍕⒑閸撴彃浜濇繛鍙夛耿閺佸秹鎮㈢亸浣诡潔闂佸湱铏庨崹鐗堢妤ｅ啯鈷?
            if (onlineTtl == null) {
                redis.opsForValue().set(onlineKey, onlineJson);
            } else if (!onlineTtl.isZero() && !onlineTtl.isNegative()) {
                redis.opsForValue().set(onlineKey, onlineJson, onlineTtl);
            } else {
                redis.delete(onlineKey);
            }
            log.info("濠电姷鏁搁崕鎴犵礊閳ь剚銇勯弴鍡楀閸欏繘鏌ｉ幇顒佹儓闁圭懓鐖奸弻鏇熺箾瑜嶉崯顖炴偪閸ヮ剚鈷戞繛鑼帛婵炲洭鏌涢弬璺ㄐх€规洖鎼埥澶婎潩鏉堚晪绱抽梻浣筋潐閸庡磭澹曢銏犵? userId={}, tenantId={}, token={}", user.getId(), tenantId, token);
        } catch (Exception e) {
            log.warn("濠电姷鏁搁崕鎴犵礊閳ь剚銇勯弴鍡楀閸欏繘鏌ｉ幇顒佹儓闁圭懓鐖奸弻鏇熺箾瑜嶉崯顖炴偪閸ヮ剚鈷戞繛鑼帛婵炲洭鏌涢弬璺ㄐх€规洖鎼埥澶婎潩鏉堚晪绱抽梻浣筋潐閸庡磭澹曢銏犵闁绘ê妯婇悢鍡涙煙椤栨稑顥嬬紒銊ユ健閹? userId={}, tenantId={}", user.getId(), tenantId, e);
        }
        
        log.info("闂傚倸鍊风欢锟犲磻閸曨垁鍥箥椤旂懓浜炬慨妯稿劚婵¤法绱掗鑺ヮ棃闁诡喓鍨荤划娆戞崉閵娿儱绠為梻鍌欒兌閹虫捇鎮洪妸鈺佺闁哄洨鍠愰崣? account={}, tenantId={}", account, tenantId);
        // 闂傚倷绀侀幉锛勬暜濡ゅ啰鐭欓柟瀵稿Х绾句粙鏌熼崜褏甯涢柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂佺粯鎸诲鍑綩闂備浇顕уù鐑藉极閹间降鈧焦绻濋崶銊ョ樁?
        SysUserDTO result = new SysUserDTO();
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濡炲瓕
        result.setId(user.getId());
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噭閻庢艾顭烽弻銊╁即濡も偓娴滈箖鎮?
        result.setAccount(user.getAccount());
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濞茬喖寮?
        result.setUsername(user.getUsername());
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁绘帟濮ら妵鍕籍閸屾繃顎楅梺?
        result.setEmail(user.getEmail());
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐槈缂佺姰鍎甸幃妤呮晲鎼粹€茬凹缂備降鍔嶅畝鎼佸蓟?
        result.setPhone(user.getPhone());
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍏狀亞娆㈤悙鐑樼厸濠㈣泛顑呴悘銉╂煕?
        result.setAvatar(user.getAvatar());
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐姇闁稿鍔欓弻銈夊传閵夘喗姣岄梺?
        result.setStatus(user.getStatus());
        // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噮闁活厽鎹囬弻锝夊籍閸屻倗鍔搁梺缁樻尰濡炲瓕
        result.setTenantId(bind.getTenantId());
        return R.ok(result);
    }


    /**
     * 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樻彃顏柣顓熸崌閺岋綁寮崒銈囧姼闂佺粯鎸诲ú鐔煎蓟濞戞埃鍋撻敐搴濈敖缂佺姳鍗抽弻鏇㈠醇閵忊剝鐝梺鐟板槻椤戝鐣峰鍡╂Ь闂?
     * <p>闂傚倷鐒﹀鍨焽閸ф绀夐悗锝庡墲婵櫕銇勯幒鎴濐仼闁告濞婇幃妤€鈽夊▍铏灴閹繝鍩€椤掑嫭鈷戞繛鑼帛婵炲洭鏌涢弬璺ㄐх€规洖鎼埥澶愬閳ュ啿澹勯梻浣告啞濞诧箓宕ｆ惔鈽嗙劷闁规儼濮ら悡娆撴煙缂併垹鏋熼柡渚€浜堕幃浠嬵敍濞戞瑥闉嶉梺璇″灡濡胶绮诲☉銏犵闁哄鍩堟导鍛攽椤旇棄绗氱紒瀣浮閳ワ箓宕奸妷顔芥櫍闂佺厧顫曢崐妤呮儗閹剧粯鐓熼柡鍐ㄥ亞閻掗箖鏌ｉ幘瀛樼闁诡喛顫夐幏鍛矙鎼存挻瀵栭梻浣圭湽閸婃洖螞濠靛洣绻?/p>
     * <p>闂傚倷绀侀幉锟犳偡閵夆晛纾圭憸鐗堝笒濮规煡鏌ｉ弮鍌氬妺閻庢碍宀搁幃妤€鈽夊▍铏灴閿濈偤寮介鐔哄弳?/p>
     * <ul>
     *   <li>account闂傚倷鐒︾€笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌曢崼婵愭Ц缂佺媴缍侀弻锝夊Χ鎼达紕浠╁銈忚吂閺呯娀寮?/li>
     *   <li>ordered闂傚倷鐒︾€笛呯矙閹烘埈娼╅柕濞炬櫔缂嶆牠鏌曢崼婵愭Ц缂佺媴绲跨槐鎺斺偓锝冨妷閸嬫捇濡烽埡鍌滃幍闂佺粯鍨堕敋缂佹甯炵槐鎺椻€﹂幋婵嗙睄閻庤娲橀懝鎹愮亙闂佸憡娲嶉弬渚€宕?/li>
     *   <li>defaultTenantId闂傚倷鐒︾€笛呯矙閹烘垹鏆嗛柛婵勫劚閸ㄦ繃銇勯弽銊х煁濠殿垰銈搁弻鏇㈠醇濠靛洨銈╁┑鐐插悑婵炲﹪寮婚悢琛″亾閸偅鏆柣?/li>
     * </ul>
     *
     * @param account 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹寸偟顣查悗姘煼閺屻劑寮村Δ鈧禍楣冩偡?
     * @param ordered 缂傚倸鍊风粈渚€藝闁秴绐楅柟鐗堟緲閺勩儲鎱ㄩ崷顓熷珒闂傚倷绀佸﹢閬嶅磿閵堝洦鏆滈柟鐑樻婵櫕銇勯幘鍗炵仼缂佲偓閸℃ü绻嗛柕鍫濇噹椤忋儵鏌?
     * @param defaultTenantId 婵犳鍠楃敮妤冪矙閹烘せ鈧箓宕奸妷顔芥櫍闂佺厧顫曢崐妤呮儗閹剧粯鐓熼柡鍐ㄥ亞閻掗箖鏌ｉ幘瀛橆槺D
     * @return 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樻彃顏痪鎯с偢閺岀喖骞嗚閸ょ喎霉?
     */
    @Override
    public R<Boolean> updateTenantPreferences(String account, java.util.List<Long> ordered, Long defaultTenantId) {
        if (!StringUtils.hasText(account) || ordered == null) return R.fail(CommonPrompt.BAD_REQUEST);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account));
        if (user == null) return R.fail(CommonPrompt.USER_NOT_FOUND);
        // 闂備浇宕垫慨宕囨閵堝洦顫曢柡鍥ュ灪閸嬧晛鈹戦悩瀹犲缂佺嫏鍥ㄧ厱闁逛即娼ч弸娑欑箾閸涱偄鐏查柡灞诲妼閳藉螣閻撳寒鏉搁梺姹囧焺閸ㄩ亶骞愭繝姘畾闁哄啫娲ㄩ悿鈧梺瑙勫劤绾绢厽绂掑鑸电厽闁绘劕顕埣銈夋煕閺冣偓閸ㄥ灝顕ｉ搹顐ｇ秶闁靛绠戦崝鍛存⒑閹稿孩顥嗛柛瀣姉缁?
        int n = ordered.size(); int weight = n;
        // 闂傚倸鍊风欢锟犲礈濞嗘垹鐭撻柡澶嬪焾閸熷懎鈹戦悩瀹犲缂佺姵濞婇弻鐔兼倻濡櫣浠肩紒鐐緲缁夊綊寮诲☉銏犵闁瑰灝鍟悾浠嬫⒑闂堟稒顥滈柛鐔告尦瀵偄顓奸崶锔藉媰闂佺粯鍔﹂崜娑㈠礄娴犲鈷戦柛婵嗗閳ь剛绮弲璺何旈崨顔煎墾闂佹枼鏅涢崯浼村煝閺冣偓閵囧嫯绠涢幘杈剧礊濠电偛鍚嬫繛濠囧蓟閻旂厧绠绘い鏇炴噺椤ユ牕鈹戦悙鑼闁搞劌鐏濋悾鐑藉箣閿濆洨鏉搁梺瑙勫劤瀹曨剚绂掗悜妯圭箚闁绘劦浜滈埀顒佹礋楠炴牠顢曢妶鍫澬?
        for (Long tid : ordered) {
            // 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樻彃顏柣顓熸崌閺岋綁寮崒銈囧姼闂佺粯鎸诲ú鐔煎蓟濞戞埃鍋撻敐搴濈敖缂佺姳鍗抽弻鏇㈠醇閿濆憛鎾绘煏閸パ冾伃鐎规洦鍋婂畷鐔碱敇閻欌偓閺嗭繝姊绘担鍛婂暈缂侇喖鐭傞幆宀勫磼濠婂啫鐏婂銈嗙墬缁秵鍒婃總鍛婄厪闁割偅绻冮ˉ鐘绘煕閵娿儯鍋㈤柟?
            userTenantMapper.update(null, new LambdaUpdateWrapper<SysUserTenant>()
                    .eq(SysUserTenant::getUserId, user.getId())
                    .eq(SysUserTenant::getTenantId, tid)
                    .set(SysUserTenant::getPrefOrder, weight)
                    .set(SysUserTenant::getIsDefault, (defaultTenantId != null && defaultTenantId.equals(tid))));
            // 闂傚倷绀侀幖顐λ囬鐐茬柈闁圭虎鍠栭梻顖炴煙闂傚鍔嶉柣鎺戭煼閺岀喖鎮滃Ο铏逛淮濠?
            weight--;
        }
        return R.ok(true);
    }

    @Override
    public R<Boolean> changeLanguage(String lang) {
        // 闂傚倷绀侀幉锟犳偡閵夆晛纾圭憸鐗堝笒濮规煡鏌ｉ弮鍌氬付闁告劏鍋撻梻浣芥硶閸犳挻鎱ㄥ畡鎵殾闁挎繂顦伴悡銉︾箾閹寸偟鎳佸ù婧垮灪閵囧嫰寮村Ο铏逛紕闂佺娅曢惄顖炲春閳ь剚銇勯幒宥堝厡闁崇粯姊婚埀顒€绠嶉崕閬嶆偋閸℃稑鍌ㄩ柡澶嬪灍濡插牓鏌熼幑鎰垫綈妞ゅ繒濞€閺?
        if (!StringUtils.hasText(lang)) {
            return R.fail(500, AuthPromptEnum.LANG_EMPTY);
        }
        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔荤暗濞存粌缍婇弻鐔煎箚瑜嶉弳杈ㄣ亜閵堝懏鍤囬柡宀嬬秮閿濈偤顢楅埀顒佷繆娴犲鐓曢柍鍝勫€瑰渚?
        Long userId = CurrentUserUtils.getUserId();
        // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔荤暗濞存粌缍婇弻鐔煎箚瑜嶉弳杈ㄣ亜閵堝懏鍣界紒杈ㄥ笚瀵板嫮浠﹂懞銉р偓楣冩⒑缁洘娅嗛柡澶屾瀺
        Long tenantId = CurrentUserUtils.getTenantId();
        // 闂傚倷绀侀幖顐ょ矙閸曨厽宕叉繝闈涱儐閸嬫ɑ绻涢崱妯诲鞍闁稿鍔戦弻鏇熺節韫囨洜鏆犻梺缁樻尰濞茬喖寮婚敓鐘茬闂傚牊绋撴禒鈺呮⒑鐠団€崇仧缂佽埖宀搁獮鍡涘磼濠婂嫬鐝伴梺鑲┾拡閸撴稑宕?
        if (userId == null || tenantId == null) {
            return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        }

        // 闂傚倷绀侀幖顐︻敄閸涱垪鍋撳顐㈠祮闁糕斁鍋撳銈嗗笒閸婃悂鐎锋俊鐐€栧ú蹇涘垂婵犳艾绠犳繛鎴欏灩閻掑灚銇勯幒鎴濐仼缁炬儳顭烽悡顐﹀炊閵婏箑钄奸梺绋款儍閸旀垵顫?
        String prefKey = "fx:lang:" + tenantId + ":" + userId;
        try {
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍐噭閻庢碍纰嶉妵鍕箻椤栨侗鈧棝鏌＄€ｎ亝鍤囬柡灞剧〒閳ь剨缍嗘禍鐐电不娴煎瓨鐓忛柛顐ｇ矌閻瑧鈧娲樺畝鎼佸箚閸屾埃鏌﹂柣搴☆吙s
            redis.opsForValue().set(prefKey, lang);
        } catch (Exception e) {
            // 闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍏狀亞娆㈤悙鍝勭骇闁割偒鍋勬禍顖滄偖?
            return R.fail(500, AuthPromptEnum.LANG_SET_FAILED);
        }

        return R.ok(true);
    }


    /**
     * 缂傚倸鍊烽懗鑸靛垔鐎靛憡顫曢柡鍥ュ灩缁犳牕鈹戦悩鍙夋悙鐎瑰憡绻冩穱濠囶敍濠婂懎绗＄紓鍌氱М閸嬫捇姊婚崒姘偓鍝モ偓姘煎弮瀹曟繈寮介鐔蜂罕闁瑰吋鐣崺鍕枔?
     * <p>闂傚倸鍊风欢锟犲磻閸涱厙锝夊箳閺冣偓椤愯姤銇勯幇鍫曟闁哄拋鍓氶幈銊ヮ潨閸℃瀛ｅ┑顔硷躬缁犳牠寮婚敍鍕勃閻犲洦褰冩慨鏇炩攽閳ュ啿绾ч柟顔煎€搁悾鐑藉Ψ閳哄倹娅嗛梺鑺ッˇ鑺ョ瑜斿?admin 闂備浇宕甸崰鎰版偡閿旂偓鏆滈柣鏂挎憸閻?/p>
     *
     * @return 闂傚倷绀侀幖顐ょ矙閸曨厽宕叉繝闈涱儐閸嬫ɑ绻涢崱妯虹仼缁炬儳銈搁弻鐔煎箚瑜滈崵鐔访?
     * @see cn.dev33.satoken.stp.StpUtil#checkRole(String)
     */
    @Override
    @Deprecated
    public R<Boolean> secureAdmin() {
        // 濠电姷顣藉Σ鍛村磻閳ь剟鏌涚€ｎ偅宕岄柡宀嬬磿娴狅妇鎷犻幓鎺戭潛缂傚倸鍊哥粔鎾敄婢跺﹦鏆︽繝濠傚枤閸氬鏌涢妷锝呭婵炲牄鍨藉铏规喆閸曨剙鍞夐梺琛″亾闁告鍊ｉ敐澶婄濞达絾鐡曡闂備焦瀵уΛ浣肝涢崟顖ｆ晜闁绘柨鍚嬮悡娑㈡煕椤愶絿杩旈柍铏瑰瀾in闂備浇宕甸崰鎰版偡閿旂偓鏆滈柣鏂挎憸閻?
        StpUtil.checkLogin();
        return R.fail(CommonPrompt.BAD_REQUEST);
    }

    /**
     * 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺嗐倕銆掑锝呬壕闂佽鍠曠划娆愪繆閹间焦鏅濋柍褜鍓熼幃锟犲籍缂嶆繈姊婚崒娆戝妽鐟滄澘鍟扮划鏃堝醇閺囩喎浠鹃梺绯曞墲閻熴垽宕戦幘缁橆棃婵炴垶鐟Λ銈夋⒑?
     * <p>闂備浇顕х换鎰崲閹邦儵娑樷枎閹存柨浜鹃柛顭戝亝閻ㄦ垿鏌嶈閸撴繈锝炴径濠庢僵闁靛ň鏅涢弸渚€鏌曢崼婵愭Ц缂佺媴绲介—鍐偓锝庝簻椤掋垽鏌ｉ敐澶夋喚闁诡喗顨婂Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掗幘璇茶摕閻忕偟鐡旈崥瀣煕閵夈垺娅嗛柣搴幗缁绘盯骞嬮悙鏉戦瀺濠碉紕鍋熼弲顐﹀礆閹烘鏁嶆繝濠傚椤庢盯姊洪棃娑氬闁硅櫕鍔曞嵄闁哄洢鍨洪悡?"123456"闂傚倷鐒︾€笛呯矙閹寸偟闄勯柡鍐ｅ亾缂佺粯鐩畷鍫曨敆娴ｉ晲缂?BCrypt 缂傚倸鍊烽懗鑸垫叏閻㈡悶鈧啴宕卞▎鎰幑闂佽偐顭堥悘姘閵堝棛绠鹃柟瀵稿仧閻倝鏌￠埀顒佺鐎ｎ偆鍘卞┑鐘绘涧濡稒鏅堕弴鐘电＜婵°倐鍋撴繛灞傚姂閸┾偓妞ゆ帊鑳堕埊鏇㈡煥濮樿埖鐓曢悗锝庝簽鏁堟繝?/p>
     *
     * @param userId 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︽叏閸︻厽瀚?
     * @return 闂傚倸鍊烽悞锕併亹閸愵亞鐭撻柛顐ｆ礃閸嬵亪鏌涢埄鍐噮缁炬儳銈搁弻鐔煎箚瑜滈崵鐔访?
     */
    @Override
    public R<Boolean> resetPasswordById(Long userId) {
        if (userId == null) {
            return R.fail(CommonPrompt.USER_ID_CANNOT_BE_EMPTY);
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        String hashed = encryptPassword(resolveDefaultPassword());
        int n = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, hashed)
                .eq(SysUser::getId, userId));
        if (n > 0) {
            return R.ok(true);
        }
        return R.fail(CommonPrompt.RESET_FAILED);
    }

    /**
     * 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃澒闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掓惔锝呭灊闁汇垻顭堥崘鈧銈嗗姧缁查箖顢樺ú顏呪拻濞达綀顫夌欢鑼磼婢跺﹦鍩ｉ柟?
     * <p>
     * 婵犵數鍋涢顓熸叏閹绢喗鐓€闁挎繂顦崒銊╂煏閸繃宸濈痪鍓у亾閵囧嫰寮崶顬挾绱掗悩宕囨创闁哄本鐩顒傛偘閳ュ厖澹曢梺鑹板濞夋盯鐛埀顒勬⒑閼姐倕孝婵炴祴鏅犻、姘愁樁闁靛洦鍔欏畷鍫曞煘閹傚闂傚倸鐗婄粙鎺椝夐悩缁樼厽闁哄秲鍔庨惌宀€绱掗鐐毈鐎殿喕绮欓、鏇㈡晲閸♀晛鎮堥梻鍌氬€烽悞锕€顭垮Ο鑲╃煋闁割偅娲橀崑顏堟煕閳╁啰鈯曢柡鍜佸墴閹﹢鎮欓悽鍨啒濠电偛鐪伴崐婵嬪蓟閿涘嫧鍋撻敐搴′簽闁活厼妫涚槐鎺懳旈埀顒勊囬柆宥呯劦妞ゆ帊鑳堕埊鏇㈡煥濮橆厾绡€闁逞屽墴椤㈡棃宕煎┑鍫Т婵犵數鍋涘Λ娆戞暜閻愮數涓嶉柡灞诲劜閻撴洟鏌曟竟顖氬暕濡叉劗绱撴笟鍥ф珮闁搞劍濞婇獮蹇曗偓锝庡枛鍥存繝銏ｆ硾閿曘倗鎸х€ｎ剛纾介柛灞捐壘閺嬨倝鏌涢悩铏磳闁糕斁鍋?
     * </p>
     *
     * @return 闂備浇顕ч柊锝咁焽瑜嶈灋婵炴垯鍨洪崑澶嬬節婵犲倹鍣藉瑙勫▕閺屸€愁吋鎼粹€茬凹濠电偛鐗炵紞浣割潖濞差亜绾ч悹鎭掑壉閵堝鐓熼柕鍫濇噺椤ョ娀鏌嶈閸撴盯寮幖浣碘偓浣圭節閸ャ劌鐦?
     * @see com.forgex.common.domain.config.PasswordPolicyConfig
     * @see com.forgex.common.config.ConfigService#getJson(String, Class, Object)
     */
    private String resolveLoginTerminal(String loginTerminal) {
        return StringUtils.hasText(loginTerminal) ? loginTerminal.trim().toUpperCase(Locale.ROOT) : AuthTerminalConstants.B;
    }

    private String resolveLoginType(String loginType) {
        return StringUtils.hasText(loginType) ? loginType.trim().toUpperCase(Locale.ROOT) : LoginTypeConstants.ACCOUNT_PASSWORD;
    }

    private PasswordPolicyConfig getPasswordPolicyConfig() {
        PasswordPolicyConfig defaults = new PasswordPolicyConfig();
        defaults.setStore("bcrypt");
        defaults.setDefaultPassword("Aa123456");
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, defaults);
        return policy == null ? defaults : policy;
    }

    /**
     * 闂備浇宕甸崰鎰版偡鏉堚晛绶ゅΔ锝呭暞閸婄敻鏌ｉ敐鍛槵闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掓惔銊ョ劦妞ゆ帊鑳堕埊鏇㈡煥濮樿埖鐓曢悗锝庝簽鑲栫紓渚囧枤閺佽顕ｆ禒瀣垫晝闁挎繂妫崥?
     * <p>
     * 婵犵數鍋涢顓熸叏鐎涙﹩娈介柟闂寸劍閸庡﹪鏌熸潏楣冩闁稿孩锚闇夐柨婵嗙墛椤忕娀鏌熼懞銉х煉闁哄矉缍侀弫鎰板礃閵娿儱袝闂備礁鎲¤彠闁稿﹥顨堝Σ鎰板籍閸偅鏅╅梺鍝勫缁绘劙鐛埀顒勬⒑閼姐倕鏋戦柣鐔讳含瀵板﹥銈ｉ崘鈺佲偓鐢告煟閿濆懎顦柛瀣崌濡啫鈽夊▎妯伙骏闂備胶绮悧妤冨垝鎼淬劌鐒垫い鎺嶈兌閳洟鏌ㄥ鑸电厱閻庯綆浜炴晥閻庢鍣崜鐔煎箖閻戣棄绠ユい鏃傝檸閺€銊╂⒒娴ｇ懓顕滅紒瀣灴閹矂宕掑鍐ㄧ亰濡炪倖鐗楃划宥嗗垔婵傚憡鐓忛柛顐ｇ箓椤忣剛绱掗埀?bcrypt闂?
     * </p>
     *
     * @param policy 闂備浇顕ч柊锝咁焽瑜嶈灋婵炴垯鍨洪崑澶嬬節婵犲倹鍣藉瑙勫▕閺屸€愁吋鎼粹€茬凹濠电偛鐗炵紞浣割潖濞差亜绾ч悹鎭掑壉閵堝鐓熼柕鍫濇噺椤ョ娀鏌嶈閸撴盯寮幖浣碘偓浣圭節閸ャ劌鐦?
     * @return 闂備浇顕ч柊锝咁焽瑜嶈灋婵炴垯鍨洪崑澶嬬節婵犲倹鍣抽柛瀣尭閳藉鈻嶉褌閭€规洘鍨甸濂稿炊閳哄應鏀洪梻浣告惈缁嬩線宕戦崨顓涙灁闁挎繂顦伴悡銉︾箾閹寸倖鎴犵礊閸掝晿ypt 闂?sm2闂?
     * @see com.forgex.common.domain.config.PasswordPolicyConfig#getStore()
     */
    private String resolvePasswordStore(PasswordPolicyConfig policy) {
        return policy == null || !StringUtils.hasText(policy.getStore()) ? "bcrypt" : policy.getStore();
    }

    /**
     * 闂備浇宕甸崰鎰版偡鏉堚晛绶ゅΔ锝呭暞閸婄敻鏌ら幁鎺戝姶婵炴捁鍩栨穱濠囶敍濞戝崬鍔岄梺鍛婅壘濞差參骞冨Δ鍛棃婵炴垶鐟Λ銈夋⒑?
     * <p>
     * 婵犵數鍋涢顓熸叏鐎涙﹩娈介柟闂寸劍閸庡﹪鏌熸潏楣冩闁稿孩锚闇夐柨婵嗙墛椤忕娀鏌熼懞銉х煉闁哄矉缍侀弫鎰板礃閵娿儱袝闂備礁鎲¤彠闁稿﹥顨堝Σ鎰板籍閸偅鏅╅梺鍝勫缁绘劙鐛埀顒勬⒒娴ｇ鎮戦柛搴㈠▕瀹曟煡鎳犻煬韫睏闂佹悶鍎洪悡鍫濄€掗懜鍏哥箚妞ゆ牗渚楅崕銉╂煕濮橆剚鍠橀柟顔筋殜濡啫鈽夊▎妯伙骏闂備胶绮悧妤冨垝閹捐鏋佺€广儱鎳愰弳鍡涙煃瑜滈崜鐔奉嚕椤愶箑围闁糕剝鐟ú鎼佹煙閸忚偐鏆橀柛鏂胯嫰閳诲秹濮€閵忋垻锛濋梺鍛婂姈閸庢娊寮搁妶澶嬬厸濞达綁娼ч埀顒佺箞閻涱喚鈧綆浜為弳瀣煙濞堝灝鏋熸い顐ｅ哺濮婂搫煤鐠囧弶鐏嗛梻鍌氬缁夊綊宕洪埀?
     * </p>
     *
     * @return 婵犳鍠楃敮妤冪矙閹烘せ鈧箓宕奸妷顔芥櫍闂佺厧顫曢崐鎰板磻閹剧粯顥堟繛鎴炵懐濡倝姊虹紒妯煎缂侇喖閰ｉ崺鈧い鎺嶈兌閳洘銇勯妸銉уⅵ妞ゃ垺鐗楅幏鍛村捶椤撴稒鐏?
     * @see com.forgex.common.domain.config.PasswordPolicyConfig#getDefaultPassword()
     */
    private String resolveDefaultPassword() {
        PasswordPolicyConfig policy = getPasswordPolicyConfig();
        return StringUtils.hasText(policy.getDefaultPassword()) ? policy.getDefaultPassword() : "Aa123456";
    }

    /**
     * 闂傚倷绀侀幉鈥愁潖缂佹ɑ鍙忛柟缁㈠枟閸庡﹪鏌熺€电袥闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻?
     * <p>
     * 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺嗐倕霉閿濆牄鈧偓闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掓惔锝呭灊闁汇垻顭堥崘鈧銈嗗姧缁查箖顢樺ú顏呪拻濞达綀顫夌欢鑼磼婢跺﹦鍩ｉ柟顔兼健瀹曞崬鈽夊▎蹇撳闂備礁鎲″ú锕傚磻閸曨垽缍栭柨婵嗩槹閸婄敻姊婚崼鐔衡姇妞ゃ儳鍋ら弻鈥崇暆閳ь剟宕版惔顭戞晪闁挎繂鐗忛悿鈧梺鐟扮仢閸燁偄鈻撻幖浣光拺闁告稑顭悞浠嬫煕閻樻煡鍙勯柨婵堝仱楠炲洭顢欓悷棰佸闂傚倸鐗婄粙鎺椝夐悩缁樼厽闁哄秲鍔庨惌瀣箾閼归偊鍤欓柍钘夘槸椤劍鎯旈姀鈩冩緫闂傚倷绀侀幉鈥愁潖缂佹ɑ鍙忛柟缁㈠枟閸庡﹪鏌熸潏鍓х暠鏉?
     * </p>
     *
     * @param rawPassword 闂傚倷绀侀幉锟犫€﹂崶顒€绐楅幖鎼厜缂嶆牠鏌熼柇锕€鏋撻柛瀣崌濡啫鈽夊▎妯伙骏闂?
     * @return 闂傚倷绀侀幉鈥愁潖缂佹ɑ鍙忛柟缁㈠枟閸庡﹪鏌熸潏鍓х暠閻熸瑱绠撻獮鏍ㄦ綇閸撗吷戦梺浼欑秮娴滃爼骞冨Δ鍛棃婵炴垶鐟Λ銈夋⒑缂佹澧紒顔奸叄閸┾偓妞ゆ帊鑳堕埊鏇熴亜閵娿儳澧︽い銏＄墬閹峰懘宕烽娑欑亙?
     * @see com.forgex.common.crypto.CryptoPasswordProvider#encrypt(String)
     * @see com.forgex.common.crypto.CryptoProviders#resolve(String, com.forgex.common.config.ConfigService)
     */
    private String encryptPassword(String rawPassword) {
        CryptoPasswordProvider provider = CryptoProviders.resolve(resolvePasswordStore(getPasswordPolicyConfig()), configService);
        if (provider.supportsEncrypt()) {
            return provider.encrypt(rawPassword);
        }
        if (provider.supportsHash()) {
            return provider.hash(rawPassword);
        }
        throw new IllegalStateException("Unsupported password store: " + provider.name());
    }

    /**
     * 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁稿骸鐭傞幃褰掑炊椤忓嫮姣㈢紓浣割槸濞硷繝骞冨Δ鍛仭闂侇叏闄勭瑧闂備礁鎲￠悷銉ф崲濮椻偓瀵粯绻濋崒銈呮倯闂佹悶鍎弲婊堟倵?
     * <p>
     * 婵犵數鍋涢顓熸叏閹绢喗鐓€闁挎繂顦崒銊╂煏閸繃宸濈痪鍓у亾閵囧嫰寮崶顬挾绱掗悩宕囨创闁哄本鐩顒傛偘閳ュ厖澹曢梺鑹板濞夋盯鐛埀顒勬⒑閼姐倕孝婵炴祴鏅犻、姘愁樁闁靛洦鍔欏畷鍫曨敆娴ｇ澹冮梺鑽ゅТ濞测晝浜稿▎鎴犵煋闁秆勵殕閸婄敻鏌ｉ悢鐓庝喊婵″弶鎮傞弻娑樷枎韫囨洜顔掗梺鍝勮嫰閿曘倝顢樻總绋跨倞闁冲搫鍊婚埀顒夊弮濮婃椽骞愭惔锝傛闂佺粯顨呯换鎰板煝瀹ュ唯闁靛绲煎Ч妤呮⒑閸濆嫭宸濋柛瀣灦缁傚秵绺介崨濠備化闂佽澹嬮弲娑㈠礉閿旀垝绻嗛柣鎰靛墯閵囨繄鈧娲樺銊╁箯鐎ｎ喖鎹舵い鎾跺濡叉挳姊绘担鐟邦嚋缂佸鍨块幊鐔碱敍閻愵亖鍋撻崨閭︽▌閻庢鍠氶…鍫モ€﹂妸鈺佺劦妞ゆ帒鍊荤粻鏂款熆鐠哄搫顦柛瀣尭閳藉鈻嶉褌绨婚柨鏇樺灲椤㈡棃宕煎┑鍫Т婵犵數鍋涘Λ娆戞暜閻愮數涓嶉柡灞诲劜閻撴洟鏌曟竟顖氬暕濡叉劗绱撴笟鍥ф珮闁搞劍濞婇獮蹇曗偓锝庡枛鍥存繝銏ｆ硾閿曘倗鎸х€ｎ剛纾介柛灞捐壘閺嬨倝鏌涢悩铏磳闁糕斁鍋?
     * </p>
     *
     * @return 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閸洖鐒垫い鎺戝€归崵鈧銈忓瘜閸ㄨ泛鐣烽敐鍫㈢杸婵炴垶鐟ч崢鎰版倵楠炲灝鍔氭繛灞傚姂瀵悂宕掗悙鏉戔偓鐢告煟閵忊槅鍟忛柡鈧导瀛樼參?
     * @see com.forgex.common.domain.config.LoginSecurityConfig
     * @see com.forgex.common.config.ConfigService#getJson(String, Class, Object)
     */
    private LoginSecurityConfig getLoginSecurityConfig() {
        LoginSecurityConfig defaults = LoginSecurityConfig.defaults();
        LoginSecurityConfig config = configService.getJson(KEY_SECURITY_LOGIN_FAILURE, LoginSecurityConfig.class, defaults);
        return config == null ? defaults : config;
    }

    /**
     * 濠电姷顣藉Σ鍛村磻閳ь剟鏌涚€ｎ偅宕岄柡宀嬬磿娴狅妇鎷犻幓鎺懶撳┑鐘灱濞夋稓鈧碍婢橀悾鐑芥晲婢跺鍙嗛柣搴秵閸婏綁鏁冮崒娑氬幈濠殿喗锚婢т粙鎮鹃搹顐犱簻妞ゆ劦鍓涢悾鐢告煛鐏炶濡介柟宄版嚇瀹曨偊濡烽敂鐣屾Д
     * <p>
     * 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺嗐倕銆掑锝呬壕闂佽鍨伴崐濠氬箯閻樼粯鍤戞い鎺戝€愰銏＄厽閹艰揪绲鹃崵鈧銈忓瘜閸ㄨ泛鐣烽敐鍫㈢杸婵炴垶鐟ч崢鎰版倵楠炲灝鍔氭繛灞傚姂瀵悂宕掑☉妤冪畾濡炪倖鍔楅崰搴㈢妤ｅ啯鈷戦柛婵嗗椤箓鏌涙惔锛勬憼鐎垫澘瀚板畷鐔碱敍濮樿京鍘梻鍌欑閻忔繈顢栭崱娆戞噮闂傚倷绀侀幉锟犳偄椤掑倻涓嶉柟瀛樼贩閸濆嫷鐓ラ柛鏇ㄥ厸濮橈箓姊虹紒妯哄闁轰焦鎮傚顐ｃ偅閸愨晝鍘介悷婊勫灴椤㈡俺顦寸紒杈╁仜椤撳ジ宕卞Δ鍐嵁闂佸搫顦悧婊堝磻婢跺寒鏉洪梻鍌欑閸氬顭垮鈧幆灞剧瑹閳ь剟鏁愰悙鐑樻櫇闁稿本绋撻崣鍡樼箾鏉堝墽鍒伴柟鑺ョ矒瀵櫕瀵肩€涙鍘?
     * </p>
     *
     * @param account 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閸洖绠憸鐗堝笚閺呮繈鏌嶈閸撴繈濡?
     * @param config 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閸洖鐒垫い鎺戝€归崵鈧銈忓瘜閸ㄨ泛鐣烽敐鍫㈢杸婵炴垶鐟ч崢鎰版倵楠炲灝鍔氭繛灞傚姂瀵?
     * @return 婵犵數濮烽。浠嬪焵椤掆偓閸熷潡鍩€椤掆偓缂嶅﹪骞冨Ο璇茬窞濠电姴楠搁弲鐘绘⒑閸濆嫯顫﹂柛搴㈢叀瀵偄顫濋懜闈涒偓鐢告偡濞嗗繐顏уù婧垮灮缁辨帞绱掑Ο铏逛紝閻庤娲栭悥濂稿箖濞嗘垳娌柛灞剧懄濠⑩偓闂備浇宕垫慨鐢稿礉閹达箑纾块梺顒€绉寸粻褰掓⒑閸噮鍎愰柛搴ｅ枛閺岋繝宕橀埡鍌氭殫缂備讲鍋撻柛鏇ㄥ灡閻撴洟鏌″畵顔煎濞咃綁姊虹拠鈥崇仩闁绘鎸搁悾宄邦煥閸繄鍘搁梺閫炲苯澧扮紒杈╁仱楠炲鏁傞悾灞藉汲濠电偠鎻徊浠嬪箺濠婂牆姹插ù鐓庣摠閻撴盯鏌涢幇鈺佸闁糕晪绲炬穱濠囶敃閵忊€充淮闂佽桨绀佺粔闈涱嚗閸曨偀妲堟慨姗€纭稿Σ鐑芥⒒娴ｅ憡鍟為柣鏃戝墰缁骞嬮敃鈧崹鍌涖亜閺嶃劎鈯曞ù婧垮€濋弻锟犲磼濞戞﹩鍤嬬紓浣插亾闁?null
     * @see com.forgex.common.domain.config.LoginSecurityConfig#getLockMinutes()
     */
    private R<List<TenantVO>> checkLoginLocked(String account, LoginSecurityConfig config) {
        if (!StringUtils.hasText(account) || config == null || config.getLockMinutes() == null || config.getLockMinutes() <= 0) {
            return null;
        }
        String lockKey = LOGIN_LOCK_KEY_PREFIX + normalizeAccountKey(account);
        Long ttl = readKeyTtlSeconds(lockKey);
        if (ttl == null || ttl <= 0) {
            return null;
        }
        long minutes = Math.max(1L, (ttl + 59) / 60);
        return R.fail(CommonPrompt.ACCOUNT_LOCKED, String.valueOf(minutes));
    }

    /**
     * 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹦浠㈡い顓炴健楠炲棝骞嶉鍓у嫎闂佽崵鍠愰悷杈╃不閹捐绠栭柍鍝勬噺閸ゅ鐥悧鍩亝绂?
     * <p>
     * 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚濞诧妇鈧艾顭烽弻銊╁即濡も偓娴滈箖鎮峰鍕凡婵炵》绻濋獮鍡涘礃椤旇偐顦板銈嗘尵閸ｃ儲绂掗鈧幃宄邦煥閸涱収鏆┑鐐插级閻楃姵淇婇悽绋跨闁绘ê鍟块鎼佹⒑闂堟侗妾ч悗闈涚焸瀹曟垼顦归柡灞稿墲閹峰懐绮欑捄銊ф晨缂傚倷娴囨ご鍝ユ崲閸℃稑桅闁圭増婢樼粻濠氭煕閿旇甯ㄩ柕蹇嬪€曠痪褔鏌嶆潪鎵妽闁哥姵蓱娣囧﹪骞撻幒鎾虫灎閻庤娲橀崝娆忕暦濡ゅ懎绀傜痪鎷岄哺琚ч梻鍌氬€风粈渚€濡靛Ο鑲╃焼濞撴埃鍋撻柟顕€绠栧畷婊嗩槼閻庢艾顭烽弻銊╁即濡も偓娴滈箖鎮峰鍕凡婵炲皷鍓濇穱?
     * </p>
     * <p>婵犵數濮伴崹鐓庘枖濞戞埃鍋撳鐓庢珝妤犵偛鍟换婵嬪炊閵娧冨Х闂備礁鎼€氼剛鎹㈤幒鏃€鏆滈柛鈩冪⊕閻?/p>
     * <ol>
     *   <li>婵犵數濮伴崹娲磿閼测晛鍨濋柛鎾楀嫬鏋傞梺鎸庢磵閸嬫捇鏌嶇拠鏌ュ弰闁糕晛瀚板畷姗€鍩￠崒銈呮櫃闂?1</li>
     *   <li>闂備浇宕垫慨宕囩矆娴ｈ娅犲ù鐘差儐閸嬵亪鏌涢埄鍏狀亞娆㈤悙鍝勭骇闁割偒鍋勬禍顖滄偖濠靛棌鏀介柣鎰级椤ユ垿鏌涢幘瀵哥疄鐎殿喗濞婇幃銏ゆ倻濡警鈧盯姊洪崫鍕垫Ъ婵炲娲滅划鏃堝锤濡や胶鍘介梺闈涱焾閸庨亶顢旈鍌滅＜閻犲洦褰冮弳锝団偓娈垮枤閺佸骞冮姀銈呭窛濠电姴瀛╄ⅶ闂?/li>
     *   <li>婵犵數濮烽。浠嬪焵椤掆偓閸熷潡鍩€椤掆偓缂嶅﹪骞冨Ο璇茬窞閻忕偟鍋撻崕閬嶆⒑閸涘﹦鈯曞鐟版缁寮介鐔哄幐闂佸壊鍋呴懝鐐閻愵剛绡€闁靛繒濮烽崹濠氭煕閹板墎绱伴柡澶樺弮閺岋綁鎮╅崘鎻掝潎闂佺瀛╅悡锟犮€侀弴鐔洪檮缂佸瀵ч悗顒勬煟閵忊晛鐏遍柛鐘虫皑缁牊寰勯幇顓涙嫼婵犮垼娉涢幗婊埶囬敃鍌涚厽闁挎繂鎳庨埛鏃堟煙瀹勬壆绉洪柡浣稿暣閸┾偓妞ゆ帒鍟ㄦ禍?/li>
     * </ol>
     *
     * @param account 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閸洖绠憸鐗堝笚閺呮繈鏌嶈閸撴繈濡?
     * @param config 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閸洖鐒垫い鎺戝€归崵鈧銈忓瘜閸ㄨ泛鐣烽敐鍫㈢杸婵炴垶鐟ч崢鎰版倵楠炲灝鍔氭繛灞傚姂瀵?
     * @see com.forgex.common.domain.config.LoginSecurityConfig#getMaxFailCount()
     * @see com.forgex.common.domain.config.LoginSecurityConfig#getLockMinutes()
     */
    private void recordLoginFailureState(String account, LoginSecurityConfig config) {
        if (!StringUtils.hasText(account) || config == null) {
            return;
        }
        Integer failWindowMinutes = config.getFailWindowMinutes();
        Integer maxFailCount = config.getMaxFailCount();
        Integer lockMinutes = config.getLockMinutes();
        if (failWindowMinutes == null || failWindowMinutes <= 0 || maxFailCount == null || maxFailCount <= 0) {
            return;
        }
        String accountKey = normalizeAccountKey(account);
        String failKey = LOGIN_FAIL_COUNT_KEY_PREFIX + accountKey;
        try {
            RAtomicLong failCounter = redissonClient.getAtomicLong(failKey);
            long currentCount = failCounter.incrementAndGet();
            failCounter.expire(Duration.ofMinutes(failWindowMinutes));
            if (currentCount >= maxFailCount && lockMinutes != null && lockMinutes > 0) {
                redissonClient.getBucket(LOGIN_LOCK_KEY_PREFIX + accountKey).set("1", Duration.ofMinutes(lockMinutes));
                failCounter.delete();
            }
        } catch (Exception e) {
            log.warn("闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儺浜滈惃铏圭磼婢跺﹦浠㈡い顓炴健楠炲棝骞嶉鍓у嫎闂佽崵鍠愰悷杈╃不閹捐绠栭柍鍝勬噺閸ゅ鐥悧鍩亝绂嶉幆顬″綊鏁愰崱妯轰哗婵炲瓨绮岄妶鎼佸箖? account={}", account, e);
        }
    }

    /**
     * 濠电姷鏁搁崑鐐哄箰閹间礁绠犳俊顖濄€€閺嬪秹骞栧ǎ顒€濡介柛搴＄焸閹綊宕堕鍕缂備礁顦悘婵嬧€﹂崸妤€绠氶柟娈垮枤缂堥亶鏌ｉ悢鍝ユ嚂缂佺姵鎹囬獮鍐煛閸涱喖娈濈紒鍓у閿氬ù?
     * <p>
     * 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓瑜版帞宓侀悗锝庡枛缁犳稒銇勯幒鍡椾壕濡炪値鍋嗘繛鈧柡灞剧☉椤繈顢楅埀顒勵敆閵忋垻纾奸柡鍌涱儥閻撳吋鎱ㄦ繝浣虹煓濠碘€崇埣瀹曠厧鈹戦幇顓夌喖姊绘担鍛婂暈闁荤啿鏅犺棢闁圭偓鏋奸弸鏃堟煙鐎电浠滅紒鍓佸仱瀵爼宕奸顫嚱閻炴碍绻傞埞鎴︽倷閸欏妫忛梺鍛婃尰缁诲牆顕ｉ幎鑺ュ亜闁绘垶锚椤庢盯姊洪崫鍕垫Ъ婵炲娲滅划鏃堝锤濡や胶鍘卞┑鐘诧工閸熶即鎮為幖浣圭厸闁逞屽墴閺佹劙宕堕敐鍛闁荤喐鐟ョ€氼參鎯冮幋婵冩斀妞ゆ棁濮ら崐鎰殽閻愨晛浜惧┑鐐舵彧缁插潡銆傛禒瀣；?
     * </p>
     *
     * @param account 闂傚倷娴囬惃顐﹀幢閳轰焦顔勭紓鍌氬€哥粔瀵哥矓閸洖绠憸鐗堝笚閺呮繈鏌嶈閸撴繈濡?
     */
    private void clearLoginFailureState(String account) {
        if (!StringUtils.hasText(account)) {
            return;
        }
        String accountKey = normalizeAccountKey(account);
        try {
            redissonClient.getAtomicLong(LOGIN_FAIL_COUNT_KEY_PREFIX + accountKey).delete();
            redissonClient.getBucket(LOGIN_LOCK_KEY_PREFIX + accountKey).delete();
        } catch (Exception e) {
            log.warn("濠电姷鏁搁崑鐐哄箰閹间礁绠犳俊顖濄€€閺嬪秹骞栧ǎ顒€濡介柛搴＄焸閹綊宕堕鍕缂備礁顦悘婵嬧€﹂崸妤€绠氶柟娈垮枤缂堥亶鏌ｉ悢鍝ユ嚂缂佺姵鎹囬獮鍐煛閸涱喖娈濈紒鍓у閿氬ù婊勫劤闇夐柨婵嗘閸嬬姴霉濠婂啨鍋㈤柟? account={}", account, e);
        }
    }

    /**
     * 闂傚倷绀侀幖顐ょ矓閺夋嚚娲Χ婢跺﹪妫峰銈嗙墱閸嬫稓绮堝畝鍕厸鐎广儱楠告晶顔姐亜閿旇娅婇柡灞剧☉閳诲酣骞嬪┑鍡楃闂?
     * <p>
     * 闂備浇顕х换鎰崲閹邦儵娑橆煥閸曨偆绐為梺鍛婃处閸ㄤ即鎯屽Δ鍛厽闁靛牆楠搁悘锛勭磼娓氬洤娅嶉柡宀€鍠撻幏鐘诲焺閸愵亞鐛㈤梺鑽ゅУ閸旀宕伴幘瀵割洸婵繂鐬奸悿鈧梺瑙勫礃濞呮洖鈻嶉姀鐘栨棃鎮╅棃娑楀摋濠电偛顦伴惄顖炴偘椤曗偓楠炴绱掑Ο閿嬪瘱闂備線娼ч悧鍡浰囬挊澹濆綊寮撮姀鈥斥偓鍫曠叓閸ャ劍灏柍閿嬫閺屽秶鎲撮崟顓炩拰閻庢鍠氶弲顐⑩槈閸偒妲归幖杈剧秵濡茬兘姊绘担瑙勩仧婵炵厧娼″畷婵堚偓锝庡墲婵?Redis 闂傚倸鍊烽悞锕傤敄濞嗘挸绐楁慨姗嗗厴閺嬫棃鏌熺€电啸缁炬儳銈搁弻锝夊棘閸噮鏆㈠銈呯箰瀹曨剟鈥︾捄銊﹀磯闁告繂瀚锋导鈧梻浣筋嚃閸犳牠鏁冮鍛箚?
     * </p>
     *
     * @param account 闂傚倷绀侀幉锟犫€﹂崶顒€绐楅幖鎼厜缂嶆牠鏌熼柇锕€鏋熼悗姘煼閺屻劑寮村Δ鈧禍楣冩偡?
     * @return 闂傚倷绀侀幖顐ょ矓閺夋嚚娲Χ婢跺﹪妫峰銈嗙墱閸嬫稓绮堝畝鍕厸鐎规搩鍠栫€氼剟鍩€椤掑倹鏆柡宀嬬秮婵℃悂濡烽妷顔荤磽濠电姰鍨煎▔娑氣偓姘緲閻ｇ兘鏁愭径瀣偓濠氭煠閹帒鍔滄繛?
     */
    private String normalizeAccountKey(String account) {
        return account == null ? "" : account.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 闂備浇宕垫慨鏉懨洪埡鍜佹晪鐟滄垿濡甸幇鏉跨倞妞ゆ巻鍋撶紒鐘卞嵆閺屾盯濡烽幋婵囧櫤婵?Redis 闂傚倸鍊烽悞锕傤敄濞嗘挸绐楁慨姗嗗厴閺嬫棃鏌熸潏鍓х暠缂佲偓閸儲鐓冮悶娑掆偓鍏呭缂?TTL闂傚倷鐒︾€笛呯矙閹达附鍋嬮煫鍥ㄥ喕缂嶆牗淇婇妶鍛櫤闁哄拋鍓欓…璺ㄦ崉閾忕懓顣甸梺?
     * <p>
     * 婵犵數鍋犻幓顏嗙礊閳ь剚绻涙径瀣鐎?Redisson 闂傚倷绀侀幉锟犫€﹂崶顒€绐楅柟鎹愵嚙閺?API闂傚倷鐒︾€笛呯矙閹达附鍎楅柛灞剧☉椤曢亶鏌嶉崫鍕櫣缂佲偓閸屾壕鍋撻獮鍨姎妞わ富鍨跺畷婵嬪Ψ閳哄倻鍘?Redisson + Spring Data Redis 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛鍟幆鏃堝閳垛晜鐏?TTL 闂備浇宕垫慨鏉懨洪埡鍜佹晪鐟滄垿濡甸幇鏉跨倞闁冲搫鍊搁惃顐︽⒑閸濆嫷妲搁柣蹇旂箞閹繝宕掗悙绮规嫼闂佸湱顭堝ù鐑藉箠閸℃ǜ浜滈柡鍥埀顒佺墵楠炲牓濡搁埡浣虹潉闂侀€炲苯澧柡鍛埣椤㈡﹢濮€閿涘嫬寮板┑鐑囩到濞诧附鏅舵惔銊ョ；?
     * </p>
     *
     * @param key Redis 闂?
     * @return 闂傚倷绀侀幉锟犲箰閹绢喖鐤炬繛鍡樺灩缁€濠囨煙鐎电啸闁活厽鎹囬弻鐔兼倻濡闉嶅┑鐐茬墕閻栧ジ寮婚妸銉㈡婵炲棙鍨熷Λ鈺冪磽娴ｈ娈旀い锕備憾閸┾偓妞ゆ帊鑳堕埊鏇㈡煥濮橆厾绡€闁逞屽墴椤㈡棃宕煎┑鍫悈闂備礁鎼粙鍕繆閸ヮ剚鍋℃繝闈涱儐閻撴洘绻涢崱妯忣亪鎮橀敐澶嬬厸濞达綁娼ч埀顒佺箓椤曪綁顢氶埀顒勭嵁閸℃稑绠ユい鏃傝檸閸炲綊姊?null
     */
    private Long readKeyTtlSeconds(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            long ttlMillis = bucket.remainTimeToLive();
            if (ttlMillis <= 0L) {
                return null;
            }
            return Math.max(1L, (ttlMillis + 999L) / 1000L);
        } catch (Exception e) {
            log.warn("闂備浇宕垫慨鏉懨洪埡鍜佹晪鐟滄垿濡甸幇鏉跨倞妞ゆ帊绀侀崜顒勬煟閻樺弶绌块悘蹇旂懅缁棃宕稿Δ浣叉嫼婵犮垼娉涢幗婊堟偩閻┿帨婵犵數濮伴崹娲磿閼测晛鍨濋柛鎾楀嫬鏋? key={}", key, e);
            return null;
        }
    }

    /**
     * 闂?Map 闂備礁鎼ˇ閬嶅磿閹版澘绀堟慨姗嗗墰閺嗭箓鏌涘▎蹇ｆШ闁?JSON 闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
     * <p>
     * 婵犵數鍋犻幓顏嗙礊閳ь剚绻涙径瀣鐎?Hutool 闂佽姘﹂～澶愬箖閸洖纾块梺顒€绉撮惌妤呮偣閹帒濡块悘蹇曟暬閹綊宕堕鍕濠?Map 闂備浇顕уù鐑藉极閹间降鈧焦绻濋崶銊ョ樁闂佸憡娲﹂崜姘跺磿閻旇偐鍙撻柛銉ｅ妽鐏忣參鏌ｈ箛鏃傛噰闁哄本鐩浠嬪Ω瑜嶉埅鍫曟煟?JSON 闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝閺冨牊鍊堕柣鎰絻閳ь剛鏁诲畷?
     * </p>
     *
     * @param value Map 闂備浇顕уù鐑藉极閹间降鈧焦绻濋崶銊ョ樁?
     * @return JSON 闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
     * @see cn.hutool.json.JSONUtil#toJsonStr(Object)
     */
    private String toJson(Map<String, Object> value) {
        return JSONUtil.toJsonStr(value);
    }

    /**
     * 闂備浇宕甸崰鎰版偡鏉堚晛绶ゅΔ锝呭暞閸婄敻鏌涙繝鍕瀭濞存粌缍婇弻鐔煎箚瑜嶉弳杈ㄣ亜?Token 闂?TTL
     * <p>
     * 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺?Token 闂備浇宕垫慨宕囨閵堝洦顫曢柡鍥ュ灪閸嬧晛鈹戦悩瀹犲閻庢艾顦…璺ㄦ崉娓氼垰鍓卞┑鐐叉噹缁绘﹢寮婚敐澶娢╅柕澶堝労娴煎倻绱撴担绛嬪殭闁哥喐娼欓锝夋偨閸涘﹤浜滈梺鍛婄☉椤剙危椤栫偞鈷掗柛灞捐壘閳ь兛绮欓獮鎾活敂閸℃﹩娼熷┑鐘绘涧濞诧附绂嶉妶澶嬬厸闁稿本绋戦婊呯磼閳ь剟鍩€?Duration 闂備浇顕уù鐑藉极閹间降鈧焦绻濋崶銊ョ樁闂佸憡娲﹂崹鎷岀箽?
     * </p>
     *
     * @param token Token 闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
     * @return Token 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸樺ジ鏁冮姀锛勭懝闁逞屽墮椤曪綁顢曢敃鈧柋鍥煟閺冨偆鐒鹃柣鎺撴倐濮婃椽宕崟顐ｆ闂佺粯鐗曢妶绋款嚕?
     * @see #resolveEffectiveTtlSeconds(String)
     */
    private Duration resolveCurrentTokenTtl(String token) {
        Long ttlSeconds = resolveEffectiveTtlSeconds(token);
        return ttlSeconds == null ? null : Duration.ofSeconds(ttlSeconds);
    }

    /**
     * 闂備浇宕甸崰鎰版偡鏉堚晛绶ゅΔ锝呭暞閸婇潧霉閻樺樊鍎忛悗姘槸椤法鎹勬笟顖氬壉濠电偛鎳庣换姗€寮?TTL 缂傚倸鍊风粈渚€藝閹殿喗鏆滄俊銈呮噹濮?
     * <p>
     * 闂備浇宕垫慨宕囨閵堝洦顫曢柡鍥ュ灪閸?Token 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸樺ジ鏁冮姀锛勭懝闁逞屽墮椤曪綁顢曢敃鈧柋鍥煥濠靛棙宸濋柣锝嗘そ濮婃椽宕崟顓炩叡闂佸摜濮甸崝鏇⑩€旈崘顔肩＜闁绘劗琛ラ幏濠氭煛婢跺﹦澧曞褏鏅划鍫ュ礋椤撶姷锛滄繝銏ｆ硾椤戝懘鏌屽鍛＜閺夊牄鍔岄崫娲煛娴ｅ摜肖濞寸媴绠撻幐濠冨緞鐏炴儳浠?tokenTimeout 闂?activeTimeout 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸樺ジ濡撮幒妤€绀堝ù锝夘棑閸撱劑妫呴銏″闁瑰憡鎸冲畷鎴﹀箻閸撲胶鐣堕柣鐐寸▓閳ь剙鍘栨竟?
     * </p>
     *
     * @param token Token 闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
     * @return 闂傚倷绀侀幖顐︽偋閸℃蛋鍥ㄥ閺夋垹鏌ч梺闈涱槴閺呮粓宕?TTL 缂傚倸鍊风粈渚€藝閹殿喗鏆滄俊銈呮噹濮规煡鏌ｉ弬鍨倯闁哄拋鍓熼幃姗€鎮欓棃娑楀濡炪們鍨圭粔鎾煡婢舵劕绠荤€规洖娉﹂妷褏纾奸悹鍥ㄥ絻閺嗭絿鈧鍠氶弫濠氬箖閵忋倖鍤掗柕鍫濇祩閸炲綊姊?null
     * @see #readTokenTimeout(String)
     * @see #readActiveTimeout(String)
     * @see #normalizeTimeout(long)
     */
    private Long resolveEffectiveTtlSeconds(String token) {
        if (!StringUtils.hasText(token)) {
            return 0L;
        }
        Long tokenTimeout = normalizeTimeout(readTokenTimeout(token));
        Long activeTimeout = normalizeTimeout(readActiveTimeout(token));
        if (tokenTimeout == null) {
            return activeTimeout;
        }
        if (activeTimeout == null) {
            return tokenTimeout;
        }
        return Math.min(tokenTimeout, activeTimeout);
    }

    /**
     * 闂備浇宕垫慨鏉懨洪埡鍜佹晪鐟滄垿濡?Token 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸ㄥ磭鍒掗弮鍫濈妞ゆ棁鍋愰惈鍕⒑閼测晩鐒鹃柣蹇斿哺钘濈憸鏂款潖?
     * <p>
     * 婵?Sa-Token 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴?Token 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸撴岸寮查崼鏇炵閹兼番鍨归鎾斥攽閻愬弶顥為柛鏃€鐗滅划濠氬箳濡や胶鍘告繛杈剧到閹芥粍鏅跺☉娆庣箚闁绘劖褰冮埀顒€鐏濋～蹇曠磼濡崵绉堕梺鍛婃寙閸滀焦袩缂傚倸鍊风粈渚€藝閹殿喗鏆滄い蹇撴椤洘鎱ㄥΟ鎸庣【鏉?
     * </p>
     *
     * @param token Token 闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
     * @return Token 闂備胶鍎甸崜婵堟暜閹烘绠犻煫鍥ㄦ惄濞撳鏌涘畝鈧崑娑氱尵瀹ュ鐓曟い鎰剁稻缁€鍐煥濞戞艾鏋涢柡灞诲妼閳藉螣閻撳簶鍙℃俊鐐€ら崑鍛淬€冩繝鍥ф瀬鐎广儱妫楃欢鐐寸箾閹寸偞鐨戞い锔诲櫍濮婃椽宕楅悡搴″Б闂佹悶鍨肩亸顏堝Φ閹版澘鐐婇柕濞垮劤缁愮偤鏌℃径濠勫⒈闁稿顦抽·鍌炴⒑閸濆嫷妲撮柡鍛矒瀵濡搁妷?0
     * @see cn.dev33.satoken.stp.StpUtil#getTokenTimeout(String)
     */
    private long readTokenTimeout(String token) {
        try {
            return StpUtil.getTokenTimeout(token);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    /**
     * 闂備浇宕垫慨鏉懨洪埡鍜佹晪鐟滄垿濡?Token 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸樺ジ鈥旈崘顏呭磯闁靛绠戠壕顖炴⒑閸涘﹤濮﹂柛妯诲劤閻☆參姊绘担鍛婃儓闁哥喐濞婂畷娲冀瑜滃〒濠氭煕瀹€鈧崑鐐哄煕?
     * <p>
     * 婵?Sa-Token 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴?Token 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸樺ジ鈥旈崘顏呭磯闁靛绠戠壕顖炴⒑閸涘﹤濮﹂柛妯诲劤閻☆參姊绘担鍛婃儓闁哥喐濞婂畷娲冀瑜滃〒濠氭煕瀹€鈧崑鐐哄煕閹烘绾ч柛顐ｇ箓閳锋梻绱掗埀顒勫礋椤撶姷锛滄繝銏ｆ硾椤戝懘鏌屽鍛＜閺夊牄鍔岄崫娲煛娴ｅ摜肖濞寸媴绠撻幐濠冨緞鐏炶浠忛梻鍌欑劍濡炲灝顭囬崸妤€绀夌€广儱顦弰銉︾箾閹寸偐妫ㄦ繛宸簻閸欏﹪鐓崶銊︾５闁衡偓椤掑嫭鈷戠紓浣癸供閻掗箖鏌涢埡鍌滃⒌闁糕斁鍋撳銈嗗灦鐎笛囁囨搴ｇ＜婵°倐鍋撻柟铏耿楠炲棝宕橀鑲╊槹濡炪倖鏌ㄦ晶浠嬫偩濞差亝鈷戦柛婵嗗婢с垽鏌涢悩鍐插妞ゎ亜鍟村畷鎺楁倷缁瀚芥繝鐢靛剳缂嶅棝宕楀鈧畷?
     * </p>
     *
     * @param token Token 闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
     * @return 濠电姷鏁搁崑娑橆嚕鐠轰綍锝夊箳閺冨倻鐓斿銈嗗笒閸婂綊骞嗛妷鈺傜厱婵炴垵宕弸鐔搞亜锜婚崶銊у幐闂佸憡鍔戦崝搴ㄥ春閿濆棔绻嗘い鎰╁灪閸嬨儵鏌℃担鍝バх€规洖銈告慨鈧柍鍝勫€告禒鎾⒒娴ｇ懓顕滅紒瀣灴绡撻柍褜鍓涚槐鎺撴綇閵娧呯杽闂佸湱鍋ㄩ崹褰掓偩閿熺姴绾ч悹鎭掑妽閸嬔冣攽閻橆喖鐏柛搴ゅ亹閸掓帡宕滆閸犲棝鏌ㄩ弮鍥撳ù婧垮€濋弻锟犲磼濞戞﹩鍤嬬紓浣插亾闁?NOT_VALUE_EXPIRE
     * @see cn.dev33.satoken.dao.SaTokenDao#NOT_VALUE_EXPIRE
     */
    private long readActiveTimeout(String token) {
        try {
            return StpUtil.getStpLogic().getTokenActiveTimeoutByToken(token);
        } catch (Exception ignored) {
            return SaTokenDao.NOT_VALUE_EXPIRE;
        }
    }

    /**
     * 闂傚倷绀侀幖顐ょ矓閺夋嚚娲Χ婢跺﹪妫峰銈嗙墱閸嬫稓绮堝畝鍕厸鐎广儱楠告晶鎵偓娑欑箞濮婃椽宕崟顐ｆ闂佺硶鏅滈悧鏇⑩€旈崘顔肩＜闁绘劗琛ラ幏?
     * <p>
     * 闂?Sa-Token 闂傚倷鐒﹂惇褰掑礉瀹€鈧埀顒佸嚬閸ㄥ磭鍒掗弮鍫濈妞ゆ棁鍋愰惈鍕⒑閼测晩鐒鹃柣蹇斿哺钘濈憸鏂款潖婵犳艾纾兼慨妯块哺閹插ジ姊虹紒妯煎缂傚秳绀侀悾宄扳枎閹惧啿鑰垮┑掳鍊愰崑鎾淬亜閺囧棗鍟犲Σ?Long 缂傚倸鍊风欢锟犲磻婢舵劦鏁嬬憸鏃堝箖濡ゅ懏鍊婚柤鎭掑劜濞呮牠鏌ｈ箛鏇炰户闁烩剝鏌ㄥ嵄闂侇剙绉甸悡鐔兼煟閹邦剙绾фい銉у仦娣囧﹤螖閳ь剟鎮ф繝鍕煓濠㈣泛顭崥瀣煕椤愮姴鐏柣锝嗘そ濮婃椽宕崟顓炩叡闂佸摜濮撮柊锝夌嵁韫囨柨顕遍柡澶嬪灩閻嫰姊虹紒姗嗙劸婵炲懏娲栭埢宥堢疀濞戞瑧鍘辨繝鐢靛Т鐎氱兘宕曢悩缁樼厓?
     * </p>
     *
     * @param seconds 闂傚倷绀侀幉锟犫€﹂崶顒€绐楅幖鎼厜缂嶆牠鏌熼柇锕€鏋熼柟顖樺劦閺屾稑鈽夐崡鐐寸亶濡炪伅浣告处閻撴盯鏌涢幇鈺佸闁糕晪绲炬穱濠囶敃閵忊€充淮闂佽桨绀佺粔鐟扮暦婵傜顫呴柍鍝勫€告禒鎾⒒?
     * @return 闂傚倷绀侀幖顐ょ矓閺夋嚚娲Χ婢跺﹪妫峰銈嗙墱閸嬫稓绮堝畝鍕厸鐎规搩鍠栫€氼剟鍩€椤掑倹鏆柡宀嬬秮婵℃悂濡烽妷顔荤磽缂傚倷绀侀ˇ浼村箰閸愬樊鍤曟い鏇楀亾闁硅櫕绮撳畷褰掝敃椤愶及婵嬫⒒閸屾艾鈧悂宕愭禒瀣妞ゅ繐妫欓～鏇熺節闂堟稒锛嶆繛灏栨櫊濮婂鍩€椤掑嫬绠涙い鏃傚亾閸犳帡姊洪崫鍕垫Т闁哄懏绋戠叅闁挎洍鍋撻柨鏇樺灲閸╁嫰宕橀埡鈧锕傛⒑閸濆嫬鈧湱鈧瑳鍛焼闁?null闂傚倷鐒︾€笛呯矙閹达箑瀚夋い鎺嗗亾閾荤偤鏌曢崼婵愭Ц闁哄绶氶弻娑㈩敃閿濆洤顩梺绋款儐閹瑰洭骞婂┑瀣妞ゆ梻铏庨崬褰掓⒒?0
     * @see cn.dev33.satoken.dao.SaTokenDao#NEVER_EXPIRE
     * @see cn.dev33.satoken.dao.SaTokenDao#NOT_VALUE_EXPIRE
     */
    private Long normalizeTimeout(long seconds) {
        if (seconds == SaTokenDao.NEVER_EXPIRE || seconds == SaTokenDao.NOT_VALUE_EXPIRE) {
            return null;
        }
        if (seconds <= 0) {
            return 0L;
        }
        return seconds;
    }

    /**
     * 濠电姷鏁搁崑鐐哄箰閹间礁绠犳俊顖濄€€閺嬪秹骞栧ǎ顒€濡奸柟鐟扮埣閺屾洘绻涜閸燁垶鎮块崶顒佲拺婵炶尙绮繛鍥煕閺傝法效鐎规洖鎼埥澶婎潩鏉堚晪绱抽梻浣筋潐閸庡磭澹曢銏犵?
     * <p>
     * 闂傚倷绀侀幖顐ょ矓閻戞枻缍栧璺猴功閺嗐倕霉閿濆牊顏犻柣顓熸崌閺岋綁寮崒銈囧姼闂?ID 闂傚倷绀侀幉锛勫垝瀹€鍕剶濠靛倻顭堥弸渚€鏌曢崼婵愭Ц缂?ID 闂傚倷绀侀幉锛勬暜閻愬绠鹃柍褜鍓氱换娑㈠川椤撱垹寮伴悗瑙勬礈閸忔ɑ淇婂宀婃Щ濠电偛鐗忔繛鈧柡宀嬬秮閿濈偤顢楅埀顒佷繆娴犲鐓曢柍鍝勫€婚妴鎺旂磼鏉堛劍宕屾鐐疵悾鐑藉炊閵婏箑鏋涢梻鍌欑劍鐎笛呯矙閹达箑瀚夋い鎺戝閸ㄥ倿鏌曟繛鐐珕闁搞倐鍋撻梻浣告啞娓氭宕板顓犵彾鐎光偓閸曨剛鍘?Token 闂傚倷娴囬～澶愵敊閺嶎厼纾婚柛娑卞弾閸ゆ洟鏌涜椤ㄥ懐绮堥崱娑欑厽婵☆垵鍋愮敮娑㈡煟濠靛牆鏋涢柟顔筋殜閹粙宕归銏＄暚缂傚倷闄嶉崝宥夋偂閿熺姴绠氶柛鎰靛枛缁€瀣亜閹捐泛孝妞わ腹鏅犻弻锝嗘償閵忊懇濮囬柤鍨﹀洦鐓?
     * </p>
     * <p>婵犵數濮伴崹鐓庘枖濞戞埃鍋撳鐓庢珝妤犵偛鍟换婵嬪炊閵娧冨Х闂備礁鎼€氼剛鎹㈤幒鏃€鏆滈柛鈩冪⊕閻?/p>
     * <ol>
     *   <li>婵犵數濮烽。浠嬪焵椤掆偓閸熷潡鍩€椤掆偓缂嶅﹪骞冨Ο璇茬窞闁归偊鍓涢宀勬⒑瑜版帒浜板ù婊呭仦濞煎寮Λ?tenantId 闂?userId闂傚倷鐒︾€笛呯矙閹达附鍎旈柣鎾崇瘍濞差亜閿ゆ俊銈傚亾缂佺姵濞婇弻鏇熷緞閸繂濮庨梺璇茬箚閺呯姴顫忔繝姘闁宠桨璁查崑鎾诲即閵忊晜鏅銈嗘婵倝宕曢悢鍏肩厸闁告劑鍔岄埀顒傜帛瀵板嫰宕熼娑樷偓?/li>
     *   <li>闂傚倷绀侀幉锟犳偄椤掑倻涓嶉柟杈剧畱閸ㄥ倹銇勯弽顐粶缂佺姰鍎查妵鍕箛閸撲礁鍩屾繛瀵稿帶闁帮綁寮婚悢鍝勬瀳婵☆垵妗ㄦ竟鏇㈡⒒娴ｅ憡鎯堥柣妤€妫濊棟妞ゆ牗鐔懓鎸庛亜韫囨挻顥犳俊顐灦閹綊骞侀幒鎴濐瀴闂佸搫妫楃换姗€寮婚悢鐓庣妞ゆ洖鎳忛ˉ鏍磽娴ｇ懓鏁惧┑鈥虫喘閸┾偓妞ゆ帊鑳堕埊鏇㈡煥閺囥劌浜炴俊鍙夊姇閳规垹鈧綆浜為ˇ閬嶆偡濠婂懎顣奸悽顖涘笧閺?Token 闂傚倷绀侀幉锟犳嚌閻愵剦娈介柟闂寸閸ㄥ倻鎲搁悧鍫濈瑲闁?/li>
     * </ol>
     *
     * @param tokenValue Token 闂?
     * @param tenantId 缂傚倸鍊风粈渚€藝闁秴绐楅柟鐗堟緲閺?ID
     * @param userId 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰?ID
     */
    private void clearOnlineUserCache(String tokenValue, Long tenantId, Long userId) {
        if (tenantId != null && userId != null) {
            String onlineKey = "fx:online:user:" + tenantId + ":" + userId;
            try {
                redis.delete(onlineKey);
                log.info("闂傚倷绀侀幉锛勬暜閻愬绠鹃柍褜鍓氱换娑㈠川椤撱垹寮伴悗瑙勬礈閸忔ɑ淇婂宀婃Щ濠电偛鐗忔繛鈧柡宀嬬秮閿濈偤顢楅埀顒佷繆娴犲鐓曢柍鍝勫€婚妴鎺旂磼鏉堛劍宕屾鐐疵悾鐑藉炊閵婏箑鏋? userId={}, tenantId={}", userId, tenantId);
            } catch (Exception e) {
                log.warn("闂傚倷绀侀幉锛勬暜閻愬绠鹃柍褜鍓氱换娑㈠川椤撱垹寮伴悗瑙勬礈閸忔ɑ淇婂宀婃Щ濠电偛鐗忔繛鈧柡宀嬬秮閿濈偤顢楅埀顒佷繆娴犲鐓曢柍鍝勫€婚妴鎺旂磼鏉堛劍宕屾鐐疵悾鐑藉炊閵婏箑鏋涙繝鐢靛О閸ㄦ椽宕曢懖鈺佸灊闁告挆鍕瀭? {}", e.getMessage());
            }
            return;
        }
        if (!StringUtils.hasText(tokenValue)) {
            return;
        }
        try {
            Set<String> keys = redis.keys("fx:online:user:*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            for (String key : keys) {
                String onlineJson = redis.opsForValue().get(key);
                if (!StringUtils.hasText(onlineJson)) {
                    continue;
                }
                try {
                    cn.hutool.json.JSONObject onlineObj = JSONUtil.parseObj(onlineJson);
                    if (tokenValue.equals(onlineObj.getStr("token"))) {
                        redis.delete(key);
                        log.info("闂傚倷绀佸﹢閬嶁€﹂崼銉ｂ偓浣搞€掑寤祅闂傚倷绀侀幉锛勬暜閻愬绠鹃柍褜鍓氱换娑㈠川椤撱垹寮伴悗瑙勬礈閸忔ɑ淇婂宀婃Щ濠电偛鐗忔繛鈧柡宀嬬秮閿濈偤顢楅埀顒佷繆娴犲鐓曢柍鍝勫€婚妴鎺旂磼鏉堛劍宕屾鐐疵悾鐑藉炊閵婏箑鏋? token={}", tokenValue);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.warn("闂傚倷娴囬～澶愵敊閺嶎厼纾婚柛娑卞弾閸ゆ洟鏌涜椤ㄥ懐绮堥崱娑欑厽婵☆垵鍋愮敮娑㈡煟濠靛牆鏋涢柡灞剧洴閺佸倿宕崟顐€烽梻浣规偠閸婃洟鏌婇敐澶婄畺濞寸姴顑呮儫闂佹寧娲嶉崑鎾绘煟閹惧瓨绀堢紒杈ㄦ崌瀹曟帒鈽夊▎蹇曨暡闂備礁顓介弶鍨瀴閻庡灚婢樼€氼厼顭囪箛娑辨晝闁? {}", e.getMessage());
        }
    }

    /**
     * 闂傚倷鐒﹀鍨焽閸ф绀夌€广儱顦弰銉︾箾閹存瑥鐏柛搴＄焸閹綊宕堕妸銉хシ濠?
     * <p>濠电姷鏁搁崑鐐哄箰閹间礁绠犳俊顖濄€€閺嬪秹骞栧ǎ顒€濡介柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂佺粯鎸诲ú鐔煎蓟閿濆牜妯佸銈嗘肠閸涱垳顦悗骞垮劚椤︿即宕戦幇鐗堢厾闁告縿鍎洪弳顖炴煕鐎ｎ偅灏扮紒妤冨枑缁绘繈宕熼褎啸濠德板€楁慨鐑藉磻閻樻祴鏋栨繛鎴欏灩缁犳牠鏌熼悙顒€澧繛闂村嵆閺屾洘寰勫☉姗嗘喘濡炪倕绻戦幃鍌炲蓟閿濆鍊烽柛娆忣樈濡偛鈹戦悩顐壕濡炪倕绻愬Λ娑樜涢鐐村仯闁诡厽甯掓俊浠嬫煛閸℃绠婚柡宀€鍠栧鍫曞箣濠靛泦鈺呮⒑濞茶浜滅紒澶婂閸掓帡宕奸妷锔藉劒闂侀潻瀵岄崢濂杆囬埡鍛厽閹兼番鍨婚埊鏇㈡嚕瑜旈弻?/p>
     *
     * @return 闂傚倷娴囬惃顐﹀幢閳轰焦顔勯梻浣规偠閸娿倝宕㈤崜褎顫曢柟鐑橆殔缁犲鎮楅悽鍛婃珳闁?
     * @see StpUtil#logout()
     */
    @Override
    public R<Boolean> logout() {
        try {
            // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔荤暗濞存粌缍婇弻鐔煎箚瑜嶉弳杈ㄣ亜閵堝懏鍤囬柡宀嬬秬椤﹁埖銇勯弴鍡楁噽缁€濠勨偓骞垮劚椤︿即宕戦妸鈺傜厪濠电姴绻掗悾閬嶆煟閹惧瓨顦盌闂傚倷鐒︾€笛呯矙閹达附鍋嬮柛娑卞枤缁犻箖鏌涢妷顔煎闁稿鍔戦弻鏇熺箾閸喖濮㈤梺鍝勬濡啴寮诲☉婊呯杸閻庯綆浜滈‖澶愭⒑閸濆嫮鐏遍柛鐘查叄閸┿垽寮崼婵嗗祮濡炪倕绻愬Λ娑⑺囬鐣岀闁瑰鍋炵亸顓犵磼婢跺﹦锛嶆俊鍙夊姈鐎靛ジ寮堕幋鐙€鍞存繝鐢靛仦閸ㄥ爼鈥﹂崶鈺侇棜濞寸姴顑嗛崑銊︺亜閺嶎偄浠﹀ù婧垮€楃槐?
            Object uid = StpUtil.getLoginIdDefaultNull();
            
            // 闂傚倷绀侀幉锛勬暜濡ゅ啯宕查柛宀€鍎戠紞鏍煙閻楀牊绶茬紒鈧畝鍕厸鐎广儱楠告禍婵嬫煛閸℃绠婚柡宀€鍠撻埀顒€鐏氶弫濂告偘閸洘鈷戦柛娑橈功閻﹪鏌ｉ鈧妶鎼佹晲閻愬搫围濠㈣泛锕﹂铏圭磽娴ｅ壊鍎嶉柍?
            Long userId = null;
            Long tenantId = null;
            String account = null;
            String tokenValue = null;
            
            // 闂備浇顕х换鎰崲閹寸姵宕查柛鈩冪⊕閸庡﹥銇勯弽顐沪闁搞倖姊婚埀顒傛嚀鐎氼厼顭垮Ο鐓庣筏缂佸搫绱漦en闂傚倷鑳堕…鍫ユ晝閿曞倸违閻庯綆鍓氶～鏇熺箾閸℃ɑ灏柣顓燁殕閵囧嫰寮介妸褏鐣甸梺鍛娚戝娆撳煡婢舵劕绠婚柛鎾茬劍椤斿紪ll闂?
            try {
                tokenValue = StpUtil.getTokenValue();
            } catch (Exception e) {
                log.debug("闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢姀鈺傜彾ken婵犵數濮伴崹娲磿閼测晛鍨濋柛鎾楀嫬鏋傞梺鎸庢礀閸婂綊寮查鍕€堕柣鎰煐椤ュ鎮规担鍦弨闁哄被鍊曢悾鐑藉炊閵婏妇褰ч梻浣芥〃闂勫秹宕愬┑瀣祦閻庯綆鍠楅悡銉╂倵閿濆骸浜滅紒鎲嬬畵濮婅櫣鎷惔鈩冿紞闁搞倖鐟х槐? {}", e.getMessage());
            }
            
            // 闂備浇顕х换鎰崲閹寸姵宕查柛鈩冪⊕閸庡﹥銇勯弽顐沪闁搞倖姊婚埀顒傛嚀鐎氼厼顭垮Ο鐓庣筏缂佸搫绌琒ession闂備浇顕уù鐑藉极閹间降鈧焦绻濋崶銊ョ樁?
            try {
                SaSession session = StpUtil.getSession(false);
                // 婵犵數鍋涢顓熸叏鐎垫瓕濮抽柤娴嬫櫆椤洟鏌熷▓鍨灍閻庢碍纰嶇换婵嬫濞戞瑥娈岄梺鍛婏供閸撶喖寮婚悢铏圭＜婵☆垰鎼～宀勬偡濠婂嫭绶叉繛宸弮楠炲啯绂掔€ｎ€晠鏌ㄩ弴妤€浜鹃梺缁樻尰濡炲瓕闂傚倷绀侀幉锛勫垝瀹€鍕剶濠靛倿娼荤紞鏍煏閸繍妲哥紒鐙呯悼缁辨帞鈧絻鍔夐崑?
                if (session != null) {
                    // 闂傚倷绀佸﹢杈╁垝椤栫偛绀夐柡鍤堕姹楅梺鎼炲労閸撴岸宕戦妸鈺傜厪濠电姴绻掗悾閬嶆煟閹惧瓨顦盌
                    Object uidObj = session.get("LOGIN_USER_ID");
                    if (uidObj instanceof Long) {
                        userId = (Long) uidObj;
                    } else if (uidObj instanceof Integer) {
                        userId = ((Integer) uidObj).longValue();
                    } else if (uidObj instanceof String) {
                        try { userId = Long.valueOf((String) uidObj); } catch (Exception ignored) { }
                    }
                    // 闂傚倷绀佸﹢杈╁垝椤栫偛绀夐柡鍤堕姹楅梺鎼炲劗閺呮繈鎯岄幘缁樼厽闁哄啫鍋嗛悞楣冩煟閹惧瓨顦盌
                    Object tidObj = session.get("LOGIN_TENANT_ID");
                    if (tidObj instanceof Long) {
                        tenantId = (Long) tidObj;
                    } else if (tidObj instanceof Integer) {
                        tenantId = ((Integer) tidObj).longValue();
                    } else if (tidObj instanceof String) {
                        try { tenantId = Long.valueOf((String) tidObj); } catch (Exception ignored) { }
                    }
                }
            } catch (Exception e) {
                log.debug("闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢姀鈺傛珨ssion婵犵數濮伴崹娲磿閼测晛鍨濋柛鎾楀嫬鏋傞梺鎸庢礀閸婂綊寮查鍕€堕柣鎰煐椤ュ鎮规担鍦弨闁哄被鍊濋幖褰掝敃閵忣澀鍠婄紓鍌欐祰妞村憡绔熼崱娑樼闁绘ɑ绁撮弨浠嬫煕閳╁喚娈旀繛鍫涘劦閺屸剝寰勯崱妯荤彅濠电偠顕滅粻鎴︽晝? {}", e.getMessage());
            }
            
            // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃珕閻庢艾顭烽弻銊╁即濡も偓娴滈箖鎮?
            account = uid == null ? null : String.valueOf(uid);

            // 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儯鍔岀徊缁樻叏閿濆拋妯€闁哄矉绻濆畷姗€濡歌閻撶喎鈹?闂傚倷绀侀幉锟犫€﹂崶顒€绐楅幖绮瑰煑閸濆嫷鐓ラ柛顐ゅ枑濞呮牠姊洪崜鎻掍簼缂佽瀚竟鏇㈡嚍閵夛箑寮垮┑鐘绘涧鐎氼剟宕濋悽鐢电＜閺夊牄鍔庣粻鏍磼鐎ｎ亶妲告い鎾炽偢瀹曘劑顢樿椤斿棝姊绘担鍛婂暈缂佽鍊荤划鏂跨暦閸ワ絽浜鹃梻鍫熺鐎氫即妫佹径濞掑綊鏁愰崼娑掑亾閳ь剟鏌＄仦璇测偓婵嬪蓟?
            if (StringUtils.hasText(tokenValue)) {
                try {
                    loginLogService.recordLogoutByToken(tokenValue, com.forgex.common.security.LogoutReason.MANUAL);
                } catch (Exception e) {
                    log.warn("闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚椤︿即宕曞畝鍕仯闁搞儯鍔岀徊缁樻叏閿濆拋妯€闁哄矉绻濆畷姗€鏁冮埀顒勫礉濮樿京纾肩紓浣股戦埛鎰偓鍨緲鐎氼厼顭囪箛娑辨晝闁? {}", e.getMessage());
                }
            }

            // 闂傚倷绀侀幉锛勬暜閻愬绠鹃柍褜鍓氱换娑㈠川椤撱垹寮伴悗瑙勬礈閸忔ɑ淇婂宀婃Щ濠电偛鐗忔繛鈧柡宀嬬秮閿濈偤顢楅埀顒佷繆娴犲鐓曢柍鍝勫€婚妴鎺旂磼鏉堛劍宕屾鐐疵悾鐑藉炊閵婏箑鏋?
            clearOnlineUserCache(tokenValue, tenantId, userId);
            
            // 闂備浇宕垫慨鎾敄閸涙潙鐤ù鍏兼綑閺嬩焦銇勯妷褜鍔員oken闂傚倷娴囬惃顐﹀幢閳轰焦顔勯梻浣规偠閸娿倝宕ｉ崘顔兼瀬鐎广儱顦柋鍥ㄧ節闂堟稒顥犻柕鍫枟缁绘稒娼忛崜褏蓱闁诲海鐟抽崘鑳偓鎸庛亜閹惧崬鐏柛搴＄焸閹綊宕堕鍕缂備礁顦悘婵嬪煡婢舵劕绫嶉柛灞剧閻庤櫣绱撴担璇℃當妞わ附澹嗛埀顒傛暩閸樠囷綖濠靛鏁囬柣鎰姈閹瑰洤顫忓ú顏勭閹艰揪绲鹃崵鍌滅磽?
            try {
                StpUtil.logout();
            } catch (Exception e) {
                log.debug("SaToken闂傚倷娴囬惃顐﹀幢閳轰焦顔勯梻浣规偠閸娿倝宕㈤挊澶樺殫闁告洦鍓氱紞鍥ㄣ亜閹扳晛鍓鹃柡鍥ュ灪閻撱儲绻涢幋鐑嗙劷濠⒀勬礈閹叉悂鎮ч崼銏犲绩闂佺娅曢〃濠囧箠閻愬搫唯闁挎洍鍋撶紒鎲嬬畵濮婅櫣鎷惔鈩冿紞闁搞倖鐟х槐鎾愁吋閸涱垍褔鏌? {}", e.getMessage());
            }
            
            log.info("闂傚倸鍊风欢锟犲磻閳ь剟鏌涚€ｎ偅宕岄柡灞剧洴楠炲鎮╅幓鎺戭瀱闂備焦鎮堕崝宥呯暆缁嬭法鏆? account={}", account);
            return R.ok(true);
        } catch (Exception e) {
            // 闂備浇宕垫慨鎶芥倿閿曗偓椤灝螣閼测晝顦悗骞垮劚濞诧妇鈧碍宀搁弻銊╁棘閸喒鎸冮梺娲诲幖闁帮綁寮婚敐澶娢╅柕澶堝労娴犺偐绱撴担浠嬪摵闁规悂绠栧顐︻敋閳ь剙顫忚ぐ鎺戠疀妞ゅ繐妫涢悾楣冩⒒?
            log.error("Logout process failed", e);
            return R.fail(CommonPrompt.LOGOUT_FAILED);
        }
    }
    
    /**
     * 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷銏℃澒闁稿鎹囬悰顕€宕归鍙ョ棯闂備胶纭堕弲婊呯矙閹捐泛鍨濋柍鍝勬媼閺佸秵绻濇繝鍌氼仼闁绘帗鎮傞弻锝嗘償閵堝孩缍堥梺鍛婃礃閵?
     * <p>
     * 婵犵數鍋涢顓熸叏閺夋埈娼╅柨鏇炲亞閺佸銇勯幒鍡椾壕闁轰礁鐗撻弻娑氫沪閹勬儧婵?X-Client-IP 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷顔煎闁哄拋鍓熼弻娑㈩敃閻樿尙浠鹃梺鍝勬婵妲愰幘璇茬闁宠桨鑳舵禒鎾⒑閸涘浼曢柛銉ｅ妿閸旂敻妫呴銏℃悙婵炲鍏橀幃娆愮節閸ャ劎鍙嗗┑鐐村灦閿曗晛危閸洘鐓?
     * 婵犵數濮烽。浠嬪焵椤掆偓閸熷潡鍩€椤掆偓缂嶅﹪骞冨Ο璇茬窞闁归偊鍘煎▓褔鎮楅悷鏉款仾婵犮垺顭囧濠囧垂椤曞懐鍞甸梺璇″灡婢瑰棛鑺遍崸妤佸仭婵炲棙鐟ч悾鐢告煛娴ｅ摜孝闁伙絾绻堝畷姗€顢旈崱鈺佹暭闂?"unknown"闂?
     * </p>
     * 
     * @return 闂備浇顕ф鍝ョ不瀹ュ鍨傛繛宸簻閺勩儲绻涢幋鐐寸殤闁告宀搁弻锝夊Χ閸℃劕浠╅梻鍌欑窔濞煎姊介崟顐唵婵☆垯璀﹂悞?
     */
    private String getClientIp() {
        try {
            org.springframework.web.context.request.RequestAttributes attrs = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest request = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
                String ip = request.getHeader("X-Client-IP");
                if (StringUtils.hasText(ip)) {
                    return ip;
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Get client IP failed", e);
        }
        return "unknown";
    }
    
    /**
     * 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢姀鈺傜９er-Agent
     * <p>
     * 婵犵數鍋涢顓熸叏閺夋埈娼╅柨鏇炲亞閺佸銇勯幒鍡椾壕闁轰礁鐗撻弻娑氫沪閹勬儧婵炲濮撮悧鎾诲蓟閵堝绠氱憸宥呂ｉ懡銈嗗枑闁哄瀵ч幖鎰版婢舵劗鍙撻柛銉ｅ妼閸ゎ剚绻涢崗鍏碱棃闁哄本鐩弫鎰板磼濞戞瑧褰簊er-Agent婵犵數鍎戠徊钘壝洪悩璇茬婵犻潧娲ら閬嶆煕濞戞瑦缍戞潻?
     * 婵犵數濮烽。浠嬪焵椤掆偓閸熷潡鍩€椤掆偓缂嶅﹪骞冨Ο璇茬窞闁归偊鍘煎▓褔鎮楅悷鏉款仾婵犮垺顭囧濠囧垂椤曞懐鍞甸梺璇″灡婢瑰棛鑺遍崸妤佸仭婵炲棙鐟ч悾鐢告煛娴ｅ摜孝闁伙絾绻堝畷姗€顢旈崱鈺佹暭闂?"unknown"闂?
     * </p>
     * 
     * @return User-Agent闂備浇顕х€涒晝绮欓幒妞尖偓鍐醇閵夘喗鏅炴繛杈剧到濠€閬嶅煝?
     */
    private String getUserAgent() {
        try {
            org.springframework.web.context.request.RequestAttributes attrs = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                jakarta.servlet.http.HttpServletRequest request = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
                String ua = request.getHeader("User-Agent");
                if (StringUtils.hasText(ua)) {
                    return ua;
                }
            }
        } catch (Exception e) {
            log.warn("Get User-Agent failed", e);
        }
        return "unknown";
    }

    // ==================== 闂傚倸鍊风欢锟犲储閹间礁纾婚柟鍓х帛閸嬶綁鏌涢妷顔煎⒒婵☆偅鍨块弻锝夊冀閵娧呯厐闂佸磭绮Λ鍐╀繆閹间礁唯闁靛闄勫?====================

    @Autowired
    private SysInviteCodeMapper inviteCodeMapper;

    @Autowired
    private SysInviteRegisterRecordMapper inviteRegisterRecordMapper;

    /**
     * 闂傚倸鍊风欢锟犲储閹间礁纾婚柟鍓х帛閸嬶綁鏌涢妷顔煎⒒婵☆偅鍨块弻锝夊冀閵娧呯厐闂佸磭绮Λ鍐╀繆閹间礁唯闁靛闄勫?
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public R<Boolean> register(com.forgex.auth.domain.param.RegisterParam param) {
        // 1. 闂傚倷鑳剁涵鍫曞疾濠靛纾块柕鍫濇嫅閼板灝霉閸忓吋缍戦柣顓燁殜閺屾稓浠﹂崜褉濮囧┑鐐茬墕閻栧ジ寮婚垾鎰佸悑闊洦娲滈惁鍫ユ⒑?
        if (param == null) {
            return R.fail(CommonPrompt.BAD_REQUEST);
        }
        String account = param.getAccount();
        String username = param.getUsername();
        String password = param.getPassword();
        String inviteCode = param.getInviteCode();

        if (!StringUtils.hasText(account)) {
            return R.fail(CommonPrompt.ACCOUNT_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(password)) {
            return R.fail(CommonPrompt.PASSWORD_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(inviteCode)) {
            return R.fail(CommonPrompt.INVITE_CODE_CANNOT_BE_EMPTY);
        }

        // 2. 婵犲痉鏉库偓妤佹叏閹绢喗鍎楀〒姘ｅ亾闁诡垯鐒﹀鍕箛椤撶偛澹撳┑鐐舵彧缁茶棄锕㈡潏銊︽珷闁炽儲绶?
        CaptchaConfig cfg = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        String mode = cfg.getMode();
        if ("image".equalsIgnoreCase(mode)) {
            if (!StringUtils.hasText(param.getCaptchaId()) || !StringUtils.hasText(param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY);
            }
            if (!captchaService.verifyImage(param.getCaptchaId(), param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_INCORRECT);
            }
        } else if ("slider".equalsIgnoreCase(mode)) {
            if (!StringUtils.hasText(param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY);
            }
            if (!captchaService.verifySlider(param.getCaptcha())) {
                return R.fail(CommonPrompt.VERIFICATION_CODE_INCORRECT);
            }
        }

        // 3. 闂傚倷绀侀幖顐ょ矙閸曨厽宕叉繝闈涱儐閸嬫ɑ绻涢崱妯诲鞍闁绘帊绮欓弻宥堫檨闁告挻鐩獮蹇涙偐閹颁焦效闁硅壈鎻徊浠嬫倶?
        com.forgex.auth.domain.entity.SysInviteCode inviteCodeEntity = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<com.forgex.auth.domain.entity.SysInviteCode>()
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getInviteCode, inviteCode)
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getDeleted, false));
        if (inviteCodeEntity == null) {
            return R.fail(CommonPrompt.INVITE_CODE_NOT_FOUND);
        }
        if (Boolean.FALSE.equals(inviteCodeEntity.getStatus())) {
            return R.fail(CommonPrompt.INVITE_CODE_DISABLED);
        }
        if (inviteCodeEntity.getExpireTime() != null && LocalDateTime.now().isAfter(inviteCodeEntity.getExpireTime())) {
            return R.fail(CommonPrompt.INVITE_CODE_EXPIRED);
        }
        if (inviteCodeEntity.getUsedCount() >= inviteCodeEntity.getMaxRegisterCount()) {
            return R.fail(CommonPrompt.INVITE_CODE_USED_UP);
        }

        // 4. 闂傚倷绀侀幖顐ょ矙閸曨厽宕叉繝闈涱儐閸嬫ɑ绻涢崱妯虹仸閻庢艾顭烽弻銊╁即濡も偓娴滈箖鎮峰鍕凡婵炲皷鈧剚鍤曞ù鍏兼儗閺佸秵鎱ㄥ鍡楀箹闁告挶鍔戦幃妤呮偡閻楀牆鏆堥悷婊勬緲閸熸挳骞冨ú顏勎╅柍杞扮劍瀹?
        Long existCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, account));
        if (existCount > 0) {
            return R.fail(CommonPrompt.ACCOUNT_ALREADY_EXISTS);
        }

        // 5. 闂備浇宕甸崰鎰版偡鏉堚晛绶ゅù鐘差儐閸庡﹪鏌熺€电袥闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掗幘璇叉瀬鐎广儱顦幑鍫曟煟濠ф儳浜? 婵犵數鍋熼ˉ鎰板磻閹邦厽鍙忛柛鎾楀嫷鍋ㄥ銈嗘尪閸ㄥ湱绮堟径鎰厽婵☆垰鐏濋惃鐑樹繆閸欏绀嬮柡?
        String rawPassword = password;
        CryptoTransportConfig cryptoCfg = configService.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
        if (cryptoCfg != null && StringUtils.hasText(cryptoCfg.getPrivateKey()) && "SM2".equalsIgnoreCase(cryptoCfg.getAlgorithm())) {
            try {
                SM2 sm2 = new SM2(cryptoCfg.getPrivateKey(), cryptoCfg.getPublicKey());
                String cipherFmt = cryptoCfg.getCipher();
                if ("BCD".equalsIgnoreCase(cipherFmt)) {
                    byte[] plain = sm2.decryptFromBcd(password, KeyType.PrivateKey);
                    rawPassword = new String(plain, StandardCharsets.UTF_8);
                } else {
                    rawPassword = sm2.decryptStr(password, KeyType.PrivateKey);
                }
            } catch (Exception ignored) {
                // 闂備浇宕甸崰鎰版偡鏉堚晛绶ゅù鐘差儐閸庡﹪鏌熺€电浠滅紒鍓佸仱瀵爼宕奸顫嚱閻炴碍绻堝娲箰鎼达絺妲堟繝闈涘€瑰鍦崲濞戙垹鐐婃い鎺嶇娴犳椽姊洪棃娑辩劸闁稿酣浜跺顒冾槻闁宠鍨块、娆戝枈閸楃偛澹堥梻浣风串闂勫嫰宕归崸妤€绠?
            }
        }

        // 6. 闂傚倷绀侀幉鈥愁潖缂佹ɑ鍙忛柟缁㈠枟閸庡﹪鏌熺€电袥闁稿鎹囧Λ鍐ㄢ槈濞嗘ɑ锟ラ梻浣虹帛閻楁鍒掓惔銊ョ劦妞ゆ帊鑳堕埊鏇㈡煥濮樿埖鐓?
        String hashedPassword = encryptPassword(rawPassword);

        // 7. 闂傚倷绀侀幉锛勬暜濡ゅ啰鐭欓柟瀵稿Х绾句粙鏌熼崜褏甯涢柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂?
        SysUser newUser = new SysUser();
        newUser.setAccount(account);
        newUser.setUsername(StringUtils.hasText(username) ? username : account);
        newUser.setPassword(hashedPassword);
        newUser.setPhone(param.getPhone());
        newUser.setEmail(param.getEmail());
        newUser.setStatus(true);
        // 濠电姷鏁搁崑娑⑺囬銏犵鐎广儱顦粈鍫澝归悡搴ｆ憼闁哄拋鍓氱换娑㈠幢濞嗘劗鏋€th 濠电姷顣藉Σ鍛村垂椤忓牆鐒垫い鎺戝暞閻濐亪鏌涚€ｃ劌鍔﹂柡?SysUser 濠电姷鏁搁崑娑欏緞閸ヮ剙绀堟繝闈涙４閼?departmentId/positionId 闂備浇顕х€涒晝绮欓幒妞尖偓鍐幢濞戣鲸鏅?
        // 闂傚倸鍊风欢锟犲窗閹捐绀夐柟瀛樻儕濞戙埄鏁嗛柛鏇ㄥ亝閵囨繈鎮楅崗澶婁壕闂佸憡鍓崟顐わ紳婵犵數鍋犻幓顏嗗緤閻ｅ瞼鐭撶憸鐗堝笒閻掑灚銇勯幒宥嗩樂濞存嚎鍨荤槐?SQL 闂傚倷鑳堕崕鐢稿疾濞戙垺鍋ら柕濞у嫭娈伴梺鍦檸閸犳牠宕￠幎鑺ュ€垫繛鎴烆伆閹达附鍋傞柍?
        userMapper.insert(newUser);

        Long userId = newUser.getId();

        // 8. 闂傚倷绀侀幖顐⒚洪妶澶嬪仱闁靛ň鏅涢拑鐔封攽閻樺弶澶勯柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂佺粯鎸诲ú鐔煎蓟閿濆惟闁靛鍎烘导鍐⒑缁嬪灝顒㈤柟绋垮暱椤繑绻濆顑┾晠鏌曟径娑氱暠妞ゅ浚鍨跺娲偂鎼绰ゅ煘婵犻潧鍊瑰鑽ゆ閻愬搫宸濋悗娑櫳戝▍鏍⒑閸撴彃浜栭柛搴櫍瀹曟垿骞橀幇浣瑰兊閻庤娲栧ú銊╂偩濞差亝鈷戦柛娑橆煬閻掍粙鏌涢悩铏磳鐎殿噮鍋婇獮鎾愁煶閸戔晠姊绘担鐟邦嚋缂佸鍨块幊鐔碱敍濮ｎ厼鎼…銊╁礋椤撴稒鐏冪紓鍌欒閳ь剝娅曠欢鐖恏濠电姷顣藉Σ鍛村垂椤忓牆鐒垫い鎺戝暞閻濐亪鏌涚€ｅ灚鐝抷sUser婵犵數鍋為崹鍫曞箰閸濄儳鐭撻柟缁㈠枛缁犳岸鏌涢銈呮瀾濞存嚎鍊栫换娑㈠幢濡や焦宕抽梺鍝勫閸婂潡骞冨Δ鈧埥澶娾枎濡崵鏆︽俊鐐€栭弻銊╂儗閸岀偛鏋?
        if (inviteCodeEntity.getDepartmentId() != null) {
            LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysUser::getId, userId);
            // 婵犵數鍋犻幓顏嗙礊閳ь剚绻涙径瀣鐎?setSql 闂傚倷鑳堕崕鐢稿疾濞戙垺鍋ら柕濞у嫭娈伴柣搴ㄦ涧閹测剝鍒婃總鍛婄厱闁圭偓顨呴幊蹇涙倵椤撱垺鐓熼幖娣灮閳洘銇勯妸銉︻棦妞?
            StringBuilder sqlParts = new StringBuilder();
            sqlParts.append("department_id = ").append(inviteCodeEntity.getDepartmentId());
            if (inviteCodeEntity.getPositionId() != null) {
                sqlParts.append(", position_id = ").append(inviteCodeEntity.getPositionId());
            }
            updateWrapper.setSql(sqlParts.toString());
            userMapper.update(null, updateWrapper);
        }

        // 9. 缂傚倸鍊搁崐鐑芥倿閿曞倸鍨傞柣銏犳啞閸嬧晛螖閿濆懎鏆為柛瀣ㄥ姂閺屾洘绻濊箛鏇犳殸闂?缂傚倸鍊风粈渚€藝闁秴绐楅柟鐗堟緲閺勩儲绻涢幋娆忕仼缂佲偓閸屾壕鍋撶憴鍕婵炲樊鍙冨畷?
        Long tenantId = inviteCodeEntity.getTenantId();
        if (tenantId != null) {
            SysUserTenant userTenant = new SysUserTenant();
            userTenant.setUserId(userId);
            userTenant.setTenantId(tenantId);
            userTenant.setIsDefault(true);
            userTenant.setPrefOrder(1);
            userTenant.setLastUsed(LocalDateTime.now());
            userTenantMapper.insert(userTenant);
        }

        // 10. 婵犵數濮幏鍐川椤撴繄鎹曞┑鐘愁問閸犳岸宕戦妶澶婅摕闁搞儺鍓欓悞鍨亜閹烘垵鈧湱鈧碍鑹鹃—鍐偓锝庝簼閹癸綁鏌涢妸銉ユ毐闂囧鏌ㄥ┑鍡欏缁绢厼鐖奸弻鈩冩媴闂堟稈鍋撻弴銏犵厺閹兼番鍔岄悞娲煕閹般劍娅嗘い?
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.forgex.auth.domain.entity.SysInviteCode> inviteUpdate =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        inviteUpdate.eq(com.forgex.auth.domain.entity.SysInviteCode::getId, inviteCodeEntity.getId())
                .setSql("used_count = used_count + 1");
        inviteCodeMapper.update(null, inviteUpdate);

        // 11. 闂傚倷绀侀幉锟犲礉閺嶎厽鍋￠柕澶嗘櫅閻鏌涢埄鍐姇闁绘帊绮欓弻宥堫檨闁告挻鐩獮蹇涙偐缂佹鍙嗛柣搴秵閸嬪懎鈻撳鈧娲川婵犲倸顫岄梺娲诲幒缁舵岸銆佸▎鎾崇倞妞ゅ繐鍊峰Ч?
        String clientIp = getClientIp();
        String region = ipLocationService.getLocationByIp(clientIp);

        com.forgex.auth.domain.entity.SysInviteRegisterRecord record = new com.forgex.auth.domain.entity.SysInviteRegisterRecord();
        record.setTenantId(tenantId);
        record.setInviteId(inviteCodeEntity.getId());
        record.setInviteCode(inviteCode);
        record.setUserId(userId);
        record.setAccount(account);
        record.setUsername(StringUtils.hasText(username) ? username : account);
        record.setDepartmentId(inviteCodeEntity.getDepartmentId());
        record.setPositionId(inviteCodeEntity.getPositionId());
        record.setRegisterIp(clientIp);
        record.setRegisterRegion(region);
        record.setRegisterTime(LocalDateTime.now());
        record.setStatus(1);
        inviteRegisterRecordMapper.insert(record);

        log.info("闂傚倸鍊风欢锟犲储閹间礁纾婚柟鍓х帛閸嬶綁鏌涢妷顔煎⒒婵☆偅鍨块弻锝夊冀閵娧呯厐闂佸磭绮Λ鍐╀繆閹间礁唯闁靛闄勫鎴︽⒒娴ｇ儤鍤€闁宦板姂瀹曟繈寮撮悢绋垮伎? account={}, inviteCode={}, tenantId={}", account, inviteCode, tenantId);

        return R.ok(CommonPrompt.REGISTER_SUCCESS, true);
    }

    /**
     * 闂傚倷绀侀幖顐ょ矙閸曨厽宕叉繝闈涱儐閸嬫ɑ绻涢崱妯诲鞍闁绘帊绮欓弻宥堫檨闁告挻鐩獮蹇涙偐閹颁焦效闁硅壈鎻徊浠嬫倶閿濆鈷戦柛婵嗗椤忔挳鏌涢妸銈呭祮妤犵偛妫欑粭鐔煎焵椤掆偓椤曪綁宕归銏㈢獮闁诲函缍嗛崑鍛存偟?
     */
    @Override
    public R<Boolean> validateInviteCode(String inviteCode) {
        if (!StringUtils.hasText(inviteCode)) {
            return R.fail(CommonPrompt.INVITE_CODE_CANNOT_BE_EMPTY);
        }
        com.forgex.auth.domain.entity.SysInviteCode entity = inviteCodeMapper.selectOne(
                new LambdaQueryWrapper<com.forgex.auth.domain.entity.SysInviteCode>()
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getInviteCode, inviteCode)
                        .eq(com.forgex.auth.domain.entity.SysInviteCode::getDeleted, false));
        if (entity == null) {
            return R.fail(CommonPrompt.INVITE_CODE_NOT_FOUND);
        }
        if (Boolean.FALSE.equals(entity.getStatus())) {
            return R.fail(CommonPrompt.INVITE_CODE_DISABLED);
        }
        if (entity.getExpireTime() != null && LocalDateTime.now().isAfter(entity.getExpireTime())) {
            return R.fail(CommonPrompt.INVITE_CODE_EXPIRED);
        }
        if (entity.getUsedCount() >= entity.getMaxRegisterCount()) {
            return R.fail(CommonPrompt.INVITE_CODE_USED_UP);
        }
        return R.ok(true);
    }
}
