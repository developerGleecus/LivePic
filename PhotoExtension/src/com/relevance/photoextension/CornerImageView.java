package com.relevance.photoextension;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressLint("ViewConstructor")
public class CornerImageView extends ImageView {
	private int imageId;

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public CornerImageView(Context context, int id) {
		super(context);
		this.imageId = id;
		this.setMinimumHeight(60);
		this.setMinimumWidth(60);
		this.setBackgroundResource(R.drawable.ball);
	}

	public int getLeftMargin() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this
				.getLayoutParams();
		return params.leftMargin;
	}

	public int getTopMargin() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this
				.getLayoutParams();
		return params.topMargin;
	}
}