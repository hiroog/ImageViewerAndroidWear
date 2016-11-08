// 2014/11/22 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:


// WearPlayer    DAPP


package jp.flatlib.flatlib3.imageviewerw;


import	android.view.View;
import	android.content.Context;
import	android.content.Intent;
import	android.os.Environment;
import	android.os.Handler;
import	android.graphics.BitmapFactory;
import	android.graphics.Bitmap;
import	android.widget.ImageView;
import	android.content.AsyncTaskLoader;

import	java.io.IOException;
import	java.io.File;
import	java.io.InputStream;
import	java.io.FileInputStream;
import	java.io.FileDescriptor;
import	java.util.ArrayList;

import	jp.flatlib.core.GLog;




public class BitmapLoader {

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

	public BitmapLoader()
	{
	}

	private Bitmap	GetSmallBitmap( Bitmap org_bitmap, int width, int height )
	{
		int	src_width= org_bitmap.getWidth();
		int	src_height= org_bitmap.getHeight();
		if( src_width <= width && src_height <= height ){
			return	org_bitmap;
		}
		if( src_width >= src_height ){
			height= (int)(src_height * (float)width / src_width);
		}else{
			width= (int)(src_width * (float)height / src_height);
		}
		return	Bitmap.createScaledBitmap( org_bitmap, width, height, true );
	}

	public Bitmap	Load( String file_name, int thumb_size )
	{
		GLog.p( "_load " + file_name );
		Bitmap	bitmap= BitmapFactory.decodeFile( file_name );
		if( bitmap != null ){
			bitmap= GetSmallBitmap( bitmap, thumb_size, thumb_size );
		}
		return	bitmap;
	}

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

}



