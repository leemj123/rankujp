let imgList = [];
let currentIndex = 0;
let id = null;

const hotelPriceSearchValue = document.getElementById('search-value');
const topDailyPercent = document.getElementById('daily-percent');
const topDailyPrice = document.getElementById('daily-price');
const topOnPrice = document.getElementById('top-on-price');
const topNoPrice = document.getElementById('top-no-price');
const topPriceWarpper = document.getElementById('top-price-warpper');
const photoIndexSpan = document.getElementById('photo-index');

document.addEventListener("DOMContentLoaded", async () => {
    // URL에서 roomId 추출
    const segs = window.location.pathname.replace(/\/+$/, "").split("/");
    id = segs[segs.length - 1];

    // 1) 이미지 먼저 로드 & 인덱스 부여
    await onDetailImgLoad();

    // 2) 포토 카드 클릭 바인딩 - 클릭에만 반응
    document.querySelectorAll(".photo-card").forEach((item, index) => {
        let isDragging = false;
        let startX = 0;
        let startY = 0;

        // 마우스(또는 터치) 눌렀을 때 시작 좌표 저장
        item.addEventListener("pointerdown", (e) => {
            isDragging = false;
            startX = e.clientX;
            startY = e.clientY;
        });

        // 움직임 감지 — 일정 거리 이상이면 드래그로 판단
        item.addEventListener("pointermove", (e) => {
            const diffX = Math.abs(e.clientX - startX);
            const diffY = Math.abs(e.clientY - startY);
            if (diffX > 5 || diffY > 5) {  // 감도 조정 가능 (5px~10px 정도)
                isDragging = true;
            }
        });

        // 클릭 해제 시
        item.addEventListener("pointerup", (e) => {
            if (isDragging) return; // 드래그로 판정되면 클릭 무시

            const attr = item.getAttribute("data-index");
            currentIndex = Number.isInteger(+attr) ? parseInt(attr, 10) : index;
            openModal();
        });

        // 선택 방지 (텍스트 블루 강조 방지)
        item.addEventListener("dragstart", (e) => e.preventDefault());
    });

    // 3) 더보기/접기
    document.querySelectorAll(".price-list .more-price").forEach((btn) => {
        btn.addEventListener("click", function () {
            const list = this.closest(".price-list");
            const isActive = list.classList.toggle("active");
            this.textContent = isActive ? "접기" : "더보기";
            this.setAttribute("aria-expanded", String(isActive));
        });
    });

    // 4) 모달 관련 바인딩
    const backdrop = document.querySelector(".modal-backdrop.hotel");
    if (backdrop) backdrop.addEventListener("click", closeModal);

    const prevBtn = document.getElementById("prevBtn");
    if (prevBtn) {
        prevBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            goPrev();
        });
    }

    const nextBtn = document.getElementById("nextBtn");
    if (nextBtn) {
        nextBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            goNext();
        });
    }

    // 5) 스와이프 제스처 바인딩 (모달 큰 이미지 영역)
    const modalImage = document.getElementById("modalImage");
    if (modalImage) {
        modalImage.setAttribute('draggable', 'false');
        modalImage.style.touchAction = 'pan-y';
        bindSwipe(modalImage);
    }

    const hoverInfoSvg = document.getElementById('des-svg');
    const hoverInfoBox = document.getElementById('des-box');

    hoverInfoBox.style.display = 'none';

    // 마우스 올렸을 때
    hoverInfoSvg.addEventListener('mouseenter', () => {
        hoverInfoBox.style.display = 'block';
    });

    // 마우스 벗어났을 때
    hoverInfoSvg.addEventListener('mouseleave', () => {
        hoverInfoBox.style.display = 'none';
    });
});

function photoIndexUpdate() {
    if (photoIndexSpan) {
        photoIndexSpan.textContent = currentIndex + 1;
    }
}

async function onDetailImgLoad() {
    try {
        const res = await fetch(`/rest/file/hotel/${id}`);
        if (!res.ok) throw new Error("Failed to fetch image list");
        imgList = await res.json();

        if (!Array.isArray(imgList) || imgList.length === 0) {
            console.warn("No images found for this room.");
            return;
        }

        // 사진 카드에 data-index 부여 & 썸네일 세팅
        document.querySelectorAll(".photo-card").forEach((card, index) => {
            if (index < imgList.length) {
                card.setAttribute("data-index", String(index));
                const img = card.querySelector("img");
                if (img) img.src = imgList[index];
                card.style.display = "";
            } else {
                card.style.display = "none";
            }
        });
    } catch (err) {
        console.error("Error fetching image list:", err);
    }
}

async function openModal() {
    if (imgList.length === 0) {
        await onDetailImgLoad();
        if (imgList.length === 0) return;
    }
    if (!Number.isInteger(currentIndex) || currentIndex < 0 || currentIndex >= imgList.length) {
        currentIndex = 0;
    }

    const modal = document.getElementById("imageModal");
    const modalImage = document.getElementById("modalImage");
    if (!modal || !modalImage) return;

    modalImage.src = imgList[currentIndex];
    photoIndexUpdate();
    modal.classList.remove("hidden");
}

function closeModal() {
    const modal = document.getElementById("imageModal");
    if (modal) modal.classList.add("hidden");
}

function updateModalImage() {
    if (!imgList.length) return;
    const modalImage = document.getElementById("modalImage");
    modalImage.classList.add("on-load");
    if (modalImage) modalImage.src = imgList[currentIndex];
    modalImage.onload = () => {
        modalImage.classList.remove("on-load");
    }
}

function goPrev() {
    if (!imgList.length) return;
    currentIndex = (currentIndex - 1 + imgList.length) % imgList.length;
    updateModalImage();
    photoIndexUpdate();
}

function goNext() {
    if (!imgList.length) return;
    currentIndex = (currentIndex + 1) % imgList.length;
    updateModalImage();
    photoIndexUpdate();
}

/** 모바일/데스크탑 겸용 스와이프 바인딩 */
function bindSwipe(el) {
    let startX = 0, startY = 0, startT = 0, tracking = false;

    const SWIPE_PX = 50;   // 이동 거리 기준(px)
    const SWIPE_VX = 0.3;  // 속도 기준(px/ms)

    const point = (e) => {
        if (e.touches && e.touches[0]) return { x: e.touches[0].clientX, y: e.touches[0].clientY };
        if (e.changedTouches && e.changedTouches[0]) return { x: e.changedTouches[0].clientX, y: e.changedTouches[0].clientY };
        return { x: e.clientX, y: e.clientY };
    };

    const down = (e) => {
        const p = point(e);
        startX = p.x; startY = p.y;
        startT = performance.now();
        tracking = true;
    };

    const move = (e) => {
        if (!tracking) return;
        const p = point(e);
        const dx = p.x - startX;
        const dy = p.y - startY;

        // 가로 제스처가 우세하면 기본 스크롤 방지(가로만 우리가 처리)
        if (Math.abs(dx) > Math.abs(dy)) {
            e.preventDefault();
        }
    };

    const up = (e) => {
        if (!tracking) return;
        tracking = false;

        const p = point(e);
        const dx = p.x - startX;
        const dy = p.y - startY;
        const dt = Math.max(1, performance.now() - startT); // ms
        const vx = dx / dt; // px/ms

        if (Math.abs(dx) > Math.abs(dy) && (Math.abs(dx) >= SWIPE_PX || Math.abs(vx) >= SWIPE_VX)) {
            if (dx < 0) goNext();
            else goPrev();
        }
    };

    // 포인터 이벤트(신규 브라우저)
    el.addEventListener('pointerdown', down, { passive: true });
    el.addEventListener('pointermove', move, { passive: false });
    el.addEventListener('pointerup', up, { passive: true });
    el.addEventListener('pointercancel', up, { passive: true });

    // 구형 iOS 등 포인터 미지원 대응
    el.addEventListener('touchstart', down, { passive: true });
    el.addEventListener('touchmove', move, { passive: false });
    el.addEventListener('touchend', up, { passive: true });
    el.addEventListener('touchcancel', up, { passive: true });
}

const fmtNum = n => Number(n).toLocaleString('ja-JP');

function searchHotelPrice() {
    const searchUrl = '/rest/search/hotel/' + id + '/date?day=' + hotelPriceSearchValue.value;
    topPriceWarpper.classList.add('on-load');
    fetch(searchUrl, { headers: { 'Accept': 'application/json' } })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            const percent = data.crossedOutRate > 0
                ? Math.round(((data.crossedOutRate - data.dailyRate) / data.crossedOutRate) * 100)
                : (data.discountPercentage > 0 ? Math.round(data.discountPercentage) : 0);

            topDailyPercent.textContent = fmtNum(
                Math.round(percent)
            );
            topDailyPrice.textContent = fmtNum(
                Math.round(data.dailyRate)
            );

            // ✅ 정상일 때
            if (!topNoPrice.classList.contains('at400')) {
                topNoPrice.classList.add('at400');
            }
            topOnPrice.classList.remove('at400');
        })
        .catch(err => {
            console.error(err);

            // ✅ 에러일 때
            if (!topOnPrice.classList.contains('at400')) {
                topOnPrice.classList.add('at400');
            }
            topNoPrice.classList.remove('at400');
        })
        .finally(()=> {
            topPriceWarpper.classList.remove('on-load');
        });
}
async function shareHotel() {
    if (navigator.share) {
        try {
            await navigator.share({
                title: document.title,
                text: "이 페이지를 확인해보세요!",
                url: window.location.href
            });
        } catch (err) {
            console.error("공유 취소 또는 오류:", err);
        }
    } else {
        alert("이 브라우저는 공유하기를 지원하지 않습니다.");
    }
}

const mediaQuery = window.matchMedia("(max-width: 768px)");
handleMediaChange(mediaQuery);
mediaQuery.addEventListener("change", handleMediaChange);

function handleMediaChange(e) {
    if (e.matches) {
        console.log("모바일 전용 로직 실행");

        const mobileSlider = document.getElementById("mobile-slider");
        if (mobileSlider) {
            mobileSlider.style.touchAction = 'pan-y';        // 세로 스크롤 유지, 가로만 우리가 처리
            bindSwipe(mobileSlider);
        }
    }
}