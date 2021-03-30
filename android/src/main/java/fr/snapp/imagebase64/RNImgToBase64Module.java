
package fr.snapp.imagebase64;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class RNImgToBase64Module extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNImgToBase64Module(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNImgToBase64";
  }
  

  @ReactMethod
  public void getBase64String(String uri, String token, Integer compression, Promise promise) {
    Bitmap image = null;
    try {
      if (uri.contains("http")) {
        image = getBitmapFromURL(uri, token);
      } else {
        image = MediaStore.Images.Media.getBitmap(reactContext.getContentResolver(), Uri.parse(uri));
      }
      if (image == null) {
        promise.reject("Error", "Failed to decode Bitmap, uri: " + uri);
      } else {
        WritableMap reponseData = new Arguments().createMap();
        String base64Image = bitmapToBase64(image,compression);
        reponseData.putString("data", base64Image);
        promise.resolve(reponseData);
      }
    } catch (Error e) {
      promise.reject("Error", "Failed to decode Bitmap: " + e);
      e.printStackTrace();
    } catch (Exception e) {
      promise.reject("Error", "Exception: " + e);
      e.printStackTrace();
    }
  }

  public Bitmap getBitmapFromURL(String src, String token) {
    try {
      URL url = new URL(src);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      String bearerAuth = "Bearer " + token;
      connection.setRequestProperty ("Authorization", bearerAuth);
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      Bitmap myBitmap = BitmapFactory.decodeStream(input);
      return myBitmap;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String bitmapToBase64(Bitmap bitmap, Integer compression) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, compression, byteArrayOutputStream);
    byte[] byteArray = byteArrayOutputStream.toByteArray();
    return Base64.encodeToString(byteArray, Base64.DEFAULT);
  }
}
