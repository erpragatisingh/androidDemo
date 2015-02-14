package com.example.homescreenwidget;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.RemoteViews;

public class MainActivity extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for(int i=0;i<appWidgetIds.length;i++)
		{
			int appWidgetId=appWidgetIds[i];
			
			Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://erpragtai.webs.com"));
			PendingIntent pending =PendingIntent.getActivity(context, 0, intent, 0);
			RemoteViews views =new RemoteViews(R.id.imageButton1,pending);
			AppWidgetManager.updateAppWidget(appWidgetId,views);
			
			
		}
	}
	
	 

}
