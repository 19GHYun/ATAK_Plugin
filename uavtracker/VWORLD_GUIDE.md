# UAV Tracker - VWorld 한국 지도 통합 가이드

## 개요

UAV Tracker 플러그인에 VWorld(브이월드) 고품질 한국 지도가 통합되었습니다!

## 주요 기능

### 1. 고품질 한국 지도
- ✅ 국토교통부 공식 VWorld API 사용
- ✅ 온라인 실시간 타일 다운로드
- ✅ 4가지 지도 레이어 타입 지원
- ✅ 캐싱으로 빠른 지도 표시

### 2. 지원하는 지도 레이어

| 레이어 타입 | 설명 | 용도 |
|------------|------|------|
| **Base (기본)** | 도로, 건물, 지명이 표시된 기본 지도 | 일반적인 지도 탐색 |
| **Satellite (위성)** | 고해상도 위성 영상 | 실제 지형/건물 확인 |
| **Hybrid (하이브리드)** | 위성 영상 + 도로/지명 라벨 | 위성 영상에 주석 필요 시 |
| **Gray (회색)** | 회색톤 지도 | 마커/오버레이 강조 시 |

## 설치 및 설정

### 1. VWorld API 키 발급

1. **VWorld 웹사이트 방문**
   ```
   https://www.vworld.kr
   ```

2. **회원가입 및 로그인**
   - 무료 계정 생성

3. **API 키 발급**
   - 마이페이지 → API 신청
   - 용도: "ATAK 플러그인 개발"
   - API 타입: "2D 지도 API" 선택

4. **API 키 복사**
   - 발급된 API 키를 안전하게 보관

### 2. 플러그인 빌드

```bash
cd C:\Users\MSI\Desktop\YGH\devlop\ATAK\ATAK-CIV-5.5.1.6-SDK\samples\uavtracker

# Clean 빌드
./gradlew clean

# Debug APK 생성
./gradlew assembleCivDebug
```

### 3. APK 설치

**APK 위치:**
```
app/build/outputs/apk/civ/debug/ATAK-Plugin-uavtracker-*.apk
```

**설치 방법:**
1. 기존 플러그인 완전 삭제
2. ATAK 앱 실행
3. 새 APK 드래그 앤 드롭 또는 adb 설치

## 사용 방법

### 1. 플러그인 실행

1. ATAK 앱 실행
2. 상단 툴바에 "Korean Map" 버튼 클릭

### 2. VWorld 지도 설정

**Korean Map 다이얼로그:**

```
┌──────────────────────────────────┐
│ VWorld 한국 지도 설정             │
├──────────────────────────────────┤
│                                  │
│ 브이월드(VWorld) API를 사용하여   │
│ 고품질 한국 지도를 표시합니다.    │
│                                  │
│ API 키 발급:                     │
│ https://www.vworld.kr/...        │
│                                  │
│ [API 키 입력란]                   │
│ YOUR_API_KEY_HERE                │
│                                  │
│ [레이어 선택]                     │
│ ▼ Base (기본 지도)                │
│                                  │
│ [적용] [비활성화] [취소]          │
└──────────────────────────────────┘
```

### 3. API 키 입력

1. **첫 번째 입력 필드**에 발급받은 API 키 붙여넣기
2. **레이어 타입** 선택:
   - Base (기본 지도) - 권장
   - Satellite (위성 영상)
   - Hybrid (하이브리드)
   - Gray (회색 지도)

3. **[적용]** 버튼 클릭

### 4. 지도 확인

✅ **성공 메시지:**
```
VWorld 지도 적용 완료!
레이어: BASE
```

⚠️ **실패 시:**
- API 키 확인
- 인터넷 연결 확인
- 한국 지역으로 지도 이동 (서울: 37.5°N, 127.0°E)

## 기능 설명

### 📍 지도 타일 시스템

**작동 방식:**
1. 사용자가 지도를 이동하면
2. 현재 뷰포트의 타일 좌표 계산
3. VWorld API로 타일 다운로드
4. 로컬 캐시에 저장 (최대 100개)
5. 지도에 렌더링

**좌표계:**
- SRID: 3857 (Web Mercator)
- 타일 크기: 256x256 픽셀
- 줌 레벨: 6~19

### 🗺️ 한국 영역 최적화

**커버리지:**
```
위도: 33°N ~ 43°N (제주도 ~ 강원도)
경도: 124°E ~ 132°E (서해 ~ 동해)
```

**권장 지역:**
- 서울, 부산, 대구, 인천 등 대도시
- 군사 시설 주변 (DMZ 제외)
- 주요 항만, 공항

### ⚡ 성능 최적화

1. **타일 캐싱**
   - 최근 100개 타일 메모리 캐시
   - 중복 다운로드 방지

2. **비동기 다운로드**
   - 지도 이동 중에도 타일 로드
   - UI 블로킹 없음

3. **네트워크 에러 처리**
   - 타임아웃: 10초
   - 실패 시 재시도 없음 (수동 새로고침)

## 레이어 변경

### 실시간 레이어 전환

1. "Korean Map" 버튼 다시 클릭
2. 다른 레이어 타입 선택
3. [적용] 클릭

**예시:**
```
Base → Satellite 전환
→ 도로 지도가 위성 영상으로 변경됨
```

### 레이어 타입별 특징

#### 🗺️ Base (기본 지도)
- 용도: 일반 탐색, 도로 확인
- 특징: 깔끔한 벡터 스타일
- 속도: 빠름 (파일 크기 작음)

#### 🛰️ Satellite (위성 영상)
- 용도: 실제 지형, 건물 배치 확인
- 특징: 고해상도 항공 사진
- 속도: 느림 (파일 크기 큼)

#### 🌐 Hybrid (하이브리드)
- 용도: 위성 + 주석 필요 시
- 특징: Satellite + 라벨
- 속도: 중간

#### ⬜ Gray (회색)
- 용도: 마커/오버레이 강조
- 특징: 배경 색상 최소화
- 속도: 빠름

## 통합 기능

### UAV와 함께 사용

1. **"Korean Map"**으로 고품질 지도 활성화
2. **"UAV Tracker"**로 UAV 추가
3. 위성 영상 위에 UAV 마커 표시
4. 정확한 위치 확인 가능

**시나리오:**
```
1. VWorld Satellite 레이어 활성화
2. UAV 좌표 입력: 37.5665°N, 126.9780°E (서울)
3. 실제 건물 위치에 UAV 마커 표시됨
```

### 3D 뷰와 결합

```
1. VWorld 지도 활성화
2. UAV 추가
3. "3D 뷰" 버튼 클릭
4. 지형 + UAV를 3D로 확인
```

## 트러블슈팅

### ❌ 지도가 안 보여요

**원인 1: API 키 오류**
```
해결: Korean Map → API 키 재입력
```

**원인 2: 인터넷 연결 끊김**
```
해결: Wi-Fi 또는 데이터 연결 확인
```

**원인 3: 한국 외 지역**
```
해결: 지도를 한국으로 이동
서울: 37.5°N, 127.0°E
```

### ❌ 지도가 느려요

**원인: 대용량 타일 (Satellite 모드)**
```
해결 1: Base 레이어로 변경
해결 2: 줌 아웃 (더 넓은 지역 보기)
해결 3: 캐시 초기화 (앱 재시작)
```

### ❌ HTTP 403 에러

**원인: API 키 만료 또는 할당량 초과**
```
해결:
1. VWorld 웹사이트 로그인
2. API 사용량 확인
3. 필요 시 새 API 키 발급
```

### ❌ 일부 타일만 표시됨

**원인: 네트워크 타임아웃**
```
해결:
1. 지도 이동 (새로고침)
2. 줌 레벨 변경
3. 앱 재시작
```

## 고급 설정

### 코드 수정 (개발자용)

**캐시 크기 변경:**
```java
// VWorldTileContainer.java:38
private final int maxCacheSize = 100;  // 기본값

// 변경 예시:
private final int maxCacheSize = 500;  // 5배 증가
```

**타임아웃 조정:**
```java
// VWorldTileContainer.java:106
connection.setConnectTimeout(5000);   // 5초
connection.setReadTimeout(10000);     // 10초

// 변경 예시:
connection.setConnectTimeout(10000);  // 10초
connection.setReadTimeout(30000);     // 30초
```

**줌 레벨 범위 변경:**
```java
// VWorldTileContainer.java:52
final int MIN_ZOOM = 6;   // 최소
final int MAX_ZOOM = 19;  // 최대

// 변경 예시:
final int MIN_ZOOM = 1;   // 더 넓은 지역
final int MAX_ZOOM = 21;  // 더 상세한 뷰
```

## API 사용량 관리

### VWorld API 제한

- **무료 계정:** 하루 10,000 요청
- **유료 계정:** 더 많은 할당량

### 사용량 계산

**1회 지도 이동 = 약 20~50 타일**
```
예시:
- 서울 전체 보기 (줌 10): ~30 타일
- 건물 단위 (줌 18): ~50 타일
- 하루 사용: ~200~500 타일
```

**절약 팁:**
1. 필요한 지역만 확대
2. 불필요한 줌 인/아웃 최소화
3. 캐시 활용 (같은 지역 재방문)

## 파일 구조

```
uavtracker/
├── app/src/main/java/.../plugin/
│   ├── VWorldTileContainer.java      # 타일 다운로드 + 캐싱
│   ├── VWorldMapComponent.java       # 지도 레이어 관리
│   └── PluginTemplate.java           # UI 통합
│
├── app/src/main/AndroidManifest.xml  # 인터넷 권한
└── VWORLD_GUIDE.md                   # 이 문서
```

## 참고 자료

### VWorld 공식 문서
- 홈페이지: https://www.vworld.kr
- API 가이드: https://www.vworld.kr/dev/v4dv_apihlpko_s001.do
- 지원 센터: 고객센터 문의

### ATAK 문서
- Plugin Guide: `ATAK_Plugin_Development_Guide.pdf`
- Javadoc: `atak-javadoc.jar`

### 관련 샘플
- `customtiles/` - 커스텀 타일 레이어
- `helloworld/` - 온라인 레이어 다운로드

## 라이선스 및 주의사항

### VWorld API 이용약관
- ✅ 비상업적 사용 무료
- ✅ 군사/방위 목적 허용 (한국군)
- ❌ API 키 공개 금지
- ❌ 대량 다운로드 금지

### 데이터 소유권
- 지도 데이터: 국토교통부
- 위성 영상: 항공우주연구원 / 외부 제공자

### 책임 제한
- 실시간 데이터 정확성 보장 안 됨
- 군사 작전 시 공식 지도 병행 사용 권장

## 문의 및 지원

### 버그 리포트
```
파일: VWorldTileContainer.java
에러 메시지: [에러 내용]
재현 단계: [단계별 설명]
```

### 기능 요청
- 추가 레이어 타입
- 오프라인 캐싱
- 3D 건물 데이터

---

**마지막 업데이트:** 2025-10-27
**버전:** 1.0.0
**작성자:** ATAK Plugin Development Team
