package su.geocaching.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.AccurateUserLocationManager;
import su.geocaching.android.controller.utils.CoordinateHelper;

public class OdometerView extends LinearLayout {

    private TextView distanceTextView;
    private ImageView startStopButton;

    public OdometerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setOrientation(LinearLayout.HORIZONTAL);
        setBackgroundResource(R.drawable.odometer_bg);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.odometer, this);

        distanceTextView = (TextView) findViewById(R.id.odometer_distance_text);
        startStopButton = (ImageView) findViewById(R.id.odometer_start_stop_button);
        startStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartStopOdometerClick();
            }
        });

        ImageView refreshButton = (ImageView) findViewById(R.id.odometer_refresh_button);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefreshOdometerClick();
            }
        });

        ImageView closeButton = (ImageView) findViewById(R.id.odometer_close_button);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onCloseOdometerClick();
            }
        });
    }

    public void onStartStopOdometerClick() {
        AccurateUserLocationManager.Odometer.setEnabled(!AccurateUserLocationManager.Odometer.isEnabled());
        updateStartStopButton();
    }

    public void onRefreshOdometerClick() {
        AccurateUserLocationManager.Odometer.refresh();
        distanceTextView.setText(CoordinateHelper.distanceToString(0));
    }

    public void onCloseOdometerClick() {
        toggleOdometerVisibility();
    }

    public void toggleOdometerVisibility() {
        AccurateUserLocationManager.Odometer.refresh();
        boolean isOdometerOn = Controller.getInstance().getPreferencesManager().isOdometerOnPreference();
        Controller.getInstance().getPreferencesManager().setOdometerOnPreference(!isOdometerOn);
        AccurateUserLocationManager.Odometer.setEnabled(!isOdometerOn);
        updateView();
    }

    public void updateView() {
        if (Controller.getInstance().getPreferencesManager().isOdometerOnPreference()) {
            this.setVisibility(View.VISIBLE);
            updateDistance();
            updateStartStopButton();
        } else {
            this.setVisibility(View.GONE);
        }
    }

    private void updateStartStopButton() {
        if (AccurateUserLocationManager.Odometer.isEnabled()) {
            startStopButton.setImageResource(R.drawable.ic_pause);
        } else {
            startStopButton.setImageResource(R.drawable.ic_play);
        }
    }

    public void updateDistance() {
        if (distanceTextView.isShown()) {
            distanceTextView.setText(CoordinateHelper.distanceToString(AccurateUserLocationManager.Odometer.getDistance()));
        }
    }
}
