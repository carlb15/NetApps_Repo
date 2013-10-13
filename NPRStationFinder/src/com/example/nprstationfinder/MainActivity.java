//============================================================================
// Name        : MainActivity.java
// Author      : Carl Barbee
// Description : The Main Activity for the app. It is updated by the AsyncThread
// 							 whenever a user enters a zip code. The static image that is 
//							 displayed on startup is changed to a new image from a website.
//============================================================================
package com.example.nprstationfinder;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
	// All the buttons and views for the application. 
	private Button enterButton_;
	private Button clearButton_;
	private EditText zipCodeData;
	private ImageView stationLogo;
	private String zipCodeText;
	private AsyncThread asyncThread;
	private ListView listView;
	private TextView zipCodeView;
	// All the parameters that will be added to element in the ArrayList.
	private static final String stationName = "NAME";
	private static final String stationFreq = "CALL";
	private static final String stationCity = "CITY";
	private static final String stationSignal = "SIGNAL";

	ArrayList<HashMap<String, String>> radioStationsList = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Sets all of the buttons and layouts for the main activity.
		setContentView(R.layout.activity_main);
		zipCodeView = (TextView) findViewById(R.id.ZipCodeTextView);
		enterButton_ = (Button) findViewById(R.id.EnterButton);
		clearButton_ = (Button) findViewById(R.id.ClearButton);
		zipCodeData = (EditText) findViewById(R.id.ZipCodeUserInput);
		stationLogo = (ImageView) findViewById(R.id.IMAGE);
		listView = (ListView) findViewById(R.id.listView1);
		// Sets the image to an image from the phone.
		stationLogo.setImageResource(R.drawable.staticnprlogo);
		stationLogo.setScaleType(ScaleType.FIT_XY);

		// Create new thread to get the data from the API Web site
		asyncThread = new AsyncThread(this);

		// Start listening for the enter button to be pressed.
		EnterPressedByUser();
		ClearPressedByUser();
	}

	public void updateImage(Drawable image) {
		// Update the image from to an image from a website.
		stationLogo.setImageDrawable(image);
		stationLogo.setScaleType(ScaleType.FIT_XY);
	}

	public void EnterPressedByUser() {
		enterButton_.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (zipCodeData.getText().toString() != "") {
					// Retrieve user inputed zip code data.
					zipCodeText = zipCodeData.getText().toString();
					enterButton_.setEnabled(false);
					// Clear the old list of data stored in the array.
					radioStationsList.clear();
					asyncThread.execute(zipCodeText);
				}
			}
		});
	}

	public void ClearPressedByUser() {
		clearButton_.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Clears the zip code text view to enter another zip code
				zipCodeData.setText("");
				stationLogo.setImageResource(R.drawable.staticnprlogo);
				stationLogo.setScaleType(ScaleType.FIT_XY);
			}
		});
	}

	public void dataFromTheNetwork(
			ArrayList<HashMap<String, String>> newRadioStationsList) {
		radioStationsList = newRadioStationsList;
		// Fills in all the parameters for the list adapter
		ListAdapter adapter = new SimpleAdapter(this, newRadioStationsList,
				R.layout.radiostations, new String[] { stationName, stationFreq,
						stationCity, stationSignal }, new int[] { R.id.NAME, R.id.CALL,
						R.id.CITY, R.id.SIGNAL });

		// Create the new list of radio stations to display to the user.
		listView.setAdapter(adapter);

		// Cancel thread and create a new one.
		asyncThread.cancel(true);
		asyncThread = new AsyncThread(this);
		EnterPressedByUser();
		enterButton_.setEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
