//============================================================================
// Name        : AsyncThread.java
// Author      : Carl Barbee
// Description : The Async Thread for the Main Activity. It parses the XML
//							 data from the NPR API website and returns names of the stations
//							 that are near the user-inputted zip code. A static image is 
//							 replaced by a new NPR Logo once the user enters a zip code.
//============================================================================
package com.example.nprstationfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class AsyncThread extends AsyncTask<String, Void, ArrayList<String>> {

	private MainActivity myMainActivity;
	private Drawable image;
	// All the parameters that will be added to element in the ArrayList.
	private static final String stationName = "NAME";
	private static final String stationFreq = "CALL";
	private static final String stationCity = "CITY";
	private static final String stationSignal = "SIGNAL";

	private ArrayList<HashMap<String, String>> myRadioStationList = new ArrayList<HashMap<String, String>>();

	public AsyncThread(MainActivity mainActivity) {
		// Creates a new main activity to update it.
		myMainActivity = mainActivity;
	}

	@Override
	protected void onPostExecute(ArrayList<String> xmlDataFromWebsite) {
		// Updates the imageview to the image from the website.
		myMainActivity.updateImage(image);
		// Sub-thread called for parsing the input data.
		SubAsyncParserThread mySubAsyncParserThread = new SubAsyncParserThread();
		mySubAsyncParserThread.execute(xmlDataFromWebsite);
	};

	private Drawable loadImageFromWeb(String url) {
		// Loads the image from the Website onto the phone.
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			return d;
		}
		catch (Exception e) {
			System.out.println("Exc=" + e);
			return null;
		}
	}

	class SubAsyncParserThread extends
			AsyncTask<ArrayList<String>, Void, ArrayList<HashMap<String, String>>> {

		protected ArrayList<HashMap<String, String>> parseRawXML(
				ArrayList<String> rawXMLData) {

			ArrayList<HashMap<String, String>> myRadioStations = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map = new HashMap<String, String>();

			try {

				for (int i = 0; i < rawXMLData.size(); i++) {

					if (rawXMLData.get(i).contains("<name>")) {
						// Get the station name of the radio station.
						String nameOfStation = rawXMLData.get(i)
								.replaceAll("\\<.*?\\>", "");
						map = new HashMap<String, String>();
						map.put(stationName, "Name: " + nameOfStation);
					}
					else if (rawXMLData.get(i).contains("<frequency>")) {
						// Get the frequency of the station.
						String freqOfStation = rawXMLData.get(i)
								.replaceAll("\\<.*?\\>", "");
						map.put(stationFreq, "Frequency: " + freqOfStation);
					}
					else if (rawXMLData.get(i).contains("<marketCity>")) {
						// Get the market city of the station.
						String marketCityOfStation = rawXMLData.get(i).replaceAll(
								"\\<.*?\\>", "");
						map.put(stationCity, "Market City: " + marketCityOfStation);
					}
					else if (rawXMLData.get(i).contains("<signal strength=")) {
						// Get the signal strength of the station.
						String signalStrength = rawXMLData.get(i).replaceAll("\\<.*?\\>",
								"");
						map.put(stationSignal, "Signal Strength: " + signalStrength);
						// Add the complete section to the array list.
						myRadioStations.add(map);
					}
				}
			}

			catch (Exception e) {
				e.printStackTrace();
			}
			return myRadioStations;
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				ArrayList<String>... params) {
			// Parses the raw XML Data from the website.
			myRadioStationList = parseRawXML(params[0]);
			return (myRadioStationList);
		}

		@Override
		protected void onPostExecute(
				ArrayList<HashMap<String, String>> myRadioStationList) {
			// return to update the activity with the new information
			myMainActivity.dataFromTheNetwork(myRadioStationList);
		}
	};

	protected ArrayList<String> doHTTPGet(String zipCode) throws Exception {

		BufferedReader in = null;
		ArrayList<String> rawDataArray = new ArrayList<String>();

		try {
			// Setups the HTTP client.
			HttpClient client = new DefaultHttpClient();
			String apiWebsite = "http://api.npr.org/stations?zip=";
			String apiKey = "&apiKey=MDEyMzAzNDMwMDEzODA0ODk0MzdjZjMxYQ001";

			// process data from
			URI website = new URI(apiWebsite + zipCode + apiKey);
			// Request using HTTP GET Method.
			HttpGet request = new HttpGet(website);
			HttpResponse response = client.execute(request);
			// string using buffered reader
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			String l = "";
			String newline = System.getProperty("line.separator");

			while ((l = in.readLine()) != null) {
				rawDataArray.add(l + newline);
			}
			in.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		// Get the NPR logo for the app.
		String nprLogo = "http://www.heymarci.com/wp-content/uploads/2008/08/npr_logo1.png";
		image = loadImageFromWeb(nprLogo);
		return rawDataArray;
	}

	@Override
	protected ArrayList<String> doInBackground(String... params) {

		try {
			// Return the parsed XML data.
			return doHTTPGet(params[0]);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
