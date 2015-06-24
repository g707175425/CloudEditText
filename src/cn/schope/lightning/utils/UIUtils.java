package com.jeremyfeinstein.slidingmenu.lib.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class UIUtils {
	private final static int UPPER_LEFT_X = 0;
	private final static int UPPER_LEFT_Y = 0;

	public static Drawable convertViewToDrawable(View view) {
		int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(spec, spec);
		view.layout(UPPER_LEFT_X, UPPER_LEFT_Y, view.getMeasuredWidth(), view.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		view.setDrawingCacheEnabled(true);
		Bitmap cacheBmp = view.getDrawingCache();
		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
		cacheBmp.recycle();
		view.destroyDrawingCache();
		return new BitmapDrawable(viewBmp);
	}

	/** dip转换px */
	public static int dip2px(Context context,int dip) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}
	
}


