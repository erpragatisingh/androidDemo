package com.video;

import java.io.IOException;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {
    /** Called when the activity is first created. */
 private MediaController mc;
 VideoView vd;
   
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        vd = (VideoView) findViewById(R.id.video);
        String url1="http://myfavouritesongs.com/mymusic/Yeh_To_Sach_Hai_Ki_Bhagwan_Hai.rm#.mp3";
        String url = "http://www.mp3url.net/download.php?name=The_saltwater_room_owl_city.mp3&url=t62XabuWw8ibk7Gol6qhx8-keqOCwaTauLdpnpe4v-Soy417rpih4U5rSq66sY_Pn5q2wq6vlGimtLa9n4p7j6OrmaO5hXuAtMCov5-qjNKnn7lwZG6-06Zqocy2p4jWwZ-djNGqxICmZW-dsKuHr5Kbf9uqvJu8tnqSnqjKZFlah9GYj8OfhaeVu8GSjK22tMWbjo-KqsbHh75hhaHFj9ywvcqrqaR31kRvdrqYqWykvKrCnbXOY4LEusjQoH6pmqyk1K-WeqebpIDsqJahwK6mkOxPREWLs7-kjJ-nh8PEr34&mode=redirect"; // your URL here
        String url2="http://www.mp3url.net/download.php?name=ye_rishta_kya_kehlata_hai_hulk_share.mp3&url=t62XabuWw8iblL2qj4ngy7q6rWaYm8ret6t2qpC0fOKnhITAwpqd5mEyUsmxd4eOtb6rycOvf6i6n5ygsa5ueLiZpp-9h6mjqoyprbWHm9a0n7R6UDPFnpJ8pce2qpDb&mode=redirect";
        String url3="http://www.mp3url.net/download.php?name=Ye_jo_chilman_hai_dushman_hai_mehboob_ki_mehndi.mp3&url=t62XabuWw8iblM9kmtCgyru3k6marKTUubdlm5iTd-6ago2gtr2VuUwwaMbHsovInoWgxcPZhp2r2b7cppxlnaCppqK9h6idsp6-tpuZkJK1sLVMWVfNzJqmnr-1pZDQuZp4m76au72RZYSZn6if2n11ociTy4esrp2jnansQkRhndG4m6-YyZuUydOSiZmxtsabtJeNmsutm8yYkomitsycqMOjzou9zGlCTLe4h4eyqbzHqajWoYLEncm7xA&mode=redirect";
        
        
        
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
			mediaPlayer.setDataSource(url);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // might take long! (for buffering, etc)
        mediaPlayer.start();

     /*   Uri uri = Uri.parse("android.resource://" + getPackageName() + R.raw.vv);

        mc = new MediaController(this);
        vd.setMediaController(mc);

        vd.setVideoURI(uri);
        vd.start();*/
    }
}

