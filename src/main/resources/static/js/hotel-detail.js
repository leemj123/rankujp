let imgList = [];
let currentIndex = 0;
let id = null;

document.addEventListener("DOMContentLoaded", async () => {
    // URL에서 roomId 추출
    const segs = window.location.pathname.replace(/\/+$/, "").split("/");
    id = segs[segs.length - 1];

    // 1) 이미지 먼저 로드 & 인덱스 부여
    await onDetailImgLoad();

    // 2) 포토 카드 클릭 바인딩 (존재할 때만)
    document.querySelectorAll(".photo-card").forEach((item, index) => {
        item.addEventListener("click", () => {
            // data-index 우선, 없으면 forEach의 index를 사용
            const attr = item.getAttribute("data-index");
            currentIndex = Number.isInteger(+attr) ? parseInt(attr, 10) : index;
            openModal();
        });
    });

    // 3) 더보기/접기 (있을 때만)
    document.querySelectorAll(".price-list .more-price").forEach((btn) => {
        btn.addEventListener("click", function () {
            const list = this.closest(".price-list");
            const isActive = list.classList.toggle("active");
            this.textContent = isActive ? "접기" : "더보기";
            this.setAttribute("aria-expanded", String(isActive));
        });
    });

    // 4) 모달 관련 바인딩 (있을 때만)
    const backdrop = document.querySelector(".modal-backdrop");
    if (backdrop) backdrop.addEventListener("click", closeModal);

    const prevBtn = document.getElementById("prevBtn");
    if (prevBtn) {
        prevBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            if (!imgList.length) return;
            currentIndex = (currentIndex - 1 + imgList.length) % imgList.length;
            updateModalImage();
            photoIndexUpdate();
        });
    }

    const nextBtn = document.getElementById("nextBtn");
    if (nextBtn) {
        nextBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            if (!imgList.length) return;
            currentIndex = (currentIndex + 1) % imgList.length;
            updateModalImage();
            photoIndexUpdate();
        });
    }
});

const photoIndexSpan = document.getElementById('photo-index');
function photoIndexUpdate() {
    photoIndexSpan.textContent = currentIndex + 1;
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
                card.style.display = ""; // 혹시 숨겨둔 게 있으면 노출
            } else {
                // 초과 요소는 숨김
                card.style.display = "none";
            }
        });
    } catch (err) {
        console.error("Error fetching image list:", err);
    }
}

async function openModal() {
    // 최초 클릭 시 아직 목록이 없으면 로드 시도
    if (imgList.length === 0) {
        await onDetailImgLoad();
        if (imgList.length === 0) return; // 여전히 없으면 중단
    }
    // 안전 가드
    if (!Number.isInteger(currentIndex) || currentIndex < 0 || currentIndex >= imgList.length) {
        currentIndex = 0;
    }

    const modal = document.getElementById("imageModal");
    const modalImage = document.getElementById("modalImage");
    if (!modal || !modalImage) return;

    modalImage.src = imgList[currentIndex];
    photoIndexSpan.textContent = currentIndex +1;
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