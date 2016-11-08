// 2014/11/19 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:

package jp.flatlib.flatlib3.imageviewerw;


import	android.content.Intent;
import	android.app.Fragment;
import	android.os.Bundle;
import	android.view.View;
import	android.view.LayoutInflater;
import	android.view.ViewGroup;
import	android.support.wearable.view.WatchViewStub;
import	android.widget.Button;
import	android.widget.TextView;
import	android.widget.ListView;
import	android.widget.AdapterView;
import	android.content.res.Configuration;
import	android.content.Context;
import	android.content.Intent;
import	android.view.MenuInflater;
import	android.view.Menu;
import	android.view.MenuItem;
import	android.view.ViewGroup;
import	android.os.ParcelFileDescriptor;
import	java.io.FileDescriptor;
import	android.graphics.BitmapFactory;
import	android.graphics.Bitmap;
import	android.widget.ImageView;


import	jp.flatlib.core.GLog;


public class ControlFragment extends Fragment
			//implements AdapterView.OnItemClickListener
	{

	private	TopActivity	mContext;
	private ListView	mFileListView;
	private Bitmap		mLoadingImage;
	private int			mLoadQueueCount= 0;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	class ClickItem implements View.OnClickListener {
		private FileInfo	Info;
		public ClickItem( FileInfo info )
		{
			Info= info;
		}
		public String	getTitle()
		{
			return	Info.FileName;
		}
		public Bitmap	getBitmap()
		{
			return	Info.Thumbnail;
		}
		public FileInfo	getFileInfo()
		{
			return	Info;
		}
		@Override
		public void	onClick( View v )
		{
			ControlFragment.this.startViewer( Info.FullName, Info.Index );
		}
	}


	class FileAdapter extends InfoMenuAdapter<ClickItem> {

		public FileAdapter( Context context )
		{
			super( context );
		}

		public void	add( FileInfo info )
		{
			add( new ClickItem( info ) );
		}

		@Override
		public void	init_menu()
		{
			//add( "title line", 0.0, 0.0, 0.0 );
		}

		@Override
		public boolean	isEnabled( int position )
		{
			return	false;
		}

		@Override
		public View	getView( int position, View convertView, ViewGroup parent )
		{
		//GLog.p( "GET VIEW " + position );
			if( convertView == null ){
				convertView= Inflater.inflate( R.layout.result_list, null );
			}
			ClickItem	item= get( position );

			TextView	title= (TextView)convertView.findViewById( R.id.list_title );
			title.setOnClickListener( item );
			title.setText( item.getTitle() );

			ImageView	image= (ImageView)convertView.findViewById( R.id.list_image );
			image.setOnClickListener( item );
			Bitmap		bitmap= item.getBitmap();
			if( bitmap != null ){
				//GLog.p( "_SET BITMAP " + position + " " + item.getTitle() );
				image.setImageBitmap( bitmap );
			}else{
				//GLog.p( "_LOAD ICON " + position + " " + item.getTitle() );
				image.setImageResource( R.drawable.ic_white );
				loadIcon( item.getFileInfo(), position );
			}

			return	convertView;
		}
	};



	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public ControlFragment()
	{
	}

	public void	setContext( TopActivity context )
	{
		mContext= context;
	}


	@Override
	public void	onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
	}


	@Override
	public View	onCreateView( LayoutInflater inflater, ViewGroup view_group, Bundle savedInstanceState )
	{
		GLog.p( "ControlFragment: onCreateView" );
		mLoadQueueCount= 0;

		View	view= inflater.inflate( R.layout.page_control_layout, view_group, false );
		mContext= (TopActivity)getActivity();

		final WatchViewStub	stub= (WatchViewStub)view.findViewById( R.id.watch_view_stub );
		stub.setOnLayoutInflatedListener( new WatchViewStub.OnLayoutInflatedListener() {
			@Override
			public void onLayoutInflated( WatchViewStub stub )
			{
				onCreateStage2( stub );
			}
		} );
		return	view;
	}



	public void	refreshMenu()
	{
		GLog.p( "ControlFragment: RefreshMenu" );
		if( mFileListView == null ){
			return;
		}

		FileAdapter	adapter= (FileAdapter)mFileListView.getAdapter();
		if( adapter == null ){
			return;
		}

		MediaList2	data_list= mContext.GetDataList();
		adapter.clear();
		int		data_count= data_list.getSize();
		for( int di= 0 ; di< data_count ; di++ ){
			adapter.add( data_list.getFileInfo( di ) );
		}
		mLoadQueueCount= 0;
		adapter.notifyDataSetChanged();
	}



	private void	onCreateStage2( WatchViewStub stub )
	{
		GLog.p( "ControlFragment: onCreateStage2" );
		{
			FileAdapter	adapter= new FileAdapter( mContext );
			ListView	list_view= (ListView)stub.findViewById( R.id.image_list );
			list_view.setAdapter( adapter );
			mFileListView= list_view;
			//list_view.setOnItemClickListener( this );
		}
		mContext.RefreshDataList();
	}


	private void	loadIcon( FileInfo info, int position )
	{
		MediaList2		data_list= mContext.GetDataList();
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
						FileAdapter	adapter= (FileAdapter)mFileListView.getAdapter();
						adapter.notifyDataSetChanged();
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

	public void	startViewer( String name, int index )
	{
		final int	index_= index;
		MediaList2	data_list= mContext.GetDataList();
		data_list.openAssetA( mContext.GetApi(), name, Command.DATA_KEY_ASSET, new MediaList2.AssetEvent(){
				@Override
				public void	Run( FileDescriptor fd )
				{
					Bitmap	bitmap= BitmapFactory.decodeFileDescriptor( fd );
					ViewerApplication	app= (ViewerApplication)mContext.getApplication();
					app.setBitmap( bitmap );
					app.setIndex( index_ );
					app.setMediaList( mContext.GetDataList() );
					startActivity( new Intent( mContext, ViewerActivity.class ) );
					bitmap= null;
				}
			});
	}

/*
	@Override
	public void onItemClick( AdapterView<?> parent, View view, int position, long id )
	{
		MediaList2	data_list= mContext.GetDataList();
		GLog.p( "ControlFragment Click " + position );
		if( position >= 0 && position < data_list.getSize() ){

			String	name= data_list.getName( position );
			startViewer( name );
		}
	}
*/

}



