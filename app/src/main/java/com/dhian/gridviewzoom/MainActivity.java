package com.dhian.gridviewzoom;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.*;
import android.graphics.*;
import android.widget.TableRow.*;
import android.content.*;
import android.net.*;
import android.util.*;
import android.nfc.*;
import android.view.*;
import android.renderscript.*;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private GridView gv;
	private Animator mCurrentAnimator;
	private int mShortAnimationDuration;
	private int j = 0;

	private final GestureDetector detector = new GestureDetector(new SwipeGestureDetector());
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	// Create Array thumbs resource id's:
	private int thumb[] ;
		
    private String user[] = {
		"User 1",
		"User 2",
		"User 3",
		"User 4",
	    "User 5",
		"User 6",
		"User 7",
		"User 8",
		"User 9",
		"User 10",
		"User 11",
		"User 12",
		"User 13",
		"User 14",
		"User 15",
		"User 16"};
		
    private String linkFb[] = {
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/1779075638",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
		"https://m.facebook.com/",
	};
	
	String linkIg[] = {
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		null,
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		null,
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		null,
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		"https://m.instagram.com/",
		"https://m.instagram.com/"
	};
	
	private String linkTw[] = {
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		null,
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		null,
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		null,
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		"http://m.twitter.com/",
		null,
	};

	private ImageView expandedImageView,iconFb,iconIg,iconTw;
	private TextView expandedText;
	private LinearLayout expandedContainer,fb,ig,tw;
	private View tView;
	private RelativeLayout mask;
	View blur;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getID("main","layout"));

		// Initialize the variables:
		gv = (GridView) findViewById(getID("creditsGridView","id"));
		expandedContainer = (LinearLayout)findViewById(getID("expanded_grid","id"));
		expandedImageView = (ImageView) findViewById(getID("expanded_image","id"));
		expandedText = (TextView)findViewById(getID("expanded_text","id"));
		fb = (LinearLayout)findViewById(getID("credits_fb","id"));
		ig = (LinearLayout)findViewById(getID("credits_ig","id"));
		tw = (LinearLayout)findViewById(getID("credits_tw","id"));
		iconFb = (ImageView)findViewById(getID("icon_fb","id"));
		iconIg = (ImageView)findViewById(getID("icon_ig","id"));
		iconTw = (ImageView)findViewById(getID("icon_tw","id"));
		blur = findViewById(getID("blur","id"));
		mask = (RelativeLayout)findViewById(getID("mask","id"));
		
		thumb = new int[] {
			getID("user_1","drawable"),
			getID("user_2","drawable"),
			getID("user_3","drawable"),
			getID("user_4","drawable"),
			getID("user_5","drawable"),
			getID("user_6","drawable"),
			getID("user_7","drawable"),
			getID("user_8","drawable"),
			getID("user_9","drawable"),
			getID("user_10","drawable"),
			getID("user_11","drawable"),
			getID("user_12","drawable"),
			getID("user_13","drawable"),
			getID("user_14","drawable"),
			getID("user_15","drawable"),
			getID("user_16","drawable")};
		
		mRS = RenderScript.create(this);
        scriptIntrinsicBlur = ScriptIntrinsicBlur.create(mRS, Element.RGBA_8888(mRS));
        scriptIntrinsicBlur.setRadius(25);
		// Set an Adapter to the ListView
		gv.setAdapter(new ImageAdapter(this));

		// Set on item click listener to the ListView
		gv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int pos,
										long id) {

					// Display the zoomed in image in full screen
					tView = v;
					j = pos;
					zoomImageFromThumb(v, thumb[pos],user[pos],linkFb[pos],linkIg[pos],linkTw[pos]);

				}
			});

		// Set the Animation time form the android defaults
		mShortAnimationDuration = getResources().getInteger(
			android.R.integer.config_shortAnimTime);

	}
	
	RenderScript mRS;
    ScriptIntrinsicBlur scriptIntrinsicBlur;
    Allocation allocOriginalScreenshot, allocBlurred;
    TextureView textureViewBlurred;
	
	@Override
    public void onBackPressed() {
		
		int stateExpanded = expandedContainer.getVisibility();
		
		if(stateExpanded == 0){
			closeZoom(tView);
			
		}else{
			
			super.onBackPressed();
			return;
		}

	}


	

	// Create an Adapter Class extending the BaseAdapter
	class ImageAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater;

		public ImageAdapter(MainActivity activity) {
			// TODO Auto-generated constructor stub
			layoutInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// Set the count value to the total number of items in the Array
			return thumb.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Inflate the item layout and set the views
			View listItem = convertView;
			int pos = position;
			if (listItem == null) {
				listItem = layoutInflater.inflate(getID("grid_item_credits","layout"), null);
			}

			// Initialize the views in the layout
			ImageView iv = (ImageView) listItem.findViewById(getID("thumb","id"));
			TextView tv = (TextView) listItem.findViewById(getID("grid_text","id"));

			// Set the views in the layout
			iv.setBackgroundResource(thumb[pos]);
			tv.setText(user[pos]);

			return listItem;
		}

	}



	private void zoomImageFromThumb(final View thumbView, int imageResId,String textId,final String linkfb,final String linkig,final String linktw) {
		// If there's an animation in progress, cancel it immediately and
		// proceed with this one.
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		// Load the high-resolution "zoomed-in" image.
		
		
		if(linkfb == null){
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				0.0f
			);
			fb.setLayoutParams(param);
			fb.setVisibility(View.INVISIBLE);
			
		}else{
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				1.0f
			);
			fb.setLayoutParams(param);
			fb.setVisibility(View.VISIBLE);
		}
		if(linkig == null){
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				0.0f
			);
			ig.setLayoutParams(param);
			ig.setVisibility(View.INVISIBLE);
		}else{
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				1.0f
			);
			ig.setLayoutParams(param);
			ig.setVisibility(View.VISIBLE);
		}
		if(linktw == null){
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				0.0f
			);
			tw.setLayoutParams(param);
			tw.setVisibility(View.INVISIBLE);
		}else{
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				1.0f
			);
			tw.setLayoutParams(param);
			tw.setVisibility(View.VISIBLE);
		}
		
		iconFb.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(linkfb));
				startActivity(browserIntent);
			}
		});
		
		iconIg.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(linkig));
					startActivity(browserIntent);
				}
			});
			
		iconTw.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(linktw));
					startActivity(browserIntent);
				}
			});
		
		expandedImageView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (detector.onTouchEvent(event))
						return true;
					else
						return false;
				}
			});

		expandedImageView.setImageResource(imageResId);
		expandedText.setText(textId);

		
		// Calculate the starting and ending bounds for the zoomed-in image.
		// This step
		// involves lots of math. Yay, math.
		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		// The start bounds are the global visible rectangle of the thumbnail,
		// and the
		// final bounds are the global visible rectangle of the container view.
		// Also
		// set the container view's offset as the origin for the bounds, since
		// that's
		// the origin for the positioning animation properties (X, Y).
		thumbView.getGlobalVisibleRect(startBounds);
		findViewById(getID("container_credits","id")).getGlobalVisibleRect(finalBounds,
														  globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		// Adjust the start bounds to be the same aspect ratio as the final
		// bounds using the
		// "center crop" technique. This prevents undesirable stretching during
		// the animation.
		// Also calculate the start scaling factor (the end scaling factor is
		// always 1.0).
		float startScale;
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
			.width() / startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		// Hide the thumbnail and show the zoomed-in view. When the animation
		// begins,
		// it will position the zoomed-in view in the place of the thumbnail.
		thumbView.setAlpha(0f);
		expandedImageView.setVisibility(View.VISIBLE);
        expandedContainer.setVisibility(View.VISIBLE);
		expandedText.setVisibility(View.VISIBLE);
		mask.setBackgroundColor(0x50000000);
		mask.setVisibility(View.VISIBLE);
		blurView(blur);
		
		// Set the pivot point for SCALE_X and SCALE_Y transformations to the
		// top-left corner of
		// the zoomed-in view (the default is the center of the view).
		//expandedImageView.setPivotX(0f);
		expandedContainer.setPivotX(0f);
		//expandedImageView.setPivotY(0f);
		expandedContainer.setPivotY(0f);

		// Construct and run the parallel animation of the four translation and
		// scale properties
		// (X, Y, SCALE_X, and SCALE_Y).
		AnimatorSet set = new AnimatorSet();
		set.play(
			ObjectAnimator.ofFloat(expandedContainer, View.X,
								   startBounds.left, finalBounds.left))
			.with(ObjectAnimator.ofFloat(expandedContainer, View.Y,
										 startBounds.top, finalBounds.top))
			.with(ObjectAnimator.ofFloat(expandedContainer, View.SCALE_X,
										 startScale, 1f))
			.with(ObjectAnimator.ofFloat(expandedContainer, View.SCALE_Y,
										 startScale, 1f));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mCurrentAnimator = null;
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					mCurrentAnimator = null;
				}
			});
		set.start();
		mCurrentAnimator = set;
		
		expandedContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					return;
					
				}
		});

		// Upon clicking the zoomed-in image, it should zoom back down to the
		// original bounds
		// and show the thumbnail instead of the expanded image.
		final float startScaleFinal = startScale;
		expandedImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mCurrentAnimator != null) {
						mCurrentAnimator.cancel();
					}

					// Animate the four positioning/sizing properties in parallel,
					// back to their
					// original values.
					AnimatorSet set = new AnimatorSet();
					set.play(
						ObjectAnimator.ofFloat(expandedContainer, View.X,
											   startBounds.left))
						.with(ObjectAnimator.ofFloat(expandedContainer, View.Y,
													 startBounds.top))
						.with(ObjectAnimator.ofFloat(expandedContainer,
													 View.SCALE_X, startScaleFinal))
						.with(ObjectAnimator.ofFloat(expandedContainer,
													 View.SCALE_Y, startScaleFinal));
					set.setDuration(mShortAnimationDuration);
					set.setInterpolator(new DecelerateInterpolator());
					set.addListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								thumbView.setAlpha(1f);
								mask.setBackgroundColor(Color.TRANSPARENT);
								expandedContainer.setVisibility(View.GONE);
								mask.setVisibility(View.GONE);
								unblurView(blur);
								mCurrentAnimator = null;
							}

							@Override
							public void onAnimationCancel(Animator animation) {
								thumbView.setAlpha(1f);
								mask.setBackgroundColor(Color.TRANSPARENT);
								expandedContainer.setVisibility(View.GONE);
								mask.setVisibility(View.GONE);
								unblurView(blur);
								mCurrentAnimator = null;
							}
						});
					set.start();
					mCurrentAnimator = set;
				}
			});
	}
	
	private void closeZoom(final View thumbView){
		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		// The start bounds are the global visible rectangle of the thumbnail,
		// and the
		// final bounds are the global visible rectangle of the container view.
		// Also
		// set the container view's offset as the origin for the bounds, since
		// that's
		// the origin for the positioning animation properties (X, Y).
		thumbView.getGlobalVisibleRect(startBounds);
		findViewById(getID("container_credits","id")).getGlobalVisibleRect(finalBounds,
																  globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);
		
		float startScale;
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
			.width() / startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}
		
		final float startScaleFinal = startScale;
		
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		// Animate the four positioning/sizing properties in parallel,
		// back to their
		// original values.
		AnimatorSet set = new AnimatorSet();
		set.play(
			ObjectAnimator.ofFloat(expandedContainer, View.X,
								   startBounds.left))
			.with(ObjectAnimator.ofFloat(expandedContainer, View.Y,
										 startBounds.top))
			.with(ObjectAnimator.ofFloat(expandedContainer,
										 View.SCALE_X, startScaleFinal))
			.with(ObjectAnimator.ofFloat(expandedContainer,
										 View.SCALE_Y, startScaleFinal));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					thumbView.setAlpha(1f);
					mask.setBackgroundColor(Color.TRANSPARENT);
					expandedContainer.setVisibility(View.GONE);
					mask.setVisibility(View.GONE);
					unblurView(blur);
					
					mCurrentAnimator = null;
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					thumbView.setAlpha(1f);
				    mask.setBackgroundColor(Color.TRANSPARENT);
					expandedContainer.setVisibility(View.GONE);
					mask.setVisibility(View.GONE);
					unblurView(blur);
					mCurrentAnimator = null;
				}
			});
		set.start();
		mCurrentAnimator = set;
	
	}


	private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

					if(thumb.length>j)
                    {
                        j++;

                        if(j < thumb.length)
                        {                           
							expandedImageView.setImageResource(thumb[j]);
							expandedText.setText(user[j]);
                            return true;
                        }
                        else
                        {
                            j = 0;
                            expandedImageView.setImageResource(thumb[j]);
							expandedText.setText(user[j]);
                            return true;
                        }

                    }
                }
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {


                    if(j>0)
                    {
                        j--;
                        expandedImageView.setImageResource(thumb[j]);
						expandedText.setText(user[j]);
                        return true;

                    }
                    else
                    {
						j = thumb.length-1;
						expandedImageView.setImageResource(thumb[j]);
						expandedText.setText(user[j]);
                        return true;
                    }
                }
            }
			catch (Exception e) {
				e.printStackTrace();
			}
			return false;
        }
    }
	
	Bitmap getViewScreenshot(View v) {
        v.setDrawingCacheEnabled(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);

        return b;
    }

    /*
	 This function replaces a view with another, mimicking the original one's layout parameters,
	 to look the same.
	 Also, it saves the new view object in the original one's tag field, to be able to restore
	 the original view later.
     */
    void replaceView(View originalView, View newView) {
        originalView.setTag(newView);

        newView.setLayoutParams(new FrameLayout.LayoutParams(originalView.getLayoutParams()));

        ViewGroup parent = (ViewGroup) originalView.getParent();
        int index = parent.indexOfChild(originalView);
        parent.removeView(originalView);

        parent.addView(newView, index);
    }

    /*
	 Restores the original view. Checks if there is a valid view inside the tag field, and restores
	 it.
     */
    void restoreView(View v) {
        View otherView = (View) v.getTag();

        if (otherView != null && otherView.getParent() != null) {
            replaceView(otherView, v);
        } else if (v != null && v.getParent() != null) {
            replaceView(v, otherView);
        }
    }
	
	void blurView(View v) {
        // First of all, take a screen of the current view
        Bitmap viewScreenshot = getViewScreenshot(v);

        // Defines allocations where to store the screenshot and the temporary blurred image
        if (allocOriginalScreenshot != null && (allocOriginalScreenshot.getType().getX() != viewScreenshot.getWidth() ||
			allocOriginalScreenshot.getType().getY() != viewScreenshot.getHeight())) {

            // Current allocations have wrong sizes!
            allocOriginalScreenshot.destroy();
            allocBlurred.destroy();

            textureViewBlurred = null;
            allocOriginalScreenshot = null;
            allocBlurred = null;
        }

        if (allocOriginalScreenshot == null) {
            allocOriginalScreenshot = Allocation.createFromBitmap(mRS, viewScreenshot);
            // Creates an allocation where to store the blur results
            allocBlurred = Allocation.createTyped(mRS, allocOriginalScreenshot.getType(), Allocation.USAGE_SCRIPT | Allocation.USAGE_IO_OUTPUT);

            // Then, replace the current view with a TextureView.
            // This lets RenderScript display the blurred screenshot with ease, without killing
            // the memory (in case of large screenshots)
            textureViewBlurred = new TextureView(this);
            textureViewBlurred.setOpaque(false);
            textureViewBlurred.setSurfaceTextureListener(surfaceTextureListener);

        } else {
            // Just copy the new view screenshot
            allocOriginalScreenshot.copyFrom(viewScreenshot);
        }

        replaceView(v, textureViewBlurred);

    }

    void unblurView(View v) {
        restoreView(v);
    }

    // Performs the actual blur calculation
    void executeBlur() {
        Log.d(TAG, "Executing blur");

        scriptIntrinsicBlur.setInput(allocOriginalScreenshot);
        scriptIntrinsicBlur.forEach(allocBlurred);

        allocBlurred.ioSend();
    }

    /*
	 This is the TextureView listener, and it uses the following "trick":
	 every time the TextureView gets added to a ViewGroup, the surface is re-instantiates. This means
	 that the onSurfaceTextureAvailable callback gets called every time. We can bound the blur calc
	 process directly to this callback.
     */
    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            // Once the surface is ready, execute the blur
            allocBlurred.setSurface(new Surface(surfaceTexture));

            executeBlur();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
	
	public int getID(String name, String Type) {
		return getBaseContext().getResources().getIdentifier(name, Type, getBaseContext().getPackageName());
	}

}

