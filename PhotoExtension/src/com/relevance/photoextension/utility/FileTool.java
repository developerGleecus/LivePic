package com.relevance.photoextension.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.relevance.photoextension.utility.Constants;

import android.os.Environment;
import android.util.Log;

//Configuring The File path to access files from Specified directory
public class FileTool {
	/** 5K */
	private final static int PIC_FILE_SIZE_MIN = 5;
	/** 300K */
	private final static int PIC_FILE_SIZE_MAX = 6000;

	/**
	 * ,
	 * 
	 * @param path
	 * @return
	 */
	public void ListFiles(String path, List<String> lstPaths) {
		File externalFile = new File(
				android.os.Environment.getExternalStorageDirectory()
						+ File.separator + Constants.PHOTO_ALBUM);
		System.out.println("File = " + externalFile.getAbsolutePath()
				+ " Exists = " + externalFile.exists());

		File file = new File(path);
		System.out.println("INFO: File = " + file.getAbsolutePath()
				+ " Exists = " + file.exists());

		if (file == null)
			return;

		File[] fs = file.listFiles();
		// System.out.println("INFO: File List Size = "+fs.length);
		if (fs == null)
			return;
		for (File f : fs) {
			System.out.println("Checking File = " + f.getAbsolutePath());
			if (f == null)
				continue;

			String fName = f.getName();
			String htx = fName.substring(fName.lastIndexOf(".") + 1,
					fName.length()).toLowerCase(); //

			if (htx.equals("png") || htx.equals("jpg") || htx.equals("gif")
					|| htx.equals("bmp")) {

				if (fileSizeValidity(f.getPath())) {
					lstPaths.add(f.getPath());
					System.out.println("Adding File = " + f.getAbsolutePath());
				}
			} else {

			}

			path = f.getAbsolutePath();
			if (f.isDirectory() == true) {
				ListFiles(path, lstPaths);
			}
		}
	}

	/**
	 * 
	 * 5K-300K?FALSE
	 * 
	 * @param path
	 * @return
	 */
	private boolean fileSizeValidity(String path) {
		File f = new File(path);
		if (f.exists()) {
			int cur = 0;
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);

				cur = fis.available() / 1000;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// 5K-300K?
			if (cur >= PIC_FILE_SIZE_MIN && cur <= PIC_FILE_SIZE_MAX)
				return true;

		}

		return false;
	}
}
