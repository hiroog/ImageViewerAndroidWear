// 2014/11/22 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:


// WearPlayer    DAPP


package jp.flatlib.flatlib3.imageviewerw;


import	android.app.Activity;
import	android.app.Fragment;
import	android.app.FragmentManager;
import	android.app.FragmentTransaction;
import	android.os.Bundle;
import	android.view.View;
import	android.view.ViewGroup;
import	android.view.LayoutInflater;
import	android.view.MenuInflater;
import	android.view.Menu;
import	android.view.MenuItem;
import	android.view.ViewGroup;
import	android.widget.TextView;
import	android.widget.ListView;
import	android.widget.GridView;
import	android.widget.AdapterView;
import	android.widget.Button;
import	android.widget.CheckBox;
import	android.widget.PopupMenu;
import	android.view.Menu;
import	android.content.res.Configuration;
import	android.content.Context;
import	android.content.Intent;
import	java.io.IOException;
import	java.io.File;
import	java.io.InputStream;
import	java.io.FileInputStream;
import	android.content.DialogInterface;
import	android.app.AlertDialog;
import	android.os.Environment;
import	android.os.Handler;
import	android.net.Uri;
import	android.provider.MediaStore;
import	android.database.Cursor;
import	android.content.ContentResolver;
import	android.graphics.Bitmap;
import	android.widget.ImageView;
import	android.text.format.Time;


import	java.util.ArrayList;

import	jp.flatlib.core.GLog;






public class LocalFragment2 extends NamedFragment {
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------


	private Button				mListTitle= null;
	private	FileAdapter			mFileList= null;
	private	TopActivity			mContext= null;
	private	String				mFolder= null;
	private	ArrayList<String>	mMenuFolder= null;
	private	Handler				mHandler= new Handler();
	private	AlertDialog			mBusyDialog= null;
	private int					mSortMode= MediaList3.SORT_DATE;

	private MediaList3			mMediaList= null;

	private	int					mQueueCounter= 0;
	private	int					mQueueRefreshCounter= 0;
	private static final int	FORCE_REFRESH_COUNT	=	5;


	private static final int	BOX_LIMIT_WIDTH		=	1024;
	private static final int	BOX_LIMIT_HEIGHT	=	1024;
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------


	@Override
	public int	getPageNumber()
	{
		return	TopActivity.PAGE_LOCAL;
	}

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mContext= (TopActivity)getActivity();
		mFolder= Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ).getPath();

		GLog.p( "LocalFragment3 onCreate" );
	}


	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup view_group, Bundle savedInstanceState )
	{
		GLog.p( "LocalFragment3 onCreateView" );

		View	view= inflater.inflate( R.layout.local_layout2, view_group, false );

		mContext= (TopActivity)getActivity();

		mListTitle= (Button)view.findViewById( R.id.folder_button );


		((Button)view.findViewById( R.id.open_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						mContext.sendMessage( Command.MESSAGE_CMD_EXEC_TOP, null );
					}
			} );

		((Button)view.findViewById( R.id.sort_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						sortFlip();
					}
			} );

		((Button)view.findViewById( R.id.upload_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						upload();
					}
			} );

		((Button)view.findViewById( R.id.manage_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						mContext.changeToRemote();
					}
			} );
		((Button)view.findViewById( R.id.folder_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						folderList();
					}
			} );

		return	view;
	}

	@Override
	public void onActivityCreated( Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );
		GLog.p( "LocalFragment3 onActivityCreated" );

		{
			mFileList= new FileAdapter( mContext );
			GridView	grid_view= (GridView)getView().findViewById( R.id.grid_list );
			grid_view.setAdapter( mFileList );
			/*
			grid_view.setOnItemClickListener( new AdapterView.OnItemClickListener() {
				@Override
				public void	onItemClick( AdapterView<?> parent, View v, int position, long id )
				{
					//
					GLog.p( "Click " + position + " " + id );
				}
			});
			*/
			//mListTitle= (Button)findViewById( R.id.folder_button );
		}

		refreshMenu( true );
	}


	//------------------------------------------------------------------------
	// File List
	//------------------------------------------------------------------------

	class FileAdapter extends InfoMenuAdapter<SelectListItem2> {

		public FileAdapter( Context context )
		{
			super( context );
		}

		public void	add( FileInfo info )
		{
			add( new SelectListItem2( info ) );
		}

		@Override
		public View	getView( int position, View convertView, ViewGroup parent )
		{
			if( convertView == null ){
				convertView= Inflater.inflate( R.layout.grid_list, null );
			}
			SelectListItem2	item= get( position );
			TextView	file_name= (TextView)convertView.findViewById( R.id.image_name );
			TextView	image_date= (TextView)convertView.findViewById( R.id.image_date );
			CheckBox	check= (CheckBox)convertView.findViewById( R.id.check );
			ImageView	image= (ImageView)convertView.findViewById( R.id.list_image );
			if( image == null || check == null ){
				convertView= Inflater.inflate( R.layout.grid_list, null );
				file_name= (TextView)convertView.findViewById( R.id.image_name );
				image_date= (TextView)convertView.findViewById( R.id.image_date );
				check= (CheckBox)convertView.findViewById( R.id.check );
				image= (ImageView)convertView.findViewById( R.id.list_image );
			}
			check.setChecked( item.isSelected() );
			item.setCheckBox( check );
			check.setOnClickListener( item );

			Time	time= new Time();
			time.set( item.getDate() );
			//GLog.p( "D " + time.format( "%c" ) );
			image_date.setText( time.format( "%c" ) );

			file_name.setText( item.getTitle() );

			Bitmap	bitmap= null;
			synchronized( item ){
				bitmap= item.getBitmap();
			}
			if( bitmap != null ){
				image.setImageBitmap( bitmap );
				image.setOnClickListener( item );
			}else{
				image.setImageResource( R.drawable.ic_white );
				if( !item.isLoading() ){
					loadIcon( item, position );
				}
			}
			return	convertView;
		}

	}



	private void refreshMenu( boolean reload )
	{
		if( !reload && mMediaList != null ){
			mMediaList.setSortMode( mSortMode );
			mMediaList.sortList();
			FileAdapter		adapter= mFileList;
			adapter.clear();
			int		file_count= mMediaList.getSize();
			for( int fi= 0 ; fi< file_count ; fi++ ){
				adapter.add( mMediaList.getFileInfo( fi ) );
			}
			adapter.notifyDataSetChanged();
			return;
		}

		mListTitle.setText( shortFolderName( mFolder ) );


		MediaList3	file_list= new MediaList3();
		file_list.setSortMode( mSortMode );
		file_list.RefreshList( mFolder, mContext );
		mMediaList= file_list;

		FileAdapter		adapter= mFileList;
		int		file_count= file_list.getSize();
		GLog.p( "File=" + file_count );

		adapter.clear();
		for( int fi= 0 ; fi< file_count ; fi++ ){
			adapter.add( file_list.getFileInfo( fi ) );
		}
		synchronized( this ){
			//mQueueCounter= 0;
		}
		adapter.notifyDataSetChanged();
	}

	private void openFolder( String folder )
	{
		mFolder= folder;
		refreshMenu( true );
	}

	// selected( true, true );		flip on/off
	// selected( false, true );		set on
	// selected( false, false );	set off
	@Override
	public void selectAll( boolean flip_flag, boolean set_flag )
	{
		int	file_count= mFileList.getCount();
		int	select_sum= 0;
		if( flip_flag ){
			for( int fi= 0 ; fi< file_count ; fi++ ){
				SelectListItem2	select= (SelectListItem2)mFileList.getItem( fi );
				if( select.isSelected() ){
					select_sum++;
				}
			}
		}
		boolean	checked= set_flag;
		if( select_sum == file_count ){
			checked= false;
		}
		for( int fi= 0 ; fi< file_count ; fi++ ){
			SelectListItem2	select= (SelectListItem2)mFileList.getItem( fi );
			select.setSelect( checked );
		}
		mFileList.notifyDataSetChanged();
	}



	private void	openDialog()
	{
		AlertDialog.Builder	builder= new AlertDialog.Builder( mContext );
		builder.setTitle( R.string.app_name );
		builder.setMessage( getString( R.string.busy_msg ) );
		builder.setCancelable( false );
		mBusyDialog= builder.create();
		mBusyDialog.show();
	}

	private void	closeDialog()
	{
		if( mBusyDialog != null ){
			mBusyDialog.dismiss();
			mBusyDialog= null;
		}
	}


	private void doUploadThread( FileInfo[] info_list )
	{
		mContext.sendFile( info_list );

		mHandler.post( new Runnable() {
				@Override
				public void	run() {
					closeDialog();
				}
			} );
	}

	private void doUpload()
	{
		int	file_count= mFileList.getCount();
		int	selected_count= 0;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			SelectListItem2	select= (SelectListItem2)mFileList.getItem( fi );
			if( select.isSelected() ){
				selected_count++;
			}
		}
		if( selected_count > 0 ){
			int	index= 0;
			FileInfo[]	info_list= new FileInfo[selected_count];
			for( int fi= 0 ; fi< file_count ; fi++ ){
				SelectListItem2	select= (SelectListItem2)mFileList.getItem( fi );
				if( select.isSelected() ){
					info_list[index++]= select.getFileInfo();
				}
			}
			openDialog();
			final FileInfo[]	info_list_= info_list;
			new Thread( new Runnable() {
					@Override
					public void	run() {
						doUploadThread( info_list_ );
					}
				} ).start();
		}
	}

	public void upload()
	{
		int	selected_count= 0;
		int	file_count= mFileList.getCount();
		for( int fi= 0 ; fi< file_count ; fi++ ){
			SelectListItem2	select= (SelectListItem2)mFileList.getItem( fi );
			if( select.isSelected() ){
				selected_count++;
			}
		}
		if( selected_count == 0 ){
			return;
		}

		AlertDialog.Builder	builder= new AlertDialog.Builder( mContext );
		builder.setTitle( R.string.app_name );
		builder.setMessage( getString( R.string.append_msg )
					+ " (" + selected_count + " " + getString( R.string.files_msg ) + ")" );
		builder.setPositiveButton( R.string.yes_msg,
				new DialogInterface.OnClickListener(){
					@Override
					public void	onClick( DialogInterface dialog, int w ){
						doUpload();
					}
				}
			);
		builder.setNegativeButton( R.string.no_msg, null );
		builder.setCancelable( true );
		builder.create().show();
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------


	private String	shortFolderName( String full_path, String base_root )
	{
		if( full_path.startsWith( base_root ) ){
			return	full_path.substring( base_root.length() );
		}
		return	full_path;
	}

	private String	shortFolderName( String full_path )
	{
		return	shortFolderName( full_path, Environment.getExternalStorageDirectory().getPath() );
	}

	private void	enumFolder( ArrayList<String> list, File root )
	{
		for( String name : root.list() ){
			File	dir= new File( root, name );
			if( dir.isDirectory() ){
				String	base_name= dir.getName();
				if( base_name.equalsIgnoreCase( "cache" ) ){
					continue;
				}
				/*if( base_name.equalsIgnoreCase( "DCIM" ) ){
					continue;
				}*/
				if( base_name.equalsIgnoreCase( "Android" ) ){
					continue;
				}
				if( base_name.charAt(0) == '.' ){
					continue;
				}
				//list.add( shortFolderName( dir.getPath(), base_root ) );
				list.add( dir.getPath() );
				enumFolder( list, dir );
			}
		}
	}


	private void	folderList()
	{
		ArrayList<String>	list= new ArrayList<String>();
		File	folder= Environment.getExternalStorageDirectory();
		enumFolder( list, folder );

		PopupMenu	popup_menu= new PopupMenu( mContext, mListTitle );
		Menu	menu= popup_menu.getMenu();
		int		index= 0;
		for( String name : list ){
			menu.add( Menu.NONE, index++, Menu.NONE, shortFolderName( name, folder.getPath() ) );
		}
		mMenuFolder= list;
		popup_menu.setOnMenuItemClickListener(
				new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick( MenuItem item ) {
						int	index= item.getItemId();
						//openFolder( item.getTitle().toString() );
						openFolder( mMenuFolder.get(index) );
						return	true;
					}
				}
			);
		popup_menu.show();
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	sortFlip()
	{
		Button	button;
		switch( mSortMode ){
		default:
		case MediaList3.SORT_DATE:
			mSortMode= MediaList3.SORT_NAME;
			button= (Button)getView().findViewById( R.id.sort_button );
			button.setText( getString( R.string.menu_sortname ) );
			break;
		case MediaList3.SORT_NAME:
			mSortMode= MediaList3.SORT_DATE;
			button= (Button)getView().findViewById( R.id.sort_button );
			button.setText( getString( R.string.menu_sortdate ) );
			break;
		}
		refreshMenu( false );
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private Bitmap	BoxBitmap( Bitmap bitmap, int limit_w, int limit_h )
	{
		int	w= bitmap.getWidth();
		int	h= bitmap.getHeight();
		if( w == h ){
			return	bitmap;
		}
		if( w > h ){
			int	size= h;
			bitmap= Bitmap.createBitmap( bitmap, (w - size)>>1, 0, size, size );
		}else{
			int	size= w;
			bitmap= Bitmap.createBitmap( bitmap, 0, (h - size)>>1, size, size );
		}
		return	bitmap;
	}


	private void	loadIcon( SelectListItem2 item, int position )
	{

		final SelectListItem2	item_= item;
		final int				position_= position;
		synchronized( item ){
			if( item_.getBitmap() != null ){
				return;
			}
			if( item_.isLoading() ){
				return;
			}
			item_.setLoading( true );
		}
		synchronized( this ){
			mQueueCounter++;
			//GLog.p( "req Counter=" + mQueueCounter );
		}
		mContext.PostLoader(
			new Runnable(){
				@Override
				public void	run()
				{
					boolean	do_load= false;
					synchronized( item_ ){
						if( item_.getBitmap() == null ){
							do_load= true;
						}
					}
					if( do_load ){
						ContentResolver	resolver= mContext.getApplicationContext().getContentResolver();
						long	id= item_.getItemID();
						Bitmap	bitmap= MediaStore.Images.Thumbnails.getThumbnail( resolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null );
						bitmap= BoxBitmap( bitmap, BOX_LIMIT_WIDTH, BOX_LIMIT_HEIGHT );
						//Bitmap	bitmap= mBitmapLoader.Load( item_.getFilename(), THUMBNAIL_SIZE );
						synchronized( item_ ){
							item_.setBitmap( bitmap );
							item_.setLoading( false );
						}
					}
					boolean	dorefresh= false;
					synchronized( this ){
						mQueueCounter--;
						//GLog.p( "DONE Counter=" + mQueueCounter );
						if( mQueueCounter <= 0 ){
							mQueueCounter= 0;
							dorefresh= true;
							//GLog.p( "Counter==0" );
						}else{
							mQueueRefreshCounter++;
							if( mQueueRefreshCounter >= FORCE_REFRESH_COUNT ){
								dorefresh= true;
								mQueueRefreshCounter= 0;
							}
						}
					}
					if( dorefresh ){
						mContext.PostMainHandler( new Runnable(){
							@Override
							public void	run()
							{
								mFileList.notifyDataSetChanged();
								//GLog.p( "DO REFRESH" );
							}
						});
					}
				}
			}
		);

	}


}



