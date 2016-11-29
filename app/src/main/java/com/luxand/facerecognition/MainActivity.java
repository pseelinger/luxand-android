package com.luxand.facerecognition;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.luxand.FSDK;
import com.luxand.FSDK.HTracker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.Manifest;


import static com.luxand.facerecognition.R.id.clearButton;


public class MainActivity extends Activity implements OnClickListener {

	public void viewProfile(){
		Intent viewprof = new Intent(this, HomeActivity.class);
		startActivity(viewprof);
	}

	FirebaseDatabase fDatabase = FirebaseDatabase.getInstance();
	DatabaseReference userRef = fDatabase.getReference("user");
	//Storage for Memory50.dat
	FirebaseStorage storage = FirebaseStorage.getInstance();
	StorageReference memoryRef = storage.getReferenceFromUrl("gs://trial-project-80879.appspot.com/Memory50.dat");

	private DatabaseReference mDatabase;

    private boolean mIsFailed = false;
	private Preview mPreview;
	private ProcessImageAndDrawResults mDraw;
	private final String database = "Memory50.dat";
	private final String help_text = "Luxand Face Recognition\n\nJust tap any detected face and name it. The app will recognize this face further. For best results, hold the device at arm's length. You may slowly rotate the head for the app to memorize you at multiple views. The app can memorize several persons. If a face is not recognized, tap and name it again.\n\nThe SDK is available for mobile developers: www.luxand.com/facesdk";

	public static float sDensity = 1.0f;


	public void showErrorAndClose(String error, int code) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(error + ": " + code)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			})
			.show();
	}

	public void showMessage(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
				}
			})
			.setCancelable(false) // cancel with button only
			.show();
	}

    private void resetTrackerParameters() {
	    int errpos[] = new int[1];
        FSDK.SetTrackerMultipleParameters(mDraw.mTracker, "ContinuousVideoFeed=true;FacialFeatureJitterSuppression=0;RecognitionPrecision=1;Threshold=0.996;Threshold2=0.9995;ThresholdFeed=0.97;MemoryLimit=2000;HandleArbitraryRotations=false;DetermineFaceRotationAngle=false;InternalResizeWidth=70;FaceDetectionThreshold=3;", errpos);
        if (errpos[0] != 0) {
            showErrorAndClose("Error setting tracker parameters, position", errpos[0]);
        }
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// Here, thisActivity is the current activity
//		boolean x = ContextCompat.checkSelfPermission(this,
//				Manifest.permission.WRITE_EXTERNAL_STORAGE)
//				!= PackageManager.PERMISSION_GRANTED;
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 80085);
		Log.d("***********************", "goodbye");
//		if (ContextCompat.checkSelfPermission(this,
//				Manifest.permission.WRITE_EXTERNAL_STORAGE)
//				!= PackageManager.PERMISSION_GRANTED) {
//
//			// Should we show an explanation?
//			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//					Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//				ActivityCompat.requestPermissions(this,
//						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 80085);
//				Log.d("***********************", "goodbye");
//				// Show an explanation to the user *asynchronously* -- don't block
//				// this thread waiting for the user's response! After the user
//				// sees the explanation, try again to request the permission.
//
//			} else {
//
//				// No explanation needed, we can request the permission.
//
//				ActivityCompat.requestPermissions(this,
//						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 80085);
//						Log.d("**********************", "hello");
//				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//				// app-defined int constant. The callback method gets the
//				// result of the request.
//			}
//		}
		sDensity = getResources().getDisplayMetrics().scaledDensity;


		int res = FSDK.ActivateLibrary("icB/ExV1a/KMgqIy/nBStzbfiZZzgHiV+XaK9iJN+DhwVgrVyC1X2EeH8rmM8okUzLGuhOa5aYegcdqtTiTyOQoxOpOa1+Esh+zj5AyEBmQjzQ+S8RNC7FMVCNOQW3MplfUaDFugFS3PqRAbAbKwEJ0gCadZFNcOipkZGfo/DCk=");
        if (res != FSDK.FSDKE_OK) {
            mIsFailed = true;
            showErrorAndClose("FaceSDK activation failed", res);
		} else {
	        FSDK.Initialize();

			// Hide the window title (it is done in manifest too)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			// Lock orientation
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			// Camera layer and drawing layer
			mDraw = new ProcessImageAndDrawResults(this);
			mPreview = new Preview(this, mDraw);
			mDraw.mTracker = new HTracker();
			String templatePath = this.getApplicationInfo().dataDir + "/" + database;
			//handles downloading the file from Google Cloud
			try {
				final File localFile = File.createTempFile("Memory", ".dat");
				memoryRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
					@Override
					public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
						// Local temp file has been created
						String localPath = localFile.getPath();
						if (FSDK.FSDKE_OK != FSDK.LoadTrackerMemoryFromFile(mDraw.mTracker, localPath)) {
							int res = FSDK.CreateTracker(mDraw.mTracker);
							if (FSDK.FSDKE_OK != res) {
								showErrorAndClose("Error creating tracker", res);
							}
						}
					}
				}).addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception exception) {
						// Handle any errors
					}
				});


//				check if tracker already exists, if not make a new one


				resetTrackerParameters();

				this.getWindow().setBackgroundDrawable(new ColorDrawable()); //black background

				setContentView(mPreview); //creates MainActivity contents
				addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));


				// Menu
				LayoutInflater inflater = (LayoutInflater)this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
				View buttons = inflater.inflate(R.layout.bottom_menu, null );
				buttons.findViewById(R.id.helpButton).setOnClickListener(this);
				buttons.findViewById(clearButton).setOnClickListener(this);
				addContentView(buttons, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));



			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.helpButton) {
			showMessage(help_text);
		} else if (view.getId() == clearButton) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure to clear the memory?" )
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialogInterface, int j) {
						pauseProcessingFrames();
						FSDK.ClearTracker(mDraw.mTracker);
						resetTrackerParameters();
						resumeProcessingFrames();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialogInterface, int j) {
					}
				})
				.setCancelable(false) // cancel with button only
				.show();
		}
	}

	@Override
	public void onPause() {
		//saves tracker data on app pause
		String templatePath = this.getApplicationInfo().dataDir + "/" + database;
		//handles downloading the file from Google Cloud

		super.onPause();
		pauseProcessingFrames();
		templatePath = this.getApplicationInfo().dataDir + "/" + database;
		FSDK.SaveTrackerMemoryToFile(mDraw.mTracker, templatePath);
		//take the new trackers and add them to the database in Google Cloud
		Uri file = Uri.fromFile(new File(templatePath));
		UploadTask upload =  memoryRef.putFile(file);
		String localPath = this.getApplicationInfo().dataDir;

	}

	@Override
	public void onResume() {
		super.onResume();
//		try {
//			File localFile = File.createTempFile("Memory", "dat", Environment.getDataDirectory());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		if (mIsFailed)
            return;
        resumeProcessingFrames();
	}

	private void pauseProcessingFrames() {
		mDraw.mStopping = 1;

		// It is essential to limit wait time, because mStopped will not be set to 0, if no frames are feeded to mDraw
		for (int i=0; i<100; ++i) {
			if (mDraw.mStopped != 0) break;
			try { Thread.sleep(10); }
			catch (Exception ex) {}
		}
	}
	private void resumeProcessingFrames() {
		mDraw.mStopped = 0;
		mDraw.mStopping = 0;
	}
}



class FaceRectangle {
	public int x1, y1, x2, y2;
}

// Draw graphics on top of the video
class ProcessImageAndDrawResults extends View {
	public HTracker mTracker;
	
	final int MAX_FACES = 5;
	final FaceRectangle[] mFacePositions = new FaceRectangle[MAX_FACES];
	final long[] mIDs = new long[MAX_FACES];
	final Lock faceLock = new ReentrantLock();
	int mTouchedIndex;
	long mTouchedID;
	int mStopping;
	int mStopped;
	
	Context mContext;
	Paint mPaintGreen, mPaintBlue, mPaintBlueTransparent;
	byte[] mYUVData;
	byte[] mRGBData;
	int mImageWidth, mImageHeight;
	boolean first_frame_saved;
	boolean rotated;
	
	int GetFaceFrame(FSDK.FSDK_Features Features, FaceRectangle fr)
	{
		if (Features == null || fr == null)
			return FSDK.FSDKE_INVALID_ARGUMENT;
	    
	    float u1 = Features.features[0].x;
	    float v1 = Features.features[0].y;
	    float u2 = Features.features[1].x;
	    float v2 = Features.features[1].y;
	    float xc = (u1 + u2) / 2;
	    float yc = (v1 + v2) / 2;
	    int w = (int)Math.pow((u2 - u1) * (u2 - u1) + (v2 - v1) * (v2 - v1), 0.5);
	    
	    fr.x1 = (int)(xc - w * 1.6 * 0.9);
	    fr.y1 = (int)(yc - w * 1.1 * 0.9);
	    fr.x2 = (int)(xc + w * 1.6 * 0.9);
	    fr.y2 = (int)(yc + w * 2.1 * 0.9);
	    if (fr.x2 - fr.x1 > fr.y2 - fr.y1) {
	        fr.x2 = fr.x1 + fr.y2 - fr.y1;
	    } else {
	        fr.y2 = fr.y1 + fr.x2 - fr.x1;
	    }
		return 0;
	}
	
	
	public ProcessImageAndDrawResults(Context context) {
		super(context);
		
		mTouchedIndex = -1;
		
		mStopping = 0;
		mStopped = 0;
		rotated = false;
		mContext = context;
		mPaintGreen = new Paint();
		mPaintGreen.setStyle(Paint.Style.FILL);
		mPaintGreen.setColor(Color.GREEN);
		mPaintGreen.setTextSize(18 * MainActivity.sDensity);
		mPaintGreen.setTextAlign(Align.CENTER);	
		mPaintBlue = new Paint();
		mPaintBlue.setStyle(Paint.Style.FILL);
		mPaintBlue.setColor(Color.BLUE);
		mPaintBlue.setTextSize(18 * MainActivity.sDensity);
		mPaintBlue.setTextAlign(Align.CENTER);	
		
		mPaintBlueTransparent = new Paint();
		mPaintBlueTransparent.setStyle(Paint.Style.STROKE);
		mPaintBlueTransparent.setStrokeWidth(2);
		mPaintBlueTransparent.setColor(Color.BLUE);
		mPaintBlueTransparent.setTextSize(25);
		
		//mBitmap = null;
		mYUVData = null;
		mRGBData = null;
		
		first_frame_saved = false;
    }

	@Override
	protected void onDraw(Canvas canvas) {
		if (mStopping == 1) {
			mStopped = 1;
			super.onDraw(canvas);
			return;
		}
		
		if (mYUVData == null || mTouchedIndex != -1) {
			super.onDraw(canvas);
			return; //nothing to process or name is being entered now
		}
		
		int canvasWidth = canvas.getWidth();
		//int canvasHeight = canvas.getHeight();
		
		// Convert from YUV to RGB
		decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);
		
		// Load image to FaceSDK
		FSDK.HImage Image = new FSDK.HImage();
		FSDK.FSDK_IMAGEMODE imagemode = new FSDK.FSDK_IMAGEMODE();
		imagemode.mode = FSDK.FSDK_IMAGEMODE.FSDK_IMAGE_COLOR_24BIT;
		FSDK.LoadImageFromBuffer(Image, mRGBData, mImageWidth, mImageHeight, mImageWidth*3, imagemode);
		FSDK.MirrorImage(Image, false);
		FSDK.HImage RotatedImage = new FSDK.HImage();
		FSDK.CreateEmptyImage(RotatedImage);
		
		//it is necessary to work with local variables (onDraw called not the time when mImageWidth,... being reassigned, so swapping mImageWidth and mImageHeight may be not safe)
		int ImageWidth = mImageWidth;
		//int ImageHeight = mImageHeight;
		if (rotated) {
			ImageWidth = mImageHeight;
			//ImageHeight = mImageWidth;
			FSDK.RotateImage90(Image, -1, RotatedImage);
		} else {
			FSDK.CopyImage(Image, RotatedImage);
		}
		FSDK.FreeImage(Image);

		// Save first frame to gallery to debug (e.g. rotation angle)
		/*
		if (!first_frame_saved) {				
			first_frame_saved = true;
			String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
			FSDK.SaveImageToFile(RotatedImage, galleryPath + "/first_frame.jpg"); //frame is rotated!
		}
		*/
		
		long IDs[] = new long[MAX_FACES];
		long face_count[] = new long[1];
		
		FSDK.FeedFrame(mTracker, 0, RotatedImage, face_count, IDs);
		FSDK.FreeImage(RotatedImage);
								
		faceLock.lock();
			
		for (int i=0; i<MAX_FACES; ++i) {
			mFacePositions[i] = new FaceRectangle();
			mFacePositions[i].x1 = 0;
			mFacePositions[i].y1 = 0;
			mFacePositions[i].x2 = 0;
			mFacePositions[i].y2 = 0;
			mIDs[i] = IDs[i];
		}
		
		float ratio = (canvasWidth * 1.0f) / ImageWidth;
		for (int i = 0; i < (int)face_count[0]; ++i) {
			FSDK.FSDK_Features Eyes = new FSDK.FSDK_Features(); 
			FSDK.GetTrackerEyes(mTracker, 0, mIDs[i], Eyes);
		
			GetFaceFrame(Eyes, mFacePositions[i]);
			mFacePositions[i].x1 *= ratio;
			mFacePositions[i].y1 *= ratio;
			mFacePositions[i].x2 *= ratio;
			mFacePositions[i].y2 *= ratio;
		}
		
		faceLock.unlock();
		
		int shift = (int)(22 * MainActivity.sDensity);

		// Mark and name faces
		for (int i=0; i<face_count[0]; ++i) {
			canvas.drawRect(mFacePositions[i].x1, mFacePositions[i].y1, mFacePositions[i].x2, mFacePositions[i].y2, mPaintBlueTransparent);
			
			boolean named = false;
			if (IDs[i] != -1) {
				String names[] = new String[1];
				FSDK.GetAllNames(mTracker, IDs[i], names, 1024);
				if (names[0] != null && names[0].length() > 0) {
					canvas.drawText(names[0], (mFacePositions[i].x1+mFacePositions[i].x2)/2, mFacePositions[i].y2+shift, mPaintBlue);
					named = true;
				}
			}			
			if (!named) {
				canvas.drawText("Tap to name", (mFacePositions[i].x1+mFacePositions[i].x2)/2, mFacePositions[i].y2+shift, mPaintGreen);
			}
		}
		
		super.onDraw(canvas);      
	} // end onDraw method

	
	@Override
	public boolean onTouchEvent(MotionEvent event) { //NOTE: the method can be implemented in Preview class
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int x = (int)event.getX();
			int y = (int)event.getY();
			
			faceLock.lock();
			FaceRectangle rects[] = new FaceRectangle[MAX_FACES];
			long IDs[] = new long[MAX_FACES];
			for (int i=0; i<MAX_FACES; ++i) {
				rects[i] = new FaceRectangle();
				rects[i].x1 = mFacePositions[i].x1;
				rects[i].y1 = mFacePositions[i].y1;
				rects[i].x2 = mFacePositions[i].x2;
				rects[i].y2 = mFacePositions[i].y2;
				IDs[i] = mIDs[i];
			}
			faceLock.unlock();
			
			for (int i=0; i<MAX_FACES; ++i) {
				if (rects[i] != null && rects[i].x1 <= x && x <= rects[i].x2 && rects[i].y1 <= y && y <= rects[i].y2 + 30) {
					mTouchedID = IDs[i];
					
					mTouchedIndex = i;
					
					// requesting name on tapping the face	
					final EditText input = new EditText(mContext);
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setMessage("Enter person's name" )
						.setView(input)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							@Override public void onClick(DialogInterface dialogInterface, int j) {
								FSDK.LockID(mTracker, mTouchedID);
								String userName = input.getText().toString();
								FSDK.SetName(mTracker, mTouchedID, userName);
								if (userName.length() <= 0) FSDK.PurgeID(mTracker, mTouchedID);
								FSDK.UnlockID(mTracker, mTouchedID);
								mTouchedIndex = -1;
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override public void onClick(DialogInterface dialogInterface, int j) {
								mTouchedIndex = -1;
							}
						})
						.setCancelable(false) // cancel with button only
						.show();
					
					break;
				}
			}
		}
		return true;
	}
	
	static public void decodeYUV420SP(byte[] rgb, byte[] yuv420sp, int width, int height) {
		final int frameSize = width * height;
		int yp = 0;
		for (int j = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0) y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}	
				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);
				if (r < 0) r = 0; else if (r > 262143) r = 262143;
				if (g < 0) g = 0; else if (g > 262143) g = 262143;
				if (b < 0) b = 0; else if (b > 262143) b = 262143;
				
				rgb[3*yp] = (byte) ((r >> 10) & 0xff);
				rgb[3*yp+1] = (byte) ((g >> 10) & 0xff);
				rgb[3*yp+2] = (byte) ((b >> 10) & 0xff);
				++yp;
			}
		}
	}  
} // end of ProcessImageAndDrawResults class




// Show video from camera and pass frames to ProcessImageAndDraw class 
class Preview extends SurfaceView implements SurfaceHolder.Callback {
	Context mContext;
	SurfaceHolder mHolder;
	Camera mCamera;
	ProcessImageAndDrawResults mDraw;
	boolean mFinished;

	Preview(Context context, ProcessImageAndDrawResults draw) {
		super(context);      
		mContext = context;
		mDraw = draw;
		
		//Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}


	//SurfaceView callback
	public void surfaceCreated(SurfaceHolder holder) {

		mFinished = false;
		int cameraId = 1;

		// Find the ID of the camera

		boolean frontCameraFound = false;
		boolean backCameraFound = false;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
				cameraId = i;
				backCameraFound = true;
			}
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				frontCameraFound = true;
			}
		}
		
		if (frontCameraFound) {
			mCamera = Camera.open(cameraId);
		} else {
			mCamera = Camera.open();
		}
		
		try {
			mCamera.setPreviewDisplay(holder);
			
			// Preview callback used whenever new viewfinder frame is available
			mCamera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					if ( (mDraw == null) || mFinished )
						return;
		
					if (mDraw.mYUVData == null) {
						// Initialize the draw-on-top companion
						Camera.Parameters params = camera.getParameters();
						mDraw.mImageWidth = params.getPreviewSize().width;
						mDraw.mImageHeight = params.getPreviewSize().height;
						mDraw.mRGBData = new byte[3 * mDraw.mImageWidth * mDraw.mImageHeight]; 
						mDraw.mYUVData = new byte[data.length];			
					}
	
					// Pass YUV data to draw-on-top companion
					System.arraycopy(data, 0, mDraw.mYUVData, 0, data.length);
					mDraw.invalidate();
				}
			});
		} 
		catch (Exception exception) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("Cannot open camera" )
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				})
				.show();
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	//SurfaceView callback
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mFinished = true;
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	
	//SurfaceView callback, configuring camera
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mCamera == null) return;
		
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();

		//Keep uncommented to work correctly on phones:
		//This is an undocumented although widely known feature
		/**/
		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			parameters.set("orientation", "portrait");
			mCamera.setDisplayOrientation(90); // For Android 2.2 and above
			mDraw.rotated = true;
		} else {
			parameters.set("orientation", "landscape");
			mCamera.setDisplayOrientation(0); // For Android 2.2 and above
		}
		/**/
		
        // choose preview size closer to 640x480 for optimal performance
        List<Size> supportedSizes = parameters.getSupportedPreviewSizes();
        int width = 0;
        int height = 0;
        for (Size s: supportedSizes) {
            if ((width - 640)*(width - 640) + (height - 480)*(height - 480) > 
                    (s.width - 640)*(s.width - 640) + (s.height - 480)*(s.height - 480)) {
                width = s.width;
                height = s.height;
            }
        }
				
		//try to set preferred parameters
		try {
		    if (width*height > 0) {
                parameters.setPreviewSize(width, height);
            }
            //parameters.setPreviewFrameRate(10);
			parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			mCamera.setParameters(parameters);
		} catch (Exception ex) {
		}
		mCamera.startPreview();
		
		parameters = mCamera.getParameters();
	    Camera.Size previewSize = parameters.getPreviewSize();
	    makeResizeForCameraAspect(1.0f / ((1.0f * previewSize.width) / previewSize.height));
	}
	
	private void makeResizeForCameraAspect(float cameraAspectRatio){
		LayoutParams layoutParams = this.getLayoutParams();
		int matchParentWidth = this.getWidth();           
		int newHeight = (int)(matchParentWidth/cameraAspectRatio);
		if (newHeight != layoutParams.height) {
			layoutParams.height = newHeight;
			layoutParams.width = matchParentWidth;    
			this.setLayoutParams(layoutParams);
			this.invalidate();
		}		
	}


} // end of Preview class
