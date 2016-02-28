package com.hacktech.asap.pik;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.io.FileNotFoundException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import java.io.OutputStream;
import android.content.ContentValues;
import android.graphics.Bitmap.CompressFormat;
import android.provider.MediaStore.Images.Media;
import android.widget.Toast;

class Vertex {
    private double x_coord;
    private double y_coord;
    public Vertex(){
        x_coord = 0;
        y_coord = 0;
    }
    public Vertex(double x, double y){
        x_coord = x;
        y_coord = y;
    }
    public double getX(){
        return this.x_coord;
    }
    public double getY(){
        return this.y_coord;
    }
    public double distTo(Vertex v){
        double v_x = v.getX();
        double v_y = v.getY();
        return Math.sqrt((v_x - x_coord) * (v_x - x_coord) + (v_y - y_coord) * (v_y - y_coord));
    }
    public double slopeTo(Vertex v){
        double v_x = v.getX();
        double v_y = v.getY();
        if(v_x - x_coord != 0){
            return ((v_y - y_coord) / (v_x - x_coord));
        }
        else{
            return Double.POSITIVE_INFINITY;
        }
    }
    @Override
    public String toString() {
        return "{X: " + x_coord + ", Y:" + y_coord + "}";
    }
}
class Line {
    Vertex v1, v2;
    public Line(Vertex v_1, Vertex v_2) {
        v1 = v_1;
        v2 = v_2;
    }
    public double slope(){
        return v1.slopeTo(v2);
    }
    public double y_inter(){
        return v1.getY() - (v1.getX() * slope());
    }
    public double crossProduct(Line l){
        double vec_x = v1.getX() - v2.getX();
        double vec_y = v1.getY() - v2.getY();
        double lvec_x = l.v1.getX() - l.v2.getX();
        double lvec_y = l.v1.getY() - l.v2.getY();
        return (vec_x * lvec_y) - (vec_y * lvec_x);
    }

    public Boolean intersects(Line l){
        double c1 = crossProduct(new Line(v2, l.v1));
        double c2 = crossProduct(new Line(v2, l.v2));

        double c3 = l.crossProduct(new Line(l.v2, v1));
        double c4 = l.crossProduct(new Line(l.v2, v2));

        System.out.println("c1: " + c1 + ", c2: " + c2 + ", c3: " + c3 + ", c4: " + c4);
        return (c1 * c2 <= 0) && (c3 * c4 <= 0);
    }
    public Vertex intersection(Line l){

        double x;
        double y;

        if(l.slope() == Double.POSITIVE_INFINITY)
        {
            x = l.v1.getX();
            y = v1.getY() + slope()*(x - v1.getX());
        }

        else if(this.slope() == Double.POSITIVE_INFINITY)
        {
            x = v1.getX();
            y = l.v1.getY() + l.slope() * (x - l.v1.getX());
        }
        else {

            double m = slope();
            double b = y_inter();
            double k = l.slope();
            double c = l.y_inter();

            x = (c - b) / (m - k);

            y = m * x + b;
        }
        return (new Vertex(x, y));
    }
    @Override
    public String toString() {
        return "{Endpoint1: " + v1.toString() + " ; " + "Endpoint2: " + v2.toString() + "}";
    }
}
class Polygon {
    Bitmap bmp;
    ArrayList<Vertex> vertices;
    ArrayList<Line> lines;
    int tlX;
    int tlY;
    int brX;
    int brY;

    public Polygon(){
        vertices = new ArrayList<Vertex>();
        lines = new ArrayList<Line>();
        bmp = null;
        tlX = 0;
        tlY = 0;
        brX = 0;
        brY = 0;
    }
    //    Make a copy of a Polygon
    public Polygon(Polygon other) {
        this.bmp = other.bmp;
        this.lines = new ArrayList<Line>();
        this.vertices = new ArrayList<Vertex>();
        this.vertices.addAll(other.vertices);
        this.lines.addAll(other.lines);
        this.tlX = other.tlX;
        this.tlY = other.tlY;
        this.brX = other.brX;
        this.brY = other.brY;
    }
    public void addVertex(Vertex v){
        vertices.add(v);
    }
    public void addLine(Line l){
        lines.add(l);
    }
    public void addImage(Bitmap b, View v, Canvas c, Paint p){
        bmp = b;
        placeImage(bmp, c, p);
        v.invalidate();
    }

    public void placeImage(Bitmap b, Canvas canvas, Paint paint)
    {
        tlX = (int)vertices.get(0).getX();
        tlY = (int)vertices.get(0).getY();
        brX = (int)vertices.get(0).getX();
        brY = (int)vertices.get(0).getY();

        for(int i = 0; i < vertices.size(); i++)
        {
            if(vertices.get(i).getX() < tlX)
                tlX = (int)vertices.get(i).getX();

            if(vertices.get(i).getY() < tlY)
                tlY = (int)vertices.get(i).getY();

            if(vertices.get(i).getX() > brX)
                brX = (int)vertices.get(i).getX();

            if(vertices.get(i).getY() > brY)
                brY = (int)vertices.get(i).getY();
        }

        int sourceHeight = b.getHeight();
        int sourceWidth =  b.getWidth();
        int destHeight = brY - tlY;
        int destWidth = brX - tlX;

        int sourcetlX = 0;
        int sourcetlY = 0;
        int sourcebrX = 0;
        int sourcebrY = 0;

        if(((float)sourceHeight)/((float)sourceWidth) >= ((float)destHeight)/((float)destWidth))
        {
            sourcetlX = 0;
            sourcebrX = b.getWidth();
            int sourceRectHeight = (destHeight*sourcebrX)/destWidth;
            sourcetlY = (sourceHeight - sourceRectHeight)/2;
            sourcebrY = (sourceHeight + sourceRectHeight)/2;
        }
        else
        {
            sourcetlY = 0;
            sourcebrY = b.getHeight();
            int sourceRectWidth = (destWidth*sourcebrY)/destHeight;
            sourcetlX = (sourceWidth - sourceRectWidth)/2;
            sourcebrX = (sourceWidth + sourceRectWidth)/2;
        }

        Rect destination = new Rect(tlX, tlY, brX, brY);
        Rect source = new Rect(sourcetlX, sourcetlY, sourcebrX, sourcebrY);
        canvas.drawBitmap(b, source, destination, paint);
    }

    public void redraw(View v, Canvas c, Paint p){
        for (Line l : lines) {
            c.drawLine((float) l.v1.getX(), (float) l.v1.getY(), (float) l.v2.getX(), (float) l.v2.getY(), p);
            if (bmp != null) {
                placeImage(bmp, c, p);
            }
        }
}
    public ArrayList<Polygon> split(Line l){
        Line[] intr = new Line[2];
        int count = 0;
        System.out.println(this);
        for(int i = 0; i < lines.size(); i++){
            System.out.println(i);
            Line line = lines.get(i);
            if(l.intersects(line)){
                intr[count] = line;
                System.out.println("intr[" + count + "] =" + line);
                System.out.println("Slope of intr[" + count + "] = " + line.slope());
                count++;
                lines.remove(i);
                i--;
            }
        }
        System.out.println(l);
        for(int i=0; i < intr.length; i++){
            Line line = intr[i];
            System.out.println("intr[" + i + "] = " + line);
            Vertex v1 = l.intersection(line);
            Vertex lv1 = line.v1;
            Vertex lv2 = line.v2;
            lines.add(new Line(v1, lv1));
            lines.add(new Line(v1, lv2));
        }
        Polygon p1 = new Polygon();
        Polygon p2 = new Polygon();
        for(int i = 0; i < lines.size(); i++){
            Line line = lines.get(i);
            Line line_v1 = new Line(l.v2, line.v1);
            Line line_v2 = new Line(l.v2, line.v2);
            if(l.crossProduct(line_v1) >= 0 && l.crossProduct(line_v2) >= 0){
                p1.addLine(line);
                if(!p1.vertices.contains(line.v1)){
                    p1.addVertex(line.v1);
                }
                if(!p1.vertices.contains(line.v2)){
                    p1.addVertex(line.v2);
                }
            }
            else{
                p2.addLine(line);
                if(!p2.vertices.contains(line.v1)){
                    p2.addVertex(line.v1);
                }
                if(!p2.vertices.contains(line.v2)){
                    p2.addVertex(line.v2);
                }
            }
        }
        p1.addLine(l);
        p1.bmp = this.bmp;
        p2.bmp = this.bmp;
        p2.addLine(l);
        ArrayList<Polygon> splits = new ArrayList<Polygon>();
        splits.add(p1);
        splits.add(p2);
        return splits;
    }
    public boolean containVertex (Vertex interior_vertex) {
        System.out.println("Starting contain algorithm for " + interior_vertex + "\n" + "in polygon: " +
                this);
        Polygon temp = new Polygon(this);
//     Start at a random line
        Line initialLine = temp.lines.get(0);
        Vertex currVertex = initialLine.v2;
        double cross = initialLine.crossProduct(new Line (currVertex, interior_vertex));
        System.out.println("Checking line: " + initialLine + ", " + cross);
        temp.lines.remove(0);
//        Check which line contains 'v2' and then check again to see if the
//        cross product is the same sign
        while (temp.lines.size() > 0) {
//            Find the line with corresponding index and save index
            int lineIndex = 0;
            for (int i = 0; i < temp.lines.size(); i++) {
                if ((temp.lines.get(i).v1.getX() == currVertex.getX() &&
                        temp.lines.get(i).v1.getY() == currVertex.getY())
                        || (temp.lines.get(i).v2.getX() == currVertex.getX() &&
                        temp.lines.get(i).v2.getY() == currVertex.getY())){
                    lineIndex = i;
                    break;
                }
            }
//            Construct new vector
            Line currLine = temp.lines.get(lineIndex);
            Vertex firstVertex;
            Vertex secondVertex;
            if (currLine.v1.getX() == currVertex.getX() && currLine.v1.getY() == currVertex.getY()) {
                firstVertex = currLine.v1;
                secondVertex = currLine.v2;
            }
            else {
                firstVertex = currLine.v2;
                secondVertex = currLine.v1;
            }
//            Calculate cross product
            double nextCross =
                    (new Line(firstVertex, secondVertex)).crossProduct(new Line(secondVertex, interior_vertex));
            System.out.println("Checking line: " + (new Line(firstVertex, secondVertex)) + ", " + nextCross);
            if (cross * nextCross < 0) {
                System.out.println("Not contained...");
                return false;
            }
//            Remove this line and currVertex
            currVertex = secondVertex;
            temp.lines.remove(lineIndex);
        }
        System.out.println("contained...");
        return true;
    }
    @Override
    public String toString() {
        String res = "Polygon:\n";
        for (int i=0; i < lines.size(); i++) {
            res += "\t" + lines.get(i).toString() + "\n";
        }
        return res;
    }

}
class PolygonStructure {
    ArrayList<Polygon> splits;
    public PolygonStructure(){
        splits = new ArrayList<Polygon>();
    }
    public PolygonStructure(PolygonStructure ps) {
        PolygonStructure ps2 = new PolygonStructure();
        for (Polygon p : ps.splits) {
            Polygon p2 = new Polygon(p);
            ps2.splits.add(p2);
        }
        splits = ps2.splits;
    }
    @Override
    public String toString() {
        String res = "---Structure---\n";
        for (int i=0; i < splits.size(); i++) {
            res += splits.get(i).toString();
            res +=   "---------------\n";
        }
        return res;
    }
    public void redraw(View v, Canvas c, Paint paint, Polygon selected) {
        c.drawColor(Color.WHITE);
        for (Polygon p : splits) {
            if (p == selected) {
                paint.setColor(Color.YELLOW);
                paint.setStrokeWidth(8);
            }
            p.redraw(v, c, paint);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(5);
        }
        v.invalidate();
    }
    public int numPolygons() {
        return splits.size();
    }
}

public class MainActivity extends AppCompatActivity implements OnTouchListener {

    static final int REQUEST_TAKE_PHOTO = 1;
    private int PICK_IMAGE_REQUEST = 1;
    static final int CAMERA_REQUEST = 1888;

    private boolean sectionSelected;
    private Polygon selectedPolygon;

    private ImageButton mButton1;
    private ImageButton mButton2;
    private ImageButton mButton3;
    private ImageButton mButton4;
    private ImageButton mButton5;
    String mCurrentPhotoPath;
    String imgDecodableString;

    ImageView choosenImageView;
    Button choosePicture;
    Button savePicture;
    Bitmap bmp;
    Bitmap alteredBitmap;
    Bitmap galleryBitmap;
    Canvas canvas;
    Paint paint;
    Matrix matrix;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;
    int width = 1000;
    int height = 1000;
    PolygonStructure structure;

    ArrayList<PolygonStructure> history;
    ArrayList<PolygonStructure> future;
    int cancelIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        choosenImageView = (ImageView) this.findViewById(R.id.ChoosenImageView);
        choosenImageView.setOnTouchListener(this);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int height = size.x;
        Bitmap.Config cnf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(width, height, cnf);
        structure = new PolygonStructure();
        //        Construct a set of 4 points for the corners of the canvas
        Vertex botLeft = new Vertex(0, height);
        Vertex topLeft = new Vertex(0, 0);
        Vertex topRight = new Vertex(width, 0);
        Vertex botRight = new Vertex(width, height);
        //        Construct set of 4 lines out of these points
        Line left = new Line(botLeft, topLeft);
        Line top = new Line(topLeft, topRight);
        Line right = new Line(topRight, botRight);
        Line bot = new Line(botRight, botLeft);
        //        Construct Polygon out of these lines
        Polygon initPolygon = new Polygon();
        initPolygon.lines.add(left);
        initPolygon.lines.add(top);
        initPolygon.lines.add(right);
        initPolygon.lines.add(bot);
        initPolygon.vertices.add(botLeft);
        initPolygon.vertices.add(botRight);
        initPolygon.vertices.add(topLeft);
        initPolygon.vertices.add(topRight);
        //        Finally add this polygon to the structure
        structure.splits.add(initPolygon);
        System.out.print(structure.toString());
        canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        choosenImageView.invalidate();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        matrix = new Matrix();
        canvas.drawBitmap(bmp, matrix, paint);
        choosenImageView.setImageBitmap(bmp);
        choosenImageView.setOnTouchListener(this);
        //        Draw initial lines of canvas
        for (Polygon p: structure.splits) {
            for (Line l: p.lines) {
                canvas.drawLine((float) l.v1.getX(), (float) l.v1.getY(), (float) l.v2.getX(), (float) l.v2.getY(), paint);
            }
        }

        history = new ArrayList<PolygonStructure>();
        future = new ArrayList<PolygonStructure>();
        cancelIndex = 0;

        mButton1 = (ImageButton) findViewById(R.id.button1);
        mButton2 = (ImageButton) findViewById(R.id.button2);
        mButton3 = (ImageButton) findViewById(R.id.button3);
        mButton4 = (ImageButton) findViewById(R.id.button4);
        mButton5 = (ImageButton) findViewById(R.id.button5);

        final ColorPicker cp = new ColorPicker(MainActivity.this, 255, 255, 255);

        sectionSelected = false;

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sectionSelected) {
                    undo();
                } else {
                    cancel();
                    sectionSelected = false;
                    selectedPolygon = null;
                    mButton1.setImageResource(R.drawable.undo_selector);
                    mButton2.setImageResource(R.drawable.clear_selector);
                    mButton3.setImageResource(R.drawable.save_selector);
                    mButton4.setImageResource(R.drawable.highlight_selector);
                    mButton5.setImageResource(R.drawable.redo_selector);
                }
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sectionSelected == false) {
                    canvas.drawColor(Color.WHITE);
                    choosenImageView.invalidate();
                    structure = new PolygonStructure();
                    //        Construct a set of 4 points for the corners of the canvas
                    Vertex botLeft = new Vertex(0, height);
                    Vertex topLeft = new Vertex(0, 0);
                    Vertex topRight = new Vertex(width, 0);
                    Vertex botRight = new Vertex(width, height);
                    //        Construct set of 4 lines out of these points
                    Line left = new Line(botLeft, topLeft);
                    Line top = new Line(topLeft, topRight);
                    Line right = new Line(topRight, botRight);
                    Line bot = new Line(botRight, botLeft);
                    //        Construct Polygon out of these lines
                    Polygon initPolygon = new Polygon();
                    initPolygon.lines.add(left);
                    initPolygon.lines.add(top);
                    initPolygon.lines.add(right);
                    initPolygon.lines.add(bot);
                    //        Finally add this polygon to the structure
                    structure.splits.add(initPolygon);

                } else {
                    loadImageFromGallery(v);
                }
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sectionSelected) {
                    takePicture();
                } else {
                    View content = choosenImageView;

                    Bitmap bitmap = Bitmap.createBitmap(content.getWidth(), content.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    content.draw(canvas);

                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "/" + timeStamp;
                    File file = new File(path+ imageFileName + ".png");
                    FileOutputStream ostream;
                    try {
                        file.createNewFile();
                        ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                        MediaScannerConnection.scanFile(content.getContext(),
                                new String[]{file.toString()}, null,
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.i("ExternalStorage", "Scanned " + path + ":");
                                        Log.i("ExternalStorage", "-> uri=" + uri);
                                    }
                                });
                        ostream.flush();
                        ostream.close();
                        Toast.makeText(getApplicationContext(), "Saved image", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sectionSelected) {
                    /* Show color picker dialog */
                    cp.show();

                    /* On Click listener for the dialog, when the user select the color */
                    Button okColor = (Button)cp.findViewById(R.id.okColorButton);

                    okColor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /* Or the android RGB Color (see the android Color class reference) */
                            int selectedColorR = cp.getRed();
                            int selectedColorG = cp.getGreen();
                            int selectedColorB = cp.getBlue();
                            int selectedColorRGB = cp.getColor();
                            Context context = getApplicationContext();
                            CharSequence text = "RGB: " + selectedColorR + " " + selectedColorG + " " + selectedColorB;
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();

                            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            image.eraseColor(selectedColorRGB);
                            history.add(new PolygonStructure(structure));
                            future.clear();
                            selectedPolygon.addImage(image, choosenImageView, canvas, paint);

                            cp.dismiss();
                        }
                    });
                }
            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sectionSelected) {
                    sectionSelected = false;
                    selectedPolygon = null;
                    mButton1.setImageResource(R.drawable.undo_selector);
                    mButton2.setImageResource(R.drawable.clear_selector);
                    mButton3.setImageResource(R.drawable.save_selector);
                    mButton4.setImageResource(R.drawable.highlight_selector);
                    mButton5.setImageResource(R.drawable.redo_selector);
                    structure.redraw(choosenImageView, canvas, paint, selectedPolygon);
                } else {
                    redo();
                }
            }
        });
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downx = event.getX();
                downy = event.getY();
                break;
            case MotionEvent.ACTION_UP:
//                Calculate the slope of the current line based
//                on the down and up coordinates
                upx = event.getX();
                upy = event.getY();

                if(Math.abs(downx - upx) < 10 && Math.abs(downy - upy) < 10){

                    if (!sectionSelected) {
                        Vertex downVert = new Vertex(downx, downy);

                        Polygon containingPoly = structure.splits.get(0);
                        for (int i = 0; i < structure.splits.size(); i++) {
                            if (structure.splits.get(i).containVertex(downVert)) {
                                containingPoly = structure.splits.get(i);
                                break;
                            }
                        }

                        System.out.println(containingPoly);

                        sectionSelected = true;
                        selectedPolygon = containingPoly;
                        mButton1.setImageResource(R.drawable.clear_selector);
                        mButton2.setImageResource(R.drawable.photo_selector);
                        mButton3.setImageResource(R.drawable.photo_camera_selector);
                        mButton4.setImageResource(R.drawable.color_lens_selector);
                        mButton5.setImageResource(R.drawable.done_selector);
                        cancelIndex = history.size() - 1;
                        structure.redraw(choosenImageView, canvas, paint, selectedPolygon);
                    }
                }
                else if (structure.numPolygons() < 12){

                    history.add(new PolygonStructure(structure));
                    future.clear();

//                This will store the final endpoints of the line
//                going through the Action Down and Action Up points.
                    float[][] endpoints = new float[2][2];

//                Draw the newly determined line
//                canvas.drawLine(endpoints[0][0], endpoints[0][1], endpoints[1][0],
//                        endpoints[1][1], paint);
                    Line swipeLine;
                    Vertex downVert = new Vertex(downx, downy);
//                Add the line to the polygon structure
//                Determine which polygon contains the downx and downy
                    System.out.print(structure);
                    System.out.print("Down Vertex: " + downVert);
                    Polygon containingPoly = structure.splits.get(0);
                    for (int i=0; i < structure.splits.size(); i++) {
                        if (structure.splits.get(i).containVertex(downVert)) {
                            containingPoly = structure.splits.get(i);
                            break;
                        }
                    }
                    System.out.println("Containing Polygon: ");
                    System.out.println(containingPoly);

                    if (upx - downx == 0 || Math.abs((upy - downy) / (upx - downx)) >= 1.0) {
//                  This is a vertical line
//                    Determine the ymin and ymax of the polygon
                        float ymin = (float)containingPoly.lines.get(0).v1.getY();
                        float ymax = ymin;
                        for (Line l: containingPoly.lines) {
                            float y1 = (float) l.v1.getY();
                            float y2 = (float) l.v2.getY();
                            if (y1 < ymin)
                                ymin = y1;
                            else if (y1 > ymax)
                                ymax = y1;

                            if (y2 < ymin)
                                ymin = y2;
                            else if (y2 > ymax)
                                ymax = y2;
                        }
                        swipeLine = new Line(new Vertex(downx, ymin), new Vertex(downx, ymax));
                    }
                    else {
//                    This is a horizontal line
                        float min = (float)containingPoly.lines.get(0).v1.getX();
                        float max = min;
                        for (Line l: containingPoly.lines) {
                            float x1 = (float) l.v1.getX();
                            float x2 = (float) l.v2.getX();
                            if (x1 < min)
                                min = x1;
                            else if (x1 > max)
                                max = x1;

                            if (x2 < min)
                                min = x2;
                            else if (x2 > max)
                                max = x2;
                        }
                        swipeLine = new Line(new Vertex(min, downy), new Vertex(max, downy));
                    }

//                Split the Polygon into two corresponding
                    System.out.println("Swipe Line:");
                    System.out.println(swipeLine);
                    ArrayList<Polygon> polySplit = containingPoly.split(swipeLine);
//                Remove the containing polygon and add the two in the split to the
//                structure
                    structure.splits.remove(containingPoly);
                    structure.splits.addAll(polySplit);
//                Clear canvas and redraw all the lines of the polygons
//                    canvas.drawColor(Color.WHITE);
//                    for (Polygon p: structure.splits) {
//                        for (Line l: p.lines) {
//                            canvas.drawLine((float) l.v1.getX(), (float)l.v1.getY(), (float)l.v2.getX(), (float)l.v2.getY(), paint);
//                        }
//                    }
                    structure.redraw(choosenImageView, canvas, paint, selectedPolygon);
                    System.out.print(structure.toString());
                    choosenImageView.invalidate();
                    break;
                } else {
                    Toast.makeText(this, "Too many polygons!", Toast.LENGTH_SHORT)
                            .show();
                }
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void takePicture(){
        Intent cameraIntent = new  Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }


    private void loadImageFromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                galleryBitmap = BitmapFactory.decodeFile(imgDecodableString);

                history.add(new PolygonStructure(structure));
                future.clear();
                selectedPolygon.addImage(galleryBitmap, choosenImageView, canvas, paint);
            } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                    Bitmap picture = (Bitmap) data.getExtras().get("data");//this is your bitmap image and now you can do whatever you want with this
                    history.add(new PolygonStructure(structure));
                    future.clear();
                    selectedPolygon.addImage(picture, choosenImageView, canvas, paint);
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public boolean undo() {
    //        Revert to the most recent element in the history list
    //        Add that structure to futre in case they want to redo
        if (history.size() > 0) {
            future.add(structure);
            structure = history.remove(history.size() - 1);
            structure.redraw(choosenImageView, canvas, paint, selectedPolygon);
            return true;
        }
        return false;
    }
    public boolean redo() {
    //        Revert to the most recent element in future list
        if (future.size() > 0) {
            history.add(new PolygonStructure(structure));
            structure = future.remove(future.size() - 1);
            structure.redraw(choosenImageView, canvas, paint, selectedPolygon);
            return true;
        }
        return false;
    }
    public boolean cancel() {
        //        Revert to pre-editing
        while (history.size() > cancelIndex + 1) {
            undo();
        }
        return true;
    }

}