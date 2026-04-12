# I18nInput 澶氳瑷€杈撳叆缁勪欢浣跨敤鏂囨。

## 馃摉 姒傝堪

`I18nInput` 鏄竴涓€氱敤鐨勫璇█閰嶇疆缁勪欢锛岀敤浜庣畝鍖栧墠绔璇█閰嶇疆鐨勭敤鎴蜂綋楠屻€傚畠鍙互鑷姩浠庡悗绔幏鍙栨敮鎸佺殑璇█鍒楄〃锛屽苟鎻愪緵鍙嬪ソ鐨勮〃鏍肩晫闈㈣鐢ㄦ埛濉啓鍚勮瑷€鐨勭炕璇戝唴瀹广€?

## 鉁?鐗规€?

- 鉁?**鑷姩鑾峰彇璇█鍒楄〃**锛氫粠鍚庣 API 鑷姩鍔犺浇绯荤粺鏀寔鐨勮瑷€
- 鉁?**涓夌鏄剧ず妯″紡**锛歴imple锛堢畝鍗曡緭鍏ユ锛夈€乼able锛堣〃鏍硷級銆乵odal锛堜粎寮圭獥锛?
- 鉁?**鍙屽悜缁戝畾**锛氭敮鎸?v-model 缁戝畾 JSON 瀛楃涓?
- 鉁?**鐢ㄦ埛鍙嬪ソ**锛氭棤闇€鎵嬪啓 JSON锛岄€氳繃琛ㄦ牸鍙鍖栭厤缃?
- 鉁?**鏁版嵁椹卞姩**锛氭柊澧炶瑷€鏃犻渶淇敼浠ｇ爜
- 鉁?**绫诲瀷瀹夊叏**锛氬畬鏁寸殑 TypeScript 绫诲瀷瀹氫箟

## 馃幆 瑙ｅ喅鐨勯棶棰?

### 浼犵粺鏂瑰紡鐨勭棝鐐?

```json
// 鐢ㄦ埛闇€瑕佹墜鍐欒繖鏍风殑 JSON锛屽鏄撳嚭閿?
{
  "zh-CN": "鐢ㄦ埛绠＄悊",
  "en-US": "User Management",
  "ja-JP": "銉︺兗銈躲兗绠＄悊",
  "ko-KR": "靷毄鞛?甏€毽?
}
```

### 鏂版柟寮忕殑浼樺娍

浣跨敤 `I18nInput` 缁勪欢锛岀敤鎴峰彧闇€鍦ㄨ〃鏍间腑濉啓锛?

| 璇█ | 缈昏瘧鍐呭 |
|------|---------|
| 馃嚚馃嚦 绠€浣撲腑鏂?| 鐢ㄦ埛绠＄悊 |
| 馃嚭馃嚫 English | User Management |
| 馃嚡馃嚨 鏃ユ湰瑾?| 銉︺兗銈躲兗绠＄悊 |
| 馃嚢馃嚪 頃滉淡鞏?| 靷毄鞛?甏€毽?|

缁勪欢浼氳嚜鍔ㄧ敓鎴?JSON 骞舵彁浜ょ粰鍚庣銆?

## 馃摝 瀹夎

缁勪欢宸插垱寤哄湪 `src/components/common/I18nInput.vue`锛屾棤闇€棰濆瀹夎銆?

## 馃殌 蹇€熷紑濮?

### 1. 鍩虹鐢ㄦ硶锛圫imple 妯″紡锛?

閫傚悎鍦ㄨ〃鍗曚腑浣跨敤锛屾彁渚涗竴涓緭鍏ユ鍜屽璇█閰嶇疆鎸夐挳锛?

```vue
<template>
  <a-form-item label="鑿滃崟鍚嶇О">
    <I18nInput 
      v-model="form.nameI18nJson" 
      mode="simple" 
      placeholder="璇疯緭鍏ヨ彍鍗曞悕绉?
    />
  </a-form-item>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import I18nInput from '@/components/common/I18nInput.vue'

const form = reactive({
  nameI18nJson: ''
})
</script>
```

### 2. 琛ㄦ牸妯″紡锛圱able 妯″紡锛?

鐩存帴鏄剧ず琛ㄦ牸锛岄€傚悎鍦ㄧ嫭绔嬬殑澶氳瑷€閰嶇疆椤甸潰锛?

```vue
<template>
  <I18nInput 
    v-model="form.dictValueI18nJson" 
    mode="table"
  />
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import I18nInput from '@/components/common/I18nInput.vue'

const form = reactive({
  dictValueI18nJson: ''
})
</script>
```

## 馃搵 API 鏂囨。

### Props

| 鍙傛暟 | 璇存槑 | 绫诲瀷 | 榛樿鍊?| 蹇呭～ |
|------|------|------|--------|------|
| modelValue | v-model 缁戝畾鐨勫€硷紙JSON 瀛楃涓诧級 | string | '' | 鍚?|
| mode | 鏄剧ず妯″紡 | 'simple' \| 'table' \| 'modal' | 'simple' | 鍚?|
| placeholder | 鍗犱綅绗︼紙浠?simple 妯″紡锛?| string | '璇疯緭鍏? | 鍚?|
| defaultLang | 榛樿璇█浠ｇ爜 | string | 'zh-CN' | 鍚?|

### Events

| 浜嬩欢鍚?| 璇存槑 | 鍥炶皟鍙傛暟 |
|--------|------|----------|
| update:modelValue | 鍊煎彉鍖栨椂瑙﹀彂 | (value: string) => void |

### 鏁版嵁鏍煎紡

缁勪欢杈撳嚭鐨?JSON 鏍煎紡锛?

```json
{
  "zh-CN": "绠€浣撲腑鏂囩炕璇?,
  "en-US": "English Translation",
  "ja-JP": "鏃ユ湰瑾炵炕瑷?,
  "ko-KR": "頃滉淡鞏?氩堨棴"
}
```

## 馃帹 瀹為檯搴旂敤鍦烘櫙

### 鍦烘櫙1锛氳彍鍗曠鐞?

鍦ㄨ彍鍗曠鐞嗙殑鏂板/缂栬緫琛ㄥ崟涓娇鐢細

```vue
<template>
  <a-form :model="formData" :rules="rules">
    <!-- 鑿滃崟鍚嶇О - 浣跨敤澶氳瑷€杈撳叆 -->
    <a-form-item label="鑿滃崟鍚嶇О" name="nameI18nJson">
      <I18nInput 
        v-model="formData.nameI18nJson" 
        mode="simple" 
        placeholder="璇疯緭鍏ヨ彍鍗曞悕绉?
      />
    </a-form-item>
    
    <!-- 鍏朵粬瀛楁 -->
    <a-form-item label="鑿滃崟璺緞" name="path">
      <a-input v-model:value="formData.path" />
    </a-form-item>
    
    <a-form-item>
      <a-button type="primary" @click="handleSubmit">鎻愪氦</a-button>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import I18nInput from '@/components/common/I18nInput.vue'
import { createMenu } from '@/api/system/menu'

const formData = reactive({
  nameI18nJson: '',
  path: '',
  icon: '',
  orderNum: 0
})

const handleSubmit = async () => {
  try {
    // 鎻愪氦鏃讹紝nameI18nJson 宸茬粡鏄?JSON 瀛楃涓诧紝鐩存帴浼犵粰鍚庣
    await createMenu(formData)
    message.success('鍒涘缓鎴愬姛')
  } catch (error) {
    message.error('鍒涘缓澶辫触')
  }
}
</script>
```

### 鍦烘櫙2锛氬瓧鍏哥鐞?

鍦ㄥ瓧鍏搁」鐨勭紪杈戜腑浣跨敤锛?

```vue
<template>
  <a-modal v-model:open="visible" title="缂栬緫瀛楀吀椤? @ok="handleSubmit">
    <a-form :model="form">
      <a-form-item label="瀛楀吀鍚嶇О">
        <a-input v-model:value="form.dictName" />
      </a-form-item>
      
      <!-- 瀛楀吀鍊煎璇█閰嶇疆 -->
      <a-form-item label="瀛楀吀鍊肩炕璇?>
        <I18nInput 
          v-model="form.dictValueI18nJson" 
          mode="table"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import I18nInput from '@/components/common/I18nInput.vue'
import { updateDict } from '@/api/system/dict'

const visible = ref(false)
const form = reactive({
  id: null,
  dictName: '',
  dictValueI18nJson: ''
})

const handleSubmit = async () => {
  try {
    await updateDict(form)
    message.success('鏇存柊鎴愬姛')
    visible.value = false
  } catch (error) {
    message.error('鏇存柊澶辫触')
  }
}
</script>
```

### 鍦烘櫙3锛氳鑹茬鐞?

```vue
<template>
  <a-form-item label="瑙掕壊鍚嶇О">
    <I18nInput 
      v-model="role表单.nameI18nJson" 
      mode="simple" 
      placeholder="璇疯緭鍏ヨ鑹插悕绉?
    />
  </a-form-item>
</template>
```

### 鍦烘櫙4锛氶儴闂ㄧ鐞?

```vue
<template>
  <a-form-item label="閮ㄩ棬鍚嶇О">
    <I18nInput 
      v-model="dept表单.nameI18nJson" 
      mode="simple" 
      placeholder="璇疯緭鍏ラ儴闂ㄥ悕绉?
    />
  </a-form-item>
</template>
```

## 馃敡 鍚庣闆嗘垚

### 1. 瀹炰綋绫诲瓧娈?

纭繚瀹炰綋绫绘湁瀵瑰簲鐨?JSON 瀛楁锛?

```java
@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    /** 鑿滃崟鍚嶇О */
    private String name;
    
    /** 鑿滃崟鍚嶇О澶氳瑷€JSON */
    private String nameI18nJson;
    
    // 鍏朵粬瀛楁...
}
```

### 2. 鏁版嵁搴撳瓧娈?

```sql
ALTER TABLE sys_menu ADD COLUMN name_i18n_json VARCHAR(1000) COMMENT '鑿滃崟鍚嶇О澶氳瑷€JSON';
ALTER TABLE sys_dict ADD COLUMN dict_value_i18n_json VARCHAR(1000) COMMENT '瀛楀吀鍊煎璇█JSON';
```

### 3. 鍓嶇鏄剧ず閫昏緫

鍦ㄥ垪琛ㄩ〉闈㈡樉绀烘椂锛屾牴鎹綋鍓嶈瑷€浠?JSON 涓彇鍊硷細

```typescript
// 宸ュ叿鍑芥暟锛氫粠 I18n JSON 涓幏鍙栧綋鍓嶈瑷€鐨勫€?
export function getI18nValue(i18nJson: string, 降级方案: string = ''): string {
  if (!i18nJson) return 降级方案
  
  try {
    const obj = JSON.parse(i18nJson)
    const currentLang = localStorage.getItem('fx-locale') || 'zh-CN'
    return obj[currentLang] || obj['zh-CN'] || 降级方案
  } catch (error) {
    return 降级方案
  }
}

// 浣跨敤绀轰緥
const menuName = getI18nValue(menu.nameI18nJson, menu.name)
```

## 馃幆 闇€瑕侀厤缃璇█鐨勫湴鏂?

鏍规嵁浣犵殑椤圭洰鍒嗘瀽锛屼互涓嬪湴鏂归渶瑕侀厤缃璇█锛?

### 1. 鉁?鑿滃崟绠＄悊锛圫ysMenu锛?
- **瀛楁**锛歚name`锛堣彍鍗曞悕绉帮級
- **瀛樺偍瀛楁**锛歚nameI18nJson`
- **浣跨敤浣嶇疆**锛歚/views/system/menu/index.vue`

### 2. 鉁?瀛楀吀绠＄悊锛圫ysDict锛?
- **瀛楁**锛歚dictValue`锛堝瓧鍏稿€兼樉绀哄悕绉帮級
- **瀛樺偍瀛楁**锛歚dictValueI18nJson`
- **浣跨敤浣嶇疆**锛歚/views/system/dict/index.vue`

### 3. 馃攧 瑙掕壊绠＄悊锛圫ysRole锛?
- **瀛楁**锛歚roleName`锛堣鑹插悕绉帮級
- **寤鸿娣诲姞**锛歚roleNameI18nJson`

### 4. 馃攧 閮ㄩ棬绠＄悊锛圫ysDepartment锛?
- **瀛楁**锛歚deptName`锛堥儴闂ㄥ悕绉帮級
- **寤鸿娣诲姞**锛歚deptNameI18nJson`

### 5. 馃攧 宀椾綅绠＄悊锛圫ysPosition锛?
- **瀛楁**锛歚positionName`锛堝矖浣嶅悕绉帮級
- **寤鸿娣诲姞**锛歚positionNameI18nJson`

### 6. 馃攧 妯″潡绠＄悊锛圫ysModule锛?
- **瀛楁**锛歚moduleName`锛堟ā鍧楀悕绉帮級
- **寤鸿娣诲姞**锛歚moduleNameI18nJson`

## 馃摑 杩佺Щ鎸囧崡

### 姝ラ1锛氭洿鏂版暟鎹簱

```sql
-- 鑿滃崟琛紙宸叉湁锛?
-- ALTER TABLE sys_menu ADD COLUMN name_i18n_json VARCHAR(1000);

-- 瀛楀吀琛紙宸叉湁锛?
-- ALTER TABLE sys_dict ADD COLUMN dict_value_i18n_json VARCHAR(1000);

-- 瑙掕壊琛?
ALTER TABLE sys_role ADD COLUMN role_name_i18n_json VARCHAR(1000) COMMENT '瑙掕壊鍚嶇О澶氳瑷€JSON';

-- 閮ㄩ棬琛?
ALTER TABLE sys_department ADD COLUMN dept_name_i18n_json VARCHAR(1000) COMMENT '閮ㄩ棬鍚嶇О澶氳瑷€JSON';

-- 宀椾綅琛?
ALTER TABLE sys_position ADD COLUMN position_name_i18n_json VARCHAR(1000) COMMENT '宀椾綅鍚嶇О澶氳瑷€JSON';

-- 妯″潡琛?
ALTER TABLE sys_module ADD COLUMN module_name_i18n_json VARCHAR(1000) COMMENT '妯″潡鍚嶇О澶氳瑷€JSON';
```

### 姝ラ2锛氭洿鏂板疄浣撶被

鍦ㄥ搴旂殑瀹炰綋绫讳腑娣诲姞瀛楁锛?

```java
/** 瑙掕壊鍚嶇О澶氳瑷€JSON */
private String roleNameI18nJson;
```

### 姝ラ3锛氭洿鏂板墠绔〃鍗?

灏嗗師鏉ョ殑杈撳叆妗嗘浛鎹负 `I18nInput` 缁勪欢锛?

```vue
<!-- 鍘熸潵 -->
<a-input v-model:value="form.roleName" placeholder="璇疯緭鍏ヨ鑹插悕绉? />

<!-- 鐜板湪 -->
<I18nInput v-model="form.roleNameI18nJson" mode="simple" placeholder="璇疯緭鍏ヨ鑹插悕绉? />
```

### 姝ラ4锛氭洿鏂版樉绀洪€昏緫

鍦ㄥ垪琛ㄩ〉闈娇鐢ㄥ伐鍏峰嚱鏁版樉绀猴細

```vue
<template>
  <a-table :columns="columns" :data-source="dataSource">
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'roleName'">
        {{ getI18nValue(record.roleNameI18nJson, record.roleName) }}
      </template>
    </template>
  </a-table>
</template>
```

## 馃悰 甯歌闂

### Q1: 濡備綍璁剧疆鍒濆鍊硷紵

```vue
<script setup>
const form = reactive({
  // 鐩存帴璁剧疆 JSON 瀛楃涓?
  nameI18nJson: '{"zh-CN":"鐢ㄦ埛绠＄悊","en-US":"User Management"}'
})
</script>
```

### Q2: 濡備綍楠岃瘉蹇呭～锛?

```vue
<script setup>
const rules = {
  nameI18nJson: [
    { 
      required: true, 
      message: '璇烽厤缃璇█', 
      trigger: 'change',
      validator: (rule, value) => {
        if (!value || value === '{}' || value === '') {
          return Promise.reject('璇疯嚦灏戦厤缃竴绉嶈瑷€')
        }
        return Promise.resolve()
      }
    }
  ]
}
</script>
```

### Q3: 濡備綍鑾峰彇褰撳墠璇█鐨勫€硷紵

```typescript
import { getI18nValue } from '@/utils/i18n'

const displayName = getI18nValue(menu.nameI18nJson, menu.name)
```

## 馃帀 鎬荤粨

浣跨敤 `I18nInput` 缁勪欢鍚庯細

1. 鉁?**鐢ㄦ埛浣撻獙鎻愬崌**锛氫笉闇€瑕佹墜鍐?JSON锛岄€氳繃琛ㄦ牸鍙鍖栭厤缃?
2. 鉁?**寮€鍙戞晥鐜囨彁鍗?*锛氱粺涓€鐨勭粍浠讹紝鍑忓皯閲嶅浠ｇ爜
3. 鉁?**缁存姢鎴愭湰闄嶄綆**锛氭柊澧炶瑷€鍙渶鍦ㄥ悗绔厤缃紝鍓嶇鑷姩閫傞厤
4. 鉁?**鏁版嵁涓€鑷存€?*锛氱粺涓€鐨勬暟鎹牸寮忥紝鍑忓皯閿欒

## 馃摎 鐩稿叧鏂囨。

- [Vue I18n 瀹樻柟鏂囨。](https://vue-i18n.intlify.dev/)
- [Ant Design Vue 琛ㄥ崟缁勪欢](https://antdv.com/components/form-cn)
- [椤圭洰澶氳瑷€閰嶇疆鏂囨。](../locales/README.md)

