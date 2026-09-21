package com.forgex.mobile.feature.home

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.forgex.mobile.core.component.FxAutoCompleteField
import com.forgex.mobile.core.component.FxBarChart
import com.forgex.mobile.core.component.FxBottomSheet
import com.forgex.mobile.core.component.FxBottomSheetAction
import com.forgex.mobile.core.component.FxBottomSheetActionList
import com.forgex.mobile.core.component.FxChartPoint
import com.forgex.mobile.core.component.FxChartSlice
import com.forgex.mobile.core.component.FxConfirmDialog
import com.forgex.mobile.core.component.FxDatePickerField
import com.forgex.mobile.core.component.FxDateRangePickerField
import com.forgex.mobile.core.component.FxDateTimePickerField
import com.forgex.mobile.core.component.FxDictOption
import com.forgex.mobile.core.component.FxDictSelect
import com.forgex.mobile.core.component.FxDictTag
import com.forgex.mobile.core.component.FxDonutChart
import com.forgex.mobile.core.component.FxEmptyView
import com.forgex.mobile.core.component.FxErrorView
import com.forgex.mobile.core.component.FxFormItem
import com.forgex.mobile.core.component.FxFormReadonlyBlock
import com.forgex.mobile.core.component.FxFormSection
import com.forgex.mobile.core.component.FxLineChart
import com.forgex.mobile.core.component.FxListItem
import com.forgex.mobile.core.component.FxLoadingDialog
import com.forgex.mobile.core.component.FxLoadingView
import com.forgex.mobile.core.component.FxNumberField
import com.forgex.mobile.core.component.FxPagedList
import com.forgex.mobile.core.component.FxPageScaffold
import com.forgex.mobile.core.component.FxPasswordField
import com.forgex.mobile.core.component.FxPieChart
import com.forgex.mobile.core.component.FxSearchField
import com.forgex.mobile.core.component.FxSelectField
import com.forgex.mobile.core.component.FxStatisticCard
import com.forgex.mobile.core.component.FxStatusBadge
import com.forgex.mobile.core.component.FxTable
import com.forgex.mobile.core.component.FxTextField
import com.forgex.mobile.core.component.FxToast
import com.forgex.mobile.core.component.scanner.FxNfcStatusHint
import com.forgex.mobile.core.component.scanner.FxScanActionBar
import com.forgex.mobile.core.component.scanner.FxScanActionButton
import com.forgex.mobile.core.component.scanner.FxScanInputBox
import com.forgex.mobile.core.component.scanner.FxScanResultDialog
import com.forgex.mobile.core.device.FxCameraCapture
import com.forgex.mobile.core.device.FxCameraScanManager
import com.forgex.mobile.core.device.FxNfcScanManager
import com.forgex.mobile.core.device.FxScanFeedback
import com.forgex.mobile.core.device.FxScannerManager
import com.forgex.mobile.core.model.FxScanResult
import com.forgex.mobile.core.model.FxTableAlign
import com.forgex.mobile.core.model.FxTableColumn
import com.forgex.mobile.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest

/** 组件验收页路由。 */
const val COMPONENT_SHOWCASE_ROUTE = "component/showcase"

/**
 * 组件验收页：集中展示共享组件库的真实行为，供开发自检与验收对照。
 */
@Composable
fun ComponentShowcaseScreen(
    modifier: Modifier = Modifier,
    viewModel: ComponentShowcaseViewModel = hiltViewModel(),
    scanBridgeViewModel: ShowcaseScanBridgeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagingState by viewModel.pagingState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val context = LocalContext.current

    var scanValue by remember { mutableStateOf("") }
    var latestScanResult by remember { mutableStateOf<FxScanResult?>(null) }
    var lastScanResultForDialog by remember { mutableStateOf<FxScanResult?>(null) }
    var cameraScanNotice by remember { mutableStateOf<Int?>(null) }
    var demoToast by remember { mutableStateOf<String?>(null) }

    if (demoToast != null) {
        FxToast(
            message = demoToast!!,
            onShown = { demoToast = null }
        )
    }

    val startCameraScan = rememberShowcaseCameraScan(
        cameraScanManager = scanBridgeViewModel.cameraScanManager,
        scanFeedback = scanBridgeViewModel.scanFeedback,
        onDecoded = { result -> scanBridgeViewModel.scannerManager.submit(result) },
        onNotify = { messageRes -> cameraScanNotice = messageRes }
    )
    cameraScanNotice?.let { messageRes ->
        FxToast(
            message = context.getString(messageRes),
            onShown = { cameraScanNotice = null }
        )
    }

    LaunchedEffect(scanBridgeViewModel) {
        scanBridgeViewModel.scannerManager.results.collectLatest { result ->
            latestScanResult = result
            lastScanResultForDialog = result
            scanBridgeViewModel.scannerManager.clearCachedResult()
        }
    }

    val uploadPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let(viewModel::uploadImage)
    }

    FxPageScaffold(title = stringResource(R.string.component_showcase_title)) { _ ->
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 16.dp
            )
        ) {
            item {
                Text(
                    text = stringResource(R.string.component_showcase_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---------- 字典标签与选择 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_dict)) {
                    if (uiState.dictFallback) {
                        Text(
                            text = stringResource(R.string.component_showcase_dict_fallback),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (uiState.dictLoading) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        var dictValue by remember { mutableStateOf("PENDING") }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.dictOptions.take(4).forEach { option ->
                                FxDictTag(
                                    value = option.value,
                                    options = uiState.dictOptions,
                                    fallbackColor = MaterialTheme.colorScheme.primary
                                )
                            }
                            FxDictTag(
                                value = "NOT_IN_DICT",
                                options = uiState.dictOptions,
                                fallbackLabel = "未知"
                            )
                        }
                        FxDictSelect(
                            label = "单据状态",
                            value = dictValue,
                            options = uiState.dictOptions,
                            onValueChange = { dictValue = it }
                        )
                    }
                }
            }

            // ---------- 表单输入 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_form)) {
                    var textValue by remember { mutableStateOf("") }
                    var passwordValue by remember { mutableStateOf("") }
                    var searchValue by remember { mutableStateOf("") }
                    var numberValue by remember { mutableStateOf("12.5") }

                    FxTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        label = "普通文本",
                        required = true,
                        supportingText = "必填、可清空输入演示",
                        errorText = textValue.takeIf { it.isBlank() }?.let { "内容不能为空" },
                        clearable = true
                    )
                    FxTextField(
                        value = "只读内容不可编辑",
                        onValueChange = {},
                        label = "禁用文本",
                        enabled = false
                    )
                    FxNumberField(
                        value = numberValue,
                        onValueChange = { numberValue = it },
                        label = "数量（支持小数）",
                        allowDecimal = true,
                        supportingText = "当前值：$numberValue"
                    )
                    FxPasswordField(
                        value = passwordValue,
                        onValueChange = { passwordValue = it },
                        label = "密码"
                    )
                    FxSearchField(
                        value = searchValue,
                        onValueChange = { searchValue = it },
                        label = "物料搜索",
                        onSearch = { demoToast = "触发搜索：$searchValue" }
                    )
                    FxSelectField(
                        label = "普通选择字段",
                        value = "选项 A",
                        supportingText = "点击触发回调演示",
                        onClick = { demoToast = "FxSelectField 被点击" }
                    )
                    FxFormItem(label = "表单项容器", description = "FxFormItem 包裹自定义内容") {
                        Text(
                            text = "自定义内容区域",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FxFormReadonlyBlock(label = "只读字段", value = "RO-20260921-0001")

                    val allMaterials = remember {
                        listOf("铝合金外壳", "不锈钢支架", "尼龙齿轮", "铜螺母 M6", "橡胶垫圈", "深沟球轴承 6204")
                    }
                    var materialValue by remember { mutableStateOf("") }
                    val filteredMaterials = remember(materialValue) {
                        if (materialValue.isBlank()) {
                            emptyList()
                        } else {
                            allMaterials.filter { it.contains(materialValue) }
                        }
                    }
                    FxAutoCompleteField(
                        value = materialValue,
                        onValueChange = { materialValue = it },
                        label = "物料自动补全",
                        suggestions = filteredMaterials,
                        onSuggestionSelected = { selected ->
                            materialValue = selected
                            demoToast = "选中：$selected"
                        },
                        supportingText = "输入关键字过滤本地建议项"
                    )
                }
            }

            // ---------- 日期时间选择 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_datetime)) {
                    var dateValue by remember { mutableStateOf("2026-09-21") }
                    var rangeValue by remember { mutableStateOf("") }
                    var dateTimeValue by remember { mutableStateOf("") }
                    FxDatePickerField(
                        value = dateValue,
                        onValueChange = { dateValue = it },
                        label = "日期"
                    )
                    FxDateRangePickerField(
                        value = rangeValue,
                        onValueChange = { rangeValue = it },
                        label = "日期范围"
                    )
                    FxDateTimePickerField(
                        value = dateTimeValue,
                        onValueChange = { dateTimeValue = it },
                        label = "日期时间"
                    )
                }
            }

            // ---------- 图表 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_chart)) {
                    FxStatisticCard(title = "本月产量", value = "12,480", subtitle = "较上月 +8.2%")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FxStatisticCard(
                            title = "合格率",
                            value = "99.2%",
                            modifier = Modifier.weight(1f)
                        )
                        FxStatisticCard(
                            title = "待检批次",
                            value = "17",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FxLineChart(
                        points = listOf(
                            FxChartPoint("周一", 120f),
                            FxChartPoint("周二", 150f),
                            FxChartPoint("周三", 135f),
                            FxChartPoint("周四", 180f),
                            FxChartPoint("周五", 165f),
                            FxChartPoint("周六", 140f),
                            FxChartPoint("周日", 190f)
                        )
                    )
                    FxBarChart(
                        points = listOf(
                            FxChartPoint("注塑", 320f),
                            FxChartPoint("冲压", 280f),
                            FxChartPoint("装配", 460f),
                            FxChartPoint("包装", 210f)
                        )
                    )
                    val defectSlices = listOf(
                        FxChartSlice("尺寸不良", 42f),
                        FxChartSlice("外观划伤", 28f),
                        FxChartSlice("装配错位", 18f),
                        FxChartSlice("其他", 12f)
                    )
                    FxPieChart(slices = defectSlices)
                    FxDonutChart(
                        slices = defectSlices,
                        centerLabel = "100"
                    )
                }
            }

            // ---------- 状态徽标 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_badge)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FxStatusBadge(text = "运行中")
                        FxStatusBadge(
                            text = "已停机",
                            color = MaterialTheme.colorScheme.error
                        )
                        FxStatusBadge(
                            text = "待检",
                            color = androidx.compose.ui.graphics.Color(0xFFF59E0B)
                        )
                        FxStatusBadge(
                            text = "离线",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // ---------- 状态视图 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_status)) {
                    var statusMode by remember { mutableStateOf("empty") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = statusMode == "loading",
                            onClick = { statusMode = "loading" },
                            label = { Text("加载中") }
                        )
                        FilterChip(
                            selected = statusMode == "error",
                            onClick = { statusMode = "error" },
                            label = { Text("错误") }
                        )
                        FilterChip(
                            selected = statusMode == "empty",
                            onClick = { statusMode = "empty" },
                            label = { Text("空态") }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(top = 8.dp)
                    ) {
                        when (statusMode) {
                            "loading" -> FxLoadingView()
                            "error" -> FxErrorView(
                                message = "模拟加载失败",
                                onRetry = {
                                    demoToast = "点击了重试"
                                    statusMode = "loading"
                                }
                            )
                            else -> FxEmptyView(message = "暂无数据")
                        }
                    }
                }
            }

            // ---------- 反馈与弹层 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_feedback)) {
                    var confirmVisible by remember { mutableStateOf(false) }
                    var loadingDialogVisible by remember { mutableStateOf(false) }
                    var sheetVisible by remember { mutableStateOf(false) }
                    var scanDialogVisible by remember { mutableStateOf(false) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FxScanActionButton(text = "确认弹窗") { confirmVisible = true }
                        FxScanActionButton(text = "加载弹窗") { loadingDialogVisible = true }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FxScanActionButton(text = "底部弹层") { sheetVisible = true }
                        FxScanActionButton(text = "扫码结果弹窗") {
                            if (lastScanResultForDialog != null) {
                                scanDialogVisible = true
                            } else {
                                demoToast = "还没有扫描结果，先扫码或拍码试试"
                            }
                        }
                    }

                    if (confirmVisible) {
                        FxConfirmDialog(
                            title = "删除确认",
                            message = "确认删除该条示例数据吗？此操作仅用于组件演示。",
                            onConfirm = {
                                confirmVisible = false
                                demoToast = "已确认删除"
                            },
                            onDismiss = { confirmVisible = false },
                            destructive = true
                        )
                    }
                    if (loadingDialogVisible) {
                        FxLoadingDialog()
                        LaunchedEffect(loadingDialogVisible) {
                            kotlinx.coroutines.delay(1500)
                            loadingDialogVisible = false
                        }
                    }
                    if (scanDialogVisible && lastScanResultForDialog != null) {
                        FxScanResultDialog(
                            result = lastScanResultForDialog!!,
                            onDismiss = { scanDialogVisible = false }
                        )
                    }
                    FxBottomSheet(
                        visible = sheetVisible,
                        onDismiss = { sheetVisible = false },
                        title = "更多操作",
                        subtitle = "FxBottomSheetActionList 演示"
                    ) {
                        FxBottomSheetActionList(
                            actions = listOf(
                                FxBottomSheetAction(title = "查看详情"),
                                FxBottomSheetAction(title = "编辑内容"),
                                FxBottomSheetAction(title = "导出数据", enabled = false),
                                FxBottomSheetAction(title = "删除", destructive = true)
                            ),
                            onActionClick = { action ->
                                sheetVisible = false
                                demoToast = "选择了：${action.title}"
                            }
                        )
                    }
                }
            }

            // ---------- 分页列表 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_paging)) {
                    FxPagedList(
                        state = pagingState,
                        emptyText = "暂无数据",
                        onRefresh = viewModel::refreshPaging,
                        onLoadMore = viewModel::loadMorePaging,
                        modifier = Modifier.height(420.dp)
                    ) { item ->
                        FxListItem(
                            title = item,
                            subtitle = "滚动到底部自动加载下一页",
                            status = "进行中"
                        )
                    }
                }
            }

            // ---------- 扫码与 NFC ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_scan)) {
                    val nfcStatus = rememberShowcaseNfcStatus(scanBridgeViewModel.nfcScanManager)
                    FxScanInputBox(
                        value = scanValue,
                        onValueChange = { scanValue = it },
                        label = "扫码内容（PDA/NFC/扫描枪自动回填）",
                        latestScanResult = latestScanResult,
                        onScanConsumed = { latestScanResult = null }
                    )
                    FxScanActionBar(
                        hint = "点击按钮调用摄像头拍照识别条码",
                        onActionClick = startCameraScan
                    )
                    FxNfcStatusHint(
                        supported = nfcStatus?.supported == true,
                        enabled = nfcStatus?.enabled == true,
                        onOpenSettings = {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                            }
                        }
                    )
                }
            }

            // ---------- 表格 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_table)) {
                    val tableColumns = listOf(
                        FxTableColumn(key = "code", title = "物料编码"),
                        FxTableColumn(key = "name", title = "物料名称"),
                        FxTableColumn(key = "qty", title = "数量", align = FxTableAlign.END),
                        FxTableColumn(key = "status", title = "状态")
                    )
                    val tableRows = listOf(
                        listOf("M-1001", "铝合金外壳", "120", "合格"),
                        listOf("M-1002", "不锈钢支架", "86", "待检"),
                        listOf("M-1003", "尼龙齿轮", "240", "合格")
                    )
                    FxTable(
                        columns = tableColumns,
                        rows = tableRows,
                        rowContent = { it }
                    )
                }
            }

            // ---------- 附件上传与预览 ----------
            item {
                FxFormSection(title = stringResource(R.string.component_showcase_section_upload)) {
                    Button(
                        onClick = { uploadPicker.launch("image/*") },
                        enabled = uploadState !is UploadDemoState.Uploading
                    ) {
                        Text(stringResource(R.string.component_showcase_upload_pick))
                    }
                    when (val state = uploadState) {
                        UploadDemoState.Uploading -> Text(
                            text = stringResource(R.string.common_processing),
                            style = MaterialTheme.typography.bodySmall
                        )
                        is UploadDemoState.Success -> {
                            Text(
                                text = stringResource(R.string.component_showcase_upload_success),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = state.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AsyncImage(
                                model = state.url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                        is UploadDemoState.Error -> Text(                            text = stringResource(R.string.component_showcase_upload_failed, state.message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        UploadDemoState.Idle -> Unit
                    }
                }
            }
        }
    }
}

/**
 * 扫描总线桥接 ViewModel，向验收页暴露全局设备能力。
 */
@HiltViewModel
class ShowcaseScanBridgeViewModel @Inject constructor(
    val scannerManager: FxScannerManager,
    val cameraScanManager: FxCameraScanManager,
    val scanFeedback: FxScanFeedback,
    val nfcScanManager: FxNfcScanManager
) : androidx.lifecycle.ViewModel()

/**
 * 摄像头扫码启动器（验收页内联版，与 feature/auth 同一交互协议）。
 */
@Composable
private fun rememberShowcaseCameraScan(
    cameraScanManager: FxCameraScanManager,
    scanFeedback: FxScanFeedback,
    onDecoded: (FxScanResult) -> Unit,
    onNotify: (Int) -> Unit
): () -> Unit {
    var pendingCapture by remember { mutableStateOf<FxCameraCapture?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            onNotify(R.string.scan_camera_unavailable)
        }
    }

    val captureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val capture = pendingCapture
        pendingCapture = null
        if (capture == null) {
            return@rememberLauncherForActivityResult
        }
        if (result.resultCode != Activity.RESULT_OK) {
            cameraScanManager.deleteCapture(capture)
            return@rememberLauncherForActivityResult
        }
        cameraScanManager.decode(
            capture = capture,
            onResult = { scanResult ->
                cameraScanManager.deleteCapture(capture)
                if (scanResult != null) {
                    scanFeedback.onScanSuccess(FxScanFeedback.HARDWARE_SCAN_CONFIG)
                    onDecoded(scanResult)
                } else {
                    onNotify(R.string.scan_camera_no_code)
                }
            },
            onError = {
                cameraScanManager.deleteCapture(capture)
                onNotify(R.string.scan_camera_failed)
            }
        )
    }

    return startScan@{
        if (!cameraScanManager.hasCameraPermission()) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return@startScan
        }
        val capture = cameraScanManager.createCaptureRequest()
        if (capture == null) {
            onNotify(R.string.scan_camera_unavailable)
            return@startScan
        }
        pendingCapture = capture
        try {
            captureLauncher.launch(capture.intent)
        } catch (_: ActivityNotFoundException) {
            pendingCapture = null
            cameraScanManager.deleteCapture(capture)
            onNotify(R.string.scan_camera_unavailable)
        }
    }
}

private data class ShowcaseNfcStatus(val supported: Boolean, val enabled: Boolean)

/**
 * 读取 NFC 支持与开关状态，回到前台时刷新。
 */
@Composable
private fun rememberShowcaseNfcStatus(
    nfcScanManager: FxNfcScanManager
): ShowcaseNfcStatus? {
    val activity = LocalContext.current as? Activity
    var status by remember { mutableStateOf<ShowcaseNfcStatus?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, activity) {
        fun refresh() {
            val host = activity ?: return
            status = ShowcaseNfcStatus(
                supported = nfcScanManager.isNfcSupported(host),
                enabled = nfcScanManager.isNfcEnabled(host)
            )
        }
        refresh()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return status
}
