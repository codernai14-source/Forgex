/**
 * JSON 树形结构工具函数
 * 
 * 提供 JSON 数据与树形结构之间的转换功能，支持参数配置、参数映射等场景
 * 
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */

/**
 * 树形节点数据类型
 */
export type TreeNodeType = 'OBJECT' | 'ARRAY' | 'FIELD';

/**
 * 树形节点数据结构
 */
export interface JsonTreeNode {
  /**
   * 节点唯一标识（路径）
   */
  key: string;

  /**
   * 节点显示名称
   */
  title: string;

  /**
   * 节点类型：OBJECT（对象）、ARRAY（数组）、FIELD（字段）
   */
  type: TreeNodeType;

  /**
   * 节点值（仅 FIELD 类型有值）
   */
  value?: any;

  /**
   * 节点描述信息
   */
  description?: string;

  /**
   * 是否必填
   */
  required?: boolean;

  /**
   * 数据类型（FIELD 类型使用）
   */
  dataType?: string;

  /**
   * 子节点列表
   */
  children?: JsonTreeNode[];

  /**
   * 是否可编辑
   */
  editable?: boolean;

  /**
   * 父节点路径
   */
  parentKey?: string;

  /**
   * 节点在数组中的索引（仅 ARRAY 类型或数组元素使用）
   */
  index?: number;
}

/**
 * 映射配置数据结构
 */
export interface ParamMapping {
  /**
   * 映射 ID
   */
  id?: string;

  /**
   * 源字段路径
   */
  sourcePath: string;

  /**
   * 源字段名称
   */
  sourceName: string;

  /**
   * 目标字段路径
   */
  targetPath: string;

  /**
   * 目标字段名称
   */
  targetName: string;

  /**
   * 转换规则
   */
  transformRule?: string;

  /**
   * 转换规则类型：DIRECT（直接映射）、FUNCTION（函数转换）、CONSTANT（常量值）
   */
  transformType?: 'DIRECT' | 'FUNCTION' | 'CONSTANT';

  /**
   * 常量值（当 transformType 为 CONSTANT 时使用）
   */
  constantValue?: any;

  /**
   * 备注说明
   */
  remark?: string;
}

/**
 * 获取字段类型
 * 
 * 根据 JavaScript 值判断 JSON 字段的类型
 * 
 * @param value 字段值
 * @returns 字段类型字符串：OBJECT、ARRAY、STRING、NUMBER、BOOLEAN、NULL
 * @see getFieldTypeByValue
 */
export function getFieldType(value: any): string {
  if (value === null) {
    return 'NULL';
  }
  
  if (Array.isArray(value)) {
    return 'ARRAY';
  }
  
  if (typeof value === 'object') {
    return 'OBJECT';
  }
  
  if (typeof value === 'string') {
    return 'STRING';
  }
  
  if (typeof value === 'number') {
    return 'NUMBER';
  }
  
  if (typeof value === 'boolean') {
    return 'BOOLEAN';
  }
  
  return 'UNKNOWN';
}

/**
 * 获取字段类型的显示名称
 * 
 * 将类型代码转换为中文显示名称
 * 
 * @param type 类型代码
 * @returns 中文显示名称
 * @see getFieldType
 */
export function getFieldTypeName(type: string): string {
  const typeMap: Record<string, string> = {
    'OBJECT': '对象',
    'ARRAY': '数组',
    'FIELD': '字段',
    'STRING': '字符串',
    'NUMBER': '数字',
    'BOOLEAN': '布尔',
    'NULL': '空值',
    'UNKNOWN': '未知'
  };
  
  return typeMap[type] || type;
}

/**
 * 解析 JSON 为树形结构
 * 
 * 将 JSON 对象转换为树形节点结构，支持嵌套对象和数组
 * 
 * @param json JSON 对象或字符串
 * @param rootKey 根节点的 key，默认为 'root'
 * @param rootTitle 根节点的 title，默认为 '根节点'
 * @returns 树形节点数组
 * @see parseJsonToTreeWithMetadata
 * 
 * @example
 * // 示例：解析简单 JSON 对象
 * const json = { name: 'test', value: 123 };
 * const tree = parseJsonToTree(json);
 * // 返回包含两个子节点的树形结构
 */
export function parseJsonToTree(
  json: any,
  rootKey: string = 'root',
  rootTitle: string = '根节点'
): JsonTreeNode[] {
  if (typeof json === 'string') {
    try {
      json = JSON.parse(json);
    } catch (error) {
      console.error('JSON 解析失败:', error);
      return [];
    }
  }
  
  if (json === null || json === undefined) {
    return [];
  }
  
  const rootNode: JsonTreeNode = {
    key: rootKey,
    title: rootTitle,
    type: getFieldType(json) as TreeNodeType,
    value: json,
    children: [],
    editable: true,
    parentKey: undefined
  };
  
  // 如果是对象或数组，递归解析子节点
  if (typeof json === 'object') {
    rootNode.children = parseObjectToTree(json, rootKey);
  }
  
  return [rootNode];
}

/**
 * 解析对象为树形子节点
 * 
 * 递归解析对象或数组的子节点
 * 
 * @param obj 要解析的对象或数组
 * @param parentKey 父节点路径
 * @returns 子节点数组
 * @see parseJsonToTree
 */
function parseObjectToTree(obj: any, parentKey: string): JsonTreeNode[] {
  const children: JsonTreeNode[] = [];
  
  if (Array.isArray(obj)) {
    // 数组类型：为每个元素创建节点
    obj.forEach((item, index) => {
      const key = `${parentKey}[${index}]`;
      const type = getFieldType(item) as TreeNodeType;
      
      const node: JsonTreeNode = {
        key,
        title: `[${index}]`,
        type,
        value: item,
        index,
        parentKey,
        editable: true
      };
      
      // 如果元素是对象或数组，递归解析
      if (typeof item === 'object' && item !== null) {
        node.children = parseObjectToTree(item, key);
      }
      
      children.push(node);
    });
  } else if (typeof obj === 'object' && obj !== null) {
    // 对象类型：为每个属性创建节点
    Object.keys(obj).forEach(key => {
      const value = obj[key];
      const fullKey = `${parentKey}.${key}`;
      const type = getFieldType(value) as TreeNodeType;
      
      const node: JsonTreeNode = {
        key: fullKey,
        title: key,
        type,
        value,
        parentKey,
        editable: true
      };
      
      // 如果是对象或数组，递归解析子节点
      if (typeof value === 'object' && value !== null) {
        node.children = parseObjectToTree(value, fullKey);
      }
      
      children.push(node);
    });
  }
  
  return children;
}

/**
 * 解析带元数据的 JSON 为树形结构
 * 
 * 支持解析包含字段描述、类型定义、必填标记等元数据的 JSON Schema
 * 
 * @param jsonSchema 带元数据的 JSON Schema 对象
 * @param rootKey 根节点的 key，默认为 'root'
 * @param rootTitle 根节点的 title，默认为 '根节点'
 * @returns 树形节点数组
 * @see parseJsonToTree
 * 
 * @example
 * // 示例：解析 JSON Schema
 * const schema = {
 *   name: { type: 'string', description: '用户名', required: true },
 *   age: { type: 'number', description: '年龄' }
 * };
 * const tree = parseJsonToTreeWithMetadata(schema);
 */
export function parseJsonToTreeWithMetadata(
  jsonSchema: any,
  rootKey: string = 'root',
  rootTitle: string = '根节点'
): JsonTreeNode[] {
  if (typeof jsonSchema === 'string') {
    try {
      jsonSchema = JSON.parse(jsonSchema);
    } catch (error) {
      console.error('JSON Schema 解析失败:', error);
      return [];
    }
  }
  
  if (jsonSchema === null || jsonSchema === undefined) {
    return [];
  }
  
  const rootNode: JsonTreeNode = {
    key: rootKey,
    title: rootTitle,
    type: 'OBJECT',
    value: jsonSchema,
    children: [],
    editable: true,
    parentKey: undefined
  };
  
  // 解析带元数据的字段
  if (typeof jsonSchema === 'object') {
    rootNode.children = parseMetadataObjectToTree(jsonSchema, rootKey);
  }
  
  return [rootNode];
}

/**
 * 解析带元数据的对象为树形子节点
 * 
 * 专门处理包含 type、description、required 等元数据的字段
 * 
 * @param schema 带元数据的 Schema 对象
 * @param parentKey 父节点路径
 * @returns 子节点数组
 * @see parseJsonToTreeWithMetadata
 */
function parseMetadataObjectToTree(schema: any, parentKey: string): JsonTreeNode[] {
  const children: JsonTreeNode[] = [];
  
  if (Array.isArray(schema)) {
    // 数组类型
    schema.forEach((item, index) => {
      const key = `${parentKey}[${index}]`;
      const node = createNodeFromMetadata(item, key, `[${index}]`, parentKey);
      children.push(node);
    });
  } else if (typeof schema === 'object' && schema !== null) {
    // 对象类型
    Object.keys(schema).forEach(key => {
      const value = schema[key];
      const fullKey = `${parentKey}.${key}`;
      
      // 如果字段包含元数据（type、description 等）
      if (value && typeof value === 'object' && 'type' in value) {
        const node = createNodeFromMetadata(value, fullKey, key, parentKey);
        children.push(node);
      } else {
        // 普通字段，递归解析
        const type = getFieldType(value) as TreeNodeType;
        const node: JsonTreeNode = {
          key: fullKey,
          title: key,
          type,
          value,
          parentKey,
          editable: true
        };
        
        if (typeof value === 'object' && value !== null) {
          node.children = parseMetadataObjectToTree(value, fullKey);
        }
        
        children.push(node);
      }
    });
  }
  
  return children;
}

/**
 * 从元数据创建节点
 * 
 * 根据字段的元数据（type、description、required 等）创建树形节点
 * 
 * @param metadata 字段元数据
 * @param key 节点 key
 * @param title 节点显示名称
 * @param parentKey 父节点 key
 * @returns 树形节点
 * @see parseMetadataObjectToTree
 */
function createNodeFromMetadata(
  metadata: any,
  key: string,
  title: string,
  parentKey?: string
): JsonTreeNode {
  const type = metadata.type || getFieldType(metadata.value);
  const isObject = type === 'OBJECT' || type === 'object';
  const isArray = type === 'ARRAY' || type === 'array';
  
  const node: JsonTreeNode = {
    key,
    title,
    type: isObject ? 'OBJECT' : isArray ? 'ARRAY' : 'FIELD',
    value: metadata.value,
    description: metadata.description,
    required: metadata.required || false,
    dataType: typeof type === 'string' ? type.toUpperCase() : type,
    parentKey,
    editable: true
  };
  
  // 如果有子节点，递归解析
  if (metadata.properties) {
    node.children = parseMetadataObjectToTree(metadata.properties, key);
  } else if (metadata.items) {
    node.children = parseMetadataObjectToTree(metadata.items, `${key}[]`);
  }
  
  return node;
}

/**
 * 将树形结构转换为 JSON 对象
 * 
 * 与 parseJsonToTree 相反，将树形节点转换回 JSON 对象
 * 
 * @param treeNodes 树形节点数组
 * @returns JSON 对象
 * @see parseJsonToTree
 */
export function convertTreeToJson(treeNodes: JsonTreeNode[]): any {
  if (!treeNodes || treeNodes.length === 0) {
    return {};
  }
  
  const rootNode = treeNodes[0];
  return convertNodeToObject(rootNode);
}

/**
 * 将单个节点转换为对象
 * 
 * 递归将树形节点转换为普通对象或数组
 * 
 * @param node 树形节点
 * @returns 转换后的对象或数组
 * @see convertTreeToJson
 */
function convertNodeToObject(node: JsonTreeNode): any {
  if (node.type === 'FIELD') {
    return node.value;
  }
  
  if (node.type === 'ARRAY') {
    if (!node.children) {
      return [];
    }
    return node.children.map(child => convertNodeToObject(child));
  }
  
  if (node.type === 'OBJECT') {
    if (!node.children) {
      return {};
    }
    
    const result: any = {};
    node.children.forEach(child => {
      // 提取属性名（去除父路径前缀）
      const propName = child.key.includes('.') 
        ? child.key.substring(child.key.lastIndexOf('.') + 1)
        : child.key;
      result[propName] = convertNodeToObject(child);
    });
    return result;
  }
  
  return node.value;
}

/**
 * 根据路径获取节点
 * 
 * 在树形结构中根据 key 路径查找对应的节点
 * 
 * @param treeNodes 树形节点数组
 * @param path 节点路径（key）
 * @returns 找到的节点，未找到返回 undefined
 * @see findNodesByType
 */
export function findNodeByPath(treeNodes: JsonTreeNode[], path: string): JsonTreeNode | undefined {
  for (const node of treeNodes) {
    if (node.key === path) {
      return node;
    }
    
    if (node.children) {
      const found = findNodeByPath(node.children, path);
      if (found) {
        return found;
      }
    }
  }
  
  return undefined;
}

/**
 * 根据类型筛选节点
 * 
 * 获取树形结构中指定类型的所有节点
 * 
 * @param treeNodes 树形节点数组
 * @param type 节点类型
 * @returns 符合条件的节点数组
 * @see findNodeByPath
 */
export function findNodesByType(treeNodes: JsonTreeNode[], type: TreeNodeType): JsonTreeNode[] {
  const result: JsonTreeNode[] = [];
  
  for (const node of treeNodes) {
    if (node.type === type) {
      result.push(node);
    }
    
    if (node.children) {
      result.push(...findNodesByType(node.children, type));
    }
  }
  
  return result;
}

/**
 * 获取所有叶子节点（字段节点）
 * 
 * 获取树形结构中所有的 FIELD 类型节点（即最底层的字段）
 * 
 * @param treeNodes 树形节点数组
 * @returns 字段节点数组
 * @see findNodesByType
 */
export function getLeafNodes(treeNodes: JsonTreeNode[]): JsonTreeNode[] {
  return findNodesByType(treeNodes, 'FIELD');
}

/**
 * 添加节点到树形结构
 * 
 * 在指定的父节点下添加新的子节点
 * 
 * @param treeNodes 树形节点数组
 * @param parentKey 父节点路径
 * @param newNode 新节点
 * @returns 是否添加成功
 * @see removeNode
 */
export function addNode(
  treeNodes: JsonTreeNode[],
  parentKey: string,
  newNode: JsonTreeNode
): boolean {
  const parentNode = findNodeByPath(treeNodes, parentKey);
  
  if (!parentNode) {
    return false;
  }
  
  if (!parentNode.children) {
    parentNode.children = [];
  }
  
  // 设置新节点的父路径和默认值
  newNode.parentKey = parentKey;
  if (!newNode.key) {
    const index = parentNode.children.length;
    newNode.key = parentNode.type === 'ARRAY' 
      ? `${parentKey}[${index}]`
      : `${parentKey}.${newNode.title}`;
  }
  
  parentNode.children.push(newNode);
  return true;
}

/**
 * 从树形结构中移除节点
 * 
 * 根据路径删除指定的节点
 * 
 * @param treeNodes 树形节点数组
 * @param nodeKey 要删除的节点路径
 * @returns 是否删除成功
 * @see addNode
 */
export function removeNode(treeNodes: JsonTreeNode[], nodeKey: string): boolean {
  // 处理根节点
  if (treeNodes.length === 1 && treeNodes[0].key === nodeKey) {
    treeNodes.pop();
    return true;
  }
  
  // 查找父节点
  const parentKey = nodeKey.substring(0, nodeKey.lastIndexOf('.'));
  const parentNode = findNodeByPath(treeNodes, parentKey);
  
  if (!parentNode || !parentNode.children) {
    return false;
  }
  
  const index = parentNode.children.findIndex(child => child.key === nodeKey);
  if (index === -1) {
    return false;
  }
  
  parentNode.children.splice(index, 1);
  return true;
}

/**
 * 更新节点信息
 * 
 * 更新指定节点的属性
 * 
 * @param treeNodes 树形节点数组
 * @param nodeKey 节点路径
 * @param updates 要更新的属性
 * @returns 是否更新成功
 * @see addNode
 */
export function updateNode(
  treeNodes: JsonTreeNode[],
  nodeKey: string,
  updates: Partial<JsonTreeNode>
): boolean {
  const node = findNodeByPath(treeNodes, nodeKey);
  
  if (!node) {
    return false;
  }
  
  Object.assign(node, updates);
  return true;
}

/**
 * 生成唯一的节点路径
 * 
 * 根据父路径和节点名称生成唯一的 key
 * 
 * @param parentKey 父节点路径
 * @param nodeName 节点名称
 * @param existingKeys 已存在的 key 列表
 * @returns 唯一的节点路径
 * @see addNode
 */
export function generateUniqueKey(
  parentKey: string,
  nodeName: string,
  existingKeys: string[] = []
): string {
  const baseKey = parentKey ? `${parentKey}.${nodeName}` : nodeName;
  
  if (!existingKeys.includes(baseKey)) {
    return baseKey;
  }
  
  // 如果已存在，添加序号
  let index = 1;
  while (existingKeys.includes(`${baseKey}_${index}`)) {
    index++;
  }
  
  return `${baseKey}_${index}`;
}

/**
 * 验证 JSON 格式
 * 
 * 检查字符串是否为有效的 JSON 格式
 * 
 * @param jsonString 要验证的 JSON 字符串
 * @returns 是否为有效的 JSON
 * @see parseJsonToTree
 */
export function isValidJson(jsonString: string): boolean {
  try {
    JSON.parse(jsonString);
    return true;
  } catch {
    return false;
  }
}

/**
 * 格式化 JSON 字符串
 * 
 * 将 JSON 对象或字符串格式化为美观的字符串
 * 
 * @param json JSON 对象或字符串
 * @param spaces 缩进空格数，默认为 2
 * @returns 格式化后的 JSON 字符串
 * @see isValidJson
 */
export function formatJsonString(json: any, spaces: number = 2): string {
  if (typeof json === 'string') {
    try {
      json = JSON.parse(json);
    } catch {
      return json;
    }
  }
  
  return JSON.stringify(json, null, spaces);
}
