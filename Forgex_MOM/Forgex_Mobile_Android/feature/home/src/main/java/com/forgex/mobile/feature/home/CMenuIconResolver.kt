package com.forgex.mobile.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.ui.graphics.vector.ImageVector
import com.forgex.mobile.core.network.model.workbench.CMenuVO

internal object CMenuIconResolver {

    fun resolve(menu: CMenuVO): ImageVector {
        val iconName = menu.icon?.trim().orEmpty()
        val path = menu.path?.trim().orEmpty().lowercase()
        val componentKey = menu.componentKey?.trim().orEmpty().lowercase()
        val moduleId = menu.moduleId

        resolveBackendIcon(iconName)?.let { return it }

        return when {
            path.contains("workbench") || componentKey.contains("home") -> Icons.Filled.Dashboard
            path.contains("workflow") || componentKey.contains("workflow") -> Icons.Filled.Workspaces
            path.contains("message") || componentKey.contains("message") -> Icons.Filled.Mail
            path.contains("profile") || componentKey.contains("profile") -> Icons.Filled.Person
            path.contains("basic") || moduleId == 5L -> Icons.Filled.Inventory2
            path.contains("info-test") -> Icons.Filled.Info
            else -> Icons.Filled.Widgets
        }
    }

    private fun resolveBackendIcon(iconName: String): ImageVector? {
        if (iconName.isBlank()) {
            return null
        }
        return when (iconName.lowercase()) {
            "appstoreoutlined", "appsoutlined", "appstorefilled" -> Icons.Filled.Apps
            "dashboardoutlined", "dashboardfilled" -> Icons.Filled.Dashboard
            "homeoutlined", "homefilled" -> Icons.Filled.Home
            "mailoutlined", "messageoutlined", "notificationoutlined" -> Icons.Filled.Mail
            "messagefilled", "mailfilled" -> Icons.Filled.Mail
            "belloutlined", "notificationsoutlined", "notificationfilled", "notificationsfilled" -> Icons.Filled.Notifications
            "useroutlined", "userfilled" -> Icons.Filled.Person
            "profileoutlined", "accountcircleoutlined", "accountcirclefilled" -> Icons.Filled.AccountCircle
            "folderoutlined", "folderfilled" -> Icons.Filled.Folder
            "fileoutlined", "filefilled", "readoutlined" -> Icons.Filled.Description
            "settingoutlined", "settingfilled", "tooloutlined" -> Icons.Filled.Settings
            "infocircleoutlined", "infocirclefilled" -> Icons.Filled.Info
            "unorderedlistoutlined", "menuoutlined", "widgetsoutlined" -> Icons.Filled.Widgets
            "databaseoutlined", "inboxoutlined", "deploymentunitoutlined", "clusteroutlined" -> Icons.Filled.Inventory2
            else -> null
        }
    }
}
