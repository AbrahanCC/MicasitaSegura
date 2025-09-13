<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
String ctx   = request.getContextPath();
String uname = (String) session.getAttribute("uname");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Mi Casita Segura • Mi QR</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
  <style>
    .qr-box{width:240px;height:240px;border:2px dashed rgba(0,0,0,.15);border-radius:20px;
            display:flex;align-items:center;justify-content:center;margin:0 auto;background:#fff;}
  </style>
</head>
<body>

<%@ include file="/view/_menu.jsp" %>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:780px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-qr-code"></i></div>
      <div>
        <h4 class="mb-0">Hola <%= (uname != null ? uname : "") %></h4>
        <small class="text-muted">este es tu código QR</small>
      </div>
    </div>

    <div class="text-center">
      <div class="qr-box mb-3">
        <!-- PNG generado por /qr con el mismo token que el email -->
        <img src="<%=ctx%>/qr?op=me" class="img-fluid" alt="Mi QR">
      </div>

      <a class="btn btn-brand" href="<%=ctx%>/qr?op=me&download=1">
        <i class="bi bi-download me-1"></i>Descargar
      </a>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
