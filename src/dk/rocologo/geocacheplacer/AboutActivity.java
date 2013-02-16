package dk.rocologo.geocacheplacer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutActivity extends Activity implements OnClickListener {

	private Button button1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_dialog);

		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(this);
	}

	public void onClick(View v) {
		Integer clickedButton = v.getId();
		if (clickedButton == button1.getId()) {
			finish();
		}
	}
}
