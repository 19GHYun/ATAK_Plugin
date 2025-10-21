# Click to Coordinate Plugin - Development Notes (Korean)

## Overview (개요)
ATAK 지도를 클릭하면 마커를 생성하는 플러그인

## Based On (기반)
- coordinatemarker plugin

## Key Changes (주요 변경사항)

### 1. Package Name Changes (패키지명 변경)
`coordinatemarker`에서 `clicktocoordinate`로 모든 항목 변경:
- PluginTemplate.java - package 선언
- PluginNativeLoader.java - package 선언
- build.gradle - namespace
- plugin.xml - impl 경로 (매우 중요!)
- settings.gradle - rootProject.name
- strings.xml - 앱 이름 및 설명

### 2. Added Imports (추가된 Import)
```java
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import android.graphics.PointF;
```

### 3. Added Fields (추가된 필드)
```java
private MapEventDispatcher.MapEventDispatchListener mapClickListener;
private boolean isClickModeEnabled = false;
```

### 4. Key Methods Added (추가된 주요 메서드)

setupMapClickListener() - 클릭 리스너 생성
toggleClickMode() - 클릭 모드 토글 (켜기/끄기)
enableClickMode() - 리스너 등록 및 활성화
disableClickMode() - 리스너 제거 및 비활성화

### 5. Coordinate Conversion Pattern (좌표 변환 패턴)
```java
PointF pointF = event.getPointF();  // 화면 좌표 획득
GeoPointMetaData geoData = mapView.inverseWithElevation(pointF.x, pointF.y);
GeoPoint point = geoData.get();  // 지리 좌표 획득
```

## Problems Solved (해결한 문제들)

### Problem 1: plugin.xml not updated (plugin.xml 미수정)
증상: "Failed to load Click to Coordinate"
해결: plugin.xml의 impl 경로를 새 패키지명으로 업데이트

### Problem 2: Debug/Release mismatch (Debug/Release 불일치)
증상: "Debug Plugin: Release may be incompatible"
해결: assembleCivDebug로 Debug 빌드

### Problem 3: event.getPoint() doesn't exist (event.getPoint() 메서드 없음)
증상: 컴파일 에러
해결: event.getPointF() 사용 후 inverseWithElevation() 호출

## Build Commands (빌드 명령어)

Debug build (Debug 빌드):
```
./gradlew clean
./gradlew assembleCivDebug
```

Release build (Release 빌드):
```
./gradlew assembleCivRelease
```

## Files Modified (수정한 파일)

CRITICAL (매우 중요):
- plugin.xml - impl 경로

REQUIRED (필수):
- PluginTemplate.java - 모든 로직
- PluginNativeLoader.java - 패키지명
- build.gradle - namespace

RECOMMENDED (권장):
- strings.xml - 이름/설명
- settings.gradle - 프로젝트명

## Usage (사용 방법)

1. 툴바 버튼 클릭 - 클릭 모드 활성화
2. 지도 클릭 - 마커 생성
3. 툴바 버튼 다시 클릭 - 클릭 모드 비활성화

## Output APK Location (생성된 APK 위치)

Debug: app/build/outputs/apk/civ/debug/
Release: app/build/outputs/apk/civ/release/
