package com.example.reconocedorfacial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceLandmark

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
        defaultbitmap = BitmapFactory.decodeResource(resources, R.drawable.redonda  , bitmapOptions)
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
        val textocara = findViewById<TextView>(R.id.textoCara)
        val textoAnchura = findViewById<TextView>(R.id.textoAnchura)
        for (i in 0 until sparseArray.size()) {
            val face = sparseArray.valueAt(i)
            val left = face.position.x
            val top = face.position.y
            val right = left + face.width
            val bottom = top + face.height
            val rectF = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rectF, 2f, 2f, rectPaint)
            val ancho = face.width.toFloat()
            val alto = face.height.toFloat()



            // Obtenemos los landmarks de la cara
            val landmarks = face.landmarks

            // Calculamos las distancias entre algunos landmarks clave
            val distanciaOjos = calcularDistancia(landmarks[FaceLandmark.LEFT_EYE].position, landmarks[FaceLandmark.RIGHT_EYE].position)
            val distanciaNarizBoca = calcularDistancia(landmarks[FaceLandmark.NOSE_BASE].position, landmarks[FaceLandmark.MOUTH_BOTTOM].position)

            // Calculamos la distancia entre los pómulos (si están disponibles)


             var distanciaPomulos = calcularDistancia(landmarks[FaceLandmark.LEFT_CHEEK].position, landmarks[FaceLandmark.RIGHT_CHEEK].position)
            val aspectRatio = alto / distanciaPomulos

            // Calculamos la distancia entre el pómulo izquierdo y la nariz (si están disponibles)


              var distanciaPomuloIzquierdoNariz = calcularDistancia(landmarks[FaceLandmark.LEFT_CHEEK].position, landmarks[FaceLandmark.NOSE_BASE].position)


            // Usamos estas distancias para ayudar a determinar la forma de la cara
            when {
               aspectRatio <= 3.5 -> textocara.setText("Ancha")
              aspectRatio >= 3.5 -> textocara.setText("Alargada")
                /* aspectRatio < 0.95 && aspectRatio >= 0.85 -> textocara.setText("Ovalada")
               aspectRatio < 0.85 -> textocara.setText("Redonda")
               distanciaPomulos / ancho > 0.6 -> textocara.setText("Redonda")
               distanciaPomulos / ancho <= 0.6 -> textocara.setText("Cuadrada")*/


            }
            val altura = face.height

            textoAnchura.setText("Ancho: $distanciaPomulos" + " Altura: $altura" + "Relacion: $aspectRatio")
        detectLandmarks(face)
        }
    }

    // Función para calcular la distancia entre dos puntos
    private fun calcularDistancia(punto1: PointF, punto2: PointF): Float {
        return Math.sqrt(Math.pow((punto2.x - punto1.x).toDouble(), 2.0) + Math.pow((punto2.y - punto1.y).toDouble(), 2.0)).toFloat()
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
