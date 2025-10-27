package com.atakmap.android.uavtracker.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.atakmap.android.maps.AbstractMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

/**
 * VWorld 지도 설정 관리 컴포넌트
 * API 키를 저장하고 관리합니다
 */
public class VWorldMapComponent extends AbstractMapComponent {

    private static final String TAG = "VWorldMapComponent";
    private static final String PREF_VWORLD_API_KEY = "vworld_api_key";
    private static final String PREF_VWORLD_ENABLED = "vworld_enabled";
    private static final String PREF_VWORLD_LAYER_TYPE = "vworld_layer_type";

    private Context pluginContext;
    private String currentApiKey;
    private String currentLayerType;

    @Override
    public void onCreate(Context context, Intent intent, MapView view) {
        this.pluginContext = context;

        // SharedPreferences에서 설정 로드
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        currentApiKey = prefs.getString(PREF_VWORLD_API_KEY, "");
        boolean enabled = prefs.getBoolean(PREF_VWORLD_ENABLED, false);
        currentLayerType = prefs.getString(PREF_VWORLD_LAYER_TYPE, "BASE");

        if (enabled && !currentApiKey.isEmpty()) {
            Log.d(TAG, "VWorld settings loaded - API Key: " + maskApiKey(currentApiKey));
        }
    }

    /**
     * API 키 일부를 마스킹
     */
    private String maskApiKey(String apiKey) {
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * API 키 설정
     */
    public void setApiKey(String apiKey) {
        this.currentApiKey = apiKey;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pluginContext);
        prefs.edit()
            .putString(PREF_VWORLD_API_KEY, apiKey)
            .putBoolean(PREF_VWORLD_ENABLED, true)
            .apply();

        Log.d(TAG, "API key saved: " + maskApiKey(apiKey));

        Toast.makeText(pluginContext,
            "✅ VWorld API 키 저장 완료!\n\n" +
            "저장된 키: " + maskApiKey(apiKey) + "\n\n" +
            "📝 참고:\n" +
            "완전한 지도 렌더링 기능은\n" +
            "추가 개발이 필요합니다.\n\n" +
            "현재는 설정 저장만 지원됩니다.",
            Toast.LENGTH_LONG).show();
    }

    /**
     * 레이어 타입 변경
     */
    public void changeLayerType(String layerType) {
        this.currentLayerType = layerType;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pluginContext);
        prefs.edit()
            .putString(PREF_VWORLD_LAYER_TYPE, layerType)
            .apply();

        String layerDesc = getLayerDescription(layerType);
        Toast.makeText(pluginContext,
            "✅ 레이어 타입 저장: " + layerType + "\n\n" +
            layerDesc,
            Toast.LENGTH_LONG).show();
    }

    /**
     * 레이어 설명
     */
    private String getLayerDescription(String layerType) {
        switch (layerType.toUpperCase()) {
            case "BASE":
                return "📍 Base: 기본 도로 지도";
            case "SATELLITE":
                return "🛰️ Satellite: 위성 영상";
            case "HYBRID":
                return "🌐 Hybrid: 위성 + 라벨";
            case "GRAY":
                return "⬜ Gray: 회색 지도";
            default:
                return "알 수 없는 레이어";
        }
    }

    /**
     * 설정 상태 토글
     */
    public void toggleVWorldLayer(boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pluginContext);
        prefs.edit().putBoolean(PREF_VWORLD_ENABLED, enabled).apply();

        if (enabled) {
            if (currentApiKey == null || currentApiKey.isEmpty()) {
                Toast.makeText(pluginContext,
                    "먼저 API 키를 설정해주세요",
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(pluginContext,
                    "VWorld 설정이 활성화되었습니다",
                    Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(pluginContext,
                "VWorld 설정이 비활성화되었습니다",
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 현재 설정 확인
     */
    public void showCurrentSettings() {
        if (currentApiKey == null || currentApiKey.isEmpty()) {
            Toast.makeText(pluginContext,
                "❌ VWorld API 키가 설정되지 않았습니다\n\n" +
                "Korean Map 버튼을 눌러\nAPI 키를 입력해주세요.",
                Toast.LENGTH_LONG).show();
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(pluginContext);
            boolean enabled = prefs.getBoolean(PREF_VWORLD_ENABLED, false);

            Toast.makeText(pluginContext,
                "✅ VWorld 설정 정보\n\n" +
                "API 키: " + maskApiKey(currentApiKey) + "\n" +
                "레이어: " + currentLayerType + "\n" +
                "상태: " + (enabled ? "활성화" : "비활성화"),
                Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        // 정리 작업
    }
}
