// 2014/11/19 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:

package jp.flatlib.flatlib3.imageviewerw;


import	android.content.Context;
import	android.app.Fragment;
import	android.os.Bundle;
import	android.view.View;
import	android.view.LayoutInflater;
import	android.view.ViewGroup;
import	android.support.wearable.view.WatchViewStub;
//import	android.widget.Button;


import	jp.flatlib.core.GLog;


public class MenuFragment extends Fragment
					{

	private	TopActivity	mContext;

	public MenuFragment()
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
		View	view= inflater.inflate( R.layout.page_open_layout, view_group, false );

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

	public void	onCreateStage2( WatchViewStub stub )
	{
		stub.findViewById( R.id.refresh_button ).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick( View button )
				{
					mContext.RefreshDataList();
				}
			} );

	}

	//------------------------------------------------------------------------
	//------------------------------------------------------------------------

}

