package com.mingwei.floatlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/**
 * �Զ����������layout������������΢�������ţ�������Ч��
 * 
 * @author mingwei
 * 
 */
public class FloatLayout extends LinearLayout {

	private RelativeLayout mHeaderLayout;
	private LinearLayout mFloatLayout;
	private ViewPager mContent;

	private int mHeaderHeight;
	private boolean isHeaderHidden;
	private ViewGroup mInnerScrollview;

	private OverScroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchSlop;
	private int mMaximumVelocity, mMinimumVelocity;

	private float mLastY;
	private boolean isDragging;
	private boolean isMove = false;

	public FloatLayout(Context context) {
		this(context, null);
	}

	public FloatLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mScroller = new OverScroller(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
		mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mHeaderLayout = (RelativeLayout) findViewById(R.id.float_layout_top);
		mFloatLayout = (LinearLayout) findViewById(R.id.float_layout_float);
		mContent = (ViewPager) findViewById(R.id.float_layout_content);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		ViewGroup.LayoutParams layoutParams = mContent.getLayoutParams();
		layoutParams.height = getMeasuredHeight() - mFloatLayout.getMeasuredHeight();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mHeaderHeight = mHeaderLayout.getMeasuredHeight();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			float moveY = y - mLastY;
			getCurrentScrollView();
			if (mInnerScrollview instanceof ScrollView) {
				if (mInnerScrollview.getScrollY() == 0 && isHeaderHidden && moveY > 0 && !isMove) {
					isMove = true;
					return dispatchInnerChild(ev);
				}
			} else if (mInnerScrollview instanceof ListView) {
				ListView listView = (ListView) mInnerScrollview;
				View viewItem = listView.getChildAt(listView.getFirstVisiblePosition());
				if (viewItem != null && viewItem.getTop() == 0 && isHeaderHidden && moveY > 0 && !isMove) {
					isMove = true;
					return dispatchInnerChild(ev);
				}
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean dispatchInnerChild(MotionEvent ev) {
		ev.setAction(MotionEvent.ACTION_CANCEL);
		MotionEvent newMotionEvent = MotionEvent.obtain(ev);
		dispatchTouchEvent(ev);
		newMotionEvent.setAction(MotionEvent.ACTION_DOWN);
		return dispatchTouchEvent(newMotionEvent);
	}

	/**
	 * �¼����أ�������ʲôʱ��Ӧ�û����Ǹ����ֵ�����
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			float moveY = y - mLastY;
			getCurrentScrollView();
			if (Math.abs(moveY) > mTouchSlop) {
				isDragging = true;
				if (mInnerScrollview instanceof ScrollView) {
					if (!isHeaderHidden || (mInnerScrollview.getScrollY() == 0 && isHeaderHidden && moveY > 0)) {
						initVelocityTracker();
						mVelocityTracker.addMovement(ev);
						mLastY = y;
						return true;
					}
				} else if (mInnerScrollview instanceof ListView) {
					ListView listView = (ListView) mInnerScrollview;
					View viewItem = listView.getChildAt(listView.getFirstVisiblePosition());
					if (!isHeaderHidden || (viewItem != null && viewItem.getTop() == 0 && moveY > 0)) {
						initVelocityTracker();
						mVelocityTracker.addMovement(ev);
						mLastY = y;
						return true;
					}
				}
			}

		case MotionEvent.ACTION_CANCEL:

		case MotionEvent.ACTION_UP:
			isDragging = false;
			recycleVelocityTracker();
			break;
		default:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		initVelocityTracker();
		mVelocityTracker.addMovement(event);
		int action = event.getAction();
		float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastY = y;
			return true;
		case MotionEvent.ACTION_MOVE:
			float moveY = y - mLastY;
			if (!isDragging && Math.abs(moveY) > mTouchSlop) {
				isDragging = true;
			}
			if (isDragging) {
				scrollBy(0, (int) -moveY);
			}
			mLastY = y;
			break;
		case MotionEvent.ACTION_CANCEL:
			isDragging = false;
			recycleVelocityTracker();
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_UP:
			isDragging = false;
			mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			int velocity = (int) mVelocityTracker.getYVelocity();
			if (Math.abs(velocity) > mMinimumVelocity) {
				fling(-velocity);
			}
			recycleVelocityTracker();
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * ��дscrollTo,���������ڹ����Ĺ����в����� ������Χ.
	 * 
	 * y<0,��Header��ȫ��ʾ�ڸ�����ʱ�Ͳ�������Header�ܼ�������.
	 * 
	 * y>mHeaderHeight,��Header������ȫ�������ؼ�ʱ��y�ܵ�������ֵ���Ǿ���Header�ĸ߶�.
	 * 
	 * y!=getScrollY(),���ø����scrollTo,��y�����仯ʱ�����ø���scrollTo����.
	 */
	@Override
	public void scrollTo(int x, int y) {
		y = (y < 0) ? 0 : y;
		y = (y > mHeaderHeight) ? mHeaderHeight : y;
		if (y != getScrollY()) {
			super.scrollTo(x, y);
		}
		isHeaderHidden = getScrollY() == mHeaderHeight;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			invalidate();
		}
	}

	/**
	 * ��������ʱ�ɿ���ָ�����velocity�Զ�������ָ��λ��
	 * 
	 * @param velocityY
	 *            �ɿ�ʱ���ٶȣ�OverScroll��������Ǽ���Ҫ����Զ
	 */
	public void fling(int velocityY) {
		mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mHeaderHeight);
		invalidate();
	}

	/**
	 * ���ݵ�ǰ��View�������¼��ַ�,��������������ScrollView������ListViewʱ
	 */
	private void getCurrentScrollView() {
		int cuttentItem = mContent.getCurrentItem();
		PagerAdapter pagerAdapter = mContent.getAdapter();
		if (pagerAdapter instanceof FragmentPagerAdapter) {
			FragmentPagerAdapter adapter = (FragmentPagerAdapter) pagerAdapter;
			Fragment fragment = adapter.getItem(cuttentItem);
			mInnerScrollview = (ViewGroup) fragment.getView().findViewById(R.id.float_layout_inner_view);
		} else if (pagerAdapter instanceof FragmentStatePagerAdapter) {
			FragmentStatePagerAdapter adapter = (FragmentStatePagerAdapter) pagerAdapter;
			Fragment fragment = adapter.getItem(cuttentItem);
			mInnerScrollview = (ViewGroup) fragment.getView().findViewById(R.id.float_layout_inner_view);
		}
	}

	/**
	 * ��ʼ��VelocityTracker
	 */
	private void initVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	/**
	 * ����VelocityTracker
	 */
	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

}
