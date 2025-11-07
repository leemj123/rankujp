const updateTimerBox = document.getElementById('update-timer');
const sandTimer = document.querySelectorAll('.sand-timer');
const shineBtn = document.getElementById('shine-btn');

document.addEventListener('DOMContentLoaded', () => {
    let count = 0;
    let intervalTime = 3000; // 초기 5초
    let intervalId;

    const triggerAnimation = () => {
        updateTimerBox.classList.remove('shaking');
        shineBtn.classList.remove('shining');

        setTimeout(() => {
            updateTimerBox.classList.add('shaking');
            shineBtn.classList.add('shining');
        }, 100);

        count++;

        // 5회 이후 주기 변경
        if (count === 5) {
            clearInterval(intervalId); // 기존 인터벌 해제
            intervalTime = 7000; // 10초로 변경
            intervalId = setInterval(triggerAnimation, intervalTime);
        }
    };

    // 최초 인터벌 시작
    intervalId = setInterval(triggerAnimation, intervalTime);
});



function updateCountdown() {
    sandTimer.forEach (item => {
        item.textContent = getNextUpdateTimeString();
    })
}

document.addEventListener('DOMContentLoaded', () => {
    updateCountdown();                 // 처음 한 번 표시
    setInterval(updateCountdown, 1000); // 1초마다 갱신
});

function getNextUpdateTimeString() {
    const now = new Date();
    const currentHour = now.getHours();
    const nextPoints = [0,3, 6,9,12,15,18,21];

    // 다음 타임포인트 찾기
    let nextHour = nextPoints.find(h => h > currentHour);
    if (nextHour === undefined) nextHour = 24; // 21~23시는 다음날 00시로 설정

    // 다음 타임포인트의 실제 날짜/시간 객체
    const nextDate = new Date(now);
    nextDate.setHours(nextHour, 0, 0, 0);

    // 남은 초 계산
    const diffSec = Math.floor((nextDate - now) / 1000);

    // 시/분/초 변환
    const hours = String(Math.floor(diffSec / 3600)).padStart(2, '0');
    const minutes = String(Math.floor((diffSec % 3600) / 60)).padStart(2, '0');
    const seconds = String(diffSec % 60).padStart(2, '0');

    return `${hours}:${minutes}:${seconds}`;
}

document.addEventListener("DOMContentLoaded", async () => {
    const startInput = document.getElementById('search-value');
    const url = window.location.href; // 예: http://localhost:40004/hotel/2735437?top=3
    const todayTrafficDiv = document.getElementById('today-traffic');

    // URL에서 hotelId 추출
    const match = url.match(/\/hotel\/(\d+)/);
    if (!match) {
        console.error("호텔 ID를 URL에서 찾을 수 없습니다.");
        return;
    }
    const hotelId = match[1];

    const startDate = startInput.value; // yyyy-MM-dd
    if (!startDate) return;

    // finDate = startDate + 2일
    const finDate = new Date(startDate);
    finDate.setDate(finDate.getDate() + 2);
    const finDateStr = finDate.toISOString().split('T')[0];

    const res = await fetch(
        `/odongdev/engagement?stayDate=${startDate}&finDate=${finDateStr}&hotelId=${hotelId}`
    );
    if (!res.ok) {console.info("아고다 트래픽 없음"); return;}

    const todayBookingNumber = await res.text(); // 백엔드에서 숫자 문자열 리턴
    if (todayBookingNumber == null || todayBookingNumber === '') {return;}
    console.log("오늘 예약 수:", todayBookingNumber);

    // 예: 화면에 표시
    todayTrafficDiv.innerHTML = `
          <div class="detail-thunder-svg"></div>
          <p style="color: #fff; white-space: nowrap;" class="f-15 f-b">오늘 이 호텔을 <span class="f-17 f-b" style="color: #ffd700;">${todayBookingNumber}</span>명이 예약했어요</p>
        `
    todayTrafficDiv.style.display = 'flex';

    const hello = document.getElementById('bottom-engagement');

    hello.innerHTML = `
        <span class="detail-thunder-svg" style=" margin-right: 3px;"></span>오늘&nbsp;
        <span class="f-15 f-b" style="color: #FF6B00;" >${todayBookingNumber}명</span>이 예약완료!
    `
    hello.style.display = 'flex';

});