package com.kodak.relive.main;


import com.kodak.relive.R;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private Cursor mCursor;
	private LayoutInflater mInflater;
	
	public ImageAdapter(Context context, Cursor cur) {
        mContext = context;
        mCursor = cur;
        mInflater = LayoutInflater.from(context);
    }

	@Override
	public int getCount() {
		return mCursor.getCount();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = new ViewHolder();		    
	    mCursor.moveToPosition(position);
	    try {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.main_gridview_frame,
						null);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				mCursor.moveToPosition(position);
				int imageId = mCursor.getInt(0);
				holder.icon.setImageBitmap(MediaStore.Images.Thumbnails.
	    				getThumbnail(MainScreen.mCr,imageId, MediaStore.Images.Thumbnails.MINI_KIND, null));
//				if (holder.icon.getDrawingCache() == null) {
//					holder.icon.setImageBitmap(MediaStore.Video.Thumbnails.
//		    				getThumbnail(MainScreen.mCr,imageId, MediaStore.Video.Thumbnails.MINI_KIND, null));
//				}
				switch (position) {
				case 0:
					convertView.setPadding(0, 0, 1, 1);
					break;
				case 1:
					convertView.setPadding(1, 0, 0, 1);
					break;
				case 2:
					convertView.setPadding(0, 1, 1, 0);
					break;
				case 3:
					convertView.setPadding(1, 1, 0, 0);
					break;
				}
				convertView.setFocusable(false);
				convertView.setTag(holder);

			}
		} catch (Exception e) {
			Log.d("Exception Found::", "" + e.getMessage());
		}
		return convertView;
	}
	
	class ViewHolder {
        ImageView icon;
    }

}
