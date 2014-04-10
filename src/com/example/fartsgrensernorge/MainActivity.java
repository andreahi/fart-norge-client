package com.example.fartsgrensernorge;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.LDAPCertStoreParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

import com.example.fartsgrensernorge.KdTreeOld.XYZPoint;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends  Activity implements LocationListener, OnSharedPreferenceChangeListener{

	private TextView speedLimit;
	private ImageView atkimage;
	private ImageView btnActivate;
	private ImageView imageSpeed1;
	private ImageView imageSpeed2;

	private LocationManager locationManager;
	private String provider;
	KdTree<KdTree.XYZPoint> kdTree;
	private static final long MIN_TIME = 5 *1000; 
	boolean activated;
	int prevSpeed = 0;
	int updateDelay = 5000;
	DataOutputStream dos;
	File file;
	/** Called when the activity is first created. */

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//debugText1 = (TextView) findViewById(R.id.latitudeText);
		//debugText2 = (TextView) findViewById(R.id.longitudeText);
		//distance = (TextView) findViewById(R.id.distance);
		atkimage = (ImageView)findViewById(R.id.atkimage);
		speedLimit = (TextView) findViewById(R.id.speedlimit);
		imageSpeed1 = (ImageView)findViewById(R.id.imageSpeed1);
		imageSpeed2 = (ImageView)findViewById(R.id.imageSpeed2);
		btnActivate = (ImageView)findViewById(R.id.activateButton);
		kdTree = new KdTree<KdTree.XYZPoint>(getExternalFilesDir("coordsdata"));
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.updateDelay = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("searchdelay", "5000"));

		locationManager.requestLocationUpdates( locationManager.getBestProvider(crit, false), this.updateDelay, 0, this);
		System.out.println("after regquest location");
		//addPreferencesFromResource(R.xml.preferences);

		/*
		Collection<KdTree.XYZPoint> c =		kdTree.nearestNeighbourSearch(1, new KdTree.XYZPoint(10.9542, 59.3252));

		Iterator iterator = c.iterator();
		KdTree.XYZPoint xyzPoint = (KdTree.XYZPoint) iterator.next();
		System.out.println("x: " + xyzPoint.x + " y: " + xyzPoint.y);
		 */
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);



		boolean screenOn =PreferenceManager.getDefaultSharedPreferences(this).getBoolean("screenOn", false);
		if(screenOn)
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		File sdCard = Environment.getExternalStorageDirectory();
		file = new File(sdCard, "test/datab"+System.currentTimeMillis()+".txt");

		try {
			dos = new DataOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(){
			public void run(){
				File sdCard = Environment.getExternalStorageDirectory();

				File file = new File(sdCard,  "test/datab1379405009443.dat");
				DataInputStream dos = null;
				try {
					dos = new DataInputStream(new FileInputStream(file));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				for (int i = 0; i < 10; i++) {
					try {
						double a = dos.readDouble();
						double b = dos.readDouble();

						//a = 59.846135313591695;
						//b = 10.776621332799328;
						//onLocationChangedTest(new TestLocation( a,b, 8));
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}
		}.start();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println("optionitemselected");
		switch (item.getItemId()) {

		case R.id.action_settings:
			System.out.println("actionsettings");
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			//startActivityForResult(i, RESULT_SETTINGS);
			break;

		}

		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, 0, this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	public void onConfigData(View view){
		Intent intObjP = new Intent(this,
				ConfigData.class);
		startActivity(intObjP);
	}
	public void onActivate(View view){
		if(!activated){
			System.out.println("activate pressed");
			activated = true;
			//btnActivate.setText("Deactivate");
			//distance.setText("Waiting for GPS data");
			btnActivate.setImageResource(R.drawable.deactivate);
		}

		else {
			activated = false;	
			btnActivate.setImageResource(R.drawable.activate);


			//distance.setText("Press Activate to see the speedlimit for your location");
			//btnActivate.setText("Activate");
		}
	}
	double lineToPointDistance (double a, double c, double x, double y){
		double b = -1;
		double tmp1 = Math.abs(a*x +  b*y + c);
		double tmp2 = Math.sqrt((a*a)+(b*b));

		return tmp1/tmp2;
	}
	double previousLat = 0;
	double previousLng = 0;
	@Override
	public void  onDestroy(){
		super.onDestroy();
		try {
			dos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void onLocationChanged(final Location location) {
		System.out.println("location changed");
		int currentSpeedLimit = 0;

		if(activated){
			double lat = (location.getLatitude());
			double lng = (location.getLongitude());
			try {
				dos.writeDouble(lat);
				dos.writeDouble(lng);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//			double a = (lat-previousLat)/(lng-previousLng);
			//			double c = lat-a*lng;
			//debugText1.setText("lat: " + String.valueOf(lat) + ", lng: " + String.valueOf(lng));

			Collection<KdTree.XYZPoint> nearestCoords = kdTree.nearestNeighbourSearch(1, new KdTree.XYZPoint(lng, lat, 0));
			Iterator iterator = nearestCoords.iterator();

			final KdTree.XYZPoint closestPoint = (KdTree.XYZPoint) iterator.next();

			if(closestPoint == null){
				Toast.makeText(this, "No data found, make sure you have downloaded data for your area",
						Toast.LENGTH_SHORT).show();
				return;
			}
			float[] results = new float[4];

			System.out.println("x: " + closestPoint.x + " y: " + closestPoint.y);
			System.out.println("closestpoint data: "  + closestPoint.speed);
			System.out.println("lng: " + lng + " lat: " + lat);
			System.out.println("time elapsed: " + closestPoint.timeElapsed);


			Location.distanceBetween(lng, lat, closestPoint.x, closestPoint.y, results);

			currentSpeedLimit = closestPoint.getSpeed();
			//currentSpeed.setText((int)(3.6*location.getSpeed()) + "Km/h"+ " time: " + closestPoint.timeElapsed );
			//distance.setText("nearest speed : " + results[0] + "m with Speedlimit: " + xyzPoint.speed);
			runOnUiThread(new Runnable() {
				public void run() {
					
					//currentSpeed.setText(3.6*location.getSpeed() + "Km/h"+ " time: " + closestPoint.timeElapsed );
					switch((int)(3.6*location.getSpeed()) % 10){
					case 1:
						imageSpeed1.setImageResource(R.drawable.speed1);
						break;
					case 2:
						imageSpeed1.setImageResource(R.drawable.speed2);
						break;
					case 3:
						imageSpeed1.setImageResource(R.drawable.speed3);
						break;
					case 4:
						imageSpeed1.setImageResource(R.drawable.speed4);
						break;
					case 5:
						imageSpeed1.setImageResource(R.drawable.speed5);
						break;
					case 6:
						imageSpeed1.setImageResource(R.drawable.speed6);
						break;
					case 7:
						imageSpeed1.setImageResource(R.drawable.speed7);
						break;
					case 8:
						imageSpeed1.setImageResource(R.drawable.speed8);
						break;
					case 9:
						imageSpeed1.setImageResource(R.drawable.speed9);
						break;
					case 0:
						imageSpeed1.setImageResource(R.drawable.speed0);
						break;
					}
					
					switch((int)(3.6*location.getSpeed()/10) % 10){
					case 1:
						imageSpeed2.setImageResource(R.drawable.speed1);
						break;
					case 2:
						imageSpeed2.setImageResource(R.drawable.speed2);
						break;
					case 3:
						imageSpeed2.setImageResource(R.drawable.speed3);
						break;
					case 4:
						imageSpeed2.setImageResource(R.drawable.speed4);
						break;
					case 5:
						imageSpeed2.setImageResource(R.drawable.speed5);
						break;
					case 6:
						imageSpeed2.setImageResource(R.drawable.speed6);
						break;
					case 7:
						imageSpeed2.setImageResource(R.drawable.speed7);
						break;
					case 8:
						imageSpeed2.setImageResource(R.drawable.speed8);
						break;
					case 9:
						imageSpeed2.setImageResource(R.drawable.speed9);
						break;
					case 0:
						imageSpeed2.setImageResource(R.drawable.speed0);
						break;
					}
					
					atkimage.setVisibility(closestPoint.isATKclose()?View.VISIBLE:View.INVISIBLE);
						
				}
			});
			previousLat = lat;
			previousLng = lng;
			int proximityLimit = (int) location.getSpeed();
			if(results[0] < proximityLimit*2){
				if(prevSpeed == currentSpeedLimit)
					speedLimit.setText(""+currentSpeedLimit);
				prevSpeed = currentSpeedLimit;
			}
		}



	}


	public void onLocationChangedTest(final TestLocation location) {
		System.out.println("location changed");

		if(!activated){
			double lat = (location.getLatitude());
			double lng = (location.getLongitude());


			//			double a = (lat-previousLat)/(lng-previousLng);
			//			double c = lat-a*lng;
			//debugText1.setText("lat: " + String.valueOf(lat) + ", lng: " + String.valueOf(lng));

			Collection<KdTree.XYZPoint> nearestCoords = kdTree.nearestNeighbourSearch(1, new KdTree.XYZPoint(lng, lat, 0));
			Iterator iterator = nearestCoords.iterator();

			final KdTree.XYZPoint closestPoint = (KdTree.XYZPoint) iterator.next();

			if(closestPoint == null){
				Toast.makeText(this, "No data found, make sure you have downloaded data for your area",
						Toast.LENGTH_SHORT).show();
				return;
			}
			float[] results = new float[4];

			System.out.println("x: " + closestPoint.x + " y: " + closestPoint.y);
			System.out.println(" lat, lng: " + lat +","+lng);
			System.out.println("time elapsed: " + closestPoint.timeElapsed);
			System.out.println("data: " + closestPoint.speed);
			System.out.println(Integer.toBinaryString(closestPoint.speed));
			Location.distanceBetween(lng, lat, closestPoint.x, closestPoint.y, results);
			System.out.println("distance: " + results[0]);
			final int currentSpeedLimit = closestPoint.getSpeed();
			System.out.println("Speed: " + currentSpeedLimit);
			runOnUiThread(new Runnable() {
				public void run() {
					
					//currentSpeed.setText(3.6*location.getSpeed() + "Km/h"+ " time: " + closestPoint.timeElapsed );
					switch((int)(3.6*location.getSpeed()) % 10){
					case 1:
						imageSpeed1.setImageResource(R.drawable.speed1);
						break;
					case 2:
						imageSpeed1.setImageResource(R.drawable.speed2);
						break;
					case 3:
						imageSpeed1.setImageResource(R.drawable.speed3);
						break;
					case 4:
						imageSpeed1.setImageResource(R.drawable.speed4);
						break;
					case 5:
						imageSpeed1.setImageResource(R.drawable.speed5);
						break;
					case 6:
						imageSpeed1.setImageResource(R.drawable.speed6);
						break;
					case 7:
						imageSpeed1.setImageResource(R.drawable.speed7);
						break;
					case 8:
						imageSpeed1.setImageResource(R.drawable.speed8);
						break;
					case 9:
						imageSpeed1.setImageResource(R.drawable.speed9);
						break;
					case 0:
						imageSpeed1.setImageResource(R.drawable.speed0);
						break;
					}
					
					switch((int)(3.6*location.getSpeed()/10) % 10){
					case 1:
						imageSpeed2.setImageResource(R.drawable.speed1);
						break;
					case 2:
						imageSpeed2.setImageResource(R.drawable.speed2);
						break;
					case 3:
						imageSpeed2.setImageResource(R.drawable.speed3);
						break;
					case 4:
						imageSpeed2.setImageResource(R.drawable.speed4);
						break;
					case 5:
						imageSpeed2.setImageResource(R.drawable.speed5);
						break;
					case 6:
						imageSpeed2.setImageResource(R.drawable.speed6);
						break;
					case 7:
						imageSpeed2.setImageResource(R.drawable.speed7);
						break;
					case 8:
						imageSpeed2.setImageResource(R.drawable.speed8);
						break;
					case 9:
						imageSpeed2.setImageResource(R.drawable.speed9);
						break;
					case 0:
						imageSpeed2.setImageResource(R.drawable.speed0);
						break;
					}
					
					atkimage.setVisibility(closestPoint.isATKclose()?View.VISIBLE:View.INVISIBLE);
						
				}
			});
			System.out.println("ATK CLOSE BY?: " + closestPoint.isATKclose());
			//distance.setText("nearest speed : " + results[0] + "m with Speedlimit: " + xyzPoint.speed);
			System.out.println("speed1 thingy: " +(int)(3.6*location.getSpeed()) % 10);
			previousLat = lat;
			previousLng = lng;
			int proximityLimit = location.getSpeed();
			if(results[0] < proximityLimit){
				if(prevSpeed == currentSpeedLimit)
					runOnUiThread(new Runnable() {
						public void run() {
							speedLimit.setText(""+currentSpeedLimit);

						}
					});
				prevSpeed = currentSpeedLimit;
			}
		}



	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		System.out.println("pref changed lawl");
		if(key.equals("screenOn")){
			boolean screenOn =PreferenceManager.getDefaultSharedPreferences(this).getBoolean("screenOn", false);
			if(screenOn)
				this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			else
				this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		if(key.equals("searchdelay")){
			this.updateDelay = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("searchdelay", "5000"));
			Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);

			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
			locationManager.requestLocationUpdates( locationManager.getBestProvider(crit, false), this.updateDelay, 0, this);

		}
	}
} 

class TestLocation {
	double lat, lng;
	int speed;
	TestLocation(double lat, double lng, int speed){
		this.lat = lat;
		this.lng = lng;
		this.speed = speed;
	}
	double getLatitude(){
		return lat;
	}
	double getLongitude(){
		return lng;
	}
	int getSpeed(){
		return speed;
	}

}
class SpeedLimit extends Thread{

	@Override
	public void run(){

	}
}
/*
class SpeedLimits extends Thread{
	Scanner sc;
	Activity mainActivity;
	public SpeedLimits(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	@Override
	public void run(){
		readFile();
	}
	final int NR_OF_FILES = 1;
	void readFile(){
		Log.e("debug", "in read file");
		double shortestDistance = Float.MAX_VALUE;

		//InputStream databaseInputStream = mainActivity.getResources().openRawResource(R.raw.vegdatafartsgrenser1);
		InputStream is;
		int nrOfFilesRead = 0;
		try {
			String[] files = mainActivity.getAssets().list("");
			for (int i = 0; i < files.length; i++) {
				Log.e("debug", files[i]);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		KdTree<XYZPoint> kd = new KdTree<XYZPoint>();

		while(nrOfFilesRead++ < NR_OF_FILES){
			try {
				is = mainActivity.getAssets().open("data"+nrOfFilesRead+".txt");
			} catch (IOException e) {
				Log.e("debug", "cant find data file " + "data"+nrOfFilesRead+".txt");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			sc = new Scanner(is);


			Log.e("debug", "going to read the data");
			int count = 0;
			float [] results;
			String coordsText;
			String speedText;
			while(sc.hasNextLine()){
				coordsText = sc.nextLine();
				speedText = sc.nextLine();
				coordsText = coordsText.replace("[", "");
				coordsText = coordsText.replace("]", "");
				coordsText = coordsText.replace("'", "");
				coordsText = coordsText.replace(",", "");

				Scanner coordsScanner = new Scanner(coordsText);
				Scanner speedScanner = new Scanner(speedText);

				int speed = speedScanner.nextInt();
				results = new float[4] ;

				while(coordsScanner.hasNext()){
					double tmpLng = Double.parseDouble(coordsScanner.next("\\d+\\.\\d+"));
					double tmpLat = Double.parseDouble(coordsScanner.next("\\d+\\.\\d+"));
					//al.add(new Speed(tmpLng, tmpLat, speed));
					kd.add(new XYZPoint(tmpLng, tmpLat));

				}
				coordsScanner.close();
				speedScanner.close();

				Log.e("debug", "count: " + ++count);
			}
			sc.close();
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*	

		for (Iterator iterator = al.iterator(); iterator.hasNext();) {
			Speed speed2 = (Speed) iterator.next();
			Log.e("debug","lng: " + speed2.lng + " lat: " + speed2.lat + " speed: " + speed2.speed);
			Location.distanceBetween(speed2.lng, speed2.lat, 5, 59, results);
			if(results[0] < shortestDistance)
				shortestDistance = results[0];


		}

		Log.e("debug", "shortestDistance: " + shortestDistance );

	}
}

class Speed{
	double lng;
	double lat;
	int speed;
	public Speed(double lng, double lat, int speed) {
		this.lng = lng;
		this.lat = lat;
		this.speed = speed;
	}
}*/
