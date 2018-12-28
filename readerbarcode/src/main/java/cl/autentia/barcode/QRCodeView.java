package cl.autentia.barcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class QRCodeView extends RelativeLayout {

    private int maskColor;
    private int boxViewWidth;
    private int boxViewHeight;
    private int cornerColor;
    private int borderColor;
    private int cornerSize;
    private int cornerLength;
    private int cornerOffset;

    private FrameLayout boxView;
    private TextView textView;
    private OnClickListener lightOnClickListener;
    private Context mContext;
    private PreferencesUtils oPreferences;
    private CheckBox btn_switch;
    private CheckBox btn_switch_portrait;

    public QRCodeView(Context context) {
        super(context);
        this.mContext = context;
        initialize(context, null, 0, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initialize(context, attrs, 0, 0);
    }

    public QRCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initialize(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("NewApi")
    public QRCodeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(final Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.layout_qr_code_view, this);

        this.mContext = context;
        oPreferences = new PreferencesUtils(this.mContext);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QRCodeView, defStyleAttr, 0);
        Resources resources = getResources();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_maskColor, resources.getColor(R.color.qr_code_view_mask));
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_corner));
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, resources.getColor(R.color.qr_code_view_border));
        } else {
            maskColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_mask, null));
            cornerColor = typedArray.getColor(R.styleable.QRCodeView_boxViewCornerColor, resources.getColor(R.color.qr_code_view_corner, null));
            borderColor = typedArray.getColor(R.styleable.QRCodeView_boxViewBorderColor, resources.getColor(R.color.qr_code_view_border, null));
        }

        cornerOffset = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerOffset, (int) resources.getDimension(R.dimen.size_qr_box_view_corner_offset));
        cornerLength = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerLength, (int) resources.getDimension(R.dimen.length_qr_box_view_corner));
        cornerSize = typedArray.getInt(R.styleable.QRCodeView_boxViewCornerSize, (int) resources.getDimension(R.dimen.size_qr_box_view_corner));
        boxViewWidth = typedArray.getInt(R.styleable.QRCodeView_boxViewWidth, (int) resources.getDimension(R.dimen.width_qr_box_view));
        boxViewHeight = typedArray.getInt(R.styleable.QRCodeView_boxViewHeight, (int) resources.getDimension(R.dimen.height_qr_box_view));

        typedArray.recycle();
        boxView = (FrameLayout) findViewById(R.id.fl_box_view);
        textView = (TextView) findViewById(R.id.tv_desc);
        textView.setText("Captura Cédula Nueva");
        setBackgroundResource(R.color.qr_code_view_mask);

        btn_switch = (CheckBox) findViewById(R.id.btn_switch);
        btn_switch_portrait = (CheckBox) findViewById(R.id.btn_switch_portrait);

        if (oPreferences.getBoolean(PreferencesUtils.KEY_PORTRAIT)) {
            btn_switch_portrait.setVisibility(View.VISIBLE);
        } else {
            btn_switch.setVisibility(View.VISIBLE);
        }

        if (oPreferences.getString(PreferencesUtils.KEY_TYPE) != null) {

            if (oPreferences.getString(PreferencesUtils.KEY_TYPE).equalsIgnoreCase(QRView.QR)) {
                drawGuideQrOverlay();
            } else {
                drawGuide();
            }

        } else {
            drawGuide();
        }
        oPreferences.setString(PreferencesUtils.KEY_TYPE, null);
        oPreferences.setBoolean(PreferencesUtils.KEY_PORTRAIT, false);
    }

    public void pressOnClickBackButton(final Activity activity) {
        btn_switch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
        btn_switch_portrait.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.btn_light).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    checkBox.setText(R.string.action_light_off_desc);
                } else {
                    checkBox.setText(R.string.action_light_on_desc);
                }
                if (lightOnClickListener != null) {
                    lightOnClickListener.onClick(view);
                }
            }
        });
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exlore_line_move);
        animation.setInterpolator(new LinearInterpolator());
        findViewById(R.id.img_scan_line).setAnimation(animation);
    }

    @SuppressLint("NewApi")
    private void drawGuide() {

        textView.setText("Captura Cédula Antigua");
//        checkBox.setText(QRView.PDF_417);

        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int hudH = size.y;
        int hudW = size.x;

        double proportionOfScreen = 0.85;

        double maxFrameH = hudH * proportionOfScreen;
        double maxFrameW = hudW * proportionOfScreen;

        double hudRelationAspect = maxFrameH / maxFrameW;
        double ciRelationAspect = 0.258;
        double PDFFingerRelation = 0.75;
        double SeparationRelation = 0.02;

        double frameH = 0;
        double frameW = 0;

        if (hudRelationAspect <= ciRelationAspect) { // mas largo
            frameH = maxFrameH;
            frameW = frameH / ciRelationAspect;

        } else {// mas alto
            frameW = maxFrameW;
            frameH = frameW * ciRelationAspect;
        }

        double marginW = (hudW - frameW) / 2;
        double marginH = (hudH - frameH) / 2;

        Point up_left_pdf = new Point((int) marginW, (int) marginH);
        Point down_rigth_pdf = new Point((int) (marginW + frameW
                * PDFFingerRelation), (int) (marginH + frameH));

        Point up_left_finger = new Point((int) (marginW + frameW
                * (PDFFingerRelation + SeparationRelation)), (int) marginH);
        Point down_rigth_finger = new Point((int) (marginW + frameW),
                (int) (marginH + frameH));


        Bitmap.Config conf = Bitmap.Config.ARGB_8888;

        Bitmap bmp = Bitmap.createBitmap(hudW, hudH, conf);

        Canvas canvas = new Canvas(bmp);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.TRANSPARENT);
        paint.setAlpha(2);
        paint.setStrokeWidth(5);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(200, 176, 130));

        canvas.drawRect(up_left_pdf.x, up_left_pdf.y, down_rigth_pdf.x,
                down_rigth_pdf.y, paint);
        canvas.drawRect(up_left_finger.x, up_left_finger.y,
                down_rigth_finger.x, down_rigth_finger.y, paint);

        //qr

        proportionOfScreen = 0.30;

        maxFrameH = hudH * proportionOfScreen;
        maxFrameW = hudW * proportionOfScreen;

        hudRelationAspect = maxFrameH / maxFrameW;
        ciRelationAspect = 0.958;
        PDFFingerRelation = 0.1;// altura
        SeparationRelation = 0.02;

        if (hudRelationAspect <= ciRelationAspect) { // mas largo
            frameH = maxFrameH;
            frameW = frameH / ciRelationAspect;

        } else {// mas alto
            frameW = maxFrameW;
            frameH = frameW * ciRelationAspect;
        }

        marginW = (hudW - frameW) / 2;
        marginH = (hudH - frameH) / 2;


        Point up_left_qr = new Point((int) (marginW + frameW
                * (PDFFingerRelation + SeparationRelation)), (int) marginH);
        Point down_rigth_qr = new Point((int) (marginW + frameW),
                (int) (marginH + frameH));

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(197, 213, 228));

        canvas.drawRect(up_left_qr.x, up_left_qr.y,
                down_rigth_qr.x, down_rigth_qr.y, paint);

        BitmapDrawable br = new BitmapDrawable(getResources(), bmp);

        int androidVersion = Integer.valueOf(android.os.Build.VERSION.SDK);

        if (androidVersion < 16) {
            boxView.setBackgroundDrawable(br);
        } else {
            boxView.setBackground(br);
        }

    }


    @SuppressLint("NewApi")
    private void drawGuideQrOverlay() {

        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int hudH = size.y;
        int hudW = size.x;

        double proportionOfScreen = 0.30;

        double maxFrameH = hudH * proportionOfScreen;
        double maxFrameW = hudW * proportionOfScreen;

        double hudRelationAspect = maxFrameH / maxFrameW;
        double ciRelationAspect = 0.958;
        double PDFFingerRelation = 0.1;// altura
        double SeparationRelation = 0.02;

        double frameH = 0;
        double frameW = 0;

        if (hudRelationAspect <= ciRelationAspect) { // mas largo
            frameH = maxFrameH;
            frameW = frameH / ciRelationAspect;

        } else {// mas alto
            frameW = maxFrameW;
            frameH = frameW * ciRelationAspect;
        }

        double marginW = (hudW - frameW) / 2;
        double marginH = (hudH - frameH) / 2;

        Point up_left_finger = new Point((int) (marginW + frameW
                * (PDFFingerRelation + SeparationRelation)), (int) marginH);
        Point down_rigth_finger = new Point((int) (marginW + frameW),
                (int) (marginH + frameH));

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;

        Bitmap bmp = Bitmap.createBitmap(hudW, hudH, conf);

        Canvas canvas = new Canvas(bmp);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.TRANSPARENT);
        paint.setAlpha(100);
        paint.setStrokeWidth(8);

        //borde
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(197, 213, 228));

        canvas.drawRect(up_left_finger.x, up_left_finger.y,
                down_rigth_finger.x, down_rigth_finger.y, paint);


        BitmapDrawable br = new BitmapDrawable(getResources(), bmp);

        int androidVersion = Integer.valueOf(android.os.Build.VERSION.SDK);

        if (androidVersion < 16) {
            boxView.setBackgroundDrawable(br);
        } else {
            boxView.setBackground(br);
        }
    }


//drawguide solo pdf

    @Override
    public void onDraw(Canvas canvas) {
//        /** Draw the exterior dark mask*/
//        int width = getWidth();
//        int height = getHeight();
//        float boxViewX = boxView.getX();
//        float boxViewY = boxView.getY();
//
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColor(maskColor);
//        canvas.drawRect(0, boxViewY, boxViewX, boxViewY + boxViewHeight, paint);// left rect
//        canvas.drawRect(boxViewX + boxViewWidth, boxViewY, width, boxViewY + boxViewHeight, paint);// right rect
//        canvas.drawRect(0, 0, width, boxViewY, paint);// top rect
//        canvas.drawRect(0, boxViewY + boxViewHeight, width, height, paint);// bottom rect
//
//        /** Draw the border lines*/
//        paint.setColor(borderColor);
//        canvas.drawLine(boxViewX, boxViewY, boxViewX + boxViewWidth, boxViewY, paint);
//        canvas.drawLine(boxViewX, boxViewY, boxViewX, boxViewY + boxViewHeight, paint);
//        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX, boxViewY + boxViewHeight, paint);
//        canvas.drawLine(boxViewX + boxViewWidth, boxViewY + boxViewHeight, boxViewX + boxViewWidth, boxViewY, paint);
//
//        /** Draw the corners*/
//        Rect rect = new Rect();
//        rect.set((int) boxViewX, (int) boxViewY, (int) boxViewX + boxViewWidth, (int) boxViewY + boxViewHeight);
//        paint.setColor(cornerColor);
//
//        /** top the corners*/
//        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.top - cornerSize + cornerOffset, rect.left + cornerLength - cornerSize + cornerOffset, rect.top + cornerOffset, paint);
//        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.top - cornerSize + cornerOffset, rect.left + cornerOffset, rect.top + cornerLength - cornerSize + cornerOffset, paint);
//        canvas.drawRect(rect.right - cornerLength + cornerSize - cornerOffset, rect.top - cornerSize + cornerOffset, rect.right + cornerSize - cornerOffset, rect.top + cornerOffset, paint);
//        canvas.drawRect(rect.right - cornerOffset, rect.top - cornerSize + cornerOffset, rect.right + cornerSize - cornerOffset, rect.top + cornerLength - cornerSize + cornerOffset, paint);
//
//        /** bottom the corners*/
//        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.bottom - cornerOffset, rect.left + cornerLength - cornerSize + cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
//        canvas.drawRect(rect.left - cornerSize + cornerOffset, rect.bottom - cornerLength + cornerSize - cornerOffset, rect.left + cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
//        canvas.drawRect(rect.right - cornerLength + cornerSize - cornerOffset, rect.bottom - cornerOffset, rect.right + cornerSize - cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
//        canvas.drawRect(rect.right - cornerOffset, rect.bottom - cornerLength + cornerSize - cornerOffset, rect.right + cornerSize - cornerOffset, rect.bottom + cornerSize - cornerOffset, paint);
    }

    public void setDescription(String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    public void setPickImageListener(OnClickListener onClickListener) {
        if (onClickListener != null) {
            findViewById(R.id.btn_photo).setOnClickListener(onClickListener);
        }
    }

    public void setProduceQRListener(OnClickListener onClickListener) {
        if (onClickListener != null) {
            findViewById(R.id.btn_produce).setOnClickListener(onClickListener);
        }
    }

    public void setLightOnClickListener(OnClickListener lightOnClickListener) {
        this.lightOnClickListener = lightOnClickListener;
    }
}