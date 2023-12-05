package com.example.kidswatch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

@Composable
fun ResultScreen(result: String?, uri: String?) {

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

        // 첫 번째 텍스트
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
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    val result = "Your Result"
    val uri = "Your Uri"
    ResultScreen(result, uri)
}