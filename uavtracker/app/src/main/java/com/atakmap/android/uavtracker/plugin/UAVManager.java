package com.atakmap.android.uavtracker.plugin;

import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * UAV 마커들을 관리하는 클래스
 */
public class UAVManager {

    private static UAVManager instance;
    private Map<String, Marker> uavMarkers;  // UID -> Marker 매핑
    private Map<String, UAVInfo> uavInfoMap;  // UID -> UAVInfo 매핑

    private UAVManager() {
        uavMarkers = new HashMap<>();
        uavInfoMap = new HashMap<>();
    }

    /**
     * 싱글톤 인스턴스 획득
     */
    public static synchronized UAVManager getInstance() {
        if (instance == null) {
            instance = new UAVManager();
        }
        return instance;
    }

    /**
     * UAV 추가
     */
    public void addUAV(UAVInfo uavInfo, Marker marker) {
        uavMarkers.put(uavInfo.getUid(), marker);
        uavInfoMap.put(uavInfo.getUid(), uavInfo);
    }

    /**
     * UAV 제거
     */
    public void removeUAV(String uid) {
        Marker marker = uavMarkers.remove(uid);
        if (marker != null) {
            // 지도에서 마커 제거
            MapView mapView = MapView.getMapView();
            if (mapView != null) {
                mapView.getRootGroup().removeItem(marker);
            }
        }
        uavInfoMap.remove(uid);
    }

    /**
     * 모든 UAV 제거
     */
    public void removeAllUAVs() {
        MapView mapView = MapView.getMapView();
        if (mapView != null) {
            for (Marker marker : uavMarkers.values()) {
                mapView.getRootGroup().removeItem(marker);
            }
        }
        uavMarkers.clear();
        uavInfoMap.clear();
    }

    /**
     * UAV 정보 획득
     */
    public UAVInfo getUAVInfo(String uid) {
        return uavInfoMap.get(uid);
    }

    /**
     * UAV 마커 획득
     */
    public Marker getUAVMarker(String uid) {
        return uavMarkers.get(uid);
    }

    /**
     * 모든 UAV 정보 목록 획득
     */
    public List<UAVInfo> getAllUAVs() {
        return new ArrayList<>(uavInfoMap.values());
    }

    /**
     * UAV 개수
     */
    public int getUAVCount() {
        return uavInfoMap.size();
    }

    /**
     * UAV 위치 업데이트
     */
    public void updateUAVPosition(String uid, double latitude, double longitude, double altitude) {
        UAVInfo info = uavInfoMap.get(uid);
        Marker marker = uavMarkers.get(uid);

        if (info != null && marker != null) {
            info.setLatitude(latitude);
            info.setLongitude(longitude);
            info.setAltitude(altitude);
            info.setTimestamp(System.currentTimeMillis());

            // 마커 위치 업데이트
            GeoPoint newPoint = new GeoPoint(latitude, longitude, altitude);
            marker.setPoint(newPoint);
        }
    }

    /**
     * UAV 위협 레벨 업데이트
     */
    public void updateThreatLevel(String uid, UAVInfo.ThreatLevel level) {
        UAVInfo info = uavInfoMap.get(uid);
        Marker marker = uavMarkers.get(uid);

        if (info != null && marker != null) {
            info.setThreatLevel(level);

            // 위협 레벨에 따라 마커 색상 변경
            updateMarkerColor(marker, level);
        }
    }

    /**
     * 위협 레벨에 따른 마커 색상 변경
     */
    private void updateMarkerColor(Marker marker, UAVInfo.ThreatLevel level) {
        int color;
        switch (level) {
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
    }
}
