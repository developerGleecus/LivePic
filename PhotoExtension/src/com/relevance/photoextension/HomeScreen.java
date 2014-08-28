package com.relevance.photoextension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TableLayout;
import android.widget.Toast;

import com.relevance.photoextension.dao.MySQLiteHelper;
import com.relevance.photoextension.hotspot.AddHotspotToImage;
import com.relevance.photoextension.hotspot.HotspotDetails;
import com.relevance.photoextension.utility.Constants;
import com.relevance.photoextension.utility.FileTool;
import com.relevance.photoextension.utility.Utility;

public class HomeScreen extends Activity implements OnClickListener,
		OnDrawerOpenListener, OnDrawerCloseListener {

	private ImageView upArrow;
	private TableLayout tableLayout;
	private String imageFilePath = "";
	private boolean upArrowPress = false;
	private SlidingDrawer sldr;
	private Context context;

	// GridView utility variables
	// Path of the project Livepic directory
	private String mRootPath = Environment.getExternalStorageDirectory()
			+ File.separator + Constants.PHOTO_ALBUM;
	private Intent mIntent;
	private ProgressDialog m_ProgressDialog = null;
	final int MENU_ONE = Menu.FIRST;
	final int MENU_TWO = Menu.FIRST + 1;
	public static List<String> lstFilePath = null;
	private GridView gridView;

	/** Unloading file path before use */
	public static void unloadLstFilePath() {
		if (lstFilePath != null) {
			lstFilePath.clear();
			lstFilePath = null;
		}
	}

	// Image Path Initialization
	private void getValues() {
		System.out.println("INFO: Inside Get Values");
		System.out.println("INFO: lstFilePath is NULL");
		lstFilePath = new ArrayList<String>();
		lstFilePath.clear();

		new FileTool().ListFiles(mRootPath, lstFilePath);
		System.out.println("INFO: lstFilePath now = " + lstFilePath.size());
		String addPath = this.getFilesDir().getPath();
		new FileTool().ListFiles(addPath, lstFilePath);
		System.out.println("INFO: lstFilePath now 2= " + lstFilePath.size());
		runOnUiThread(returnRes);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_screen);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilize();
	}

	private void initilize() {

		// Initializing GirdView to Main layout
		gridView = (GridView) findViewById(R.id.grid_view);

		ImageView cameraButton = (ImageView) findViewById(R.id.home_camera);
		ImageView galleryButton = (ImageView) findViewById(R.id.home_gallery);
		upArrow = (ImageView) findViewById(R.id.up_button);
		tableLayout = (TableLayout) findViewById(R.id.tableLayout1);
		sldr = (SlidingDrawer) this.findViewById(R.id.slidingDrawer1);

		cameraButton.setOnClickListener(this);
		galleryButton.setOnClickListener(this);
		upArrow.setOnClickListener(this);

		// Listen for open event
		sldr.setOnDrawerOpenListener(this);

		// Listen for close event
		sldr.setOnDrawerCloseListener(this);

		cameraButton.setOnClickListener(this);
		galleryButton.setOnClickListener(this);
		upArrow.setOnClickListener(this);

		if (upArrowPress) {

			sldr.open();
		} else {
			sldr.close();
		}

		mIntent = this.getIntent();

		Runnable viewOrders = new Runnable() {
			public void run() {
				getValues();
			}
		};

		Thread thread = new Thread(null, viewOrders, "MagentoBackground");
		thread.start();

		m_ProgressDialog = ProgressDialog
				.show(HomeScreen.this, "", "...", true);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.home_camera: // Start camera lunch activity
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			imageFilePath = Utility.getCapturedImage();
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(imageFilePath)));
			startActivityForResult(intent, Constants.CAMERA_REQUEST_CODE);
			break;

		case R.id.home_gallery:
			// Request scan of gallery to check for existing images
			Intent scanIntent = new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			sendBroadcast(scanIntent);

			// Start activity to choose photo from gallery
			Intent galleryIntent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(galleryIntent,
					Constants.GALLERY_REQUEST_CODE);
			break;

		case R.id.up_button:
			upArrowPress = true;
			upArrow.setVisibility(View.INVISIBLE);
			tableLayout.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onDestroy() {
		// Remove adapter refference from list
		gridView.setAdapter(null);
		super.onDestroy();
	}

	// ////////// Sumit,s Modification ////////////

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.GALLERY_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			int found = -1;
			imageFilePath = Utility.getRealPathFromURI(this, data.getData());

			MySQLiteHelper dbHelper = new MySQLiteHelper(
					getApplicationContext());
			found = dbHelper.getImage(imageFilePath);

			if (found == -1) {
				Intent intent = new Intent(this, AddHotspotToImage.class);
				intent.putExtra("imageName", imageFilePath);
				intent.putExtra("edit", false);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, HotspotDetails.class);
				intent.putExtra("imageName", imageFilePath);
				startActivity(intent);
			}

			upArrowPress = true;

		}

		else if (requestCode == Constants.CAMERA_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(this, AddHotspotToImage.class);
				intent.putExtra("imageName", imageFilePath);
				startActivity(intent);

				Utility.mediaScanForCapturedImage(this, imageFilePath);
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.capture_cancelled),
						Toast.LENGTH_SHORT).show();
			}
			upArrowPress = true;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("imageName", imageFilePath);
		outState.putBoolean("ifPressed", upArrowPress);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		imageFilePath = savedInstanceState.getString("imageName");
		upArrowPress = savedInstanceState.getBoolean("ifPressed");
	}

	@Override
	public void onDrawerClosed() {
		Toast.makeText(getApplicationContext(), "Drawer Closed",
				Toast.LENGTH_SHORT).show();
		upArrowPress = false;
	}

	@Override
	public void onDrawerOpened() {
		Toast.makeText(getApplicationContext(), "Drawer Opened",
				Toast.LENGTH_SHORT).show();
		upArrowPress = true;
	}

	private Runnable returnRes = new Runnable() {

		public void run() {
			DisplayMetrics metric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metric);
			int columnWidth = metric.widthPixels / 4; // ??
			GridViewAdapter iadapter = new GridViewAdapter(HomeScreen.this,
					lstFilePath);
			gridView.setAdapter(iadapter);
			gridView.setColumnWidth(columnWidth);
			m_ProgressDialog.dismiss();
		}
	};
}