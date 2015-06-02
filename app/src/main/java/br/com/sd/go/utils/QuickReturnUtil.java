package br.com.sd.go.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.Dictionary;
import java.util.Hashtable;

import br.com.sd.go.R;

import static android.widget.LinearLayout.LayoutParams;

public class QuickReturnUtil {

    public static final int REFRESH_ITEM = 0;
    public static final int MAP_ITEM = 1;
    public static final int INFO_ITEM = 2;
    public static final int APPS_ITEM = 3;

    public static Dictionary<Integer, View> getOptionsMenu(Context context) {
        Dictionary<Integer, View> items = new Hashtable<>();
        items.put(REFRESH_ITEM, setUpItemByType(context, REFRESH_ITEM));
        items.put(MAP_ITEM, setUpItemByType(context, MAP_ITEM));
        items.put(INFO_ITEM, setUpItemByType(context, INFO_ITEM));
        items.put(APPS_ITEM, setUpItemByType(context, APPS_ITEM));
        return items;
    }

    private static View setUpItemByType(final Context context, int typeItem) {
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        View view = View.inflate(context, R.layout.content_floating_menu, null);
        int drawableId;
        switch (typeItem) {
            case REFRESH_ITEM:
                drawableId = R.drawable.ic_notification_sync;
                break;
            case INFO_ITEM:
                drawableId = R.drawable.ic_action_info;
                break;
            case MAP_ITEM:
                drawableId = R.drawable.ic_maps_pin_drop_gray;
                break;
            case APPS_ITEM:
                drawableId = R.drawable.ic_navigation_apps;
                break;
            default:
                drawableId = R.drawable.ic_notification_sync;
                break;
        }
        ((ImageView) view.findViewById(R.id.icon)).setImageResource(drawableId);
        view.setLayoutParams(layoutParams);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onMenuTouched(context, v, event);
            }
        });
        return view;
    }

    private static boolean onMenuTouched(Context context, View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundColor(
                    context.getResources().getColor(R.color.footer_list_content_pressed));
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundColor(
                    context.getResources().getColor(R.color.footer_list_content));
        }
        return false;
    }
}