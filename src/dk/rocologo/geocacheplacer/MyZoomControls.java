package dk.rocologo.geocacheplacer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ZoomControls;

public class MyZoomControls extends ZoomControls implements OnClickListener {
	
	static final String TAG = "GeocachePlacer.ZoomListener";
	private ZoomControls zoomControls1;
	
	public MyZoomControls(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Integer clickedButton = v.getId();

		
		zoomControls1 = (ZoomControls) findViewById(R.id.zoomControls1);
		zoomControls1.setOnClickListener(this);
		
		Log.d(TAG,"ZoomControls-clickedbutton:"+clickedButton);
		Log.d(TAG,"ZoomControls:"+zoomControls1.toString());
		
	}

}
