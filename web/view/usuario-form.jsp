<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  String ctx = request.getContextPath();
  // Catálogos como List<String>
  java.util.List<String> lotes = (java.util.List<String>) request.getAttribute("lotes");   // A..Z
  java.util.List<String> casas = (java.util.List<String>) request.getAttribute("casas");   // 001..050
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Gestión de Usuarios • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/view/_menu.jsp"/>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:880px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-person-lines-fill"></i></div>
      <div>
        <h4 class="mb-0">${u.id > 0 ? "Editar usuario" : "Nuevo usuario"}</h4>
        <small class="text-muted">Complete los datos obligatorios</small>
      </div>
    </div>

    <!-- ✅ MENSAJES FLASH (éxito / error) -->
    <c:if test="${not empty sessionScope.flashOk}">
      <div class="alert alert-success">${sessionScope.flashOk}</div>
      <c:remove var="flashOk" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
      <div class="alert alert-danger">${sessionScope.flashError}</div>
      <c:remove var="flashError" scope="session"/>
    </c:if>

    <c:if test="${not empty error}">
      <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/usuarios">
      <input type="hidden" name="id" value="${u.id}"/>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label">DPI del residente</label>
          <input class="form-control" name="dpi" value="${u.dpi}" required pattern="[0-9]{4,25}">
        </div>
        <div class="col-md-6">
          <label class="form-label">Nombre</label>
          <input class="form-control" name="nombre" value="${u.nombre}" required>
        </div>

        <div class="col-md-6">
          <label class="form-label">Apellidos</label>
          <input class="form-control" name="apellidos" value="${u.apellidos}" required>
        </div>

        <div class="col-md-6">
          <label class="form-label">Correo</label>
          <input class="form-control" type="email" name="correo" value="${u.correo}" required>
        </div>

        <div class="col-md-6">
          <label class="form-label">Usuario</label>
          <input class="form-control" name="username" value="${u.username}" required>
        </div>

        <div class="col-md-6">
          <label class="form-label">Contraseña</label>
          <input class="form-control" type="password" name="pass">
        </div>

        <div class="col-md-6">
          <label class="form-label">Rol del usuario</label>
          <select class="form-select" name="rolId" id="rolId" required>
            <option value="">Seleccione...</option>
            <c:forEach var="r" items="${roles}">
              <option value="${r.id}" ${r.id == u.rolId ? "selected" : ""}>${r.nombre}</option>
            </c:forEach>
          </select>
        </div>

        <!-- Lote -->
        <div class="col-sm-3">
          <label class="form-label">Lote</label>
          <select class="form-select" name="lote" id="lote">
            <option value="">Seleccione…</option>
            <% if (lotes != null) for (String l : lotes) { %>
              <option value="<%=l%>">Lote <%=l%></option>
            <% } %>
          </select>
        </div>

        <!-- Número de casa -->
        <div class="col-sm-3">
          <label class="form-label">Número de casa</label>
          <select class="form-select" name="numeroCasa" id="numeroCasa">
            <option value="">Seleccione…</option>
            <% if (casas != null) for (String c : casas) {
                 int n = 0;
                 try { n = Integer.parseInt(c); } catch(Exception ignore) {}
            %>
              <option value="<%=c%>">Casa <%= (n > 0 ? n : c) %></option>
            <% } %>
          </select>
        </div>

        <div class="col-md-3 d-flex align-items-end">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" name="activo" id="activo" ${u.activo ? "checked" : ""}>
            <label class="form-check-label" for="activo">Activo</label>
          </div>
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <!-- Botón Limpiar formulario -->
        <a href="#" class="btn btn-outline-secondary"
           onclick="document.querySelector('form').reset(); return false;">
          <i class="bi bi-eraser me-1"></i>Limpiar
        </a>
        <button class="btn btn-brand" type="submit"><i class="bi bi-save me-1"></i>Guardar</button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/usuarios"><i class="bi bi-arrow-left me-1"></i>Cancelar</a>
      </div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>

  //  - Guardia  (2): lote/numeroCasa DESHABILITADOS y se limpian
  (function () {
    const rolSel = document.getElementById('rolId');
    const lote = document.getElementById('lote');
    const numeroCasa = document.getElementById('numeroCasa');

    function applyRules() {
      const rol = parseInt(rolSel.value || "0", 10);
      const isResidente = (rol === 3);
      const isGuardia   = (rol === 2);

      // requeridos solo para residente
      lote.required = isResidente;
      numeroCasa.required = isResidente;

      // deshabilitar si es guardia
      lote.disabled = isGuardia;
      numeroCasa.disabled = isGuardia;

      // si se deshabilitan, limpiar valores
      if (isGuardia) {
        lote.value = "";
        numeroCasa.value = "";
      }
    }
    if (rolSel) {
      rolSel.addEventListener('change', applyRules);
      applyRules(); // inicial
    }
  })();
</script>
</body>
</html>
