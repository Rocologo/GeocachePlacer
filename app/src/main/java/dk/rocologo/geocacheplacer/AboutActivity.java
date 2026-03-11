package dk.rocologo.geocacheplacer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class AboutActivity extends AppCompatActivity implements OnClickListener {

    private String version;
    private Button buttonBack, buttonProblemReport, buttonRateApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "ukendt";
        }
        ((TextView) findViewById(R.id.version)).setText(version);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        buttonBack = findViewById(R.id.button_return);
        buttonBack.setOnClickListener(this);
        buttonProblemReport = findViewById(R.id.button_problem_report);
        buttonProblemReport.setOnClickListener(this);
        buttonRateApp = findViewById(R.id.button_rate_app);
        buttonRateApp.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_return) {
            finish();
        } else if (id == R.id.button_problem_report) {
            mailButtonClicked();
        } else if (id == R.id.button_rate_app) {
            rateButtonClicked();
        }
    }

    private void mailButtonClicked() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"Rocologo@hotmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.problem_report));
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL +
                " (Android " + Build.VERSION.RELEASE + ") v" + version + " " + Locale.getDefault();
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.problem_report_body, deviceInfo));
        startActivity(i);
    }

    private void rateButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=dk.rocologo.geocacheplacer"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
