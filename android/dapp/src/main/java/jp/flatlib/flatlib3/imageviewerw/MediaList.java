// 2014/11/20 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:


package	jp.flatlib.flatlib3.imageviewerw;

import	java.io.File;
import	java.io.FilenameFilter;
import	java.util.Random;
import	java.lang.System;
import	android.os.SystemClock;

import	jp.flatlib.core.GLog;


public class MediaList {

	private static final String[]	SupportList= {
		".jpeg",
		".bmp",
		".jpg",
		".png",
		".gif",
	};

	public static class FileFilter implements FilenameFilter {
		public boolean	accept( File dir, String name )
		{
			String	lname= name.toLowerCase();
			for( String ext : SupportList ){
				if( lname.endsWith( ext ) ){
					return	true;
				}
			}
			return	false;
		}
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private File[]	FileList= null;
	private int		Index= 0;

	public MediaList()
	{
	}


	public void	RefreshList( String base_folder )
	{
		File	folder= new File( base_folder );
		File[]	list= folder.listFiles( new FileFilter() );
		FileList= list;
		Index= 0;
	}



	public int	getSize()
	{
		if( FileList != null ){
			return	FileList.length;
		}
		return	0;
	}

	public String	getName( int index )
	{
		if( FileList != null ){
			return	FileList[index].getName();
		}
		return	null;
	}

	public String	getPath( int index )
	{
		if( FileList != null ){
			return	FileList[index].getPath();
		}
		return	null;
	}


}


