const hotelSection = document.getElementById('hotel-section');
const placeSection = document.getElementById('place-section');
const navLinks = document.querySelectorAll('.content-navigation a');

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        const id = entry.target.getAttribute('id');
        const link = document.querySelector(`.content-navigation a[href="#${id}"]`);

        if (entry.isIntersecting) {
            navLinks.forEach(a => a.classList.remove('active'));
            link.classList.add('active');
        }
    });
}, {
    threshold: 0.6
});

// sections.forEach(section => observer.observe(section));
observer.observe(hotelSection);
observer.observe(placeSection);


const placeModal = document.getElementById('placeModal');
const placeImage = document.getElementById('placeImage');
const placeBackdrop = document.querySelector(".modal-backdrop.place");
if (placeBackdrop) placeBackdrop.addEventListener("click", closePlaceModal);

const photoAuthor = document.getElementById('place-photo-author');
function openPlaceModal(src,author,authorLink) {
    photoAuthor.innerHTML = ``
    photoAuthor.setAttribute("href", authorLink);
    photoAuthor.innerHTML = `
        <span class="f-14 f-b">ⓒ </span><span class="f-14 f-b">${author}</span>
    `

    placeModal.classList.remove("hidden");
    placeImage.classList.add("on-load");
    placeImage.src = src ;
    placeImage.onload = () => {
        placeImage.classList.remove("on-load");
    }

}
function closePlaceModal() {
    if (placeModal) placeModal.classList.add("hidden");
}

document.addEventListener("DOMContentLoaded", () => {
    let placeImgList = document.querySelectorAll(".place-photo img");

    placeImgList.forEach(img => {
        const dataSrc = img.getAttribute("data-src");
        if (dataSrc) {
            img.src = dataSrc;

            // 선택적으로: 로딩 완료 시 클래스 처리
            img.classList.add("on-load");
            img.onload = () => {
                img.classList.remove("on-load");
            };
        }
    });
});
document.addEventListener("DOMContentLoaded", () => {
    const slider = document.getElementById('restaurant-slide-wrapper');
    if (!slider) return;

    const DRAG_THRESHOLD = 12;     // 픽셀: 미세 흔들림 무시
    const HORIZONTAL_BIAS = 1.5;   // 수평 이동이 수직보다 충분히 커야 드래그로 인정
    let isDown = false;
    let dragging = false;
    let startX = 0, startY = 0;
    let startScroll = 0;

    // CSS로 두는 게 베스트: .restaurant-slide-wrapper { touch-action: pan-y; }
    slider.style.touchAction = 'pan-y';

    slider.addEventListener('pointerdown', (e) => {
        isDown = true;
        dragging = false;
        startX = e.clientX;
        startY = e.clientY;
        startScroll = slider.scrollLeft;
        slider.classList.add('dragging');
        // 캡처는 진짜 드래그로 확정된 뒤에 설정 (클릭 오인 방지)
    }, { passive: true });

    slider.addEventListener('pointermove', (e) => {
        if (!isDown) return;

        const dx = e.clientX - startX;
        const dy = e.clientY - startY;

        // 수평 이동이 충분히 크고, 수직 대비 수평성이 뚜렷할 때만 드래그 인지
        if (!dragging && Math.abs(dx) > DRAG_THRESHOLD && Math.abs(dx) > Math.abs(dy) * HORIZONTAL_BIAS) {
            dragging = true;
            // 이 시점에만 캡처 (클릭만 했을 땐 캡처 안 걸림)
            slider.setPointerCapture(e.pointerId);
        }

        if (dragging) {
            slider.scrollLeft = startScroll - dx;
        }
    }, { passive: true });

    const endDrag = (e) => {
        if (!isDown) return;
        isDown = false;
        slider.classList.remove('dragging');

        if (dragging) {
            // 드래그가 있었으면 '이번 한 번의 클릭'만 무효 (링크 오작동 방지)
            const cancelOnce = (ev) => {
                ev.preventDefault();
                ev.stopPropagation();
            };
            slider.addEventListener('click', cancelOnce, { once: true, capture: true });
        }
        dragging = false;
    };

    slider.addEventListener('pointerup', endDrag, { passive: true });
    slider.addEventListener('pointercancel', endDrag, { passive: true });
    slider.addEventListener('pointerleave', endDrag, { passive: true });
});