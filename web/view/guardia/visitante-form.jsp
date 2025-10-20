<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  String ctx = request.getContextPath();
  String nombreResidente = (String) session.getAttribute("nombreUsuario"); // RN2
  java.util.List<String> lotes = (java.util.List<String>) request.getAttribute("lotes");
  java.util.List<String> casas = (java.util.List<String>) request.getAttribute("casas");
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
      <div class="alert alert-success">
        <strong>QR emitido para:</strong> ${nombreMostrado}
      </div>
    </c:if>

    <c:if test="${not empty error}">
      <div class="alert alert-danger">${error}</div>
    </c:if>

    <form id="frmVisitante" class="row g-3" method="post" action="${pageContext.request.contextPath}/api/emit">
      <!-- Nombre visitante -->
      <div class="col-md-6">
        <label class="form-label">Nombre del visitante</label>
        <input class="form-control" name="nombre" required>
      </div>

      <!-- DPI -->
      <div class="col-md-6">
        <label class="form-label">DPI del visitante</label>
        <input class="form-control" name="dpi" pattern="[0-9]{4,25}" oninput="this.value=this.value.replace(/[^0-9]/g,'');">
      </div>

      <!-- Tipo de visita -->
      <div class="col-md-6">
        <label class="form-label">Tipo de visita</label>
        <select class="form-select" name="visitType" id="visitType" required>
          <option value="">Seleccione…</option>
          <option value="visita">Visita</option>
          <option value="por_intentos">Por intentos</option>
        </select>
      </div>

      <!-- Campo condicional: fecha -->
      <div class="col-md-6 d-none" id="fechaVisitaField">
        <label class="form-label">Fecha de visita</label>
        <input class="form-control" type="date" name="fechaVisita" id="fechaVisita">
      </div>

      <!-- Campo condicional: intentos -->
      <div class="col-md-6 d-none" id="intentosField">
        <label class="form-label">Intentos permitidos</label>
        <input class="form-control" type="number" name="usosMax" id="usosMax" min="2">
      </div>

      <!-- Residente (solo lectura) -->
      <div class="col-md-6">
        <label class="form-label">Residente que registra</label>
        <input class="form-control" readonly value="${nombreResidente}">
      </div>

      <!-- Lote -->
      <div class="col-sm-3">
        <label class="form-label">Lote</label>
        <select class="form-select" name="lote" id="lote" required>
          <option value="">Seleccione…</option>
          <% if (lotes != null) for (String l : lotes) { %>
            <option value="<%=l%>"><%=l%></option>
          <% } %>
        </select>
      </div>

      <!-- Número de casa -->
      <div class="col-sm-3">
        <label class="form-label">Número de casa</label>
        <select class="form-select" name="numeroCasa" id="numeroCasa" required>
          <option value="">Seleccione…</option>
          <% if (casas != null) for (String c : casas) { %>
            <option value="<%=c%>"><%=c%></option>
          <% } %>
        </select>
      </div>

      <!-- Correo visitante -->
      <div class="col-sm-6">
        <label class="form-label">Correo del visitante</label>
        <input class="form-control" type="email" name="email" placeholder="correo@ejemplo.com">
      </div>

      <!-- Botones -->
      <div class="col-12 d-flex gap-2 mt-3">
        <button class="btn btn-brand" type="submit" id="btnRegistrar" disabled><i class="bi bi-save me-1"></i>Registrar visita</button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/visitantes"><i class="bi bi-arrow-left me-1"></i>Volver</a>
      </div>

      <div class="col-12 d-flex gap-2 mt-2">
        <button class="btn btn-outline-primary" type="button" id="btnQR" disabled><i class="bi bi-qr-code me-1"></i>Descargar QR</button>
        <button class="btn btn-outline-danger" type="button" id="btnCancelar" disabled><i class="bi bi-x-circle me-1"></i>Cancelar visita</button>
      </div>
    </form>
  </div>
</div>

<script>
  // Mostrar campos según tipo de visita
  const tipo = document.getElementById('visitType');
  const fechaF = document.getElementById('fechaVisitaField');
  const intentosF = document.getElementById('intentosField');
  const btnRegistrar = document.getElementById('btnRegistrar');

  tipo.addEventListener('change', function() {
    fechaF.classList.add('d-none');
    intentosF.classList.add('d-none');

    if (this.value === 'visita') fechaF.classList.remove('d-none');
    if (this.value === 'por_intentos') intentosF.classList.remove('d-none');
  });

  // Habilitar registrar solo si campos obligatorios están llenos
  const form = document.getElementById('frmVisitante');
  form.addEventListener('input', () => {
    const nombre = form.nombre.value.trim();
    const tipoVal = tipo.value.trim();
    btnRegistrar.disabled = !(nombre && tipoVal);
  });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
