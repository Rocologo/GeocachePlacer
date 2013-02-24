package dk.rocologo.geocacheplacer;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener {

	private String version;
	private Button buttonBack, buttonProblemReport, buttonRateApp;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		try {
			version = " "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = " unknown";
		}
		((TextView) findViewById(R.id.version)).setText(getString(R.string.about_version_label)+version);

		buttonBack = (Button) findViewById(R.id.button_return);
		buttonBack.setOnClickListener(this);
		buttonProblemReport = (Button) findViewById(R.id.button_problem_report);
		buttonProblemReport.setOnClickListener(this);
		buttonRateApp = (Button) findViewById(R.id.button_rate_app);
		buttonRateApp.setOnClickListener(this);
	}

	public void onClick(View v) {
		Integer clickedButton = v.getId();
		if (clickedButton == buttonBack.getId()) {
			finish();
		} 
		else if (clickedButton == buttonProblemReport.getId()) {
			mailButtonClicked(v);
		}
		
		else if (clickedButton == buttonRateApp.getId()) {
			rateButtonClicked(v);
		}
		
	}
	
	/**
	 * Sends email to the developer.
	 */
	public void mailButtonClicked(View view) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "Rocologo@hotmail.com" });
		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.problem_report));
		String usersPhone = Build.MANUFACTURER + " " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ") " + "v"
				+ version + "-" + Locale.getDefault();
		i.putExtra(Intent.EXTRA_TEXT, getString(R.string.problem_report_body, usersPhone));
		startActivity(i);
	}

	/**
	 * Rates app on Google Play.
	 */
	public void rateButtonClicked(View view) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("https://play.google.com/store/apps/details?id=dk.rocologo.geocacheplacer"));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);
	}
}
