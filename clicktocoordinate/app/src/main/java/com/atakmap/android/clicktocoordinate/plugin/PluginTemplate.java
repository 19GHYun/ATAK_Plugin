package com.atakmap.android.clicktocoordinate.plugin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.atak.plugins.impl.PluginContextProvider;
import com.atak.plugins.impl.PluginLayoutInflater;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.PaneBuilder;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import android.graphics.PointF;
import java.util.UUID;

public class PluginTemplate implements IPlugin {

    IServiceController serviceController;
    Context pluginContext;
    IHostUIService uiService;
    ToolbarItem toolbarItem;
    Pane templatePane;
    private MapEventDispatcher.MapEventDispatchListener mapClickListener;
    private boolean isClickModeEnabled = false;

    public PluginTemplate(IServiceController serviceController) {
        this.serviceController = serviceController;
        final PluginContextProvider ctxProvider = serviceController
                .getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            pluginContext = ctxProvider.getPluginContext();
            pluginContext.setTheme(R.style.ATAKPluginTheme);
        }

        // obtain the UI service
        uiService = serviceController.getService(IHostUIService.class);

        // initialize the toolbar button for the plugin

        // create the button
        toolbarItem = new ToolbarItem.Builder(
                pluginContext.getString(R.string.app_name),
                MarshalManager.marshal(
                        pluginContext.getResources().getDrawable(R.drawable.ic_launcher),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        toggleClickMode();
                    }
                })
                .build();

        // Initialize map click listener
        setupMapClickListener();
    }

    @Override
    public void onStart() {
        // the plugin is starting, add the button to the toolbar
        if (uiService == null)
            return;

        uiService.addToolbarItem(toolbarItem);
    }

    @Override
    public void onStop() {
        // the plugin is stopping, remove the button from the toolbar
        if (uiService == null)
            return;

        // Disable click mode and remove listener
        disableClickMode();

        uiService.removeToolbarItem(toolbarItem);
    }

    private void setupMapClickListener() {
        mapClickListener = new MapEventDispatcher.MapEventDispatchListener() {
            @Override
            public void onMapEvent(MapEvent event) {
                if (!isClickModeEnabled) {
                    return;
                }

                if (event.getType().equals(MapEvent.MAP_CLICK)) {
                    PointF pointF = event.getPointF();
                    if (pointF != null) {
                        MapView mapView = MapView.getMapView();
                        if (mapView != null) {
                            GeoPointMetaData geoPointMetaData = mapView.inverseWithElevation(pointF.x, pointF.y);
                            GeoPoint point = geoPointMetaData.get();
                            addMarkerAtCoordinate(point.getLatitude(), point.getLongitude());
                            Toast.makeText(pluginContext,
                                String.format("마커 추가: %.6f, %.6f", point.getLatitude(), point.getLongitude()),
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        };
    }

    private void toggleClickMode() {
        if (isClickModeEnabled) {
            disableClickMode();
        } else {
            enableClickMode();
        }
    }

    private void enableClickMode() {
        MapView mapView = MapView.getMapView();
        if (mapView != null && mapClickListener != null) {
            mapView.getMapEventDispatcher().addMapEventListener(MapEvent.MAP_CLICK, mapClickListener);
            isClickModeEnabled = true;
            Toast.makeText(pluginContext, "지도 클릭 모드 활성화", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableClickMode() {
        MapView mapView = MapView.getMapView();
        if (mapView != null && mapClickListener != null) {
            mapView.getMapEventDispatcher().removeMapEventListener(MapEvent.MAP_CLICK, mapClickListener);
            isClickModeEnabled = false;
            Toast.makeText(pluginContext, "지도 클릭 모드 비활성화", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPane() {
        // Show coordinate input dialog
        showCoordinateInputDialog();
    }

    private void showCoordinateInputDialog() {
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
                    R.layout.coordinate_input_dialog, null);

            EditText latitudeInput = dialogView.findViewById(R.id.latitude_input);
            EditText longitudeInput = dialogView.findViewById(R.id.longitude_input);

            // Build and show dialog
            new AlertDialog.Builder(mapContext)
                .setTitle("좌표 입력")
                .setView(dialogView)
                .setPositiveButton("확인", (dialog, which) -> {
                    try {
                        double latitude = Double.parseDouble(latitudeInput.getText().toString());
                        double longitude = Double.parseDouble(longitudeInput.getText().toString());

                        // Validate coordinates
                        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                            Toast.makeText(pluginContext, "잘못된 좌표입니다", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Add marker at coordinates
                        addMarkerAtCoordinate(latitude, longitude);
                        Toast.makeText(pluginContext, "마커가 추가되었습니다", Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        Toast.makeText(pluginContext, "숫자를 입력해주세요", Toast.LENGTH_SHORT).show();
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

    private void addMarkerAtCoordinate(double latitude, double longitude) {
        try {
            // Create GeoPoint with the provided coordinates
            GeoPoint geoPoint = new GeoPoint(latitude, longitude);

            // Create a unique ID for the marker
            String uid = UUID.randomUUID().toString();

            // Create the marker
            Marker marker = new Marker(geoPoint, uid);
            marker.setTitle("좌표 마커");
            marker.setMetaString("callsign", "좌표 " + latitude + ", " + longitude);
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
            Toast.makeText(pluginContext, "마커 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
