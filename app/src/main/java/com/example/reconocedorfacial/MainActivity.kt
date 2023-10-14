package com.example.reconocedorfacial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import java.lang.Math.abs

class MainActivity : ComponentActivity() {

    lateinit var imageView: ImageView
    lateinit var defaultbitmap: Bitmap
    lateinit var temporarybitmap: Bitmap
    lateinit var eyepatchbitmap: Bitmap
    lateinit var canvas: Canvas
    lateinit var textocara: TextView


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
        defaultbitmap = BitmapFactory.decodeResource(resources, R.drawable.cuadrada2, bitmapOptions)
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
        Log.d("ReconocedorFacial" ,"Detectando caras")
        val textocara = findViewById<TextView>(R.id.textoCara)
        for(i in 0 until sparseArray.size()){
            // Obtiene la cara actual en el SparseArray
            val face = sparseArray.valueAt(i)

            // Obtiene las coordenadas del rectángulo que rodea la cara
            val left = face.position.x
            val top = face.position.y
            val right = left + face.width
            val bottom = top + face.height

            // Crea un objeto RectF para representar el rectángulo
            val rectF = RectF(left, top, right, bottom)

            // Dibuja un rectángulo redondeado alrededor de la cara en el lienzo
            canvas.drawRoundRect(rectF, 2f, 2f, rectPaint)

            // Obtiene el ancho y el alto de la cara
            val ancho = face.width.toFloat()
            Log.d("ReconocedorFacial" ,"width: $ancho")
            val alto = face.height.toFloat()
            Log.d("ReconocedorFacial" ,"height: $alto")




            // Calcula la relación ancho/alto de la cara
            val aspectRatio1 = abs(ancho - alto) // Relación ancho/alto
            Log.d("ReconocedorFacial" ,"Aspectratio: $aspectRatio1")
            val aspectRatio2 = ancho / alto // Relación ancho/alto
            Log.d("ReconocedorFacial" ,"Aspectratio dividido: $aspectRatio2")
            // Clasificación de tipos de rostro basada en la relación ancho/alto
            val isCuadrada = aspectRatio1 <= 0.1
            val isRedonda = aspectRatio2 <= 0.7
            val isOvalada = aspectRatio2 >= 0.8 && aspectRatio2 <= 1.2
            val isAlargada = aspectRatio2 >= 1.3


            if (isCuadrada) {
                // La cara se clasifica como cuadrada
                textocara.setText("La cara se clasifica como cuadrada")
                System.out.println("La cara se clasifica como cuadrada")
                Log.d("ReconocedorFacial" ,"Cuadrada")
            } else if (isRedonda) {
                // La cara se clasifica como redonda
                textocara.setText("La cara se clasifica como redonda")
                System.out.println("La cara se clasifica como redonda")
                Log.d("ReconocedorFacial" ,"Redonda")
            } else if (isOvalada) {
                // La cara se clasifica como ovalada
                textocara.setText("La cara se clasifica como ovalada")
                System.out.println("La cara se clasifica como ovalada")
                Log.d("MiApp", "Ovalada")
            } else if (isAlargada) {
                // La cara se clasifica como alargada
                textocara.setText("La cara se clasifica como alargada")
                System.out.println("La cara se clasifica como alargada")
                Log.d("ReconocedorFacial", "Alargada")
            }
            detectLandmarks(face)

        }
    }
    private fun isCuadrada(face: Face): Boolean {
        // Identificar una cara cuadrada podría basarse en la proporción entre el ancho y el alto
        val ancho = abs(face.position.x + face.width - face.position.y)
        val alto = abs(face.position.y + face.height - face.position.x)

        val aspectRatio = ancho / alto

        // Un valor de aspectRatio menor que 0.7 podría ser indicativo de una cara cuadrada
        return aspectRatio <= 0.7
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
