package tools;


import com.activeandroid.query.Delete;

import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import adapters.ContactsAdapter;
import application.Constants;
import models.Contact;
import models.ContactInfo;

public class ContactsHelper {

    public static Bitmap getContactPhoto(Context context, long contactId) {
        Uri contactUri = ContentUris
                .withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri
                .withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public static Contact getContact(Context context, Uri contactData) {
        Cursor c = context.getContentResolver().query(contactData, null, null, null, null);

        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            long id = Long.parseLong(c.getString(c.getColumnIndex(ContactsContract.Contacts._ID)));
            String photoName = String.valueOf(id); //unique id of photo get from contact_id
            Bitmap contactPhotoBitmap = getContactPhoto(context, id);
            saveBitmapToInternalStorage(contactPhotoBitmap, photoName, context);

            return new Contact(name, photoName);
        } else {
            return null;
        }
    }

    public static String saveBitmapToInternalStorage(Bitmap bitmapImage, String fileName,
            Context context) {

        /*ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir*/
        File DIRECTORY_IMAGES = new ContextWrapper(context.getApplicationContext())
                .getDir(Constants.PHOTOS_DIR_NAME, Context.MODE_PRIVATE);

        File myPath = new File(DIRECTORY_IMAGES, fileName);

        FileOutputStream fileOutputStream;
        try {

            fileOutputStream = new FileOutputStream(myPath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return DIRECTORY_IMAGES.getAbsolutePath();
    }

    public static Bitmap loadBitmapFromStorage(String fileName, Context context) {
        try {
            File DIRECTORY_IMAGES = new ContextWrapper(context.getApplicationContext())
                    .getDir(Constants.PHOTOS_DIR_NAME, Context.MODE_PRIVATE);
            File fileContactPhoto = new File(DIRECTORY_IMAGES.getAbsolutePath(), fileName);
            return BitmapFactory.decodeStream(new FileInputStream(fileContactPhoto));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void removeContact(Contact longClickedItem, ContactsAdapter contactsAdapter) {
        //cascade delete of info
        new Delete().from(ContactInfo.class).where("Contact = ?", longClickedItem.getId())
                .execute();

        longClickedItem.delete();
        if (longClickedItem.getPhotoName() != null) {
            deleteBitmapFromStorage(longClickedItem.getPhotoName(), contactsAdapter.getContext());
        }

        contactsAdapter.getList().remove(longClickedItem);
        contactsAdapter.notifyDataSetChanged();
    }

    public static void addContact(Contact contact, ContactsAdapter contactsAdapter) {

        //avoiding of duplication
       /* if (contactsAdapter.getList().contains(contact)) {
            Toast.makeText(this, R.string.contact_already_exist, Toast.LENGTH_SHORT)
                                .show();
        }*/

        contactsAdapter.getList().add(contact);
        contact.save();

        //adding basic info
        String standartValue = "enter some info here...";
        //for (int i = 0; i < 20; i++) {
        ContactInfo info = new ContactInfo("Profession", standartValue, contact);
        info.save();
        //}
        info = new ContactInfo("Interests", standartValue, contact);
        info.save();
        info = new ContactInfo("May be helpful in", standartValue, contact);
        info.save();

        contactsAdapter.notifyDataSetChanged();

    }

    public static void deleteBitmapFromStorage(String fileName, Context context) {
        File DIRECTORY_IMAGES = new ContextWrapper(context.getApplicationContext())
                .getDir(Constants.PHOTOS_DIR_NAME, Context.MODE_PRIVATE);
        File filePhoto = new File(DIRECTORY_IMAGES.getAbsolutePath(), fileName);
        filePhoto.delete();
    }

    public static int convertToPixels(int dps, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static File getExternalCacheDir(final Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
            return context.getExternalCacheDir(); // >API level 8
        }
        // e.g. "<sdcard>/Android/data/<package_name>/cache/"
        final File extCacheDir = new File(Environment.getExternalStorageDirectory(),
                "/Android/data/" + context.getApplicationInfo().packageName + "/cache/");
        extCacheDir.mkdirs();
        return extCacheDir;
    }

    public static String readPemberContactFile(File file) {
//Read text from file
        StringBuilder text = new StringBuilder();

        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            while ((line = br.readLine()) != null) {
                text.append(line);
                //text.append('\n');
            }
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return String.valueOf(text);
    }
}
