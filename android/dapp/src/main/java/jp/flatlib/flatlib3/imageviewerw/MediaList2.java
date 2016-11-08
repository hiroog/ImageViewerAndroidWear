// 2014/11/20 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:

// WearPlayer   DAPP


package	jp.flatlib.flatlib3.imageviewerw;

import	java.io.File;
import	java.io.FilenameFilter;
import	java.io.FileDescriptor;
import	java.util.Random;
import	java.lang.System;
import	java.util.Set;
import	java.util.Map;
import	android.os.SystemClock;
import	java.util.ArrayList;

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




public class MediaList2 extends MediaList {


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private FileInfo[]			FileList= null;
	private ArrayList<FileInfo>	TempFileList= null;
	private int			StorageImagePathLength= 0;

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

	public int	getSize()
	{
		if( FileList == null ){
			return	0;
		}
		return	FileList.length;
		//return	FileList.size();
	}

	public String	getName( int index )
	{
		return	FileList[index].FileName;
		//return	FileList.get( index );
	}

	public FileInfo	getFileInfo( int index )
	{
		return	FileList[index];
	}


	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private void	refreshListInternal( GoogleApiClient mApiClient, DataItem item )
	{
		String	file_name= item.getUri().getPath().substring( StorageImagePathLength );
		DataMapItem	ditem= DataMapItem.fromDataItem( item );
		DataMap		dmap= ditem.getDataMap();

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
	}

	public void	RefreshList( GoogleApiClient mApiClient, CallEvent event )
	{
		final CallEvent			event_= event;
		final GoogleApiClient	mApiClient_= mApiClient;

		TempFileList= new ArrayList<FileInfo>();
		TempFileList.clear();

		Wearable.DataApi.getDataItems( mApiClient )
			.setResultCallback( new ResultCallback<DataItemBuffer>() {
				@Override
				public void onResult( DataItemBuffer result )
				{
					if( result.getStatus().isSuccess() ){
						for( DataItem item : result ){
							if( item.getUri().getPath().startsWith( Command.STORAGE_IMAGE_PATH ) ){
								refreshListInternal( mApiClient_, item );
							}
						}
						finishArray();
						if( event_ != null ){
							event_.Run( MediaList2.this );
						}
					}else{
						if( event_ != null ){
							event_.Run( null );
						}
					}
					TempFileList= null;
					result.release();
				}
			} );
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

}


