const wrapper = document.getElementById('filters');
const topSection = document.getElementById('top-item-section');
const normalSection = document.getElementById('normal-item-section');

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

    const [firstValue, secondValue] = Array.from(onButtons).map(b => b.dataset.value);


    const url = new URL('/rest/score', location.origin);
    url.searchParams.set('location', firstValue);
    url.searchParams.set('type', secondValue);


    fetch(url, { headers: { 'Accept': 'application/json' } })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            renderRanking(data);

        })
        .catch(console.error);

});
// ------------------------------------------------------------------
const fmt = new Intl.NumberFormat('ko-KR');
const esc = (s='') => String(s)
    .replaceAll('&','&amp;').replaceAll('<','&lt;')
    .replaceAll('>','&gt;').replaceAll('"','&quot;')
    .replaceAll("'",'&#39;');

const prefLabel = (v) => {
    switch (Number(v)) {
        case 1: return '비지니스';
        case 2: return '커플';
        case 3: return '혼자';
        default: return '가족';
    }
};

const prefIconClass = (v) => {
    switch (Number(v)) {
        case 1: return 'business-svg';
        case 2: return 'couple-svg';
        case 3: return 'solo-svg';
        default: return 'family-svg';
    }
};

const rankBadgeClass = (rank) => {
    if (rank === 1) return 'gold';
    if (rank === 2) return 'sliver';
    if (rank === 3) return 'bronze';
    return '';
};
const rankuScoreSVGClass = (rank) => {
    switch (Number(rank)) {
        case rank >= 86: return 'best';
        case rank >= 71: return 'verygood';
        case rank >= 41: return 'good';
        case 3: return 'normal';
    }
};
const rankuScoreClass = (rank) => {
    switch (Number(rank)) {
        case rank >= 86: return '매우 훌륭함';
        case rank >= 71: return '훌륭함';
        case rank >= 41: return '좋음';
        case 3: return '보통';
    }
};


// TOP 3 카드
const topCard = (item, rank) => {
    return `
      <a href="/hotel/${item.id}?top=${rank}" class="top-item top-${rank}">
        <div class="head-line"></div>
        <img src="${esc(item.thumbnailImg)}" alt="${esc(item.koName)}의 대표 이미지" loading="lazy" />
        <div class="ranku list ${rankBadgeClass(rank)}">
          <span class="ranku-value">${rank}</span>
        </div>
        <div class="top-item-description up">
           <div>
              <h2 class="ml top-item-title">{item.koName}</h2>
              <div style="display: flex; align-items: center; gap: .6rem;">
                <span class="xl" style="color: #fff;">${item.rankuScore}</span>
                  <span class="f-15" style="color: #fff;">/100</span>
                  <span class="f-15 f-b" style="color: #fff">${rankuScoreClass(item.rankuScore)}</span>
              </div>
            </div>
        </div>
        <div class="top-item-description down">
          <div style="display: flex; gap: .6rem;">
              <div class="couple-svg"></div>
                <p class="f-17 f-b" style="color: #fff;">
                    <b class="f-17 f-b" style="color: #fff;">${prefLabel(item.preferenceValue)}</b>
                    에게 가장인기!
                </p>
            </div>
          <div style="display:flex;gap:.6rem;">
            <div class="hotel-star-svg"></div>
            <p class="f-17 f-b" style="color:#fff;">${esc(item.starRating)}성</p>
          </div>
        </div>
      </a>
    `.trim();
};

// 일반 랭킹 카드 (4위~)
const normalCard = (item, rank) => {
    return `
      <li>
        <a href="/hotel/${item.id}?top=${rank}">
            <article class="ranku-item">
              <div class="ranku-img-box">
                <img src="${esc(item.thumbnailImg)}" alt="${esc(item.koName)}의 대표사진" loading="lazy">
                <div class="ranku list normal">
                  <span class="ranku-value">${rank}</span>
                </div>
              </div>
              <div class="description-warpper">
                <div class="price-left">
                  <h2 class="skip-2">${esc(item.koName)}</h2>
                  <section>
                    <div class="price-left-content">
                      <div class="${prefIconClass(item.preferenceValue)}"></div>
                      <p class="f-17">
                        <span class="highlight f-b">${prefLabel(item.preferenceValue)}</span>에게 가장인기!
                      </p>
                    </div>
                    <div class="price-left-content">
                      <div class="hotel-star-svg"></div>
                      <p class="f-17">${esc(item.starRating)}성</p>
                    </div>
                  </section>
                </div>
                <div class="score-warpper">
                        <div class="ranku-total-score">
                        <div class="score ${rankuScoreSVGClass(item.rank)} "></div>
                          <p class="score-value xl">${item.rankuScore}</p>
                            <p class="f-15">${rankuScoreClass(item.rank)}</p>
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
    top3.forEach((item, i) => {
        const rank = i + 1;
        const wrap = document.createElement('div');
        wrap.innerHTML = topCard(item, rank);
        topFrag.appendChild(wrap.firstElementChild);
    });
    topSection.appendChild(topFrag);

    // NORMAL (4위~)
    normalSection.innerHTML = '';
    const normalFrag = document.createDocumentFragment();
    rest.forEach((item, i) => {
        const rank = i + 4;
        const wrap = document.createElement('div');
        wrap.innerHTML = normalCard(item, rank);
        normalFrag.appendChild(wrap.firstElementChild);
    });
    normalSection.appendChild(normalFrag);
}
