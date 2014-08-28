package com.relevance.photoextension;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class CustomImageTouchListener implements OnTouchListener {
	private int leftMargin;
	private int topMargin;

	private int currentX;
	private int currentY;

	private int width;
	private int height;

	private CornerImageView i1;
	private CornerImageView i2;
	private CornerImageView i3;
	private CornerImageView i4;
	
	//touch listener to move and stretch the inner rectangle

	public CustomImageTouchListener(int leftMargin, int topMargin, int width,
			int height) {
		this.leftMargin = leftMargin;
		this.topMargin = topMargin;

		this.width = width;
		this.height = height;
	}

	public void setCornerPoint(CornerImageView i1, CornerImageView i2,
			CornerImageView i3, CornerImageView i4) {
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
		this.i4 = i4;
	}

	@Override
	public boolean onTouch(View view, MotionEvent arg1) {

		if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
			currentX = (int) arg1.getRawX();
			currentY = (int) arg1.getRawY();
			return true;
		}

		if ((arg1.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
			int newX = (int) arg1.getRawX();
			int newY = (int) arg1.getRawY();

			leftMargin = leftMargin + (newX - currentX);
			topMargin = topMargin + (newY - currentY);

			currentX = newX;
			currentY = newY;

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			lp.setMargins(leftMargin, topMargin, 0, 0);
			view.setLayoutParams(lp);

			lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
			width = lp.width;
			height = lp.height;

			// Now redraw the Corner Items
			final RelativeLayout.LayoutParams layoutParams_p1 = (RelativeLayout.LayoutParams) i1
					.getLayoutParams();
			layoutParams_p1.setMargins(leftMargin - 30, topMargin - 30, 0, 0);
			i1.setLayoutParams(layoutParams_p1);
			// i1.invalidate();

			final RelativeLayout.LayoutParams layoutParams_p2 = (RelativeLayout.LayoutParams) i2
					.getLayoutParams();
			layoutParams_p2.setMargins(leftMargin + width - 30, topMargin - 30,
					0, 0);
			i2.setLayoutParams(layoutParams_p2);
			// i2.invalidate();

			final RelativeLayout.LayoutParams layoutParams_p3 = (RelativeLayout.LayoutParams) i3
					.getLayoutParams();
			layoutParams_p3.setMargins(leftMargin - 30,
					topMargin + height - 30, 0, 0);
			i3.setLayoutParams(layoutParams_p3);
			// i3.invalidate();

			final RelativeLayout.LayoutParams layoutParams_p4 = (RelativeLayout.LayoutParams) i4
					.getLayoutParams();
			layoutParams_p4.setMargins(leftMargin + width - 30, topMargin
					+ height - 30, 0, 0);
			i4.setLayoutParams(layoutParams_p4);
			// i4.invalidate();

			return true;
		}

		if (arg1.getAction() == MotionEvent.ACTION_UP) {
			int newX = (int) arg1.getRawX();
			int newY = (int) arg1.getRawY();

			leftMargin = leftMargin + (newX - currentX);
			topMargin = topMargin + (newY - currentY);

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			lp.setMargins(leftMargin, topMargin, 0, 0);
			view.setLayoutParams(lp);

			// view.invalidate();

			lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
			width = lp.width;
			height = lp.height;

			// Now redraw the Corner Items
			final RelativeLayout.LayoutParams layoutParams_p1 = (RelativeLayout.LayoutParams) i1
					.getLayoutParams();
			layoutParams_p1.setMargins(leftMargin - 30, topMargin - 30, 0, 0);
			i1.setLayoutParams(layoutParams_p1);
			// i1.invalidate();

			final RelativeLayout.LayoutParams layoutParams_p2 = (RelativeLayout.LayoutParams) i2
					.getLayoutParams();
			layoutParams_p2.setMargins(leftMargin + width - 30, topMargin - 30,
					0, 0);
			i2.setLayoutParams(layoutParams_p2);
			// i2.invalidate();

			final RelativeLayout.LayoutParams layoutParams_p3 = (RelativeLayout.LayoutParams) i3
					.getLayoutParams();
			layoutParams_p3.setMargins(leftMargin - 30,
					topMargin + height - 30, 0, 0);
			i3.setLayoutParams(layoutParams_p3);
			// i3.invalidate();

			final RelativeLayout.LayoutParams layoutParams_p4 = (RelativeLayout.LayoutParams) i4
					.getLayoutParams();
			layoutParams_p4.setMargins(leftMargin + width - 30, topMargin
					+ height - 30, 0, 0);
			i4.setLayoutParams(layoutParams_p4);
			// i4.invalidate();
			return true;
		}

		return true;
	}

	//to get left margin
	
	public int getLeftMargin() {
		return leftMargin;
	}
	
	//to get top margin

	public int getTopMargin() {
		return topMargin;
	}
	
	//to get width

	public int getWidth() {
		return width;
	}
	
	//to get height

	public int getHeight() {
		return height;
	}
}