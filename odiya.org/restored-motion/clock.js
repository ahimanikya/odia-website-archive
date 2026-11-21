/* Read-only recreation of Bill's Clock (Bill Giel, 1996).
 * Geometry, colours and the saved TIMEZONE=8.5 are documented in source notes.
 * No requests, storage, forms, navigation or historical executable code.
 */
(() => {
  const clock = document.getElementById('restored-clock');
  const control = document.getElementById('clock-motion');
  if (!clock || !control) return;
  const reduced = matchMedia('(prefers-reduced-motion: reduce)');
  let paused = reduced.matches;
  let timer;
  function draw() {
    // The original applet defines positive TIMEZONE as west of Greenwich.
    const time = new Date(Date.now() - 8.5 * 3600000);
    const hours = time.getUTCHours(), minutes = time.getUTCMinutes();
    const values = { hour: (hours % 12 + minutes / 60) * 30,
      minute: minutes * 6, second: time.getUTCSeconds() * 6 };
    for (const [name, angle] of Object.entries(values)) {
      clock.querySelector('[data-hand="' + name + '"]').setAttribute('transform', 'rotate(' + angle + ' 60 60)');
    }
    clock.querySelector('[data-date]').textContent = String(time.getUTCDate());
  }
  function sync() {
    clearInterval(timer);
    control.textContent = paused ? 'Resume clock' : 'Pause clock';
    if (!paused && !document.hidden) {
      draw();
      timer = setInterval(draw, 1000);
    }
  }
  control.hidden = false;
  draw();
  control.addEventListener('click', () => { paused = !paused; sync(); });
  reduced.addEventListener('change', () => { paused = reduced.matches; sync(); });
  document.addEventListener('visibilitychange', sync);
  sync();
})();
