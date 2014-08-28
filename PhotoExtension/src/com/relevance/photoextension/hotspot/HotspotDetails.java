package com.relevance.photoextension.hotspot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TableLayout;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.relevance.photoextension.R;
import com.relevance.photoextension.dao.MySQLiteHelper;
import com.relevance.photoextension.model.Hotspot;
import com.relevance.photoextension.model.Image;
import com.relevance.photoextension.utility.Constants;
import com.relevance.photoextension.utility.Utility;

//Activity for view the hotspot and edit its metadata over selected image


@SuppressWarnings("deprecation")
public class HotspotDetails extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener {

	private final ArrayList<Hotspot> allHotspot = new ArrayList<Hotspot>();
	private final ArrayList<ImageView> hotspotIcon = new ArrayList<ImageView>();
	private ViewGroup rootView;
	private View myView;
	private ImageView selectedImg;
	private ImageView recordIcon;
	private ImageView view_button;
	private ImageView mediaIcon;
	private EditText editText;
	private EditText editUrl;
	private MySQLiteHelper dbHelper;
	private static MediaPlayer mediaPlayer = null;
	private static MediaRecorder mediaRecorder = null;
	private String text;
	private String url;
	private String imageFilePath;
	private String selectedMediaPath = "";
	private String audioFilePath = "";
	private Dialog dlg = null;
	private boolean canPlay = true;
	private boolean canRecord = false;
	private boolean isRecording = false;
	private boolean isPlaying = false;
	private boolean isMediaPlaying = false;
	private boolean isHotspotVisible = false;
	private int index = 0;
	private int mediaEditFlag = 0;
	private int mediaIdFlag = 0;
	private static int imageLevelFlag = 0;
	protected static int densityDpi;

	private static final String TAG = "Touch";
	@SuppressWarnings("unused")
	private static final float MIN_ZOOM = 1f, MAX_ZOOM = 1f;

	// These matrices will be used to scale points of the image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	Matrix matrixInitial = new Matrix();
	Matrix matrixInitialLand = new Matrix();
	Matrix matrixInitiaPort = new Matrix();
	float iniScale;

	boolean isLoadedFirstTime = true;
	boolean isDialogOpen = false;

	// The 3 states (events) which the user is trying to perform
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// these PointF objects are used to record the point(s) the user is touching
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	private ArrayList<ImageView> hotspots = new ArrayList<ImageView>();

	float[] valuesInitial = new float[9];
	private SlidingDrawer slidingDrawer;
	private boolean activeState = false;
	private Timer _timer;
	private int _orientation;
	private int _newOrientation;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private UiLifecycleHelper uiHelper;

	String tempFilePath1 = Environment.getExternalStorageDirectory()
			+ File.separator + "test1.bmp";

	HttpURLConnection conn = null;
	String upLoadServerUri = "http://www.gleecus.com/app/uploadToServerN.php";
	String sourceFile = Environment.getExternalStorageDirectory()
			+ File.separator + "test1.jpg";
	boolean uploadFile = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_hotspot);

		imageFilePath = getIntent().getStringExtra("imageName");

		rootView = (ViewGroup) findViewById(R.id.root);
		selectedImg = (ImageView) findViewById(R.id.imageCenter);
		view_button = (ImageView) findViewById(R.id.view_button);
		ImageView new_button = (ImageView) findViewById(R.id.view_button2);
		ImageView edit_button = (ImageView) findViewById(R.id.edit_button);
		ImageView share_button = (ImageView) findViewById(R.id.share_button);
		slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);

		view_button.setOnClickListener(this);
		view_button.setOnLongClickListener(this);
		new_button.setOnClickListener(this);
		edit_button.setOnClickListener(this);
		share_button.setOnClickListener(this);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		densityDpi = (int) (metrics.density * 160f);

		dbHelper = new MySQLiteHelper(getApplicationContext());

		drawHotspot();

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

			Utility.setImage(HotspotDetails.this, imageFilePath, selectedImg);

			if (isLoadedFirstTime) {
				isLoadedFirstTime = false;
				Matrix temp = selectedImg.getImageMatrix();
				matrixInitial.set(temp);
				matrix.set(temp);
				matrixInitialLand.set(temp);
				matrixInitiaPort.set(temp);

				matrixInitial.getValues(valuesInitial);

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

					float[] curValues = new float[9];
					float[] iniValues = new float[9];
					matrixInitialLand.getValues(curValues);
					matrixInitial = new Matrix();
					matrixInitial.getValues(iniValues);
					uxHotspot(curValues, iniValues);

				} else {

					float[] curValues = new float[9];
					float[] iniValues = new float[9];
					matrixInitiaPort.getValues(curValues);
					matrixInitial.set(new Matrix());
					matrixInitial.getValues(iniValues);
					uxHotspot(curValues, iniValues);

				}

			}
		}
	}

	//creating hotspot to the image to view
	
	private void drawHotspot() {

		allHotspot.addAll(dbHelper.getAllHotspot(new Image(imageFilePath)));

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return;
		}

		if (!allHotspot.isEmpty()) {
			for (Hotspot h : allHotspot) {
				if (h.getXPosition() != 0 && h.getYPosition() != 0) {
					ImageView hotspot = new ImageView(this);
					hotspot.setImageResource(R.drawable.pin);
					RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
							60, 60);
					layoutParams.leftMargin = (int) h.getXPosition();
					layoutParams.topMargin = (int) h.getYPosition();
					hotspot.setLayoutParams(layoutParams);
					hotspot.setVisibility(View.INVISIBLE);
					hotspotIcon.add(hotspot);
					hotspot.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							myView = v;
							removeDialog(100);
							showDialog(100);
						}
					});
					rootView.addView(hotspot);
				} else if (h.getXPosition() == 0 && h.getYPosition() == 0) {
					hotspotIcon.add(selectedImg);
					hotspots.add(selectedImg);
					view_button.setOnLongClickListener(this);
					imageLevelFlag = 1;
				}
			}
		} else {
			Toast.makeText(HotspotDetails.this,
					getResources().getString(R.string.no_hotspot),
					Toast.LENGTH_LONG).show();
		}
	}
	
	// finalizing the results

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK
				&& requestCode == Constants.RQS_OPEN_AUDIO_MP3) {
			Uri selectedMediaUri = data.getData();
			selectedMediaPath = Utility.getRealPathFromURI(this,
					selectedMediaUri);
			recordIcon.setImageResource(R.drawable.microphone_icon);
			audioFilePath = null;
			canRecord = true;
			canPlay = false;
			mediaIdFlag = 2;
		}

		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	//hotspot longclick listener
	
	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.view_button) {
			if (imageLevelFlag != 0) {
				myView = selectedImg;
				removeDialog(100);
				showDialog(100);
			} else {
				Toast.makeText(
						HotspotDetails.this,
						getResources()
								.getString(R.string.no_imagelevel_hotspot),
						Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.view_button:// to view all hotspot to image
			toggleHotspots();
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

			} else {
				updateHotspotPosition();
			}

			break;

		case R.id.view_button2:
			Toast.makeText(this,
					getResources().getString(R.string.new_feature),
					Toast.LENGTH_LONG).show();

			manageBitmap();

			break;

		case R.id.edit_button:// to edit hotspot info
			Intent intent = new Intent(HotspotDetails.this,
					AddHotspotToImage.class);
			intent.putExtra("imageName", imageFilePath);
			intent.putExtra("edit", true);
			startActivity(intent);
			finish();
			break;

		case R.id.share_button:// to share image in web

			manageBitmap();

			Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

			Session session = Session.getActiveSession();
			if (session == null) {
				uploadFile = true;
				if (session == null) {
					session = new Session(this);
				}
				Session.setActiveSession(session);
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));
			} else {
				uploadFile();
			}

			break;
		}
	}
	
	//show and hide hotspot

	private void toggleHotspots() {
		for (ImageView x : hotspots) {
			if (x.equals(selectedImg))
				continue;
			if (isHotspotVisible)
				x.setVisibility(View.INVISIBLE);
			else
				x.setVisibility(View.VISIBLE);
		}
		isHotspotVisible = !isHotspotVisible;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// update
		isDialogOpen = true;

		Dialog dialog = null;
		if (id == 100 && activeState == false) {
			dialog = createViewDialog();
		} else if (id == 200 && activeState == false) {
			dialog = createEditDialog();
		}
		return dialog;
	}
	
	
	//hotspot info editing menu creation

	private Dialog createViewDialog() {

		index = hotspots.indexOf(myView);

		isMediaPlaying = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		//creating menu view

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final SlidingDrawer slidingDrawer = (SlidingDrawer) inflater.inflate(
				R.layout.hotspot_view_drawer, rootView, false);
		Animation slide = AnimationUtils.loadAnimation(getApplicationContext(),
				R.drawable.drawer_anim);
		slidingDrawer.startAnimation(slide);
		slidingDrawer.open();
		rootView.addView(slidingDrawer);
		activeState = true;

		final TableLayout attach_rl = (TableLayout) findViewById(R.id.hotspot_attach);
		final RelativeLayout url_layout = (RelativeLayout) findViewById(R.id.hotspot_link_extended);
		attach_rl.setVisibility(View.GONE);
		url_layout.setVisibility(View.GONE);

		editText = (EditText) findViewById(R.id.round_text);
		editUrl = (EditText) findViewById(R.id.et_round_text2);
		recordIcon = (ImageView) findViewById(R.id.microphone);
		mediaIcon = (ImageView) findViewById(R.id.audio);
		ImageView webIcon = (ImageView) findViewById(R.id.web);
		final ImageView attachButton = (ImageView) findViewById(R.id.attach);
		ImageView cancelButton = (ImageView) findViewById(R.id.cancelButton);
		ImageView editButton = (ImageView) findViewById(R.id.editButton);
		ImageView videoButton = (ImageView) findViewById(R.id.video);
		ImageView contactsButton = (ImageView) findViewById(R.id.contacts);
		ImageView locationButton = (ImageView) findViewById(R.id.location);
		ImageView shoppingButton = (ImageView) findViewById(R.id.shopping);

		text = allHotspot.get(index).getInputText();
		url = allHotspot.get(index).getInputUrl();

		if (url != null && !url.isEmpty()) {
			url_layout.setVisibility(View.VISIBLE);
			TranslateAnimation slide1 = new TranslateAnimation(0, 0, 100, 0);
			slide1.setDuration(200);
			url_layout.startAnimation(slide1);
			editUrl.setText(url);
		}

		editText.setFocusable(false);
		editUrl.setFocusable(false);

		if (text != null)
			editText.setText(text);

		int mediaFlag = allHotspot.get(index).getMediaTypeFlag();
		if (mediaFlag == 0) {
			if (allHotspot.get(index).getAudioFile() != null
					&& !allHotspot.get(index).getAudioFile().isEmpty()) {
				recordIcon.setImageResource(R.drawable.play);
				recordIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (!isPlaying) {

							recordIcon.setImageResource(R.drawable.stop);
							startPlayingRecordedAudio();

						} else if (isPlaying && mediaPlayer != null) {

							stopPlayingRecordedAudio();
							recordIcon.setImageResource(R.drawable.play);
							Log.v(AUDIO_SERVICE, "recording stop played");
						}
					}
				});
			} else {
				recordIcon.setVisibility(View.INVISIBLE);
			}
			mediaIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg2) {
					Toast.makeText(
							HotspotDetails.this,
							getResources()
									.getString(R.string.no_audio_selected),
							Toast.LENGTH_SHORT).show();
				}
			});

		} else if (mediaFlag == 2) {
			recordIcon.setVisibility(View.INVISIBLE);
			if (allHotspot.get(index).getAudioFile() != null
					&& allHotspot.get(index).getAudioFile() != "") {

				mediaIcon.setImageResource(R.drawable.play_media3);
				mediaIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (!isMediaPlaying) {
							isMediaPlaying = true;
							mediaIcon.setImageResource(R.drawable.stop_media);
							startPlayingRecordedAudio();

						} else if (isMediaPlaying && mediaPlayer != null) {
							try {
								mediaPlayer.stop();
								mediaPlayer.release();
							} catch (Exception e) {
								e.printStackTrace();
							}
							mediaPlayer = null;
							isMediaPlaying = false;
							mediaIcon.setImageResource(R.drawable.play_media3);
							Log.v(AUDIO_SERVICE, "Play stopped");
						}
					}
				});
			} else {
				mediaIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg2) {
						Toast.makeText(
								HotspotDetails.this,
								getResources().getString(
										R.string.no_audio_selected),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
		
		//adding web link to the hotspot
		
		webIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String path = "";
				if (url != null) {
					if (!url.isEmpty()) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						if (!url.startsWith("http://")) {
							path = url.replace(url, "http://" + url);
						} else {
							path = url;
						}
						intent.setData(Uri.parse(path));
						startActivity(intent);
					} else {
						Toast.makeText(HotspotDetails.this,
								getResources().getString(R.string.empty_url),
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		//adding file link to the hotspot
		
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
					editText.startAnimation(slide);
					editUrl.startAnimation(slide);
					recordIcon.startAnimation(slide);
					attachButton.startAnimation(slide);
					ImageView privacy1 = (ImageView) findViewById(R.id.privacy);
					privacy1.startAnimation(slide);
					if ((url_layout.getVisibility() == View.VISIBLE)) {
						url_layout.startAnimation(slide);
					}
					attach_rl.setVisibility(View.GONE);

				}
			}
		});


		//canclel playing audio 
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((isPlaying || isMediaPlaying) && mediaPlayer != null) {
					stopPlayingRecordedAudio();
					recordIcon.setImageResource(R.drawable.play);
					Log.v(AUDIO_SERVICE, "recording played cancelled");
				}
				activeState = false;
				slidingDrawer.close();

				System.out.println("rootView=" + rootView);
				rootView.removeView(slidingDrawer);
				removeDialog(100);
			}
		});

		//edit playing audio
		
		editButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((isPlaying || isMediaPlaying) && mediaPlayer != null) {
					stopPlayingRecordedAudio();
					recordIcon.setImageResource(R.drawable.play);
					Log.v(AUDIO_SERVICE,
							"recording played cancelled due to edit");
				}
				activeState = false;
				slidingDrawer.close();

				System.out.println("rootView=" + rootView);
				rootView.removeView(slidingDrawer);
				removeDialog(100);
				removeDialog(200);
				showDialog(200);
			}
		});

		videoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.video_annotation),
						Toast.LENGTH_LONG).show();
			}
		});

		locationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.poi_location),
						Toast.LENGTH_LONG).show();
			}
		});

		shoppingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.shopping_links),
						Toast.LENGTH_LONG).show();
			}
		});

		contactsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.assign_to_contacts),
						Toast.LENGTH_LONG).show();
			}
		});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if ((isPlaying || isMediaPlaying) && mediaPlayer != null) {
					stopPlayingRecordedAudio();
					recordIcon.setImageResource(R.drawable.play);
					Log.v(AUDIO_SERVICE, "recording played cancelled");
				}
				activeState = false;
				slidingDrawer.close();

				System.out.println("rootView=" + rootView);
				rootView.removeView(slidingDrawer);
				removeDialog(100);
			}
		});
		return dlg;
	}
	
	//creting menu to hotspot info

	private Dialog createEditDialog() {

		reInitMedia();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		//creating menu view
		
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = li.inflate(R.layout.dailog_in_editmode, null);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final SlidingDrawer slidingDrawer = (SlidingDrawer) inflater.inflate(
				R.layout.hotspot_edit_drawer, rootView, false);
		Animation slide = AnimationUtils.loadAnimation(getApplicationContext(),
				R.drawable.drawer_anim);
		slidingDrawer.startAnimation(slide);
		slidingDrawer.open();
		rootView.addView(slidingDrawer);
		activeState = true;

		final TableLayout attach_rl = (TableLayout) findViewById(R.id.hotspot_attach);
		final RelativeLayout url_layout = (RelativeLayout) findViewById(R.id.hotspot_link_extended);
		url_layout.setVisibility(View.GONE);
		attach_rl.setVisibility(View.GONE);

		editText = (EditText) findViewById(R.id.round_text);
		editUrl = (EditText) findViewById(R.id.et_round_text2);
		recordIcon = (ImageView) findViewById(R.id.microphone);
		final ImageView privacy = (ImageView) findViewById(R.id.privacy);
		final ImageView attachButton = (ImageView) findViewById(R.id.attach);
		ImageView link_icon = (ImageView) findViewById(R.id.link);
		ImageView mediaIcon = (ImageView) findViewById(R.id.audio);
		ImageView saveButton = (ImageView) findViewById(R.id.saveButton);
		ImageView deleteButton = (ImageView) findViewById(R.id.deleteButton);
		ImageView backButton = (ImageView) findViewById(R.id.backButton);
		ImageView videoButton = (ImageView) findViewById(R.id.video);
		ImageView contactsButton = (ImageView) findViewById(R.id.contacts);
		ImageView locationButton = (ImageView) findViewById(R.id.location);
		ImageView shoppingButton = (ImageView) findViewById(R.id.shopping);

		text = allHotspot.get(index).getInputText();
		url = allHotspot.get(index).getInputUrl();

		if (text != null)
			editText.setText(text);
		if (url != null)
			editUrl.setText(url);

		if (allHotspot.get(index).getAudioFile() != null
				&& allHotspot.get(index).getAudioFile() != "") {
			if (allHotspot.get(index).getMediaTypeFlag() == 0) {
				recordIcon.setImageResource(R.drawable.play);
				audioFilePath = allHotspot.get(index).getAudioFile();
				isPlaying = false;
				canPlay = true;
				isRecording = false;
				canRecord = false;
				mediaIdFlag = 0;
			} else if (allHotspot.get(index).getMediaTypeFlag() == 2) {
				selectedMediaPath = allHotspot.get(index).getAudioFile();
				mediaIdFlag = 2;
			}
		}

		if (allHotspot.get(index).getInputText() != null
				&& allHotspot.get(index).getInputText() != "") {
			editText.setText(allHotspot.get(index).getInputText());
		}
		if (allHotspot.get(index).getInputUrl() != null
				&& allHotspot.get(index).getInputUrl() != "") {
			editUrl.setText(allHotspot.get(index).getInputUrl());
		}

		//to record audio info to the hotspot
		
		recordIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedMediaPath != null && !selectedMediaPath.isEmpty()) {

					AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(
							HotspotDetails.this);
					// set dialog message
					alertDialogBuilder2
							.setMessage(
									getResources().getString(
											R.string.overwrite_selected_media))
							.setCancelable(false)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											selectedMediaPath = null;
											mediaIdFlag = 0;
											mediaEditFlag++;

											if (!isRecording && canRecord) {
												recordIcon
														.setImageResource(R.drawable.stop);
												startRecordAudio();

											} else if (isRecording
													&& mediaRecorder != null) {
												stopRecordAudio();
												recordIcon
														.setImageResource(R.drawable.play);
												Log.v(AUDIO_SERVICE,
														"recording stopped now click to play");

											} else if (!isPlaying && canPlay) {
												recordIcon
														.setImageResource(R.drawable.stop);
												startPlayingRecordedAudio();

											} else if (isPlaying
													&& mediaPlayer != null) {
												stopPlayingRecordedAudio();
												recordIcon
														.setImageResource(R.drawable.play);
												Log.v(AUDIO_SERVICE,
														"recording stop played");
											}
										}
									})
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alertDialog2 = alertDialogBuilder2.create();
					alertDialog2.show();

				} else {
					if (!isRecording && canRecord) {
						recordIcon.setImageResource(R.drawable.stop);
						startRecordAudio();

					} else if (isRecording && mediaRecorder != null) {
						stopRecordAudio();
						recordIcon.setImageResource(R.drawable.play);
						Log.v(AUDIO_SERVICE,
								"recording stopped now click to play");

					} else if (!isPlaying && canPlay) {
						recordIcon.setImageResource(R.drawable.stop);
						startPlayingRecordedAudio();

					} else if (isPlaying && mediaPlayer != null) {
						stopPlayingRecordedAudio();
						recordIcon.setImageResource(R.drawable.play);
						Log.v(AUDIO_SERVICE, "recording stop played");
					}
					mediaIdFlag = 0;
					mediaEditFlag++;
				}
			}
		});
		
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
					editText.startAnimation(slide);
					recordIcon.startAnimation(slide);
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

		link_icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (url_layout.getVisibility() != View.VISIBLE) {
					url_layout.setVisibility(View.VISIBLE);
					TranslateAnimation slide = new TranslateAnimation(0, 0, 70,
							0);
					slide.setDuration(200);
					url_layout.startAnimation(slide);
					if (url != null && !url.isEmpty())
						editUrl.setText(url);
				} else {
					String urlData = editUrl.getText().toString();
					if (urlData != null && !urlData.isEmpty()) {
						if (Utility.isValidUrl(urlData)) {
							url = urlData;
							TranslateAnimation slide = new TranslateAnimation(
									0, 0, -70, 0);
							slide.setDuration(200);
							editText.startAnimation(slide);
							recordIcon.startAnimation(slide);
							attachButton.startAnimation(slide);
							privacy.startAnimation(slide);
							if ((url_layout.getVisibility() == View.VISIBLE)) {
								url_layout.startAnimation(slide);
							}
							url_layout.setVisibility(View.GONE);
						} else {
							Toast.makeText(
									HotspotDetails.this,
									getResources().getString(
											R.string.invalid_url),
									Toast.LENGTH_LONG).show();
							editUrl.setFocusable(true);
						}
					} else {
						TranslateAnimation slide = new TranslateAnimation(0, 0,
								-70, 0);
						slide.setDuration(200);
						editText.startAnimation(slide);
						recordIcon.startAnimation(slide);
						attachButton.startAnimation(slide);
						privacy.startAnimation(slide);
						if ((url_layout.getVisibility() == View.VISIBLE)) {
							url_layout.startAnimation(slide);
						}
						url_layout.setVisibility(View.GONE);
					}
				}
			}
		});
		
		//adding media to the hotspot

		mediaIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (audioFilePath != null) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							HotspotDetails.this);
					alertDialogBuilder.setTitle(getResources().getString(
							R.string.media_select));
					// set dialog message
					alertDialogBuilder
							.setMessage(
									getResources().getString(
											R.string.overwrite_selected_media))
							.setCancelable(false)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {

											startActivityToChooseAudio();
										}
									})
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
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
		
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				url = editUrl.getText().toString();
				if (!url.isEmpty()) {
					if (Utility.isValidUrl(url)) {
						allHotspot.get(index).setInputText(
								editText.getText().toString());
						allHotspot.get(index).setInputUrl(
								editUrl.getText().toString());
						if (mediaIdFlag == 2) {
							allHotspot.get(index).setAudioFile(
									selectedMediaPath);
							allHotspot.get(index).setMediaTypeFlag(2);
						} else if (mediaIdFlag == 0) {
							allHotspot.get(index).setAudioFile(audioFilePath);
							allHotspot.get(index).setMediaTypeFlag(0);
						}
						dbHelper.updateHotspot(allHotspot.get(index),
								new Image(imageFilePath));
						allHotspot.addAll(dbHelper.getAllHotspot(new Image(
								imageFilePath)));
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.successfull_updation),
								Toast.LENGTH_SHORT).show();

						mediaEditFlag = 0;
						handleEventBeforeFurtherAction();
						// dlg.dismiss();
						activeState = false;
						slidingDrawer.close();

						System.out.println("rootView=" + rootView);
						rootView.removeView(slidingDrawer);
						removeDialog(200);
					} else {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.invalid_url),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					allHotspot.get(index).setInputText(
							editText.getText().toString());
					allHotspot.get(index).setInputUrl(
							editUrl.getText().toString());
					if (mediaIdFlag == 2) {
						allHotspot.get(index).setAudioFile(selectedMediaPath);
						allHotspot.get(index).setMediaTypeFlag(2);
					} else if (mediaIdFlag == 0) {
						allHotspot.get(index).setAudioFile(audioFilePath);
						allHotspot.get(index).setMediaTypeFlag(0);
					}
					dbHelper.updateHotspot(allHotspot.get(index), new Image(
							imageFilePath));
					allHotspot.addAll(dbHelper.getAllHotspot(new Image(
							imageFilePath)));
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.successfull_updation),
							Toast.LENGTH_SHORT).show();

					mediaEditFlag = 0;
					handleEventBeforeFurtherAction();
					activeState = false;
					slidingDrawer.close();

					System.out.println("rootView=" + rootView);
					rootView.removeView(slidingDrawer);
					removeDialog(200);
				}

			}
		});
		
		//delete hotspot and info from the image

		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						HotspotDetails.this);
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
									public void onClick(DialogInterface dialog,
											int id) {
										if (myView.equals(selectedImg))
											imageLevelFlag = 0;
										if (!myView.equals(selectedImg)) {
											rootView.removeViewInLayout(myView);
											Log.d("After remove ",
													allHotspot.size() + "");
										}
										int pid = dbHelper
												.getImage(imageFilePath);
										dbHelper.deleteHotspot(
												allHotspot.get(index), pid);
										// hotspotIcon.remove(index);
										hotspots.remove(index);
										allHotspot.remove(index);
										allHotspot.clear();
										allHotspot.addAll(dbHelper
												.getAllHotspot(new Image(
														imageFilePath)));
										Toast.makeText(
												HotspotDetails.this,
												getResources()
														.getString(
																R.string.hotspot_deleted),
												Toast.LENGTH_LONG).show();
										handleEventBeforeFurtherAction();
										activeState = false;
										slidingDrawer.close();

										System.out.println("rootView="
												+ rootView);
										rootView.removeView(slidingDrawer);
										removeDialog(200);
									}
								})
						.setNeutralButton(
								getResources().getString(
										R.string.delete_properties),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

										allHotspot.get(index).setInputText("");
										allHotspot.get(index).setInputUrl("");
										allHotspot.get(index)
												.setAudioFile(null);

										String imageFilePath = getIntent()
												.getStringExtra("imageName");
										dbHelper.updateHotspot(
												allHotspot.get(index),
												new Image(imageFilePath));
										allHotspot.addAll(dbHelper
												.getAllHotspot(new Image(
														imageFilePath)));

										handleEventBeforeFurtherAction();
										activeState = false;
										slidingDrawer.close();

										System.out.println("rootView="
												+ rootView);
										rootView.removeView(slidingDrawer);
										removeDialog(200);
										showDialog(200);
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										handleEventBeforeFurtherAction();
										activeState = false;
										slidingDrawer.close();

										System.out.println("rootView="
												+ rootView);
										rootView.removeView(slidingDrawer);
									}
								});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		});
		
		//get back from hotspot info menu

		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleEventBeforeFurtherAction();
				activeState = false;
				slidingDrawer.close();

				System.out.println("rootView=" + rootView);
				rootView.removeView(slidingDrawer);
				removeDialog(200);
			}
		});

		videoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.video_annotation),
						Toast.LENGTH_LONG).show();
			}
		});

		locationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.poi_location),
						Toast.LENGTH_LONG).show();
			}
		});

		shoppingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.shopping_links),
						Toast.LENGTH_LONG).show();
			}
		});

		contactsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(HotspotDetails.this,
						getResources().getString(R.string.assign_to_contacts),
						Toast.LENGTH_LONG).show();
			}
		});
		
		//to cancel the menu

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (isAnyChange()) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							HotspotDetails.this);

					alertDialogBuilder.setTitle(getResources().getString(
							R.string.hotspot));
					alertDialogBuilder
							.setMessage(
									getResources().getString(
											R.string.hotspot_save))
							.setCancelable(false)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											saveTheChange();
											dialog.cancel();
										}
									})
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				}
				activeState = false;
				slidingDrawer.close();

				System.out.println("rootView=" + rootView);
				rootView.removeView(slidingDrawer);
			}
		});
		return dlg;
	}
	
	//to save the new info

	private void saveTheChange() {

		url = editUrl.getText().toString();
		if (!url.isEmpty()) {
			if (Utility.isValidUrl(url)) {
				allHotspot.get(index).setInputText(
						editText.getText().toString());
				allHotspot.get(index).setInputUrl(editUrl.getText().toString());
				if (mediaIdFlag == 2) {
					allHotspot.get(index).setAudioFile(selectedMediaPath);
					allHotspot.get(index).setMediaTypeFlag(2);
				} else if (mediaIdFlag == 0) {
					allHotspot.get(index).setAudioFile(audioFilePath);
					allHotspot.get(index).setMediaTypeFlag(0);
				}
				dbHelper.updateHotspot(allHotspot.get(index), new Image(
						imageFilePath));
				allHotspot.addAll(dbHelper.getAllHotspot(new Image(
						imageFilePath)));
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.successfull_updation),
						Toast.LENGTH_SHORT).show();

				mediaEditFlag = 0;
				handleEventBeforeFurtherAction();
				removeDialog(200);
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.invalid_url),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			allHotspot.get(index).setInputText(editText.getText().toString());
			allHotspot.get(index).setInputUrl(editUrl.getText().toString());
			if (mediaIdFlag == 2) {
				allHotspot.get(index).setAudioFile(selectedMediaPath);
				allHotspot.get(index).setMediaTypeFlag(2);
			} else if (mediaIdFlag == 0) {
				allHotspot.get(index).setAudioFile(audioFilePath);
				allHotspot.get(index).setMediaTypeFlag(0);
			}
			dbHelper.updateHotspot(allHotspot.get(index), new Image(
					imageFilePath));
			allHotspot.addAll(dbHelper.getAllHotspot(new Image(imageFilePath)));
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.successfull_updation),
					Toast.LENGTH_SHORT).show();

			mediaEditFlag = 0;
			handleEventBeforeFurtherAction();
			dlg.dismiss();
			removeDialog(200);
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

	//to stop the playing audio
	
	private void stopPlayingRecordedAudio() {
		try {
			mediaPlayer.stop();
			mediaPlayer.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mediaPlayer = null;
		if (isMediaPlaying) {
			isMediaPlaying = false;
		}
		if (isPlaying) {
			isPlaying = false;
		}
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
			Log.v(AUDIO_SERVICE, "recording is happenin");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startPlayingRecordedAudio() {
		if (!isMediaPlaying)
			isPlaying = true;
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		mediaPlayer = new MediaPlayer();
		FileInputStream fileInputStream = null;
		try {
			if (mediaEditFlag > 0) {
				fileInputStream = new FileInputStream(audioFilePath);
				mediaPlayer.setDataSource(fileInputStream.getFD());
			} else {
				fileInputStream = new FileInputStream(allHotspot.get(index)
						.getAudioFile());
				mediaPlayer.setDataSource(fileInputStream.getFD());
			}
			mediaPlayer.prepare();
			mediaPlayer.start();
			Log.v(AUDIO_SERVICE, "recording being played");
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					if (isPlaying || isMediaPlaying && mediaPlayer != null) {
						if (isPlaying)
							recordIcon.setImageResource(R.drawable.play);
						if (isMediaPlaying)
							mediaIcon.setImageResource(R.drawable.play_media3);
						stopPlayingRecordedAudio();
						Log.v(AUDIO_SERVICE, "recording completed");
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

	private void reInitMedia() {
		canPlay = false;
		canRecord = true;
		isPlaying = false;
		isRecording = false;
	}

	private void startActivityToChooseAudio() {
		Intent intent = new Intent();
		intent.setType("audio/mp3");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(
				Intent.createChooser(intent, "Open Audio (mp3) file"),
				Constants.RQS_OPEN_AUDIO_MP3);
	}

	private boolean isAnyChange() {
		if (text.equals(editText.getText().toString())
				&& url.equals(editUrl.getText().toString())) {
			return false;
		}
		return true;
	}

	private void handleEventBeforeFurtherAction() {
		if (isRecording && mediaRecorder != null) {
			stopRecordAudio();
			recordIcon.setImageResource(R.drawable.microphone_icon);
			Log.v(AUDIO_SERVICE, "RStopped due to improper exit");
		} else if (isPlaying && mediaPlayer != null) {
			stopPlayingRecordedAudio();
			recordIcon.setImageResource(R.drawable.play);
			Log.v(AUDIO_SERVICE, "PStopped due to improper exit");
		}
		audioFilePath = null;
		selectedMediaPath = null;
		reInitMedia();
	}

	void dumpValue(ImageView view, String msc) {

		float[] values = new float[9];
		Matrix tempMat = view.getImageMatrix();
		tempMat.getValues(values);

		String msgOx = Integer.toString((int) values[2]);
		String msgOy = Integer.toString((int) values[5]);

		// Log.e(msc + " X ", msgOx);
		// Log.e(msc + " Y ", msgOy);
	}

	float scale = 1.0f;

	boolean isTouchFirst = true;

	int clickCount = 0;
	long startTime;
	static final int MAX_DURATION = 800;

	//to zoom and move the image
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		view.setScaleType(ImageView.ScaleType.MATRIX);

		float[] te = new float[9];
		matrix.getValues(te);

		Log.e("Matrix values", Float.toString(te[0]));

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: // first finger down only

			if (clickCount == 0)
				startTime = System.currentTimeMillis();

			clickCount++;

			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG"); // write to LogCat
			mode = DRAG;
			break;

		case MotionEvent.ACTION_UP: // first finger lifted
			long time = System.currentTimeMillis() - startTime;
			if (clickCount == 2) {
				if (time <= MAX_DURATION) {
					matrix.set(matrixInitial);
					savedMatrix.set(matrixInitial);
					view.setImageMatrix(matrixInitial);
					view.setScaleType(ScaleType.FIT_CENTER);

					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						float[] curValues = new float[9];
						float[] iniValues = new float[9];
						matrixInitialLand.getValues(curValues);

						matrixInitial = new Matrix();
						matrixInitial.getValues(iniValues);
						uxHotspot(curValues, iniValues);

					} else {
						float[] curValues = new float[9];
						float[] iniValues = new float[9];
						matrixInitiaPort.getValues(curValues);

						matrixInitial.set(new Matrix());
						matrixInitial.getValues(iniValues);
						uxHotspot(curValues, iniValues);
					}

				}
				clickCount = 0;
			}
			return true;

		case MotionEvent.ACTION_POINTER_UP: // second finger lifted
			mode = NONE;
			Log.d(TAG, "mode=NONE");
			clickCount = 0;
			float[] mxValues = new float[9];
			matrix.getValues(mxValues);

			if (mxValues[0] < valuesInitial[0]) {

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					matrix.set(matrixInitialLand);
					savedMatrix.set(matrixInitialLand);
				} else {
					matrix.set(matrixInitiaPort);
					savedMatrix.set(matrixInitiaPort);
				}
				view.setImageMatrix(matrixInitial);
				view.setScaleType(ScaleType.FIT_CENTER);
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

				view.setImageMatrix(matrix);
			}
			updateHotspotPosition();
			break;

		case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 5f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM");
			}
			break;

		case MotionEvent.ACTION_MOVE:

			// Log.e("DIS", Float.toString((int)event.getX() - start.x));
			// Log.e("DIS", Float.toString((int)event.getY() - start.y));

			if ((Math.abs(event.getX() - start.x) > 5)
					&& (Math.abs(event.getY() - start.y) > 5)) {
				clickCount = 0;
			}

			float[] matrixValues = new float[9];
			matrix.getValues(matrixValues);

			if (mode == DRAG) {
				// Log.e("Mode", "DRAG");
				scale = 1;
				matrix.set(savedMatrix);

				if (matrixValues[0] >= iniScale) {

					Matrix temp = selectedImg.getImageMatrix();
					float[] tempVals = new float[9];
					temp.getValues(tempVals);

					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);

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
							view.setImageMatrix(matrix);
							updateHotspotPosition();
						}

					} else if (bitmapWidth < selectedImg.getWidth()
							&& bitmapHeight < selectedImg.getHeight()) {

						if (matrixValues[2] < 0
								|| matrixValues[5] < 0
								|| matrixValues[2] + bitmapWidth > selectedImg
										.getWidth()
								|| matrixValues[5] + bitmapHeight > selectedImg
										.getHeight()) {

							matrix.set(temp);
						} else {
							view.setImageMatrix(matrix);
							updateHotspotPosition();
						}
					}

					else if (bitmapWidth < selectedImg.getWidth()
							&& bitmapHeight > selectedImg.getHeight()) {
						if (matrixValues[2] < 0
								|| matrixValues[2] + bitmapWidth > selectedImg
										.getWidth()
								|| matrixValues[5] > 0
								|| matrixValues[5] + bitmapHeight < selectedImg
										.getHeight()) {
							matrix.set(temp);
						} else {
							view.setImageMatrix(matrix);
							updateHotspotPosition();
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
							view.setImageMatrix(matrix);
							updateHotspotPosition();
						}
					}
				}
			} else if (mode == ZOOM) {
				Log.e("Mode", "ZOOM");

				if (matrixValues[0] >= valuesInitial[0]) {

					float newDist = spacing(event);
					Log.d(TAG, "newDist=" + newDist);
					if (newDist > 5f) {
						matrix.set(savedMatrix);
						scale = newDist / oldDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
						view.setImageMatrix(matrix);
					}
				}
				break;
			}

		}

		return true; // indicate event was handled
	}

	private void uxHotspot(float[] curValues, float[] iniValues) {

		if (_orientation == Configuration.ORIENTATION_LANDSCAPE) {

			// hack
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentapiVersion < android.os.Build.VERSION_CODES.KITKAT) {
				curValues[0] /= 2;
			}

		}

		// GET THE SCALE
		float scale = curValues[0] / iniValues[0];

		// REMOVING ALL THE HOTSPOT OLDLY POSITIONED
		for (ImageView imgView : hotspotIcon) {
			rootView.removeView(imgView);
		}

		hotspots.clear();
		hotspotIcon.clear();

		// ADDING ALL THE HOTSPOT NEWLY POSITIONED
		for (Hotspot hotspot : allHotspot) {

			// GET THE INITIAL HOTSPOT POSITION
			int x0 = hotspot.getXPosition();
			int y0 = hotspot.getYPosition();
			float relativeX;
			float relativeY;
			if (scale == 1 && curValues[2] == iniValues[2]
					&& curValues[5] == iniValues[5]) {
				relativeX = x0 + 30;
				relativeY = y0 + 30;
			} else {

				float x1 = x0 + 30;
				float y1 = y0 + 30;

				relativeX = (curValues[2] + (x1 - iniValues[2]) * scale);
				relativeY = (curValues[5] + (y1 - iniValues[5]) * scale);
			}

			ImageView imgViewhotspot = new ImageView(this);
			imgViewhotspot.setImageResource(R.drawable.pin);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					60, 60);
			layoutParams.leftMargin = Math.round(relativeX - 30);
			layoutParams.topMargin = Math.round(relativeY - 30);
			imgViewhotspot.setLayoutParams(layoutParams);
			imgViewhotspot.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					myView = v;
					removeDialog(100);
					showDialog(100);
				}
			});
			if (isHotspotVisible) {
				imgViewhotspot.setVisibility(View.VISIBLE);
			} else {
				imgViewhotspot.setVisibility(View.INVISIBLE);
			}
			hotspots.add(imgViewhotspot);
			hotspotIcon.add(imgViewhotspot);
			rootView.addView(imgViewhotspot);
		}

	}

	//update hotspot position in device orientation
	
	private void updateHotspotPosition() {

		Matrix tempMat = selectedImg.getImageMatrix();

		// GET THE INITIAL MARIX VALUES
		float[] iniValues = new float[9];
		matrixInitial.getValues(iniValues);

		// GET THE CURRENT MATRIX VALUES
		float[] curValues = new float[9];
		tempMat.getValues(curValues);

		uxHotspot(curValues, iniValues);

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

	//action for device orientation
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		for (ImageView imgView : hotspotIcon) {
			imgView.setVisibility(View.INVISIBLE);
		}

		selectedImg.setScaleType(ScaleType.FIT_CENTER);

		_newOrientation = newConfig.orientation;

		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 500);
	}

	private void TimerMethod() {
		this.runOnUiThread(Timer_Tick);
	}

	private void handleResetingHotspot() {

		for (ImageView hotspotImageView : hotspotIcon) {
			if (hotspotImageView == selectedImg) {
				continue;
			}
			rootView.removeView(hotspotImageView);
		}

		Matrix temp = selectedImg.getImageMatrix();

		if (_newOrientation == Configuration.ORIENTATION_PORTRAIT) {

			float[] curValues = new float[9];
			float[] iniValues = new float[9];
			matrixInitiaPort.set(temp);
			matrixInitiaPort.getValues(curValues);
			matrixInitial.set(new Matrix());
			matrixInitial.getValues(iniValues);
			uxHotspot(curValues, iniValues);

		} else {
			matrixInitialLand.set(temp);
			matrixInitial.set(new Matrix());
			updateHotspotPosition();
		}
	}

	private Runnable Timer_Tick = new Runnable() {
		public void run() {
			handleResetingHotspot();
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		updateHotspotPosition();
		_orientation = getResources().getConfiguration().orientation;
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

			if (state == SessionState.OPENED && uploadFile == true) {
				uploadFile();
				uploadFile = false;
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}
	
	//To share image in Facebook

	private void shareToFacebook(String lnk) {

		String hotspotinfo = "";
		int i = 1;
		for (Hotspot h : allHotspot) {
			hotspotinfo += Integer.toString(i) + " : " + h.getInputUrl() + ";";

			i++;

		}

		if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			// Publish the post using the Share Dialog
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
					this)
					.setLink(
							"http://www.gleecus.com/app/downloadFromN.php/"
									+ lnk).setDescription(hotspotinfo)
					.setPicture("http://www.gleecus.com/app/" + lnk + ".jpg")
					.setName("LivePic").setCaption("Images with hotspots")
					.build();
			uiHelper.trackPendingDialogCall(shareDialog.present());

		} else {

			publishFeedDialog(lnk);
		}

	}
	
	//To publish feed

	private void publishFeedDialog(String link) {

		String hotspotinfo = "";
		int i = 1;
		for (Hotspot h : allHotspot) {
			hotspotinfo += Integer.toString(i) + " : " + h.getInputUrl() + ";";

			i++;

		}

		Bundle params = new Bundle();
		params.putString("name", "LivePic");
		params.putString("caption", "Images with hotspots.");
		params.putString("description", hotspotinfo);
		params.putString("link",
				"http://www.gleecus.com/app/downloadFromN.php/" + link);
		params.putString("picture", "http://www.gleecus.com/app/" + link
				+ ".jpg");

		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(this,
				Session.getActiveSession(), params)).setOnCompleteListener(
				new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Toast.makeText(getApplicationContext(),
										"Posted story, id: " + postId,
										Toast.LENGTH_SHORT).show();
							} else {
								// User clicked the Cancel button
								Toast.makeText(getApplicationContext(),
										"Publish cancelled", Toast.LENGTH_SHORT)
										.show();
							}
						} else if (error instanceof FacebookOperationCanceledException) {
							// User clicked the "x" button
							Toast.makeText(getApplicationContext(),
									"Publish cancelled", Toast.LENGTH_SHORT)
									.show();
						} else {
							// Generic, ex: network error
							Toast.makeText(getApplicationContext(),
									"Error posting story", Toast.LENGTH_SHORT)
									.show();
						}
					}

				}).build();
		feedDialog.show();
	}

	ProgressDialog progress = null;
	String str = null;

	
	//To upload the file to the server
	
	@SuppressLint("NewApi")
	private void uploadFile() {

		progress = ProgressDialog.show(this, "Loading...",
				"Converting the image for uploading to Facebook.", true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					ThreadPolicy tp = ThreadPolicy.LAX;
					StrictMode.setThreadPolicy(tp);

					String fileName = sourceFile;

					DataOutputStream dos = null;
					String lineEnd = "\r\n";
					String twoHyphens = "--";
					String boundary = "*****";
					int bytesRead, bytesAvailable, bufferSize;
					byte[] buffer;
					int maxBufferSize = 1 * 1024 * 1024;
					File sourceFile = new File(fileName);

					FileInputStream fileInputStream = new FileInputStream(
							sourceFile);
					URL url = new URL(upLoadServerUri);

					// Open a HTTP connection to the URL
					conn = (HttpURLConnection) url.openConnection();
					conn.setDoInput(true); // Allow Inputs
					conn.setDoOutput(true); // Allow Outputs
					conn.setUseCaches(false); // Don't use a Cached Copy
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("ENCTYPE", "multipart/form-data");
					conn.setRequestProperty("Content-Type",
							"multipart/form-data;boundary=" + boundary);
					conn.setRequestProperty("uploaded_file", fileName);

					dos = new DataOutputStream(conn.getOutputStream());

					dos.writeBytes(twoHyphens + boundary + lineEnd);

					String contentType = String.format(
							"Content-Disposition: form-data; name=%s;filename=%s"
									+ lineEnd, "datafile", fileName);
					dos.writeBytes(contentType);

					dos.writeBytes(lineEnd);

					// create a buffer of maximum size
					bytesAvailable = fileInputStream.available();

					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					buffer = new byte[bufferSize];

					// read file and write it into form...
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					while (bytesRead > 0) {

						dos.write(buffer, 0, bufferSize);
						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					}

					dos.writeBytes(lineEnd);
					dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					StringBuilder stringBuilder = new StringBuilder();

					String line = null;
					while ((line = reader.readLine()) != null) {
						stringBuilder.append(line);
					}

					String s = stringBuilder.toString();
					str = s;

				} catch (Exception ex) {
					ex.printStackTrace();
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progress.dismiss();
						// publishFeedDialog(str);
						shareToFacebook(str);
					}
				});
			}
		}).start();

	}
	
	
	//managing bitmap quality at the time of uploading

	private void manageBitmap() {

		try {

			Bitmap bitmap = ((BitmapDrawable) selectedImg.getDrawable())
					.getBitmap();
			String tempFilePath = Environment.getExternalStorageDirectory()
					+ File.separator + "test.jpg";

			File file = new File(tempFilePath);
			FileOutputStream fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inMutable = true;
			Bitmap mbitmap = BitmapFactory.decodeFile(tempFilePath, options);
			String text = "X";
			Paint paint = new Paint();
			paint.setStyle(Style.FILL);
			paint.setColor(Color.RED);
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(convertToPixels(getApplicationContext(), 10));
			Canvas canvas = new Canvas(mbitmap);

			Paint paintText = new Paint();
			paintText.setStyle(Style.FILL);
			paintText.setColor(Color.BLACK);
			paintText.setTextAlign(Align.CENTER);
			paintText.setTextSize(convertToPixels(getApplicationContext(), 20));

			File fileModified = new File(sourceFile);
			fOut = new FileOutputStream(fileModified);
			int i = 0;
			for (Hotspot hotspot : allHotspot) {
				i++;
				canvas.drawCircle(hotspot.getXPosition() + 30,
						hotspot.getYPosition() + 30, 30, paint);
				canvas.drawText(Integer.toString(i),
						hotspot.getXPosition() + 30,
						hotspot.getYPosition() + 45, paintText);
			}

			mbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();

			file.delete();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static int convertToPixels(Context context, int nDP) {
		final float conversionScale = context.getResources()
				.getDisplayMetrics().density;

		return (int) ((nDP * conversionScale) + 0.5f);

	}
}