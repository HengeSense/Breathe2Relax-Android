package org.t2health.breathe2relax;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

public class MaskView extends ScrollView implements Observer {
	private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private final Rect mRectSrc = new Rect();
	private final Rect mRectDst = new Rect();
	private Bitmap mMaskBitmap;
	private float mAspectQuotient;
	private ZoomState mState;
	

	public MaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setBodyImage();
		
		setFocusable( true );
	}

	public MaskView( Context context ){
		super( context );
		
		setFocusable( true );
	}

	public MaskView( Context context, AttributeSet attributeSet, int stuff ){
		super(context, attributeSet, stuff);
		
		setFocusable( true );
	}

	private void setBodyImage() {
		try {
			mMaskBitmap = B2R_Utility.getReducedBitmap(getResources(), BODY_PARTS.MASK.getBitmapOrder(), BODY_PARTS.MASK.getBitmapInt());

			setSmoothScrollingEnabled(true);
			calculateAspectQuotient();

			invalidate();
		} catch (Exception ex) {
			Log.d("MaskView", "Exception", ex);
		}
	}

	public void setZoomState(ZoomState state) {
		if (mState != null) {
			mState.deleteObserver(this);
		}

		mState = state;
		mState.addObserver(this);

		invalidate();
	}

	private void calculateAspectQuotient() {
		if (mMaskBitmap != null) {
			mAspectQuotient = (((float)mMaskBitmap.getWidth()) / mMaskBitmap.getHeight()) / (((float)getWidth()) / getHeight());
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if ( mState != null) {
			final int viewWidth = getWidth();
			final int viewHeight = getHeight();
			final float panX = mState.getPanX();
			final float panY = mState.getPanY();
			if (mMaskBitmap != null) {
				final int bitmapWidth = mMaskBitmap.getWidth();
				final int bitmapHeight = mMaskBitmap.getHeight();
				final float zoomX = mState.getZoomX(mAspectQuotient) * viewWidth / bitmapWidth;
				final float zoomY = mState.getZoomY(mAspectQuotient) * viewHeight / bitmapHeight;

				// Setup source and destination rectangles
				mRectSrc.left = (int)(panX * bitmapWidth - viewWidth / (zoomX * 2));
				mRectSrc.top = (int)(panY * bitmapHeight - viewHeight / (zoomY * 2));
				mRectSrc.right = (int)(mRectSrc.left + viewWidth / zoomX);
				mRectSrc.bottom = (int)(mRectSrc.top + viewHeight / zoomY);
				mRectDst.left = getLeft();
				mRectDst.top = getTop();
				mRectDst.right = getRight();
				mRectDst.bottom = getBottom();

				// Adjust source rectangle so that it fits within the source image.
				if (mRectSrc.left < 0) {
					mRectDst.left += -mRectSrc.left * zoomX;
					mRectSrc.left = 0;
				}
				if (mRectSrc.right > bitmapWidth) {
					mRectDst.right -= (mRectSrc.right - bitmapWidth) * zoomX;
					mRectSrc.right = bitmapWidth;
				}
				if (mRectSrc.top < 0) {
					mRectDst.top += -mRectSrc.top * zoomY;
					mRectSrc.top = 0;
				}
				if (mRectSrc.bottom > bitmapHeight) {
					mRectDst.bottom -= (mRectSrc.bottom - bitmapHeight) * zoomY;
					mRectSrc.bottom = bitmapHeight;
				}

				mPaint.setAntiAlias(true);
				mPaint.setFilterBitmap(true);
				mPaint.setDither(true);

				mPaint.setAlpha(255);
				
				canvas.drawBitmap(mMaskBitmap, mRectSrc, mRectDst, mPaint);
			} 
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		calculateAspectQuotient();
	}

	// implements Observer
	public void update(Observable observable, Object data) {
		computeScroll();
		invalidate();
	}

}