<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Usuario,java.util.List,model.Rol"%>
<%
  // Datos del form
  Usuario u = (Usuario)request.getAttribute("u");
  boolean edit = (u!=null && u.getId()>0);
  List<Rol> roles = (List<Rol>)request.getAttribute("roles");   // RN4
  List<String> lotes = (List<String>)request.getAttribute("lotes"); // RN1
  List<String> casas = (List<String>)request.getAttribute("casas"); // RN1
  String err = (String)request.getAttribute("error");
  String ok  = (String)request.getAttribute("ok");
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
<%@ include file="/view/_menu.jsp" %>

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

    <% if (err != null) { %><div class="alert alert-danger"><%= err %></div><% } %>
    <% if ("user_created".equals(ok)) { %><div class="alert alert-success">Usuario creado correctamente.</div><% } %>

    <form id="frmUsuario" method="post" action="<%=ctx%>/usuarios" novalidate>
      <input type="hidden" name="id" value="<%=edit?u.getId():""%>">

      <div class="row g-3">
        <div class="col-sm-6">
          <label class="form-label">DPI del residente<small class="text-muted"> Solo numeros</small></label>
          <input class="form-control" name="dpi" id="dpi" required pattern="[0-9]{4,25}"
                 oninput="this.value=this.value.replace(/[^0-9]/g,'');"
                 value="<%=edit && u.getDpi()!=null ? u.getDpi() : ""%>">
        </div>

        <div class="col-sm-3">
          <label class="form-label">Lote</label>
          <select name="lote" id="lote" class="form-select" required>
            <option value="">Seleccione…</option>
            <% if (lotes!=null) for(String l : lotes){ %>
              <option value="<%=l%>" <%= edit && u.getLote()!=null && u.getLote().equals(l) ? "selected":"" %>><%=l%></option>
            <% } %>
          </select>
        </div>

        <div class="col-sm-3">
          <label class="form-label">Número de casa</label>
          <select name="numeroCasa" id="numeroCasa" class="form-select" required>
            <option value="">Seleccione…</option>
            <% if (casas!=null) for(String c : casas){ %>
              <option value="<%=c%>" <%= edit && u.getNumeroCasa()!=null && u.getNumeroCasa().equals(c) ? "selected":"" %>><%=c%></option>
            <% } %>
          </select>
        </div>

        <div class="col-sm-6">
          <label class="form-label">Nombre<small class="text-muted"> Solo letras</small></label>
          <input class="form-control" name="nombre" id="nombre" required
                 oninput="this.value=this.value.replace(/[^a-zA-ZÁÉÍÓÚáéíóúÑñ\s]/g,'');"
                 value="<%=edit && u.getNombre()!=null ? u.getNombre() : ""%>">
        </div>

        <div class="col-sm-6">
          <label class="form-label">Apellidos<small class="text-muted"> Solo letras</small></label>
          <input class="form-control" name="apellidos" id="apellidos" required
                 oninput="this.value=this.value.replace(/[^a-zA-ZÁÉÍÓÚáéíóúÑñ\s]/g,'');"
                 value="<%=edit && u.getApellidos()!=null ? u.getApellidos() : ""%>">
        </div>

        <div class="col-sm-6">
          <label class="form-label">Correo<small class="text-muted"> MiCorreo@gmail.com</small></label>
          <input type="email" class="form-control" name="correo" id="correo" required
                 pattern="[a-zA-Z0-9._%+-]+@gmail\.com"
                 title="El correo debe ser una dirección de Gmail (ejemplo@gmail.com)"
                 value="<%=edit && u.getCorreo()!=null ? u.getCorreo() : ""%>">
        </div>

        <div class="col-sm-6">
          <label class="form-label">Usuario</label>
          <input class="form-control" name="username" id="username" required
                 value="<%=edit && u.getUsername()!=null ? u.getUsername() : ""%>">
        </div>

        <div class="col-sm-6 position-relative">
          <label class="form-label">Contraseña <small class="text-muted">Ingrese su contraseña</small></label>
          <input type="password" class="form-control" name="pass" id="pass" <%= edit ? "" : "required" %>>
        </div>

        <div class="col-sm-6">
          <label class="form-label">Rol del usuario</label>
          <select name="rolId" id="rolId" class="form-select" required>
            <% if (roles!=null) for(Rol r : roles){ 
                 String nombreRol = r.getNombre();
                 String mostrar = nombreRol;
                 if ("ADMIN".equalsIgnoreCase(nombreRol)) mostrar = "Administrador de residencial";
                 else if ("GUARDIA".equalsIgnoreCase(nombreRol)) mostrar = "Agente de seguridad de residencial";
                 else if ("RESIDENTE".equalsIgnoreCase(nombreRol)) mostrar = "Residente";
            %>
              <option value="<%=r.getId()%>" <%= edit && u.getRolId()==r.getId() ? "selected":"" %>><%=mostrar%></option>
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
        <button type="submit" id="btnGuardar" class="btn btn-brand">Guardar</button>
        <button type="reset" class="btn btn-outline-secondary">Limpiar</button>
        <a href="<%=ctx%>/usuarios" class="btn btn-outline-secondary">Cancelar</a>
      </div>
    </form>
  </div>
</div>

<script>
  (function () {
    const rol = document.getElementById('rolId');
    const lote = document.getElementById('lote');
    const num  = document.getElementById('numeroCasa');
    const pass = document.getElementById('pass');
    const form = document.getElementById('frmUsuario');
    const edit = <%= edit ? "true" : "false" %>;

    function applyRN1() {
      const esGuardia = rol.value === '3';
      lote.disabled = esGuardia; num.disabled = esGuardia;
      lote.required = !esGuardia; num.required = !esGuardia;
      if (esGuardia) { lote.value = ""; num.value = ""; }
    }

    function validate() {
      if (!edit) pass.required = true;
    }

    rol.addEventListener('change', applyRN1);
    form.addEventListener('input', validate);
    applyRN1(); validate();
  })();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
