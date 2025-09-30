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

    // 2) 포토 카드 클릭 바인딩
    document.querySelectorAll(".photo-card").forEach((item, index) => {
        item.addEventListener("click", () => {
            const attr = item.getAttribute("data-index");
            currentIndex = Number.isInteger(+attr) ? parseInt(attr, 10) : index;
            openModal();
        });
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
    const backdrop = document.querySelector(".modal-backdrop");
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
        modalImage.setAttribute('draggable', 'false'); // 드래그 고스트 방지
        modalImage.style.touchAction = 'pan-y';        // 세로 스크롤 유지, 가로만 우리가 처리
        bindSwipe(modalImage);
    }
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
    if (modalImage) modalImage.src = imgList[currentIndex];
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
            topDailyPercent.textContent = fmtNum(
                Math.round(((data.crossedOutRate - data.dailyRate) / data.crossedOutRate) * 100)
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



// let imgList = [];
// let currentIndex = 0;
// let id = null;
//
// const hotelPriceSearchValue = document.getElementById('search-value');
// const topDailyPercent = document.getElementById('daily-percent');
// const topDailyPrice = document.getElementById('daily-price');
// const topOnPrice = document.getElementById('top-on-price');
// const topNoPrice = document.getElementById('top-no-price');
// const topPriceWarpper = document.getElementById('top-price-warpper');
//
// document.addEventListener("DOMContentLoaded", async () => {
//     // URL에서 roomId 추출
//     const segs = window.location.pathname.replace(/\/+$/, "").split("/");
//     id = segs[segs.length - 1];
//
//     // 1) 이미지 먼저 로드 & 인덱스 부여
//     await onDetailImgLoad();
//
//     // 2) 포토 카드 클릭 바인딩 (존재할 때만)
//     document.querySelectorAll(".photo-card").forEach((item, index) => {
//         item.addEventListener("click", () => {
//             // data-index 우선, 없으면 forEach의 index를 사용
//             const attr = item.getAttribute("data-index");
//             currentIndex = Number.isInteger(+attr) ? parseInt(attr, 10) : index;
//             openModal();
//         });
//     });
//
//     // 3) 더보기/접기 (있을 때만)
//     document.querySelectorAll(".price-list .more-price").forEach((btn) => {
//         btn.addEventListener("click", function () {
//             const list = this.closest(".price-list");
//             const isActive = list.classList.toggle("active");
//             this.textContent = isActive ? "접기" : "더보기";
//             this.setAttribute("aria-expanded", String(isActive));
//         });
//     });
//
//     // 4) 모달 관련 바인딩 (있을 때만)
//     const backdrop = document.querySelector(".modal-backdrop");
//     if (backdrop) backdrop.addEventListener("click", closeModal);
//
//     const prevBtn = document.getElementById("prevBtn");
//     if (prevBtn) {
//         prevBtn.addEventListener("click", (e) => {
//             e.stopPropagation();
//             if (!imgList.length) return;
//             currentIndex = (currentIndex - 1 + imgList.length) % imgList.length;
//             updateModalImage();
//             photoIndexUpdate();
//         });
//     }
//
//     const nextBtn = document.getElementById("nextBtn");
//     if (nextBtn) {
//         nextBtn.addEventListener("click", (e) => {
//             e.stopPropagation();
//             if (!imgList.length) return;
//             currentIndex = (currentIndex + 1) % imgList.length;
//             updateModalImage();
//             photoIndexUpdate();
//         });
//     }
//
// });
//
// const photoIndexSpan = document.getElementById('photo-index');
// function photoIndexUpdate() {
//     photoIndexSpan.textContent = currentIndex + 1;
// }
// async function onDetailImgLoad() {
//     try {
//         const res = await fetch(`/rest/file/hotel/${id}`);
//         if (!res.ok) throw new Error("Failed to fetch image list");
//         imgList = await res.json();
//
//         if (!Array.isArray(imgList) || imgList.length === 0) {
//             console.warn("No images found for this room.");
//             return;
//         }
//
//         // 사진 카드에 data-index 부여 & 썸네일 세팅
//         document.querySelectorAll(".photo-card").forEach((card, index) => {
//             if (index < imgList.length) {
//                 card.setAttribute("data-index", String(index));
//                 const img = card.querySelector("img");
//                 if (img) img.src = imgList[index];
//                 card.style.display = ""; // 혹시 숨겨둔 게 있으면 노출
//             } else {
//                 // 초과 요소는 숨김
//                 card.style.display = "none";
//             }
//         });
//     } catch (err) {
//         console.error("Error fetching image list:", err);
//     }
// }
//
// async function openModal() {
//     // 최초 클릭 시 아직 목록이 없으면 로드 시도
//     if (imgList.length === 0) {
//         await onDetailImgLoad();
//         if (imgList.length === 0) return; // 여전히 없으면 중단
//     }
//     // 안전 가드
//     if (!Number.isInteger(currentIndex) || currentIndex < 0 || currentIndex >= imgList.length) {
//         currentIndex = 0;
//     }
//
//     const modal = document.getElementById("imageModal");
//     const modalImage = document.getElementById("modalImage");
//     if (!modal || !modalImage) return;
//
//     modalImage.src = imgList[currentIndex];
//     photoIndexSpan.textContent = currentIndex +1;
//     modal.classList.remove("hidden");
// }
//
// function closeModal() {
//     const modal = document.getElementById("imageModal");
//     if (modal) modal.classList.add("hidden");
// }
//
// function updateModalImage() {
//     if (!imgList.length) return;
//     const modalImage = document.getElementById("modalImage");
//     if (modalImage) modalImage.src = imgList[currentIndex];
// }
//
// const fmtNum = n => Number(n).toLocaleString('ja-JP');
//
//
// function searchHotelPrice() {
//     const searchUrl = '/rest/search/hotel/' + id + '/date?day=' + hotelPriceSearchValue.value;
//     topPriceWarpper.classList.add('on-load');
//     fetch(searchUrl, { headers: { 'Accept': 'application/json' } })
//         .then(res => {
//             if (!res.ok) throw new Error(`HTTP ${res.status}`);
//             return res.json();
//         })
//         .then(data => {
//             topDailyPercent.textContent = fmtNum(
//                 Math.round(((data.crossedOutRate - data.dailyRate) / data.crossedOutRate) * 100)
//             );
//             topDailyPrice.textContent = fmtNum(
//                 Math.round(data.dailyRate)
//             );
//
//             // ✅ 정상일 때: topNoPrice에 at400 없으면 추가
//             if (!topNoPrice.classList.contains('at400')) {
//                 topNoPrice.classList.add('at400');
//             }
//             topOnPrice.classList.remove('at400');
//         })
//         .catch(err => {
//             console.error(err);
//
//             // ✅ 에러일 때: topOnPrice에 at400 없으면 추가
//             if (!topOnPrice.classList.contains('at400')) {
//                 topOnPrice.classList.add('at400');
//             }
//
//             // topNoPrice에서 at400 제거
//             topNoPrice.classList.remove('at400');
//         })
//         .finally(()=> {
//                 topPriceWarpper.classList.remove('on-load')
//             });
// }
