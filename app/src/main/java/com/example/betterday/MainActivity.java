package com.example.betterday;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.betterday.ui.block.BlockFragment;
import com.example.betterday.ui.calendar.CalendarFragment;
import com.example.betterday.ui.reminder.ReminderFragment;
import com.example.betterday.ui.statistics.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.betterday.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FloatingActionButton fab;

    public static boolean isNotificationAllowed = false;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for notification permission
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            } else {
                isNotificationAllowed = true;
                setupUI();
            }
        } else {
            isNotificationAllowed = true;
            setupUI();
        }
    }

    private void setupUI() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().hide();

        BottomNavigationView navView = binding.navView;
        navView.setItemIconTintList(null);
        fab = findViewById(R.id.nav_fab);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_reminders, R.id.nav_calendar, R.id.nav_statistics, R.id.nav_block)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();
            if (destinationId == R.id.nav_reminders) {
                changeFabImageResource(R.drawable.ic_add_symbol);
            } else if (destinationId == R.id.nav_calendar) {
                changeFabImageResource(R.drawable.ic_calendar_inactive);
            } else if (destinationId == R.id.nav_statistics) {
                changeFabImageResource(R.drawable.ic_statistics_inactive);
            } else if (destinationId == R.id.nav_block) {
                changeFabImageResource(R.drawable.ic_block_active);
            } else {
                fab.setVisibility(View.GONE);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main))
                        .getChildFragmentManager().getFragments().get(0);
                if (currentFragment instanceof ReminderFragment) {
                    ((ReminderFragment) currentFragment).initializeDialogFirstPage();
                }
            }
        });
    }

    private void changeFabImageResource(int imageResourceId) {
        fab.setImageResource(imageResourceId);
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isNotificationAllowed = true;
                setupUI();
            } else {
                Toast.makeText(this, "Notification permission is required to proceed. Please enable it in settings.", Toast.LENGTH_LONG).show();
                openAppSettings();
                this.finish();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

}


