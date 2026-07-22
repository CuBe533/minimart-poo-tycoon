package com.minimart.controller;

public class Sesion {

    private static String rol = "usuario";
    private static int    usuarioId = -1;
    private static String nombreUsuario = "";

    public static String getRol()                  { return rol; }
    public static void   setRol(String r)          { rol = r; }
    public static boolean esInvitado()             { return "invitado".equals(rol); }
    public static boolean esAdmin()                { return "admin".equals(rol); }
    public static boolean esEstandar()             { return "estandar".equals(rol); }

    public static int    getUsuarioId()            { return usuarioId; }
    public static void   setUsuarioId(int id)      { usuarioId = id; }

    public static String getNombreUsuario()                { return nombreUsuario; }
    public static void   setNombreUsuario(String nombre)   { nombreUsuario = nombre; }
}
