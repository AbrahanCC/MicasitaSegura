<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  String ctx = request.getContextPath();
  // catálogos desde el controller (A..Z y 1..50)
  java.util.List<String> lotes = (java.util.List<String>) request.getAttribute("lotes");
  java.util.List<String> casas = (java.util.List<String>) request.getAttribute("casas");
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
  <style>
    .hint { font-size:.85rem; color:#6c757d; }
    .err  { display:none; font-size:.85rem; color:#b00020; }
    .is-invalid { border-color:#b00020 !important; }
  </style>
</head>
<body>
<jsp:include page="/view/_menu.jsp"/>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:880px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-person-lines-fill"></i></div>
      <div>
        <h4 class="mb-0"><c:choose><c:when test="${u.id > 0}">Editar usuario</c:when><c:otherwise>Nuevo usuario</c:otherwise></c:choose></h4>
        <small class="text-muted">Complete los datos obligatorios</small>
      </div>
    </div>

    <!-- FA3 + mensajes backend -->
    <c:if test="${not empty error}">
      <div class="alert alert-danger" id="srvError">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
      <div class="alert alert-success">${success}</div>
    </c:if>

    <!-- acción guardar -->
    <!-- novalidate: evita validación nativa; validamos en JS al presionar Guardar -->
    <form id="frmUsuario" method="post" action="${pageContext.request.contextPath}/usuarios" novalidate>
      <input type="hidden" name="id" value="${u.id}"/>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label">DPI del residente *</label>
          <!-- RN2: DPI 13 dígitos -->
          <input class="form-control" name="dpi" id="dpi" value="${u.dpi}" pattern="\\d{13}">
          <div class="hint">13 dígitos</div>
          <div class="err" id="dpiErr">El DPI debe tener 13 dígitos.</div>
        </div>

        <div class="col-md-6">
          <label class="form-label">Nombre *</label>
          <input class="form-control" name="nombre" id="nombre" value="${u.nombre}">
          <div class="err" id="nombreErr">Campo obligatorio.</div>
        </div>

        <div class="col-md-6">
          <label class="form-label">Apellidos *</label>
          <input class="form-control" name="apellidos" id="apellidos" value="${u.apellidos}">
          <div class="err" id="apellidosErr">Campo obligatorio.</div>
        </div>

        <div class="col-md-6">
          <label class="form-label">Correo *</label>
          <input class="form-control" type="email" name="correo" id="correo" value="${u.correo}">
          <div class="err" id="correoErr">Formato de correo inválido.</div>
          <!-- FA3: duplicado desde backend -->
          <div class="err" id="dupErr" style="<c:if test='${not empty dupError}'>display:block</c:if>"><c:out value='${dupError}'/></div>
        </div>

        <div class="col-md-6">
          <label class="form-label">Usuario *</label>
          <input class="form-control" name="username" id="username" value="${u.username}">
          <div class="err" id="userErr">Campo obligatorio.</div>
        </div>

        <div class="col-md-6">
          <label class="form-label">Contraseña<c:if test="${u.id <= 0}">*</c:if></label>
          <!-- RN2: requerida si es nuevo; en edición opcional -->
          <input class="form-control" type="password" name="pass" id="pass"
                 placeholder="<c:choose><c:when test='${u.id > 0}'>Dejar en blanco para conservar</c:when><c:otherwise>••••••••</c:otherwise></c:choose>">
          <div class="err" id="passErr">Campo obligatorio.</div>
        </div>

        <div class="col-md-6">
          <label class="form-label">Rol del usuario *</label>
          <!-- RN4: catálogo roles -->
          <select class="form-select" name="rolId" id="rolId">
            <option value="">Seleccione…</option>
            <c:forEach var="r" items="${roles}">
              <option value="${r.id}" ${r.id == u.rolId ? "selected" : ""}>${r.nombre}</option>
            </c:forEach>
          </select>
          <div class="err" id="rolErr">Seleccione un rol.</div>
        </div>

        <!-- RN1: Lote (habilitar solo si NO es guardia) -->
        <div class="col-sm-3">
          <label class="form-label">Lote *</label>
          <select class="form-select" name="lote" id="lote">
            <option value="">Seleccione…</option>
            <% if (lotes != null) for (String l : lotes) { %>
              <option value="<%=l%>" <%= (request.getAttribute("u")!=null && l.equals(String.valueOf(((model.Usuario)request.getAttribute("u")).getLote())) ? "selected" : "") %>>Lote <%=l%></option>
            <% } %>
          </select>
          <div class="err" id="loteErr">Seleccione un lote.</div>
        </div>

        <!-- RN1: Número de casa (habilitar solo si NO es guardia) -->
        <div class="col-sm-3">
          <label class="form-label">Número de casa *</label>
          <select class="form-select" name="numeroCasa" id="numeroCasa">
            <option value="">Seleccione…</option>
            <% if (casas != null) for (String c : casas) {
                 int n = 0; try { n = Integer.parseInt(c); } catch(Exception ignore) {}
            %>
              <option value="<%=c%>" <%= (request.getAttribute("u")!=null && c.equals(String.valueOf(((model.Usuario)request.getAttribute("u")).getNumeroCasa())) ? "selected" : "") %>>
                Casa <%= (n > 0 ? n : c) %>
              </option>
            <% } %>
          </select>
          <div class="err" id="casaErr">Seleccione un número.</div>
        </div>

        <div class="col-md-3 d-flex align-items-end">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" name="activo" id="activo" ${u.activo ? "checked" : ""}>
            <label class="form-check-label" for="activo">Activo</label>
          </div>
        </div>
      </div>

      <!-- FA5 / Cancelar -->
      <div class="mt-4 d-flex gap-2">
        <!-- botón guardar habilitado desde el inicio -->
        <button class="btn btn-brand" type="submit" id="btnGuardar"><i class="bi bi-save me-1"></i>Guardar</button>
        <button class="btn btn-outline-secondary" type="button" id="btnLimpiar"><i class="bi bi-eraser me-1"></i>Limpiar</button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/usuarios"><i class="bi bi-arrow-left me-1"></i>Cancelar</a>
      </div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  // RN1: si es guardia deshabilita campos (silencioso)
  // RN2: validar SOLO al presionar Guardar; luego revalidar mientras escribes
  (function () {
    const form       = document.getElementById('frmUsuario');
    const rolSel     = document.getElementById('rolId');
    const lote       = document.getElementById('lote');
    const casa       = document.getElementById('numeroCasa');
    const btnGuardar = document.getElementById('btnGuardar');

    const dpi   = document.getElementById('dpi');
    const nom   = document.getElementById('nombre');
    const ape   = document.getElementById('apellidos');
    const mail  = document.getElementById('correo');
    const user  = document.getElementById('username');
    const pass  = document.getElementById('pass');

    const GUARDIA_ID = 2;
    let hasTriedSubmit = false; // validar al guardar

    function isGuardia() {
      const id = parseInt(rolSel.value || "0", 10);
      return id === GUARDIA_ID;
    }

    function toggleResidencial() {
      const habilitar = !isGuardia(); // si es guardia deshabilita campos
      lote.disabled = !habilitar;
      casa.disabled = !habilitar;
      if (!habilitar) { lote.value = ""; casa.value = ""; }
    }

    function showErr(el, id, show) {
      const e = document.getElementById(id);
      if (!e) return;
      if (show) { e.style.display = 'block'; el && el.classList && el.classList.add('is-invalid'); }
      else { e.style.display = 'none'; el && el.classList && el.classList.remove('is-invalid'); }
    }

    function vDPI() {
      const ok = /^\d{13}$/.test((dpi.value || '').trim());
      showErr(dpi, 'dpiErr', !ok);
      return ok;
    }
    function vReq(el, errId) {
      const ok = (el.value || '').trim().length > 0;
      showErr(el, errId, !ok);
      return ok;
    }
    function vMail() {
      const v = (mail.value || '').trim();
      const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
      showErr(mail, 'correoErr', !ok);
      return ok;
    }
    function vRol() {
      const ok = (rolSel.value || '').trim() !== "";
      showErr(rolSel, 'rolErr', !ok);
      return ok;
    }
    function vResidencial() {
      if (isGuardia()) return true; // no aplica
      const okL = (lote.value || '').trim() !== "";
      const okC = (casa.value || '').trim() !== "";
      showErr(lote, 'loteErr', !okL);
      showErr(casa, 'casaErr', !okC);
      return okL && okC;
    }

    function validar() {
      // contraseña requerida solo si es nuevo (id vacío/0)
      const isNuevo = !('<c:out value="${u.id}"/>') || parseInt('<c:out value="${u.id}"/>', 10) <= 0;
      const passOk = isNuevo ? vReq(pass, 'passErr') : true;

      const ok = vDPI() && vReq(nom,'nombreErr') && vReq(ape,'apellidosErr')
              && vMail() && vRol() && passOk && vResidencial();
      return ok;
    }

    // listeners: sólo actúan después del primer submit
    [dpi, nom, ape, mail, user, pass, rolSel, lote, casa].forEach(el=>{
      el.addEventListener('input', ()=>{ if (hasTriedSubmit) validar(); });
      el.addEventListener('change', ()=>{ if (el===rolSel) toggleResidencial(); if (hasTriedSubmit) validar(); });
      el.addEventListener('blur',   ()=>{ if (hasTriedSubmit) validar(); });
    });

    // limpiar (FA5): vuelve a estado inicial (sin advertencias)
    document.getElementById('btnLimpiar').addEventListener('click', ()=>{
      form.reset();
      hasTriedSubmit = false; // olvidar intento previo
      document.querySelectorAll('.err').forEach(e=> e.style.display='none');
      document.querySelectorAll('.is-invalid').forEach(e=> e.classList.remove('is-invalid'));
      toggleResidencial();
      // botón guardar permanece habilitado
    });

    // submit: primera vez marca errores si faltan; si ok, envía
    form.addEventListener('submit', (e)=>{
      if (!hasTriedSubmit) hasTriedSubmit = true; // desde aquí sí mostramos errores
      if (!validar()) { e.preventDefault(); e.stopPropagation(); }
    });

    // estado inicial: rn1 silencioso, sin validar
    toggleResidencial();
    // no llamamos validar() aquí para no mostrar advertencias al abrir
  })();
</script>
</body>
</html>
