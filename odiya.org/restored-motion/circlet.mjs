/* Browser port of Circlet 1.0's pixel routine, Nakagawa Masami (1999).
 * Reference SHA256: 41c12abb84dfb4f834462e6a12f08a6e28af99dde5bd996bb7f070f04eb7342a.
 * Source settings: image=asato_trial/asato.jpg, type=2, bgColor=FFFF00, 350x50.
 * Bytecode defaults: speed=1 source pixel, sleep=20ms, merge=50 percent.
 * No legacy Java runs. Image decoding and frame scheduling use the browser.
 */
export function circletFrame(source, sw, sh, offset, output = new Uint8ClampedArray(350 * 50 * 4)) {
  const width = 350, height = 50, half = Math.floor(sw / 2);
  const front = new Int32Array(width), back = new Int32Array(width);
  for (let x = 0; x < width; x++) {
    const t = Math.acos(1 - 2 * x / width) / Math.PI;
    front[x] = ((Math.trunc(offset + half * t) % sw) + sw) % sw;
    back[x] = ((Math.trunc(sw + offset - half * t) % sw) + sw) % sw;
  }
  for (let y = 0; y < height; y++) {
    const row = Math.floor(y * sh / height) * sw;
    for (let x = 0; x < width; x++) {
      const a = (row + front[x]) * 4, b = (row + back[x]) * 4;
      const p = (y * width + x) * 4;
      output[p] = Math.min(source[a], Math.floor((255 + source[b]) / 2));
      output[p + 1] = Math.min(source[a + 1], Math.floor((255 + source[b + 1]) / 2));
      output[p + 2] = Math.min(source[a + 2], Math.floor(source[b + 2] / 2));
      output[p + 3] = 255;
    }
  }
  return output;
}

async function startPreview() {
  const canvas = document.querySelector('#circlet-preview');
  const image = document.querySelector('#circlet-still');
  const button = document.querySelector('#circlet-motion');
  const wrapper = document.querySelector('#circlet-container');
  if (!canvas || !image || !button || !wrapper) return;
  try {
    await image.decode();
    const sourceCanvas = document.createElement('canvas');
    sourceCanvas.width = image.naturalWidth; sourceCanvas.height = image.naturalHeight;
    const sourceContext = sourceCanvas.getContext('2d', {willReadFrequently: true});
    sourceContext.drawImage(image, 0, 0);
    const pixels = sourceContext.getImageData(0, 0, sourceCanvas.width, sourceCanvas.height).data;
    const context = canvas.getContext('2d');
    const frame = context.createImageData(350, 50);
    const preference = matchMedia('(prefers-reduced-motion: reduce)');
    let playing = !preference.matches, skipped = false, pending = 0;
    let frameNumber = 0, lastTime = null, remainder = 0;
    function draw() {
      circletFrame(pixels, sourceCanvas.width, sourceCanvas.height, frameNumber, frame.data);
      context.putImageData(frame, 0, 0);
      canvas.dataset.frame = String(frameNumber);
    }
    function tick(time) {
      pending = 0;
      if (!playing || skipped || document.hidden) return;
      if (lastTime !== null) remainder += time - lastTime;
      lastTime = time;
      const steps = Math.floor(remainder / 20);
      if (steps) {
        remainder -= steps * 20;
        frameNumber = (frameNumber + steps) % sourceCanvas.width;
        draw();
      }
      pending = requestAnimationFrame(tick);
    }
    function sync() {
      cancelAnimationFrame(pending); pending = 0; lastTime = null;
      button.textContent = playing ? 'Pause Asato' : 'Play Asato';
      canvas.dataset.state = !playing ? 'paused' : skipped || document.hidden ? 'suspended' : 'playing';
      if (playing && !skipped && !document.hidden) pending = requestAnimationFrame(tick);
    }
    button.addEventListener('click', () => {playing = !playing; sync();});
    preference.addEventListener('change', () => {playing = !preference.matches; sync();});
    document.addEventListener('visibilitychange', sync);
    if ('oncontentvisibilityautostatechange' in wrapper) {
      wrapper.addEventListener('contentvisibilityautostatechange', event => {skipped = event.skipped; sync();});
    } else if ('IntersectionObserver' in window) {
      new IntersectionObserver(entries => {skipped = !entries[0].isIntersecting; sync();}, {rootMargin:'200px'}).observe(wrapper);
    }
    draw(); image.hidden = true; canvas.hidden = false; button.hidden = false; sync();
  } catch (error) {
    // Preserve the unaltered original image if decoding or canvas fails.
    canvas.hidden = true; image.hidden = false; button.hidden = true;
    wrapper.dataset.state = 'static-fallback';
    console.error('Circlet preview: original still image retained.', error);
  }
}
if (typeof document !== 'undefined') startPreview();
