package uk.ac.reading.sis05kol.Level;

//Other parts of the android libraries that we use
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class TheGame extends GameThread {

    //Will store the image of a ball
    private Bitmap mBall;

    //The X and Y position of the ball on the screen (middle of ball)
    private float mBallX = 0;
    private float mBallY = 0;

    //The speed (pixel/second) of the ball in direction X and Y
    private float mBallSpeedX = 0;
    private float mBallSpeedY = 0;

    //This is run before anything else, so we can prepare things here
    public TheGame(GameView gameView) {
        //House keeping
        super(gameView);

        //Prepare the image so we can draw it on the screen (using a canvas)
        mBall = BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.small_red_ball);
    }

    //This is run before a new game (also after an old game)
    @Override
    public void setupBeginning() {
        //Initialise speeds
        mBallSpeedX = 0;
        mBallSpeedY = 0;

        //Place the ball in the middle of the screen.
        mBallX = mCanvasWidth / 2;
        mBallY = mCanvasHeight / 2;
    }

    @Override
    protected void doDraw(Canvas canvas) {
        //If there isn't a canvas to draw on do nothing
        //It is ok not understanding what is happening here
        if(canvas == null) return;

        super.doDraw(canvas);

        //draw the image of the ball using the X and Y of the ball
        //drawBitmap uses top left corner as reference, we use middle of picture
        //null means that we will use the image without any extra features (called Paint)
        canvas.drawBitmap(mBall, mBallX - mBall.getWidth() / 2, mBallY - mBall.getHeight() / 2, null);
    }

    //This is run whenever the phone is touched by the user
    @Override
    protected void actionOnTouch(float x, float y) {
        //if user touches the screen, restart the Level measure
        doStart();
    }


    //This is run whenever the phone moves around its axises
    @Override
    protected void actionWhenPhoneMoved(float xDirection, float yDirection, float zDirection) {
		/*
		Increase/decrease the speed of the ball.
		If the ball moves too fast try and decrease 70f
		If the ball moves too slow try and increase 70f
		 */

        mBallSpeedX = mBallSpeedX + 70f * xDirection;
        mBallSpeedY = mBallSpeedY - 70f * yDirection;
    }

    //This is run just before the game "scenario" is printed on the screen
    @Override
    protected void updateGame(float secondsElapsed) {
        //Move the ball's X and Y using the speed (pixel/sec)
        mBallX = mBallX + secondsElapsed * mBallSpeedX;
        mBallY = mBallY + secondsElapsed * mBallSpeedY;

        //Check if the ball hits either the left side or the right side of the screen
        //If it does, restart Level measurement
        if((mBallX <= mBall.getWidth() / 2 && mBallSpeedX < 0) || (mBallX >= mCanvasWidth - mBall.getWidth() / 2 && mBallSpeedX > 0) ) {
            doStart();
        }

        //check if ball hits either top or bottom of the screen. if it does, restart Level measurement
        if((mBallY <= mBall.getHeight() /2 && mBallSpeedY < 0) || (mBallY >= mCanvasHeight - mBall.getHeight() / 2 && mBallSpeedY > 0)) {
          doStart();
        }
    }
}