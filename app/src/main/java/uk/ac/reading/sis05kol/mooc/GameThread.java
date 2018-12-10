package uk.ac.reading.sis05kol.Level;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

public abstract class GameThread extends Thread {
	//Different mMode states
	public static final int STATE_READY = 3;
	public static final int STATE_RUNNING = 4;
	public static final int STATE_WIN = 5;

	//Control variable for the mode of the game (e.g. STATE_WIN)
	protected int mMode = 1;

	//Control of the actual running inside run()
	private boolean mRun = false;

	//The surface this thread (and only this thread) writes upon
	private SurfaceHolder mSurfaceHolder;

	//the message handler to the View/Activity thread
	private Handler mHandler;

	//Android Context - this stores almost all we need to know
	private Context mContext;

	//The view
	public GameView mGameView;

	//We might want to extend this call - therefore protected
	protected int mCanvasWidth = 1;
	protected int mCanvasHeight = 1;

	//Last time we updated the game physics
	protected long mLastTime = 0;

	protected Bitmap mBackgroundImage;

	//Used for time keeping
	private long now;
	private float elapsed;

	//Rotation vectors used to calculate orientation
	float[] mGravity;
	float[] mGeomagnetic;

	//Used to ensure appropriate threading
	static final Integer monitor = 1;


	public GameThread(GameView gameView) {
		mGameView = gameView;

		mSurfaceHolder = gameView.getHolder();
		mHandler = gameView.getmHandler();
		mContext = gameView.getContext();

		mBackgroundImage = BitmapFactory.decodeResource
				(gameView.getContext().getResources(),
						R.drawable.background);
	}

	//Pre-begin a game
	abstract public void setupBeginning();

	//Starting up the game
	public void doStart() {
		synchronized(monitor) {

			setupBeginning();

			mLastTime = System.currentTimeMillis() + 100;

			setState(STATE_RUNNING);
		}
	}

	//The thread start
	@Override
	public void run() {
		Canvas canvasRun;
		while (mRun) {
			canvasRun = null;
			try {
				canvasRun = mSurfaceHolder.lockCanvas(null);
				synchronized (monitor) {
					if (mMode == STATE_RUNNING) {
						updatePhysics();
					}
					doDraw(canvasRun);
				}
			}
			finally {
				if (canvasRun != null) {
					if(mSurfaceHolder != null)
						mSurfaceHolder.unlockCanvasAndPost(canvasRun);
				}
			}
		}
	}

	/*
	 * Surfaces and drawing
	 */
	public void setSurfaceSize(int width, int height) {
		synchronized (monitor) {
			mCanvasWidth = width;
			mCanvasHeight = height;

			// don't forget to resize the background image
			mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, width, height, true);
		}
	}

	protected void doDraw(Canvas canvas) {

		if(canvas == null) return;

		if(mBackgroundImage != null) canvas.drawBitmap(mBackgroundImage, 0, 0, null);
	}

	private void updatePhysics() {
		now = System.currentTimeMillis();
		elapsed = (now - mLastTime) / 1000.0f;

		updateGame(elapsed);

		mLastTime = now;
	}

	abstract protected void updateGame(float secondsElapsed);

	/*
	 * Control functions
	 */

	//Finger touches the screen
	public boolean onTouch(MotionEvent e) {
		if(e.getAction() != MotionEvent.ACTION_DOWN) return false;

		if(mMode == STATE_READY || mMode == STATE_WIN) {
			doStart();
			return true;
		}

		synchronized (monitor) {
			this.actionOnTouch(e.getRawX(), e.getRawY());
		}

		return false;
	}

	protected void actionOnTouch(float x, float y) {
		//Override to do something

	}

	//The Orientation has changed
	@SuppressWarnings("deprecation")
	public void onSensorChanged(SensorEvent event) {
		synchronized (monitor) {

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
				mGravity = event.values;
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				mGeomagnetic = event.values;
			if (mGravity != null && mGeomagnetic != null) {
				float R[] = new float[9];
				float I[] = new float[9];
				boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
				if (success) {
					float orientation[] = new float[3];
					SensorManager.getOrientation(R, orientation);
					actionWhenPhoneMoved(orientation[2],orientation[1],orientation[0]);
				}
			}
		}
	}

	protected void actionWhenPhoneMoved(float xDirection, float yDirection, float zDirection) {
		//Override to do something
	}



	//Send messages to View/Activity thread
	public void setState(int mode) {
		synchronized (monitor) {
			setState(mode, null);
		}
	}

	public void setState(int mode, CharSequence message) {
		synchronized (monitor) {
			mMode = mode;

			if (mMode == STATE_RUNNING) {
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", "");
				b.putInt("viz", View.INVISIBLE);
				b.putBoolean("showAd", false);
				msg.setData(b);
				mHandler.sendMessage(msg);
			}
			else {
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();

				Resources res = mContext.getResources();
				CharSequence str = "";
				if (mMode == STATE_READY)
					str = res.getText(R.string.mode_ready);
				if (mMode == STATE_WIN) {
					str = res.getText(R.string.mode_win);
				}

				b.putString("text", str.toString());
				b.putInt("viz", View.VISIBLE);

				msg.setData(b);
				mHandler.sendMessage(msg);
			}
		}
	}

	/*
	 * Getter and setter
	 */

	public void setRunning(boolean running) {
		mRun = running;
	}

}