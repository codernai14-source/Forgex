package com.forgex.mobile.feature.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forgex.mobile.core.ui.R
import androidx.compose.ui.res.stringResource

const val BASIC_INFO_TEST_ROUTE = "basic/info-test"

private data class DemoRow(
    val code: String,
    val name: String,
    val owner: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInfoTestScreen(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("选项 A") }
    var inputValue by remember { mutableStateOf("测试内容") }
    var switchEnabled by remember { mutableStateOf(true) }
    var checked by remember { mutableStateOf(true) }

    val dropdownOptions = listOf("选项 A", "选项 B", "选项 C")
    val groupedItems = listOf(
        "基础资料组" to listOf("客户档案", "供应商档案"),
        "组织资料组" to listOf("部门信息", "岗位信息")
    )
    val tableRows = listOf(
        DemoRow("BASIC-001", "基础信息测试页", "系统管理员"),
        DemoRow("BASIC-002", "角色权限校验", "测试账号"),
        DemoRow("BASIC-003", "原生组件展示", "开发账号")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.basic_test_page_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(R.string.basic_test_page_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.basic_test_page_dropdown)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    dropdownOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedOption = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text(stringResource(R.string.basic_test_page_input)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.basic_test_page_switch))
                Switch(checked = switchEnabled, onCheckedChange = { switchEnabled = it })
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.basic_test_page_checkbox))
                Checkbox(checked = checked, onCheckedChange = { checked = it })
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.basic_test_page_group),
                    style = MaterialTheme.typography.titleMedium
                )
                groupedItems.forEach { (groupName, groupEntries) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(groupName, style = MaterialTheme.typography.titleSmall)
                            groupEntries.forEach { entry ->
                                Text(
                                    text = entry,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.basic_test_page_table),
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("编码", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                            Text("名称", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelMedium)
                            Text("负责人", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                        }
                        tableRows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(row.code, modifier = Modifier.weight(1f))
                                Text(row.name, modifier = Modifier.weight(1.4f))
                                Text(row.owner, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.basic_test_page_action))
            }
        }
    }
}
