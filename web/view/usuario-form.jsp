<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Usuario,java.util.List,model.Rol"%>
<%
  Usuario u = (Usuario)request.getAttribute("u");
  boolean edit = (u!=null && u.getId()>0);
  List<Rol> roles = (List<Rol>)request.getAttribute("roles");
  String err = (String)request.getAttribute("error");
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title><%=edit?"Editar":"Nuevo"%> Usuario</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<div class="container py-4 d-flex justify-content-center" style="min-height:100vh;">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:780px;">
    <div class="d-flex align-items-center mb-3">
      <div class="brand-badge me-3"><i class="bi bi-person-gear"></i></div>
      <div>
        <h4 class="mb-0"><%=edit?"Editar":"Nuevo"%> Usuario</h4>
        <small class="text-muted">Complete los campos requeridos</small>
      </div>
      <div class="ms-auto">
        <a href="<%=ctx%>/usuarios" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Volver</a>
      </div>
    </div>

    <% if (err != null) { %>
      <div class="alert alert-danger"><%= err %></div>
    <% } %>

    <form id="frmUsuario" method="post" action="<%=ctx%>/usuarios" novalidate>
      <input type="hidden" name="id" value="<%=edit?u.getId():""%>">

      <div class="row g-3">
        <div class="col-sm-6">
          <label class="form-label">DPI</label>
          <input class="form-control" name="dpi" id="dpi" required pattern="[0-9]{4,15}"
                 title="Solo dígitos (4-15)"
                 value="<%=edit && u.getDpi()!=null ? u.getDpi() : ""%>">
        </div>
        <div class="col-sm-6">
          <label class="form-label">Número de casa</label>
          <input class="form-control" name="numeroCasa" id="numeroCasa" pattern="[A-Za-z0-9\\-]{0,20}"
                 title="Máx. 20 caracteres alfanuméricos"
                 value="<%=edit && u.getNumeroCasa()!=null ? u.getNumeroCasa() : ""%>">
        </div>

        <div class="col-sm-6">
          <label class="form-label">Nombre</label>
          <input class="form-control" name="nombre" id="nombre" required
                 value="<%=edit && u.getNombre()!=null ? u.getNombre() : ""%>">
        </div>
        <div class="col-sm-6">
          <label class="form-label">Apellidos</label>
          <input class="form-control" name="apellidos" id="apellidos" required
                 value="<%=edit && u.getApellidos()!=null ? u.getApellidos() : ""%>">
        </div>

        <div class="col-sm-6">
          <label class="form-label">Correo</label>
          <input type="email" class="form-control" name="correo" id="correo" required
                 value="<%=edit && u.getCorreo()!=null ? u.getCorreo() : ""%>">
        </div>
        <div class="col-sm-6">
          <label class="form-label">Usuario</label>
          <input class="form-control" name="username" id="username" required
                 value="<%=edit && u.getUsername()!=null ? u.getUsername() : ""%>">
        </div>

        <div class="col-sm-6 position-relative">
          <label class="form-label">Contraseña <small class="text-muted">(vacío para no cambiar)</small></label>
          <input type="password" class="form-control" name="pass" id="pass">
          <i class="bi bi-eye toggle-pass" id="togglePass" style="position:absolute;right:.75rem;top:58%;cursor:pointer;"></i>
        </div>

        <div class="col-sm-6">
          <label class="form-label">Rol</label>
          <select name="rolId" class="form-select" required>
            <% if (roles!=null) for(Rol r : roles){ %>
              <option value="<%=r.getId()%>" <%= edit && u.getRolId()==r.getId() ? "selected":"" %>>
                <%=r.getNombre()%>
              </option>
            <% } %>
          </select>
        </div>

        <div class="col-12">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" name="activo" id="activo"
                   <%= edit ? (u.isActivo() ? "checked" : "") : "checked" %>>
            <label class="form-check-label" for="activo">Activo</label>
          </div>
        </div>
      </div>

      <div class="d-flex gap-2 mt-4">
        <button type="submit" id="btnGuardar" class="btn btn-brand" disabled>Guardar</button>
        <a href="<%=ctx%>/usuarios" class="btn btn-outline-secondary">Cancelar</a>
      </div>
    </form>
  </div>
</div>

<script>
  (function () {
    var edit = <%= edit ? "true" : "false" %>;
    var f = document.getElementById('frmUsuario');
    var btn = document.getElementById('btnGuardar');
    var pass = document.getElementById('pass');
    var toggle = document.getElementById('togglePass');

    function okPass() { return edit ? true : (pass.value && pass.value.trim().length > 0); }
    function validate() { btn.disabled = !(f.checkValidity() && okPass()); }

    f.addEventListener('input', validate);
    f.addEventListener('change', validate);
    validate();

    if (toggle) toggle.addEventListener('click', function () {
      var show = pass.type === 'password';
      pass.type = show ? 'text' : 'password';
      toggle.classList.toggle('bi-eye-slash', show);
      toggle.classList.toggle('bi-eye', !show);
    });
  })();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
