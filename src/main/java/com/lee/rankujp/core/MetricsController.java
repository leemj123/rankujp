//package com.lee.rankujp.core;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//public class MetricsController {
//
//    private final MetricsSample sampler;
//
//    // JSON 데이터 (프론트나 외부 수집기도 쓸 수 있음)
//    @GetMapping(value = "/ops/metrics.json", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Object json() {
//        return sampler.snapshot();
//    }
//
//    // 초간단 HTML 차트(Chart.js CDN 사용)
//    @GetMapping(value = "/ops/metrics", produces = MediaType.TEXT_HTML_VALUE)
//    public String page() {
//        return """
//<!doctype html>
//<html lang="ko"><head><meta charset="utf-8"/>
//<title>Pool & Executor Metrics</title>
//<meta name="viewport" content="width=device-width,initial-scale=1"/>
//<style>
//  body{font-family:system-ui,Segoe UI,Roboto,Apple SD Gothic Neo,Helvetica,Arial,sans-serif;margin:16px}
//  .grid{display:grid;gap:16px}
//  canvas{max-height:260px}
//</style>
//<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
//</head><body>
//  <h2>Hikari / Executor Metrics (last ~6h, 10s step)</h2>
//  <div class="grid">
//    <canvas id="c1"></canvas>
//    <canvas id="c2"></canvas>
//    <canvas id="c3"></canvas>
//  </div>
//<script>
//  const C={};
//  async function fetchData(){
//    const r = await fetch('/ops/metrics.json'); return await r.json();
//  }
//  function mkCfg(label, datasets){
//    return {type:'line', data:{labels:[], datasets:datasets.map(s=>({label:s.label,data:[],borderWidth:1,pointRadius:0,tension:0.1}))},
//      options:{animation:false, responsive:true, maintainAspectRatio:false,
//        scales:{x:{ticks:{maxTicksLimit:10}, grid:{display:false}}, y:{beginAtZero:true}},
//        plugins:{legend:{position:'bottom'}}
//      }};
//  }
//  async function tick(){
//    const data = await fetchData();
//    const labels = data.map(p=>new Date(p.ts).toLocaleTimeString());
//    const series = {
//      active: data.map(p=>p.active ?? null),
//      idle: data.map(p=>p.idle ?? null),
//      pending: data.map(p=>p.pending ?? null),
//      timeouts: (function(){
//        // timeout은 누적 카운터 → 증가분으로 파생(초간단)
//        const arr = data.map(p=>p.timeoutTotal ?? 0);
//        const deltas = arr.map((v,i)=> i===0?0:Math.max(0, v - arr[i-1]));
//        return deltas;
//      })(),
//      execActive: data.map(p=>p.execActive ?? null),
//      execQueue: data.map(p=>p.execQueue ?? null),
//    };
//
//    // Chart 초기화(최초 1회)
//    if(!C.c1){
//      C.c1 = new Chart(document.getElementById('c1'), mkCfg('Hikari Connections', [
//        {label:'active'}, {label:'idle'}, {label:'pending'}
//      ]));
//      C.c2 = new Chart(document.getElementById('c2'), mkCfg('Hikari Timeouts (Δ per sample)', [
//        {label:'timeouts'}
//      ]));
//      C.c3 = new Chart(document.getElementById('c3'), mkCfg('Crawler Executor', [
//        {label:'execActive'}, {label:'execQueue'}
//      ]));
//    }
//
//    // 데이터 주입
//    function apply(chart, map){
//      chart.data.labels = labels;
//      chart.data.datasets.forEach(ds=>{
//        ds.data = map[ds.label] || [];
//      });
//      chart.update();
//    }
//    apply(C.c1, series);
//    apply(C.c2, series);
//    apply(C.c3, series);
//  }
//  tick(); setInterval(tick, 10000); // 10초마다 갱신
//</script>
//</body></html>
//""";
//    }
//}
