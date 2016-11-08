// vim:ts=4 sw=4 noet:

package	jp.flatlib.flatlib3.wearplayer1;


import	android.app.Activity;
import	android.os.Bundle;
import	android.content.Intent;
import	android.widget.Button;
import	android.view.View;
import	android.media.AudioManager;
import	android.content.Context;

import	jp.flatlib.core.GLog;


public class MainActivity extends Activity {

	private	AudioManager	iAudioManager= null;


    @Override
    public void	onCreate( Bundle savedInctancedState )
    {
        super.onCreate( savedInctancedState );
        setContentView( R.layout.activity_main );

		iAudioManager= (AudioManager)getSystemService( Context.AUDIO_SERVICE );

		((Button)findViewById( R.id.play_button )).setOnClickListener(
			new View.OnClickListener(){
			@Override
			public void onClick( View button )
			{
				playCommand();
			}
		} );
		((Button)findViewById( R.id.stop_button )).setOnClickListener(
			new View.OnClickListener(){
			@Override
			public void onClick( View button )
			{
				stopCommand();
				stopServer();
			}
		} );
		((Button)findViewById( R.id.pause_button )).setOnClickListener(
			new View.OnClickListener(){
			@Override
			public void onClick( View button )
			{
				pauseCommand();
			}
		} );
		((Button)findViewById( R.id.next_button )).setOnClickListener(
			new View.OnClickListener(){
			@Override
			public void onClick( View button )
			{
				nextCommand();
			}
		} );
		((Button)findViewById( R.id.prev_button )).setOnClickListener(
			new View.OnClickListener(){
			@Override
			public void onClick( View button )
			{
				prevCommand();
			}
		} );
		((Button)findViewById( R.id.volm_button )).setOnClickListener(
			new View.OnClickListener(){
			@Override
			public void onClick( View button )
			{
				Volume( false );
			}
		} );
		((Button)findViewById( R.id.volp_button )).setOnClickListener(
			new View.OnClickListener(){
			@Override
			public void onClick( View button )
			{
				Volume( true );
			}
		} );

    }


	public void playCommand()
	{
		GLog.p( "client Play" );
		Intent	intent= new Intent( this, PlayerService.class );
		intent.putExtra( "Command", "Play" );
		startService( intent );
	}

	public void stopCommand()
	{
		GLog.p( "client Stop" );
		Intent	intent= new Intent( this, PlayerService.class );
		intent.putExtra( "Command", "Stop" );
		startService( intent );
	}

	public void pauseCommand()
	{
		GLog.p( "client Pause" );
		Intent	intent= new Intent( this, PlayerService.class );
		intent.putExtra( "Command", "Pause" );
		startService( intent );
	}


	public void nextCommand()
	{
		GLog.p( "client Next" );
		Intent	intent= new Intent( this, PlayerService.class );
		intent.putExtra( "Command", "Next" );
		startService( intent );
	}

	public void prevCommand()
	{
		GLog.p( "client Prev" );
		Intent	intent= new Intent( this, PlayerService.class );
		intent.putExtra( "Command", "Prev" );
		startService( intent );
	}



	public void stopServer()
	{
		GLog.p( "client Stop Server" );
		Intent	intent= new Intent( this, PlayerService.class );
		stopService( intent );
	}

	public int GetVolume()
	{
		return	iAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
	}

	public void Volume( boolean updown )
	{
		GLog.p( "Volume" );
		int	volume= iAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
		int	max_volume= iAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
		if( updown ){
			volume+= 1;
		}else{
			volume-= 1;
		}
		if( volume > max_volume ){
			volume= max_volume;
		}else if( volume < 0 ){
			volume= 0;
		}
		GLog.p( "cur=" + volume );
		GLog.p( "max=" + max_volume );
		iAudioManager.setStreamVolume( AudioManager.STREAM_MUSIC, volume, 0 );
	}
}
