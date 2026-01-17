## 菜单结构整合方案

### 目标结构
```
系统管理
├── 系统管理主页 (order: 1)
├── 组织架构 (catalog, order: 2)
│   ├── 用户管理 (id: 1)
│   ├── 部门管理 (id: 5)
│   └── 职位管理 (id: 6)
├── 授权管理 (catalog, order: 3)
│   ├── 角色管理 (id: 2)
│   ├── 租户管理 (id: 646)
│   └── 菜单管理 (id: 4)
├── 模块管理 (order: 4)
├── 导出配置 (order: 5)
├── 导入配置 (order: 6)
├── 字典管理 (order: 7)
├── 表格配置 (order: 8)
├── 登录日志 (order: 9)
└── 在线用户 (order: 10)
```

### 执行步骤

1. **创建"组织架构"二级目录**
   - type: catalog
   - icon: ApartmentOutlined
   - order_num: 2
   - menu_level: 1

2. **创建"授权管理"二级目录**
   - type: catalog
   - icon: SafetyOutlined
   - order_num: 3
   - menu_level: 1

3. **更新菜单parent_id**
   - 用户管理(id:1)、部门管理(id:5)、职位管理(id:6) → parent_id改为"组织架构"目录ID
   - 角色管理(id:2)、租户管理(id:646)、菜单管理(id:4) → parent_id改为"授权管理"目录ID
   - 其他一级菜单的parent_id保持为0，调整order_num

4. **更新按钮权限的parent_id**
   - 所有按钮的parent_id保持不变（因为父菜单ID没有变化）

5. **处理角色菜单关联表(sys_role_menu)**
   - 将新创建的两个目录添加到系统管理员角色的权限中

### 技术要点
- 使用事务确保数据一致性
- 脚本包含回滚注释，方便恢复
- 所有操作在forgex_admin数据库中执行