# 实验八　网络天气应用

使用 Java、Volley 和 Gson 实现的 Android 天气应用。启动后可浏览 2,504 个城市及区县，按地名、所属地区或天气编码筛选，也可直接选择成都、北京或天津。点选城市后，第二个页面展示气象站更新时间、气温、湿度、空气质量、日出日落和逐日预报。

## 运行

1. 在 Android Studio 中打开仓库根目录，安装 Android SDK Platform 37，等待 Gradle 同步。
2. 选择 Android 8.0（API 26）或以上的设备，运行 `app`。
3. 在搜索框输入 `101030100` 并选择天津，或点选首页的常用城市。打开详情后可刷新天气，向上滑动可查看后续日期。

Windows 下也可以用项目自带的 Gradle Wrapper 构建。

```powershell
.\gradlew.bat assembleDebug
```

生成的安装包位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 实验实现

- `CityDirectory.java` 在后台执行器中被调用，使用 Gson 解析本地 `res/raw/citycode.json`。程序利用父级记录还原地区信息，只将带天气编码的条目加入可选列表。
- `CityActivity.java` 将用户选择的名称、地区和天气编码传给 `WeatherActivity`。
- `WeatherRequest.java` 通过 Volley 访问附件指定的 `http://t.weather.itboy.net/api/weather/city/{城市编码}`。请求在工作线程解析 JSON，检查业务状态和返回的城市编码。网络配置仅为该接口域名放行 HTTP。
- `WeatherActivity.java` 显示接口返回的天气与预报，并按天气类型选用附件图片。断网时可以在详情页重试，城市列表仍可离线浏览。

## 验证与报告

在 Pixel 7 模拟器（Android 14，API 34）上验证了目录加载、成都和天津的联网查询、15 天预报滚动、搜索无结果，以及断网后恢复查询。天气读数和预报天数以接口当次返回为准。

[下载实验报告（Word，含核心代码与模拟器截图）](docs/实验八_网络天气应用_实验报告.docx)
