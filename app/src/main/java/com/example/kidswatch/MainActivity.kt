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
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("") }
    val orange = Color(0xFFFFA500)
    var isMenuOpen by remember { mutableStateOf(false) }
    val ad = "https://mind.amc.seoul.kr/asan/depts/mind/K/deptMain.do"
    var adBox by remember { mutableStateOf(false) }

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
                    result = postAndGetResult(file)
                    Log.d("result", result)
                    adBox = true
                }
            }
        } else {
            Log.e("사진 촬영 실패", "실패실패실패실패실패실패")
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    imageUri = uri
                    val file = uriToFile(context, uri)
                    result = postAndGetResult(file)
                    adBox = true
                }
            }
        }
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    isMenuOpen = pan.x > 0
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = orange, shape = RoundedCornerShape(10.dp))
                    .size(30.dp, 90.dp)
            ) {
                Text(
                    text = "Kidswatch",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            //selectUri가 null이 아닐 때만 이미지 표시 coil 라이브러리 사용
            imageUri?.let { uri ->
                val bitmap = uriToBitmap(uri, context)
                bitmap?.asImageBitmap()?.let {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        modifier = Modifier
                            .size(500.dp)
                            .padding(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
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
                        Text("사진 찍기", color = Color.Black)
                    }
                    Button(
                        onClick = {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = buttonColors(orange),
                        shape = RoundedCornerShape(5.dp),
                    ) {
                        Text("사진 가져오기", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val uri = URLEncoder.encode(imageUri?.toString(), "UTF-8")
                        navController.navigate("result/${result}/${uri}")
                    },
                    colors = buttonColors(orange),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .size(300.dp, 50.dp)
                ) {
                    Text("진단하기", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(10.dp))
                AdPlaces("심리상담 병원", ad)
            }
        }
        SlideMenu(
            isOpen = isMenuOpen,
            onOpen = { isMenuOpen = true }
        ) { modifier ->
            // Content of the menu
            Box(
                modifier
                    .width(200.dp)
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                Row {
                    Column(
                        modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "메뉴 목록",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 25.sp,
                        )
                        MenuList("상담 신청", "https://www.mykpcc.com/1702233193")
                        MenuList(
                            "가까운 병원",
                            "https://www.google.com/search?sca_esv=587945461&tbs=lf:1,lf_ui:2&tbm=lcl&sxsrf=AM9HkKmuVUCMJbWXpysjddwXOeU1XxouXw:1701766776289&q=%EC%A0%95%EC%8B%A0%EA%B3%BC+%EB%B3%91%EC%9B%90&rflfq=1&num=10&sa=X&ved=2ahUKEwi8ocOh9_eCAxVYg1YBHQ3eDLgQjGp6BAgVEAE&biw=1536&bih=739&dpr=1.25#rlfi=hd:;si:;mv:[[37.5738732,126.9851049],[37.5383335,126.9335744]];tbs:lrf:!1m4!1u3!2m2!3m1!1e1!1m4!1u2!2m2!2m1!1e1!2m1!1e2!2m1!1e3!3sIAE,lf:1,lf_ui:2"
                        )
                        MenuList("간단 검사", "http://htp-test.com/mini/htp_test01_00.htm")
                        MenuList("고객문의", "https://www.notion.so/gonuai-seoul/KidWatch-58178cb2fe214fa68a8b93719338c4a3?pvs=4")
                    }
                }
            }
        }
    }
    if (adBox) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LaunchedEffect(true) {
                delay(10000) // 3 seconds delay
                adBox = false
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

@Composable
fun SlideMenu(
    isOpen: Boolean,
    onOpen: () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val orange = Color(0xFFFFA500)
    val offset = animateDpAsState(if (isOpen) -200.dp else (-380).dp, label = "")

    Box(
        modifier = Modifier
            .offset(x = offset.value)
            .fillMaxSize()
            .background(orange.copy(alpha = 0.5f))
            .clickable(onClick = onOpen)
    ) {
        content(Modifier.align(Alignment.TopEnd))
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            Image(
                painter = painterResource(id = if (isOpen) R.drawable.baseline_arrow_left_24 else R.drawable.baseline_arrow_right_24),
                contentDescription = "열기화살",
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
fun AdPlaces(adName: String, uriString: String) {
    var isClicked by remember { mutableStateOf(false) }
    val orange = Color(0xFFFFA500)
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(10.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Red,
                        Color(1f, 0.5f, 0f), // Orange
                        Color.Yellow,
                        Color.Green,
                        Color.Blue,
                        Color(0.29f, 0f, 0.51f), // Indigo
                        Color(0.54f, 0.17f, 0.88f) // Violet
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable {
                // 광고를 클릭할 때 수행할 작업 추가
                isClicked = true
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                context.startActivity(intent)
            }
    ) {
        if (isClicked) {
            // 클릭되었을 때의 UI
            // 예를 들어, 광고 클릭 후에 할 작업을 여기에 추가
            ShowToasts("업체 웹페이지로 이동합니다")
        }

        // 광고 텍스트
        Text(
            text = adName,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        )
    }
}

@Composable
fun MenuList(menu: String, uriString: String) {
    var isClicked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (isClicked) {
        // 클릭되었을 때의 UI
        // 예를 들어, 광고 클릭 후에 할 작업을 여기에 추가
        ShowToasts("${menu}로 이동합니다.")
    }

    // 광고 텍스트
    Text(
        text = menu,
        color = Color.Black,
        style = MaterialTheme.typography.titleLarge,
        fontSize = 25.sp,
        modifier = Modifier
            .clickable {
                // 광고를 클릭할 때 수행할 작업 추가
                isClicked = true
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                context.startActivity(intent)
            }
    )
}


@Composable
fun ShowToasts(message: String) {
    val context = LocalContext.current
    val toast = remember { Toast.makeText(context, message, Toast.LENGTH_SHORT) }
    toast.show()
}