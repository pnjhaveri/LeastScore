(function initLeastScore() {
  async function api(path, opts = {}) {
    const res = await fetch(path, { headers: { 'content-type': 'application/json' }, ...opts });
    const ct = res.headers.get('content-type') || '';
    const body = ct.includes('application/json') ? await res.json() : await res.text();
    if (!res.ok) throw new Error(body?.error || body || `HTTP ${res.status}`);
    return body;
  }

  function qs(sel) {
    const el = document.querySelector(sel);
    if (!el) throw new Error(`Missing element ${sel}`);
    return el;
  }

  function setToast(msg, kind = 'ok') {
    const el = document.querySelector('[data-toast]');
    if (!el) return;
    el.textContent = msg || '';
    el.classList.remove('ok', 'err');
    if (msg) el.classList.add(kind === 'err' ? 'err' : 'ok');
  }

  function setButtonLoading(btn, loading) {
    if (!btn) return;
    btn.disabled = Boolean(loading);
    btn.classList.toggle('loading', Boolean(loading));
  }

  async function runAction(btn, fn, { successMessage } = {}) {
    try {
      setButtonLoading(btn, true);
      const result = await fn();
      if (successMessage) setToast(successMessage, 'ok');
      return result;
    } catch (e) {
      setToast(e?.message || String(e), 'err');
      throw e;
    } finally {
      setButtonLoading(btn, false);
    }
  }

  async function ensureSessionUserId() {
    const s = await api('/api/session');
    return s.userId || null;
  }

  function stompUrl() {
    return `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`;
  }

  window.LeastScore = {
    api,
    qs,
    setToast,
    runAction,
    ensureSessionUserId,
    stompUrl,
  };
})();

