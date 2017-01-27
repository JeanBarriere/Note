package fr.jean_barriere.note.listener;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;

import java.util.ArrayList;

import fr.jean_barriere.note.R;

/*
** Created by jean on 1/11/17.
*/

public class NotePagerListener implements MaterialViewPager.Listener {
    private static NotePagerListener self = null;
    private ArrayList<HeaderDesign> headers;

    private NotePagerListener() {
        headers = new ArrayList<>();
        headers.add(HeaderDesign.fromColorResAndUrl(R.color.black_semi_transparent, "http://wallpapercave.com/wp/aZlqiAT.png"));
//        headers.add(HeaderDesign.fromColorResAndUrl(R.color.blue, "http://cdn1.tnwcdn.com/wp-content/blogs.dir/1/files/2014/06/wallpaper_51.jpg"));
        headers.add(HeaderDesign.fromColorResAndUrl(R.color.black_semi_transparent, "http://www.droid-life.com/wp-content/uploads/2014/10/lollipop-wallpapers10.jpg"));
        headers.add(HeaderDesign.fromColorResAndUrl(R.color.black_semi_transparent, "http://www.tothemobile.com/wp-content/uploads/2014/07/original.jpg"));
    }

    public static NotePagerListener getInstance() {
        if (self == null)
            self = new NotePagerListener();
        return self;
    }

    @Override
    public HeaderDesign getHeaderDesign(int page) {
//        if (headers != null && headers.size() > 0)
//            return headers.get(page % headers.size());
        return HeaderDesign.fromColorResAndUrl(R.color.black_semi_transparent, "http://wallpapercave.com/wp/aZlqiAT.png");
//        return HeaderDesign.fromColorResAndUrl(R.color.half_black, "");
    }

    public void addHeader(int color, String ImageURL) {
        headers.add(HeaderDesign.fromColorResAndUrl(color, ImageURL));
    }
}
