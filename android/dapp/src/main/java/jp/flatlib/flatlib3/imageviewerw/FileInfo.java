// 2014/11/20 Hiroyuki Ogasawara
// vim:ts=4 sw=4 noet:


package	jp.flatlib.flatlib3.imageviewerw;

import	android.graphics.Bitmap;


public class FileInfo {
	public String	FileName;
	public String	FullName;
//	public int		Width;	
//	public int		Height;	
	public int		Index;
	public long		db_id;
	public long		Date;
	Bitmap			Thumbnail;
	public boolean	Selected;

}
