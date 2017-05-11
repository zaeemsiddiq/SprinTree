package monash.sprintree.fragments;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.lylc.widget.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.data.Journey;
import monash.sprintree.data.Tree;
import monash.sprintree.service.TreeService;

/**
 * Created by Zaeem on 5/1/2017.
 */

public class MyPlantFragment extends Fragment {
    FragmentListener listener;
    private ImageView imgHolder;
    float percent = 99; // ranging from 1 - 99
    CircularProgressBar circularProgressBar;

    List<String> visitedGenus= new ArrayList<>();
    List<String> unVisitedGenus = new ArrayList<>();

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
        final int[] checkPercent = {0};
        int gap = 100 - (int)percent;

        new CountDownTimer(1000, 10) { //100 ticks, fill colored image with black and white image from 0 to covered percentage in 100 ticks

            public void onTick(long millisUntilFinished) {
                checkPercent[0] +=2;
                System.out.println("seconds remaining: " + millisUntilFinished / 1000);
                //imgHolder
                  //    .setImageBitmap(doTopToBottomOperation(checkPercent[0]));
            }

            public void onFinish() {
                //imgHolder
                  //      .setImageBitmap(doTopToBottomOperation(20));
                System.out.println("done!" + (int)percent );
            }
        }.start();
        calculateGenus();
        populateTree();
        animateProgressBar();
        circularProgressBar = (CircularProgressBar) view.findViewById(R.id.circularprogressbar2);
        circularProgressBar.animateProgressTo(100, (int)percent, new CircularProgressBar.ProgressAnimationListener() {
            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationProgress(int progress) {
                circularProgressBar.setTitle(progress + "%");
                int target = (int)percent;
                int jump = 0;
                if(target <= progress) {
                    jump=100-progress;
                }
                imgHolder
                        .setImageBitmap(doTopToBottomOperation(jump));
            }

            @Override
            public void onAnimationFinish() {
                circularProgressBar.setSubTitle(visitedGenus.size()+"/"+(visitedGenus.size() + unVisitedGenus.size()) +" families visited");
            }
        });

        return view;
    }

    private void animateProgressBar() {

    }

    private void populateTree() {
        int total = visitedGenus.size() + unVisitedGenus.size();
        float visited = visitedGenus.size();
        percent = (visited/total)*100;
        //imgHolder
          //      .setImageBitmap(doBottomToTopOperation((int)percent));
    }

    private void calculateGenus() {
        for(Tree tree: Tree.findWithQuery(Tree.class, "SELECT DISTINCT GENUS FROM TREE")) {
            unVisitedGenus.add(tree.genus);
        }
        for( Tree tree : TreeService.getVisitedTrees()) {
            if(!visitedGenus.contains(tree.genus)) {
                visitedGenus.add(tree.genus);
            }
        }
        unVisitedGenus.removeAll(visitedGenus);
    }
    public Bitmap doTopToBottomOperation(int percentage) {
        Bitmap bitmapOriginal = ((BitmapDrawable) imgHolder.getDrawable())
                .getBitmap();

        Bitmap bitmapTarget = BitmapFactory.decodeResource(getResources(),
                R.drawable.tree_empty );

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
                R.drawable.tree_empty);

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
