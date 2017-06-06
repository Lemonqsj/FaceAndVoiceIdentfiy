package com.example.lemon.faceandvoiceidentfiy.util;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

/**
 * 摄像头工具类，用于保存图片，设置摄像头参数
 * 
 * @author iFlytek &nbsp;&nbsp;&nbsp;<a href="http://www.xfyun.cn/">讯飞语音云</a>
 */
public class CameraHelper {
	private final static String TAG = CameraHelper.class.getSimpleName();

	private static CameraHelper instance;
	// 图片压缩质量
	public static int JPEGQuality = 80;
	// 原始照片数据
	private byte[] mOriginalPicData;
	// 旋转纠正过后的图片
	private Bitmap mCorrectedBitmap;
	
	// contentView的尺寸，即全屏除去状态栏部分
	private static Point contentSize;
	// 合适的SurfaceView尺寸
	private static Point fitSurfaceSize;
	
	public static CameraHelper createHelper(Context context) {
		if (null == instance) {
			instance = new CameraHelper(context);
		}
		return instance;
	}
	
	private int getStatusBarHeight(Context context) {
		Class<?> c =  null; 
		Object obj =  null; 
		Field field =  null; 

		int  x = 0, sbar =  0; 
		try  { 
		    c = Class.forName("com.android.internal.R$dimen");
		    obj = c.newInstance();
		    field = c.getField("status_bar_height");
		    x = Integer.parseInt(field.get(obj).toString());
		    sbar = context.getResources().getDimensionPixelSize(x);
		} catch(Exception e1) {
		    e1.printStackTrace();
		}
		
		return sbar;
	}
	
	private void computeContentSize(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);
		
		int status_height = getStatusBarHeight(context);
		int height = metrics.heightPixels;
		int width = metrics.widthPixels;
		
		if (width < height) {
			int tmp = width;
			width = height;
			height = tmp;
		}
		
		contentSize = new Point(width - status_height, height);
	}
	
	private CameraHelper (Context context) {
		computeContentSize(context);
	}

	/**
	 * 检测设备是否具有某个摄像头
	 * 
	 * @param cameraId 摄像头Id
	 * @return 检测结果
	 */
	public static boolean hasCamera(int cameraId) {
		int cameracount = 0;
		CameraInfo camerainfo = new CameraInfo();
		// 手机物理摄像头数
		cameracount = Camera.getNumberOfCameras();
		if (cameracount < 1) {
			return false;
		}
		for (int count = 0; count < cameracount; count++) {
			Camera.getCameraInfo(count, camerainfo);
			if (camerainfo.facing == cameraId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 实现相机预览支持分辨率的升序排列
	 */
	public class CameraSizeComparator implements Comparator<Size> {

		@Override
		public int compare(Size lhs, Size rhs) {
			if (lhs.width == rhs.width) {
				return 0;
			} else if (lhs.width > rhs.width) {
				return 1;
			} else {
				return -1;
			}
		}
	}
	
	/**
	 * 从sizeList中找到最大的size
	 * 
	 * @return 最大的size
	 * */
	private Size getMaxSize(List<Size> sizeList) {
		CameraSizeComparator sizeComparator = new CameraSizeComparator();
		Collections.sort(sizeList, sizeComparator);// 拍照图片大小排序
		
		return sizeList.get(sizeList.size() - 1);
	}
	
	/**
	 * 从sizeList中找到与屏幕长宽比最接近且最大的size
	 * @return 没找到则返回null
	 * */
	private Size getMaxFitSize(List<Size> sizeList, int screenWidth, int screenHeight) {
		CameraSizeComparator sizeComparator = new CameraSizeComparator();
		Collections.sort(sizeList, sizeComparator);// 拍照图片大小排序
		
		// 保证width > height
		if (screenWidth < screenHeight) {
			int tmp = screenWidth;
			screenWidth = screenHeight;
			screenHeight = tmp;
		}
		
		double ratio = (double)screenWidth/screenHeight;
		for (Size size: sizeList) {
			int width = size.width;
			int height = size.height;
			// 保证width > height
			if (width < height) {
				int tmp = width;
				width = height;
				height = tmp;
			}
			// 网上说比例之差在0.03之间最适合
			double ratio2 = (double)width/height;
			if (Math.abs(ratio - ratio2) < 0.13) {
				return size;
			}
		}
		
		return null;
	}
	
	/**
	 * 获取最适合屏幕分辨率的预览大小
	 * */
	public Size getFitPreviewSize(List<Size> previewlist,
			List<Size> picturelist, int screenWidth, int screenHeight) {
		CameraSizeComparator sizeComparator = new CameraSizeComparator();
		Collections.sort(previewlist, sizeComparator);// 预览大小排序从小到大
		Collections.sort(picturelist, sizeComparator);// 拍照图片大小排序

		List<Size> commonsize = new ArrayList<Size>();// 共同支持的分辨率
		for (int i = 0; i < picturelist.size(); i++) {
			for (int j = 0; j < previewlist.size(); j++) {
				if ((picturelist.get(i).width == previewlist.get(j).width)
						&& picturelist.get(i).height == previewlist.get(j).height) {
					commonsize.add(picturelist.get(i));
				}
			}
		}
		
		Size maxSize = getMaxFitSize(previewlist, screenWidth, screenHeight);
		if (null != maxSize) {
			return maxSize;
		}
		
		return commonsize.get(commonsize.size() - 1);
	}
	
	/**
	 * 获得合适预览界面
	 * @param list
	 * @param th
	 * @return
	 */
	public Size getPreviewSize(List<Size> previewlist,
			List<Size> picturelist) {
		CameraSizeComparator sizeComparator = new CameraSizeComparator();
		Collections.sort(previewlist, sizeComparator);// 预览大小排序从小到大
		Collections.sort(picturelist, sizeComparator);// 图片大小排序

		List<Size> commonsize = new ArrayList<Size>();// 共同支持的分辨率
		for (int i = 0; i < picturelist.size(); i++) {
			for (int j = 0; j < previewlist.size(); j++) {
				if ((picturelist.get(i).width == previewlist.get(j).width)
						&& picturelist.get(i).height == previewlist.get(j).height) {
					commonsize.add(picturelist.get(i));
				}
			}
		}
		return commonsize.get(commonsize.size() - 1);
	}
	
	/**
	 * 获取摄相头参数设置
	 * @param context 上下文
	 * @param camera 摄像头对象
	 * @param cameraId 报像头Id
	 * @return
	 */
	public Camera.Parameters getCameraParam(Context context, Camera camera, int cameraId) {
		Camera.Parameters params = camera.getParameters();
		// 设置图片格式
		params.setPictureFormat(PixelFormat.JPEG);
		// 设置照片质量
		params.setJpegQuality(80);
		
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		List<Size> pictureSizes = params.getSupportedPictureSizes();

		Size cameraSize = getPreviewSize(previewSizes, pictureSizes);
		if (cameraId == CameraInfo.CAMERA_FACING_BACK) {
			// 自动聚焦,后置镜头可以自动对焦但是前置不行，需要硬件的支持
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
		if (null == cameraSize) {
			return params;
		}
		// 设置保存的图片尺寸
		params.setPictureSize(cameraSize.width, cameraSize.height);
		// 设置预览大小
		params.setPreviewSize(cameraSize.width, cameraSize.height);

		return params;
	}
	
	/**
	 * 获取摄相头参数设置
	 * @param context 上下文
	 * @param camera 摄像头对象
	 * @param cameraId 报像头Id
	 * @param screenWidth 屏幕宽
	 * @param screenHeight 屏幕高
	 * @return
	 */
	public Camera.Parameters getCameraParam(Context context, Camera camera, int cameraId,
			int screenWidth, int screenHeight) {
		Camera.Parameters params = camera.getParameters();
		// 设置图片格式
		params.setPictureFormat(PixelFormat.JPEG);
		// 设置照片质量
		params.setJpegQuality(80);
		
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		List<Size> pictureSizes = params.getSupportedPictureSizes();

		Size cameraSize = getFitPreviewSize(previewSizes, pictureSizes, 
				screenWidth, screenHeight);
		Size maxPictureSize = getMaxSize(pictureSizes);
		
		if (cameraId == CameraInfo.CAMERA_FACING_BACK) {
			// 自动聚焦,后置镜头可以自动对焦但是前置不行，需要硬件的支持
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
		if (null == cameraSize) {
			return params;
		}
		
		params.setPreviewSize(cameraSize.width, cameraSize.height); 		// 设置预览大小
		
		if (null != maxPictureSize) {
			params.setPictureSize(maxPictureSize.width, maxPictureSize.height); // 设置图片尺寸
		} else {
			params.setPictureSize(cameraSize.width, cameraSize.height); // 设置图片尺寸
		}

		return params;
	}
	
    /**
     * 根据手机方向获得相机预览画面需要旋转纠正的角度  
     * @param activity Activity对象
     * @param cameraId 摄像头Id
     * @return
     */
	public static int getPreviewDegree(Activity activity, int cameraId) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该旋转的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 0;
			break;
		case Surface.ROTATION_90:
			degree = 90;
			break;
		case Surface.ROTATION_180:
			degree = 180;
			break;
		case Surface.ROTATION_270:
			degree = 270;
			break;
		}
		
		int result = 0;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
	        result = (info.orientation + degree) % 360;
	        result = (360 - result) % 360;  // compensate the mirror
	    } else {  // back-facing
	        result = (info.orientation - degree + 360) % 360;
	    }
		Log.d(TAG, result + "");
		return result;
	}
	
	/** 
     * 将字节数组的图形数据转换为Bitmap
     * @return Bitmap格式的图片
     */  
    private Bitmap byte2Bitmap() {
    	try {
            // 将图像像素变为原来的1/4
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            
            Bitmap bitmap = BitmapFactory.decodeByteArray(mOriginalPicData, 0, 
            					mOriginalPicData.length, opts);
            return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
	
	/**
     * 存入图片数据
     * @param byte 图片数据
     * @param cameraId 摄相头id
     * @param context 上下文
     * @return
     */
	public void setCacheData(byte[] data, int cameraId, Context context) {
		try {
			if(null != data) {
				mOriginalPicData = data;
			}
				
			Bitmap bitmap = byte2Bitmap();

			Matrix matrix = new Matrix();
			// 前置镜头需要向左旋转90°，后置镜头向右旋转90°
			if (CameraInfo.CAMERA_FACING_FRONT == cameraId) {
				matrix.setRotate(0 - getPreviewDegree((Activity)context, cameraId));
				// 前置摄相头加入水平翻转，防止得到镜像
				matrix.postScale(-1, 1);
			} else {
				matrix.setRotate(getPreviewDegree((Activity)context, cameraId));
			}
			
			recycleCacheBitmap();
			mCorrectedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			recycleBitmap(bitmap);
			bitmap = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Bitmap getImageBitmap() {
		return mCorrectedBitmap;
	}
	
	/**
     * 获取旋转纠正后的图片数据（等比放缩到800*600）
     * @return
     */
	public byte[] getImageData() {
		ByteArrayOutputStream baos  = null;
		try {
			Matrix matrix = new Matrix();
			// 获取原始图片的宽和高度
			int orgWidth = mCorrectedBitmap.getWidth();
			int orgHeight = mCorrectedBitmap.getHeight();
			float scaleWidth = ((float) orgWidth) / 800;
			float scaleHeigth = ((float) orgHeight) / 600;
			float scale = Math.max(scaleWidth, scaleHeigth);
			matrix.postScale(1 / scale, 1 / scale);
			
			// 得到放缩后的图片
			Bitmap scaledBitmap = Bitmap.createBitmap(mCorrectedBitmap, 0, 0, orgWidth, orgHeight, matrix, true);
			Log.d(TAG, "Image orgwidth:" + orgWidth + ",scaleWidth:" + scaleWidth
					+ ",orgHeight:" + orgHeight + ",scaleHeigth:" + scaleHeigth
					+ ",newWidth:" + scaledBitmap.getWidth() + ",newHeight:"
					+ scaledBitmap.getHeight());
			
			// 将调整后的图片转换成字节流
			baos = new ByteArrayOutputStream();
			byte[] bytes = null;
			
			scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEGQuality, baos);
			bytes = baos.toByteArray();
			
			recycleBitmap(scaledBitmap);
			scaledBitmap = null;
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
				baos = null;
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 释放bitmap资源
	 * @param bitmap
	 */
	private void recycleBitmap(Bitmap bitmap) {
		if(bitmap != null && !bitmap.isRecycled()){
			bitmap.recycle();
			bitmap = null;
		}
	}
	
	/**
	 * 释放缓存的bitmap
	 */
	private void recycleCacheBitmap() {
		recycleBitmap(mCorrectedBitmap);
		mCorrectedBitmap = null;
		System.gc();
	}
	
	public Point getFitSurfaceSize() {
		return fitSurfaceSize;
	}
	
	/**
	 * 计算得到fitSurfaceSize和contentSize的差距
	 * */
	private int getFitSizeDiff(Point fitSize) {
		int sum = Math.abs(fitSize.x - contentSize.x) + 
				Math.abs(fitSize.y - contentSize.y);
		
		return sum;
	}
	
	/**
	 * 调整得到的fitSurfaceSize，如果宽/高与contentSize差距不大，就将其调整为contentSize的宽/高
	 * */
	private void adjustFitSize(Point fitSize) {
		if ((float)getFitSizeDiff(fitSize)/contentSize.x < 0.05) {
			fitSize.x = contentSize.x;
		}
		
		if ((float)getFitSizeDiff(fitSize)/contentSize.y < 0.05) {
			fitSize.y = contentSize.y;
		}
	}
	
	public Point getFitSurfaceSize(Size previewSize) {
		if (null == fitSurfaceSize) {
			fitSurfaceSize = getFitSize(contentSize, previewSize);
		}
		adjustFitSize(fitSurfaceSize);
		
		return fitSurfaceSize;
	}

	public Point getContentSize() {
		return contentSize;
	}
	
	/**
	 *  给定SurfaceView父控件的尺寸和预览尺寸，返回合适的SurfaceView尺寸
	 * */
	public static Point getFitSize(Point parentViewSize, Size previewSize) {
		int maxSurfaceWidth = Math.max(parentViewSize.x, parentViewSize.y);
		int maxSurfaceHeight = Math.min(parentViewSize.x, parentViewSize.y);
		
		int preWidth = Math.max(previewSize.width, previewSize.height);
		int preHeight = Math.min(previewSize.width, previewSize.height);
		
		float ratioHeight = (float)preHeight / maxSurfaceHeight;
		float ratioWidth = (float)preWidth / maxSurfaceWidth;
		float ratio;
		
		if (ratioHeight > 1) {
			ratio = (ratioHeight > ratioWidth)? ratioHeight:ratioWidth;
		} else {
			ratio = (ratioHeight > ratioWidth)? ratioHeight:ratioWidth;
		}
		
		Point fitSize = new Point();
		fitSize.x = (int) (preWidth / ratio);
		fitSize.y = (int) (preHeight / ratio);
		
		return fitSize;
	}
	
}