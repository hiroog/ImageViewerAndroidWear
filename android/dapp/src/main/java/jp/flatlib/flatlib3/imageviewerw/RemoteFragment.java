// 2014/11/22 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:


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
import	android.widget.AdapterView;
import	android.widget.Button;
import	android.widget.CheckBox;
import	android.content.res.Configuration;
import	android.content.Context;
import	android.content.Intent;
import	java.util.ArrayList;
import	android.content.DialogInterface;
import	android.app.AlertDialog;
import	android.graphics.BitmapFactory;
import	android.graphics.Bitmap;
import	android.widget.ImageView;
import	android.os.ParcelFileDescriptor;
import	java.io.FileDescriptor;


import	jp.flatlib.core.GLog;





public class RemoteFragment extends NamedFragment {
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------


	private TextView	mListTitle= null;
	private	FileAdapter	mFileList= null;
	private	TopActivity	mContext= null;
	private	Object		mListLock= new Object();
	private int			mLoadQueueCount= 0;
	private MediaList2	mMediaList= null;



	@Override
	public int	getPageNumber()
	{
		return	TopActivity.PAGE_REMOTE;
	}

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		GLog.p( "RemoteFragment onCreate" );
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup view_group, Bundle savedInstanceState )
	{
		GLog.p( "RemoteFragment onCreateView" );

		View	view= inflater.inflate( R.layout.remote_layout, view_group, false );

		mContext= (TopActivity)getActivity();

		{
			mFileList= new FileAdapter( mContext );
			ListView	list_view= (ListView)view.findViewById( R.id.upload_list );
			list_view.setAdapter( mFileList );

			mListTitle= (TextView)view.findViewById( R.id.list_disp );
		}


		((Button)view.findViewById( R.id.open_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						mContext.sendMessage( Command.MESSAGE_CMD_EXEC_TOP, null );
					}
			} );
/*
		((Button)view.findViewById( R.id.all_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						selectAll( true, true );
					}
			} );
*/
		((Button)view.findViewById( R.id.remove_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						removeFiles();
					}
			} );

		((Button)view.findViewById( R.id.manage_button )).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick( View button )
					{
						//mContext.changeToLocal();
						mContext.backPage();
					}
			} );

		refreshRemoteMenu();
		return	view;
	}



	//------------------------------------------------------------------------
	// File List
	//------------------------------------------------------------------------

	class SelectList implements View.OnClickListener {
		private FileInfo	Info;
		private boolean		Selected;
		public SelectList( FileInfo info, boolean selected )
		{
			Info= info;
			Selected= selected;
		}
		public String	getTitle()
		{
			return	Info.FileName;
		}
		public FileInfo	getFileInfo()
		{
			return	Info;
		}
		public Bitmap	getBitmap()
		{
			return	Info.Thumbnail;
		}
		public boolean	isSelected()
		{
			return	Selected;
		}
		public void	setSelect( boolean select )
		{
			Selected= select;
		}
		@Override
		public void	onClick( View v )
		{
			CheckBox	check= (CheckBox)v;
			Selected= check.isChecked();
		}
	}

	class FileAdapter extends InfoMenuAdapter<SelectList> {

		public FileAdapter( Context context )
		{
			super( context );
		}

		public void	add( FileInfo info, boolean selected )
		{
			add( new SelectList( info, selected ) );
		}

		@Override
		public View	getView( int position, View convertView, ViewGroup parent )
		{
			if( convertView == null ){
				convertView= Inflater.inflate( R.layout.remote_list, null );
			}
			SelectList	item= get( position );
			TextView	title= (TextView)convertView.findViewById( R.id.title );
			CheckBox	check= (CheckBox)convertView.findViewById( R.id.check );
			ImageView	image= (ImageView)convertView.findViewById( R.id.list_image );
			if( title == null || check == null || image == null ){
				convertView= Inflater.inflate( R.layout.remote_list, null );
				title= (TextView)convertView.findViewById( R.id.title );
				check= (CheckBox)convertView.findViewById( R.id.check );
				image= (ImageView)convertView.findViewById( R.id.list_image );
			}
			title.setText( item.getTitle() );
			check.setChecked( item.isSelected() );
			check.setOnClickListener( item );

			Bitmap	bitmap= item.getBitmap();
			if( bitmap != null ){
				image.setImageBitmap( bitmap );
			}else{
				image.setImageResource( R.drawable.ic_white );
				loadIcon( item.getFileInfo(), position );
			}
			return	convertView;
		}

	}



	// selected( true, true );		flip on/off
	// selected( false, true );		set on
	// selected( false, false );	set off
	@Override
	public void selectAll( boolean flip_flag, boolean set_flag )
	{
		synchronized( mListLock ){
			int	file_count= mFileList.getCount();
			int	select_sum= 0;
			if( flip_flag ){
				for( int fi= 0 ; fi< file_count ; fi++ ){
					SelectList	select= (SelectList)mFileList.getItem( fi );
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
				SelectList	select= (SelectList)mFileList.getItem( fi );
				select.setSelect( checked );
			}
			mFileList.notifyDataSetChanged();
		}
	}




	private void refreshRemoteMenuInternal( MediaList2 media_list )
	{
		synchronized( mListLock ){
			mMediaList= media_list;
			mFileList.clear();
			int	file_count= media_list.getSize();
			for( int fi= 0 ; fi< file_count ; fi++ ){
				mFileList.add( media_list.getFileInfo( fi ), false );
			}
			mFileList.notifyDataSetChanged();
		}
	}

	public void refreshRemoteMenu()
	{
		GLog.p( "Refresh RemoteMenu\n" );
		synchronized( mListLock ){
			mListTitle.setText( R.string.remote_list_name );

			mFileList.clear();
			mFileList.notifyDataSetChanged();

			mContext.getMediaList2( new MediaList2.CallEvent(){
				@Override
				public void	Run( MediaList2 list )
				{
					if( list != null ){
						refreshRemoteMenuInternal( list );
					}
				}
			});
		}
	}




	private void doRemoveFiles()
	{
		int	file_count= mFileList.getCount();
		int	selected_count= 0;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			SelectList	select= (SelectList)mFileList.getItem( fi );
			if( select.isSelected() ){
				selected_count++;
			}
		}
		if( selected_count > 0 ){
			int	index= 0;
			FileInfo[]	info_list= new FileInfo[selected_count];
			for( int fi= 0 ; fi< file_count ; fi++ ){
				SelectList	select= (SelectList)mFileList.getItem( fi );
				if( select.isSelected() ){
					info_list[index++]= select.getFileInfo();
				}
			}
			mContext.removeFile( info_list, new Runnable(){
				@Override
				public void run(){
					refreshRemoteMenu();
				}
			} );
		}
	}

	public void removeFiles()
	{
		int	selected_count= 0;
		int	file_count= mFileList.getCount();
		for( int fi= 0 ; fi< file_count ; fi++ ){
			SelectList	select= (SelectList)mFileList.getItem( fi );
			if( select.isSelected() ){
				selected_count++;
			}
		}
		if( selected_count == 0 ){
			return;
		}

		AlertDialog.Builder	builder= new AlertDialog.Builder( mContext );
		builder.setTitle( R.string.app_name );
		builder.setMessage( getString( R.string.delete_msg )
					+ " (" + selected_count + " " + getString( R.string.files_msg ) + ")" );
		builder.setPositiveButton( R.string.yes_msg,
				new DialogInterface.OnClickListener(){
					@Override
					public void	onClick( DialogInterface dialog, int w ){
						doRemoveFiles();
					}
				}
			);
		builder.setNegativeButton( R.string.no_msg, null );
		builder.setCancelable( true );
		builder.create().show();
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	loadIcon( FileInfo info, int position )
	{
		if( mMediaList == null ){
			return;
		}
		MediaList2		data_list= mMediaList;
		final FileInfo	info_= info;
		final int		position_= position;
		mLoadQueueCount++;
		data_list.openAssetA( mContext.GetApi(), info.FullName, Command.DATA_KEY_THUMB, new MediaList2.AssetEvent(){
				@Override
				public void	Run( FileDescriptor fd )
				{
					//GLog.p( "LoadICON SetView " + position_ );
					Bitmap	bitmap= BitmapFactory.decodeFileDescriptor( fd );
					info_.Thumbnail= bitmap;
					mLoadQueueCount--;
					if( mLoadQueueCount <= 0 ){
						//GLog.p( "_Refresh" );
						mFileList.notifyDataSetChanged();
					}
					/*
					FileAdapter	adapter= (FileAdapter)mFileListView.getAdapter();
					View	view= mFileListView.getChildAt( position_ );
					if( view != null ){
						GLog.p( "_set bitmap " + position_ + " " + info_.FileName );
						ImageView	image= (ImageView)view.findViewById( R.id.list_image );
						image.setImageBitmap( bitmap );
					}
					*/
					//adapter.getView( position_, view, mFileListView );
				}
			});
	}




	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

}



