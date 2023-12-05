package com.example.kidswatch

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kidswatch.ui.theme.KidsWatchTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.ui.graphics.asImageBitmap
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.net.URLEncoder


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            KidsWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
//        composable("draw") { DrawScreen(navController) }
        composable("result/{result}/{uri}") {
            val result = it.arguments?.getString("result")
            val uri = it.arguments?.getString("uri")
            ResultScreen(result, uri)
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
//    var selectUris by remember { mutableStateOf<MutableList<Uri?>?>(mutableListOf()) }
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("") }
    val orange = Color(0xFFFFA500)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = {},
                colors = buttonColors(orange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .size(80.dp)
            ) {
                Text(
                    text = "Kidswatch",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        //카메라 퍼미션 확인
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        //카메라 퍼미션 확인 런쳐
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                hasCameraPermission = true
            } else {
                Toast.makeText(
                    context, "Camera permission is required to take photos", Toast.LENGTH_LONG
                ).show()
            }
        }

        //카메라로 찍은 파일 Uri로 바꿔줌
        fun createImageUri(): Uri {
            val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
            ).also { uri ->
                imageUri = uri
            }
        }


        //카메라 런쳐
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success: Boolean ->
            if (success) {
                // 사진 촬영 성공, imageUri에 이미지가 저장됨
                scope.launch {
                    imageUri?.let { uri ->
                        val file = uriToFile(context, uri)

//                        val inputStream = context.contentResolver.openInputStream(uri)
//                        val file = File(context.cacheDir, "image.png")
//                        inputStream?.use { input ->
//                            file.outputStream().use { output ->
//                                input.copyTo(output)
//                            }
//                        }
                        result = postAndGetResult(file)
                        Log.d("result", result)
                    }
                }
            } else {
                Log.e("사진 촬영 실패", "실패실패실패실패실패실패")
            }
        }

        //selectUri가 null이 아닐 때만 이미지 표시 coil 라이브러리 사용
        imageUri?.let { uri ->
            val bitmap = uriToBitmap(uri, context)
            bitmap?.asImageBitmap()?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
            }
        }
        Text(text = "분석 결과: $result")

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,

                ) {
                // 사용자가 버튼을 눌렀을 때 카메라 실행
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            val uri = createImageUri()
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }, colors = buttonColors(orange),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("카메라로 사진 찍기", color = Color.Black)
                }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        if (uri != null) {
                            scope.launch {
                                imageUri = uri
                                val file = uriToFile(context, uri)
//                                val inputStream = context.contentResolver.openInputStream(uri)
//                                val file = File(context.cacheDir, "image.png")
//                                inputStream?.use { input ->
//                                    file.outputStream().use { output ->
//                                        input.copyTo(output)
//                                    }
//                                }
                                result = postAndGetResult(file)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }, colors = buttonColors(orange),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text("사진 가져오기", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val uri = URLEncoder.encode(imageUri?.toString(), "UTF-8")
                    navController.navigate("result/${result}/${uri}")
                },
                colors = buttonColors(orange),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.fillMaxWidth(5f)
            ) {
                Text("진단하기", color = Color.Black)
            }
        }
    }
}


fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "image.png")
    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file
}

suspend fun postAndGetResult(file: File): String = withContext(Dispatchers.IO) {
    val url = "http://192.168.1.22:5000/predict" //trend wifi url
    val client = OkHttpClient()

    val reqestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "image",
            "image.png",
            RequestBody.create(MediaType.parse("image/*"), file)
        )
        .build()

    val request = Request.Builder()
        .url(url)
        .post(reqestBody)
        .build()

    try {
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body()?.string()

            val gson = Gson()
            val result = gson.fromJson(responseBody, Result::class.java)
            Log.d("result", result.toString())
            return@withContext result.result
        } else {
            Log.d("result", "실패")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } as String
}

data class Result(
    @SerializedName("predicted_class")
    val result: String
)

fun uriToBitmap(uri: Uri, context: Context): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


@Preview(showBackground = true)
@Composable
fun Preview() {
    val navController = rememberNavController()
    MainScreen(navController = navController)
}
