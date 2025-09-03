<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath(); // ej. /MiCasitaSegura
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Escaneo de QR</title>
  <link href="<%=ctx%>/css/bootstrap.css" rel="stylesheet">
  <style>
    body { font-family: system-ui, sans-serif; }
    .wrap { max-width: 840px; margin: 18px auto; padding: 0 12px; }
    #preview { width: 100%; max-width: 640px; border: 1px solid #ddd; border-radius: 10px; }
    #status { font-weight: 600; margin-top: 10px; }
    .ok { color: #0a7e07; }
    .fail { color: #c62828; }
  </style>
</head>
<body class="bg-light">
<div class="wrap">
  <h3 class="mb-3">Escaneo y validación de QR</h3>

  <div class="mb-2">
    <label class="form-label">Cámara</label>
    <div class="d-flex gap-2">
      <select id="cameras" class="form-select" style="max-width:420px"></select>
      <button id="startBtn" class="btn btn-primary">Iniciar</button>
      <button id="stopBtn"  class="btn btn-outline-secondary" disabled>Detener</button>
      <div class="form-check ms-2 align-self-center">
        <input class="form-check-input" type="checkbox" id="autoContinue" checked>
        <label class="form-check-label" for="autoContinue">Seguir leyendo</label>
      </div>
    </div>
  </div>

  <video id="preview" playsinline muted></video>
  <div id="status" class="mt-2"></div>

  <div class="mt-3">
    <a class="btn btn-link" href="<%=ctx%>/view/guardia/control.jsp">← Volver al panel</a>
  </div>
</div>

<script src="https://unpkg.com/@zxing/library@latest"></script>
<script>
  const ctx = "<%=ctx%>";
  const video = document.getElementById('preview');
  const camerasSel = document.getElementById('cameras');
  const startBtn = document.getElementById('startBtn');
  const stopBtn  = document.getElementById('stopBtn');
  const statusEl = document.getElementById('status');
  const autoContinue = document.getElementById('autoContinue');

  const codeReader = new ZXing.BrowserMultiFormatReader();
  let controls = null;

  function setStatus(msg, ok) {
    statusEl.className = ok === true ? 'ok' : ok === false ? 'fail' : '';
    statusEl.textContent = msg;
  }

  async function listCameras() {
    try {
      const devices = await codeReader.listVideoInputDevices();
      camerasSel.innerHTML = '';
      devices.forEach((d, i) => {
        const opt = document.createElement('option');
        opt.value = d.deviceId;
        opt.textContent = d.label || `Cámara ${i+1}`;
        camerasSel.appendChild(opt);
      });
      if (devices.length === 0) setStatus('No se detectaron cámaras. Revisa permisos del navegador.', false);
    } catch (e) {
      setStatus('Error enumerando cámaras: ' + e, false);
    }
  }

  function extractToken(text) {
    // Si el QR es una URL con ?token=..., usa ese token. Si no, usa el texto tal cual.
    try { const u = new URL(text); return u.searchParams.get('token') || text; }
    catch { return text; }
  }

  async function validate(token) {
        const base = location.origin + ctx; // ej. http://localhost:8080/MiCasitaSegura
        const url  = `${base}/api/validate?token=${encodeURIComponent(token)}`;
    const resp = await fetch(url, { method: 'GET' });
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    return await resp.json(); // {valid, reason}
  }

  async function start() {
    setStatus('Iniciando cámara…', null);
    const deviceId = camerasSel.value || undefined;
    controls = await codeReader.decodeFromVideoDevice(deviceId, video, async (result, err, ctl) => {
      if (result) {
        ctl.stop(); // no leer el mismo cuadro varias veces
        setStatus('Procesando…', null);
        const token = extractToken(result.getText());
        try {
          const json = await validate(token);
          if (json.valid) {
            setStatus('✅ VÁLIDO — apertura en curso…', true);
          } else {
            setStatus('❌ DENEGADO: ' + (json.reason || 'desconocido'), false);
          }
        } catch (e) {
          setStatus('Error al validar: ' + e.message, false);
        }
        if (autoContinue.checked) setTimeout(start, 1500);
        else { startBtn.disabled = false; stopBtn.disabled = true; }
      }
    });
    startBtn.disabled = true;
    stopBtn.disabled = false;
    setStatus('Apunta un QR a la cámara…', null);
  }

  function stop() {
    if (controls) { controls.stop(); controls = null; }
    startBtn.disabled = false;
    stopBtn.disabled = true;
    setStatus('Cámara detenida.', null);
  }

  startBtn.addEventListener('click', start);
  stopBtn .addEventListener('click', stop);
  window.addEventListener('load', async () => {
  await listCameras();   // llena el <select>
  start();               // arranca de una
});
</script>
</body>
</html>
