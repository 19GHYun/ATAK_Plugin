package com.atakmap.android.uavtracker.plugin;

/**
 * UAV 정보를 저장하는 데이터 클래스
 * Counter-UAS를 위한 UAV 추적 정보
 */
public class UAVInfo {

    // 고유 식별자
    private String uid;

    // 위치 정보
    private double latitude;
    private double longitude;
    private double altitude;  // 미터 단위

    // 이동 정보
    private double speed;     // m/s 단위
    private double heading;   // 0-360도, 북쪽이 0도

    // 메타 정보
    private long timestamp;   // 탐지 시간 (Unix timestamp)
    private ThreatLevel threatLevel;  // 위협 레벨
    private String uavType;   // UAV 종류 (예: "Quadcopter", "Fixed-wing", "Unknown")

    // 위협 레벨 열거형
    public enum ThreatLevel {
        LOW,      // 낮음 - 멀리 떨어져 있음
        MEDIUM,   // 중간 - 주의 필요
        HIGH,     // 높음 - 위협적, 즉시 대응 필요
        UNKNOWN   // 알 수 없음
    }

    /**
     * UAV 정보 생성자
     */
    public UAVInfo(String uid, double latitude, double longitude, double altitude) {
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = 0.0;
        this.heading = 0.0;
        this.timestamp = System.currentTimeMillis();
        this.threatLevel = ThreatLevel.UNKNOWN;
        this.uavType = "Unknown";
    }

    // Getters
    public String getUid() { return uid; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }
    public double getSpeed() { return speed; }
    public double getHeading() { return heading; }
    public long getTimestamp() { return timestamp; }
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public String getUavType() { return uavType; }

    // Setters
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setHeading(double heading) { this.heading = heading; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setThreatLevel(ThreatLevel threatLevel) { this.threatLevel = threatLevel; }
    public void setUavType(String uavType) { this.uavType = uavType; }

    /**
     * UAV 정보를 문자열로 반환
     */
    @Override
    public String toString() {
        return String.format("UAV[%s] Type:%s Pos:(%.6f,%.6f) Alt:%.1fm Speed:%.1fm/s Heading:%.1f° Threat:%s",
            uid.substring(0, Math.min(8, uid.length())),
            uavType,
            latitude,
            longitude,
            altitude,
            speed,
            heading,
            threatLevel);
    }

    /**
     * 고도를 피트 단위로 반환
     */
    public double getAltitudeInFeet() {
        return altitude * 3.28084;  // 미터를 피트로 변환
    }

    /**
     * 속도를 km/h 단위로 반환
     */
    public double getSpeedInKmh() {
        return speed * 3.6;  // m/s를 km/h로 변환
    }

    /**
     * 속도를 knots 단위로 반환
     */
    public double getSpeedInKnots() {
        return speed * 1.94384;  // m/s를 knots로 변환
    }
}
