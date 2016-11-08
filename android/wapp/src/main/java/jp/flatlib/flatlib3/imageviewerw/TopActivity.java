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
import	android.widget.TextView;
import	android.widget.ListView;
import	android.widget.AdapterView;
import	android.widget.Button;
import	android.content.res.Configuration;
import	android.content.Context;
import	android.content.Intent;
import	jp.flatlib.core.GLog;
import	java.util.ArrayList;
import	android.support.v4.view.ViewPager;
import	android.support.v13.app.FragmentPagerAdapter;
import	android.app.Fragment;
import	android.app.FragmentManager;
import	android.media.AudioManager;
import	java.io.IOException;


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









public class TopActivity extends Activity
	implements ConnectionCallbacks, OnConnectionFailedListener ,DataApi.DataListener
					{
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private	ViewPager		mPager;
	private	TextView		mNext;
	private int				mFragmentCount;
	private ControlFragment	mControlFragment;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private	AudioManager	iAudioManager= null;
	private MediaList2		mDataList;
	private volatile boolean	mRefreshRunning= false;
	private volatile boolean	mDoRefresh= true;

	private	GoogleApiClient	mApiClient;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------


	public void setPage()
	{
		int	page= mPager.getCurrentItem();
		int	count= mFragmentCount;
		String	t= null;
		for( int ci= 0 ; ci< count ; ci++ ){
			if( ci == page ){
				if( t == null ){
					t= "●";
				}else{
					t= t + "●";
				}
			}else{
				if( t == null ){
					t= "○";
				}else{
					t= t + "○";
				}
			}
		}
		mNext.setText( t );
	}

	@Override
	public void	onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		GLog.p( "TopActivity onCreate" );


		setContentView( R.layout.page_layout );
		mDataList= new MediaList2();



		{
			mNext= (TextView)findViewById( R.id.next_button );
			mPager= (ViewPager)findViewById( R.id.pager );
			PagerAdapter	adapter= new PagerAdapter( getFragmentManager() );

			ControlFragment		control= new ControlFragment();
			control.setContext( this );
			adapter.addFragment( control );
			mControlFragment= control;

			//NextPrevFragment	nextprev= new NextPrevFragment();
			//nextprev.setContext( this );
			//adapter.addFragment( nextprev );


			MenuFragment	openphone= new MenuFragment();
			openphone.setContext( this );
			adapter.addFragment( openphone );


			mFragmentCount= adapter.getCount();
			mPager.setOnPageChangeListener( new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled( int i, float v, int index ) {
					}
					@Override
					public void onPageSelected( int i ) {
					}
					@Override
					public void onPageScrollStateChanged( int i ) {
						setPage();
					}
				});
			mPager.setAdapter( adapter );

		}
		setPage();

		mApiClient= new GoogleApiClient.Builder( this )
			.addConnectionCallbacks( this )
			.addOnConnectionFailedListener( this )
			.addApi( Wearable.API )
			.build();
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public void	RefreshDataList()
	{
		if( mRefreshRunning ){
			return;
		}
		mRefreshRunning= true;
		mDataList.RefreshListA( mApiClient, new MediaList2.CallEvent(){
				@Override
				public void	Run( MediaList2 list )
				{
					mControlFragment.refreshMenu();
					mRefreshRunning= false;
				}
			} );
	}


	//-------------------------------------------------------------------------
	// Data
	//-------------------------------------------------------------------------
	@Override
	public void onDataChanged( DataEventBuffer events ) {
		GLog.p( "TopActivity onDataChanged" );
		//EventDecoder	decode= new EventDecoder();
		//decode.Decode( events, this );
		//mDataList.RefreshListA( mApiClient, null );
	}

	//-------------------------------------------------------------------------
	// ConnectionCallbacs
	//-------------------------------------------------------------------------

	@Override
	public void	onConnected( Bundle connection_hint ) {
		GLog.p( "TopActivity: connected" );

		Wearable.DataApi.addListener( mApiClient, this );
		if( mDoRefresh ){
			mDoRefresh= false;
			RefreshDataList();
		}
	}
	@Override
	public void	onConnectionSuspended( int cause ) {
		GLog.p( "TopActivity: suspended" );
	}

	//-------------------------------------------------------------------------
	// OnConnectionFailedListener
	//-------------------------------------------------------------------------

	@Override
	public void onConnectionFailed( ConnectionResult result ) {
		GLog.p( "TopActivity: connection failed" );
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public GoogleApiClient	GetApi()
	{
		return	mApiClient;
	}

	public MediaList2	GetDataList()
	{
		return	mDataList;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	@Override
	protected void onStart()
	{
		super.onStart();
		GLog.p( "TopActivity onStart" );
		mApiClient.connect();
	}


	@Override
	protected void onStop()
	{
		if( mApiClient != null && mApiClient.isConnected() ){
			Wearable.DataApi.removeListener( mApiClient, this );
			mApiClient.disconnect();
		}
		super.onStop();
		GLog.p( "TopActivity onStop" );
	}

}

