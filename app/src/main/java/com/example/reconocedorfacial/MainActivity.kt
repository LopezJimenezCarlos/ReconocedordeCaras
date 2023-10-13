package com.example.reconocedorfacial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable

import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.reconocedorfacial.ui.theme.ReconocedorFacialTheme
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector

class MainActivity : ComponentActivity() {

    lateinit var imageView: ImageView
    lateinit var defaultbitmap: Bitmap
    lateinit var temporarybitmap: Bitmap
    lateinit var eyepatchbitmap: Bitmap
    lateinit var canvas: Canvas

    // Paint para dibujar rectángulos alrededor de las caras detectadas
    val rectPaint = Paint()
    // Detector de caras
    val faceDetector: FaceDetector
        get() = initializeDetector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        imageView = findViewById(R.id.imageView)
    }

    fun processImage(view: View) {
        // Opciones para decodificar el bitmap
        val bitmapOptions = BitmapFactory.Options().apply {
            inMutable = true
        }
        initializeBitmap(bitmapOptions)
        createRectanglePaint()

        // Crear un lienzo (Canvas) para dibujar
        canvas = Canvas(temporarybitmap).apply {
            drawBitmap(defaultbitmap, 0f, 0f, null)
        }
        if (!faceDetector.isOperational){
            System.out.println("Face Detector could not be set up on your device")
        }else {
            val frame = Frame.Builder().setBitmap(defaultbitmap).build()
            val sparseArray = faceDetector.detect(frame)
            detectFaces(sparseArray)
            imageView.setImageDrawable(BitmapDrawable(resources, temporarybitmap))
            faceDetector.release()
        }
    }

    // Inicializar los bitmaps
    private fun initializeBitmap(bitmapOptions: BitmapFactory.Options) {
        defaultbitmap = BitmapFactory.decodeResource(resources, R.drawable.yo, bitmapOptions)
        temporarybitmap = Bitmap.createBitmap(defaultbitmap.width, defaultbitmap.height, Bitmap.Config.RGB_565)
        eyepatchbitmap = BitmapFactory.decodeResource(resources, R.drawable.rojo, bitmapOptions)
    }

    // Configurar el Paint para dibujar los rectángulos
    private fun createRectanglePaint() {
        rectPaint.apply {
            style = Paint.Style.STROKE
            color = Color.Magenta.hashCode()
            strokeWidth = 5f
        }
    }

    // Inicializar el detector de caras
    private fun initializeDetector(): FaceDetector {
        return FaceDetector.Builder(this)
            .setTrackingEnabled(false)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build()
    }

    // Detectar caras en la imagen y dibujar rectángulos
    private fun detectFaces(sparseArray: SparseArray<Face>) {
        for(i in 0 until sparseArray.size()){
            val face = sparseArray.valueAt(i)
            val left = face.position.x
            val top = face.position.y
            val right = left + face.width
            val bottom = top + face.height

            val rectF = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rectF, 2f, 2f, rectPaint)

            detectLandmarks(face)
        }
    }

    // Detectar landmarks en las caras y dibujarlos
    private fun detectLandmarks(face: Face) {
        for (landmark in face.landmarks){
            val cx = landmark.position.x
            val cy = landmark.position.y

            canvas.drawCircle(cx, cy, 10f, rectPaint)
            drawLandmarkType(landmark.type, cx, cy)
        }
    }

    // Dibujar el tipo de landmark
    private fun drawLandmarkType(landmarkType: Int, cx: Float, cy: Float) {
        val type = landmarkType.toString()
        rectPaint.textSize = 30f
        canvas.drawText(type, cx, cy, rectPaint)
    }
}
