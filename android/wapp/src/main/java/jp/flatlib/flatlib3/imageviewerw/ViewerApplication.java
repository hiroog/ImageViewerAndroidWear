// 2014/11/19 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:

package jp.flatlib.flatlib3.imageviewerw;



import	android.app.Application;
import	android.content.Context;
import	java.util.ArrayList;
import	jp.flatlib.core.GLog;
import	java.util.Locale;
import	android.graphics.Bitmap;




public class ViewerApplication extends Application {

	private	Bitmap		mBitmap;
	private	MediaList2	mMediaList;
	private int			mIndex;

	@Override
	public void onCreate()
	{
		GLog.p( "Application onCreate\n" );
		super.onCreate();
	}


	@Override
	public void onTerminate()
	{
		GLog.p( "Application onTerminate\n" );
		mBitmap= null;
		mMediaList= null;
		super.onTerminate();
	}



	public void	setBitmap( Bitmap bitmap )
	{
		mBitmap= bitmap;
	}

	public Bitmap	getBitmap()
	{
		return	mBitmap;
	}


	public void	setIndex( int index )
	{
		mIndex= index;
	}

	public int	getIndex()
	{
		return	mIndex;
	}


	public void	setMediaList( MediaList2 list )
	{
		mMediaList= list;
	}

	public MediaList2	getMediaList()
	{
		return	mMediaList;
	}

}


