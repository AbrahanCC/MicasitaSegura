<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String ctx = request.getContextPath(); // ej. /MiCasitaSegura
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
    #overlay {
      display:none; position:fixed; inset:0; background:rgba(0,0,0,.6);
      color:#fff; font-size:28px; text-align:center; padding-top:20vh; z-index:9999;
    }
  </style>
</head>
<body>
  <h3>Escaneo y validación de QR</h3>

  <div id="reader"></div>
  <div id="status"></div>
  <div id="overlay"></div>

  <!-- Librería más tolerante de lectura -->
  <script src="https://unpkg.com/html5-qrcode"></script>
  <script>
    const ctx  = "<%=ctx%>";
    const base = location.origin + ctx;

    const statusEl = document.getElementById('status');
    const overlay  = document.getElementById('overlay');
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
    async function validate(token) {
      const url = base + "/api/validate?token=" + encodeURIComponent(token);
      const resp = await fetch(url, { method: "GET" });
      if (!resp.ok) throw new Error("HTTP " + resp.status);
      return await resp.json(); // {valid, reason}
    }

    // Config: mejor lectura en cámaras modestas
    const config = {
      fps: 12,
      qrbox: { width: 320, height: 320 },        // zona de enfoque
      aspectRatio: 1.3333,                        // 4:3 ayuda a cámaras integradas
      rememberLastUsedCamera: true,
      // pedir más resolución y back camera si existe
      videoConstraints: {
        facingMode: "environment",
        width:  { ideal: 1280 },
        height: { ideal: 720 }
      },
      experimentalFeatures: { useBarCodeDetectorIfSupported: true }
    };

    let html5QrCode;

    async function startScanner() {
      try {
        setStatus("Inicializando cámara…", null);

        html5QrCode = new Html5Qrcode("reader");

        // Selección de cámara
        const cams = await Html5Qrcode.getCameras();
        if (!cams || !cams.length) {
          setStatus("No se detectaron cámaras. Revisa permisos del navegador.", false);
          return;
        }
        const camId = (cams.find(c => /back|rear|environment/i.test(c.label)) || cams[0]).id;

        // Callback de éxito
        const onScanSuccess = async (decodedText, decodedResult) => {
          try {
            await html5QrCode.stop(); // detenemos para no leer múltiples veces
          } catch (_) {}
          setStatus("Procesando…", null);

          const token = extractToken(decodedText);
          try {
            const j = await validate(token);
            if (j.valid) {
              setStatus("✅ VÁLIDO — apertura en curso…", true);
              showOverlay("ACCESO PERMITIDO", true);
            } else {
              setStatus("❌ DENEGADO: " + (j.reason || "desconocido"), false);
              showOverlay("ACCESO DENEGADO", false);
            }
          } catch (e) {
            setStatus("Error al validar: " + e.message, false);
            showOverlay("ERROR DE VALIDACIÓN", false);
          }

          // Reanudar lectura tras 1.5 s
          setTimeout(() => startScanner(), 1500);
        };

        // Callback de fallo (ruido normal mientras no hay QR)
        const onScanFailure = (err) => { /* no-op */ };

        await html5QrCode.start(camId, config, onScanSuccess, onScanFailure);
        setStatus("Apunta un QR a la cámara…", null);

      } catch (e) {
        setStatus("No se pudo iniciar la cámara: " + e.message, false);
      }
    }

    window.addEventListener('load', async () => {
      // Permiso previo ayuda a que el navegador ofrezca mejor resolución
      try {
        const s = await navigator.mediaDevices.getUserMedia({ video: { width: {ideal:1280}, height:{ideal:720}, facingMode:"environment" }, audio:false });
        s.getTracks().forEach(t => t.stop());
      } catch (_) {}
      const auto = new URL(location.href).searchParams.get('auto') === '1';
      if (auto) startScanner();
      else setStatus("Listo. Agrega ?auto=1 para iniciar automáticamente.", null);
    });
  </script>
</body>
</html>
