package net.tirgan.mex.utilities;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesUtil {

    public final static List<Integer> MEX_PLACES_TYPES = new ArrayList<Integer>() {{
        add(Place.TYPE_BAKERY);
        add(Place.TYPE_BAR);
        add(Place.TYPE_CAFE);
        add(Place.TYPE_FOOD);
        add(Place.TYPE_GROCERY_OR_SUPERMARKET);
        add(Place.TYPE_RESTAURANT);
    }};

    public static boolean isFoodPlace(List<Integer> aList) {
        for (int list : aList) {
            if (MEX_PLACES_TYPES.contains(list)) {
                return true;
            }
        }
        return false;
    }
}
