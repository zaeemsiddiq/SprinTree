package monash.sprintree.fragments;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import monash.sprintree.R;
import monash.sprintree.data.Journey;

/**
 * Created by Zaeem on 5/1/2017.
 */

public class MyPlantFragment extends Fragment {
    FragmentListener listener;
    private ImageView imgHolder;
    int percent = 10;

    Button animateButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static MyPlantFragment newInstance(FragmentListener listener) {
        MyPlantFragment fragment = new MyPlantFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_plant, container, false);
        imgHolder = (ImageView) view.findViewById(R.id.coringImage);
        imgHolder
                .setImageBitmap(doBottomToTopOperation(percent));

        animateButton = (Button) view.findViewById(R.id.animateButton);
        animateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                percent+=5;
                if(percent == 100) {
                    percent=0;
                }
                imgHolder
                        .setImageBitmap(doBottomToTopOperation(percent));
            }
        });
        return view;
    }

    public Bitmap doTopToBottomOperation(int percentage) {
        Bitmap bitmapOriginal = ((BitmapDrawable) imgHolder.getDrawable())
                .getBitmap();

        Bitmap bitmapTarget = BitmapFactory.decodeResource(getResources(),
                R.drawable.tree_full );

        int heightToCrop = bitmapTarget.getHeight() * percentage / 100;

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmapTarget, 0, 0,
                bitmapTarget.getWidth(), heightToCrop);

        Bitmap bmOverlay = Bitmap.createBitmap(bitmapOriginal.getWidth(),
                bitmapOriginal.getHeight(), bitmapOriginal.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bitmapOriginal, new Matrix(), null);
        canvas.drawBitmap(croppedBitmap, new Matrix(), null);

        return bmOverlay;
    }

    public Bitmap doBottomToTopOperation(int percentage) {
        Bitmap bitmapOriginal = ((BitmapDrawable) imgHolder.getDrawable())
                .getBitmap();

        Bitmap bitmapTarget = BitmapFactory.decodeResource(getResources(),
                R.drawable.tree_full);

        int heightToCrop = bitmapTarget.getHeight() * (100 - percentage) / 100;

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmapTarget, 0,
                heightToCrop, bitmapTarget.getWidth(), bitmapTarget.getHeight()
                        - heightToCrop);

        Bitmap bmOverlay = Bitmap.createBitmap(bitmapOriginal.getWidth(),
                bitmapOriginal.getHeight(), bitmapOriginal.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bitmapOriginal, new Matrix(), null);
        canvas.drawBitmap(croppedBitmap,
                canvas.getWidth() - croppedBitmap.getWidth(),
                canvas.getHeight() - croppedBitmap.getHeight(), null);

        return bmOverlay;
    }
}
