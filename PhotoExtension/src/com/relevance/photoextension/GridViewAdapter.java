package com.relevance.photoextension;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.relevance.photoextension.hotspot.HotspotDetails;
import com.relevance.photoextension.utility.AsyncImageLoader3;
import com.relevance.photoextension.utility.AsyncImageLoader3.ImageCallback;

public class GridViewAdapter extends BaseAdapter {

	// Adapter variables
	private LayoutInflater layoutInflater;
	private List<String> imgUrls;
	private AsyncImageLoader3 imageLoader = new AsyncImageLoader3();
	Context context;

	// Constructor
	public GridViewAdapter(Context context, List<String> imgUrls) {
		layoutInflater = LayoutInflater.from(context);
		this.imgUrls = imgUrls;
		this.context = context;
	}

	@Override
	public int getCount() {
		System.out.println("INFO: GetCount Returning = " + imgUrls.size());
		return imgUrls.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// Container View Of images Configuration

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		final String path = imgUrls.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.photolist_item, null);
			viewHolder.ivImage = (ImageView) convertView
					.findViewById(R.id.iv_photoicon_photolist_item);
			viewHolder.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Bitmap cachedImage = imageLoader.loadBitmap(path, viewHolder.ivImage,
				new ImageCallback() {
					public void imageLoaded(Bitmap imageBitmap,
							ImageView image, String imageUrl) {
						viewHolder.ivImage.setImageBitmap(imageBitmap);
					}
				});

		if (cachedImage != null) {
			viewHolder.ivImage.setImageBitmap(cachedImage);
		} else if (cachedImage == null) {
			viewHolder.ivImage.setImageResource(R.drawable.livepic_launcher);
		}

		convertView.setOnClickListener(new OnClickListener() {

			// Setting Image Onclick event
			@Override
			public void onClick(View arg0) {
				System.out
						.println("************************* INSIDE ON CLICK LISTENER");
				Intent intent = new Intent(context, HotspotDetails.class);
				intent.putExtra("imageName", path);
				context.startActivity(intent);
			}

		});
		return convertView;
	}

	class ViewHolder {
		/**
* 
*/
		public ImageView ivImage;
		/**
* 
*/
		// public TextView tvInfo;

	}
}
