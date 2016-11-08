// 2014/11/20 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:

// WearPlayer   WAPP


package	jp.flatlib.flatlib3.imageviewerw;

import	java.io.File;
import	java.io.FilenameFilter;
import	java.io.FileDescriptor;
import	java.util.Random;
import	java.lang.System;
import	java.util.Set;
import	java.util.Map;
import	android.os.SystemClock;
import	android.os.ParcelFileDescriptor;
import	android.content.Context;
import	java.util.concurrent.TimeUnit;
import	java.util.ArrayList;
import	java.util.Arrays;
import	java.util.Comparator;
import	android.graphics.Bitmap;


import	jp.flatlib.core.GLog;


import	com.google.android.gms.wearable.DataEventBuffer;
import	com.google.android.gms.wearable.DataEvent;
import	com.google.android.gms.wearable.DataMap;
import	com.google.android.gms.wearable.DataMapItem;

import	com.google.android.gms.common.api.GoogleApiClient;
import	com.google.android.gms.wearable.PutDataMapRequest;
import	com.google.android.gms.wearable.PutDataRequest;
import	com.google.android.gms.wearable.Wearable;
import	com.google.android.gms.wearable.DataApi;
import	com.google.android.gms.wearable.Asset;
import	com.google.android.gms.wearable.MessageApi;
import	com.google.android.gms.wearable.NodeApi;
import	com.google.android.gms.wearable.Node;
import	com.google.android.gms.wearable.DataItem;
import	com.google.android.gms.wearable.DataItemBuffer;
import	com.google.android.gms.wearable.DataItemAsset;
import	com.google.android.gms.common.api.ResultCallback;
import	com.google.android.gms.common.ConnectionResult;




public class MediaList2 extends MediaList {

	public static final int		SORT_DATE	=	0;
	public static final int		SORT_NAME	=	1;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private FileInfo[]			FileList= null;
	private ArrayList<FileInfo>	TempFileList= null;
	private int			StorageImagePathLength= 0;
	private int			Index= 0;
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
		switch( SortMode ){
		default:
		case SORT_DATE: Arrays.sort( FileList, new Sort_Date() );	break;
		case SORT_NAME: Arrays.sort( FileList, new Sort_Name() );	break;
		}
		int	data_count= FileList.length;
		for( int di= 0 ; di< data_count ; di++ ){
			FileList[di].Index= di;
		}
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public static class CallEvent {
		public void	Run( MediaList2 list )
		{
		}
	}

	public static class AssetEvent {
		public void	Run( FileDescriptor fd )
		{
		}
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public MediaList2()
	{
		StorageImagePathLength= Command.STORAGE_IMAGE_PATH.length();
	}

/*
	public void	Connect( Context context )
	{
		mApiClient= new GoogleApiClient.Builder( context )
			.addApi( Wearable.API )
			.build();
		ConnectionResult	result= mApiClient.blockingConnect( 30, TimeUnit.SECONDS );
		if( !result.isSuccess() ){
			mApiClient= null;
			GLog.p( "API connect faled" );
			return;
		}
	}

	public void	Stop()
	{
		mApiClient.disconnect();
		mApiClient= null;
	}
*/

	public int	getSize()
	{
		if( FileList == null ){
			return	0;
		}
		return	FileList.length;
	}

	public String	getName( int index )
	{
		return	FileList[index].FileName;
	}

	public String	getFullName( int index )
	{
		return	FileList[index].FullName;
	}

	public FileInfo	getFileInfo( int index )
	{
		return	FileList[index];
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	refreshListInternalA( GoogleApiClient mApiClient, DataItem item )
	{
		String	file_name= item.getUri().getPath().substring( StorageImagePathLength );
		DataMapItem		ditem= DataMapItem.fromDataItem( item );
		DataMap			dmap= ditem.getDataMap();

		if( dmap.containsKey( Command.DATA_KEY_FNAME ) ){
			int	index= TempFileList.size();
			FileInfo	info= new FileInfo();
			info.FullName= file_name;
			info.FileName= dmap.getString( Command.DATA_KEY_FNAME );
			//info.Width= dmap.getInt( Command.DATA_KEY_WIDTH );
			//info.Height= dmap.getInt( Command.DATA_KEY_HEIGHT );
			info.Date= dmap.getLong( Command.DATA_KEY_DATE );
			info.Index= index;
			TempFileList.add( info );
			GLog.p( "  Asset(" + index + ") " + file_name + "  ," + item.getUri().toString() );
		}
	}

	private void	finishArray()
	{
		int	file_count= TempFileList.size();
		FileList= new FileInfo[file_count];
		for( int fi= 0 ; fi< file_count ; fi++ ){
			FileList[fi]= TempFileList.get( fi );
		}
		TempFileList.clear();
		TempFileList= null;

		sortList();
	}

	public void	RefreshListA( GoogleApiClient mApiClient, CallEvent event )
	{
		final CallEvent			event_= event;
		final GoogleApiClient	mApiClient_= mApiClient;

		TempFileList= new ArrayList<FileInfo>();
		TempFileList.clear();

		Index= 0;
		Wearable.DataApi.getDataItems( mApiClient )
			.setResultCallback( new ResultCallback<DataItemBuffer>() {
				@Override
				public void onResult( DataItemBuffer result )
				{
					if( result.getStatus().isSuccess() ){
						for( DataItem item : result ){
							if( item.getUri().getPath().startsWith( Command.STORAGE_IMAGE_PATH ) ){
								refreshListInternalA( mApiClient_, item );
							}
						}
						finishArray();
						if( event_ != null ){
							Shuffle();
							event_.Run( MediaList2.this );
						}
					}else{
						if( event_ != null ){
							event_.Run( null );
						}
						GLog.p( "RefreshList error " + result.getStatus() );
					}
					TempFileList= null;
					result.release();
				}
			} );
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public String	getNext()
	{
		if( FileList != null ){
			if( Index < FileList.length ){
				GLog.p( "getNext " + (Index+1) );
				return	FileList[Index++].FileName;
			}
			Index= 0;
			Shuffle();
			return	FileList[Index++].FileName;
		}
		return	null;
	}

	public void	setPrev()
	{
		if( FileList != null ){
			if( Index > 0 ){
				Index--;
				GLog.p( "setPrev " + Index );
				return;
			}
			Index= FileList.length-1;
			GLog.p( "setPrev " + Index );
		}
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------


	private void	openAssetInternalA( GoogleApiClient mApiClient, DataItem item, String name, String key, AssetEvent event )
	{
		final AssetEvent	event_= event;
		DataMapItem			ditem= DataMapItem.fromDataItem( item );
		DataMap				dmap= ditem.getDataMap();

		if( dmap.containsKey( key ) ){
			Asset	asset= dmap.getAsset( key );
			String	file_name= dmap.getString( Command.DATA_KEY_FNAME );
			GLog.p( "open " + file_name );
			Wearable.DataApi.getFdForAsset( mApiClient, asset )
				.setResultCallback( new ResultCallback<DataApi.GetFdForAssetResult>() {
					@Override
					public void onResult( DataApi.GetFdForAssetResult result )
					{
						if( result.getStatus().isSuccess() ){
							if( event_ != null ){
								event_.Run( result.getFd().getFileDescriptor() );
								result.release();
								return;
							}
						}
						if( event_ != null ){
							event_.Run( null );
						}
						result.release();
					}
				} );
			return;
		}else{
			GLog.p( "not found " + name );
		}
		if( event_ != null ){
			event_.Run( null );
		}
	}

	public void	openAssetA( GoogleApiClient mApiClient, String name, String key, AssetEvent event )
	{
		final AssetEvent		event_= event;
		final GoogleApiClient	mApiClient_= mApiClient;
		final String			name_= name;
		final String			key_= key;
		Wearable.DataApi.getDataItems( mApiClient )
			.setResultCallback( new ResultCallback<DataItemBuffer>() {
				@Override
				public void onResult( DataItemBuffer result )
				{
					if( result.getStatus().isSuccess() ){
						for( DataItem item : result ){
							String	path= item.getUri().getPath();
							if( path.startsWith( Command.STORAGE_IMAGE_PATH ) ){
								String	file_name= path.substring( StorageImagePathLength );
								if( file_name.equals( name_ ) ){
									openAssetInternalA( mApiClient_, item, name_, key_, event_ );
									result.release();
									return;
								}
							}
						}
					}
					if( event_ != null ){
						event_.Run( null );
					}
					result.release();
				}
			} );
	}



	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

/*
	private void	refreshListInternal( DataItem item )
	{
		Map<String,DataItemAsset>	asset_map= item.getAssets();
		Set<String>	set= asset_map.keySet();
		int			size= set.size();
		FileList= new String[size];
		int			index= 0;
		for( String key : set ){
			FileList[index++]= key;
		}
		for( int i= 0 ; i< size ; i++ ){
			GLog.p( "  Asset(" + i + ") " + FileList[i] );
		}
	}

	public void	RefreshList()
	{
		Index= 0;
		DataItemBuffer	result= Wearable.DataApi.getDataItems( mApiClient ).await();
		if( result.getStatus().isSuccess() ){
			for( DataItem item : result ){
				if( item.getUri().getPath().equals( Command.STORAGE_PATH ) ){
					refreshListInternal( item );
					break;
				}
			}
		}
	}

	public void	Refresh()
	{
		RefreshList();
		Shuffle();
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private FileDescriptor	openAssetInternal( DataItem item, String name )
	{
		Map<String,DataItemAsset>	asset_map= item.getAssets();
		if( asset_map.containsKey( name ) ){
			DataItemAsset	asset= asset_map.get( name );
			DataApi.GetFdForAssetResult	result= Wearable.DataApi.getFdForAsset( mApiClient, asset ).await();
			if( result.getStatus().isSuccess() ){
				return	result.getFd().getFileDescriptor();
			}
		}
		return	null;
	}

	public FileDescriptor	openAsset( String name )
	{
		DataItemBuffer	result= Wearable.DataApi.getDataItems( mApiClient ).await();
		if( result.getStatus().isSuccess() ){
			for( DataItem item : result ){
				if( item.getUri().getPath().equals( Command.STORAGE_PATH ) ){
					return	openAssetInternal( item, name );
				}
			}
		}
		return	null;
	}
*/
}


