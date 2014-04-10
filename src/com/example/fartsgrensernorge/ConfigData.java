package com.example.fartsgrensernorge;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

public class ConfigData extends Activity {

	List<String> groupList;
	List<String> childList;
	Map<String, List<String>> laptopCollection;
	ExpandableListView expListView;
	boolean backpressed;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configdata_layout);

		createGroupList();

		createCollection();

		expListView = (ExpandableListView) findViewById(R.id.laptop_list);
		final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
				this, groupList, laptopCollection);
		expListView.setAdapter(expListAdapter);

		//setGroupIndicatorToRight();

		expListView.setOnChildClickListener(new OnChildClickListener() {

			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				final String selected = (String) expListAdapter.getChild(
						groupPosition, childPosition);
				Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG)
				.show();

				return true;
			}
		});
	}
	@Override
	protected void onDestroy(){	
		backpressed = true;
		super.onDestroy();
		Log.d("debug message", "in onDestroy()");
	}
	private void createGroupList() {
		groupList = new ArrayList<String>();
		groupList.add("Nord-Norge");
		groupList.add("Trøndelag");
		groupList.add("Vestlandet");
		groupList.add("Østlandet");
		groupList.add("Sørlandet");
	}
	public void onStartDownload(final View view){



		new Thread(){
			public void run(){
				Socket s;

				LinkedList<String> checked = ExpandableListAdapter.checked;
				String getQuarry = "GET";
				for (Iterator iterator = checked.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					System.out.println(string);

					if(string.equals("Østfold"))
						getQuarry+= " 1";
					else if(string.equals("Akershus"))
						getQuarry += " 2";
					else if(string.equals("Oslo"))
						getQuarry += " 3";
					else if(string.equals("Hedmark"))
						getQuarry += " 4";
					else if(string.equals("Oppland"))
						getQuarry += " 5";
					else if(string.equals("Buskerud"))
						getQuarry += " 6";
					else if(string.equals("Vestfold"))
						getQuarry += " 7";
					else if(string.equals("Telemark"))
						getQuarry += " 8";
					else if(string.equals("Aust-Agder"))
						getQuarry += " 9";
					else if(string.equals("Vest-Agder"))
						getQuarry += " 10";
					else if(string.equals("Rogaland"))
						getQuarry += " 11";
					else if(string.equals("Hordaland"))
						getQuarry += " 12";
					else if(string.equals("Sogn og Fjordane"))
						getQuarry += " 14";
					else if(string.equals("Møre og Romsdal"))
						getQuarry += " 15";
					else if(string.equals("Sør-Trøndelag"))
						getQuarry += " 16";
					else if(string.equals("Nord-Trøndelag"))
						getQuarry += " 17";
					else if(string.equals("Nordland"))
						getQuarry += " 18";
					else if(string.equals("Troms"))
						getQuarry += " 19";
					else if(string.equals("Finnmark"))
						getQuarry += " 20";
				}

				try {

					String state = Environment.getExternalStorageState();

					if (!Environment.MEDIA_MOUNTED.equals(state)) {
						System.out.println("Storage not found");
						return;
					}

					s = new Socket("188.64.45.149", 1234);


					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					System.out.println("getQuarry: " + getQuarry);
					out.writeBytes(getQuarry +"\n");

					byte [] buffer= new byte[4];
					s.getInputStream().read(buffer);
					int size = 0;
					for (int i = 0; i < 4; i++)
						size |= (((int)buffer[i] & 0xff) << (i * 8));

					System.out.println("info: " + size);
					File sdCard = getExternalFilesDir("coordsdata");
					//File sdCard = Environment.getExternalStorageDirectory();



					File file = new File(sdCard, "datab.dat");
					file.delete();
					DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));


					int buffer_size = 50240;
					buffer = new byte[buffer_size];
					int totalBytesRead = 0;
					boolean printed = false;
					byte lastbyte = -1;
					while(totalBytesRead < size){
						int noBytesRead = 0;
						System.out.println("total bytes read" + totalBytesRead);

						while(noBytesRead < buffer_size && totalBytesRead < size){
							int nrRead = s.getInputStream().read(buffer, noBytesRead, buffer.length-noBytesRead);
							//System.out.println("returned by read: " + nrRead);
							lastbyte = buffer[noBytesRead];
							noBytesRead += (nrRead > 0) ? nrRead : 0;
							totalBytesRead += (nrRead > 0) ? nrRead : 0;
							if(backpressed){
								s.close();
								return;
							}
						}
						if(!printed){
							System.out.println("first bytes: ");
							for (int i = 0; i < 100; i++) {
								if(i % 4 == 0)
									System.out.println();
								System.out.printf("%02x ",buffer[i]);
							}

							int firstCoord = 0;

							firstCoord = 0;
							for (int i = 0; i < 4; i++)
								firstCoord |= (((int)buffer[3-i] & 0xff) << (i * 8));

							System.out.println("firstCoord: " + firstCoord);

							firstCoord = 0;
							for (int i = 0; i < 4; i++)
								firstCoord |= (((int)buffer[7-i] & 0xff) << (i * 8));

							System.out.println("secondCoord: " + firstCoord);
							int data = 0;
							for (int i = 0; i < 4; i++)
								data |= (((int)buffer[11-i] & 0xff) << (i * 8));
							System.out.println("data: " + Integer.toBinaryString(data));

							printed = true;
						}
						for (int i = 0; i < noBytesRead; i++) {						
							dos.write(buffer[i]);
						}
						final int percent = (totalBytesRead*100)/size;

						runOnUiThread(new Runnable() {
							public void run() {
								((Button)view).setText( percent + "%");
								((Button)view).setClickable(false);
							}
						});

					}

					dos.close();
					System.out.println("total bytes read" + totalBytesRead);
					runOnUiThread(new Runnable() {
						public void run() {
							((Button)view).setText("Download complete");
							((Button)view).setClickable(true);

						}
					});

					file = new File(sdCard, "datab.dat");

					InputStream is = new FileInputStream(file);
					ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
					try {
						ZipEntry ze;
						while ((ze = zis.getNextEntry()) != null) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] zipBuffer = new byte[1024];
							int count;
							while ((count = zis.read(zipBuffer)) != -1) {
								baos.write(zipBuffer, 0, count);
							}
							byte []textfilebytes = baos.toByteArray();
							for (int i = 0; i < textfilebytes.length; i++) {
								System.out.println((char)textfilebytes[i]);
								System.out.println(Integer.toHexString(textfilebytes[i]));
							}
							System.out.println(textfilebytes[0]);
							String filename = ze.getName();
							byte[] bytes = baos.toByteArray();
							// do something with 'filename' and 'bytes'...
						}
					} finally {
						zis.close();
					}


					//   for (int i = 0; i < buffer.length; i++) {
					//		System.out.print(buffer[i]);
					//	}

				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block

					e.printStackTrace();
				}


			}
		}.start();

	}
	private void createCollection() {
		// preparing laptops collection(child)
		String[] nordnorge = { "Finnmark", "Nordland", "Troms" };
		String[] trondelag = { "Nor-Trøndelag", "Sør-Trøndelag" };
		String[] vestlandet = { "Hordaland", "Møre og Romsdal", "Rogaland", "Sogn og Fjordane" };
		String[] ostlandet = { "Akershus", "Buskerud", "Hedmark", "Oppland", "Oslo", "Telemark", "Vestfold",  "Østfold" };
		String[] sorlandet = { "Aust-Agder", "Vest-Agder" };

		laptopCollection = new LinkedHashMap<String, List<String>>();

		for (String laptop : groupList) {
			if (laptop.equals("Nord-Norge")) {
				loadChild(nordnorge);
			} else if (laptop.equals("Trøndelag"))
				loadChild(trondelag);
			else if (laptop.equals("Vestlandet"))
				loadChild(vestlandet);
			else if (laptop.equals("Østlandet"))
				loadChild(ostlandet);
			else if (laptop.equals("Sørlandet"))
				loadChild(sorlandet);


			laptopCollection.put(laptop, childList);
		}
	}

	private void loadChild(String[] laptopModels) {
		childList = new ArrayList<String>();
		for (String model : laptopModels)
			childList.add(model);
	}

	private void setGroupIndicatorToRight() {
		/* Get the screen width */
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;

		expListView.setIndicatorBounds(width - getDipsFromPixel(35), width
				- getDipsFromPixel(5));
	}

	// Convert pixel to dip
	public int getDipsFromPixel(float pixels) {
		// Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}