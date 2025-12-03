package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.model.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ActivityFragment - sửa lỗi, hỗ trợ cả STEP_COUNTER & STEP_DETECTOR,
 * xử lý baseline đúng, và request ACTIVITY_RECOGNITION nếu cần.
 */
public class ActivityFragment extends Fragment implements OnMapReadyCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "activity_map_view";
    private static final String PREF_RECENT_ACTIVITY = "recent_activity_pref";
    private static final String KEY_RECENT_DISTANCE = "recent_distance";
    private static final String KEY_RECENT_DURATION = "recent_duration";
    private static final String KEY_RECENT_STEPS = "recent_steps";
    private static final String KEY_RECENT_TIMESTAMP = "recent_timestamp";
    private static final String KEY_RECENT_CALORIES = "recent_calories";

    private static final double KCAL_PER_STEP = 0.04;
    private static final float FIXED_ZOOM_LEVEL = 17.5f;

    // GPS Filter Constants
    private static final float MIN_ACCURACY_METERS = 25f;
    private static final float MIN_DISTANCE_METERS = 4f;
    private static final float MAX_SPEED_KMH = 28f; // >28km/h → chắc chắn là xe

    private enum TrackingState {IDLE, RUNNING, PAUSED}
    private TrackingState trackingState = TrackingState.IDLE;

    // Views
    private MapView mapView;
    private GoogleMap googleMap;
    private Polyline routePolyline;
    private Marker liveMarker;
    private Marker startMarker;

    private TextView timerText, stepsText, caloriesText, durationText, distanceText;
    private TextView recentTitleText, recentTimeText, recentCaloriesText, recentDurationText, recentDistanceText;
    private TextView emptyRecentText;
    private CardView recentCard;

    private AppCompatButton startButton, pauseButton, resumeButton, finishButton;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastValidLocation;

    // Step Sensor
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepsAtSessionStart = 0;           // baseline session
    private int totalSensorSteps = 0;             // only meaningful for TYPE_STEP_COUNTER
    private int detectorAccumulatedSteps = 0;     // accumulate for TYPE_STEP_DETECTOR while running
    private boolean stepSensorIsCounter = false;  // true if TYPE_STEP_COUNTER, false if TYPE_STEP_DETECTOR
    private boolean waitingForFirstCounterEvent = false; // used to set baseline if start before sensor event

    // Tracking data
    private final List<LatLng> routePoints = new ArrayList<>();
    private double distanceMeters = 0d;
    private long sessionStartMillis = 0L;
    private long accumulatedTimeMillis = 0L;
    private boolean isFirstLocation = true;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    // Permissions
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ActivityResultLauncher<String> activityRecognitionLauncher;

    // Database
    private DatabaseHelper databaseHelper;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (trackingState == TrackingState.RUNNING) {
                updateStats();
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    private final SensorEventListener stepSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                // TYPE_STEP_COUNTER: event.values[0] = cumulative steps since last boot (float)
                totalSensorSteps = (int) event.values[0];

                // If we are waiting to set baseline (because startSession ran before first sensor event),
                // initialize stepsAtSessionStart now.
                if (waitingForFirstCounterEvent && trackingState == TrackingState.RUNNING) {
                    stepsAtSessionStart = totalSensorSteps;
                    waitingForFirstCounterEvent = false;
                }

                // Update UI only if running (or you can show total ever)
                if (trackingState == TrackingState.RUNNING) {
                    updateStats();
                }
            } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                // TYPE_STEP_DETECTOR: each event is one step (value = 1.0)
                // Note: when using STEP_DETECTOR we must count steps ourselves only while RUNNING
                if (trackingState == TrackingState.RUNNING) {
                    // Some devices may deliver multiple events per onSensorChanged call; loop through values
                    for (int i = 0; i < event.values.length; i++) {
                        if (event.values[i] == 1.0f) {
                            detectorAccumulatedSteps++;
                        }
                    }
                    updateStats();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Optional: handle accuracy changes
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo ActivityResultLauncher trong onCreate
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if ((fineLocationGranted != null && fineLocationGranted)
                            || (coarseLocationGranted != null && coarseLocationGranted)) {
                        enableMyLocationLayer();

                        // Di chuyển tới vị trí hiện tại sau khi có quyền
                        if (googleMap != null) {
                            moveToCurrentLocation();
                        }

                        if (trackingState == TrackingState.RUNNING) {
                            startLocationUpdates();
                        }
                    } else {
                        if (isAdded()) {
                            Toast.makeText(requireContext(),
                                    "Cần cấp quyền vị trí để theo dõi đường chạy!",
                                    Toast.LENGTH_LONG).show();
                            if (trackingState == TrackingState.RUNNING) {
                                pauseSession();
                            }
                        }
                    }
                }
        );

        // Activity recognition (step sensors may require ACTIVITY_RECOGNITION runtime permission on newer Android)
        activityRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {
                        // nếu user từ chối, cảnh báo nhưng vẫn cho phép fallback accelerometer nếu bạn implement
                        Toast.makeText(requireContext(),
                                "Quyền Activity recognition bị từ chối — đếm bước có thể bị giới hạn.",
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_activity, container, false);
        databaseHelper = new DatabaseHelper(requireContext());
        bindViews(view);
        setupButtons();
        setupLocation();
        setupStepSensor();
        initMap(savedInstanceState);
        updateRecentCard();
        updateButtonState();
        updateStats(); // Cập nhật stats ban đầu
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

    private void initMap(Bundle savedInstanceState) {
        if (mapView == null) return;

        Bundle mapBundle = null;
        if (savedInstanceState != null) {
            mapBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);
    }

    private void setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(8000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                for (Location location : result.getLocations()) {
                    if (location != null) handleLocationUpdate(location);
                }
            }
        };
    }

    private void setupStepSensor() {
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            // Prefer TYPE_STEP_COUNTER (low power, cumulative)
            Sensor counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (counter != null) {
                stepSensor = counter;
                stepSensorIsCounter = true;
            } else {
                // fallback to STEP_DETECTOR (event per step)
                Sensor detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                if (detector != null) {
                    stepSensor = detector;
                    stepSensorIsCounter = false;
                } else {
                    // Optionally, one could fallback to accelerometer-based algorithm here
                    Toast.makeText(requireContext(),
                            "Thiết bị không hỗ trợ đếm bước tự động (STEP_COUNTER/STEP_DETECTOR).",
                            Toast.LENGTH_LONG).show();
                    stepSensor = null;
                }
            }
        }
    }

    private void startSession() {
        // Request activity recognition if required (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ uses ACTIVITY_RECOGNITION runtime permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
                // continue after permission result (user may need to press start again)
                Toast.makeText(requireContext(), "Yêu cầu quyền Activity Recognition trước khi bắt đầu", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        // Kiểm tra cảm biến bước chân
        if (stepSensor == null) {
            Toast.makeText(requireContext(),
                    "Không thể bắt đầu: Thiết bị không hỗ trợ đếm bước",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Reset các giá trị session
        resetSession();

        trackingState = TrackingState.RUNNING;
        sessionStartMillis = System.currentTimeMillis();
        accumulatedTimeMillis = 0L;

        // Nếu dùng STEP_COUNTER: chúng ta cần baseline = current cumulative value khi bắt đầu session.
        // Nhưng có thể sensor chưa gửi event ngay — do đó nếu totalSensorSteps == 0 (chưa có event),
        // đánh dấu waitingForFirstCounterEvent để set baseline khi event đến.
        if (stepSensorIsCounter) {
            if (totalSensorSteps > 0) {
                stepsAtSessionStart = totalSensorSteps;
                waitingForFirstCounterEvent = false;
            } else {
                // sẽ được set khi onSensorChanged lần đầu
                waitingForFirstCounterEvent = true;
            }
        } else {
            // TYPE_STEP_DETECTOR: zero accumulated at session start
            detectorAccumulatedSteps = 0;
            stepsAtSessionStart = 0;
            waitingForFirstCounterEvent = false;
        }

        // register sensor listener
        if (sensorManager != null && stepSensor != null) {
            sensorManager.registerListener(stepSensorListener, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }

        startLocationUpdates();
        timerHandler.post(timerRunnable);
        updateButtonState();
        updateStats();

        Toast.makeText(requireContext(), "Đã bắt đầu theo dõi", Toast.LENGTH_SHORT).show();
    }

    private void pauseSession() {
        if (trackingState == TrackingState.RUNNING) {
            accumulatedTimeMillis += System.currentTimeMillis() - sessionStartMillis;
        }
        trackingState = TrackingState.PAUSED;

        // Khi pause, ta có thể un-register listener để tiết kiệm pin (vì session tạm dừng)
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepSensorListener);
        }

        stopLocationUpdates();
        timerHandler.removeCallbacks(timerRunnable);
        updateButtonState();
        updateStats();

        Toast.makeText(requireContext(), "Đã tạm dừng", Toast.LENGTH_SHORT).show();
    }

    private void resumeSession() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        if (stepSensor == null) {
            Toast.makeText(requireContext(),
                    "Không thể tiếp tục: Thiết bị không hỗ trợ đếm bước",
                    Toast.LENGTH_LONG).show();
            return;
        }

        trackingState = TrackingState.RUNNING;
        sessionStartMillis = System.currentTimeMillis();

        // Nếu TYPE_STEP_COUNTER: baseline (stepsAtSessionStart) phải được set dựa trên hiện tại
        // để tránh nhảy số khi resume. Cách đơn giản: set stepsAtSessionStart = current cumulative - alreadyCountedThisSession
        if (stepSensorIsCounter) {
            // if totalSensorSteps is available, adjust baseline so that getCurrentSteps() continues from previous value
            if (totalSensorSteps > 0) {
                // currentStepsAlready = previousSavedStepsFromThisSession = what getCurrentSteps() returned before pause
                // We already preserved stepsAtSessionStart at session start, and detectorAccumulatedSteps for detector.
                // To avoid jumps, we don't change stepsAtSessionStart here.
            } else {
                // If we still haven't received any counter event, mark waiting to set baseline when event arrives
                waitingForFirstCounterEvent = true;
            }
        } else {
            // TYPE_STEP_DETECTOR: nothing special; detectorAccumulatedSteps retains previous count
        }

        // register sensor listener again
        if (sensorManager != null && stepSensor != null) {
            sensorManager.registerListener(stepSensorListener, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }

        startLocationUpdates();
        timerHandler.post(timerRunnable);
        updateButtonState();

        Toast.makeText(requireContext(), "Đã tiếp tục", Toast.LENGTH_SHORT).show();
    }

    private void finishSession() {
        if (trackingState == TrackingState.RUNNING) {
            accumulatedTimeMillis += System.currentTimeMillis() - sessionStartMillis;
        }

        trackingState = TrackingState.IDLE;

        // Unregister sensor to save resources
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepSensorListener);
        }

        stopLocationUpdates();
        timerHandler.removeCallbacks(timerRunnable);

        // Lưu session nếu đủ điều kiện
        if (distanceMeters >= 50 && getCurrentDuration() >= 30000) {
            saveRecentSession();
            updateRecentCard();
            syncSessionToDailySteps();
            Toast.makeText(requireContext(),
                    "Đã lưu hoạt động: " + formatDistance(distanceMeters),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(),
                    "Hoạt động quá ngắn, không được lưu",
                    Toast.LENGTH_SHORT).show();
        }

        // reset session state but keep last saved recent session visible
        resetSession();
        updateButtonState();
        updateStats();
    }

    /**
     * Đồng bộ session hiện tại vào bảng steps (DatabaseHelper) để dữ liệu
     * ở màn Home / Report / Achievement luôn khớp với Activity.
     */
    private void syncSessionToDailySteps() {
        if (!isAdded() || databaseHelper == null) return;

        int steps = getCurrentSteps();
        if (steps <= 0) return;

        double calories = steps * KCAL_PER_STEP;
        // distanceMeters đang là mét, convert sang km để khớp cách lưu hiện tại (HomeFragment dùng km)
        double distanceKm = distanceMeters / 1000d;
        long timeMillis = getCurrentDuration(); // HomeFragment cũng đang lưu time ở đơn vị millis

        databaseHelper.addToToday(steps, calories, distanceKm, timeMillis);
    }

    private void resetSession() {
        distanceMeters = 0d;
        accumulatedTimeMillis = 0L;
        sessionStartMillis = 0L;
        routePoints.clear();
        lastValidLocation = null;
        isFirstLocation = true;

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

        // --- CRITICAL FIX for step counters ---
        // Với TYPE_STEP_COUNTER: đặt baseline = totalSensorSteps để UI hiển thị 0 ngay sau finish/reset.
        // Với TYPE_STEP_DETECTOR: giữ detectorAccumulatedSteps = 0.
        if (stepSensorIsCounter) {
            // Nếu chúng ta đã có giá trị cumulative từ sensor, dùng nó làm baseline.
            // Nếu chưa có (totalSensorSteps == 0), chờ event đầu tiên khi start session.
            if (totalSensorSteps > 0) {
                stepsAtSessionStart = totalSensorSteps;
                waitingForFirstCounterEvent = false;
            } else {
                // chưa có event nào tới, giữ behavior: khi start sẽ set baseline khi event đầu tiên tới
                stepsAtSessionStart = 0;
                waitingForFirstCounterEvent = true;
            }
        } else {
            // STEP_DETECTOR: clear bộ đếm session-local
            detectorAccumulatedSteps = 0;
            stepsAtSessionStart = 0;
            waitingForFirstCounterEvent = false;
        }

        // Reset UI
        updateStats();
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (hasLocationPermission()) {
            try {
                fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                );
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(),
                        "Lỗi quyền truy cập vị trí",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLocationUpdate(@NonNull Location location) {
        if (trackingState != TrackingState.RUNNING) return;

        // 1. Lọc độ chính xác
        if (!location.hasAccuracy() || location.getAccuracy() > MIN_ACCURACY_METERS) return;

        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());

        if (lastValidLocation != null) {
            float distance = location.distanceTo(lastValidLocation);

            // 2. Lọc jitter nhỏ
            if (distance < MIN_DISTANCE_METERS) {
                updateLiveMarker(newPoint);
                return;
            }

            // 3. Lọc tốc độ bất thường
            long timeDiff = location.getTime() - lastValidLocation.getTime();
            if (timeDiff > 0) {
                float speedKmh = (distance / timeDiff) * 3.6f;
                if (speedKmh > MAX_SPEED_KMH) return; // Đi xe
            }

            distanceMeters += distance;
        } else {
            addStartMarker(newPoint);
        }

        lastValidLocation = location;
        routePoints.add(newPoint);
        updatePolyline();
        updateLiveMarker(newPoint);
        moveCameraSmooth(newPoint);
        updateStats();
    }

    private void updatePolyline() {
        if (googleMap == null || routePoints.isEmpty()) return;
        if (routePolyline == null) {
            routePolyline = googleMap.addPolyline(new PolylineOptions()
                    .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    .width(14f)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap()));
        }
        routePolyline.setPoints(routePoints);
    }

    private void updateLiveMarker(LatLng point) {
        if (googleMap == null) return;
        if (liveMarker == null) {
            liveMarker = googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Vị trí hiện tại")
                    .flat(true));
        } else {
            liveMarker.setPosition(point);
        }
    }

    private void addStartMarker(LatLng point) {
        if (googleMap != null) {
            startMarker = googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Điểm xuất phát"));
        }
    }

    private void moveCameraSmooth(LatLng point) {
        if (googleMap == null) return;

        CameraPosition position = new CameraPosition.Builder()
                .target(point)
                .zoom(isFirstLocation ? FIXED_ZOOM_LEVEL : googleMap.getCameraPosition().zoom)
                .tilt(30)
                .build();

        if (isFirstLocation) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
            isFirstLocation = false;
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(point), 800, null);
        }
    }

    private void updateStats() {
        long duration = getCurrentDuration();
        int steps = getCurrentSteps();
        double calories = steps * KCAL_PER_STEP;

        timerText.setText(formatElapsedTime(duration));
        durationText.setText(formatElapsedTime(duration));
        stepsText.setText(String.valueOf(steps));
        caloriesText.setText(String.format(Locale.getDefault(), "%.1f", calories));
        distanceText.setText(formatDistance(distanceMeters));
    }

    /**
     * Trả về số bước hiện tại của session.
     * - Nếu dùng STEP_COUNTER: totalSensorSteps - stepsAtSessionStart
     * - Nếu dùng STEP_DETECTOR: detectorAccumulatedSteps
     */
    private int getCurrentSteps() {
        if (stepSensor == null) return 0;

        if (stepSensorIsCounter) {
            // Nếu đang chờ event đầu tiên (waitingForFirstCounterEvent) — chưa có baseline -> 0
            if (waitingForFirstCounterEvent) return 0;
            int result = Math.max(0, totalSensorSteps - stepsAtSessionStart);
            return result;
        } else {
            return Math.max(0, detectorAccumulatedSteps);
        }
    }

    private long getCurrentDuration() {
        if (trackingState == TrackingState.RUNNING) {
            return accumulatedTimeMillis + (System.currentTimeMillis() - sessionStartMillis);
        }
        return accumulatedTimeMillis;
    }

    private void saveRecentSession() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_RECENT_ACTIVITY, Context.MODE_PRIVATE);
        int steps = getCurrentSteps();
        double calories = steps * KCAL_PER_STEP;

        prefs.edit()
                .putFloat(KEY_RECENT_DISTANCE, (float) distanceMeters)
                .putLong(KEY_RECENT_DURATION, getCurrentDuration())
                .putInt(KEY_RECENT_STEPS, steps)
                .putFloat(KEY_RECENT_CALORIES, (float) calories)
                .putLong(KEY_RECENT_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    private void updateRecentCard() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_RECENT_ACTIVITY, Context.MODE_PRIVATE);
        long timestamp = prefs.getLong(KEY_RECENT_TIMESTAMP, 0);
        if (timestamp == 0) {
            recentCard.setVisibility(View.GONE);
            emptyRecentText.setVisibility(View.VISIBLE);
            return;
        }

        recentCard.setVisibility(View.VISIBLE);
        emptyRecentText.setVisibility(View.GONE);

        float dist = prefs.getFloat(KEY_RECENT_DISTANCE, 0);
        long dur = prefs.getLong(KEY_RECENT_DURATION, 0);
        int steps = prefs.getInt(KEY_RECENT_STEPS, 0);
        float calories = prefs.getFloat(KEY_RECENT_CALORIES, 0);

        recentTimeText.setText(new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp)));
        recentDistanceText.setText(formatDistance(dist));
        recentDurationText.setText(formatElapsedTime(dur));
        recentCaloriesText.setText(String.format(Locale.getDefault(), "%.1f", calories));
    }

    private String formatElapsedTime(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    private String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", meters);
        }
        return String.format(Locale.getDefault(), "%.2f km", meters / 1000d);
    }

    private void updateButtonState() {
        startButton.setVisibility(trackingState == TrackingState.IDLE ? View.VISIBLE : View.GONE);
        pauseButton.setVisibility(trackingState == TrackingState.RUNNING ? View.VISIBLE : View.GONE);
        resumeButton.setVisibility(trackingState == TrackingState.PAUSED ? View.VISIBLE : View.GONE);
        finishButton.setVisibility(trackingState == TrackingState.PAUSED ? View.VISIBLE : View.GONE);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocationLayer() {
        if (googleMap == null) return;

        if (hasLocationPermission()) {
            try {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true); // Cho phép nút my location
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            // Tự động request permission khi mở fragment nếu chưa có
            requestLocationPermission();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        enableMyLocationLayer();

        // Thêm padding nếu cần
        googleMap.setPadding(0, 0, 0, 200);

        moveToCurrentLocation();


    }


    @SuppressLint("MissingPermission")
    private void moveToCurrentLocation() {
        if (!hasLocationPermission() || googleMap == null) {
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null && googleMap != null) {
                            LatLng currentLatLng = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(currentLatLng)
                                    .zoom(FIXED_ZOOM_LEVEL)
                                    .tilt(30)
                                    .build();

                            googleMap.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                                    800,
                                    null
                            );
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Có thể log lỗi hoặc show message nhẹ
                        Log.e("ActivityFragment", "Failed to get current location", e);
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void initLocationOnStart() {
        if (hasLocationPermission() && googleMap != null) {
            // Đã có permission và map ready
            moveToCurrentLocation();
        } else if (!hasLocationPermission() && googleMap != null) {
            // Chưa có permission, request và sẽ move sau khi có permission
            requestLocationPermission();
        }
        // Trường hợp map chưa ready sẽ xử lý trong onMapReady
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        // Cập nhật vị trí hiện tại khi quay lại fragment
        if (googleMap != null && hasLocationPermission()) {
            moveToCurrentLocation();
        }

        // Nếu đang RUNNING, cần đảm bảo listener được đăng ký
        if (sensorManager != null && stepSensor != null && trackingState == TrackingState.RUNNING) {
            sensorManager.registerListener(stepSensorListener, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (trackingState == TrackingState.RUNNING) {
            startLocationUpdates();
            timerHandler.post(timerRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        // Nếu fragment ra sau khi app visible, chúng ta có thể unregister để tiết kiệm pin.
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepSensorListener);
        }
        if (trackingState == TrackingState.RUNNING) {
            // giữ state RUNNING nhưng tạm dừng cập nhật (tuỳ UX bạn có muốn auto-pause)
            // code gốc của bạn auto-pause khi thoát màn hình — mình giữ hành vi này:
            pauseSession(); // tự động pause khi thoát màn hình
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
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
        timerHandler.removeCallbacksAndMessages(null);
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepSensorListener);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            Bundle bundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
            if (bundle == null) bundle = new Bundle();
            mapView.onSaveInstanceState(bundle);
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, bundle);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
