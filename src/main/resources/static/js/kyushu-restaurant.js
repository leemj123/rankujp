const wrapper = document.getElementById('filters');
const topSection = document.getElementById('top-item-section');
const normalSection = document.getElementById('normal-item-section');

let page = 2;
let paramLocation = 1;
let paramDetailLocation = 1;
let paramType = 1;


wrapper.addEventListener('click', (e) => {
    const btn = e.target.closest('button.chip');
    if (!btn || !wrapper.contains(btn)) return;

    const row = btn.closest('.chip-row');
    if (!row) return;

    if (btn.classList.contains('on')) return;

    btn.classList.add('on');


    row.querySelectorAll('.chip.on').forEach(el => {
        if (el !== btn) el.classList.remove('on');
    });


    const onButtons = wrapper.querySelectorAll('.chip-row .chip.on');
    if (onButtons.length < 2) return;

    const [firstValue, secondValue, thirdValue] = Array.from(onButtons).map(b => b.dataset.value);

    page = 1;
    paramLocation = firstValue;
    paramDetailLocation = secondValue;
    paramType = thirdValue;

    initRender()

});
const detailChipColum = document.getElementById('detail-area-row');

function controlDetailChip(value) {
    paramDetailLocation = 1;

    switch(value) {
        case 'FUKUOKA' : {
            detailChipColum.innerHTML = `
                <button class="f-15 chip on" data-value="1" onclick="setParamDetailLocation(this.dataset.value)">전체</button>
                <button class="f-15 chip" data-value="2" onclick="setParamDetailLocation(this.dataset.value)">하카타</button>
                <button class="f-15 chip" data-value="3" onclick="setParamDetailLocation(this.dataset.value)">텐진</button>
                <button class="f-15 chip" data-value="4" onclick="setParamDetailLocation(this.dataset.value)">나카스</button>
            `;
            break;
        }
        case 'OITA' : {
            detailChipColum.innerHTML = `
                <button class="f-15 chip on" data-value="1" onclick="setParamDetailLocation(this.dataset.value)">전체</button>
                <button class="f-15 chip" data-value="2" onclick="setParamDetailLocation(this.dataset.value)">유후</button>
                <button class="f-15 chip" data-value="3" onclick="setParamDetailLocation(this.dataset.value)">벳푸</button>
                <button class="f-15 chip" data-value="4" onclick="setParamDetailLocation(this.dataset.value)">오이타시</button>
            `;
            break;
        }
        case 'NAGASAKI' : {
            detailChipColum.innerHTML = `
                <button class="f-15 chip on" data-value="1" onclick="setParamDetailLocation(this.dataset.value)">전체</button>
                <button class="f-15 chip" data-value="2" onclick="setParamDetailLocation(this.dataset.value)">나가사키시</button>
                <button class="f-15 chip" data-value="3" onclick="setParamDetailLocation(this.dataset.value)">사세보</button>
                <button class="f-15 chip" data-value="4" onclick="setParamDetailLocation(this.dataset.value)">운젠</button>
            `;
            break;
        }
        case 'SAGA' : {
            detailChipColum.innerHTML = `
                <button class="f-15 chip on" data-value="1" onclick="setParamDetailLocation(this.dataset.value)">전체</button>
                <button class="f-15 chip" data-value="2" onclick="setParamDetailLocation(this.dataset.value)">사가시</button>
                <button class="f-15 chip" data-value="3" onclick="setParamDetailLocation(this.dataset.value)">우레시노</button>
                <button class="f-15 chip" data-value="4" onclick="setParamDetailLocation(this.dataset.value)">아리타</button>
            `;
            break;
        }
    }
    detailChipColum.style.display ='flex';
}

function setParamDetailLocation(value) {

    if (!value) {
        paramDetailLocation = 1;
        detailChipColum.innerHTML = `
            <button class="f-15 chip on" data-value="1" onclick="setParamDetailLocation(this.dataset.value)">전체</button>
        `
        detailChipColum.style.display ='none';
        return;
    }

    paramDetailLocation = value;
}
// ------------------------------------------------------------------
const fmt = new Intl.NumberFormat('ko-KR');
const esc = (s='') => String(s)
    .replaceAll('&','&amp;').replaceAll('<','&lt;')
    .replaceAll('>','&gt;').replaceAll('"','&quot;')
    .replaceAll("'",'&#39;');


const rankBadgeClass = (rank) => {
    if (rank === 1) return 'gold';
    if (rank === 2) return 'sliver';
    if (rank === 3) return 'bronze';
    return '';
};

// TOP 3 카드
const topCard = (item, rank) => {
    return `
      <a href="${item.googleMapsUri}" class="top-item top-${rank}">
        <div class="head-line"></div>
        <img src="${esc(item.thumbnailUri)}" alt="${esc(item.koName)}의 대표 이미지" loading="lazy" onerror="this.onerror=null; this.src='/public/default.svg'; this.style.objectFit='none';"/>
        <div class="ranku list ${rankBadgeClass(rank)}">
          <span class="ranku-value">${rank}</span>
        </div>
        <div class="top-item-description up">
            <h2 class="ml top-item-title">${esc(item.title)}</h2>
            <div class="restaurant-info-div">
                <p class="f-17 f-b" style="color: #fff">${item.rating +'점'}</p>
                <div class="restaurant-info-line"></div>
                <p class="f-17 f-b" style="color: #fff"> 리뷰 ${fmt.format(item.userRatingCount)}</p>
            </div>
        </div>
      </a>
    `.trim();
};

// 일반 랭킹 카드 (4위~)
const normalCard = (item, rank) => {
    return `
        <li>
            <a href="${item.googleMapsUri}" target="_blank">
                <article class="ranku-item">
                    <div class="ranku-img-box">
                        <img src="${esc(item.thumbnailUri)}" alt="${esc(item.title)}의 대표사진" loading="lazy" onerror="this.onerror=null; this.src='/public/default.svg'; this.style.objectFit='none';">
                        <div class="ranku list normal">
                          <span class="ranku-value">${rank}</span>
                        </div>
                    </div>
                    <div class="description-warpper">
                        <h2 class="skip-2">${item.title}</h2>
                        <div class="restaurant-info-div normal">
                            <div class="restaurant-info-item">
                                <div class="restaurant-star-svg"></div>
                                <p class="ml">${item.rating}</p>
                            </div>
                            <div class="restaurant-info-line normal-line"></div>
                            <div class="restaurant-info-item">
                                <div class="restaurant-comment-svg"></div>
                                <p class="ml">${fmt.format(item.userRatingCount)}</p>
                            </div>
                        </div>
                    </div>
                </article>
            </a>
        </li>
    `.trim();
};

const noneRankCard = (item, rank) => {
    return `
        <li>
            <a href="${item.googleMapsUri}" target="_blank">
                <article class="ranku-item">
                    <div class="ranku-img-box">
                        <img src="${esc(item.thumbnailUri)}" alt="${esc(item.title)}의 대표사진" loading="lazy" onerror="this.onerror=null; this.src='/public/default.svg'; this.style.objectFit='none';">
                    </div>
                    <div class="description-warpper">
                        <h2 class="skip-2">${item.title}</h2>
                        <div class="restaurant-info-div normal">
                            <div class="restaurant-info-item">
                                <div class="restaurant-star-svg"></div>
                                <p class="ml">${item.rating}</p>
                            </div>
                            <div class="restaurant-info-line normal-line"></div>
                            <div class="restaurant-info-item">
                                <div class="restaurant-comment-svg"></div>
                                <p class="ml">${fmt.format(item.userRatingCount)}</p>
                            </div>
                        </div>
                    </div>
                </article>
            </a>
        </li>
    `.trim();
};

// 렌더 함수: content 배열을 받아 두 섹션에 배치
function renderRanking(data){
    const top3 = data.content.slice(0, 3);
    const rest = data.content.slice(3);

    // TOP 3
    topSection.innerHTML = '';
    const topFrag = document.createDocumentFragment();
    top3.forEach((item,i) => {
        const wrap = document.createElement('div');
        wrap.innerHTML = topCard(item, i+1);
        topFrag.appendChild(wrap.firstElementChild);
    });
    topSection.appendChild(topFrag);

    // NORMAL (4위~)
    normalSection.innerHTML = '';
    const normalFrag = document.createDocumentFragment();
    rest.forEach((item,i) => {
        const wrap = document.createElement('div');
        wrap.innerHTML = normalCard(item, i+4);
        normalFrag.appendChild(wrap.firstElementChild);
    });
    normalSection.appendChild(normalFrag);
}



let ticking = false;
let LOCK = false;


function nearBottom() {
    const gap = document.documentElement.scrollHeight - window.innerHeight - window.scrollY;
    return gap < 1200;
}

window.addEventListener('scroll', () => {
    if (ticking) return;
    ticking = true;
    requestAnimationFrame(() => {
        if (nearBottom() && !LOCK) {
            LOCK = true;
            renderInfinityPageNation();
            setTimeout(() => { LOCK = false; }, 800);
        }
        ticking = false;
    });
}, { passive: true });

function initRender() {
    page = 1;
    const url = new URL('/kyushu/restaurant/list', location.origin);
    url.searchParams.set('page', page);
    url.searchParams.set('location', paramLocation);
    url.searchParams.set('area', paramDetailLocation)
    url.searchParams.set('type', paramType);

    fetch(url, { headers: { 'Accept': 'application/json' } })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            renderRanking(data);

        })
        .catch(console.error);

}

function renderInfinityPageNation() {

    const url = new URL('/kyushu/restaurant/list', location.origin);
    url.searchParams.set('page', page);
    url.searchParams.set('location', paramLocation);
    url.searchParams.set('area', paramDetailLocation);
    url.searchParams.set('type', paramType);

    fetch(url, { headers: { 'Accept': 'application/json' } })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            renderNormal(data.content);
            page++;
        })
        .catch(console.error);

}

function renderNormal(data) {
    const base = 4 + normalSection.querySelectorAll('li').length;
    const normalFrag = document.createDocumentFragment();

    if (base < 99) {
        data.forEach((item, i) => {
            const wrap = document.createElement('div');
            wrap.innerHTML = normalCard(item, base + i);
            normalFrag.appendChild(wrap.firstElementChild);
        });
    } else {
        data.forEach((item, i) => {
            const wrap = document.createElement('div');
            wrap.innerHTML = noneRankCard(item, base + i);
            normalFrag.appendChild(wrap.firstElementChild);
        });
    }


    normalSection.appendChild(normalFrag);
}