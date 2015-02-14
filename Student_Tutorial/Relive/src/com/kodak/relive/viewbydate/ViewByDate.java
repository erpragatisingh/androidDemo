package com.kodak.relive.viewbydate;


import com.kodak.gallery.camera.BitmapManager;
import com.kodak.gallery.camera.ImageManager;
import com.kodak.relive.R;

public class ViewByDate extends Activity {
		private static final int YEAR_TEXT_SIZE = 60;
		private static final int LEFT_PADDING_FOR_RECENT_IMAGE = 20;
		private static final int MONTH_GRID_COLS = 4;
		private static final int MONTH_GRID_WIDTH = 260;
		private static final int MONTH_GRID_HEIGHT = 194;
		private static final int RECENT_HEIGHT = MONTH_GRID_HEIGHT; // recent pic and month grid height is same
		private static final int RECENT_WIDTH = RECENT_HEIGHT; // picture is square

    	private static Handler mHandler;  // handler for the main thread
		static int numberOfPages;
		static int id = 1;
		int arridx = 0;
		private GridView mGridView;
		private TextView mYearTextView;
		private ImageView mYearImageView;
		private ImageView mHomeicon;
		private TreeSet<Integer> mNumYear;
		private Vector<GridView> mGridVector;
		private Vector<ImageView> mImageVector;
		private Vector<TextView> mTextViewVector;
		private RelativeLayout mRelativeLayout;
		private int mCurrentShowingPage;
		private String[] mMonthText;
		ContentResolver mCR;
		Thread mWorkerThread;
		ArrayList<YearData> mYearData = new ArrayList<YearData>();
		ViewFlipper mViewFlipper;
		Button mPageBar[];
		private ImageAdapter mAdapter; 
		//private static boolean isOnStopCalled = false;
		private GestureDetector mGestureDetector;

		/** Called when the activity is first created. */
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			//dvs start logging
			//Debug.startMethodTracing("profiling");
			
			if (mHandler == null) {
				 mHandler = new Handler();
			}
			mMonthText = getApplicationContext().getResources().getStringArray(R.array.viewbydate_monthName);
			//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,	WindowManager.LayoutParams.FLAG_FULLSCREEN);
			mCR = getContentResolver();
			mNumYear = ImageManager.getYears(mCR);
			numberOfPages = mNumYear.size();
						
			mTextViewVector = new Vector<TextView>(numberOfPages);	
			mImageVector = new Vector<ImageView>(numberOfPages);
			mGridVector = new Vector<GridView>(numberOfPages);
							
			switch (numberOfPages){
			case 0:
				setContentView(R.layout.gallerypicker_no_images);
		    	RelativeLayout noImage = (RelativeLayout)findViewById(R.id.no_images);
		    	noImage.setVisibility(View.VISIBLE);
				break;
				
			case 1:
				setContentView(R.layout.viewbydate_wo_scrollbar);	
				/*mHomeicon = (ImageView) findViewById(R.id.HomeIcon);
				mHomeicon.setOnTouchListener(new OnTouchListener() {			
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						closeViewByDate();
						return true;
					}
				});*/
				break;
				
			case 2:
			case 3:
			case 4:
			case 5:
				setContentView(R.layout.viewbydate_pagingbar);	
				/*mHomeicon = (ImageView) findViewById(R.id.HomeIcon);
				mHomeicon.setOnTouchListener(new OnTouchListener() {			
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						closeViewByDate();
						return true;
					}
				});*/
				mPageBar = new Button[5];
				mViewFlipper = (ViewFlipper) findViewById(R.id.PageBarViewFlipper);
				mViewFlipper.setAnimateFirstView(true);
				mRelativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout01);
				
				for (int i = 1; i < numberOfPages; i++) {			
					mViewFlipper.addView(createPage(i));
				}			
				
				//Getting of page buttons
				mPageBar[0] = (Button) findViewById(R.id.page1);
				mPageBar[1] = (Button) findViewById(R.id.page2);
				mPageBar[2] = (Button) findViewById(R.id.page3);
				mPageBar[3] = (Button) findViewById(R.id.page4);
				mPageBar[4] = (Button) findViewById(R.id.page5);
				
				//Switch for handling the visibility of  paging bars 				
				switch(mNumYear.size()){		
				
				case 2: mPageBar[2].setVisibility(Button.GONE);
		                mPageBar[3].setVisibility(Button.GONE);
		                mPageBar[4].setVisibility(Button.GONE);
		                break;
				case 3: mPageBar[3].setVisibility(Button.GONE);
		        		mPageBar[4].setVisibility(Button.GONE);
		        		break;
				case 4: mPageBar[4].setVisibility(Button.GONE);
		        		break;
				case 5: 
		        		break;
					
				}
				
				mPageBar[0].setBackgroundColor(Color.WHITE);			
				setTouchListenerOnPageBar();
				//setGestureListener(findViewById(R.id.rootView));

				break;
				
			default:
				setContentView(R.layout.viewbydate_with_scrollbar);
				
				///////
				mHomeicon = (ImageView) findViewById(R.id.HomeIcon);
//				mHomeicon.setClickable(true);
//				mHomeicon.setOnClickListener(new OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						closeViewByDate();
//					}
//				});
//				mHomeicon.setOnTouchListener(new OnTouchListener() {
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						
//						closeViewByDate();
//						return true;
//					}
//				});
				/////////
				
				mRelativeLayout = (RelativeLayout)findViewById(R.id.RelativeLayout01); 
				mTextViewVector.add((TextView) findViewById(R.id.yearTextView));
				mImageVector.add((ImageView) findViewById(R.id.ImageView01));	
				mGridVector.add((GridView) findViewById(R.id.GridView01));			
				addDynamicViews();
				break;				
			}		
			
			if (numberOfPages > 0){
				// Set First Page Layout
				mGridView = (GridView) findViewById(R.id.GridView01);
				mYearTextView = (TextView) findViewById(R.id.yearTextView);
				mYearImageView = (ImageView) findViewById(R.id.ImageView01);
				monthGridViewListener(mGridView);
				mHomeicon = (ImageView) findViewById(R.id.HomeIcon);
				
				monthGridViewListener(mGridView);
				mHomeicon.setOnTouchListener(new OnTouchListener() {			
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						//Log.d("ViewByData.onTouchListener", "HomeButton Clicked");
						closeViewByDate();
						return true;
					}
				});
//dvsb
//				mHomeicon.setClickable(true);
//				mHomeicon.setOnClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						closeViewByDate();
//					}
//				});
//	          mHandler.post(new Runnable() {
//	        	  public void run() {
//	        		  updateItem(finalItem, index);
//	        	  }
//	          	});
//dvse			

			}

//			setupOnTouchListeners(findViewById(R.id.rootView));
		}
		
		
		public void setTouchListenerOnPageBar() {
			mCurrentShowingPage = 0;
			for (int i = 0; i < numberOfPages; i++) {
				final int  idx = i;
				mPageBar[i].setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						 if (event.getAction() == MotionEvent.ACTION_UP) {
							if (mCurrentShowingPage < idx) {
								mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.push_left_in));
								mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.push_left_out));
								mPageBar[idx].setBackgroundColor(Color.WHITE);
								mPageBar[mCurrentShowingPage].setBackgroundColor(Color.GRAY);
								mCurrentShowingPage = idx;
								mViewFlipper.setDisplayedChild(idx);
							} else if (mCurrentShowingPage > idx) {  
								mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.push_right_in));
								mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.push_right_out));
								mPageBar[idx].setBackgroundColor(Color.WHITE);
								mPageBar[mCurrentShowingPage].setBackgroundColor(Color.GRAY);
								mCurrentShowingPage = idx;
								mViewFlipper.setDisplayedChild(idx);
							} else {
								mViewFlipper.clearAnimation();
							}						
						}																		
						return true;
					}
				});
			}			
		}
		
		@Override
		public void onStop() {
			super.onStop();
		    
		     mGridView = null;
			 mYearTextView = null;
			 mYearImageView = null;
			 mHomeicon = null;
			 mNumYear = null;
			 mGridVector = null;
			 mImageVector = null;
			 mTextViewVector = null;
			 mRelativeLayout = null;
						
			 mMonthText = null;
			 mCR = null;
		    
			 mYearData =  null;
			 mViewFlipper = null;
			 mPageBar = null;
		 }
	    	    
		@Override
		public void onStart() {
			super.onStart();
			startWorker();
		}
		
		// This is used to stop the worker thread.
	    volatile boolean mAbort = false;
	    
		// Create the worker thread.
	    private void startWorker() {
	        mAbort = false;
	        mWorkerThread = new Thread("ViewByDate Worker") {
	            @Override
	            public void run() {
	                workerRun();
	            }
	        };
	        BitmapManager.instance().allowThreadDecoding(mWorkerThread);
	        mWorkerThread.start();
	    }

	    private void abortWorker() {
	        try {
	    	if (mWorkerThread != null) {
	            BitmapManager.instance().cancelThreadDecoding(mWorkerThread);
	            MediaStore.Images.Thumbnails.cancelThumbnailRequest(getContentResolver(), -1);
	            mAbort = true;
	            try {
	                mWorkerThread.join();
	            } catch (InterruptedException ex) {
	                Log.e("Thread aborted", "join interrupted");
	            }
	            if (mAdapter != null) {
	            	mAdapter.clear();
	            }
	            if (mWorkerThread != null) {
		            mWorkerThread.interrupt();
		            mWorkerThread = null;
	            }
	            // Remove all runnables in mHandler.
	            // (We assume that the "what" field in the messages are 0
	            // for runnables).
	            
	            mHandler.removeMessages(0);
	            //mAdapter.clear();
	            //mAdapter.updateDisplay();
	            //clearImageLists();
	        }
	        } catch (Exception e) {
	        	Log.i("View by date:  ", "Exception abortWorker()" + e);
			}
	    }
	    
	 // This is run in the worker thread.
	    private void workerRun() {
	    	ArrayList<YearData> yearData = new ArrayList<YearData>();
	        fillYearDataList(yearData);
	        if (mAbort) return;
	    }   

	 // This is run in the worker thread.
		private void fillYearDataList(ArrayList<YearData> yearData) {
			try {
		        int length = numberOfPages;
		        Iterator<Integer> yearPtr = mNumYear.iterator();
		        for (int i = 0; i < length; i++) {
		            YearData yearDataObject = new YearData();
		            yearDataObject.setYear(yearPtr.next());	
		            yearDataObject = getYearData(getContentResolver(), yearDataObject.getYear());
		            if (mAbort) {
		            	if (yearDataObject.mYearImage != null) {
		            		yearDataObject.mYearImage.recycle();
		            	}
		            	if (yearDataObject.mMonthImageList != null) {
		            		for (Bitmap item : yearDataObject.mMonthImageList) {
		            			if (item != null) {
		            				item.recycle();
		            			}
		            		}
		            	}
		            	return;
		            }
	
		            final YearData finalItem = yearDataObject;
		            final int index = i;
		            mHandler.post(new Runnable() {
		                        public void run() {
		                            updateItem(finalItem, index);
		                        }
		                    });
		        }
			} catch (Exception e) {
				Log.i("Exception::  ", "Exception is " + e);
			}
	    }
		
		// This is run in the main thread.
	    private void updateItem(YearData item, int index) {
	    	int page_no = index-1;
	    	try {
		    	if(numberOfPages > 5 ){
		    		page_no = index;
		    	}
		    	
		    	switch (index){
				case 0:
					mYearTextView.setText(String.valueOf(item.getYear()));
					mYearImageView.setImageBitmap(item.getYearImage());
					mAdapter = new ImageAdapter(item);
					mGridView.setAdapter(mAdapter);
					break;
				default:
					mTextViewVector.get(page_no).setText(String.valueOf(item.getYear()));
					mImageVector.get(page_no).setImageBitmap(item.getYearImage());
					mAdapter = new ImageAdapter(item);
					mGridVector.get(page_no).setAdapter(mAdapter);
					break;
		    	} 
	    	} catch (Exception e) {
				Log.i("Exception::  ", "Exception here: " + e);
			}
	    }
	    
		public YearData getYearData(ContentResolver cr, int year)
		{
			YearData yearData = new YearData();
			yearData.setYear(year);
			yearData.setYearImage(ImageManager.getYearImage(cr, year));
			Bitmap[] bitmapList = new Bitmap[12]; 
			for (int month = 0; month<=11; month++) {
				try {
					bitmapList[month] = ImageManager.getMonthImage(cr, year, month);
				} catch (Exception e)
				{
					//TODO: handle exception
				}
			}
			yearData.setMonthImageList(bitmapList);
			mYearData.add(yearData);
			return(yearData);
		}

		public void monthGridViewListener(GridView monthGridView) {
			monthGridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
			        //TODO: enable later
					//Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
					//Intent intent = new Intent(Intent.ACTION_VIEW,uri);
			        //intent.putExtra("windowTitle", "View By Date");
			        ////intent.putExtra("mediaTypes", getIncludeMediaTypes());		  
			        //startActivity(intent);
				}
			});
		}

		// Add dynamic
		public RelativeLayout createPage(int yearIdx) {
			RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());
			relativeLayout.setId(id++);
			LayoutParams relativeLayoutParameter = new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			relativeLayout.setLayoutParams(relativeLayoutParameter);
			
			FrameLayout yearFrameLayout = new FrameLayout(this);
			yearFrameLayout.setId(id++);
			
			ImageView imageView = new ImageView(this);
			mImageVector.add(imageView);
			imageView.setId(id++);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			RelativeLayout.LayoutParams lpImage2 =  new RelativeLayout.LayoutParams(RECENT_WIDTH, RECENT_HEIGHT);
			lpImage2.addRule(RelativeLayout.CENTER_VERTICAL);
			yearFrameLayout.addView(imageView, lpImage2);

			// add year field for page 2
			TextView year = new TextView(this);
			year.setId(id++);
			year.setTextSize(YEAR_TEXT_SIZE);
			year.setGravity(Gravity.RIGHT);
			mTextViewVector.add(year);
			
			FrameLayout.LayoutParams yearlp = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);			
			yearlp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
			yearlp.bottomMargin = 0;
			
			yearFrameLayout.addView(year, yearlp);
			yearFrameLayout.setPadding(LEFT_PADDING_FOR_RECENT_IMAGE, 0, 0, 0);

			RelativeLayout.LayoutParams lpFrame = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);	
			lpFrame.addRule(RelativeLayout.CENTER_VERTICAL);
			//dvs lpFrame.leftMargin = 10;
			relativeLayout.addView(yearFrameLayout,lpFrame);
			
			GridView monthGridView = new GridView(this);
			monthGridView.setId(id++);			
			monthGridViewListener(monthGridView);
			mGridVector.add(monthGridView);
			monthGridView.setNumColumns(MONTH_GRID_COLS);
			monthGridView.setVerticalScrollBarEnabled(false);
			monthGridView.setSelector(R.drawable.viewbydate_selector);
			// TODO: set the dpi for this layout
			RelativeLayout.LayoutParams gridLayoutParam = 
				new RelativeLayout.LayoutParams(MONTH_GRID_WIDTH, MONTH_GRID_HEIGHT);
			gridLayoutParam.addRule(RelativeLayout.RIGHT_OF, yearFrameLayout.getId());
			gridLayoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
			relativeLayout.addView(monthGridView,gridLayoutParam);

				return relativeLayout;
		}

		// Add dynamic views to the relative layout
		public void addDynamicViews() {
			int gridVectorIndex = 0;
			//int imageVectorIndex = 0;

			for (int idx = 0; idx < numberOfPages-1; idx++) {

				FrameLayout yearFrameLayout = new FrameLayout(this);
				yearFrameLayout.setId(id++);
				
				ImageView imageView = new ImageView(this);
				mImageVector.add(imageView);
				imageView.setId(id++);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				RelativeLayout.LayoutParams lpImage2 =  new RelativeLayout.LayoutParams(RECENT_WIDTH, RECENT_HEIGHT);
				lpImage2.addRule(RelativeLayout.CENTER_VERTICAL);
				yearFrameLayout.addView(imageView, lpImage2);

				// add year field for page 2
				TextView year = new TextView(this);
				year.setId(id++);
				year.setTextSize(YEAR_TEXT_SIZE);
				mTextViewVector.add(year);
				
				FrameLayout.LayoutParams yearlp = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.WRAP_CONTENT,
						FrameLayout.LayoutParams.WRAP_CONTENT);
				yearlp.bottomMargin = 0;
				
				yearlp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
				yearFrameLayout.addView(year, yearlp);
				yearFrameLayout.setPadding(LEFT_PADDING_FOR_RECENT_IMAGE, 0, 0, 0);

				RelativeLayout.LayoutParams lpFrame = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				lpFrame.addRule(RelativeLayout.CENTER_VERTICAL);
				lpFrame.addRule(RelativeLayout.RIGHT_OF, mGridVector.elementAt(gridVectorIndex).getId());

				mRelativeLayout.addView(yearFrameLayout,lpFrame);
				
				// add the grid view for second page
				GridView monthGridView = new GridView(this);
				monthGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
				monthGridView.setVerticalScrollBarEnabled(false);	
				monthGridViewListener(monthGridView);
				monthGridView.setSelector(R.drawable.viewbydate_selector);
				mGridVector.add(monthGridView);
				mGridVector.elementAt(gridVectorIndex++);

				mGridVector.elementAt(gridVectorIndex).setId(id++);
				mGridVector.elementAt(gridVectorIndex).setNumColumns(MONTH_GRID_COLS);
				
				RelativeLayout.LayoutParams grid2lp = 
					new RelativeLayout.LayoutParams(MONTH_GRID_WIDTH, MONTH_GRID_HEIGHT);
				grid2lp.addRule(RelativeLayout.RIGHT_OF, yearFrameLayout.getId());
				grid2lp.addRule(RelativeLayout.CENTER_VERTICAL);
				
				mRelativeLayout.addView(mGridVector.elementAt(gridVectorIndex),
						grid2lp);
			}
		}
		
		class YearData {	    
		    public int mYear;
		    public Bitmap mYearImage;
		    public Bitmap[] mMonthImageList;

		    public YearData() {
		    }
		    
		    public int getYear() {
		    	return mYear;
		    }
		    
		    public Bitmap getYearImage() {
		    	return mYearImage;
		    }

		    public Bitmap[] getMonthImageList() {
		    	return mMonthImageList;
		    }

			public void setYear(int year) {
		    	mYear = year;
		    }

			public void setYearImage(Bitmap yearImage) {
				mYearImage = yearImage;
		    }
			
			public void setMonthImageList(Bitmap[] monthImageList) {
				mMonthImageList = monthImageList;
		    }
			
			public YearData(int year, Bitmap yearImage, Bitmap[] monthImageList) {
		    	mYear = year;
		    	mYearImage = yearImage;
		    	mMonthImageList = monthImageList;
		    }
		}

		private void setGestureListener(View rootView) {
	        mGestureDetector = new GestureDetector(this, new ViewByDateGestureListener());
	         OnTouchListener rootListener = new OnTouchListener() {
	            public boolean onTouch(View v, MotionEvent event) {
	                mGestureDetector.onTouchEvent(event);
	                // We do not use the return value of
	                // mGestureDetector.onTouchEvent because we will not receive
	                // the "up" event if we return false for the "down" event.
	                return true;
	            }
	        };
	        rootView.setOnTouchListener(rootListener);
	    }
		
		public class ViewByDateGestureListener extends SimpleOnGestureListener{
	    	private int swipe_Max_Distance = 350;
	    	
	    	@Override
	    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	    		final float xDistance = Math.abs(e1.getX() - e2.getX());
	    		final float yDistance = Math.abs(e1.getY() - e2.getY());

	    		if (xDistance > this.swipe_Max_Distance || yDistance > this.swipe_Max_Distance) {
	    			return false;
	    		}
	    		velocityX = Math.abs(velocityX);
	    		velocityY = Math.abs(velocityY);
	    		boolean result = false;
	   		
	    		Log.i("Touch event::  ", "Touch:::::::::::::::;"+mCurrentShowingPage);
	    		if (e1.getX() > e2.getX()) {
	    			
	    			if (mCurrentShowingPage < numberOfPages-1) {
	    			mPageBar[mCurrentShowingPage+1].setBackgroundColor(Color.WHITE);
	    			mPageBar[mCurrentShowingPage].setBackgroundColor(Color.GRAY);
	    			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in));
					mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_out));
	    			mCurrentShowingPage++;
	    			mViewFlipper.showPrevious();
	    	}
	    		} else {
	    			if (mCurrentShowingPage > 0) {
		    			mPageBar[mCurrentShowingPage-1].setBackgroundColor(Color.WHITE);
		    			mPageBar[mCurrentShowingPage].setBackgroundColor(Color.GRAY);
		    			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_in));
						mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_right_out));				
		    			mCurrentShowingPage--;
		    			mViewFlipper.showNext();
	    			}
	    				
	    		}
	    		return result;
	    	}
	    }
		
		
		
		public class ImageAdapter extends BaseAdapter {
			private LayoutInflater mInflater;
			private ViewByDate.YearData mYearDataObject;		
			
			public ImageAdapter(YearData yearData) {
		        mInflater = LayoutInflater.from(getApplicationContext());
		        mYearDataObject = yearData;
			}
			
			@Override
			public int getCount() {
				if (mMonthText != null) {
					return mMonthText.length;
		        } else {
					return 0;
				}
			}

			public void clear() {
		    	if (mMonthText != null) {
		        	mMonthText = null;
		        }
		    }
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
					ViewHolder holder = new ViewHolder();		    
				   		        		
				    try {
						if(convertView == null) {
							convertView = mInflater.inflate(R.layout.viewbydate_month_text, null);
							holder.icon = (ImageView)convertView.findViewById(R.id.monthImage);
							holder.text = (TextView)convertView.findViewById(R.id.monthText);
							holder.text.setText(mMonthText[position]);
							Bitmap monthImage = mYearDataObject.getMonthImageList()[position];
							if(monthImage != null) {
								holder.icon.setImageBitmap(monthImage);
								convertView.setClickable(false);
							} else {
								convertView.setClickable(true);
							}
							convertView.setPadding(1, 1, 1, 1);
							convertView.setTag(holder);
						}
				    } catch (Exception e) {
				    	Log.e("imageAdapter.getView", "e is " + e);
					}
					return convertView; 				
			}			

			private class ViewHolder {
		        TextView text;
		        ImageView icon;
		    }
		}

		public void closeViewByDate() {
        	abortWorker();
        	finish();
			//dvs start logging
			//Debug.stopMethodTracing();
		}

		@Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        	closeViewByDate();
	        	return true;
	        }
	    	return false;
	    }
}