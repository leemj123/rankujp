document.addEventListener("DOMContentLoaded", () => {
    // 지정된 클래스의 모든 요소를 선택
    const targets = document.querySelectorAll(".track-click");

    targets.forEach(el => {
        el.addEventListener("click", () => {

            fetch("/detected/hotel/detail", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(
                    Number(el.dataset.number)
                )
            })
                .then(res => {
                    if (!res.ok) throw new Error("요청 실패");
                    return res.json();
                })
                .then(data => console.log("성공:", data))
                .catch(err => console.error("에러:", err));
        });
    });
});