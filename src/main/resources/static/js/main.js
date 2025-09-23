const fmtJPY = el => {
    const n = +el.textContent.replace(/[^\d.-]/g, ''); // 기존 콤마 제거
    if (!Number.isFinite(n)) return;
    el.textContent = n.toLocaleString('ja-JP');
};

// 2) 초기 렌더된 요소들 포맷
document.querySelectorAll('.JPY').forEach(fmtJPY);

// 3) 동적으로 추가되는 요소들 자동 포맷
const mo = new MutationObserver(muts => {
    for (const m of muts) {
        m.addedNodes.forEach(node => {
            if (node.nodeType !== 1) return;              // ELEMENT_NODE만
            if (node.matches?.('.JPY')) fmtJPY(node);     // 자신이 .JPY
            node.querySelectorAll?.('.JPY').forEach(fmtJPY); // 자식 중 .JPY
        });
    }
});
mo.observe(document.body, { childList: true, subtree: true });