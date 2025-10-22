<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Pago, model.PagoTipo, model.MetodoPago, model.Usuario, java.util.*, java.time.*, java.time.format.*"%>

<!DOCTYPE html>
<html>
<head>
  <title>Gestionar Pagos</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <style>
    .card { max-width: 820px; margin: auto; }
    .field-readonly { background-color: #f8f9fa; }
  </style>
</head>
<body class="bg-light">

<jsp:include page="/view/_menu.jsp"/>

<div class="container my-4">
  <h3 class="mb-3">Gestionar Pagos</h3>

  <%
    List<PagoTipo> tipos   = (List<PagoTipo>) request.getAttribute("tipos");
    List<MetodoPago> metodos = (List<MetodoPago>) request.getAttribute("metodos");
    Pago calculo = (Pago) request.getAttribute("calculo"); // solo tras "consultar"
    Usuario usr = (Usuario) session.getAttribute("usuario");

    String flashOk  = (String) session.getAttribute("flash_ok");  if (flashOk != null)  session.removeAttribute("flash_ok");
    String flashErr = (String) session.getAttribute("flash_err"); if (flashErr != null) session.removeAttribute("flash_err");
  %>

  <% if (flashOk != null)  { %><div class="alert alert-success"><%=flashOk%></div><% } %>
  <% if (flashErr != null) { %><div class="alert alert-danger">Error: <%=flashErr%></div><% } %>

  <div class="card shadow-sm">
    <div class="card-body">
      <form id="pagoForm" action="${pageContext.request.contextPath}/residente/pagos" method="post" novalidate>
        <input type="hidden" name="action" id="action" value="<%= (calculo == null) ? "consultar" : "registrar" %>">

        <!-- RN1: Tipo de pago -->
        <div class="mb-3">
          <label class="form-label">Tipo de pago <span class="text-danger">*</span></label>
          <select name="tipo_id" id="tipoPago" class="form-select" <%= (calculo!=null) ? "disabled" : "" %> required>
            <option value="">-- Seleccionar --</option>
            <% if (tipos != null) {
                 for (PagoTipo t : tipos) {
                   boolean sel = (calculo != null && calculo.getTipoId() == t.getId());
            %>
              <option value="<%=t.getId()%>" data-monto="<%=t.getMonto()%>" <%= sel ? "selected" : "" %>>
                <%= t.getNombre() %>
              </option>
            <% } } %>
          </select>
          <div class="form-text">Seleccione el tipo de pago y presione “Consultar”.</div>
        </div>

        <!-- Cuando ya consultó, el select está disabled. Enviamos el tipo por un hidden -->
        <% if (calculo != null) { %>
          <input type="hidden" name="tipo_id" value="<%=calculo.getTipoId()%>">
        <% } %>

        <!-- Botón FA01 / RN6: Consultar -->
        <div class="mb-4">
          <% if (calculo == null) { %>
            <button type="submit" id="btnConsultar" class="btn btn-outline-primary" disabled>
              <i class="bi bi-search"></i> Consultar
            </button>
          <% } %>
        </div>

        <% if (calculo != null) {
             Locale esGT = new Locale("es","GT");
             String fechaPagoTxt = (calculo.getFechaPago()!=null)
                 ? calculo.getFechaPago().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                 : "";
             YearMonth ym = YearMonth.from(calculo.getMesAPagar());
             String mesAPagarTxt = ym.getMonth().getDisplayName(TextStyle.FULL, esGT) + " " + ym.getYear();
             String nombre = (usr!=null)
                 ? ((usr.getNombre()==null?"":usr.getNombre()) + " " + (usr.getApellidos()==null?"":usr.getApellidos())).trim()
                 : "Residente";
        %>

        <!-- Bloque de salida tras CONSULTAR -->
        <div class="border rounded p-3 bg-light mb-3">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Nombre</label>
              <input type="text" class="form-control field-readonly" value="<%=nombre%>" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label">Fecha de pago</label>
              <input type="text" class="form-control field-readonly" value="<%=fechaPagoTxt%>" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label">Mes a pagar</label>
              <input type="text" class="form-control field-readonly" value="<%=mesAPagarTxt%>" readonly>
            </div>
            <div class="col-md-3">
              <label class="form-label">Mora</label>
              <input type="text" class="form-control field-readonly" value="<%=String.format("Q%.2f", calculo.getRecargo())%>" readonly>
            </div>
            <div class="col-md-3">
              <label class="form-label">Total</label>
              <input type="text" class="form-control field-readonly fw-semibold" value="<%=String.format("Q%.2f", calculo.getTotal())%>" readonly>
            </div>
          </div>
        </div>

        <!-- Observaciones (obligatorio) -->
        <div class="mb-3">
          <label class="form-label">Observaciones <span class="text-danger">*</span></label>
          <textarea name="observaciones" id="observaciones" class="form-control" rows="2" required></textarea>
        </div>

        <!-- Método de pago -->
        <div class="mb-2">
          <label class="form-label">Método de pago</label>
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="usar_guardada" value="si" id="usarGuardada" checked>
            <label class="form-check-label" for="usarGuardada">Usar tarjeta guardada</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="usar_guardada" value="no" id="usarNueva">
            <label class="form-check-label" for="usarNueva">Nueva tarjeta</label>
          </div>
        </div>

        <!-- Tarjeta guardada -->
        <div id="guardadaBlock" class="mb-3">
          <label class="form-label">Seleccionar tarjeta guardada</label>
          <select name="metodo_pago_id" id="metodo_pago_id" class="form-select">
            <option value="">-- Seleccionar --</option>
            <% if (metodos != null) for (MetodoPago m : metodos) { %>
              <option value="<%=m.getId()%>"><%=m.getMarca()%> **** **** **** <%=m.getUltimos4()%> (exp <%=m.getMesExpiracion()%>/<%=m.getAnioExpiracion()%>)</option>
            <% } %>
          </select>
        </div>

        <!-- Nueva tarjeta -->
        <div id="nuevaBlock" class="mb-3" style="display:none;">
          <div class="row g-2">
            <div class="col-12">
              <label class="form-label">Número de tarjeta <span class="text-danger">*</span></label>
              <input type="text" name="numero_tarjeta" id="numero_tarjeta" class="form-control"
                     maxlength="19" minlength="13" placeholder="#### #### #### ####"
                     pattern="[0-9\s]{13,19}" title="Solo números (13 a 19 dígitos)">
            </div>
            <div class="col-md-6">
              <label class="form-label">Nombre del titular <span class="text-danger">*</span></label>
              <input type="text" name="nombre_titular" id="nombre_titular" class="form-control"
                     pattern="[A-Za-zÁÉÍÓÚáéíóúÑñ\s]+" title="Solo letras y espacios">
            </div>
            <div class="col-md-4">
              <label class="form-label">Fecha de vencimiento <span class="text-danger">*</span></label>
              <input type="month" name="fecha_vencimiento" id="fecha_vencimiento" class="form-control"
                     min="<%= java.time.YearMonth.now() %>" required>
            </div>
            <div class="col-md-2">
              <label class="form-label">CVV <span class="text-danger">*</span></label>
              <input type="password" name="cvv" id="cvv" class="form-control"
                     maxlength="3" minlength="3" pattern="[0-9]{3}" title="3 números">
            </div>
          </div>
          <div class="form-check mt-2">
            <input class="form-check-input" type="checkbox" name="guardar_nueva" value="si" id="guardarNueva">
            <label class="form-check-label" for="guardarNueva">Guardar esta tarjeta</label>
          </div>
        </div>

        <!-- Botones -->
        <div class="d-flex gap-2 justify-content-end mt-4">
          <a href="${pageContext.request.contextPath}/residente/pagos?action=listar"
             class="btn btn-outline-secondary"
             onclick="return confirm('¿Desea cancelar el pago?');">
            Cancelar pago
          </a>
          <button type="button" class="btn btn-outline-warning" id="btnLimpiar">Limpiar</button>
          <button type="submit" id="btnRegistrar" class="btn btn-primary" disabled>
            <i class="bi bi-credit-card"></i> Registrar pago
          </button>
        </div>

        <% } %> <!-- fin bloque calculo != null -->
      </form>
    </div>
  </div>
</div>

<script>
  // Habilitar Consultar si hay tipo seleccionado
  (function initConsultar() {
    const tipo = document.getElementById('tipoPago');
    const btnCons = document.getElementById('btnConsultar');
    if (tipo && btnCons) {
      btnCons.disabled = !tipo.value;
      tipo.addEventListener('change', () => { btnCons.disabled = !tipo.value; });
    }
  })();

  // Mostrar/ocultar bloques de tarjeta + habilitar Registrar (RN3)
  (function pagosUI() {
    const usarGuardada = document.getElementById('usarGuardada');
    const usarNueva    = document.getElementById('usarNueva');
    const guardada     = document.getElementById('guardadaBlock');
    const nueva        = document.getElementById('nuevaBlock');
    const btnRegistrar = document.getElementById('btnRegistrar');

    function toggleMetodo() {
      if (!usarGuardada) return; // aún no está en la fase post-consulta
      const g = usarGuardada.checked;
      guardada.style.display = g ? 'block' : 'none';
      nueva.style.display    = g ? 'none'  : 'block';
      evalRegistrarEnabled();
    }

    function evalRegistrarEnabled() {
      if (!btnRegistrar) return;

      const obs = document.getElementById('observaciones');
      let ok = obs && obs.value.trim().length > 0;

      if (usarGuardada && usarGuardada.checked) {
        const sel = document.getElementById('metodo_pago_id');
        ok = ok && sel && sel.value !== '';
      } else if (usarNueva && usarNueva.checked) {
        const num = document.getElementById('numero_tarjeta');
        const nom = document.getElementById('nombre_titular');
        const venc = document.getElementById('fecha_vencimiento');
        const cvv = document.getElementById('cvv');
        ok = ok &&
             num && /^[0-9\s]{13,19}$/.test(num.value.trim()) &&
             nom && nom.value.trim().length >= 2 &&
             venc && venc.value &&
             cvv && /^[0-9]{3}$/.test(cvv.value.trim());
      }
      btnRegistrar.disabled = !ok;
    }

    if (usarGuardada && usarNueva) {
      usarGuardada.addEventListener('change', toggleMetodo);
      usarNueva.addEventListener('change', toggleMetodo);
      document.addEventListener('input', evalRegistrarEnabled);
      toggleMetodo();
    }

    const btnLimpiar = document.getElementById('btnLimpiar');
    if (btnLimpiar) {
      btnLimpiar.addEventListener('click', () => {
        window.location.href = '${pageContext.request.contextPath}/residente/pagos?action=form';
      });
    }
  })();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
