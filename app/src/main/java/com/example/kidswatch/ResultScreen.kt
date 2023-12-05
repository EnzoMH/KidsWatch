package com.example.kidswatch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

@Composable
fun ResultScreen(result: String?, uri: String?) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val script = when (result) {
        "위험" -> "정신적 외상 스트레스가 있으며, 현재 갈등상황에 놓여있습니다. 불만 또한 있는 상태입니다.외부 환경에 민감하며 지나치면 자신을 통제하려합니다."
        "위험(열쇠)" -> "저항적이고 부정적입니다. 무언가 하려는 동기가 적고, 잠재적으로 정서가 폭발할 가능성이 있습니다. 주의를 요망합니다."
        "위험(그루터기)" -> "현재 심하게 유약하고 위축되어 있습니다. 우울감 또한 잠재되어있어 주의를 요망합니다."
        "위험(둥근수관)" -> "아동의 경우는 의존적이며 호기심이 풍부합니다. 또한 성취욕구가 강합니다. 성인의 경우 현재 상황이 혼란스러운 상태입니다"
        "보통" -> "정서적으로 안정된 상태입니다."
        "경고(다람쥐)" -> "자아정체감이 덜 발달되어 있거나 발달되어 가는 과정에 있을 수 있습니다. 환경의 영향을 받으며 불안하거나 무력감을 느낄 때가 있습니다."
        "경고(나뭇잎)" -> "안정감에 대한 욕구가 강한 당신은 현재 중심을 잃고 외부자극에 영향을 받고 있는 상태 입니다."
        else -> "자아정체감이 덜 발달되어 있거나 발달되어 가는 과정에 있을 수 있습니다. 환경의 영향을 받으며 불안하거나 무력감을 느낄 때가 있습니다. 안정감에 대한 욕구가 강한 당신은 현재 중심을 잃고 외부자극에 영향을 받고 있는 상태 입니다."
    }
    val danger = when (result) {
        "위험" -> "위험"
        "위험(열쇠)" -> "위험"
        "위험(그루터기)" -> "위험"
        "위험(둥근수관)" -> "위험"
        "경고(다람쥐)" -> "경고"
        "경고(나뭇잎)" -> "경고"
        "경고(다람쥐+나뭇잎)" -> "경고"
        else -> "보통"
    }
    val orange = Color(0xFFFFA500)

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = orange, shape = RoundedCornerShape(10.dp))
                    .size(30.dp, 80.dp)
            ) {
                Text(
                    text = "진단 결과",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .size(350.dp)
                    .padding(16.dp)
            )

            Text(
                text = danger,
                color = if (danger == "위험") Color.Red else if (danger == "경고") Color.Blue else Color.Green,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )

            // 두 번째 텍스트
            Text(
                text = script,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(10.dp)
            )
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
                        MenuList("가까운 병원", "https://www.google.com/search?sca_esv=587945461&tbs=lf:1,lf_ui:2&tbm=lcl&sxsrf=AM9HkKmuVUCMJbWXpysjddwXOeU1XxouXw:1701766776289&q=%EC%A0%95%EC%8B%A0%EA%B3%BC+%EB%B3%91%EC%9B%90&rflfq=1&num=10&sa=X&ved=2ahUKEwi8ocOh9_eCAxVYg1YBHQ3eDLgQjGp6BAgVEAE&biw=1536&bih=739&dpr=1.25#rlfi=hd:;si:;mv:[[37.5738732,126.9851049],[37.5383335,126.9335744]];tbs:lrf:!1m4!1u3!2m2!3m1!1e1!1m4!1u2!2m2!2m1!1e1!2m1!1e2!2m1!1e3!3sIAE,lf:1,lf_ui:2" )
                        MenuList("간단 검사", "http://htp-test.com/mini/htp_test01_00.htm")
                        MenuList("고객문의", "https://www.notion.so/gonuai-seoul/KidWatch-58178cb2fe214fa68a8b93719338c4a3?pvs=4")
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    val result = "Your Result"
    val uri = "Your Uri"
    ResultScreen(result, uri)
}