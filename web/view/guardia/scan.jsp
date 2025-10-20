<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  // Autorización: ADMIN(1) y GUARDIA(2)
  String ctx = request.getContextPath();
  Integer rol = (Integer) session.getAttribute("rol");
  if (rol == null || (rol != 1 && rol != 2)) {
    response.sendRedirect(ctx + "/login");
    return;
  }
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Escaneo de QR</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 900px; margin: 18px auto; padding: 0 12px; }
    #reader { width: 100%; max-width: 720px; margin: 0 auto; }
    #status { font-weight: 600; margin-top: 10px; }
    .ok { color: #0a7e07; } .fail { color: #c62828; }
    #overlay { display:none; position:fixed; inset:0; background:rgba(0,0,0,.6); color:#fff; font-size:28px; text-align:center; padding-top:20vh; z-index:9999; }
    .topbar { display:flex; align-items:center; gap:12px; margin-bottom:12px; }
    .topbar a { text-decoration:none; font-size:14px; }
  </style>
</head>
<body>
  <div class="topbar">
    <a href="<%=ctx%>">&larr; Volver</a>
    <h3 style="margin:0">Escaneo y validación de QR</h3>
  </div>

  <div id="reader"></div>
  <div id="status"></div>
  <div id="overlay"></div>

  <script src="https://unpkg.com/html5-qrcode"></script>
  <script>
    const ctx  = "<%=ctx%>";
    const base = location.origin + ctx;
    const statusEl = document.getElementById('status');
    const overlay  = document.getElementById('overlay');

    const params = new URLSearchParams(location.search);
    const dirParam = (params.get('dir') || '').toLowerCase(); // 'in' | 'out' | ''
    const auto = params.get('auto') === '1';

    function setStatus(msg, ok) {
      statusEl.className = ok === true ? 'ok' : ok === false ? 'fail' : '';
      statusEl.textContent = msg;
    }
    function showOverlay(text, ok) {
      overlay.style.display = 'block';
      overlay.style.background = ok ? 'rgba(0,128,0,.75)' : 'rgba(160,0,0,.75)';
      overlay.textContent = text;
      setTimeout(()=> overlay.style.display='none', 1200);
    }
    function extractToken(text) {
      try { const u = new URL(text); return u.searchParams.get('token') || text; }
      catch { return text; }
    }
    async function validate(token, dir) {
      // enviamos token + dirección (entrada/salida) + fuente 'cam'
      const url = base + "/api/validate?token=" + encodeURIComponent(token)
                           + (dir ? "&dir=" + encodeURIComponent(dir) : "")
                           + "&origin=cam";
      const resp = await fetch(url, { method: "GET" });
      if (!resp.ok) throw new Error("HTTP " + resp.status);
      return await resp.json(); // {valid, reason, used_count?}
    }

    const config = {
      fps: 12, qrbox: { width: 320, height: 320 }, aspectRatio: 1.3333,
      rememberLastUsedCamera: true,
      videoConstraints: { facingMode: "environment", width:{ideal:1280}, height:{ideal:720} },
      experimentalFeatures: { useBarCodeDetectorIfSupported: true }
    };

    let html5QrCode;
    async function startScanner() {
      try {
        setStatus("Inicializando cámara…", null);
        html5QrCode = new Html5Qrcode("reader");
        const cams = await Html5Qrcode.getCameras();
        if (!cams || !cams.length) { setStatus("No se detectaron cámaras.", false); return; }
        const camId = (cams.find(c => /back|rear|environment/i.test(c.label)) || cams[0]).id;

        const onScanSuccess = async (decodedText) => {
          try { await html5QrCode.stop(); } catch (_) {}
          setStatus("Procesando…", null);
          const token = extractToken(decodedText);
          try {
            const j = await validate(token, dirParam);
            if (j.valid) {
              const info = j.used_count != null ? ` (usos: ${j.used_count}/2)` : "";
              setStatus("VÁLIDO — apertura en curso…" + info, true);
              showOverlay("ACCESO PERMITIDO", true);
            } else {
              setStatus("DENEGADO: " + (j.reason || "desconocido"), false);
              showOverlay("ACCESO DENEGADO", false);
            }
          } catch (e) {
            setStatus("Error al validar: " + e.message, false);
            showOverlay("ERROR DE VALIDACIÓN", false);
          }
          // Evitar dobles lecturas: reanudar tras 1.5s
          setTimeout(() => startScanner(), 1500);
        };

        const onScanFailure = (_) => {};
        await html5QrCode.start(camId, config, onScanSuccess, onScanFailure);
        setStatus("Apunta un QR a la cámara…", null);
      } catch (e) {
        setStatus("No se pudo iniciar la cámara: " + e.message, false);
      }
    }

    window.addEventListener('load', async () => {
      if (auto) {
        // Pide permiso “silencioso” y libera para no bloquear start()
        try {
          const s = await navigator.mediaDevices.getUserMedia({ video: { width:{ideal:1280}, height:{ideal:720}, facingMode:"environment" }, audio:false });
          s.getTracks().forEach(t => t.stop());
        } catch (_) {}
        startScanner();
      } else {
        setStatus("Listo. Presente un QR para validar.", null);
        startScanner();
      }
    });
  </script>
</body>
</html>
