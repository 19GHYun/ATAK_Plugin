package com.atakmap.android.uavtracker.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Button;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.assets.Icon;

import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * UAV Tracker Plugin - Counter-UAS 시스템
 * UAV 위치를 추적하고 관리하는 플러그인
 */
public class PluginTemplate implements IPlugin {

    IServiceController serviceController;
    Context pluginContext;
    IHostUIService uiService;
    ToolbarItem toolbarItem;
    ToolbarItem mapToolbarItem;
    UAVManager uavManager;
    VWorldMapComponent vworldMapComponent;

    public PluginTemplate(IServiceController serviceController) {
        this.serviceController = serviceController;
        final PluginContextProvider ctxProvider = serviceController
                .getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            pluginContext = ctxProvider.getPluginContext();
            pluginContext.setTheme(R.style.ATAKPluginTheme);
        }

        // UAV Manager 초기화
        uavManager = UAVManager.getInstance();

        // VWorld Map Component 초기화
        vworldMapComponent = new VWorldMapComponent();

        // obtain the UI service
        uiService = serviceController.getService(IHostUIService.class);

        // initialize the toolbar button for the plugin (UAV 추가)
        toolbarItem = new ToolbarItem.Builder(
                "UAV Tracker",
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        showUAVInputDialog();
                    }
                })
                .build();

        // VWorld 지도 툴바 버튼 생성
        mapToolbarItem = new ToolbarItem.Builder(
                "Korean Map",
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        showVWorldMapDialog();
                    }
                })
                .build();
    }

    @Override
    public void onStart() {
        // the plugin is starting, add the button to the toolbar
        if (uiService == null)
            return;

        uiService.addToolbarItem(toolbarItem);
        uiService.addToolbarItem(mapToolbarItem);

        // VWorld Map Component 시작
        MapView mapView = MapView.getMapView();
        if (mapView != null) {
            vworldMapComponent.onCreate(pluginContext, null, mapView);
        }

        // 마커 클릭 리스너는 각 마커마다 개별 등록됨
    }

    @Override
    public void onStop() {
        // the plugin is stopping, remove the button from the toolbar
        if (uiService == null)
            return;

        uiService.removeToolbarItem(toolbarItem);
        uiService.removeToolbarItem(mapToolbarItem);

        // VWorld Map Component 정리
        MapView mapView = MapView.getMapView();
        if (mapView != null && vworldMapComponent != null) {
            vworldMapComponent.onDestroyImpl(pluginContext, mapView);
        }

        // 모든 UAV 제거
        uavManager.removeAllUAVs();
    }

    /**
     * UAV 입력 다이얼로그 표시
     */
    private void showUAVInputDialog() {
        try {
            // Get MapView's context (Activity context)
            MapView mapView = MapView.getMapView();
            if (mapView == null) {
                Toast.makeText(pluginContext, "지도를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            Context mapContext = mapView.getContext();

            // Create dialog layout using PluginLayoutInflater
            View dialogView = PluginLayoutInflater.inflate(pluginContext,
                    R.layout.uav_input_dialog, null);

            // Get input fields
            EditText latitudeInput = dialogView.findViewById(R.id.uav_latitude_input);
            EditText longitudeInput = dialogView.findViewById(R.id.uav_longitude_input);
            EditText altitudeInput = dialogView.findViewById(R.id.uav_altitude_input);
            EditText speedInput = dialogView.findViewById(R.id.uav_speed_input);
            EditText headingInput = dialogView.findViewById(R.id.uav_heading_input);
            Spinner typeSpinner = dialogView.findViewById(R.id.uav_type_spinner);

            // Setup UAV type spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    pluginContext,
                    R.array.uav_types,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);

            // Build and show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mapContext);
            builder.setTitle("UAV 정보 입력")
                .setView(dialogView)
                .setPositiveButton("UAV 추가", (dialog, which) -> {
                    try {
                        // Parse input values
                        double latitude = Double.parseDouble(latitudeInput.getText().toString());
                        double longitude = Double.parseDouble(longitudeInput.getText().toString());
                        double altitude = Double.parseDouble(altitudeInput.getText().toString());
                        double speed = Double.parseDouble(speedInput.getText().toString());
                        double heading = Double.parseDouble(headingInput.getText().toString());
                        String uavType = typeSpinner.getSelectedItem().toString();

                        // Validate coordinates
                        if (latitude < -90 || latitude > 90) {
                            Toast.makeText(pluginContext, "위도는 -90 ~ 90 사이여야 합니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (longitude < -180 || longitude > 180) {
                            Toast.makeText(pluginContext, "경도는 -180 ~ 180 사이여야 합니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (altitude < 0) {
                            Toast.makeText(pluginContext, "고도는 0 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (heading < 0 || heading > 360) {
                            Toast.makeText(pluginContext, "방향은 0 ~ 360 사이여야 합니다", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Add UAV
                        addUAV(latitude, longitude, altitude, speed, heading, uavType);

                        Toast.makeText(pluginContext,
                            "UAV가 추가되었습니다 (총 " + uavManager.getUAVCount() + "대)",
                            Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        Toast.makeText(pluginContext, "올바른 숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("모두 제거", (dialog, which) -> {
                    int count = uavManager.getUAVCount();
                    uavManager.removeAllUAVs();
                    Toast.makeText(pluginContext,
                        count + "개의 UAV가 제거되었습니다",
                        Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null);

            // 버튼 구성
            AlertDialog dialog = builder.create();
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "모두 제거", (d, which) -> {
                int count = uavManager.getUAVCount();
                uavManager.removeAllUAVs();
                Toast.makeText(pluginContext,
                    count + "개의 UAV가 제거되었습니다",
                    Toast.LENGTH_SHORT).show();
            });

            // UAV 목록이 있으면 "목록 보기" 버튼 추가
            if (uavManager.getUAVCount() > 0) {
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "UAV 목록", (d, which) -> {
                    showUAVListDialog();
                });
            }

            // 3D 뷰 전환 버튼 추가
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "3D 뷰", (d, which) -> {
                toggle3DView();
            });

            dialog.show();

        } catch (Exception e) {
            // Catch any errors and show them
            Toast.makeText(pluginContext,
                "에러: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * 대한민국 중심 좌표 (서울)
     */
    private static final double KOREA_LAT = 37.5665;
    private static final double KOREA_LON = 126.9780;

    /**
     * UAV 추가
     */
    private void addUAV(double latitude, double longitude, double altitude,
                        double speed, double heading, String uavType) {
        try {
            // Create UAV Info
            String uid = "UAV-" + UUID.randomUUID().toString().substring(0, 8);
            UAVInfo uavInfo = new UAVInfo(uid, latitude, longitude, altitude);
            uavInfo.setSpeed(speed);
            uavInfo.setHeading(heading);
            uavInfo.setUavType(uavType);

            // 위협 레벨 자동 판단 (임시 - 고도 기반)
            if (altitude < 50) {
                uavInfo.setThreatLevel(UAVInfo.ThreatLevel.HIGH);
            } else if (altitude < 150) {
                uavInfo.setThreatLevel(UAVInfo.ThreatLevel.MEDIUM);
            } else {
                uavInfo.setThreatLevel(UAVInfo.ThreatLevel.LOW);
            }

            // Create GeoPoint
            GeoPoint geoPoint = new GeoPoint(latitude, longitude, altitude);

            // Create Marker
            Marker marker = new Marker(geoPoint, uid);

            // 상세한 타이틀 설정 (UAV 정보 포함)
            String detailedTitle = String.format("%s\n고도: %.0fm (%.0fft)\n속도: %.1fm/s (%.1fkm/h)\n방향: %.0f°",
                uavType,
                altitude,
                altitude * 3.28084,  // 피트로 변환
                speed,
                speed * 3.6,  // km/h로 변환
                heading);

            marker.setTitle(detailedTitle);
            marker.setMetaString("callsign", uid);
            marker.setMetaString("type", uavType);
            marker.setMetaDouble("altitude", altitude);
            marker.setMetaDouble("speed", speed);
            marker.setMetaDouble("heading", heading);

            // UAV 고유 정보 저장
            marker.setMetaString("uav_uid", uid);

            // UAV 마커 타입 (적대적 UAV)
            marker.setType("a-h-G-U-C");  // Hostile UAV
            marker.setAlwaysShowText(true);

            // 마커 설정
            marker.setClickable(true);

            // UAV가 대한민국 쪽으로 가는지 판단
            boolean headingToKorea = isHeadingToKorea(latitude, longitude, heading);

            // 방향에 따른 마커 아이콘 설정
            try {
                // 자동 아이콘 변경 방지
                marker.setMetaBoolean("adapt_marker_icon", false);

                int iconResId;
                if (headingToKorea) {
                    // 대한민국 쪽으로 오는 경우 - 빨간색 마커
                    iconResId = R.drawable.rd;
                } else {
                    // 대한민국 반대쪽으로 가는 경우 - 파란색 마커
                    iconResId = R.drawable.bd;
                }

                // 커스텀 아이콘 URI 생성
                String iconUri = "android.resource://" + pluginContext.getPackageName() + "/" + iconResId;

                // Icon.Builder를 사용하여 아이콘 생성
                Icon.Builder iconBuilder = new Icon.Builder();
                iconBuilder.setImageUri(Icon.STATE_DEFAULT, iconUri);
                iconBuilder.setAnchor(16, 16);  // 아이콘 중심점 설정
                iconBuilder.setSize(32, 32);    // 아이콘 크기 설정

                // 마커에 아이콘 적용
                marker.setIcon(iconBuilder.build());

            } catch (Exception e) {
                // 아이콘 로드 실패 시 기본 색상 사용
                Toast.makeText(pluginContext,
                    "아이콘 로드 실패: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // 위협 레벨에 따른 색상 설정 (아이콘이 없을 경우 대비)
            int color;
            switch (uavInfo.getThreatLevel()) {
                case HIGH:
                    color = 0xFFFF0000;  // 빨강
                    break;
                case MEDIUM:
                    color = 0xFFFFA500;  // 주황
                    break;
                case LOW:
                    color = 0xFFFFFF00;  // 노랑
                    break;
                default:
                    color = 0xFF808080;  // 회색
                    break;
            }
            marker.setColor(color);

            // Add to map
            MapView mapView = MapView.getMapView();
            if (mapView != null) {
                mapView.getRootGroup().addItem(marker);

                // Pan the map to the new marker location
                mapView.getMapController().panTo(geoPoint, true);
            }

            // UAV Manager에 등록
            uavManager.addUAV(uavInfo, marker);

        } catch (Exception e) {
            Toast.makeText(pluginContext,
                "UAV 추가 실패: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /**
     * UAV 목록 다이얼로그 표시
     */
    private void showUAVListDialog() {
        try {
            MapView mapView = MapView.getMapView();
            if (mapView == null) {
                return;
            }
            Context mapContext = mapView.getContext();

            java.util.List<UAVInfo> uavList = uavManager.getAllUAVs();
            if (uavList.isEmpty()) {
                Toast.makeText(pluginContext, "추적 중인 UAV가 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            // UAV 목록을 문자열 배열로 변환
            String[] uavItems = new String[uavList.size()];
            for (int i = 0; i < uavList.size(); i++) {
                UAVInfo info = uavList.get(i);
                String threatIcon;
                switch (info.getThreatLevel()) {
                    case HIGH: threatIcon = "🔴"; break;
                    case MEDIUM: threatIcon = "🟠"; break;
                    case LOW: threatIcon = "🟡"; break;
                    default: threatIcon = "⚪"; break;
                }
                uavItems[i] = String.format("%s %s - %.0fm, %.1fkm/h",
                    threatIcon,
                    info.getUavType(),
                    info.getAltitude(),
                    info.getSpeedInKmh());
            }

            new AlertDialog.Builder(mapContext)
                .setTitle("UAV 목록 (" + uavList.size() + "대)")
                .setItems(uavItems, (dialog, which) -> {
                    // 선택한 UAV의 상세 정보 표시
                    UAVInfo selectedUAV = uavList.get(which);
                    showUAVDetailDialog(selectedUAV.getUid());
                })
                .setNegativeButton("닫기", null)
                .show();

        } catch (Exception e) {
            Toast.makeText(pluginContext,
                "에러: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * UAV 상세 정보 다이얼로그 표시
     */
    private void showUAVDetailDialog(String uavUid) {
        try {
            UAVInfo uavInfo = uavManager.getUAVInfo(uavUid);
            if (uavInfo == null) {
                Toast.makeText(pluginContext, "UAV 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            MapView mapView = MapView.getMapView();
            if (mapView == null) {
                return;
            }
            Context mapContext = mapView.getContext();

            // 시간 포맷
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timeStr = sdf.format(new Date(uavInfo.getTimestamp()));

            // 위협 레벨 색상
            String threatColor;
            switch (uavInfo.getThreatLevel()) {
                case HIGH:
                    threatColor = "🔴 HIGH (높음)";
                    break;
                case MEDIUM:
                    threatColor = "🟠 MEDIUM (중간)";
                    break;
                case LOW:
                    threatColor = "🟡 LOW (낮음)";
                    break;
                default:
                    threatColor = "⚪ UNKNOWN (알 수 없음)";
                    break;
            }

            // 상세 정보 메시지 구성
            StringBuilder message = new StringBuilder();
            message.append("━━━━━━━━━━━━━━━━━━━━━\n");
            message.append(String.format("🔷 UAV ID: %s\n\n", uavInfo.getUid()));

            message.append("📍 위치 정보\n");
            message.append(String.format("  • 위도: %.6f°\n", uavInfo.getLatitude()));
            message.append(String.format("  • 경도: %.6f°\n", uavInfo.getLongitude()));
            message.append(String.format("  • 고도: %.0fm (%.0fft)\n\n",
                uavInfo.getAltitude(),
                uavInfo.getAltitudeInFeet()));

            message.append("🚁 비행 정보\n");
            message.append(String.format("  • 속도: %.1fm/s (%.1fkm/h, %.1fkt)\n",
                uavInfo.getSpeed(),
                uavInfo.getSpeedInKmh(),
                uavInfo.getSpeedInKnots()));
            message.append(String.format("  • 방향: %.0f° %s\n\n",
                uavInfo.getHeading(),
                getCompassDirection(uavInfo.getHeading())));

            message.append("⚠️ 위협 평가\n");
            message.append(String.format("  • 위협 레벨: %s\n", threatColor));
            message.append(String.format("  • UAV 종류: %s\n\n", uavInfo.getUavType()));

            message.append("🕒 탐지 시간\n");
            message.append(String.format("  • %s\n", timeStr));
            message.append("━━━━━━━━━━━━━━━━━━━━━");

            AlertDialog detailDialog = new AlertDialog.Builder(mapContext)
                .setTitle("UAV 상세 정보")
                .setMessage(message.toString())
                .setPositiveButton("3D로 보기", (dialog, which) -> {
                    // 3D 뷰로 UAV 보기
                    viewUAVIn3D(uavUid);
                })
                .setNeutralButton("위치로 이동", (dialog, which) -> {
                    // UAV 위치로 지도 이동
                    GeoPoint point = new GeoPoint(
                        uavInfo.getLatitude(),
                        uavInfo.getLongitude(),
                        uavInfo.getAltitude());
                    mapView.getMapController().panTo(point, true);
                })
                .setNegativeButton("제거", (dialog, which) -> {
                    // UAV 제거
                    uavManager.removeUAV(uavUid);
                    Toast.makeText(pluginContext,
                        "UAV가 제거되었습니다",
                        Toast.LENGTH_SHORT).show();
                })
                .create();

            detailDialog.show();

        } catch (Exception e) {
            Toast.makeText(pluginContext,
                "에러: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * UAV가 대한민국 쪽으로 향하고 있는지 판단
     * @param uavLat UAV의 위도
     * @param uavLon UAV의 경도
     * @param heading UAV의 heading (0-360도, 북쪽이 0도)
     * @return 대한민국 쪽으로 가면 true, 반대쪽으로 가면 false
     */
    private boolean isHeadingToKorea(double uavLat, double uavLon, double heading) {
        // UAV에서 대한민국(서울)을 향하는 방위각 계산
        double bearingToKorea = calculateBearing(uavLat, uavLon, KOREA_LAT, KOREA_LON);

        // Heading과 bearingToKorea의 차이 계산
        double diff = Math.abs(heading - bearingToKorea);

        // 각도 차이를 0-180도 범위로 정규화
        if (diff > 180) {
            diff = 360 - diff;
        }

        // 차이가 90도 이하이면 대한민국 쪽으로 가는 것으로 판단
        // 90도보다 크면 대한민국 반대쪽으로 가는 것
        return diff <= 90;
    }

    /**
     * 두 지점 사이의 방위각(bearing) 계산
     * @param lat1 시작점 위도
     * @param lon1 시작점 경도
     * @param lat2 목표점 위도
     * @param lon2 목표점 경도
     * @return 방위각 (0-360도, 북쪽이 0도)
     */
    private double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        // 위도/경도를 라디안으로 변환
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lonDiff = Math.toRadians(lon2 - lon1);

        // Bearing 계산 (Haversine 공식 사용)
        double y = Math.sin(lonDiff) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(lonDiff);

        double bearing = Math.toDegrees(Math.atan2(y, x));

        // 0-360도 범위로 정규화
        bearing = (bearing + 360) % 360;

        return bearing;
    }

    /**
     * 각도를 나침반 방향으로 변환
     */
    private String getCompassDirection(double heading) {
        if (heading < 0 || heading > 360) return "?";

        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                               "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(heading / 22.5) % 16;
        return directions[index];
    }

    /**
     * 3D 뷰 전환
     */
    private boolean is3DMode = false;

    private void toggle3DView() {
        try {
            MapView mapView = MapView.getMapView();
            if (mapView == null) {
                Toast.makeText(pluginContext, "지도를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!is3DMode) {
                // 3D 모드로 전환 - 지형이 보이도록 최적화된 설정

                // 1. 카메라 기울이기 (45-70도가 지형을 보기 좋음)
                mapView.getMapController().tiltTo(65, true);

                // 2. 지형 렌더링 활성화 시도
                try {
                    // MapView의 preferences를 통해 3D 지형 활성화
                    android.content.SharedPreferences prefs =
                        android.preference.PreferenceManager.getDefaultSharedPreferences(
                            mapView.getContext());
                    android.content.SharedPreferences.Editor editor = prefs.edit();

                    // ATAK의 3D 지형 관련 설정 활성화
                    editor.putBoolean("atakControlForceThreeDimensionalRendering", true);
                    editor.putBoolean("terrain_enabled", true);
                    editor.putInt("terrain_level", 5);  // 지형 상세도 (0-9)
                    editor.apply();

                } catch (Exception e) {
                    // 설정 실패해도 계속 진행
                    e.printStackTrace();
                }

                // UAV가 있으면 첫 번째 UAV 위치로 이동하며 3D 뷰 적용
                java.util.List<UAVInfo> uavList = uavManager.getAllUAVs();
                if (!uavList.isEmpty()) {
                    UAVInfo firstUAV = uavList.get(0);
                    GeoPoint point = new GeoPoint(
                        firstUAV.getLatitude(),
                        firstUAV.getLongitude(),
                        firstUAV.getAltitude());

                    // 카메라 위치와 각도 설정
                    mapView.getMapController().panTo(point, true);

                    // 3D 뷰에 최적화된 줌 레벨 (너무 가까우면 지형이 안 보임)
                    double scale = mapView.getMapScale();
                    if (scale > 10000) {
                        mapView.getMapController().zoomTo(8000, true);
                    } else if (scale < 2000) {
                        mapView.getMapController().zoomTo(3000, true);
                    }
                }

                is3DMode = true;
                Toast.makeText(pluginContext,
                    "3D 지형 뷰 활성화\n(지형의 고저차가 표시됩니다)\n※ 지형 데이터가 있는 지역에서만 보입니다",
                    Toast.LENGTH_LONG).show();
            } else {
                // 2D 모드로 전환
                mapView.getMapController().tiltTo(0, true);

                try {
                    // 3D 렌더링 설정 해제 (선택사항)
                    android.content.SharedPreferences prefs =
                        android.preference.PreferenceManager.getDefaultSharedPreferences(
                            mapView.getContext());
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("atakControlForceThreeDimensionalRendering", false);
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                is3DMode = false;
                Toast.makeText(pluginContext,
                    "2D 뷰로 전환",
                    Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(pluginContext,
                "3D 뷰 전환 실패: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * UAV를 최적의 3D 각도로 보기
     */
    private void viewUAVIn3D(String uavUid) {
        try {
            UAVInfo uavInfo = uavManager.getUAVInfo(uavUid);
            if (uavInfo == null) {
                return;
            }

            MapView mapView = MapView.getMapView();
            if (mapView == null) {
                return;
            }

            // 3D 뷰로 전환
            mapView.getMapController().tiltTo(65, true);

            // UAV 위치로 이동
            GeoPoint point = new GeoPoint(
                uavInfo.getLatitude(),
                uavInfo.getLongitude(),
                uavInfo.getAltitude());

            mapView.getMapController().panTo(point, true);

            // 고도에 따라 줌 레벨 조정
            double altitude = uavInfo.getAltitude();
            double zoomScale;
            if (altitude < 100) {
                zoomScale = 2000;  // 낮은 고도 - 가까이
            } else if (altitude < 300) {
                zoomScale = 5000;  // 중간 고도
            } else {
                zoomScale = 10000; // 높은 고도 - 멀리
            }
            mapView.getMapController().zoomTo(zoomScale, true);

            is3DMode = true;

            Toast.makeText(pluginContext,
                String.format("3D 뷰: %s (%.0fm)", uavInfo.getUavType(), altitude),
                Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * VWorld 지도 설정 다이얼로그 표시
     */
    private void showVWorldMapDialog() {
        try {
            MapView mapView = MapView.getMapView();
            if (mapView == null) {
                Toast.makeText(pluginContext, "지도를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            Context mapContext = mapView.getContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pluginContext);
            String currentApiKey = prefs.getString("vworld_api_key", "");

            // API 키 입력 필드 생성
            final EditText apiKeyInput = new EditText(mapContext);
            apiKeyInput.setHint("VWorld API 키를 입력하세요");
            apiKeyInput.setText(currentApiKey);
            apiKeyInput.setSingleLine(true);

            // 레이어 타입 선택 스피너
            final Spinner layerSpinner = new Spinner(mapContext);
            String[] layerTypes = {"Base (기본 지도)", "Satellite (위성 영상)", "Hybrid (하이브리드)", "Gray (회색 지도)"};
            ArrayAdapter<String> layerAdapter = new ArrayAdapter<>(
                mapContext,
                android.R.layout.simple_spinner_item,
                layerTypes
            );
            layerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            layerSpinner.setAdapter(layerAdapter);

            // 레이아웃 구성
            android.widget.LinearLayout layout = new android.widget.LinearLayout(mapContext);
            layout.setOrientation(android.widget.LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);
            layout.addView(apiKeyInput);
            layout.addView(layerSpinner);

            new AlertDialog.Builder(mapContext)
                .setTitle("VWorld 한국 지도 설정")
                .setMessage("브이월드(VWorld) API를 사용하여 고품질 한국 지도를 표시합니다.\n\nAPI 키는 VWorld 사이트에서 무료로 발급받을 수 있습니다:\nhttps://www.vworld.kr/dev/v4dv_apihlpko_s001.do")
                .setView(layout)
                .setPositiveButton("적용", (dialog, which) -> {
                    String apiKey = apiKeyInput.getText().toString().trim();

                    if (apiKey.isEmpty()) {
                        Toast.makeText(pluginContext,
                            "API 키를 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 레이어 타입 매핑
                    String layerType;
                    switch (layerSpinner.getSelectedItemPosition()) {
                        case 0: layerType = "BASE"; break;
                        case 1: layerType = "SATELLITE"; break;
                        case 2: layerType = "HYBRID"; break;
                        case 3: layerType = "GRAY"; break;
                        default: layerType = "BASE"; break;
                    }

                    // API 키 저장 및 레이어 적용
                    vworldMapComponent.setApiKey(apiKey);
                    vworldMapComponent.changeLayerType(layerType);

                })
                .setNeutralButton("비활성화", (dialog, which) -> {
                    vworldMapComponent.toggleVWorldLayer(false);
                })
                .setNegativeButton("취소", null)
                .show();

        } catch (Exception e) {
            Toast.makeText(pluginContext,
                "에러: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
