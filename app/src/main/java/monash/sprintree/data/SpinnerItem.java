package monash.sprintree.data;

/**
 * Created by kalyani on 22/4/17.
 */

public class SpinnerItem {
    String text;
    Integer imageId;
    public SpinnerItem(String text, Integer imageId){
        this.text=text;
        this.imageId=imageId;
    }

    public String getText(){
        return text;
    }


    public Integer getImageId(){
        return imageId;
    }
}

