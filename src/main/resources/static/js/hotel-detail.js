document.querySelectorAll('.price-list .more-price').forEach(btn => {
    btn.addEventListener('click', function () {
        const list = this.closest('.price-list');
        const isActive = list.classList.toggle('active');
        this.textContent = isActive ? '접기' : '더보기';
        this.setAttribute('aria-expanded', String(isActive));
    });
});