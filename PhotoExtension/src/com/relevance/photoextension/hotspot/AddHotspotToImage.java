package com.relevance.photoextension.hotspot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;
import android.widget.TableLayout;
import android.widget.Toast;

import com.relevance.photoextension.CornerImageView;
import com.relevance.photoextension.CornerTouchListener;
import com.relevance.photoextension.CustomImageTouchListener;
import com.relevance.photoextension.CustomImageView;
import com.relevance.photoextension.R;
import com.relevance.photoextension.dao.MySQLiteHelper;
import com.relevance.photoextension.model.Hotspot;
import com.relevance.photoextension.model.Image;
import com.relevance.photoextension.utility.Constants;
import com.relevance.photoextension.utility.Utility;

//Activity for create,edit and delete the hotspot and its metadata over selected image as well as taking snip and save it to predefined directory.

@SuppressWarnings("deprecation")
public class AddHotspotToImage extends Activity implements OnTouchListener,
		OnClickListener, OnLongClickListener {
	
	//initializing variables to be used
	
	private final ArrayList<HotspotImageView> hotspotIconForAdd = new ArrayList<HotspotImageView>();
	private final ArrayList<Hotspot> hotSpots = new ArrayList<Hotspot>();
	private MySQLiteHelper dbHelper;
	private ViewGroup rootView;
	private View myView;
	private ImageView addButton;
	private ImageView selectedImg;
	private ImageView recordButton;
	private EditText textEdit;
	private EditText urlEdit;
	private int xDelta;
	private int yDelta;
	private int index;
	private static int counter = 0;
	private static int xDisp = 0;
	private static int yDisp = 0;
	private int mediaIdFlag;
	private static int imageLevelFlag;
	private String text;
	private String url;
	private String urlData;
	private String selectedMediaPath;
	private String imageFilePath = "";
	private String audioFilePath = "";
	private boolean canPlay = false;
	private boolean canRecord = true;
	private boolean isPlaying = false;
	private boolean isRecording = false;
	private boolean isEditable;
	private boolean isHotspotVisible = true;
	private Dialog dlg = null;
	private static MediaRecorder mediaRecorder = null;
	private static MediaPlayer mediaPlayer = null;

	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	Matrix matrixInitial = new Matrix();
	Matrix matrixTemp = new Matrix();
	float iniScale;
	boolean isLoadedFirstTime = true;
	boolean isDialogOpen = false;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	float[] valuesInitial = new float[9];

	private boolean isImageLoaded = false;
	private Timer _timer;
	private int _orientation;
	private int _newOrientation;
	CustomImageView civ;
	Point pt1, pt2, pt3, pt4;
	public boolean zoneExists = false;
	public boolean taskComplete = false;
	int civWidth = 200;
	int civHeight = 200;

	int leftMargin = 0;
	int topMargin = 0;

	int leftPadding = -1;
	int topPadding = -1;
	float scaleU = 1.0f;
	private ArrayList<CornerImageView> cornerArrayList = new ArrayList<CornerImageView>();
	private CornerTouchListener cornerListener;

	private Bitmap mBitmap;
	String tempFilePath = Environment.getExternalStorageDirectory()
			+ File.separator;

	private LinearLayout linear;
	private RelativeLayout relative1;
	private SlidingDrawer slidingDrawer;
	private boolean activeState = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Create the view screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_hotspot);

		imageFilePath = getIntent().getStringExtra("imageName");
		isEditable = getIntent().getBooleanExtra("edit", false);

		rootView = (ViewGroup) findViewById(R.id.root);
		selectedImg = (ImageView) findViewById(R.id.imageCenter);
		linear = (LinearLayout) findViewById(R.id.linear);
		relative1 = (RelativeLayout) findViewById(R.id.relative1);
		addButton = (ImageView) findViewById(R.id.add_hotspot_button);
		ImageView saveButton = (ImageView) findViewById(R.id.iv_save_hotspot);
		ImageView cameraButton = (ImageView) findViewById(R.id.iv_camera_hotspot);
		ImageView toggleButton = (ImageView) findViewById(R.id.iv_hide_hotspot);
		findViewById(R.id.privacy);
		slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);

		addButton.setOnClickListener(this);
		addButton.setOnLongClickListener(this);
		saveButton.setOnClickListener(this);
		cameraButton.setOnClickListener(this);
		toggleButton.setOnClickListener(this);

		File folder = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "LivePicWishlist");
		if (!folder.exists()) {
			folder.mkdir();
			Log.v("Adding line", "/");
			Toast t = Toast.makeText(AddHotspotToImage.this,
					"LivePicWishlist Created", Toast.LENGTH_SHORT);
			t.show();
		}

		File folder1 = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "LivePicClipboard");
		if (!folder1.exists()) {
			folder1.mkdir();
			Log.v("Adding line", "/");
			Toast t = Toast.makeText(AddHotspotToImage.this,
					"LivePicClipboard Created", Toast.LENGTH_SHORT);
			t.show();
		}

		dbHelper = new MySQLiteHelper(getApplicationContext());

		imageLevelFlag = 0;
		if (isEditable) {
			drawAvailableHotspot();
		}

		selectedImg.setOnTouchListener(this);
		_timer = new Timer();
		_orientation = getResources().getConfiguration().orientation;
	}

	//Impact if device orientation changes
	
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			if (isDialogOpen) {
				return;
			}

			Utility.setImage(AddHotspotToImage.this, imageFilePath, selectedImg);

			if (_orientation == Configuration.ORIENTATION_LANDSCAPE) {
				selectedImg.setScaleType(ScaleType.MATRIX);
				if (isLoadedFirstTime) {
					isLoadedFirstTime = false;
					loadspotsInLandscape();
				}
				updateHotspotPosition();

			} else {

				if (isLoadedFirstTime) {
					isLoadedFirstTime = false;
					Matrix temp = selectedImg.getImageMatrix();
					matrixInitial.set(temp);
					matrix.set(temp);

					matrixInitial.getValues(valuesInitial);
					updateHotspotPosition();
				}
			}
		}
	}

	// finalizing the results
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.RQS_OPEN_AUDIO_MP3
				&& resultCode == RESULT_OK) {
			Uri selectedMediaUri = data.getData();
			selectedMediaPath = Utility.getRealPathFromURI(this,
					selectedMediaUri);
			recordButton.setImageResource(R.drawable.microphone_icon);
			audioFilePath = null;
			canRecord = true;
			canPlay = false;
			mediaIdFlag = 2;

		} else if (requestCode == Constants.CAMERA_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Intent intent = getIntent();
				intent.putExtra("imageName", imageFilePath);
				intent.putExtra("edit", false);
				startActivity(intent);
				finish();
				selectedImg.setScaleType(ScaleType.MATRIX);
				Utility.mediaScanForCapturedImage(this, imageFilePath);
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.capture_cancelled),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	//hotspot longclick listener
	
	@Override
	public boolean onLongClick(View v) {
		removeDialog(100);
		myView = selectedImg;
		if (imageLevelFlag == 0 && activeState == false) {
			imageLevelFlag++;
			mediaIdFlag = 0;
			Hotspot h = new Hotspot(0, 0, "", "", null, 0);
			hotSpots.add(h);
			HotspotImageView hotspotImageView = new HotspotImageView();
			hotspotImageView.hotspot = null;
			hotspotImageView.imgView = selectedImg;
			hotspotIconForAdd.add(hotspotImageView);
			showDialog(100);
		} else {
			showDialog(100);
		}
		return false;
	}

	
	//menu item's functionalities
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.add_hotspot_button:// to add hotspot to image

			if (myView != null && myView.getTag() != null) {
				break;
			}

			addHotspot();
			addButton.setImageResource(R.drawable.edit_b_grayedout);
			break;

		case R.id.iv_save_hotspot:// to save the image with respective hotspots
			if (isEditable) {
				int pid = dbHelper.getImage(imageFilePath);
				dbHelper.deleteAllHotspot(pid);
				saveHotspotInDatabase();
				startHotspotDetailsActivity();
			} else {
				saveHotspotInDatabase();
			}
			finish();
			break;

		case R.id.iv_camera_hotspot: // to launch camera activity

			if (zoneExists == false) {
				addZone();
			}

			break;

		case R.id.iv_hide_hotspot: // to toggle hotspot hide/unhide

			toggleHotspots();
			updateHotspotPosition();
			break;

		}
	}

	//Directory path configuration for saving Image snip
	protected void pathConfigure() {
		final CharSequence[] items = { "LivePicWishlist", "LivePicClipboard" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Directory");

		builder.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if ("LivePicWishlist".equals(items[which])) {
							tempFilePath = Environment
									.getExternalStorageDirectory()
									+ File.separator
									+ "LivePicWishlist"
									+ File.separator;
							Toast t = Toast.makeText(AddHotspotToImage.this,
									tempFilePath, Toast.LENGTH_SHORT);
							t.show();
							dialog.dismiss();
							onSaveClickedEx();

						} else if ("LivePicClipboard".equals(items[which])) {
							tempFilePath = Environment
									.getExternalStorageDirectory()
									+ File.separator
									+ "LivePicClipboard"
									+ File.separator;

							Toast t = Toast.makeText(AddHotspotToImage.this,
									tempFilePath, Toast.LENGTH_SHORT);
							t.show();
							dialog.dismiss();
							onSaveClickedEx();
						}
					}
				});
		builder.show();

	}

	//To save the image after snip
	
	protected void onSaveClickedEx() {
		int lowestX = 2000;
		int lowestY = 2000;

		int highestX = 0;
		int highestY = 0;

		mBitmap = ((BitmapDrawable) selectedImg.getDrawable()).getBitmap();
		{
			for (int i = 0; i < cornerArrayList.size(); i++) {
				CornerImageView tmp = cornerArrayList.get(i);
				if (tmp.getLeftMargin() < lowestX)
					lowestX = tmp.getLeftMargin();
				if (tmp.getTopMargin() < lowestY)
					lowestY = tmp.getTopMargin();

				if (tmp.getLeftMargin() > highestX)
					highestX = tmp.getLeftMargin();
				if (tmp.getTopMargin() > highestY)
					highestY = tmp.getTopMargin();
			}
		}

		int localLeftMargin = lowestX;
		int localTopMargin = lowestY;
		int localWidth = highestX - lowestX;
		int localHeight = highestY - lowestY;

		System.out.println("INFO: LOCAL LEFT = " + localLeftMargin + " TOP = "
				+ localTopMargin + " WIDTH  = " + localWidth + " HEIGHT = "
				+ localHeight);
		System.out.println("INFO: ORG LEFT = " + leftMargin + " TOP = "
				+ topMargin + " WIDTH  = " + civWidth + " HEIGHT = "
				+ civHeight);

		int newWidth = (int) (localWidth * (float) (1 / scaleU));
		int newHeight = (int) (localHeight * (float) (1 / scaleU));
		int nLeftMargin = (int) ((localLeftMargin - leftPadding) * (float) (1 / scaleU));
		int nTopMargin = (int) ((localTopMargin - topPadding) * (float) (1 / scaleU));

		System.out.println("INFO: Using LeftMargin = " + nLeftMargin
				+ " Top = " + nTopMargin + " Width = " + newWidth
				+ " Height = " + newHeight);

		Bitmap croppedBitmap = Bitmap.createBitmap(mBitmap, nLeftMargin + 30,
				nTopMargin + 30, newWidth, newHeight);

		FileOutputStream fOut = null;

		try {
			Long tsLong = System.currentTimeMillis() / 1000;
			String ts = tsLong.toString();

			int index = imageFilePath.lastIndexOf("/");
			String fileName = imageFilePath.substring(index + 1);
			System.out.println("Now the tempFilePath is :" + tempFilePath);
			File file = new File(tempFilePath + ts + fileName);

			fOut = new FileOutputStream(file);
			croppedBitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				fOut.close();
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}

	//Adding Image snip selector to the screen
	
	private void addZone() {

		int rootWidth = rootView.getMeasuredWidth();
		int rootHeight = rootView.getMeasuredHeight();

		int imgWidth = selectedImg.getDrawable().getIntrinsicWidth();
		int imgHeight = selectedImg.getDrawable().getIntrinsicHeight();

		System.out.println("TEST : WIDTH = " + imgWidth + " HEIGHT = "
				+ imgHeight);
		System.out.println("ROOTVIEW WIDTH = " + rootView.getMeasuredWidth()
				+ " HEIGHT = " + rootView.getMeasuredHeight());

		int revisedWidth = imgWidth;
		int revisedHeight = imgHeight;

		float widthScale = ((float) rootWidth / (float) imgWidth);
		float heightScale = ((float) rootHeight / (float) imgHeight);

		if (widthScale < heightScale) {
			scaleU = widthScale;
			revisedHeight = (int) (scaleU * imgHeight);
			revisedWidth = (int) (scaleU * imgWidth);
		} else {
			scaleU = heightScale;
			revisedWidth = (int) (scaleU * imgWidth);
			revisedHeight = (int) (scaleU * imgHeight);
		}

		leftPadding = (rootWidth - revisedWidth) / 2;
		topPadding = (rootHeight - revisedHeight) / 2;

		System.out.println("INFO: SCLALE  = " + scaleU + " LEFT  = "
				+ leftPadding + " RIGHT  = " + topPadding);

		int parentWidth = rootView.getMeasuredWidth();
		int parentHeight = rootView.getMeasuredHeight();

		civWidth = 200;
		civHeight = 200;

		leftMargin = (parentWidth - civWidth) / 2;
		topMargin = (parentHeight - civHeight) / 2;

		//to draw the inside rectangle of selector
		
		civ = new CustomImageView(this, leftMargin, topMargin);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				civWidth, civHeight);
		lp.setMargins(leftMargin, topMargin, 0, 0);
		civ.setLayoutParams(lp);

		//to draw the rectangle handles to the corner
		
		pt1 = new Point((leftMargin - 30), (topMargin - 30));
		pt2 = new Point((leftMargin + civWidth - 30), (topMargin - 30));
		pt3 = new Point((leftMargin - 30), (topMargin + civHeight - 30));
		pt4 = new Point((leftMargin + civWidth - 30),
				(topMargin + civHeight - 30));

		final CornerImageView p1 = new CornerImageView(this, 100);
		final RelativeLayout.LayoutParams layoutParams_p1 = new RelativeLayout.LayoutParams(
				60, 60);
		layoutParams_p1.setMargins(pt1.x, pt1.y, 0, 0);
		p1.setLayoutParams(layoutParams_p1);
		cornerArrayList.add(p1);

		final CornerImageView p2 = new CornerImageView(this, 200);
		final RelativeLayout.LayoutParams layoutParams_p2 = new RelativeLayout.LayoutParams(
				60, 60);
		layoutParams_p2.setMargins(pt2.x, pt2.y, 0, 0);
		p2.setLayoutParams(layoutParams_p2);
		cornerArrayList.add(p2);

		final CornerImageView p3 = new CornerImageView(this, 300);
		final RelativeLayout.LayoutParams layoutParams_p3 = new RelativeLayout.LayoutParams(
				60, 60);
		layoutParams_p3.setMargins(pt3.x, pt3.y, 0, 0);
		p3.setLayoutParams(layoutParams_p3);
		cornerArrayList.add(p3);

		final CornerImageView p4 = new CornerImageView(this, 400);
		final RelativeLayout.LayoutParams layoutParams_p4 = new RelativeLayout.LayoutParams(
				60, 60);
		layoutParams_p4.setMargins(pt4.x, pt4.y, 0, 0);
		p4.setLayoutParams(layoutParams_p4);
		cornerArrayList.add(p4);

		cornerListener = new CornerTouchListener(civ, pt1, pt2, pt3, pt4, p1,
				p2, p3, p4);

		p1.setOnTouchListener(cornerListener);
		p2.setOnTouchListener(cornerListener);
		p3.setOnTouchListener(cornerListener);
		p4.setOnTouchListener(cornerListener);

		rootView.addView(civ, lp);
		rootView.addView(p1, layoutParams_p1);
		rootView.addView(p2, layoutParams_p2);
		rootView.addView(p3, layoutParams_p3);
		rootView.addView(p4, layoutParams_p4);

		//Touch Listener for the rectangle
		
		CustomImageTouchListener imageListener = new CustomImageTouchListener(
				leftMargin, topMargin, civWidth, civHeight);
		imageListener.setCornerPoint(p1, p2, p3, p4);
		civ.setOnTouchListener(imageListener);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.snip,
				rootView, false);
		rootView.addView(ll);
		findViewById(R.id.save_image).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						pathConfigure();
						rootView.removeView(civ);
						rootView.removeView(ll);
						rootView.removeView(p1);
						rootView.removeView(p2);
						rootView.removeView(p3);
						rootView.removeView(p4);
						zoneExists = false;
					}
				});

		findViewById(R.id.cancel_crop).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						setResult(RESULT_CANCELED);
						rootView.removeView(civ);
						rootView.removeView(ll);
						rootView.removeView(p1);
						rootView.removeView(p2);
						rootView.removeView(p3);
						rootView.removeView(p4);
						zoneExists = false;
					}
				});

		zoneExists = true;
	}

	//saving hotspot and it details to database
	
	private void saveHotspotInDatabase() {
		Image img = new Image(imageFilePath);
		dbHelper.addImage(img);
		for (Hotspot h : hotSpots) {
			dbHelper.addHotspot(h, img);
		}
		Toast.makeText(this, getResources().getString(R.string.hotspot_saved),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!hotSpots.isEmpty()) {
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
						AddHotspotToImage.this);

				alertBuilder.setTitle(getResources()
						.getString(R.string.hotspot));
				alertBuilder.setCancelable(false);
				alertBuilder.setMessage(getResources().getString(
						R.string.hotspot_save));
				alertBuilder.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								int pid = dbHelper.getImage(imageFilePath);
								dbHelper.deleteAllHotspot(pid);
								saveHotspotInDatabase();
								if (isEditable) {
									startHotspotDetailsActivity();
								}
								finish();
							}
						});
				alertBuilder.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (isEditable) {
									startHotspotDetailsActivity();
								}
								finish();
							}
						});
				AlertDialog dlg = alertBuilder.create();
				dlg.show();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	//show and hide hotspot to the screen
	
	private void toggleHotspots() {
		for (HotspotImageView x : hotspotIconForAdd) {
			if (x.imgView.equals(selectedImg))
				continue;
			if (isHotspotVisible)
				x.imgView.setVisibility(View.INVISIBLE);
			else
				x.imgView.setVisibility(View.VISIBLE);
		}
		isHotspotVisible = !isHotspotVisible;
	}
	
	//Hotspot position displacement over the image after device rotation 

	private void displacement() {
		if (counter < 40) {
			xDisp += 5;
			yDisp += 5;
			counter += 5;
		} else if (counter >= 40 && counter < 80) {
			xDisp += 5;
			yDisp -= 5;
			counter += 5;
		} else if (counter >= 80 && counter < 120) {
			xDisp -= 5;
			yDisp -= 5;
			counter += 5;
		} else if (counter >= 120 && counter < 160) {
			xDisp -= 5;
			yDisp += 5;
			counter += 5;
		} else {
			counter = 0;
			xDisp += 5;
			yDisp += 5;
		}
	}

	//Adding hotspot to the image
	
	private void addHotspot() {
		ImageView hotspot = new ImageView(this);
		hotspot.setImageResource(R.drawable.boundarym);
		hotspot.setClickable(true);
		hotspot.setEnabled(true);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				60, 60);
		displacement();

		Matrix tempMat = selectedImg.getImageMatrix();
		float[] curValues = new float[9];
		tempMat.getValues(curValues);

		float[] iniValues = new float[9];
		matrixInitial.set(new Matrix());
		matrixInitial.getValues(iniValues);

		float scale = iniValues[0] / curValues[0];

		float x0 = (AddHotspotToImage.this.getWindowManager()
				.getDefaultDisplay().getWidth()) / 2 - 25 + xDisp;
		float y0 = (AddHotspotToImage.this.getWindowManager()
				.getDefaultDisplay().getHeight()) / 2 - 25 + yDisp;

		layoutParams.leftMargin = Math.round(x0);
		layoutParams.topMargin = Math.round(y0);

		float relativeX = 0;
		float relativeY = 0;
		if (tempMat.equals(matrixInitial)) {

		} else {
			x0 += 30;
			y0 += 30;

			relativeX = (iniValues[2] + (x0 - curValues[2]) * scale);
			relativeY = (iniValues[5] + (y0 - curValues[5]) * scale);
		}

		xDelta = Math.round(x0) - layoutParams.leftMargin;
		yDelta = Math.round(y0) - layoutParams.topMargin;

		hotspot.setLayoutParams(layoutParams);
		hotspot.setOnTouchListener(this);

		hotspot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
		Hotspot hotspotProperty = new Hotspot(Math.round(relativeX - 30),
				Math.round(relativeY - 30), "", "", null, 0);
		rootView.addView(hotspot);
		HotspotImageView hotspotImageView = new HotspotImageView();
		hotspotImageView.imgView = hotspot;
		hotspotIconForAdd.add(hotspotImageView);
		hotSpots.add(hotspotProperty);
		hotspotImageView.hotspot = hotspotProperty;

		Log.d(Constants.ADD_HOTSPOT_LOGTAG,
				"Hotspot count after add button is clicked " + hotSpots.size());

		myView = hotspot;
		myView.setTag("MOVE");
	}

	float scale = 1.0f;
	boolean isTouchFirst = true;
	int clickCount = 0;
	long startTime;
	static final int MAX_DURATION = 300;
	long startTimeHotspot;
	boolean longPress = false;
	Point srtHotspot = new Point();

	
	//hotspot movement over image with user touch
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {

		Matrix tempMat = selectedImg.getImageMatrix();

		if (!isImageLoaded) {
			isImageLoaded = true;

			if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				matrixInitial.set(tempMat);
				matrixInitial.getValues(valuesInitial);
			} else {
				matrixInitial.set(new Matrix());
				matrixInitial.getValues(valuesInitial);
			}
		}

		final int X = (int) event.getRawX();
		final int Y = (int) event.getRawY();
		// find the index of the hotspot moved
		int index = -1, i = 0;
		for (HotspotImageView x : hotspotIconForAdd) {
			if (x.imgView.equals(view)) {
				index = i;
			}
			++i;
		}
		ImageView imgView = (ImageView) view;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:

			if (view == selectedImg) {

				if (clickCount <= 1)
					startTime = System.currentTimeMillis();

				clickCount++;

				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				// Log.d(TAG, "mode=DRAG"); // write to LogCat
				mode = DRAG;

				// selected hotspots is movable
				if (myView != null && myView.getTag() != null) {

					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myView
							.getLayoutParams();

					int[] location = new int[2];
					myView.getLocationOnScreen(location);

					xDelta = location[0] - layoutParams.leftMargin;
					yDelta = location[1] - layoutParams.topMargin;

					layoutParams.leftMargin = X - xDelta;
					layoutParams.topMargin = Y - yDelta;
					myView.setLayoutParams(layoutParams);

					int myViewIndex = -1, j = 0;
					for (HotspotImageView x : hotspotIconForAdd) {
						if (x.imgView.equals(myView)) {
							myViewIndex = j;
							break;
						}
						++j;
					}

					if (myViewIndex != -1) {

						int tempX = X - xDelta;
						int tempY = Y - yDelta;

						matrixInitial.set(new Matrix());

						float[] iniValues = new float[9];
						matrixInitial.getValues(iniValues);

						float[] curValues = new float[9];
						tempMat.getValues(curValues);
						float scale = iniValues[0] / curValues[0];

						tempX += 30;
						tempY += 30;

						tempX = Math
								.round((iniValues[2] + (tempX - curValues[2])
										* scale));
						tempY = Math
								.round((iniValues[5] + (tempY - curValues[5])
										* scale));

						hotSpots.get(myViewIndex).setXPosition(tempX - 30);
						hotSpots.get(myViewIndex).setYPosition(tempY - 30);
					}
				}
			} else {
				// Log.e("one touch", "one touch");
				srtHotspot.x = X;
				srtHotspot.y = Y;
				longPress = true;
				startTimeHotspot = System.currentTimeMillis();

				RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view
						.getLayoutParams();
				xDelta = X - lParams.leftMargin;
				yDelta = Y - lParams.topMargin;
			}

			break;

		case MotionEvent.ACTION_UP:
			long time = System.currentTimeMillis() - startTime;
			if (view == selectedImg) {
				;
				if (clickCount == 2) {
					if (time <= MAX_DURATION
							&& (myView == null || myView.getTag() == null)) {
						matrix.set(matrixInitial);
						savedMatrix.set(matrixInitial);
						imgView.setScaleType(ScaleType.FIT_CENTER);
						imgView.setImageMatrix(matrixInitial);
						// REMOVING ALL THE HOTSPOT OLDLY POSITIONED
						for (HotspotImageView hotspotImageView : hotspotIconForAdd) {
							rootView.removeView(hotspotImageView.imgView);
						}
						// hotspotIconForAdd.clear();
						resetHotspots();
					}
					clickCount = 0;
				}
				if (myView != null && myView.getTag() != null) {

				} else {
					updateHotspotPosition();
				}
			} else {

				if (longPress) {
					time = System.currentTimeMillis() - startTimeHotspot;
					if (time > 400) {
						myView = view;
						((ImageView) myView)
								.setImageResource(R.drawable.boundarys);
						myView.setTag(null);
						if ((hotSpots.get(index).getInputText().length() == 0)
								&& (hotSpots.get(index).getInputUrl().length() == 0)
								&& (hotSpots.get(index).getAudioFile() == null || hotSpots
										.get(index).getAudioFile().length() == 0)) {
							removeDialog(100);
							showDialog(100);
						} else {
							removeDialog(200);
							showDialog(200);
						}

					}
				}

			}
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			// Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 5f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				// Log.d(TAG, "mode=ZOOM");
			}

			for (HotspotImageView hotspotImageView : hotspotIconForAdd) {
				hotspotImageView.imgView.setVisibility(View.INVISIBLE);
				;
			}

			break;

		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			// Log.d(TAG, "mode=NONE");
			clickCount = 0;

			float[] mxValues = new float[9];
			matrix.getValues(mxValues);

			if (mxValues[0] < valuesInitial[0]) {
				matrix.set(matrixInitial);
				savedMatrix.set(matrixInitial);
				imgView.setScaleType(ScaleType.FIT_CENTER);
				imgView.setImageMatrix(matrixInitial);

			} else {

				RectF bmpRect = new RectF();
				bmpRect.right = selectedImg.getDrawable().getIntrinsicWidth();
				bmpRect.bottom = selectedImg.getDrawable().getIntrinsicHeight();
				matrix.mapRect(bmpRect);
				int bmpWidth = (int) bmpRect.width();
				int bmpHeight = (int) bmpRect.height();

				if (mxValues[2] + bmpWidth < selectedImg.getWidth()) {
					matrix.postTranslate(selectedImg.getWidth()
							- (mxValues[2] + bmpWidth), 0);
				}

				if (mxValues[5] + bmpHeight < selectedImg.getHeight()) {
					matrix.postTranslate(0, selectedImg.getHeight()
							- (mxValues[5] + bmpHeight));
				}

				if (mxValues[2] > 0) {
					matrix.postTranslate(-mxValues[2], 0);
				}

				if (mxValues[5] > 0) {
					matrix.postTranslate(0, -mxValues[5]);
				}

				imgView.setImageMatrix(matrix);
			}
			for (HotspotImageView hotspotImageView : hotspotIconForAdd) {
				hotspotImageView.imgView.setVisibility(View.VISIBLE);
				;
			}

			break;

		case MotionEvent.ACTION_MOVE:
			if (view == selectedImg) {

				if ((Math.abs(event.getX() - start.x) > 5)
						&& (Math.abs(event.getY() - start.y) > 5)) {
					clickCount = 0;
				}

				if (mode == DRAG) {
					// Log.e("Mode", "DRAG");
					scale = 1;
					matrix.set(savedMatrix);

					float[] matrixValues = new float[9];
					matrix.getValues(matrixValues);

					if (matrixValues[0] >= valuesInitial[0]) {

						Matrix temp = selectedImg.getImageMatrix();
						float[] tempOld = new float[9];
						temp.getValues(tempOld);

						matrix.postTranslate(event.getX() - start.x,
								event.getY() - start.y);

						matrixValues = new float[9];
						matrix.getValues(matrixValues);

						RectF bitmapRect = new RectF();
						bitmapRect.right = selectedImg.getDrawable()
								.getIntrinsicWidth();
						bitmapRect.bottom = selectedImg.getDrawable()
								.getIntrinsicHeight();

						matrix.mapRect(bitmapRect);

						int bitmapWidth = (int) bitmapRect.width();
						int bitmapHeight = (int) bitmapRect.height();

						if (bitmapWidth > selectedImg.getWidth()
								&& bitmapHeight > selectedImg.getHeight()) {

							if (matrixValues[2] > 0
									|| matrixValues[5] > 0
									|| matrixValues[2] + bitmapWidth < selectedImg
											.getWidth()
									|| matrixValues[5] + bitmapHeight < selectedImg
											.getHeight()) {

								matrix.set(temp);
							} else {
								imgView.setImageMatrix(matrix);
							}

						} else if (bitmapWidth < selectedImg.getWidth()
								&& bitmapHeight < selectedImg.getHeight()) {

							imgView.setScaleType(ScaleType.FIT_CENTER);
						}

						else if (bitmapWidth < selectedImg.getWidth()
								&& bitmapHeight > selectedImg.getHeight()) {
							if (matrixValues[2] < tempOld[2] + 10
									|| matrixValues[2] + bitmapWidth > selectedImg
											.getWidth() + tempOld[2] + 10
									|| matrixValues[5] > 0
									|| matrixValues[5] + bitmapHeight < selectedImg
											.getHeight()) {
								matrix.set(temp);
							} else {
								imgView.setImageMatrix(matrix);
							}

						} else if (bitmapHeight < selectedImg.getHeight()
								&& bitmapWidth > selectedImg.getWidth()) {
							if (matrixValues[5] < 0
									|| matrixValues[5] + bitmapHeight > selectedImg
											.getHeight()
									|| matrixValues[2] > 0
									|| matrixValues[2] + bitmapWidth < selectedImg
											.getWidth()) {
								matrix.set(temp);
							} else {
								imgView.setImageMatrix(matrix);
							}
						}
					}
				} else if (mode == ZOOM) {
					// Log.e("Mode", "ZOOM");
					float newDist = spacing(event);
					// Log.d(TAG, "newDist=" + newDist);
					if (newDist > 5f) {
						float[] iniValues = new float[9];
						matrixInitial.getValues(iniValues);

						float[] curValues = new float[9];
						matrix.getValues(curValues);

						if (valuesInitial[0] > curValues[0]) {
							// do nothing
						} else if (newDist > oldDist
								&& curValues[0] / valuesInitial[0] > 4f) {
							// do noting
						} else {
							scale = newDist / oldDist;
							if (scale > 0) {
								matrix.postScale(scale, scale, mid.x, mid.y);
								float[] matrixValues = new float[9];
								matrix.getValues(matrixValues);
								imgView.setScaleType(ScaleType.MATRIX);
								imgView.setImageMatrix(matrix);
							}

						}
					}
				}

			} else {

			}
			break;
		}

		if (view == selectedImg) {
			return true;

		} else {
			rootView.invalidate();
			return false;
		}
	}

	//update hotspot position after movement
	
	private void updateHotspotPosition() {

		matrixInitial.set(new Matrix());

		// GET THE CURRENT MATRIX
		Matrix tempMat = selectedImg.getImageMatrix();

		if (tempMat.equals(matrixInitial)) {

			return;
		}

		// GET THE INITIAL MARIX VALUES
		float[] iniValues = new float[9];
		matrixInitial.getValues(iniValues);

		// GET THE CURRENT MATRIX VALUES
		float[] curValues = new float[9];
		tempMat.getValues(curValues);

		// GET THE SCALE
		float scale = curValues[0] / iniValues[0];

		// ADDING ALL THE HOTSPOT NEWLY POSITIONED
		for (HotspotImageView hotspotImageView : hotspotIconForAdd) {

			if (hotspotImageView.hotspot == null) {
				continue;
			}

			// GET THE INITIAL HOTSPOT POSITION
			int x0 = hotspotImageView.hotspot.getXPosition();
			int y0 = hotspotImageView.hotspot.getYPosition();

			float relativeX;
			float relativeY;
			if (tempMat.equals(matrixInitial)) {
				relativeX = x0 + 30;
				relativeY = y0 + 30;
			} else {

				float x1 = x0 + 30;
				float y1 = y0 + 30;

				relativeX = (curValues[2] + (x1 - iniValues[2]) * scale);
				relativeY = (curValues[5] + (y1 - iniValues[5]) * scale);
			}

			RelativeLayout.LayoutParams layoutParams = (LayoutParams) hotspotImageView.imgView
					.getLayoutParams();
			layoutParams.leftMargin = Math.round(relativeX - 30);
			layoutParams.topMargin = Math.round(relativeY - 30);
			hotspotImageView.imgView.setLayoutParams(layoutParams);
			if (isHotspotVisible) {
				hotspotImageView.imgView.setVisibility(View.VISIBLE);
			} else {
				hotspotImageView.imgView.setVisibility(View.INVISIBLE);
			}
		}
	}

	//Reset the hotspot to the image
	
	private void resetHotspotsInView() {

		for (HotspotImageView hotspotImageView : hotspotIconForAdd) {
			rootView.removeView(hotspotImageView.imgView);
		}
		resetHotspots();
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	//hotspot info editing menu creation
	
	@Override
	public Dialog onCreateDialog(final int id) {

		isDialogOpen = true;

		for (int i = 0; i < hotspotIconForAdd.size(); i++) {

			HotspotImageView hotspotImageView = hotspotIconForAdd.get(i);
			if (hotspotImageView.imgView == myView) {
				index = i;
				break;
			}
		}

		//creating menu view
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		if (id == 100 && activeState == false) {
			reInitMedia();

			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			li.inflate(R.layout.dailog_in_editmode, null);

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final SlidingDrawer slidingDrawer = (SlidingDrawer) inflater
					.inflate(R.layout.hotspot_edit_drawer, rootView, false);
			Animation slide = AnimationUtils.loadAnimation(
					getApplicationContext(), R.drawable.drawer_anim);
			slidingDrawer.startAnimation(slide);
			slidingDrawer.open();
			rootView.addView(slidingDrawer);
			activeState = true;
			// slidingDrawer.setVisibility(View.VISIBLE);
			final TableLayout attach_rl = (TableLayout) findViewById(R.id.hotspot_attach);
			final RelativeLayout url_layout = (RelativeLayout) findViewById(R.id.hotspot_link_extended);
			url_layout.setVisibility(View.GONE);
			attach_rl.setVisibility(View.GONE);

			text = hotSpots.get(index).getInputText();
			url = hotSpots.get(index).getInputUrl();

			textEdit = (EditText) findViewById(R.id.round_text);
			urlEdit = (EditText) findViewById(R.id.et_round_text2);
			recordButton = (ImageView) findViewById(R.id.microphone);
			final ImageView attachMedia = (ImageView) findViewById(R.id.audio);
			final ImageView privacy = (ImageView) findViewById(R.id.privacy);
			final ImageView attachButton = (ImageView) findViewById(R.id.attach);
			ImageView linkButton = (ImageView) findViewById(R.id.link);
			ImageView saveButtonDlg = (ImageView) findViewById(R.id.saveButton);
			ImageView deleteButton = (ImageView) findViewById(R.id.deleteButton);
			ImageView backButton = (ImageView) findViewById(R.id.backButton);
			ImageView videoButton = (ImageView) findViewById(R.id.video);
			ImageView contactsButton = (ImageView) findViewById(R.id.contacts);
			ImageView locationButton = (ImageView) findViewById(R.id.location);
			ImageView shoppingButton = (ImageView) findViewById(R.id.shopping);

			if (isEditable) {
				mediaIdFlag = hotSpots.get(index).getMediaTypeFlag();
				if (mediaIdFlag == 2) {
					selectedMediaPath = hotSpots.get(index).getAudioFile();
				} else {
					audioFilePath = hotSpots.get(index).getAudioFile();
				}
				if (url != null && !url.isEmpty()) {
					url_layout.setVisibility(View.VISIBLE);
					TranslateAnimation slide1 = new TranslateAnimation(0, 0,
							100, 0);
					slide1.setDuration(200);
					url_layout.startAnimation(slide1);
					urlEdit.setText(url);
				} else {
					TranslateAnimation slide1 = new TranslateAnimation(0, 0,
							-70, 0);
					slide1.setDuration(200);
					textEdit.startAnimation(slide1);
					recordButton.startAnimation(slide1);
					attachButton.startAnimation(slide1);
					privacy.startAnimation(slide1);
					url_layout.startAnimation(slide1);
					url_layout.setVisibility(View.GONE);
				}
			}

			//to record audio info to the hotspot
			
			recordButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selectedMediaPath != null
							&& !selectedMediaPath.isEmpty()) {

						AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(
								AddHotspotToImage.this);
						alertDialogBuilder2
								.setMessage(
										getResources()
												.getString(
														R.string.overwrite_selected_media))
								.setCancelable(false)
								.setPositiveButton(android.R.string.yes,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {

												selectedMediaPath = null;
												mediaIdFlag = 0;

												if (!isRecording && canRecord) {
													recordButton
															.setImageResource(R.drawable.stop);
													startRecordAudio();

												} else if (isRecording
														&& mediaRecorder != null) {
													stopRecordAudio();
													recordButton
															.setImageResource(R.drawable.play);
													Log.v(AUDIO_SERVICE,
															"recording stopped now click to play");
												} else if (!isPlaying
														&& canPlay) {
													recordButton
															.setImageResource(R.drawable.stop);
													startPlayingRecordedAudio();

												} else if (isPlaying
														&& mediaPlayer != null) {
													stopPlayingRecordedAudio();
													recordButton
															.setImageResource(R.drawable.play);
													Log.v(AUDIO_SERVICE,
															"recording stop played");
												}
											}
										})
								.setNegativeButton(android.R.string.no,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						AlertDialog alertDialog2 = alertDialogBuilder2.create();
						alertDialog2.show();

					} else {
						if (!isRecording && canRecord) {
							recordButton.setImageResource(R.drawable.stop);
							startRecordAudio();

						} else if (isRecording && mediaRecorder != null) {
							stopRecordAudio();
							recordButton.setImageResource(R.drawable.play);
							Log.v(AUDIO_SERVICE,
									"Recording stopped.Click to play");

						} else if (!isPlaying && canPlay) {
							recordButton.setImageResource(R.drawable.stop);
							startPlayingRecordedAudio();

						} else if (isPlaying && mediaPlayer != null) {
							stopPlayingRecordedAudio();
							recordButton.setImageResource(R.drawable.play);
							Log.v(AUDIO_SERVICE, "recording stop played");
						}
						mediaIdFlag = 0;
					}
				}
			});

			if (hotSpots.get(index).getInputText() != null) {
				textEdit.setText(hotSpots.get(index).getInputText());
			}

			if (hotSpots.get(index).getAudioFile() != null
					&& hotSpots.get(index).getAudioFile() != "") {
				if (hotSpots.get(index).getMediaTypeFlag() == 2) {
					selectedMediaPath = hotSpots.get(index).getAudioFile();

				}
				if (hotSpots.get(index).getMediaTypeFlag() == 0) {
					audioFilePath = hotSpots.get(index).getAudioFile();
					recordButton.setImageResource(R.drawable.play);
					canPlay = true;
					isPlaying = false;
					canRecord = false;
				}
			}

			//attaching text and audio to selected hotspot
			
			attachButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (attach_rl.getVisibility() != View.VISIBLE) {
						Animation slideUp = AnimationUtils.loadAnimation(
								getApplicationContext(), R.drawable.menu_anim);
						attach_rl.setVisibility(View.VISIBLE);
						attach_rl.startAnimation(slideUp);
					} else {

						TranslateAnimation slide = new TranslateAnimation(0, 0,
								-100, 0);
						slide.setDuration(200);
						textEdit.startAnimation(slide);
						recordButton.startAnimation(slide);
						attachButton.startAnimation(slide);
						privacy.startAnimation(slide);
						if ((url_layout.getVisibility() == View.VISIBLE)) {
							url_layout.startAnimation(slide);
						}
						attach_rl.setVisibility(View.GONE);
					}
				}
			});

			//adding web link to the hotspot
			
			linkButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (url_layout.getVisibility() != View.VISIBLE) {
						url_layout.setVisibility(View.VISIBLE);
						TranslateAnimation slide = new TranslateAnimation(0, 0,
								70, 0);
						slide.setDuration(200);
						url_layout.startAnimation(slide);
						if (hotSpots.get(index).getInputUrl() != null)
							urlEdit.setText(hotSpots.get(index).getInputUrl());
					} else {
						urlData = urlEdit.getText().toString();
						if (urlData != null && !urlData.isEmpty()) {
							if (Utility.isValidUrl(urlData)) {
								hotSpots.get(index).setInputUrl(urlData);
								TranslateAnimation slide = new TranslateAnimation(
										0, 0, -70, 0);
								slide.setDuration(200);
								textEdit.startAnimation(slide);
								recordButton.startAnimation(slide);
								attachButton.startAnimation(slide);
								privacy.startAnimation(slide);
								if ((url_layout.getVisibility() == View.VISIBLE)) {
									url_layout.startAnimation(slide);
								}
								url_layout.setVisibility(View.GONE);
							} else {
								Toast.makeText(
										AddHotspotToImage.this,
										getResources().getString(
												R.string.invalid_url),
										Toast.LENGTH_LONG).show();
								urlEdit.setFocusable(true);
							}
						} else {
							TranslateAnimation slide = new TranslateAnimation(
									0, 0, -70, 0);
							slide.setDuration(200);
							textEdit.startAnimation(slide);
							recordButton.startAnimation(slide);
							attachButton.startAnimation(slide);
							privacy.startAnimation(slide);
							url_layout.startAnimation(slide);
							url_layout.setVisibility(View.GONE);
						}
					}
				}
			});
			
			//adding media to the hotspot

			attachMedia.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (audioFilePath != null) {

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								AddHotspotToImage.this);
						alertDialogBuilder.setTitle(getResources().getString(
								R.string.media_select));
						// set dialog message
						alertDialogBuilder
								.setMessage(
										getResources()
												.getString(
														R.string.overwite_recorded_audio))
								.setCancelable(false)
								.setPositiveButton(android.R.string.yes,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {

												startActivityToChooseAudio();
											}
										})
								.setNegativeButton(android.R.string.no,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						AlertDialog alertDialog = alertDialogBuilder.create();
						alertDialog.show();

					} else {
						startActivityToChooseAudio();
					}
				}
			});
 
			//save the added or edited info of the hotspot
			
			saveButtonDlg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// saveTheChange();
					hotSpots.get(index).setInputText(
							textEdit.getText().toString());
					urlData = urlEdit.getText().toString();

					if (urlData != null && !urlData.isEmpty()) {
						if (Utility.isValidUrl(urlData)) {
							hotSpots.get(index).setInputUrl(urlData);
						} else {
							Toast.makeText(
									AddHotspotToImage.this,
									getResources().getString(
											R.string.invalid_url),
									Toast.LENGTH_LONG).show();
							urlEdit.setFocusable(true);
						}
					} else {
						hotSpots.get(index).setInputUrl(urlData);
					}
					if (audioFilePath != null) {
						hotSpots.get(index).setAudioFile(audioFilePath);
					} else if (selectedMediaPath != null) {
						hotSpots.get(index).setAudioFile(selectedMediaPath);
					}
					hotSpots.get(index).setMediaTypeFlag(mediaIdFlag);
					handleEventBeforeFurtherAction();
					System.out.println("slidingDrawer=" + slidingDrawer);
					activeState = false;
					slidingDrawer.close();

					System.out.println("rootView=" + rootView);
					rootView.removeView(slidingDrawer);

					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
				}
			});

			//delete hotspot and info from the image
			
			deleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							AddHotspotToImage.this);
					alertDialogBuilder.setTitle(getResources().getString(
							R.string.hotspot));
					alertDialogBuilder
							.setMessage(
									getResources().getString(
											R.string.delete_options))
							.setCancelable(false)
							.setPositiveButton(
									getResources().getString(
											R.string.delete_hotspot),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {

											hotSpots.remove(index);
											hotspotIconForAdd.remove(index);
											if (myView.equals(selectedImg))
												imageLevelFlag = 0;
											else {
												rootView.removeViewInLayout(myView);
												Log.i("After remove ",
														hotSpots.size() + "");
											}
											handleEventBeforeFurtherAction();
											activeState = false;
											slidingDrawer.close();
											rootView.removeView(slidingDrawer);
										}
									})
							.setNeutralButton(
									getResources().getString(
											R.string.delete_properties),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											hotSpots.get(index)
													.setInputText("");
											hotSpots.get(index).setInputUrl("");
											hotSpots.get(index).setAudioFile(
													null);
											handleEventBeforeFurtherAction();
											activeState = false;
											slidingDrawer.close();
											rootView.removeView(slidingDrawer);
											removeDialog(100);
											showDialog(100);
										}
									})
							.setNegativeButton(android.R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											handleEventBeforeFurtherAction();
											activeState = false;
											slidingDrawer.close();
											rootView.removeView(slidingDrawer);
										}
									});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();

					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
				}
			});

			//get back from hotspot info menu
			
			backButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					handleEventBeforeFurtherAction();
					activeState = false;
					slidingDrawer.close();
					rootView.removeView(slidingDrawer);
					ImageView iv = (ImageView) myView;
					iv.setImageResource(R.drawable.boundaryd);
					iv.setTag(null);
					addButton.setImageResource(R.drawable.edit_b);

					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
				}
			});

			videoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(
							AddHotspotToImage.this,
							getResources().getString(R.string.video_annotation),
							Toast.LENGTH_LONG).show();

					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
				}
			});

			locationButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(AddHotspotToImage.this,
							getResources().getString(R.string.poi_location),
							Toast.LENGTH_LONG).show();

					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
				}
			});

			shoppingButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(AddHotspotToImage.this,
							getResources().getString(R.string.shopping_links),
							Toast.LENGTH_LONG).show();

					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
				}
			});

			contactsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(
							AddHotspotToImage.this,
							getResources().getString(
									R.string.assign_to_contacts),
							Toast.LENGTH_LONG).show();

					myView.setTag(null);
					activeState = false;
					slidingDrawer.close();
					rootView.removeView(slidingDrawer);
				}
			});

			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (isAnyChange()) {
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								AddHotspotToImage.this);
						alertDialogBuilder.setTitle(getResources().getString(
								R.string.hotspot));
						// set dialog message
						alertDialogBuilder
								.setMessage(
										getResources().getString(
												R.string.hotspot_save))
								.setCancelable(false)
								.setPositiveButton(android.R.string.yes,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												saveTheChange();
												dialog.cancel();
											}
										})
								.setNegativeButton(android.R.string.no,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						AlertDialog alertDialog = alertDialogBuilder.create();
						alertDialog.show();
					}
					slidingDrawer.close();
					rootView.removeView(slidingDrawer);

					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
					addButton.setImageResource(R.drawable.edit_b);
				}
			});
		} else if (id == 200) {

			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = li.inflate(R.layout.dailog_in_editmode_update, null);

			ImageView deleteButton = (ImageView) v
					.findViewById(R.id.xdeletebutton);
			ImageView editButton = (ImageView) v.findViewById(R.id.xeditbutton);
			ImageView moveButton = (ImageView) v.findViewById(R.id.xmovebutton);

			builder.setView(v);

			deleteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					rootView.removeViewInLayout(myView);
					dlg.dismiss();
					myView.setTag(null);
					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					addButton.setImageResource(R.drawable.edit_b);

					hotSpots.remove(index);
					hotspotIconForAdd.remove(index);
					if (myView.equals(selectedImg))
						imageLevelFlag = 0;
					else {
						rootView.removeViewInLayout(myView);
						Log.i("After remove ", hotSpots.size() + "");
					}
					handleEventBeforeFurtherAction();
				}

			});
			
			//to edit text info of hotspot

			editButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					addButton.setImageResource(R.drawable.edit_b);
					dlg.dismiss();
					removeDialog(100);
					showDialog(100);
				}
			});

			moveButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dlg.dismiss();
					((ImageView) myView).setImageResource(R.drawable.boundarym);
					myView.setTag("MOVE");
					addButton.setImageResource(R.drawable.edit_b_grayedout);
				}
			});

			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					dlg.dismiss();
					((ImageView) myView).setImageResource(R.drawable.boundaryd);
					myView.setTag(null);
					addButton.setImageResource(R.drawable.edit_b);
				}
			});

			dlg = builder.create();
			Window window = dlg.getWindow();
			WindowManager.LayoutParams wlp = window.getAttributes();
			wlp.gravity = Gravity.BOTTOM;
			wlp.y = 150;
			// wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			window.setAttributes(wlp);
			window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

			return dlg;

		}
		return super.onCreateDialog(id);
	}
	
	//to Play the recorded audio

	private void startPlayingRecordedAudio() {
		isPlaying = true;
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		mediaPlayer = new MediaPlayer();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(audioFilePath);
			mediaPlayer.setDataSource(fileInputStream.getFD());
			mediaPlayer.prepare();
			mediaPlayer.start();
			Log.v(AUDIO_SERVICE, "recording being played");
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					if (isPlaying && mediaPlayer != null) {
						stopPlayingRecordedAudio();
						recordButton.setImageResource(R.drawable.play);
						Log.v(AUDIO_SERVICE, "recording play completed");
					}
				}
			});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//to stop the playing audio

	private void stopPlayingRecordedAudio() {
		try {
			mediaPlayer.stop();
			mediaPlayer.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mediaPlayer = null;
		isPlaying = false;
	}

	//start audio recording
	
	private void startRecordAudio() {
		isRecording = true;
		if (mediaRecorder != null) {
			mediaRecorder.release();
		}
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		audioFilePath = Utility.getRecordedAudio(this);
		mediaRecorder.setOutputFile(audioFilePath);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mediaRecorder.prepare();
			mediaRecorder.start();
			Log.v(AUDIO_SERVICE, "record is happening");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//stop audio recording

	private void stopRecordAudio() {
		try {
			mediaRecorder.stop();
			mediaRecorder.release();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		mediaRecorder = null;
		isRecording = false;
		canPlay = true;
		canRecord = false;
	}

	private void reInitMedia() {
		canPlay = false;
		canRecord = true;
		isPlaying = false;
		isRecording = false;
	}

	//To save all the changes to the info
	
	private void saveTheChange() {
		hotSpots.get(index).setInputText(textEdit.getText().toString());
		urlData = urlEdit.getText().toString();

		if (urlData != null && !urlData.isEmpty()) {
			if (Utility.isValidUrl(urlData)) {
				hotSpots.get(index).setInputUrl(urlData);
			} else {
				Toast.makeText(AddHotspotToImage.this,
						getResources().getString(R.string.invalid_url),
						Toast.LENGTH_LONG).show();
				urlEdit.setFocusable(true);
			}
		} else {
			hotSpots.get(index).setInputUrl(urlData);
		}
		if (audioFilePath != null) {
			hotSpots.get(index).setAudioFile(audioFilePath);
		} else if (selectedMediaPath != null) {
			hotSpots.get(index).setAudioFile(selectedMediaPath);
		}
		hotSpots.get(index).setMediaTypeFlag(mediaIdFlag);
		handleEventBeforeFurtherAction();
		System.out.println("slidingDrawer=" + slidingDrawer);
		slidingDrawer.close();

		System.out.println("rootView=" + rootView);
		rootView.removeView(slidingDrawer);
	}

	//To load hotspot to the screen in landscape mode
	
	private void loadspotsInLandscape() {

		for (Hotspot h : hotSpots) {
			if (h.getXPosition() != 0 && h.getYPosition() != 0) {
				ImageView hotspot = new ImageView(this);
				hotspot.setImageResource(R.drawable.boundaryd);
				hotspot.setTag(null);

				addButton.setImageResource(R.drawable.edit_b);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
						60, 60);
				layoutParams.leftMargin = (int) h.getXPosition();
				layoutParams.topMargin = (int) h.getYPosition();
				hotspot.setLayoutParams(layoutParams);
				hotspot.setOnTouchListener(this);
				HotspotImageView hotspotImageView = new HotspotImageView();
				hotspotImageView.imgView = hotspot;
				hotspotImageView.hotspot = h;
				hotspotIconForAdd.add(hotspotImageView);
				hotspot.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});
				rootView.addView(hotspot);
			} else if (h.getXPosition() == 0 && h.getYPosition() == 0) {
				HotspotImageView hotspotImageView = new HotspotImageView();
				hotspotImageView.imgView = null;
				hotspotImageView.hotspot = h;
				hotspotIconForAdd.add(hotspotImageView);
				addButton.setOnLongClickListener(this);
				imageLevelFlag = 1;
			}
		}
	}
	
	//to reset hotspot after orientation

	private void resetHotspots() {

		for (Hotspot h : hotSpots) {
			if (h.getXPosition() != 0 && h.getYPosition() != 0) {

				ImageView hotspot = getImageViewFromHotspotImageView(h);

				addButton.setImageResource(R.drawable.edit_b);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
						60, 60);
				layoutParams.leftMargin = (int) h.getXPosition();
				layoutParams.topMargin = (int) h.getYPosition();
				System.out
						.println("now the the state of hotspot is :............."
								+ hotspot);
				hotspot.setOnTouchListener(this);
				HotspotImageView hotspotImageView = new HotspotImageView();
				hotspotImageView.imgView = hotspot;
				hotspotImageView.hotspot = h;
				hotspot.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});
				rootView.addView(hotspot);
			}

		}
	}

	//To create hotspot image
	
	private ImageView getImageViewFromHotspotImageView(Hotspot hotspot) {

		ImageView iv = null;

		for (HotspotImageView hotspotImageView : hotspotIconForAdd) {

			if (hotspotImageView.hotspot == hotspot) {
				iv = hotspotImageView.imgView;
				break;
			}
		}

		return iv;

	}
	
	//to draw hotspot to the image screen

	private void drawAvailableHotspot() {
		hotSpots.addAll(dbHelper.getAllHotspot(new Image(imageFilePath)));

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return;
		}

		if (!hotSpots.isEmpty()) {
			for (Hotspot h : hotSpots) {
				if (h.getXPosition() != 0 && h.getYPosition() != 0) {
					ImageView hotspot = new ImageView(this);
					hotspot.setImageResource(R.drawable.boundaryd);
					hotspot.setTag(null);
					RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
							60, 60);
					layoutParams.leftMargin = (int) h.getXPosition();
					layoutParams.topMargin = (int) h.getYPosition();
					hotspot.setLayoutParams(layoutParams);
					hotspot.setOnTouchListener(this);
					HotspotImageView hotspotImageView = new HotspotImageView();
					hotspotImageView.imgView = hotspot;
					hotspotImageView.hotspot = h;
					hotspotIconForAdd.add(hotspotImageView);
					hotspot.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

						}
					});

					rootView.addView(hotspot);
				} else if (h.getXPosition() == 0 && h.getYPosition() == 0) {
					HotspotImageView hotspotImageView = new HotspotImageView();
					hotspotImageView.imgView = null;
					hotspotImageView.hotspot = h;
					hotspotIconForAdd.add(hotspotImageView);
					addButton.setOnLongClickListener(this);
					imageLevelFlag = 1;
				}
			}
		} else {
			Toast.makeText(AddHotspotToImage.this,
					getResources().getString(R.string.no_hotspot),
					Toast.LENGTH_LONG).show();
		}
	}

	//to track any changes to hotspot info
	
	private boolean isAnyChange() {
		if (text.equals(textEdit.getText().toString())
				&& url.equals(urlEdit.getText().toString())) {
			return false;
		}
		return true;
	}

	private void handleEventBeforeFurtherAction() {
		if (isRecording && mediaRecorder != null) {
			stopRecordAudio();
			recordButton.setImageResource(R.drawable.microphone_icon);
			Log.v(AUDIO_SERVICE, "RStopped due to clicking save button");

		} else if (isPlaying && mediaPlayer != null) {
			stopPlayingRecordedAudio();
			recordButton.setImageResource(R.drawable.play);
			Log.v(AUDIO_SERVICE, "PStopped due to clicking save button");
		}
		audioFilePath = null;
		selectedMediaPath = null;
		reInitMedia();
	}

	//audio choose action
	
	private void startActivityToChooseAudio() {
		Intent intent = new Intent();
		intent.setType("audio/mp3");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(
				Intent.createChooser(intent, "Open Audio (mp3) file"),
				Constants.RQS_OPEN_AUDIO_MP3);
	}

	private void startHotspotDetailsActivity() {
		Intent intent = new Intent(AddHotspotToImage.this, HotspotDetails.class);
		intent.putExtra("imageName", imageFilePath);
		startActivity(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("imageName", imageFilePath);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		imageFilePath = savedInstanceState.getString("imageName");
	}
	
	//action for device orientation

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		selectedImg.setScaleType(ScaleType.FIT_CENTER);

		for (HotspotImageView hotspotImageView : hotspotIconForAdd) {
			hotspotImageView.imgView.setVisibility(View.INVISIBLE);
			;
		}

		_newOrientation = newConfig.orientation;

		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 500);

		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
		}

	}

	private void TimerMethod() {
		this.runOnUiThread(Timer_Tick);
	}

	private Runnable Timer_Tick = new Runnable() {
		public void run() {

			Matrix tm = selectedImg.getImageMatrix();
			savedMatrix.set(tm);
			tm.getValues(valuesInitial);

			if (_newOrientation == Configuration.ORIENTATION_PORTRAIT) {

			} else {
			}

			resetHotspotsInView();
			updateHotspotPosition();

		}
	};
}
