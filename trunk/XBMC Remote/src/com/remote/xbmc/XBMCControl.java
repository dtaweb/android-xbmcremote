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

package com.remote.xbmc;

import java.net.Inet4Address;
import org.xbmc.eventclient.XBMCClient;
import org.xbmc.httpapi.XBMC;
import android.app.Activity;

public class XBMCControl {
	private static XBMC httpApiInstance;
	private static XBMCClient eventClientInstance;
	public static final String PREFS_NAME = "XBMCRemotePrefsFile";
	
	static public XBMC getHttpApiInstance(Activity activity) {
		if (httpApiInstance == null)
		{
			String host = activity.getSharedPreferences(XBMCControl.PREFS_NAME, 0).getString("host", "");
			int port = activity.getSharedPreferences(XBMCControl.PREFS_NAME, 0).getInt("httpPort", 80);
			httpApiInstance = new XBMC(host, port);
		}
		return httpApiInstance;
	}
	
	static public XBMCClient getEventClientInstance(Activity activity) {
		if (eventClientInstance == null)
		{
			String host = activity.getSharedPreferences(XBMCControl.PREFS_NAME, 0).getString("host", "");
			int port = activity.getSharedPreferences(XBMCControl.PREFS_NAME, 0).getInt("eventClientPort", 9777);
			try {
				eventClientInstance = new XBMCClient(Inet4Address.getByName(host), port, "Android XBMC Remote");
			} catch (Exception e) {
				return null;
			}
		}
		return eventClientInstance;
	}
}
