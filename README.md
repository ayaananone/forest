# 智慧林场综合管理平台 🌲

[![Vue 3](https://img.shields.io/badge/Vue-3.2+-4FC08D?logo=vue.js)](https://vuejs.org/)
[![OpenLayers](https://img.shields.io/badge/OpenLayers-10.8-1F6B75?logo=openlayers)](https://openlayers.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7+-6DB33F?logo=spring)](https://spring.io/)
[![GeoServer](https://img.shields.io/badge/GeoServer-2.24-0099CC)](http://geoserver.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> 基于 WebGIS 技术的林业资源可视化管理系统，实现林场数据的空间展示、查询分析与统计可视化。

---

## 📋 项目简介

本项目是一个面向林业管理的综合GIS平台，采用前后端分离架构，集成了地图可视化、空间查询、数据统计分析等核心功能。系统支持林场小班数据的空间展示、多条件筛选、半径范围查询、蓄积量可视化等专业林业GIS功能。

### 核心功能

| 功能模块 | 描述 |
|---------|------|
| 🗺️ **地图可视化** | 基于 OpenLayers 的林场小班空间数据展示，支持 WMS/WFS 服务 |
| 🔍 **空间查询** | 半径范围查询、点击查询、属性筛选 |
| 📊 **统计分析** | 树种分布图、蓄积量趋势图、生长趋势分析 |
| 🎛️ **图层控制** | 多图层叠加、透明度调节、图层切换 |
| 📋 **详情展示** | 林场小班详情卡片、样地数据展示 |

---

## 🏗️ 技术架构

### 前端技术栈

```
Vue 3.2 + Composition API
├── OpenLayers 10.8    # GIS 地图引擎
├── Element Plus 2.3   # UI 组件库
├── Chart.js 3.9       # 数据可视化图表
└── Vue CLI 5.0        # 构建工具
```

### 后端技术栈

```
Spring Boot 2.7
├── Spring Data JPA    # 数据持久层
├── PostgreSQL + PostGIS  # 空间数据库
├── GeoServer 2.24     # GIS 地图服务
└── Gradle 7.x         # 构建工具
```

### 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                      前端层 (Vue 3)                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐ │
│  │  Map View   │ │  Charts     │ │  Layer Control      │ │
│  │  (OpenLayers)│ │ (Chart.js)  │ │  (Element Plus)     │ │
│  └─────────────┘ └─────────────┘ └─────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                           │ REST API
┌─────────────────────────────────────────────────────────┐
│                      后端层 (Spring Boot)                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐ │
│  │ Controller  │ │   Service   │ │   Repository        │ │
│  │  (REST API) │ │ (Business)  │ │   (JPA/PostGIS)     │ │
│  └─────────────┘ └─────────────┘ └─────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                      数据层                               │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐ │
│  │ PostgreSQL  │ │  PostGIS    │ │    GeoServer        │ │
│  │  (属性数据)  │ │ (空间数据)   │ │   (WMS/WFS服务)      │ │
│  └─────────────┘ └─────────────┘ └─────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 项目结构

```
forest/
├── frontend/forest_front/          # 前端项目
│   ├── src/
│   │   ├── api/                    # API 接口封装
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 组件
│   │   │   ├── charts/             # 图表组件
│   │   │   │   ├── ChartContainer.vue
│   │   │   │   ├── SpeciesChart.vue    # 树种分布图
│   │   │   │   ├── TrendChart.vue      # 趋势分析图
│   │   │   │   └── VolumeChart.vue     # 蓄积量图
│   │   │   ├── common/             # 通用组件
│   │   │   └── map/                # 地图组件
│   │   │       ├── LayerControl.vue    # 图层控制器
│   │   │       ├── MapContainer.vue    # 地图容器
│   │   │       ├── MapPopup.vue        # 地图弹窗
│   │   │       └── RadiusQuery.vue     # 半径查询
│   │   ├── composables/            # 组合式函数
│   │   ├── config/                 # 配置文件
│   │   ├── styles/                 # 样式文件
│   │   ├── utils/                  # 工具函数
│   │   ├── views/                  # 页面视图
│   │   ├── App.vue                 # 根组件
│   │   └── main.js                 # 入口文件
│   └── package.json
│
├── src/main/java/com/ceshi/forest/ # 后端项目
│   ├── Controller/                 # 控制器层
│   │   ├── ForestStandController.java      # 林场小班API
│   │   ├── GeoserverProxyController.java   # GeoServer代理
│   │   ├── SamplePlotController.java       # 样地数据API
│   │   └── TreeMeasurementController.java  # 测树数据API
│   ├── config/                     # 配置类
│   ├── dto/                        # 数据传输对象
│   ├── entity/                     # 实体类
│   ├── repository/                 # 数据访问层
│   ├── service/                    # 业务逻辑层
│   ├── util/                       # 工具类
│   └── ForestGisApplication.java   # 启动类
│
├── geoserver/                      # GeoServer 样式配置
└── build.gradle                    # Gradle 构建配置
```

---

## 🚀 快速开始

### 环境要求

- **Node.js** >= 18.0
- **Java** >= 11
- **PostgreSQL** >= 13 + PostGIS 扩展
- **GeoServer** >= 2.24

### 前端运行

```bash
# 进入前端目录
cd frontend/forest_front

# 安装依赖
npm install

# 启动开发服务器
npm run serve

# 构建生产环境
npm run build
```

### 后端运行

```bash
# 进入后端目录（根目录）

# 使用 Gradle 运行
./gradlew bootRun

# 或构建后运行
./gradlew build
java -jar build/libs/forest-*.jar
```

### 数据库配置

1. 创建 PostgreSQL 数据库并启用 PostGIS 扩展：

```sql
CREATE DATABASE forest_gis;
\c forest_gis;
CREATE EXTENSION postgis;
```

2. 配置 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/forest_gis
    username: your_username
    password: your_password
```

3. 配置 GeoServer 连接信息

---

## 💡 技术亮点

### 前端亮点

1. **OpenLayers 深度定制**
   - 自定义 WMS/WFS 图层加载
   - 实现 Feature 点击高亮与详情展示
   - 半径范围空间查询（Buffer Analysis）

2. **Vue 3 Composition API 最佳实践**
   - 使用 `composables` 封装可复用逻辑
   - 组件化设计，高内聚低耦合
   - 响应式数据流管理

3. **数据可视化**
   - Chart.js 实现多维度统计图表
   - 实时数据联动更新
   - 图表与地图交互联动

### 后端亮点

1. **分层架构设计**
   - 清晰的 Controller-Service-Repository 分层
   - DTO 模式实现数据封装
   - RESTful API 设计规范

2. **GeoServer 集成**
   - 代理 GeoServer WMS/WFS 服务
   - 实现空间数据查询接口
   - 动态样式配置

3. **空间数据操作**
   - PostGIS 空间函数应用
   - 地理坐标系转换
   - 空间关系查询

---

## 📸 功能展示

### 主界面

智慧林场综合管理平台采用响应式布局，左侧为统计面板，右侧为地图展示区。

### 地图功能

- **多图层叠加**：支持小班边界、蓄积量热力图、树种分布图等多图层叠加
- **交互查询**：点击小班显示详细信息卡片
- **半径查询**：以指定点为中心，查询范围内的林业资源

### 统计分析

- **树种分布**：饼图展示各树种面积占比
- **蓄积量趋势**：折线图展示历年蓄积量变化
- **生长分析**：柱状图对比不同龄组生长情况

---

## 🔧 开发计划

- [x] 基础地图展示
- [x] 图层控制功能
- [x] 属性筛选查询
- [x] 统计图表展示
- [x] 半径范围查询
- [x] 详情卡片展示
- [ ] 用户认证系统
- [ ] 数据编辑功能
- [ ] 移动端适配
- [ ] 三维场景展示

---

## 📚 相关技术

| 技术 | 版本 | 用途 |
|-----|------|------|
| [Vue](https://vuejs.org/) | 3.2+ | 前端框架 |
| [OpenLayers](https://openlayers.org/) | 10.8 | GIS 地图库 |
| [Element Plus](https://element-plus.org/) | 2.3 | UI 组件库 |
| [Chart.js](https://www.chartjs.org/) | 3.9 | 图表库 |
| [Spring Boot](https://spring.io/projects/spring-boot) | 2.7 | 后端框架 |
| [PostGIS](https://postgis.net/) | 3.4 | 空间数据库 |
| [GeoServer](http://geoserver.org/) | 2.24 | 地图服务 |

---

## 👨‍💻 开发者

**南京林业大学 - 地理信息科学专业**

- 专注于 WebGIS 开发与林业信息化
- 熟悉 Vue、OpenLayers、Spring Boot 全栈开发
- 具备空间数据分析与可视化能力

---

## 📄 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

---

> 🌲 用技术守护绿水青山，让林业管理更智慧！
