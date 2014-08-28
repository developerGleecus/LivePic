package com.relevance.photoextension;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CustomImageView extends ImageView {
	int currentX = 0;
	int currentY = 0;

	private int leftMargin;
	private int topMargin;

	private Paint borderPaint = null;
	private Paint backgroundPaint = null;

	private float mPosX = 0f;
	private float mPosY = 0f;

	private float mLastTouchX;
	private float mLastTouchY;
	private static final int INVALID_POINTER_ID = -1;
	private static final String LOG_TAG = "TouchImageView";

	// The â€˜active pointerâ€™ is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;

	public CustomImageView(Context context, int leftMargin, int topMargin) {
		this(context, null, 0);
		this.leftMargin = leftMargin;
		this.topMargin = topMargin;
	}

	public CustomImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	private float mScaleFactor = 1.f;

	// Existing code ...
	public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		borderPaint = new Paint();
		borderPaint.setARGB(255, 255, 128, 0);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(4);

		backgroundPaint = new Paint();
		backgroundPaint.setARGB(32, 255, 255, 255);
		backgroundPaint.setStyle(Paint.Style.FILL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.drawRect(0, 0, getWidth() - 1, getHeight() - 1, borderPaint);
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, getWidth() - 1, getHeight() - 1, backgroundPaint);
		if (this.getDrawable() != null) {
			canvas.save();
			canvas.translate(mPosX, mPosY);

			Matrix matrix = new Matrix();
			matrix.postScale(mScaleFactor, mScaleFactor, pivotPointX,
					pivotPointY);

			canvas.drawBitmap(
					((BitmapDrawable) this.getDrawable()).getBitmap(), matrix,
					null);
			canvas.restore();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable
	 * )
	 */
	@Override
	public void setImageDrawable(Drawable drawable) {
		// Constrain to given size but keep aspect ratio
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		mLastTouchX = mPosX = 0;
		mLastTouchY = mPosY = 0;

		int borderWidth = (int) borderPaint.getStrokeWidth();
		mScaleFactor = Math.min(((float) getLayoutParams().width - borderWidth)
				/ width, ((float) getLayoutParams().height - borderWidth)
				/ height);
		pivotPointX = (((float) getLayoutParams().width - borderWidth) - (int) (width * mScaleFactor)) / 2;
		pivotPointY = (((float) getLayoutParams().height - borderWidth) - (int) (height * mScaleFactor)) / 2;
		super.setImageDrawable(drawable);
	}

	float pivotPointX = 0f;
	float pivotPointY = 0f;

}