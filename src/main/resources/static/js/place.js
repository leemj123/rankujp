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
    if (slider) {
        let isDown = false;
        let startX = 0;
        let startScroll = 0;
        let moved = false;

        // 드래그 시작
        slider.addEventListener('pointerdown', (e) => {
            isDown = true;
            moved = false;
            slider.classList.add('dragging');
            slider.setPointerCapture(e.pointerId);
            startX = e.clientX;
            startScroll = slider.scrollLeft;
        }, { passive: true });

        // 드래그 이동
        slider.addEventListener('pointermove', (e) => {
            if (!isDown) return;
            const dx = e.clientX - startX;
            if (Math.abs(dx) > 3) moved = true;
            slider.scrollLeft = startScroll - dx;
        }, { passive: true });

        // 드래그 종료
        const endDrag = (e) => {
            if (!isDown) return;
            isDown = false;
            slider.classList.remove('dragging');
        };

        slider.addEventListener('pointerup', endDrag, { passive: true });
        slider.addEventListener('pointercancel', endDrag, { passive: true });
        slider.addEventListener('pointerleave', endDrag, { passive: true });

        // 드래그 중 카드 안의 a 클릭 무효화(의도치 않은 클릭 방지)
        slider.addEventListener('click', (e) => {
            if (moved) {
                e.preventDefault();
                e.stopPropagation();
            }
        }, true);
    }
})