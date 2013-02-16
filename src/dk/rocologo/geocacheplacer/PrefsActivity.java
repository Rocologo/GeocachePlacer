package dk.rocologo.geocacheplacer;

import android.os.Bundle;

public class PrefsActivity extends CompatiblePreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setPrefs(R.xml.prefs);
		super.onCreate(savedInstanceState);
	}

}
