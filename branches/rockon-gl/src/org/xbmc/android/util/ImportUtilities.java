/*
 * Copyright (C) 2008 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xbmc.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.xbmc.api.object.ICoverArt;
import org.xbmc.api.type.MediaType;
import org.xbmc.api.type.ThumbSize;
import org.xbmc.api.type.ThumbSize.Dimension;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public abstract class ImportUtilities {
	
	private static final String TAG = "ImportUtilities";
    private static final String CACHE_DIRECTORY = "xbmc";
    private static final double MIN_FREE_SPACE = 3;
    private static final byte[] BUFFER_SMALL = new byte[4 * ThumbSize.getPixel(ThumbSize.SMALL) * ThumbSize.getPixel(ThumbSize.SMALL)];
    private static final byte[] BUFFER_MEDIUM = new byte[4 * ThumbSize.getPixel(ThumbSize.MEDIUM) * ThumbSize.getPixel(ThumbSize.MEDIUM)];

    public static File getCacheDirectory(String type, int size) {
    	StringBuilder sb = new StringBuilder(CACHE_DIRECTORY);
    	sb.append(type);
    	sb.append(ThumbSize.getDir(size));
        return IOUtilities.getExternalFile(sb.toString());
    }
    
    private static String getCacheFileName(String type, int size, String name) {
    	StringBuilder sb = new StringBuilder(32);
    	sb.append(CACHE_DIRECTORY);
    	sb.append(type);
    	sb.append(ThumbSize.getDir(size));
    	sb.append("/");
    	sb.append(name);
    	return sb.toString();
    }
    public static File getCacheFile(String type, int size, String name) {
    	return IOUtilities.getExternalFile(getCacheFileName(type, size, name));
    }

    public static FileInputStream getCacheFileInputStream(String type, int size, String name) {
    	return IOUtilities.getExternalFileInputStream(getCacheFileName(type, size, name));
    }
    
    public static boolean loadBitmap(ICoverArt cover, Bitmap bitmap, int thumbSize) {
    		
    	final File file  = ImportUtilities.getCacheFile(MediaType.getArtFolder(cover.getMediaType()), thumbSize, Crc32.formatAsHexLowerCase(cover.getCrc()));
    	if (file.exists()) {
    		bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
    		return true;
    	} else {
    		return false;
    	}
    	
//    	final FileInputStream albumCoverFileInputStream  = ImportUtilities.getCacheFileInputStream(MediaType.getArtFolder(cover.getMediaType()), thumbSize, Crc32.formatAsHexLowerCase(cover.getCrc()));
//    	if (albumCoverFileInputStream == null) {
//    		return false;
//    	}
//    	try {
//    		final byte[] buffer = thumbSize == ThumbSize.SMALL ? BUFFER_SMALL : BUFFER_MEDIUM;
//			albumCoverFileInputStream.read(buffer, 0, buffer.length);
//			bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buffer)); 
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
    }
    
    public static Bitmap addCoverToCache(ICoverArt cover, Bitmap bitmap, int thumbSize) {
    	Bitmap sizeToReturn = null;
    	Bitmap resized = null;
    	File cacheDirectory;
    	final int mediaType = cover.getMediaType();
    	for (int currentThumbSize : ThumbSize.values()) {
    		// don't save big covers
    		if (currentThumbSize == ThumbSize.BIG) {
    			if (thumbSize == currentThumbSize)
    				return bitmap;
    			else
    				continue;
    		}
    		try {
    			cacheDirectory = ensureCache(MediaType.getArtFolder(mediaType), currentThumbSize);
    		} catch (IOException e) {
    			return null;
    		}
    		File coverFile = new File(cacheDirectory, Crc32.formatAsHexLowerCase(cover.getCrc()));
    		FileOutputStream out = null;
    		try {
    			final Bitmap resizing = resized == null ? bitmap : resized;
    			Dimension uncroppedDim = ThumbSize.getDimension(currentThumbSize, mediaType, resizing.getWidth(), resizing.getHeight());
    			Dimension targetDim = ThumbSize.getTargetDimension(currentThumbSize, mediaType, resizing.getWidth(), resizing.getHeight());

    			Log.i(TAG, "Resizing to: " + uncroppedDim + " in order to fit into " + targetDim);
				resized = Bitmap.createScaledBitmap(resizing, uncroppedDim.x, uncroppedDim.y, true);

				// crop
				final Bitmap cropped;
				if (!uncroppedDim.equals(targetDim)) {
					if (uncroppedDim.x != targetDim.x) {
						final int dx = (uncroppedDim.x - targetDim.x) / 2;
						cropped = Bitmap.createBitmap(resized, dx, 0, targetDim.x, targetDim.y);
						Log.i(TAG, "Horizontally cropping with dx = " + dx + ".");
					} else {
						final int dy = (uncroppedDim.y - targetDim.y) / 2;
						Log.i(TAG, "Vertically cropping with dy = " + dy + ".");
						cropped = Bitmap.createBitmap(resized, 0, dy, targetDim.x, targetDim.y);
					}
				} else {
					Log.i(TAG, "Dimension matches, skipping cropping.");
					cropped = resized;
				}
//				final FileOutputStream fileOutStream = new FileOutputStream(coverFile);

//				ByteBuffer bitmapBuffer = ByteBuffer.allocate(cropped.getRowBytes() * cropped.getHeight());
//				cropped.copyPixelsToBuffer(bitmapBuffer);
//				fileOutStream.write(bitmapBuffer.array());

				cropped.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(coverFile));

    			if (thumbSize == currentThumbSize) {
    				sizeToReturn = cropped;
    			}
    			
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    			return null;
    		} catch (IOException e) {
    			e.printStackTrace();
    			return null;
    		} catch (OutOfMemoryError  e) {
    			e.printStackTrace();
    			return null;
    		} finally {
    			IOUtilities.closeStream(out);
    		}
    	}
        return sizeToReturn;
    }
    
	public static int calculateSampleSize(BitmapFactory.Options options, Dimension targetDimension) {
		if (targetDimension.x == 0 || targetDimension.y == 0) {
			return 1;
		}
		Boolean scaleByHeight = Math.abs(options.outHeight - targetDimension.y) >= Math.abs(options.outWidth - targetDimension.x);
		if (options.outHeight * options.outWidth * 2 >= 200 * 200 * 2) {
			// Load, scaling to smallest power of 2 that'll get it <= desired dimensions
			double sampleSize = scaleByHeight ? options.outHeight / targetDimension.y : options.outWidth / targetDimension.x;
			return (int) Math.pow(2d, Math.floor(Math.log(sampleSize) / Math.log(2d)));
		} else {
			return 1;
		}
	}
    
    /**
     * Returns number of free bytes on the SD card.
     * @return Number of free bytes on the SD card.
     */
    public static long freeSpace() {
    	StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
    
    /**
     * Returns total size of SD card in bytes.
     * @return Total size of SD card in bytes.
     */
    public static long totalSpace() {
    	StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
    	long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
    	return totalBlocks * blockSize;
    }
    
    public static String assertSdCard() {
    	if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
    		return "Your SD card is not mounted. You'll need it for caching thumbs.";
    	}
    	if (freePercentage() < MIN_FREE_SPACE) {
    		return "You need to have more than " + MIN_FREE_SPACE + "% of free space on your SD card.";
    	}
    	return null;
    }
    
    
    /**
     * Returns free space in percent.
     * @return Free space in percent.
     */
    public static double freePercentage() {
    	StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long availableBlocks = stat.getAvailableBlocks();
        long totalBlocks = stat.getBlockCount();
        return (double)availableBlocks / (double)totalBlocks * 100;
    }

    private static File ensureCache(String type, int size) throws IOException {
        File cacheDirectory = getCacheDirectory(type, size);
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs();
            new File(cacheDirectory, ".nomedia").createNewFile();
        }
        return cacheDirectory;
    }
    
    public static void purgeCache() {
    	final int size[] = ThumbSize.values();
    	final int[] mediaTypes = MediaType.getTypes();
    	for (int i = 0; i < mediaTypes.length; i++) {
    		String folder = MediaType.getArtFolder(mediaTypes[i]);
    		for (int j = 0; j < size.length; j++) {
    			File cacheDirectory = getCacheDirectory(folder, size[j]);
    			if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
    				for (File file : cacheDirectory.listFiles()) {
    					file.delete();
    				}
    			}
    		}
    	}
    }
}