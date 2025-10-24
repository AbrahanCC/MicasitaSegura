<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  String ctx = request.getContextPath();
  java.util.List<String> lotes = (java.util.List<String>) request.getAttribute("lotes");
  java.util.List<String> casas = (java.util.List<String>) request.getAttribute("casas");
  java.util.List<String> tiposVisita = (java.util.List<String>) request.getAttribute("tiposVisita");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Registrar visitante • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/view/_menu.jsp" />

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:880px;">

    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-person-plus"></i></div>
      <div>
        <h4 class="mb-0">Registro de visitante</h4>
        <small class="text-muted">Completa los datos del visitante</small>
      </div>
    </div>

    <c:if test="${ok}">
      <div class="alert alert-success"><strong>QR emitido para:</strong> ${nombreMostrado}</div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert alert-danger">${error}</div>
    </c:if>

    <form id="frmVisitante" class="row g-3" method="post" action="<%=ctx%>/api/emit">
      <div class="col-md-6">
        <label class="form-label">Nombre del visitante</label>
        <input class="form-control" name="nombre" required>
      </div>

      <div class="col-md-6">
        <label class="form-label">DPI del visitante</label>
        <input class="form-control" name="dpi" pattern="[0-9]{4,25}" oninput="this.value=this.value.replace(/[^0-9]/g,'');">
      </div>

      <div class="col-md-6">
        <label class="form-label">Tipo de visita</label>
        <select class="form-select" id="tipoVisitaUi" required>
          <option value="">Seleccione…</option>
          <%
            if (tiposVisita != null) {
              for (String v : tiposVisita) {
          %>
                <option value="<%=v%>"><%=v%></option>
          <%
              }
            }
          %>
        </select>
        <input type="hidden" name="tipoVisita" id="tipoVisita">
      </div>

      <div class="col-md-6 d-none" id="fechaVisitaField">
        <label class="form-label">Fecha de visita</label>
        <input class="form-control" type="date" name="fechaVisita" id="fechaVisita">
      </div>

      <div class="col-md-6 d-none" id="intentosField">
        <label class="form-label">Intentos permitidos (mínimo 2)</label>
        <input class="form-control" type="number" name="usosMax" id="usosMax" min="2">
      </div>

      <div class="col-sm-3">
        <label class="form-label">Lote</label>
        <select class="form-select" name="lote" id="lote" required>
          <option value="">Seleccione…</option>
          <% if (lotes != null) for (String l : lotes) { %>
            <option value="<%=l%>"><%=l%></option>
          <% } %>
        </select>
      </div>

      <div class="col-sm-3">
        <label class="form-label">Número de casa</label>
        <select class="form-select" name="numeroCasa" id="numeroCasa" required>
          <option value="">Seleccione…</option>
          <% if (casas != null) for (String c : casas) { %>
            <option value="<%=c%>"><%=c%></option>
          <% } %>
        </select>
      </div>

      <div class="col-sm-6">
        <label class="form-label">Correo del visitante</label>
        <input class="form-control" type="email" name="correo" placeholder="correo@ejemplo.com">
      </div>

      <div class="col-12">
        <label class="form-label">Motivo</label>
        <input class="form-control" name="motivo" maxlength="200" placeholder="Motivo de la visita">
      </div>

      <div class="col-12 d-flex gap-2 mt-3">
        <button class="btn btn-brand" type="submit" id="btnRegistrar" disabled>
          <i class="bi bi-save me-1"></i>Registrar visita
        </button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/visitantes">
          <i class="bi bi-x-circle me-1"></i>Cerrar
        </a>
      </div>
    </form>
  </div>
</div>

<script>
  const tipoUi = document.getElementById('tipoVisitaUi');
  const tipoReal = document.getElementById('tipoVisita');
  const fechaF = document.getElementById('fechaVisitaField');
  const intentosF = document.getElementById('intentosField');
  const btnRegistrar = document.getElementById('btnRegistrar');

  function normalizeTipo(val) {
    const v = (val || '').toString().trim().toLowerCase();
    if (v.includes('visit')) return 'visita';
    if (v.includes('intent')) return 'por_intentos';
    if (v === 'visita' || v === 'por_intentos') return v;
    return '';
  }

  function toggleExtras() {
    const n = normalizeTipo(tipoUi.value);
    tipoReal.value = n;

    fechaF.classList.add('d-none');
    intentosF.classList.add('d-none');
    if (n === 'visita') fechaF.classList.remove('d-none');
    if (n === 'por_intentos') intentosF.classList.remove('d-none');

    const form = document.getElementById('frmVisitante');
    const nombre = (form.nombre.value || '').trim();
    btnRegistrar.disabled = !(nombre && n);
  }
  tipoUi.addEventListener('change', toggleExtras);
  document.getElementById('frmVisitante').addEventListener('input', toggleExtras);
  toggleExtras();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
