package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.pedometer.steptracker.runwalk.dailytrack.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityFragment extends Fragment implements OnMapReadyCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "activity_map_view";
    private static final String PREF_RECENT_ACTIVITY = "recent_activity_pref";
    private static final String KEY_RECENT_DISTANCE = "recent_distance";
    private static final String KEY_RECENT_DURATION = "recent_duration";
    private static final String KEY_RECENT_STEPS = "recent_steps";
    private static final String KEY_RECENT_TIMESTAMP = "recent_timestamp";

    private static final double KM_PER_STEP = 0.0008d;
    private static final double KCAL_PER_STEP = 0.04d;

    private enum TrackingState {IDLE, RUNNING, PAUSED}

    private TrackingState trackingState = TrackingState.IDLE;

    private MapView mapView;
    private GoogleMap googleMap;
    private Polyline routePolyline;
    private Marker liveMarker;
    private Marker startMarker;

    private TextView timerText;
    private TextView stepsText;
    private TextView caloriesText;
    private TextView durationText;
    private TextView distanceText;
    private TextView recentTitleText;
    private TextView recentTimeText;
    private TextView recentCaloriesText;
    private TextView recentDurationText;
    private TextView recentDistanceText;
    private TextView emptyRecentText;
    private CardView recentCard;

    private AppCompatButton startButton;
    private AppCompatButton pauseButton;
    private AppCompatButton resumeButton;
    private AppCompatButton finishButton;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private final List<LatLng> routePoints = new ArrayList<>();
    private double distanceMeters = 0d;
    private long sessionStartMillis = 0L;
    private long accumulatedTimeMillis = 0L;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false))
                        || Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                if (granted) {
                    enableMyLocationLayer();
                    if (trackingState == TrackingState.RUNNING) {
                        startLocationUpdates();
                    }
                } else if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.activity_permission_required, Toast.LENGTH_LONG).show();
                }
            });

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (trackingState == TrackingState.RUNNING) {
                updateStats();
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_activity, container, false);
        bindViews(view);
        setupButtons();
        setupLocationDependencies();
        initMap(savedInstanceState);
        updateStats();
        updateRecentCard();
        updateButtonState();
        return view;
    }

    private void bindViews(View root) {
        mapView = root.findViewById(R.id.activityMapView);
        timerText = root.findViewById(R.id.activityTimerValue);
        stepsText = root.findViewById(R.id.activityStepsValue);
        caloriesText = root.findViewById(R.id.activityCaloriesValue);
        durationText = root.findViewById(R.id.activityDurationValue);
        distanceText = root.findViewById(R.id.activityDistanceValue);

        recentCard = root.findViewById(R.id.activityRecentCard);
        recentTitleText = root.findViewById(R.id.activityRecentTitle);
        recentTimeText = root.findViewById(R.id.activityRecentTime);
        recentCaloriesText = root.findViewById(R.id.activityRecentCalories);
        recentDurationText = root.findViewById(R.id.activityRecentDuration);
        recentDistanceText = root.findViewById(R.id.activityRecentDistance);
        emptyRecentText = root.findViewById(R.id.activityEmptyRecentText);

        startButton = root.findViewById(R.id.activityStartButton);
        pauseButton = root.findViewById(R.id.activityPauseButton);
        resumeButton = root.findViewById(R.id.activityResumeButton);
        finishButton = root.findViewById(R.id.activityFinishButton);
    }

    private void setupButtons() {
        startButton.setOnClickListener(v -> startSession());
        pauseButton.setOnClickListener(v -> pauseSession());
        resumeButton.setOnClickListener(v -> resumeSession());
        finishButton.setOnClickListener(v -> finishSession());
    }

    private void setupLocationDependencies() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1500);

        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(requireActivity());
        settingsClient.checkLocationSettings(settingsRequest);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        handleLocationUpdate(location);
                    }
                }
            }
        };
    }

    private void initMap(Bundle savedInstanceState) {
        if (mapView == null) return;
        Bundle mapBundle = null;
        if (savedInstanceState != null) {
            mapBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);
    }

    private void startSession() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }
        resetSessionData();
        trackingState = TrackingState.RUNNING;
        sessionStartMillis = System.currentTimeMillis();
        updateButtonState();
        updateStats();
        startLocationUpdates();
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void pauseSession() {
        if (trackingState != TrackingState.RUNNING) return;
        accumulatedTimeMillis += System.currentTimeMillis() - sessionStartMillis;
        trackingState = TrackingState.PAUSED;
        stopLocationUpdates();
        timerHandler.removeCallbacks(timerRunnable);
        updateButtonState();
        updateStats();
    }

    private void resumeSession() {
        if (trackingState != TrackingState.PAUSED) return;
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }
        trackingState = TrackingState.RUNNING;
        sessionStartMillis = System.currentTimeMillis();
        startLocationUpdates();
        timerHandler.postDelayed(timerRunnable, 1000);
        updateButtonState();
    }

    private void finishSession() {
        if (trackingState == TrackingState.RUNNING) {
            accumulatedTimeMillis += System.currentTimeMillis() - sessionStartMillis;
        }
        trackingState = TrackingState.IDLE;
        stopLocationUpdates();
        timerHandler.removeCallbacks(timerRunnable);
        saveRecentSession();
        resetSessionData();
        updateStats();
        updateRecentCard();
        updateButtonState();
    }

    private void resetSessionData() {
        distanceMeters = 0d;
        accumulatedTimeMillis = 0L;
        sessionStartMillis = 0L;
        routePoints.clear();
        if (routePolyline != null) {
            routePolyline.remove();
            routePolyline = null;
        }
        if (liveMarker != null) {
            liveMarker.remove();
            liveMarker = null;
        }
        if (startMarker != null) {
            startMarker.remove();
            startMarker = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!hasLocationPermission()) {
            return;
        }
        if (fusedLocationClient == null || locationCallback == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), R.string.activity_tracking_error, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        enableMyLocationLayer();
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void handleLocationUpdate(@NonNull Location location) {
        if (trackingState != TrackingState.RUNNING) {
            return;
        }
        if (location.hasAccuracy() && location.getAccuracy() > 60f) {
            return; // ignore noisy readings
        }
        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
        if (!routePoints.isEmpty()) {
            LatLng lastPoint = routePoints.get(routePoints.size() - 1);
            float[] results = new float[1];
            Location.distanceBetween(lastPoint.latitude, lastPoint.longitude, newPoint.latitude, newPoint.longitude, results);
            if (results[0] < 1f) {
                return; // skip jitter
            }
            distanceMeters += results[0];
        } else {
            addStartMarker(newPoint);
            moveCamera(newPoint);
        }
        routePoints.add(newPoint);
        updatePolyline();
        updateLiveMarker(newPoint);
        updateStats();
    }

    private void updatePolyline() {
        if (googleMap == null || !isAdded()) return;
        if (routePolyline == null) {
            routePolyline = googleMap.addPolyline(new PolylineOptions()
                    .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    .width(12f)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap()));
        }
        routePolyline.setPoints(routePoints);
    }

    private void updateLiveMarker(LatLng point) {
        if (googleMap == null) return;
        if (liveMarker == null) {
            liveMarker = googleMap.addMarker(new MarkerOptions().position(point));
        } else {
            liveMarker.setPosition(point);
        }
        moveCamera(point);
    }

    private void addStartMarker(LatLng point) {
        if (googleMap == null) return;
        startMarker = googleMap.addMarker(new MarkerOptions().position(point));
    }

    private void moveCamera(LatLng point) {
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16f));
        }
    }

    private void updateStats() {
        long durationMillis = getCurrentDuration();
        String durationFormatted = formatElapsedTime(durationMillis);

        timerText.setText(durationFormatted);
        durationText.setText(durationFormatted);
        stepsText.setText(String.valueOf(calculateSteps(distanceMeters)));
        caloriesText.setText(String.format(Locale.getDefault(), "%.2f", calculateCalories(distanceMeters)));
        distanceText.setText(formatDistance(distanceMeters));
    }

    private long getCurrentDuration() {
        if (trackingState == TrackingState.RUNNING) {
            return accumulatedTimeMillis + (System.currentTimeMillis() - sessionStartMillis);
        }
        return accumulatedTimeMillis;
    }

    private void saveRecentSession() {
        if (!isAdded()) return;
        if (distanceMeters <= 0 || accumulatedTimeMillis <= 0) {
            return;
        }
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_RECENT_ACTIVITY, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(KEY_RECENT_DISTANCE, (float) distanceMeters)
                .putLong(KEY_RECENT_DURATION, accumulatedTimeMillis)
                .putInt(KEY_RECENT_STEPS, calculateSteps(distanceMeters))
                .putLong(KEY_RECENT_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    private void updateRecentCard() {
        if (!isAdded()) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_RECENT_ACTIVITY, Context.MODE_PRIVATE);
        float savedDistance = prefs.getFloat(KEY_RECENT_DISTANCE, -1f);
        long savedDuration = prefs.getLong(KEY_RECENT_DURATION, -1L);
        long savedTimestamp = prefs.getLong(KEY_RECENT_TIMESTAMP, 0L);

        if (savedDistance <= 0 || savedDuration <= 0 || savedTimestamp == 0) {
            recentCard.setVisibility(View.GONE);
            emptyRecentText.setVisibility(View.VISIBLE);
            return;
        }

        recentCard.setVisibility(View.VISIBLE);
        emptyRecentText.setVisibility(View.GONE);

        recentTitleText.setText(getString(R.string.recent_activity_placeholder_title));
        recentTimeText.setText(formatRecentTime(savedTimestamp));
        recentCaloriesText.setText(String.format(Locale.getDefault(), "%.2f", calculateCalories(savedDistance)));
        recentDurationText.setText(formatElapsedTime(savedDuration));
        recentDistanceText.setText(formatDistance(savedDistance));
    }

    private String formatRecentTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void updateButtonState() {
        startButton.setVisibility(trackingState == TrackingState.IDLE ? View.VISIBLE : View.GONE);
        pauseButton.setVisibility(trackingState == TrackingState.RUNNING ? View.VISIBLE : View.GONE);
        resumeButton.setVisibility(trackingState == TrackingState.PAUSED ? View.VISIBLE : View.GONE);
        finishButton.setVisibility(trackingState == TrackingState.PAUSED ? View.VISIBLE : View.GONE);
    }

    private String formatElapsedTime(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", 0, minutes, seconds);
        }
    }

    private int calculateSteps(double meters) {
        if (meters <= 0) return 0;
        return (int) Math.max(0, Math.round(meters / 0.8d));
    }

    private double calculateCalories(double meters) {
        return calculateSteps(meters) * KCAL_PER_STEP;
    }

    private String formatDistance(double meters) {
        double km = meters / 1000d;
        return String.format(Locale.getDefault(), "%.2f", km);
    }

    private boolean hasLocationPermission() {
        Context context = getContext();
        if (context == null) return false;
        int fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return fine == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                coarse == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocationLayer() {
        if (googleMap == null || !hasLocationPermission()) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        enableMyLocationLayer();
        if (!routePoints.isEmpty()) {
            updatePolyline();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        if (trackingState == TrackingState.RUNNING) {
            startLocationUpdates();
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        if (trackingState == TrackingState.RUNNING) {
            pauseSession();
        } else {
            stopLocationUpdates();
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        stopLocationUpdates();
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            Bundle mapBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
            if (mapBundle == null) {
                mapBundle = new Bundle();
                outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapBundle);
            }
            mapView.onSaveInstanceState(mapBundle);
        }
    }
}

