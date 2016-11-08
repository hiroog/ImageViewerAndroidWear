// 2014/11/20 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:


package	jp.flatlib.flatlib3.imageviewerw;

import	java.io.File;
import	java.io.FilenameFilter;
import	java.util.Random;
import	java.lang.System;
import	android.os.SystemClock;
import	java.util.ArrayList;
import	android.net.Uri;
import	android.provider.MediaStore;
import	android.database.Cursor;
import	android.content.ContentResolver;
import	android.graphics.Bitmap;
import	android.content.Context;
import	java.util.Arrays;
import	java.util.Comparator;

import	jp.flatlib.core.GLog;


public class MediaList3 {

	public static final int		SORT_DATE	=	0;
	public static final int		SORT_NAME	=	1;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private FileInfo[]			FileList= null;
	private	ArrayList<FileInfo>	TempFileList= null;
	private int					SortMode= SORT_DATE;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public class	Sort_Date implements Comparator<FileInfo> {
		public int	compare( FileInfo a, FileInfo b )
		{
			long	diff= b.Date - a.Date;
			if( diff < 0 ){
				return	-1;
			}
			if( diff == 0 ){
				return	0;
			}
			return	1;
		}
	}

	public class	Sort_Name implements Comparator<FileInfo> {
		public int	compare( FileInfo a, FileInfo b )
		{
			return	a.FileName.compareToIgnoreCase( b.FileName );
		}
	}

	public void		setSortMode( int mode )
	{
		SortMode= mode;
	}

	public void		sortList()
	{
		if( FileList != null ){
			switch( SortMode ){
			default:
			case SORT_DATE: Arrays.sort( FileList, new Sort_Date() );	break;
			case SORT_NAME: Arrays.sort( FileList, new Sort_Name() );	break;
			}
		}
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public MediaList3()
	{
	}

	private void	finishArray()
	{
		int	file_count= TempFileList.size();
		FileList= null;
		if( file_count > 0 ){
			FileList= new FileInfo[file_count];
			for( int fi= 0 ; fi< file_count ; fi++ ){
				FileList[fi]= TempFileList.get( fi );
			}
		}
		TempFileList.clear();
		TempFileList= null;

		sortList();
	}

	public void	RefreshList( String base_folder, Context context )
	{
		ContentResolver	resolver= context.getApplicationContext().getContentResolver();
		Cursor	cur= resolver.query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					null,
					null,
					null,
					null
				);
		TempFileList= new ArrayList<FileInfo>();
		TempFileList.clear();
		if( !cur.moveToFirst() ){
			finishArray();
			return;
		}
		do{
			long	id= cur.getLong( cur.getColumnIndex( MediaStore.Images.Thumbnails._ID ) );
//			long	width= cur.getLong( cur.getColumnIndex( MediaStore.Images.Thumbnails.WIDTH ) );
//			long	height= cur.getLong( cur.getColumnIndex( MediaStore.Images.Thumbnails.HEIGHT ) );
			long	tdate= cur.getLong( cur.getColumnIndex( MediaStore.Images.Media.DATE_TAKEN ) );
			String	fname= cur.getString( cur.getColumnIndex( MediaStore.Images.Media.DATA ) );
			File	file= new File( fname );
			FileInfo	info= new FileInfo();
			info.FullName= fname;
			info.FileName= file.getName();
//			info.Width= (int)width;
//			info.Height= (int)height;
			info.db_id= id;
			info.Date= tdate;
			info.Thumbnail= null;
			info.Selected= false;
			TempFileList.add( info );
		}while( cur.moveToNext() );
		finishArray();
	}


	public int	getSize()
	{
		if( FileList != null ){
			return	FileList.length;
		}
		return	0;
	}

	public FileInfo	getFileInfo( int index )
	{
		if( FileList != null ){
			return	FileList[index];
		}
		return	null;
	}

}


