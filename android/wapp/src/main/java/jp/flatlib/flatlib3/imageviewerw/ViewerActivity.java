// 2014/11/19 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:

// WearPlayer   WAPP


package jp.flatlib.flatlib3.imageviewerw;


import	android.app.Activity;
import	android.os.Bundle;
import	android.view.View;
import	android.view.MenuInflater;
import	android.view.Menu;
import	android.view.MenuItem;
import	android.view.ViewGroup;
import	android.view.MotionEvent;
import	android.view.Display;
import	android.view.WindowManager;
import	android.widget.TextView;
import	android.widget.Button;
import	android.content.res.Configuration;
import	android.content.Context;
import	android.content.Intent;
import	java.util.ArrayList;
import	android.support.v4.view.ViewPager;
import	android.support.v13.app.FragmentPagerAdapter;
import	android.app.Fragment;
import	android.app.FragmentManager;
import	android.media.AudioManager;
import	java.io.IOException;
import	android.os.ParcelFileDescriptor;
import	android.widget.ImageView;
import	java.io.FileDescriptor;
import	android.graphics.Bitmap;
import	android.graphics.BitmapFactory;
import	android.graphics.Matrix;
import	android.util.FloatMath;
import	android.graphics.RectF;
import	android.animation.ObjectAnimator;
import	android.animation.AnimatorSet;
import	android.os.Handler;
import	android.os.Message;
import	android.os.SystemClock;
import	java.lang.Runnable;

import	jp.flatlib.core.GLog;




import	com.google.android.gms.common.api.GoogleApiClient;
import	com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import	com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import	com.google.android.gms.common.api.ResultCallback;
import	com.google.android.gms.common.ConnectionResult;
import	com.google.android.gms.wearable.Wearable;
import	com.google.android.gms.wearable.DataApi;
import	com.google.android.gms.wearable.PutDataMapRequest;
import	com.google.android.gms.wearable.PutDataRequest;
import	com.google.android.gms.wearable.DataEventBuffer;
import	com.google.android.gms.wearable.DataEvent;
import	com.google.android.gms.wearable.DataMap;
import	com.google.android.gms.wearable.DataMapItem;





public class ViewerActivity extends Activity
			implements View.OnTouchListener,
				ConnectionCallbacks, OnConnectionFailedListener
					{
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private	GoogleApiClient	mApiClient;
	private MediaList2	mMediaList;
	private int			mIndex= 0;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	class AnimationStep implements Runnable {
		private static final int	ANIMATION_STEP_TIME=	16;
		private	int		mScrollY;
		private	int		mStepY;
		private View	mView;
		private long	mTime;
		private boolean	mActive;
		public AnimationStep()
		{
		}
		public void	SetScroll( View view, int scroll, int speed )
		{
			mView= view;
			mScrollY= scroll;
			mStepY= speed;
		}
		@Override
		public void run()
		{
			if( !mActive ){
				return;
			}
			int	sy= mView.getScrollY();
			if( sy > mScrollY ){
				sy-= mStepY;
				if( sy <= mScrollY ){
					mView.scrollTo( 0, mScrollY );
					//mView.scrollTo( 0, 0 );
					return;
				}
			}else{
				sy+= mStepY;
				if( sy >= mScrollY ){
					mView.scrollTo( 0, mScrollY );
					//mView.scrollTo( 0, 0 );
					return;
				}
			}
			mView.scrollTo( 0, sy );
			for(; SystemClock.uptimeMillis() > mTime ;){
				mTime+= ANIMATION_STEP_TIME;
			}
			if( mActive && mHandler != null ){
				mHandler.postAtTime( this, mTime );
			}
		}
		public void	Start()
		{
			mActive= true;
			mTime= SystemClock.uptimeMillis() + ANIMATION_STEP_TIME;
			mHandler.postAtTime( this, mTime );
		}
		public void	Stop()
		{
			mActive= false;
		}
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	class ScrollAnimation implements Runnable {
		private static final int	ANIMATION_STEP_TIME=	16;
		private static final float	FADE_SCALE=	0.9f;
		private	float	mVX;
		private	float	mVY;
		private View	mView;
		private long	mTime;
		private boolean	mActive;
		public ScrollAnimation()
		{
		}
		public void	Set( View view, float vx, float vy )
		{
			mView= view;
			mVX= vx;
			mVY= vy;
		}
		@Override
		public void run()
		{
			if( !mActive ){
				return;
			}
			if( mVX != 0.0f && mVY != 0.0f ){
				mMatrix.postTranslate( mVX, mVY );
				AdjustRange();
				FlushMatrix();
				mVX*= FADE_SCALE;
				mVY*= FADE_SCALE;
			}
			for(; SystemClock.uptimeMillis() > mTime ;){
				mTime+= ANIMATION_STEP_TIME;
			}
			if( mActive && mHandler != null ){
				mHandler.postAtTime( this, mTime );
			}
		}
		public void	Start()
		{
			mActive= true;
			mVX= 0.0f;
			mVY= 0.0f;
			mTime= SystemClock.uptimeMillis() + ANIMATION_STEP_TIME;
			mHandler.postAtTime( this, mTime );
		}
		public void	Stop()
		{
			mActive= false;
		}
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private Handler			mHandler= new Handler();
	private AnimationStep	mPageAnimation= new AnimationStep();
	private ScrollAnimation	mScrollAnimation= new ScrollAnimation();

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private static final int	ZOOM_RESET= 0;
	private static final int	ZOOM_STEP1= 1;
	private static final int	ZOOM_STEP2= 1;
	private static final int	ZOOM_FREE= 10;


	private	Bitmap		mBitmap;
	private ImageView	mImageView;
	private	Matrix		mMatrix= null;

	private	float	mImageWidth;
	private	float	mImageHeight;
	private float	mScreenWidth;
	private float	mScreenHeight;
	private RectF	mTempRect= new RectF();

	private Bitmap	mTempBitmap= null;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private int		mZoomMode= ZOOM_RESET;
	private boolean	mIsEdge= false;
	private	float	mTouchStartX;
	private	float	mTouchStartY;
	private	float	mStartX;
	private	float	mStartY;
	private int		mTouchID= -1;
	private int		mClickCount= 0;
	private long	mStartTime= 0;
	private long	mPrevTime= 0;
	private float	mPrevDistance= 0.0f;
	private static final long	CLICK_TIME= 200;	// 100ms
	private static final long	DOUBLE_CLICK_TIME= 500;	// 100ms
	private static final float	CLICK_LIMIT= 5.0f;
	private static final int	INVALID_TOUCH_ID= -1;


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	AdjustRange()
	{
		mTempRect.set( 0.0f, 0.0f, mImageWidth, mImageHeight );
		mMatrix.mapRect( mTempRect );

		mIsEdge= mTempRect.left >= 0.0f;

		float	width= mTempRect.width();
		float	move_to_x= 0.0f;
		if( width < mScreenWidth ){
			move_to_x= (mScreenWidth - width) * 0.5f - mTempRect.left;
		}else{
			if( mTempRect.left > 0.0f ){
				move_to_x= -mTempRect.left;
			}
			if( mTempRect.right < mScreenWidth ){
				move_to_x= mScreenWidth - mTempRect.right;
			}
		}

		float	height= mTempRect.height();
		float	move_to_y= 0.0f;
		if( height < mScreenHeight ){
			move_to_y= (mScreenHeight - height) * 0.5f - mTempRect.top;
		}else{
			if( mTempRect.top > 0.0f ){
				move_to_y= -mTempRect.top;
			}
			if( mTempRect.bottom < mScreenHeight ){
				move_to_y= mScreenHeight - mTempRect.bottom;
			}
		}
		mMatrix.postTranslate( move_to_x, move_to_y );
	}

	private void	ScaleToMatrix( float cx, float cy, float scale )
	{
		mMatrix.postTranslate( -cx, -cy );
		mMatrix.postScale( scale, scale );
		mMatrix.postTranslate( cx, cy );
	}

	private void	ResetScale()
	{
		mMatrix.reset();
		float	scale_w= mScreenWidth / mImageWidth;
		float	scale_h= mScreenHeight / mImageHeight;
		float	scale=  scale_w < scale_h ? scale_w : scale_h;
		ScaleToMatrix( 0.0f, 0.0f, scale );
		AdjustRange();
	}

	private void	FlushMatrix()
	{
		mImageView.setImageMatrix( mMatrix );
	}

	private boolean	IsReset()
	{
		return	mZoomMode == ZOOM_RESET;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	StepScale( float cx, float cy )
	{
		if( mZoomMode == ZOOM_FREE ){
			ResetScale();
			FlushMatrix();
			mZoomMode= ZOOM_RESET;
		}else{
			ScaleToMatrix( cx, cy, 4.0f );
			AdjustRange();
			FlushMatrix();
			mZoomMode= ZOOM_FREE;
		}
	}


	private void	Zoom( float x, float y, float scale )
	{
		ScaleToMatrix( x, y, scale );
		AdjustRange();
		FlushMatrix();
		mZoomMode= ZOOM_FREE;
	}


	private void	Scroll( float x, float y )
	{
		mScrollAnimation.Set( mImageView, x, y );
		AdjustRange();
		FlushMatrix();
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	ScrollAnimation( float target_y )
	{
		mPageAnimation.SetScroll( mImageView, (int)target_y, 20 );
		mPageAnimation.Start();
	}

	private void	PageScroll( float x, float y )
	{
		float	yp= mImageView.getScrollY() - y;
		if( yp < 0.0f ){
			if( mIndex <= 0 ){
				mImageView.scrollBy( 0, 0 );
				return;
			}
		}else if( yp > 0.0f ){
			if( mIndex >= mMediaList.getSize() -1 ){
				mImageView.scrollBy( 0, 0 );
				return;
			}
		}
		mImageView.scrollBy( 0, (int)-y );
	}


	private void	ResetScroll()
	{
		float	yp= mImageView.getScrollY();
		float	half_height= mScreenHeight * 0.4f;
		if( yp <= -half_height ){
			if( mIndex > 0 ){
				mIndex--;
				ScrollAnimation( -mScreenHeight );
				LoadIndex( mIndex );
				return;
			}
		}else if( yp >= half_height ){
			if( mIndex < mMediaList.getSize()-1 ){
				mIndex++;
				ScrollAnimation( mScreenHeight );
				LoadIndex( mIndex );
				return;
			}
		}
		mImageView.scrollTo( 0, 0 );
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	ResetClick()
	{
		mClickCount= 0;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	NewImage( Bitmap bitmap )
	{
		mBitmap= bitmap;

		mImageWidth= mBitmap.getWidth();
		mImageHeight= mBitmap.getHeight();

		ImageView	image= (ImageView)findViewById( R.id.image_view );
		mImageView= image;
		image.setImageBitmap( mBitmap );
		image.setScaleType( ImageView.ScaleType.MATRIX );
		image.setOnTouchListener( this );

		mPageAnimation.Stop();
		ResetScale();
		FlushMatrix();
		mImageView.scrollTo( 0, 0 );
		mImageView.setTranslationY( 0.0f );
	}

	private void	LoadIndex( int index )
	{
		String	name= mMediaList.getFullName( index );
		mMediaList.openAssetA( mApiClient, name, Command.DATA_KEY_ASSET, new MediaList2.AssetEvent(){
				@Override
				public void	Run( FileDescriptor fd )
				{
					Bitmap	bitmap= BitmapFactory.decodeFileDescriptor( fd );
					NewImage( bitmap );
				}
			});
	}


	private void	initSize()
	{
		if( mTempBitmap == null ){
			return;
		}
	/*
		WindowManager	win_man= getWindowManager();
		Display	disp= win_man.getDefaultDisplay();
		mScreenWidth= disp.getWidth();
		mScreenHeight= disp.getHeight();
	*/

GLog.p( "SCREEN " + mScreenWidth + ", " + mScreenHeight );

		//NewImage( app.getBitmap() );
		NewImage( mTempBitmap );
		mTempBitmap= null;
	}


	@Override
	public void	onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		GLog.p( "ViewerActivity onCreate" );

		mApiClient= new GoogleApiClient.Builder( this )
			.addConnectionCallbacks( this )
			.addOnConnectionFailedListener( this )
			.addApi( Wearable.API )
			.build();

		setContentView( R.layout.viewer );

		mMatrix= new Matrix();

		ViewerApplication	app= (ViewerApplication)getApplication();
		mTempBitmap= app.getBitmap();
		mMediaList= app.getMediaList();
		mIndex= app.getIndex();
		app.setBitmap( null );
		app.setMediaList( null );
	}

	@Override
	public void	onWindowFocusChanged( boolean focus )
	{
		GLog.p( "ViewerActivity onWindowFocusChanged " + focus );
		super.onWindowFocusChanged( focus );
		if( focus ){
			View	root= getWindow().getDecorView();
			mScreenWidth= root.getWidth();
			mScreenHeight= root.getHeight();

			initSize();
		}
	}

	private void	ExecTouch( MotionEvent event )
	{
		int	action_masked= event.getActionMasked();

		if( action_masked == MotionEvent.ACTION_MOVE ){
			int tcount= event.getPointerCount();
			if( tcount == 2 ){
				float	cx= 0.0f;
				float	cy= 0.0f;
				float	px= 0.0f;
				float	py= 0.0f;
				float	distx= 1.0f;
				float	disty= 1.0f;
				for( int ti= 0 ; ti < tcount ; ti++ ){
					float	x= event.getX(ti);
					float	y= event.getY(ti);
					int		pid= event.getPointerId( ti );
					//GLog.p( "MOVE pid=" + pid + " tc=" + tcount + "(" + ti + ") " + x + ", " + y );
					cx+= x;
					cy+= y;
					distx= x - px;
					disty= y - py;
					px= x;
					py= y;
					if( pid == mTouchID ){
						Scroll( x - mStartX, y - mStartY );
						mStartX= x;
						mStartY= y;
					}
				}
				float	dist= FloatMath.sqrt( distx * distx + disty * disty );
				if( mPrevDistance > 1.0f ){
					Zoom( cx * 0.5f, cy * 0.5f, dist / mPrevDistance );
					ResetScroll();
				}
				mPrevDistance= dist;
			}else{
				mPrevDistance= 0.0f;
				for( int ti= 0 ; ti < tcount ; ti++ ){
					float	x= event.getX(ti);
					float	y= event.getY(ti);
					int		pid= event.getPointerId( ti );
					//GLog.p( "MOVE pid=" + pid + " tc=" + tcount + " (" + ti + ") " + x + ", " + y );
					if( pid == mTouchID ){
						if( IsReset() ){
							PageScroll( x - mStartX, y - mStartY );
						}else{
							Scroll( x - mStartX, y - mStartY );
						}
						mStartX= x;
						mStartY= y;
					}
				}
			}
		}else{
			int		action_id= event.getActionIndex();
			int 	tcount= event.getPointerCount();
			float	x= event.getX( action_id );
			float	y= event.getY( action_id );
			int		pid= event.getPointerId(  action_id  );
			if( action_masked == MotionEvent.ACTION_UP || action_masked == MotionEvent.ACTION_POINTER_UP ){
				//GLog.p( "UP pid=" + pid + " tc=" + tcount + " " + x + ", " + y );
				mPrevDistance= 0.0f;
				if( mTouchID == pid ){
					long	touch_time= event.getEventTime() - mStartTime;
					if( touch_time < CLICK_TIME ){
						float	dx= Math.abs( x - mTouchStartX );
						float	dy= Math.abs( y - mTouchStartY );
						if( dx < CLICK_LIMIT && dy < CLICK_LIMIT ){
							mClickCount++;
							//GLog.p( "IS CLICK" );
							if( mClickCount == 2 ){
								//GLog.p( "IS DOUBLE CLICK" );
								StepScale( x, y );
								ResetClick();
							}
						}else{
							ResetClick();
						}
					}else{
						ResetClick();
					}
					ResetScroll();
					mTouchID= INVALID_TOUCH_ID;
				}
			}else if( action_masked == MotionEvent.ACTION_DOWN || action_masked == MotionEvent.ACTION_POINTER_DOWN ){
				//GLog.p( "DOWN pid=" + pid + " tc=" + tcount + " " + x + ", " + y );
				if( mTouchID == INVALID_TOUCH_ID ){
					mTouchID= pid;
					mTouchStartX= mStartX= x;
					mTouchStartY= mStartY= y;
					mStartTime= event.getEventTime();
					long	prev_time= mStartTime - mPrevTime;
					if( prev_time >= DOUBLE_CLICK_TIME ){
						ResetClick();
					}
					mPrevTime= mStartTime;
				}
			}
		}
	}

	public boolean	onTouch( View view, MotionEvent event )
	{
		ExecTouch( event );
		return	true;
	}

    @Override
    public boolean dispatchTouchEvent( MotionEvent event )
	{
		if( IsReset() || mIsEdge ){
			return	super.dispatchTouchEvent( event );
		}
		ExecTouch( event );
		return	true;
    }


	//-------------------------------------------------------------------------
	// ConnectionCallbacs
	//-------------------------------------------------------------------------

	@Override
	public void	onConnected( Bundle connection_hint ) {
		GLog.p( "ViewerActivity: connected" );
		//Wearable.DataApi.addListener( mApiClient, this );
	}
	@Override
	public void	onConnectionSuspended( int cause ) {
		GLog.p( "ViewerActivity: suspended" );
	}

	//-------------------------------------------------------------------------
	// OnConnectionFailedListener
	//-------------------------------------------------------------------------

	@Override
	public void onConnectionFailed( ConnectionResult result ) {
		GLog.p( "ViewerActivity: connection failed" );
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------


	@Override
	protected void onStart()
	{
		super.onStart();
		GLog.p( "ViewerActivity onStart" );
		mApiClient.connect();
		mScrollAnimation.Start();
	}


	@Override
	protected void onStop()
	{
		mScrollAnimation.Stop();
		mPageAnimation.Stop();
		if( mApiClient != null && mApiClient.isConnected() ){
			//Wearable.DataApi.removeListener( mApiClient, this );
			mApiClient.disconnect();
		}
		super.onStop();
		GLog.p( "ViewerActivity onStop" );
	}

	@Override
	protected void onDestroy()
	{
		GLog.p( "ViewerActivity onDestroy" );
		mScrollAnimation.Stop();
		mPageAnimation.Stop();
		mImageView= null;
		mBitmap= null;
		mMatrix= null;
		mHandler= null;
		mPageAnimation= null;
		mScrollAnimation= null;
		super.onDestroy();
	}
}

