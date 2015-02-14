package com.kodak.gallery.camera;

import static com.kodak.gallery.camera.Util.Assert;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

import com.kodak.gallery.camera.gallery.IImage;
import com.kodak.gallery.camera.gallery.IImageList;

class ImageBlockManagerL {
    @SuppressWarnings("unused")
    private static final String TAG = "CustomImageBlockManager";

    // Number of rows we want to cache.
    // Assume there are 6 columns per page, this caches 4 pages.
    private static final int CACHE_ROWS = 24;

    // mCache maps from row number to the ImageBlock.
    private final HashMap<Integer, ImageBlock> mCache;

    // These are parameters set in the constructor.
    private final Handler mHandler;
    private final Runnable mRedrawCallback;  // Called after a row is loaded,
                                             // so GridViewSpecial can draw
                                             // again using the new images.
    private final IImageList mImageList;
    private final ImageLoader mLoader;
    private final GridViewL.DrawAdapter mDrawAdapter;
    private final GridViewL.LayoutSpec mSpec;
    private final int mColumns;  // Columns per row.
    private final int mBlockWidth;  // The width of an ImageBlock.
    private final Bitmap mOutline;  // The outline bitmap put on top of each
                                    // image.
    private final int mCount;  // Cache mImageList.getCount().
    private final int mRows;  // Cache (mCount + mColumns - 1) / mColumns
    private final int mBlockHeight;  // The height of an ImageBlock.

    // Visible row range: [mStartRow, mEndRow). Set by setVisibleRows().
    private int mStartRow = 0;
    private int mEndRow = 0;

    // Visible row range: [mStartCol, mEndCol). Set by setVisibleColumns().
    private int mStartCol = 0;
    private int mEndCol = 0;
    ImageBlockManagerL(Handler handler, Runnable redrawCallback,
            IImageList imageList, ImageLoader loader,
            GridViewL.DrawAdapter adapter,
            GridViewL.LayoutSpec spec,
            int rows, int blockHeight, Bitmap outline) {
    	Log.e("ImageBlockManagerL", "ImageBlockManagerL");
        mHandler = handler;
        mRedrawCallback = redrawCallback;
        mImageList = imageList;
        mLoader = loader;
        mDrawAdapter = adapter;
        mSpec = spec;
        mCount = imageList.getCount();
        mRows = rows;
        mColumns = (mCount + mRows - 1) / mRows;
        mBlockWidth = mSpec.mCellSpacing + mSpec.mCellWidth; 
        mOutline = outline;
        mBlockHeight = blockHeight;//mSpec.mCellSpacing + mSpec.mCellHeight;    
        mCache = new HashMap<Integer, ImageBlock>();
        mPendingRequest = 0;
        initGraphics();
    }

    // Set the window of visible rows. Once set we will start to load them as
    // soon as possible (if they are not already in cache).

    public void setVisibleColumns(int startCol, int endCol) {
    	Log.e("setVisibleColumns", "ImageBlockManagerL");
        if (startCol != mStartCol || endCol != mEndCol) {
            mStartCol = startCol;
            mEndCol = endCol;
            startLoading();
        }
    }

    int mPendingRequest;  // Number of pending requests (sent to ImageLoader).
    // We want to keep enough requests in ImageLoader's queue, but not too
    // many.
    static final int REQUESTS_LOW = 3;
    static final int REQUESTS_HIGH = 6;

    // After clear requests currently in queue, start loading the thumbnails.
    // We need to clear the queue first because the proper order of loading
    // may have changed (because the visible region changed, or some images
    // have been invalidated).
    private void startLoading() {
    	Log.e("startLoading", "ImageBlockManagerL");
        clearLoaderQueue();
        continueLoading();
    }

    private void clearLoaderQueue() {
    	Log.e("clearLoaderQueue", "ImageBlockManagerL");
        int[] tags = mLoader.clearQueue();
        for (int pos : tags) {
            int col = pos / mRows;
            int row = pos - col * mRows;
            ImageBlock blk = mCache.get(col);
            Assert(blk != null);  // We won't reuse the block if it has pending
                                  // requests. See getEmptyBlock().
            blk.cancelRequest(row);
        }
    }

    // Scan the cache and send requests to ImageLoader if needed.
    private void continueLoading() {
    	Log.e("continueLoading", "ImageBlockManagerL");
        // Check if we still have enough requests in the queue.
        if (mPendingRequest >= REQUESTS_LOW) return;

        // Scan the visible columns.
        for (int i = mStartCol; i < mEndCol; i++) {
            if (scanOne(i)) return;
        }

        int range = (CACHE_ROWS - (mEndCol - mStartCol)) / 2;
        // Scan other columns.
        // d is the distance between the column and visible region.
        for (int d = 1; d <= range; d++) {
            int after = mEndCol - 1 + d;
            int before = mStartCol - d;
            if (after >= mColumns && before < 0) {
                break;  // Nothing more to scan.
            }
            if (after < mColumns && scanOne(after)) return;
            if (before >= 0 && scanOne(before)) return;
        }
    }

    // Returns true if we can stop scanning.
    private boolean scanOne(int i) {
    	Log.e("scanOne", "ImageBlockManagerL");
        mPendingRequest += tryToLoad(i);
        return mPendingRequest >= REQUESTS_HIGH;
    }

    // Returns number of requests we issued for this row.
    private int tryToLoad(int col) {
    	Log.e("tryToLoad", "ImageBlockManagerL");
        Assert(col >= 0 && col < mColumns);
        ImageBlock blk = mCache.get(col);
        if (blk == null) {
            // Find an empty block
            blk = getEmptyBlock();
            //blk.setRow(row);
            blk.setCol(col);
            blk.invalidate();
            mCache.put(col, blk);
        }
        return blk.loadImages();
    }

    // Get an empty block for the cache.
    private ImageBlock getEmptyBlock() {
    	Log.e("getEmptyBlock", "ImageBlockManagerL");
        // See if we can allocate a new block.
        if (mCache.size() < CACHE_ROWS) {
            return new ImageBlock();
        }
        // Reclaim the old block with largest distance from the visible region.
        int bestDistance = -1;
        int bestIndex = -1;
        for (int index : mCache.keySet()) {
            // Make sure we don't reclaim a block which still has pending
            // request.
            if (mCache.get(index).hasPendingRequests()) {
                continue;
            }
            int dist = 0;
            if (index >= mEndCol) {
                dist = index - mEndCol + 1;
            } else if (index < mStartCol) {
                dist = mStartCol - index;
            } else {
                // Inside the visible region.
                continue;
            }
            if (dist > bestDistance) {
                bestDistance = dist;
                bestIndex = index;
            }
        }
        return mCache.remove(bestIndex);
    }

    public void invalidateImage(int index) {
    	Log.e("invalidateImage", "ImageBlockManagerL");
    	int col = index / mRows;
        int row = index - col * mRows;
        ImageBlock blk = mCache.get(col);
        if (blk == null) return;
        if ((blk.mCompletedMask & (1 << row)) != 0) {
            blk.mCompletedMask &= ~(1 << row);
        }
        startLoading();
    }

    // After calling recycle(), the instance should not be used anymore.
    public void recycle() {
    	Log.e("recycle", "ImageBlockManagerL");
        for (ImageBlock blk : mCache.values()) {
            blk.recycle();
        }
        mCache.clear();
        mEmptyBitmap.recycle();
    }

    // Draw the images to the given canvas.
    public void doDraw(Canvas canvas, int thisWidth, int thisHeight,
            int scrollPos) {
    	Log.e("doDraw", "ImageBlockManagerL");
        final int height = mBlockHeight;
        final int width = mBlockWidth;

        // Note that currentBlock could be negative.
        int currentBlock = (scrollPos < 0)
        ? ((scrollPos - width + 1) / width)
        : (scrollPos / width);

        while (true) {
            final int xPos = currentBlock * width;
            if (xPos >= scrollPos + thisWidth) {
                break;
            }

            ImageBlock blk = mCache.get(currentBlock);
            if (blk != null) {
                blk.doDraw(canvas, xPos, 0);
            } else {
                drawEmptyBlock(canvas, xPos, 0, currentBlock);
            }

            currentBlock += 1;
        }
    }

    // Return number of columns in the given row. (This could be less than
    // mColumns for the last row).

    private int numRows(int col) {
    	Log.e("numRows", "ImageBlockManagerL");
        return Math.min(mRows, mCount - col * mRows);
    }

    // Draw a block which has not been loaded.
    private void drawEmptyBlock(Canvas canvas, int xPos, int yPos, int col) {
    	Log.e("drawEmptyBlock", "ImageBlockManagerL");
        // Draw the background.
        canvas.drawRect(xPos, yPos, xPos + mBlockWidth, yPos + mBlockHeight,
                mBackgroundPaint);

        // Draw the empty images.
        int x = xPos + mSpec.mLeftEdgePadding;
        int y = yPos + mSpec.mCellSpacing;
        int rows = numRows(col);

        for (int i = 0; i < rows; i++)
        {
            canvas.drawBitmap(mEmptyBitmap, x, y, null);
            y += (mSpec.mCellHeight + mSpec.mCellSpacing);
        }
    }

    // mEmptyBitmap is what we draw if we the wanted block hasn't been loaded.
    // (If the user scrolls too fast). It is a gray image with normal outline.
    // mBackgroundPaint is used to draw the (black) background outside
    // mEmptyBitmap.
    Paint mBackgroundPaint;
    private Bitmap mEmptyBitmap;

    private void initGraphics() {
    	Log.e("initGraphics", "ImageBlockManagerL");
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(0xFF000000);  // black
        mEmptyBitmap = Bitmap.createBitmap(mSpec.mCellWidth, mSpec.mCellHeight,
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(mEmptyBitmap);
        canvas.drawRGB(0xDD, 0xDD, 0xDD);
        canvas.drawBitmap(mOutline, 0, 0, null);
    }

    // ImageBlock stores bitmap for one row. The loaded thumbnail images are
    // drawn to mBitmap. mBitmap is later used in onDraw() of GridViewSpecial.
    private class ImageBlock {
        private Bitmap mBitmap;
        private final Canvas mCanvas;

        // Columns which have been requested to the loader
        private int mRequestedMask;

        // Columns which have been completed from the loader
        private int mCompletedMask;

        // The Column number this block represents.        
        private int mCol;
        public ImageBlock() {
        	Log.e("ImageBlock", "ImageBlock");
            mBitmap = Bitmap.createBitmap(mBlockWidth, mBlockHeight,
                    Bitmap.Config.RGB_565);
            mCanvas = new Canvas(mBitmap);
            mCol = -1;
        }

        public void setCol(int col)
        {
        	Log.e("setCol", "ImageBlock");
        	mCol = col;
        }
        public void invalidate() {
        	Log.e("invalidate", "ImageBlock");
            // We do not change mRequestedMask or do cancelAllRequests()
            // because the data coming from pending requests are valid. (We only
            // invalidate data which has been drawn to the bitmap).
            mCompletedMask = 0;
        }

        // After recycle, the ImageBlock instance should not be accessed.
        public void recycle() {
        	Log.e("recycle", "ImageBlock");
        	cancelAllRequests();
            mBitmap.recycle();
            mBitmap = null;
        }

        private boolean isVisible() {
        	Log.e("isVisible", "ImageBlock");
            return mCol >= mStartCol && mCol < mEndCol;
        }

        // Returns number of requests submitted to ImageLoader.
        public int loadImages() {
        	Log.e("loadImages", "ImageBlock");
            Assert(mCol != -1);

            int rows = numRows(mCol);

            // Calculate what we need.
            int needMask = ((1 << rows) - 1)
                    & ~(mCompletedMask | mRequestedMask);

            if (needMask == 0) {
                return 0;
            }

            int retVal = 0;
            int base = mCol * mRows;


            for (int r = 0; r < rows; r++)
            {
                if ((needMask & (1 << r)) == 0)
                {
                    continue;
                }
                int pos = base + r;

                final IImage image = mImageList.getImageAt(pos);
                if (image != null)
                {
                    // This callback is passed to ImageLoader. It will invoke
                    // loadImageDone() in the main thread. We limit the callback
                    // thread to be in this very short function. All other
                    // processing is done in the main thread.
                    final int rowFinal = r;
                    ImageLoader.LoadedCallback cb =
                            new ImageLoader.LoadedCallback() {
                                    public void run(final Bitmap b) {
                                        mHandler.post(new Runnable() {
                                            public void run() {
                                                loadImageDone(image, b,
                                                        rowFinal);
                                            }
                                        });
                                    }
                                };
                    // Load Image
                    mLoader.getBitmap(image, cb, pos);
                    mRequestedMask |= (1 << r);
                    retVal += 1;
                }
            }

            return retVal;
        }

        // Whether this block has pending requests.
        public boolean hasPendingRequests() {
        	Log.e("hasPendingRequests", "ImageBlock");
            return mRequestedMask != 0;
        }

        // Called when an image is loaded.
        private void loadImageDone(IImage image, Bitmap b,
                int row) {
        	Log.e("loadImageDone", "ImageBlock");
            if (mBitmap == null) return;  // This block has been recycled.

            int spacing = mSpec.mCellSpacing;
            int leftSpacing = mSpec.mLeftEdgePadding;

            final int xPos = leftSpacing;
            final int yPos = spacing  + (row * (mSpec.mCellHeight + spacing));
            drawBitmap(image, b, xPos, yPos);

            if (b != null) {
                b.recycle();
            }

            int mask = (1 << row);
            Assert((mCompletedMask & mask) == 0);
            Assert((mRequestedMask & mask) != 0);
            mRequestedMask &= ~mask;
            mCompletedMask |= mask;
            mPendingRequest--;

            if (isVisible()) {
                mRedrawCallback.run();
            }

            // Kick start next block loading.
            continueLoading();
        }

        // Draw the loaded bitmap to the block bitmap.
        private void drawBitmap(
                IImage image, Bitmap b, int xPos, int yPos) {
        	Log.e("drawBitmap", "ImageBlock");
            mDrawAdapter.drawImage(mCanvas, image, b, xPos, yPos,
                    mSpec.mCellWidth, mSpec.mCellHeight);
            //HCL 
            //DONE TO decrease the cellpadding
            //mCanvas.drawBitmap(mOutline, xPos, yPos, null);
        }

        // Draw the block bitmap to the specified canvas.
        public void doDraw(Canvas canvas, int xPos, int yPos) {
        	Log.e("doDraw", "ImageBlock");
           
        	int rows = numRows(mCol);

            if (rows == mRows) {
                canvas.drawBitmap(mBitmap, xPos, yPos, null);
            } else {

                // This must be the last row -- we draw only part of the block.
                // Draw the background.
                canvas.drawRect(xPos, yPos, xPos + mBlockWidth,
                        yPos + mBlockHeight, mBackgroundPaint);
                // Draw part of the block.
                int w = mSpec.mCellSpacing
                        + rows * (mSpec.mCellHeight + mSpec.mCellSpacing);
                Rect srcRect = new Rect(0, 0, mBlockWidth, w);
                Rect dstRect = new Rect(srcRect);
                dstRect.offset(xPos, yPos);
                canvas.drawBitmap(mBitmap, srcRect, dstRect, null);
            }

            // Draw the part which has not been loaded.
            int isEmpty = ((1 << rows) - 1) & ~mCompletedMask;

            if (isEmpty != 0) {
                int x = xPos + mSpec.mLeftEdgePadding;
                int y = yPos + mSpec.mCellSpacing;

                for (int i = 0; i < rows; i++)
                {
                    if ((isEmpty & (1 << i)) != 0)
                    {
                        canvas.drawBitmap(mEmptyBitmap, x, y, null);
                    }
                    //x += (mSpec.mCellWidth + mSpec.mCellSpacing);
                    y += (mSpec.mCellHeight + mSpec.mCellSpacing);
                }
            }
        }

        // Mark a request as cancelled. The request has already been removed
        // from the queue of ImageLoader, so we only need to mark the fact.
        public void cancelRequest(int row) {
        	Log.e("cancelRequest", "ImageBlock");
            int mask = (1 << row);
            Assert((mRequestedMask & mask) != 0);
            mRequestedMask &= ~mask;
            mPendingRequest--;
        }

        // Try to cancel all pending requests for this block. After this
        // completes there could still be requests not cancelled (because it is
        // already in progress). We deal with that situation by setting mBitmap
        // to null in recycle() and check this in loadImageDone().
        private void cancelAllRequests() {
        	Log.e("cancelAllRequests", "ImageBlock");
            for (int i = 0; i < mColumns; i++) {
                int mask = (1 << i);
                if ((mRequestedMask & mask) != 0) {
                    int pos = (mCol * mRows) + i;
                    if (mLoader.cancel(mImageList.getImageAt(pos))) {
                        mRequestedMask &= ~mask;
                        mPendingRequest--;
                    }
                }
            }
        }
    }
}
