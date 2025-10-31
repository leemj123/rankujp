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

document.getElementById('upup').addEventListener('click', () => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
});

const historyBox = document.getElementById('history-box');
const history = document.getElementById('history');

document.addEventListener("DOMContentLoaded", () => {
    // 외부 클릭 감지
    document.addEventListener("click", (e) => {
        // box 또는 버튼 클릭이면 무시
        const isClickInside = historyBox.contains(e.target) || history.contains(e.target);
        if (!isClickInside) {
            historyBox.classList.remove("show");
        }
    });
});

history.addEventListener('click', () => {
    if ( historyBox.classList.contains('show') )
        historyBox.classList.remove('show');
     else {
        this.loadCookie();
        historyBox.classList.add('show');
    }

});

const historyUl = document.getElementById('history-items');

function loadCookie () {
    const found = document.cookie.split('; ').find(c => c.startsWith('ranku_history='));
    if (!found) batchCookie(null);
    try {
        const json = decodeURIComponent(found.split('=')[1]);
        return batchCookie(JSON.parse(json));
    } catch {
        return null;
    }
}
function hideHistory() {
    historyBox.classList.remove('show');
}

function batchCookie(data) {
    historyUl.innerHTML = "";

    if (data == null) {
        historyUl.classList.add('none');
        return historyUl.innerHTML = `
            <li><div style="content: url('/public/default2.svg')"></div></li>
        `;
    } else {

        historyUl.classList.remove('none');

        data.forEach((item, index) => {
            // li 요소
            const li = document.createElement("li");
            const a = document.createElement("a");
            const img = document.createElement("img");
            const p = document.createElement("p");


            a.href = item.url;
            img.src = item.thumb;
            img.alt = item.title;
            p.textContent = decodeURIComponent(item.title).replace(/\+/g, " ");
            p.className = 'f-15 f-b';
            a.appendChild(img);
            a.appendChild(p);
            ``
            li.appendChild(a);

            const sep = document.createElement("div");
            sep.className = "separator";

            historyUl.appendChild(li);
            if (index < data.length - 1) historyUl.appendChild(sep);
        });
    }
}