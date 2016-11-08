// 2014/07/20 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:

// WearPlayer  DAPP

package jp.flatlib.flatlib3.imageviewerw;

import	android.content.Context;
import	android.content.SharedPreferences;
import	android.preference.PreferenceManager;
import	android.content.Intent;
import	android.graphics.Bitmap;
import	android.graphics.BitmapFactory;
import	java.io.ByteArrayOutputStream;

import	jp.flatlib.core.GLog;

import	com.google.android.gms.wearable.DataEventBuffer;
import	com.google.android.gms.wearable.DataEvent;
import	com.google.android.gms.wearable.DataMap;
import	com.google.android.gms.wearable.DataMapItem;
import	android.net.Uri;
import	java.util.HashSet;


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
import	com.google.android.gms.common.api.ResultCallback;

import	java.io.IOException;
import	java.io.File;
import	java.io.InputStream;
import	java.io.FileInputStream;




public class RequestCommand {

	private int	mRemoveCount= 0;

	private static final int	SMALL_ICON_SIZE= 256;
	private static final int	SAFE_GUARD_LIMIT_SIZE= 2048;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private byte[]	LoadFile( String full_name )
	{
		final long	LIMIT_FILE_SIZE= 1024*1024*512;
		File	file= new File( full_name );
		InputStream	fp= null;
		byte[]	binary= null;
		long	file_size_long= file.length();
		if( file_size_long >= LIMIT_FILE_SIZE ){
			GLog.p( "Skip too large " + full_name );
			return	null;
		}
		int	file_size= (int)file_size_long;
		int	read_size= 0;
		try {
			try {
				fp= new FileInputStream( file );
				binary= new byte[file_size];
				read_size= fp.read( binary, 0, file_size );
			}
			catch( IOException e ){
				GLog.p( "File Read Error " + full_name );
			}
			finally {
				if( fp != null ){
					fp.close();
				}
			}
		}
		catch( IOException e ){
			GLog.p( "File Close Error " + full_name );
		}
		if( read_size == file_size ){
			return	binary;
		}
		return	null;
	}

	private byte[]	BitmapToByte( Bitmap bitmap )
	{
		ByteArrayOutputStream	output_stream= new ByteArrayOutputStream();
		bitmap.compress( Bitmap.CompressFormat.PNG, 100, output_stream );
		return	output_stream.toByteArray();
	}

	private byte[]	GetSmallBitmap( byte[] binary, int width, int height )
	{
		Bitmap	org_bitmap= BitmapFactory.decodeByteArray( binary, 0, binary.length );
		int	src_width= org_bitmap.getWidth();
		int	src_height= org_bitmap.getHeight();
		if( src_width <= width && src_height <= height ){
			return	binary;
		}
		if( src_width >= src_height ){
			height= (int)(src_height * (float)width / src_width);
		}else{
			width= (int)(src_width * (float)height / src_height);
		}
		Bitmap	bitmap= Bitmap.createScaledBitmap( org_bitmap, width, height, true );
		org_bitmap= null;
		return	BitmapToByte( bitmap );
	}

	private byte[]	GetSafeGuard( byte[] binary, int width, int height )
	{
		Bitmap	org_bitmap= BitmapFactory.decodeByteArray( binary, 0, binary.length );
		int	src_width= org_bitmap.getWidth();
		int	src_height= org_bitmap.getHeight();
		if( src_width <= width && src_height <= height ){
			return	binary;
		}
		if( src_width >= src_height ){
			height= (int)(src_height * (float)width / src_width);
		}else{
			width= (int)(src_width * (float)height / src_height);
		}
		Bitmap	bitmap= Bitmap.createScaledBitmap( org_bitmap, width, height, true );
		org_bitmap= null;
		return	BitmapToByte( bitmap );
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

/*
	private void	AppendList( GoogleApiClient mApiClient, DataItem item, String[] full_name_list )
	{
		PutDataRequest	request= PutDataRequest.createFromDataItem( item );
		int	file_count= full_name_list.length;
		int	item_count= 0;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			String	full_name= full_name_list[fi];
			String	file_name= new File( full_name ).getName();

			GLog.p( "Add Storage : " + file_name + " (" + full_name + ")" );

			byte[]	binary= LoadFile( full_name );
			if( binary != null ){
				Asset	asset= Asset.createFromBytes( binary );
				request.putAsset( file_name, asset );
				item_count++;
			}
		}
		if( item_count != 0 ){
			Wearable.DataApi.putDataItem( mApiClient, request )
				.setResultCallback( new ResultCallback<DataApi.DataItemResult>() {
					@Override
					public void onResult( DataApi.DataItemResult result ) {
						if( result.getStatus().isSuccess() ){
							GLog.p( "SendFile: result SUCCESS " + result.getStatus().getStatusCode()  );
						}else{
							GLog.p( "SendFile: result ERROR " + result.getStatus().getStatusCode()  );
						}
					}
				} );
		}
	}

	public void	AppendSyncList( GoogleApiClient mApiClient, String[] full_name_list )
	{
		final String[]			full_name_list_= full_name_list; 
		final GoogleApiClient	mApiClient_= mApiClient;
		Wearable.DataApi.getDataItems( mApiClient )
			.setResultCallback( new ResultCallback<DataItemBuffer>() {
				@Override
				public void onResult( DataItemBuffer result ) {
					if( result.getStatus().isSuccess() ){
						for( DataItem item : result ){
							if( item.getUri().getPath().equals( Command.STORAGE_PATH ) ){
								AppendList( mApiClient_, item, full_name_list_ );
							}
						}
					}
				}
			} );
	}
*/

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

/*
	public void	AppendSyncListBlockSEP( GoogleApiClient mApiClient, String[] full_name_list )
	{
		PutDataMapRequest	request= null;

		int	file_count= full_name_list.length;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			String	full_name= full_name_list[fi];
			String	file_name= new File( full_name ).getName();

			request= PutDataMapRequest.create( Command.STORAGE_IMAGE_PATH + file_name );
			DataMap	dmap= request.getDataMap();

			GLog.p( "Add storage : " + file_name + " (" + full_name + ")" );


			byte[]	binary= LoadFile( full_name );
			if( binary != null ){

				Asset	asset= null;
				{
					int	orgsize= binary.length;
					binary= GetSafeGuard( binary, SAFE_GUARD_LIMIT_SIZE, SAFE_GUARD_LIMIT_SIZE );
					GLog.p( "compressed size=" + binary.length + " <=" + orgsize );
					asset= Asset.createFromBytes( binary );
					dmap.putAsset( Command.DATA_KEY_ASSET, asset );
					dmap.putString( Command.DATA_KEY_FNAME, file_name );
				}

				byte[]	small_binary= GetSmallBitmap( binary, SMALL_ICON_SIZE, SMALL_ICON_SIZE );
				binary= null;
				Asset	small_asset= Asset.createFromBytes( small_binary );
				dmap.putAsset( Command.DATA_KEY_THUMB, small_asset );
				small_binary= null;

				DataApi.DataItemResult	result= Wearable.DataApi.putDataItem( mApiClient, request.asPutDataRequest() ).await();
				if( result.getStatus().isSuccess() ){
					GLog.p( "SnedFile: result SUCCESS " + result.getStatus().getStatusCode() );
				}else{
					GLog.p( "SnedFile: result ERROR " + result.getStatus().getStatusCode() );
				}
				asset= null;
				small_asset= null;
			}
			binary= null;
			request= null;
		}

	}
*/

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

	public void	AppendSyncListBlockSEP_Info( GoogleApiClient mApiClient, Context context, FileInfo[] info_list )
	{
		PutDataMapRequest	request= null;

		int	file_count= info_list.length;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			FileInfo	info= info_list[fi];
			String	file_name= info.FileName;
			String	full_name= info.FullName;

			request= PutDataMapRequest.create( Command.STORAGE_IMAGE_PATH + full_name );
			DataMap	dmap= request.getDataMap();

			GLog.p( "Add storage : " + file_name + " (" + full_name + ")" );

			byte[]	binary= LoadFile( full_name );
			if( binary != null ){

				Asset	asset= null;
				{
					int	orgsize= binary.length;
					binary= GetSafeGuard( binary, SAFE_GUARD_LIMIT_SIZE, SAFE_GUARD_LIMIT_SIZE );
					GLog.p( "compressed size=" + binary.length + " <=" + orgsize );
					asset= Asset.createFromBytes( binary );
					dmap.putAsset( Command.DATA_KEY_ASSET, asset );
					dmap.putString( Command.DATA_KEY_FNAME, file_name );
					dmap.putLong( Command.DATA_KEY_DATE, info.Date );
				}

				byte[]	small_binary= GetSmallBitmap( binary, SMALL_ICON_SIZE, SMALL_ICON_SIZE );
				binary= null;
				Asset	small_asset= Asset.createFromBytes( small_binary );
				dmap.putAsset( Command.DATA_KEY_THUMB, small_asset );
				small_binary= null;

				DataApi.DataItemResult	result= Wearable.DataApi.putDataItem( mApiClient, request.asPutDataRequest() ).await();
				if( result.getStatus().isSuccess() ){
					GLog.p( "SnedFile: result SUCCESS " + result.getStatus().getStatusCode() );
				}else{
					GLog.p( "SnedFile: result ERROR " + result.getStatus().getStatusCode() );
				}
				asset= null;
				small_asset= null;
			}
			binary= null;
			request= null;
		}

	}


	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

/*
	private void	RemoveList( GoogleApiClient mApiClient, DataItem item, String[] name_list, Runnable event )
	{
		PutDataRequest	request= PutDataRequest.createFromDataItem( item );
		int	file_count= name_list.length;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			String	file_name= name_list[fi];

			GLog.p( "Remove Storage : " + file_name );
			request.removeAsset( file_name );
		}
		if( file_count != 0 ){
			final Runnable	event_= event;
			Wearable.DataApi.putDataItem( mApiClient, request )
				.setResultCallback( new ResultCallback<DataApi.DataItemResult>() {
					@Override
					public void onResult( DataApi.DataItemResult result ) {
						if( result.getStatus().isSuccess() ){
							GLog.p( "SendFile: result SUCCESS " + result.getStatus().getStatusCode()  );
							if( event_ != null ){
								event_.run();
							}
						}else{
							GLog.p( "SendFile: result ERROR " + result.getStatus().getStatusCode()  );
						}
					}
				} );
		}
	}

	public void	RemoveSyncList( GoogleApiClient mApiClient, String[] name_list, Runnable event )
	{
		final String[]			name_list_= name_list; 
		final GoogleApiClient	mApiClient_= mApiClient;
		final Runnable			event_= event;
		Wearable.DataApi.getDataItems( mApiClient )
			.setResultCallback( new ResultCallback<DataItemBuffer>() {
				@Override
				public void onResult( DataItemBuffer result ) {
					if( result.getStatus().isSuccess() ){
						for( DataItem item : result ){
							if( item.getUri().getPath().equals( Command.STORAGE_PATH ) ){
								RemoveList( mApiClient_, item, name_list_, event_ );
							}
						}
					}
				}
			} );
	}
*/

/*
	public void	RemoveSyncListSEP( GoogleApiClient mApiClient, String[] name_list, Runnable event )
	{
		final Runnable	event_= event;
		int	file_count= name_list.length;
		mRemoveCount= file_count;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			Uri	uri= new Uri.Builder().scheme( "wear" ).path( Command.STORAGE_IMAGE_PATH + name_list[fi] ).build();
			GLog.p( "remove " + uri.toString() );
			Wearable.DataApi.deleteDataItems( mApiClient, uri )
				.setResultCallback( new ResultCallback<DataApi.DeleteDataItemsResult>() {
					@Override
					public void onResult( DataApi.DeleteDataItemsResult result ) {
						if( result.getStatus().isSuccess() ){
						}
						mRemoveCount--;
						if( mRemoveCount <= 0 ){
							if( event_ != null ){
								event_.run();
							}
						}
						//result.release();
					}
				} );
		}
	}
*/


	public void	RemoveSyncListSEP_Info( GoogleApiClient mApiClient, FileInfo[] info_list, Runnable event )
	{
		final Runnable	event_= event;
		int	file_count= info_list.length;
		mRemoveCount= file_count;
		for( int fi= 0 ; fi< file_count ; fi++ ){
			Uri	uri= new Uri.Builder().scheme( "wear" ).path( Command.STORAGE_IMAGE_PATH + info_list[fi].FullName ).build();
			GLog.p( "remove " + uri.toString() );
			Wearable.DataApi.deleteDataItems( mApiClient, uri )
				.setResultCallback( new ResultCallback<DataApi.DeleteDataItemsResult>() {
					@Override
					public void onResult( DataApi.DeleteDataItemsResult result ) {
						if( result.getStatus().isSuccess() ){
						}
						mRemoveCount--;
						if( mRemoveCount <= 0 ){
							if( event_ != null ){
								event_.run();
							}
						}
						//result.release();
					}
				} );
		}
	}

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

	private void	sendMessage( NodeApi.GetConnectedNodesResult nodes, GoogleApiClient mApiClient, String command, byte[] data )
	{
		for( Node node : nodes.getNodes() ){
			GLog.p( " node=" + node.getId() );
			Wearable.MessageApi.sendMessage( mApiClient, node.getId(), command, data )
				.setResultCallback( new ResultCallback<MessageApi.SendMessageResult>() {
					@Override
					public void	onResult( MessageApi.SendMessageResult result )
					{
						if( result.getStatus().isSuccess() ){
							GLog.p( "SendMessage: result SUCCESS " + result.getStatus().getStatusCode()  );
						}else{
							GLog.p( "SendMessage: result ERROR " + result.getStatus().getStatusCode()  );
						}
						//result.release();
					}
				} );
		}
	}

	public void	SendMessage( GoogleApiClient mApiClient, String command, byte[] data )
	{
		GLog.p( "Send Message " + command );
		if( mApiClient == null || !mApiClient.isConnected() ){
			GLog.p( "Send Message no connection" );
			return;
		}
		final GoogleApiClient	mApiClient_= mApiClient;
		final String			command_= command;
		final byte[]			data_= data;
		Wearable.NodeApi.getConnectedNodes( mApiClient )
			.setResultCallback( new ResultCallback<NodeApi.GetConnectedNodesResult>() {
				@Override
				public void onResult( NodeApi.GetConnectedNodesResult result )
				{
					if( result.getStatus().isSuccess() ){
						GLog.p( "GetConnectedNodes: result SUCCESS " + result.getStatus().getStatusCode()  );
						sendMessage( result, mApiClient_, command_, data_ );
					}
					//result.release();
				}
			} );
	}

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

}

