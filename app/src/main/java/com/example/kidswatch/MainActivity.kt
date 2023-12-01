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
import androidx.compose.material3.DrawerDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kidswatch.ui.theme.KidsWatchTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.rememberImagePainter


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            KidsWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
        composable("draw") { DrawScreen(navController) }
        composable(
            "result/{result}",
            arguments = listOf(navArgument("result") { type = NavType.StringType })
        ) { backStackEntry ->
            ResultScreen(backStackEntry.arguments?.getString("result"))
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectUris by remember { mutableStateOf<MutableList<Uri?>?>(mutableListOf()) }
    val scope = rememberCoroutineScope()
    val maxUrisSize = 3
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "메인 화면",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        //카메라 퍼미션 확인
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
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
                    context,
                    "Camera permission is required to take photos",
                    Toast.LENGTH_LONG
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
                        //이미지 uri들을 selectUris에 하나씩 저장
                        selectUris?.let { uris ->
                            val newList = uris.toMutableList()
                            newList.add(uri)
                            selectUris = if (newList.size > maxUrisSize) {
                                newList.takeLast(maxUrisSize).toMutableList()
                            } else {
                                newList
                            }
                        }

                    }
                }
            } else {
                Log.e("사진 촬영 실패", "실패실패실패실패실패실패")
            }
        }



        // 사용자가 버튼을 눌렀을 때 카메라 실행
        Button(onClick = {
            if (hasCameraPermission) {
                val uri = createImageUri()
                cameraLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }) {
            Text("카메라로 사진 찍기")
        }

        // 찍은 사진이 있을 때만 이미지 표시
        capturedBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
        }


//// 사용자가 버튼을 눌렀을 때 포토피커 실행
//        Button(onClick = {
//            singlePhotoLoader.launch(null)
//        }) {
//            Text("사진 가져오기")
//        }

        Button(onClick = { navController.navigate("draw") }) {
            Text("그림판 이동")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("result/분석결과") }) {
            Text("진단하기")
        }
    }
}

@Composable
fun DrawScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "카메라 화면",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = { navController.navigate("result/분석결과") }) {
            Text("분석 결과 확인")
        }
    }
}

@Composable
fun ResultScreen(result: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "결과 화면",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(text = "분석 결과: $result")
    }
}


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    KidsWatchTheme {
//        MainScreen(navController)
//    }
//}