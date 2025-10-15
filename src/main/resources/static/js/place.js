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
function openPlaceModal(src) {
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