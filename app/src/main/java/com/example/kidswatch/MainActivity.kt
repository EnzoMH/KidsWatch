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
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import java.io.FileOutputStream
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            KidsWatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(onClick = {})
            {
                Text(
                    text = "Kidswatch",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Button(onClick = {}, modifier = Modifier.padding(8.dp)) {
                Text("메뉴")
            }
        }

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

        Column(

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 사진 보여주는 곳
            selectUris?.lastOrNull()?.let { uri ->
                val headBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val decodeBitmap = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver, uri
                        )
                    )
                    decodeBitmap
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                Image(
                    bitmap = headBitmap.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(300.dp)
                        .clickable {
                            // 클릭한 이미지의 uri를 제거
                            selectUris?.let { currentUris ->
                                val updatedUris = currentUris
                                    .filter { it != uri }
                                    .toMutableList()
                                selectUris = updatedUris
                            }
                        }
                )
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

// 사용자가 버튼을 눌렀을 때 포토피커 실행
        Button(onClick = {})
        {
            Text("사진 가져오기")
        }

        Button(onClick = { navController.navigate("draw") }) {
            Text("그림판 이동")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("result/분석결과") }) {
            Text("진단하기")
        }
    }
}

// 선을 표현하는 데이터 클래스
data class Line(
    var path: Path = Path(),  // 선의 경로
    var start: Offset,  // 선의 시작점
    var end: Offset  // 선의 끝점
)

@Composable
fun DrawScreen(navController: NavController) {
    var lines by remember { mutableStateOf(mutableListOf<Line>()) }  // 그려진 선들의 목록
    var currentLine by remember { mutableStateOf<Line?>(null) }  // 현재 그리고 있는 선
    var eraseMode by remember { mutableStateOf(false) }  // 지우개 모드인지 여부
    var eraserPosition by remember { mutableStateOf<Offset?>(null) }  // 지우개의 위치
    val strokeWidth = 10f  // 선의 굵기
    val color = Color.Black  // 선의 색상
    val eraserRadius = 50f  // 지우개의 반지름

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        if (!eraseMode) {
                            // 그리기 모드일 때는 선을 새로 시작
                            currentLine = Line(start = startOffset, end = startOffset).apply {
                                path.moveTo(startOffset.x, startOffset.y)
                            }
                        } else {
                            // 지우개 모드일 때는 터치한 지점을 통과하는 모든 선을 제거
                            lines = lines
                                .filterNot { it.isTouching(startOffset, eraserRadius) }
                                .toMutableList()
                        }
                        // 지우개의 위치를 업데이트
                        eraserPosition = startOffset
                    },
                    onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        if (eraseMode) {
                            // 지우개 모드일 때는 터치한 지점을 통과하는 모든 선을 제거
                            lines = lines
                                .filterNot { it.isTouching(change.position, eraserRadius) }
                                .toMutableList()
                        } else {
                            // 그리기 모드일 때는 선을 계속 그림
                            currentLine?.let {
                                it.end = change.position
                                it.path.lineTo(change.position.x, change.position.y)
                                lines = lines
                                    .toMutableList()
                                    .apply {
                                        add(it)
                                    }
                                currentLine =
                                    Line(start = change.position, end = change.position).apply {
                                        path.moveTo(change.position.x, change.position.y)
                                    }
                            }
                        }
                        // 지우개의 위치를 업데이트
                        eraserPosition = change.position
                    },
                    onDragEnd = {
                        // 드래그가 끝나면 지우개의 위치를 null로 설정
                        eraserPosition = null
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = {
                for (line in lines) {
                    // 모든 선을 그림
                    drawPath(
                        path = line.path,
                        color = color,
                        style = Stroke(width = strokeWidth)
                    )
                }
                if (eraseMode) {
                    // 지우개 모드일 때는 지우개의 범위를 표시
                    eraserPosition?.let {
                        drawCircle(
                            color = Color.Gray,
                            radius = eraserRadius,
                            center = it,
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
        )
        Button(onClick = {
            // 지우기 버튼을 누르면 모든 선을 제거
            lines = mutableListOf()
        }) {
            Text(text = "지우기")
        }
        Button(onClick = { eraseMode = !eraseMode }, modifier = Modifier.offset(y = 50.dp))
        {
            // 지우개 모드와 그리기 모드를 전환하는 버튼
            Text(text = if (eraseMode) "그리기 모드로 변경" else "지우개 모드로 변경")
        }

        Button(onClick = {
            // 분석 결과 화면으로 이동
            navController.navigate("result/분석결과")

        }) {
            Text("분석 하기")
        }

    }
}


// 선이 특정 지점을 터치하는지 판단하는 함수
fun Line.isTouching(point: Offset, radius: Float): Boolean {
    val distanceToStart = sqrt((point.x - start.x).pow(2) + (point.y - start.y).pow(2))
    val distanceToEnd = sqrt((point.x - end.x).pow(2) + (point.y - end.y).pow(2))
    val lineLength = sqrt((start.x - end.x).pow(2) + (start.y - end.y).pow(2))

    // 터치 지점이 선의 양 끝점 사이에 있거나 근처에 있으면 터치한 것으로 판단
    return distanceToStart + distanceToEnd <= lineLength * 1.1 + 2 * radius
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


@Preview(showBackground = true)
@Composable
fun Preview() {
    val navController = rememberNavController()
    MainScreen(navController = navController)
}
