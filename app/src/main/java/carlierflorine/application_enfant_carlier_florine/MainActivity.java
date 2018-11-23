package carlierflorine.application_enfant_carlier_florine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements DrawingImage.DrawFinishListener{
    public boolean layoutA ;
    public boolean layoutB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutA = true ;
        layoutB = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Charge le layout de la lettre A
        LinearLayout MainLayout = findViewById(R.id.MainLayout);
        int drawable = R.drawable.a;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawable);
        DrawingImage image = new DrawingImage(this, bitmap);
        image.setImageBitmap(bitmap);
        MainLayout.addView(image);
    }

    // Afficher un toast si la lettre est complétée
    @Override
    public void onDrawFinish() {
        Toast.makeText(getApplicationContext(), "Dessin terminé", Toast.LENGTH_LONG).show();
        // Si on se trouve dans le layoutA
        if(layoutA){
            layoutA = false;
            layoutB = true;
            // alors on lance le layoutB
            startLayoutB();
        }
        // sinon, si on se trouve dans le layout B
        else{
            // lancer la layout de félicitation
            startLayoutCongrat();
        }
    }
    // Lance le layoutA
    public void startLayoutA(){
        setContentView(R.layout.activity_main);
        LinearLayout Main = findViewById(R.id.MainLayout);
        int drawable = R.drawable.a;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawable);
        DrawingImage image = new DrawingImage(this, bitmap);
        image.setImageBitmap(bitmap);
        Main.addView(image);
    }

    // Lance le layout B
    public void startLayoutB(){
        setContentView(R.layout.activity_main2);
        LinearLayout Main = findViewById(R.id.MainLayout);
        int drawable = R.drawable.b;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawable);
        DrawingImage image = new DrawingImage(this, bitmap);
        image.setImageBitmap(bitmap);
        Main.addView(image);
    }

    // Lance le layout de félicitation
    public void startLayoutCongrat(){
        setContentView(R.layout.activity_main3);
    }


    @Override
    public void onDrawStop() {
        Log.i("Action: ", "Draw stop");
    }

    @Override
    public void onDrawStart() {
        Log.i("Action: ", "Draw start");
    }

    //Création du menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_goBack:
                if(layoutB)
                    startLayoutA();
                return true;

            case R.id.action_clear:
                if(layoutA)
                    startLayoutA();
                if(layoutB){
                    startLayoutB();
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


}