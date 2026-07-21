package com.minimart.controller;

public class Sesion {

    private static String rol = "usuario";

    public static String getRol()          { return rol; }
    public static void   setRol(String r)  { rol = r; }
    public static boolean esInvitado()     { return "invitado".equals(rol); }

}
