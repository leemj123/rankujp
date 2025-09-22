// minimal mobile nav toggle
const toggle = document.querySelector('.nav-toggle');
const panel = document.getElementById('nav-panel');
if (toggle && panel) {
  toggle.addEventListener('click', () => {
    const open = toggle.getAttribute('aria-expanded') === 'true';
    toggle.setAttribute('aria-expanded', String(!open));
    panel.hidden = open;
  });
}
