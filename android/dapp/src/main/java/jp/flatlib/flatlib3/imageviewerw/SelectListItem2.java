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
import	android.widget.AdapterView;
import	android.widget.Button;
import	android.widget.CheckBox;
import	android.widget.PopupMenu;
import	android.view.Menu;
import	android.content.res.Configuration;
import	android.content.Context;
import	android.content.Intent;
import	android.content.DialogInterface;
import	android.app.AlertDialog;
import	android.os.Environment;
import	android.os.Handler;
import	android.graphics.BitmapFactory;
import	android.graphics.Bitmap;
import	android.widget.ImageView;
import	android.os.ParcelFileDescriptor;
import	android.content.Loader;
import	android.app.LoaderManager;

import	java.io.IOException;
import	java.io.File;
import	java.io.InputStream;
import	java.io.FileInputStream;
import	java.io.FileDescriptor;
import	java.util.ArrayList;


import	jp.flatlib.core.GLog;



class SelectListItem2 implements View.OnClickListener {

	private	FileInfo	Info;
	private boolean		Loading;
//	private boolean		Selected;
	private	CheckBox	checkBox;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	public SelectListItem2( FileInfo info )
	{
		Info= info;
		Loading= false;
	}
	public String	getTitle()
	{
		return	Info.FileName;
	}
	public String	getFilename()
	{
		return	Info.FileName;
	}
	public Bitmap	getBitmap()
	{
		return	Info.Thumbnail;
	}
	public void		setBitmap( Bitmap bitmap )
	{
		Info.Thumbnail= bitmap;
	}
	public long		getItemID()
	{
		return	Info.db_id;
	}

	public FileInfo	getFileInfo()
	{
		return	Info;
	}

	public long	getDate()
	{
		return	Info.Date;
	}

	public boolean	isSelected()
	{
		return	Info.Selected;
	}
	public void	setSelect( boolean select )
	{
		Info.Selected= select;
	}

	public void	setLoading( boolean loading )
	{
		Loading= loading;
	}
	public boolean	isLoading()
	{
		return	Loading;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public void	setCheckBox( CheckBox box )
	{
		checkBox= box;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	@Override
	public void	onClick( View v )
	{
		if( v == checkBox ){
			CheckBox	check= (CheckBox)v;
			Info.Selected= check.isChecked();
			return;
		}
		if( checkBox != null ){
			if( checkBox.isChecked() ){
				checkBox.setChecked( false );
			}else{
				checkBox.setChecked( true );
			}
			Info.Selected= checkBox.isChecked();
		}
	}
}

