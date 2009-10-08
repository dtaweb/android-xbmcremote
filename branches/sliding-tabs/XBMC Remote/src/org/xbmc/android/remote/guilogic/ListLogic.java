/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.remote.guilogic;

import java.io.Serializable;

import org.xbmc.android.remote.R;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ListLogic implements Serializable {
	
	public static final String EXTRA_LIST_LOGIC = "ListLogic"; 
	public static final String EXTRA_ALBUM = "album"; 
	public static final String EXTRA_ARTIST = "artist";
	public static final String EXTRA_SHARE_TYPE = "shareType"; 
	public static final String EXTRA_PATH = "path"; 
	
	protected ListView mList;
	protected Activity mActivity;
	
	private TextView mTitleView;
	private boolean isCreated = false;
	
	
	public void onCreate(Activity activity, ListView list) {
		mList = list;
		mActivity = activity;
		isCreated = true;
	}
	
	public abstract void onContextItemSelected(MenuItem item);
	
	public abstract void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
	
	public void findTitleView(View parent) {
		mTitleView = (TextView)parent.findViewById(R.id.titlebar_text);	
	}
	
	protected void setTitle(String title) {
		if (mTitleView != null) {
			mTitleView.setText(title);
		}
	}
	
	protected boolean isCreated() {
		return isCreated;
	}
	


	private static final long serialVersionUID = 2903701184005613570L;
}
