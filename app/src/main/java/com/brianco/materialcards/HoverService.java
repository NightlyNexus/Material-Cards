package com.brianco.materialcards;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;

import com.brianco.materialcards.model.PaletteColor;

public class HoverService extends Service {

    public static final String COLOR_VALUE_EXTRA = "COLOR_VALUE_EXTRA";
    public static final String COLOR_NAME_EXTRA = "COLOR_NAME_EXTRA";
    public static final String COLOR_BASE_NAME_EXTRA = "COLOR_BASE_NAME_EXTRA";
    public static final String COLOR_PARENT_NAME_EXTRA = "COLOR_PARENT_NAME_EXTRA";
    public static final String COLOR_PARENT_VALUE_EXTRA = "COLOR_PARENT_VALUE_EXTRA";

    private static final String COPY_FILTER = "com.brianco.materialcards.HoverService.COPY_FILTER";
    private static final String STOP_FILTER = "com.brianco.materialcards.HoverService.STOP_FILTER";
    private static final int NOTIFICATION_ID = 1;
    private static final int START_REQUEST_CODE = 2;
    private static final int COPY_REQUEST_CODE = 3;
    private static final int STOP_REQUEST_CODE = 4;
    private static final int LONG_PRESS_MILLIS = ViewConfiguration.getLongPressTimeout();

    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private View hoverView;
    private Drawable drawable;

    private boolean isPressed;
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        public void run() {
            checkLongPress();
        }
    };

    private final BroadcastReceiver copyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            PaletteColor.copyColorToClipboard(context,
                    intent.getStringExtra(COLOR_PARENT_NAME_EXTRA),
                    intent.getStringExtra(COLOR_BASE_NAME_EXTRA),
                    intent.getStringExtra(COLOR_NAME_EXTRA));
        }
    };

    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            context.stopService(new Intent(context, HoverService.class));
        }
    };

    private void checkLongPress() {
        if (isPressed) {
            final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(getResources().getInteger(R.integer.vibrate_long_press));
            stopSelf();
        } else {
            throw new RuntimeException("The handler should remove its callback" +
                    " when the view is not being pressed anymore.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        initHoverLocation();
        final int color = intent.getIntExtra(COLOR_VALUE_EXTRA, 0);
        final String colorName = intent.getStringExtra(COLOR_NAME_EXTRA);
        final String colorBaseName = intent.getStringExtra(COLOR_BASE_NAME_EXTRA);
        final String colorParentName = intent.getStringExtra(COLOR_PARENT_NAME_EXTRA);
        final int parentColor = intent.getIntExtra(COLOR_PARENT_VALUE_EXTRA, 0);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC);
        hoverView.setBackground(drawable);
        final Intent startIntent = new Intent(this, PaletteActivity.class);
        startIntent.setAction(PaletteActivity.ACTION_START_COLOR);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startIntent.putExtra(PaletteActivity.COLOR_SECTION_VALUE_EXTRA, parentColor);
        final PendingIntent startPendingIntent = PendingIntent.getActivity(this, START_REQUEST_CODE,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Intent copyIntent = new Intent(COPY_FILTER);
        copyIntent.putExtra(COLOR_NAME_EXTRA, colorName);
        copyIntent.putExtra(COLOR_BASE_NAME_EXTRA, colorBaseName);
        copyIntent.putExtra(COLOR_PARENT_NAME_EXTRA, colorParentName);
        final PendingIntent copyPendingIntent = PendingIntent.getBroadcast(this, COPY_REQUEST_CODE,
                copyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Intent stopIntent = new Intent(STOP_FILTER);
        final PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, STOP_REQUEST_CODE,
                stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.hover)
                .setLargeIcon(drawableToBitmap(drawable))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(colorName)
                .setLights(color, 500, 500)
                .setPriority(Notification.PRIORITY_MIN)
                .addAction(R.drawable.ic_action_copy_light,
                        getString(R.string.copy_color_description), copyPendingIntent)
                .addAction(R.drawable.ic_action_close, getString(R.string.close), stopPendingIntent)
                .setContentIntent(startPendingIntent);
        final Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final float MOVE_THRESHOLD = getResources().getDimension(R.dimen.hover_move_threshold);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        drawable = getResources().getDrawable(R.drawable.hover);

        hoverView = new ImageView(this);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        windowManager.addView(hoverView, params);

        initHoverLocation();

        hoverView.setOnTouchListener(new View.OnTouchListener() {
            private final WindowManager.LayoutParams paramsF = params;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ensureParams();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isPressed) {
                            isPressed = true;
                            handler.postDelayed(runnable, LONG_PRESS_MILLIS);
                        }
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isPressed) {
                            isPressed = false;
                            handler.removeCallbacks(runnable);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isPressed
                                && (Math.abs(initialTouchX - event.getRawX()) > MOVE_THRESHOLD
                                || Math.abs(initialTouchY - event.getRawY()) > MOVE_THRESHOLD)) {
                            isPressed = false;
                            handler.removeCallbacks(runnable);
                        }
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(hoverView, paramsF);
                        break;
                }
                ensureParams();
                return false;
            }
        });

        registerReceiver(copyReceiver, new IntentFilter(COPY_FILTER));
        registerReceiver(stopReceiver, new IntentFilter(STOP_FILTER));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hoverView != null) windowManager.removeView(hoverView);
        unregisterReceiver(copyReceiver);
        unregisterReceiver(stopReceiver);
    }

    private void initHoverLocation() {
        final Resources resources = getResources();
        final Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = (int) (size.x - resources.getDimension(R.dimen.hover_color)
                - resources.getDimension(R.dimen.hover_x_offset_from_right));
        params.y = (int) resources.getDimension(R.dimen.hover_y_offset_from_top);
        windowManager.updateViewLayout(hoverView, params);
    }

    private void ensureParams() {
        final Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        final int hoverSize = (int) getResources().getDimension(R.dimen.hover_color);
        final int maxX = size.x - hoverSize;
        final int maxY = size.y - hoverSize;
        if (params.x > maxX) {
            params.x = maxX;
        } else if (params.x < 0) {
            params.x = 0;
        }
        if (params.y > maxY) {
            params.y = maxY;
        } else if (params.y < 0) {
            params.y = 0;
        }
    }

    private static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
