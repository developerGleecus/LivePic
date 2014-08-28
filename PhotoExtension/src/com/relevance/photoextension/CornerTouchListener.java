package com.relevance.photoextension;

import java.util.ArrayList;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class CornerTouchListener implements OnTouchListener {
	private CustomImageView civ;

	private Point p1;
	private Point p2;
	private Point p3;
	private Point p4;

	private CornerImageView i1;
	private CornerImageView i2;
	private CornerImageView i3;
	private CornerImageView i4;

	private int lowestX;
	private int lowestY;
	private int highestX;
	private int highestY;

	private int currentX;
	private int currentY;

	private ArrayList<CornerImageView> cornerArrayList;

	//creating four corner handlers
	
	public CornerTouchListener(CustomImageView v, Point p1, Point p2, Point p3,
			Point p4, CornerImageView i1, CornerImageView i2,
			CornerImageView i3, CornerImageView i4) {
		this.civ = v;
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;

		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
		this.i4 = i4;

		cornerArrayList = new ArrayList<CornerImageView>();
		cornerArrayList.add(i1);
		cornerArrayList.add(i2);
		cornerArrayList.add(i3);
		cornerArrayList.add(i4);

		lowestX = p1.x;
		lowestY = p1.y;
		highestX = p4.x;
		highestY = p4.y;
	}

	//movement listener of four corners
	
	@Override
	public boolean onTouch(View view, MotionEvent arg1) {
		CornerImageView v = (CornerImageView) view;
		if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
			System.out.println("INFO: Pulling Image = " + v.getImageId());
			currentX = (int) arg1.getRawX();
			currentY = (int) arg1.getRawY();

			lowestX = 2000;
			lowestY = 2000;

			highestX = 0;
			highestY = 0;
			System.out.println("INFO: ACTION DOWN X = " + currentX + " Y = "
					+ currentY);
			return true;
		}

		if (arg1.getAction() == MotionEvent.ACTION_UP) {
			int newX = (int) arg1.getRawX();
			int newY = (int) arg1.getRawY();
			System.out.println("INFO: ACTION UP X = " + newX + " Y = " + newY);

			int diffX = newX - currentX;
			int diffY = newY - currentY;

			System.out.println("INFO: DIFF X = " + diffX + " Y = " + diffY
					+ " CORNER SIZE = " + cornerArrayList.size());

			for (int i = 0; i < cornerArrayList.size(); i++) {
				CornerImageView tmp = cornerArrayList.get(i);

				System.out.println("Pulled ID  = " + v.getImageId()
						+ " CHECKING = " + tmp.getImageId());
				if (v.getImageId() != tmp.getImageId()) {
					System.out.println("PULLED IMG LEFT = " + v.getLeftMargin()
							+ " TOP  = " + v.getTopMargin());
					System.out.println("CHECKING IMG LEFT = "
							+ tmp.getLeftMargin() + " TOP  = "
							+ tmp.getTopMargin());
					if (v.getLeftMargin() == tmp.getLeftMargin()) {
						System.out.println("INFO: DIFFX APPLIED TO = "
								+ tmp.getImageId());
						// Update the Left Margin by DiffX
						final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
								60, 60);
						lp.setMargins(tmp.getLeftMargin() + diffX,
								tmp.getTopMargin(), 0, 0);
						tmp.setLayoutParams(lp);
						tmp.invalidate();
					} else if (v.getTopMargin() == tmp.getTopMargin()) {
						System.out.println("INFO: DIFFY APPLIED TO = "
								+ tmp.getImageId());
						// Update the Right Margin by DiffY
						final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
								60, 60);
						lp.setMargins(tmp.getLeftMargin(), tmp.getTopMargin()
								+ diffY, 0, 0);
						tmp.setLayoutParams(lp);
						tmp.invalidate();
					}
				}
			}
			{
				System.out.println("********* UPDATING MYSELF");
				// Update Self with both DiffX and DiffY
				final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						60, 60);
				lp.setMargins(v.getLeftMargin() + diffX, v.getTopMargin()
						+ diffY, 0, 0);
				v.setLayoutParams(lp);
				v.invalidate();
			}
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
			{
				System.out.println("INFO: HIGHEST X = " + highestX + " Y = "
						+ highestY + "LOWEST X = " + lowestX + " Y = "
						+ lowestY);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						(highestX - lowestX), (highestY - lowestY));
				lp.setMargins(lowestX + 30, lowestY + 30, 0, 0);
				civ.setLayoutParams(lp);
				civ.invalidate();
			}
			return true;
		}
		return true;
	}

	//to get left margin
	public int getLeftMargin() {
		return lowestX;
	}

	//to get top margin
	
	public int getTopMargin() {
		return lowestY;
	}

	//to get width
	
	public int getWidth() {
		return (highestX - lowestX);
	}
	
	//to get height

	public int getHeight() {
		return (highestY - lowestY);
	}
}