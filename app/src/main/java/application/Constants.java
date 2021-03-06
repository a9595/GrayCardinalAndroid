package application;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.File;

public class Constants {
    public static final int PICK_CONTACT = 1;
    public static final String EXTRAS_CONTACT_ID = "ContactExtra";
    public static final String EXTRAS_INFO_NAME = "InfoName";
    public static final String EXTRAS_INFO_VALUE = "InfoValue";
    public static final int REQUEST_CODE_ADD_INFO = 1;

    public static final int DEVIDER_HEIGHT = 5*2;
    public final static int EDIT_IN_POPUP = 0;
    public final static int REMOVE_IN_POPUP = 1;
    public static final int STATE_ONSCREEN = 0;
    public static final int STATE_OFFSCREEN = 1;
    public static final int STATE_RETURNING = 2;
    public static final String PHOTOS_DIR_NAME = "imageDir";
}
