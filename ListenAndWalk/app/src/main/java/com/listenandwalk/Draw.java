package com.listenandwalk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

class Draw extends View
{
    Context c;
    Rect r;
    String str;
    RectF rtf;
    public Draw(Context context,Rect rect,String text)
    {
        super(context);
        c=context;
        r=rect;
        //rect=r;
        str=text;
        rtf = new RectF(r);
    }
    Paint boundaryPaint = new Paint();
    Paint textPaint = new Paint();

    private void init()
    {
        boundaryPaint.setColor(Color.RED);
        boundaryPaint.setStrokeWidth(10f);
        boundaryPaint.setStyle(Paint.Style.STROKE);

        textPaint.setColor(Color.RED);
        textPaint.setTextSize(50f);
        textPaint.setStyle(Paint.Style.STROKE);

    }
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
         canvas.drawText(str, ((float) r.centerX()), ((float) r.centerY()),textPaint);
         //canvas.drawRoundRect(rtf,r.centerX(),r.centerY(),boundaryPaint);
         canvas.drawRect(((float) r.left), ((float) r.top), ((float) r.right), ((float) r.bottom),boundaryPaint);

     }
}
