# ATAK 플러그인 개발 가이드 - 좌표 마커 플러그인

## 프로젝트 개요

### 목표
ATAK에서 사용자가 좌표를 입력하면 해당 위치에 마커를 표시하는 간단한 플러그인 개발

### 개발 환경
- SDK: ATAK-CIV-5.5.1.6-SDK
- 빌드 도구: Gradle
- 언어: Java
- 플러그인 이름: coordinatemarker

---

## 개발 단계

### 1단계: 프로젝트 생성

#### 플러그인 템플릿 복사

```bash
cp -r samples/plugintemplate samples/coordinatemarker
```

**목적**: 기존 템플릿을 복사하여 새 플러그인의 기본 구조 생성

---

### 2단계: 패키지명 변경

#### 디렉토리 구조 변경

```bash
mv samples/coordinatemarker/app/src/main/java/com/atakmap/android/plugintemplate samples/coordinatemarker/app/src/main/java/com/atakmap/android/coordinatemarker
```

#### 수정한 파일들

**파일 1: build.gradle**

위치: `samples/coordinatemarker/app/build.gradle:109`

```gradle
# 변경 전
namespace 'com.atakmap.android.plugintemplate.plugin'

# 변경 후
namespace 'com.atakmap.android.coordinatemarker.plugin'
```

**파일 2: PluginTemplate.java**

위치: `samples/coordinatemarker/app/src/main/java/com/atakmap/android/coordinatemarker/plugin/PluginTemplate.java:2`

```java
// 변경 전
package com.atakmap.android.plugintemplate.plugin;

// 변경 후
package com.atakmap.android.coordinatemarker.plugin;
```

**파일 3: PluginNativeLoader.java**

동일하게 패키지명 변경

```java
package com.atakmap.android.coordinatemarker.plugin;
```

**파일 4: build.gradle (ATAK 버전 설정)**

위치: `samples/coordinatemarker/app/build.gradle:12`

```gradle
# 변경 전
ext.ATAK_VERSION = "5.5.0"

# 변경 후
ext.ATAK_VERSION = "5.5.1"
```

**중요**: ATAK 앱의 버전과 플러그인의 `ATAK_VERSION`이 일치해야 합니다. 버전이 맞지 않으면 플러그인 로드 시 다음과 같은 에러가 발생합니다:
```
this plugin requires software version: 5.5.0.CIV but you are running: 5.5.1.CIV
```

**파일 5: plugin.xml (플러그인 진입점 설정)** ⭐ **매우 중요**

위치: `samples/coordinatemarker/app/src/main/assets/plugin.xml:7`

```xml
<!-- 변경 전 -->
<extension
    type="gov.tak.api.plugin.IPlugin"
    impl="com.atakmap.android.plugintemplate.plugin.PluginTemplate"
    singleton="true" />

<!-- 변경 후 -->
<extension
    type="gov.tak.api.plugin.IPlugin"
    impl="com.atakmap.android.coordinatemarker.plugin.PluginTemplate"
    singleton="true" />
```

**매우 중요**: 이 파일을 수정하지 않으면 플러그인 로드 시 다음과 같은 에러가 발생합니다:
```
Failed to load Plugin Template
```

ATAK이 잘못된 패키지 경로에서 클래스를 찾으려고 시도하기 때문입니다.

**파일 6: strings.xml (플러그인 이름 및 설명)**

위치: `samples/coordinatemarker/app/src/main/res/values/strings.xml`

```xml
<!-- 변경 전 -->
<string name="app_name">Plugin Template</string>
<string name="app_desc">This plugin demonstrate bare bones plugin UI, and may be used to start a new plugin project</string>

<!-- 변경 후 -->
<string name="app_name">Coordinate Marker</string>
<string name="app_desc">좌표를 입력하여 지도에 마커를 표시하는 플러그인입니다.</string>
```

---

### 3단계: 필요한 Import 추가

**PluginTemplate.java에 추가한 Import 문**

```java
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.coremap.maps.coords.GeoPoint;
import java.util.UUID;
```

**주요 클래스 설명**

| 클래스 | 용도 |
|--------|------|
| AlertDialog | 좌표 입력 다이얼로그 생성 |
| EditText | 좌표 입력 필드 |
| Marker | ATAK 지도에 마커 생성 |
| GeoPoint | 위도/경도 좌표 표현 |
| MapView | ATAK 지도 뷰 접근 |

---

### 4단계: 좌표 입력 UI 생성

#### 레이아웃 XML 파일 생성

**파일 경로**: `samples/coordinatemarker/app/src/main/res/layout/coordinate_input_dialog.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="위도 (Latitude)"
        android:textSize="14sp"
        android:layout_marginBottom="8dp"/>

    <EditText
        android:id="@+id/latitude_input"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="예: 37.5665"
        android:inputType="numberDecimal|numberSigned"
        android:layout_marginBottom="16dp"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="경도 (Longitude)"
        android:textSize="14sp"
        android:layout_marginBottom="8dp"/>

    <EditText
        android:id="@+id/longitude_input"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="예: 126.9780"
        android:inputType="numberDecimal|numberSigned"/>

</LinearLayout>
```

**주요 구성 요소**

- `LinearLayout`: 세로 방향 레이아웃
- `TextView`: 입력 필드 라벨
- `EditText`: 위도/경도 입력 필드
  - `inputType="numberDecimal|numberSigned"`: 부호와 소수점 입력 가능

---

### 5단계: 다이얼로그 로직 구현

**PluginTemplate.java의 showPane() 메서드 수정**

```java
private void showPane() {
    // Show coordinate input dialog
    showCoordinateInputDialog();
}
```

**새로운 메서드 추가: showCoordinateInputDialog()**

```java
private void showCoordinateInputDialog() {
    try {
        // Get MapView's context (Activity context)
        MapView mapView = MapView.getMapView();
        if (mapView == null) {
            Toast.makeText(pluginContext, "지도를 찾을 수 없습니다",
                Toast.LENGTH_SHORT).show();
            return;
        }

        Context mapContext = mapView.getContext();

        // Create dialog layout using PluginLayoutInflater
        View dialogView = PluginLayoutInflater.inflate(pluginContext,
                R.layout.coordinate_input_dialog, null);

        EditText latitudeInput = dialogView.findViewById(R.id.latitude_input);
        EditText longitudeInput = dialogView.findViewById(R.id.longitude_input);

        // Build and show dialog
        new AlertDialog.Builder(mapContext)
            .setTitle("좌표 입력")
            .setView(dialogView)
            .setPositiveButton("확인", (dialog, which) -> {
                try {
                    double latitude = Double.parseDouble(
                        latitudeInput.getText().toString());
                    double longitude = Double.parseDouble(
                        longitudeInput.getText().toString());

                    // Validate coordinates
                    if (latitude < -90 || latitude > 90 ||
                        longitude < -180 || longitude > 180) {
                        Toast.makeText(pluginContext,
                            "잘못된 좌표입니다", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add marker at coordinates
                    addMarkerAtCoordinate(latitude, longitude);
                    Toast.makeText(pluginContext,
                        "마커가 추가되었습니다", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(pluginContext,
                        "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("취소", null)
            .show();
    } catch (Exception e) {
        // Catch any errors and show them
        Toast.makeText(pluginContext,
            "에러: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
            Toast.LENGTH_LONG).show();
        e.printStackTrace();
    }
}
```

**코드 동작 순서**

1. **전체 try-catch로 감싸기** ⭐ **매우 중요**
   - 크래시 방지 및 에러 메시지 표시
2. **MapView에서 Activity Context 가져오기** ⭐ **중요**
   - `MapView.getMapView().getContext()` 사용
   - 플러그인 Context가 아닌 Activity Context 필요
3. **PluginLayoutInflater 사용** ⭐ **중요**
   - `PluginLayoutInflater.inflate(pluginContext, R.layout.coordinate_input_dialog, null)`
   - 일반 `LayoutInflater`가 아닌 `PluginLayoutInflater` 사용
   - 플러그인 리소스를 올바르게 로드하기 위함
4. `findViewById()`로 EditText 참조 획득
5. `AlertDialog.Builder`로 다이얼로그 구성 (mapContext 사용)
6. "확인" 버튼 클릭 시:
   - 입력값을 double로 변환
   - 좌표 유효성 검사 (위도: -90~90, 경도: -180~180)
   - 마커 생성 메서드 호출
7. 예외 처리로 잘못된 입력 대응

**핵심 포인트**
- ✅ `PluginLayoutInflater` 사용 (리소스 로드)
- ✅ `mapContext` 사용 (Dialog 생성)
- ✅ 전체 try-catch (에러 처리)

---

### 6단계: 마커 생성 기능 구현

**새로운 메서드 추가: addMarkerAtCoordinate()**

```java
private void addMarkerAtCoordinate(double latitude, double longitude) {
    try {
        // Create GeoPoint with the provided coordinates
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        // Create a unique ID for the marker
        String uid = UUID.randomUUID().toString();

        // Create the marker
        Marker marker = new Marker(geoPoint, uid);
        marker.setTitle("좌표 마커");
        marker.setMetaString("callsign",
            "좌표 " + latitude + ", " + longitude);
        marker.setType("a-f-G-E-V-A"); // Standard marker type
        marker.setAlwaysShowText(true);

        // Get MapView and add marker to the map
        MapView mapView = MapView.getMapView();
        if (mapView != null) {
            mapView.getRootGroup().addItem(marker);

            // Pan the map to the new marker location
            mapView.getMapController().panTo(geoPoint, true);
        }
    } catch (Exception e) {
        Toast.makeText(pluginContext,
            "마커 생성 실패: " + e.getMessage(),
            Toast.LENGTH_SHORT).show();
    }
}
```

**코드 설명**

| 단계 | 코드 | 설명 |
|------|------|------|
| 1 | `new GeoPoint(latitude, longitude)` | 위도/경도로 좌표 객체 생성 |
| 2 | `UUID.randomUUID().toString()` | 마커 고유 ID 생성 |
| 3 | `new Marker(geoPoint, uid)` | 마커 객체 생성 |
| 4 | `marker.setTitle()` | 마커 제목 설정 |
| 5 | `marker.setMetaString()` | 메타데이터 설정 |
| 6 | `marker.setType()` | 마커 타입 설정 (a-f-G-E-V-A) |
| 7 | `marker.setAlwaysShowText(true)` | 텍스트 항상 표시 |
| 8 | `getRootGroup().addItem(marker)` | 지도에 마커 추가 |
| 9 | `panTo(geoPoint, true)` | 마커 위치로 지도 이동 |

---

## 중요한 개발 포인트

### 1. 마커를 지도에 추가하는 올바른 방법

**잘못된 방법 (컴파일 에러 발생)**

```java
marker.addToGroup(mapView.getRootGroup());
```

**에러 메시지**
```
error: method addToGroup in class Marker cannot be applied to given types;
  required: String[],Marker
  found:    RootMapGroup
```

**올바른 방법**

```java
mapView.getRootGroup().addItem(marker);
```

**이유**: `Marker.addToGroup()` 메서드의 시그니처는 `addToGroup(String[], Marker)`이므로 `RootMapGroup`을 직접 전달할 수 없습니다.

---

### 2. ATAK API 클래스 사용

**사용한 주요 클래스**

| 패키지 | 클래스 | 용도 |
|--------|--------|------|
| com.atakmap.android.maps | Marker | ATAK의 마커 클래스 |
| com.atakmap.coremap.maps.coords | GeoPoint | 좌표 클래스 |
| com.atakmap.android.maps | MapView | 지도 뷰 접근 |

**주의사항**

`gov.tak.api.*` 패키지가 아닌 `com.atakmap.*` 패키지를 사용해야 합니다.

---

### 3. 버전 호환성 문제

**문제 상황**

플러그인 설치 시 다음과 같은 에러 발생:
```
this plugin requires software version: 5.5.0.CIV but you are running: 5.5.1.CIV
```

**원인**

`build.gradle`의 `ATAK_VERSION`과 실제 ATAK 앱 버전 불일치

**해결 방법**

1. ATAK 앱의 버전 확인 (설정 → 앱 정보)
2. `build.gradle`의 `ATAK_VERSION` 수정
3. 플러그인 재빌드
4. 새 APK 재설치

**버전 확인 방법**
- ATAK APK가 있는 폴더의 `VERSION.txt` 파일 확인
- 또는 ATAK 앱 내 설정에서 확인

---

### 4. 플러그인 로드 실패 문제 ⭐ **가장 흔한 오류**

**문제 상황**

플러그인 설치 후 로드 시 다음과 같은 에러 발생:
```
Failed to load Plugin Template
```

**원인**

`plugin.xml` 파일의 클래스 경로(`impl`)가 업데이트되지 않음

**상세 설명**

패키지명을 변경할 때 다음 파일들을 모두 수정해야 합니다:
1. ✅ Java 파일의 `package` 선언
2. ✅ 디렉토리 구조
3. ✅ `build.gradle`의 `namespace`
4. ❌ **`plugin.xml`의 `impl` 경로** ← 이것을 놓치기 쉬움!

**해결 방법**

`plugin.xml` 파일 수정:
```xml
<!-- 잘못된 경로 -->
<extension
    impl="com.atakmap.android.plugintemplate.plugin.PluginTemplate" />

<!-- 올바른 경로 -->
<extension
    impl="com.atakmap.android.coordinatemarker.plugin.PluginTemplate" />
```

**중요**:
- 이 파일은 ATAK이 플러그인의 진입점을 찾는 데 사용됩니다
- 경로가 틀리면 ATAK이 클래스를 찾을 수 없어서 로드 실패
- 패키지명 변경 시 **반드시** 이 파일도 함께 수정해야 함

**디버깅 팁**

플러그인 로드 실패 시 확인할 사항:
1. `plugin.xml`의 `impl` 경로가 실제 클래스 경로와 일치하는지
2. Java 파일의 `package` 선언과 일치하는지
3. APK를 다시 빌드했는지 (파일 수정 후 재빌드 필수)
4. 핸드폰에서 이전 버전을 완전히 삭제했는지

---

### 5. 앱 크래시 문제 (LayoutInflater와 Context 문제) ⭐ **매우 중요**

**문제 상황**

플러그인 버튼을 클릭하면 앱이 즉시 종료됨 (크래시)

**원인 1: 잘못된 Context 사용**

`pluginContext`로 AlertDialog를 생성하려고 시도

**원인 2: 일반 LayoutInflater 사용 (더 흔한 문제)**

`LayoutInflater.from(context)`로 플러그인 레이아웃을 inflate하면 리소스를 찾지 못함

**상세 설명**

Android에는 두 가지 주요 Context 타입이 있습니다:
1. **Application Context** (`pluginContext`) - 앱 전체의 생명주기
2. **Activity Context** - 화면(Activity)의 생명주기

AlertDialog, Window 등 UI 컴포넌트는 **Activity Context**가 필요합니다.

또한, **플러그인 리소스는 `PluginLayoutInflater`로만 올바르게 로드됩니다.**

**잘못된 코드** (크래시 발생)

```java
private void showCoordinateInputDialog() {
    // ❌ 잘못된 방법 1: pluginContext로 inflate
    View dialogView = LayoutInflater.from(pluginContext)
        .inflate(R.layout.coordinate_input_dialog, null);

    // ❌ 잘못된 방법 2: pluginContext로 Dialog 생성
    new AlertDialog.Builder(pluginContext)
        .setTitle("좌표 입력")
        .show();
}
```

**발생 가능한 에러 메시지**
```
WindowManager$BadTokenException: Unable to add window
또는
ResourceNotFoundException: Resource ID #0x7f0a...
```

**올바른 코드** (정상 작동) ✅

```java
private void showCoordinateInputDialog() {
    try {
        // 1. MapView에서 Activity Context 가져오기
        MapView mapView = MapView.getMapView();
        if (mapView == null) {
            Toast.makeText(pluginContext, "지도를 찾을 수 없습니다",
                Toast.LENGTH_SHORT).show();
            return;
        }

        Context mapContext = mapView.getContext();  // ✅ Activity Context

        // 2. PluginLayoutInflater 사용 (중요!)
        View dialogView = PluginLayoutInflater.inflate(pluginContext,
                R.layout.coordinate_input_dialog, null);  // ✅

        EditText latitudeInput = dialogView.findViewById(R.id.latitude_input);
        EditText longitudeInput = dialogView.findViewById(R.id.longitude_input);

        // 3. mapContext로 Dialog 생성
        new AlertDialog.Builder(mapContext)  // ✅ 올바른 Context
            .setTitle("좌표 입력")
            .setView(dialogView)
            .setPositiveButton("확인", (dialog, which) -> { /* ... */ })
            .setNegativeButton("취소", null)
            .show();

    } catch (Exception e) {
        // 4. 에러 처리 (크래시 방지)
        Toast.makeText(pluginContext,
            "에러: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
            Toast.LENGTH_LONG).show();
        e.printStackTrace();
    }
}
```

**해결 방법 요약**

| 작업 | 올바른 방법 | 잘못된 방법 |
|------|------------|------------|
| 플러그인 레이아웃 inflate | `PluginLayoutInflater.inflate(pluginContext, ...)` | `LayoutInflater.from(...).inflate(...)` |
| AlertDialog 생성 | `new AlertDialog.Builder(mapContext)` | `new AlertDialog.Builder(pluginContext)` |
| 리소스 접근 | `pluginContext.getString()` | - |
| Toast 표시 | `Toast.makeText(pluginContext, ...)` | - |

**핵심 규칙**
1. **플러그인 리소스 로드** → `PluginLayoutInflater` 사용 (필수!)
2. **Dialog 생성** → `MapView.getMapView().getContext()` 사용 (Activity Context)
3. **에러 처리** → 전체를 try-catch로 감싸서 크래시 방지
4. **리소스 접근** → `pluginContext` 사용 가능

---

## 빌드 및 실행

### 빌드 명령어

```bash
cd samples/coordinatemarker
./gradlew assembleCivDebug
```

### 생성된 APK 위치

```
samples/coordinatemarker/app/build/outputs/apk/civ/debug/
ATAK-Plugin-coordinatemarker-0.1--5.5.1-civ-debug.apk
```

**참고**: APK 파일명에 ATAK 버전(5.5.1)이 포함됩니다.

### 빌드 결과

```
BUILD SUCCESSFUL in 3s
34 actionable tasks: 7 executed, 27 up-to-date
```

---

## 플러그인 사용 방법

### 설치 및 실행

1. **설치**: APK를 ATAK이 설치된 Android 기기에 설치
2. **실행**: ATAK 실행 시 플러그인 자동 로드

### 사용 방법

1. ATAK 툴바에서 플러그인 버튼 클릭
2. 다이얼로그에 위도/경도 입력
   - 예: 위도 37.5665, 경도 126.9780 (서울)
3. "확인" 버튼 클릭
4. 지도에 마커가 생성되고 해당 위치로 이동

---

## 최종 파일 구조

```
samples/coordinatemarker/
├── app/
│   ├── build.gradle                          # 패키지명, ATAK 버전 수정
│   └── src/main/
│       ├── assets/
│       │   └── plugin.xml                    # ⭐ 플러그인 진입점 (impl 경로 수정)
│       ├── java/com/atakmap/android/coordinatemarker/plugin/
│       │   ├── PluginTemplate.java           # 메인 로직 구현
│       │   └── PluginNativeLoader.java       # 패키지명만 수정
│       ├── res/
│       │   ├── layout/
│       │   │   └── coordinate_input_dialog.xml   # 새로 생성한 UI
│       │   └── values/
│       │       └── strings.xml               # 플러그인 이름/설명 수정
│       └── AndroidManifest.xml               # 원본 유지
└── build/outputs/apk/civ/debug/
    └── ATAK-Plugin-coordinatemarker-0.1--5.5.1-civ-debug.apk
```

**수정한 파일 요약**

| 파일 | 위치 | 수정 내용 |
|------|------|----------|
| build.gradle | app/build.gradle:12, 109 | ATAK 버전, 패키지명 |
| PluginTemplate.java | .../plugin/PluginTemplate.java:2 | 패키지 선언, 전체 로직 |
| PluginNativeLoader.java | .../plugin/PluginNativeLoader.java:2 | 패키지 선언만 |
| plugin.xml ⭐ | app/src/main/assets/plugin.xml:7 | impl 클래스 경로 |
| strings.xml | app/src/main/res/values/strings.xml | 앱 이름, 설명 |
| coordinate_input_dialog.xml | app/src/main/res/layout/ | 새로 생성 |
```

---

## 핵심 학습 포인트

### 1. ATAK 플러그인 구조
- `IPlugin` 인터페이스 구현
- `IServiceController`로 서비스 접근
- `ToolbarItem`으로 UI 통합

### 2. Android UI 통합
- `AlertDialog`로 사용자 입력 받기
- **`PluginLayoutInflater`로 플러그인 레이아웃 인플레이션** (필수!)
- Activity Context vs Application Context 이해
- `Toast`로 사용자 피드백

### 3. ATAK 지도 API
- `MapView.getMapView()`로 지도 접근
- `GeoPoint`로 좌표 표현
- `Marker` 객체 생성 및 설정
- `getRootGroup().addItem()`로 마커 추가

### 4. 데이터 검증
- 좌표 범위 검증 (위도: -90~90, 경도: -180~180)
- `try-catch`로 숫자 변환 오류 처리

### 5. 사용자 경험
- 입력 후 자동으로 지도 이동
- Toast 메시지로 작업 결과 알림
- 예외 상황에 대한 명확한 메시지

### 6. 에러 처리 및 디버깅
- 전체 try-catch로 크래시 방지
- 에러 메시지를 Toast로 표시
- `e.printStackTrace()`로 로그 출력
- 방어적 프로그래밍 (null 체크 등)

---

## 추가 개선 아이디어

### 기능 확장
- 여러 좌표를 한 번에 입력
- 마커 색상/아이콘 선택
- 이전에 입력한 좌표 목록 저장
- 마커 클릭 시 상세 정보 표시

### UI 개선
- 지도를 직접 클릭하여 좌표 선택
- MGRS, UTM 등 다양한 좌표 형식 지원
- 현재 위치를 기본값으로 설정

### 데이터 관리
- SharedPreferences로 마커 목록 저장
- 마커 내보내기/가져오기 기능
- 다른 ATAK 사용자와 마커 공유

---

## 참고 자료

### 코드 참조
- HelloWorld 샘플: `samples/helloworld/`
- 플러그인 템플릿: `samples/plugintemplate/`

### 문서
- ATAK Plugin Development Guide: `ATAK_Plugin_Development_Guide.pdf`
- ATAK Javadoc: `atak-javadoc.jar`

### 주요 파일 위치
- 메인 로직: `PluginTemplate.java:86-148`
- UI 레이아웃: `coordinate_input_dialog.xml`
- 빌드 설정: `app/build.gradle:109`
