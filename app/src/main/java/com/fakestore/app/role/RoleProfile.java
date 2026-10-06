package com.fakestore.app.role;

import android.content.Context;

import com.fakestore.app.session.SessionManager;

/**
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (respuesta a una duda del profesor):
 *
 * El profesor comentó que "la clase padre no era necesaria porque no la
 * mandábamos llamar" (es decir, nunca hacemos `new RoleProfile()` en
 * ningún lado). Tiene razón en la observación: efectivamente NUNCA se
 * instancia RoleProfile directamente, y de hecho ni se podría, porque es
 * "abstract" (Java no deja crear objetos de una clase abstracta).
 *
 * Pero justo ESO es el punto de tener una clase padre abstracta en POO:
 * no existe para que la instanciemos, existe para que sea el "molde" o
 * "contrato" común que TODAS las subclases (AdminRoleProfile,
 * AuditorRoleProfile, ClientRoleProfile) deben cumplir. Gracias a que
 * existe RoleProfile como tipo, podemos escribir código como:
 *
 *     RoleProfile profile = RoleProfile.from(role); // puede ser CUALQUIER subclase
 *     profile.getEmoji();                            // Java decide en tiempo de
 *                                                      // ejecución cuál versión usar
 *
 * Eso es POLIMORFISMO: una sola variable de tipo "RoleProfile" puede
 * apuntar a un Admin, un Auditor o un Cliente, y cada quien responde
 * getEmoji()/getLabel()/etc. a su manera, sin que ProfileActivity tenga
 * que preguntar "¿qué rol eres?" con un montón de if/else.
 *
 * Si quitáramos la clase padre, tendríamos que volver a usar un
 * if/else o un switch en ProfileActivity para decidir colores e
 * íconos según el rol (como estaba ANTES de este refactor). Es decir,
 * SÍ es necesaria: no porque la mandemos llamar con "new", sino porque
 * es el tipo común que hace posible el polimorfismo.
 *
 * Conclusión: decidimos CONSERVAR la clase padre abstracta.
 * ---------------------------------------------------------------------
 * NOTA DE CLASE (POO - Herencia / Abstracción):
 * Esta es una clase ABSTRACTA: no se puede instanciar directamente
 * (no existe "new RoleProfile()" en ningún lado del proyecto), solo
 * sirve como plantilla para sus hijas (AdminRoleProfile, etc.), que
 * extienden esta clase con "extends" y sobrescriben sus métodos con
 * "@Override".
 * ---------------------------------------------------------------------
 */
public abstract class RoleProfile {

    /**
     * Método abstracto: no tiene cuerpo aquí, cada subclase está
     * OBLIGADA a implementarlo a su manera. Esto es lo que permite el
     * polimorfismo (cada hija "responde" distinto a la misma pregunta).
     */
    public abstract String getEmoji();

    public abstract String getLabel();

    public abstract int getAccentColor(Context context);

    public abstract int getPillBackgroundColor(Context context);

    /**
     * Factory method (método fábrica): traduce el enum de rol a su
     * implementación concreta (AdminRoleProfile, AuditorRoleProfile o
     * ClientRoleProfile). Este es el ÚNICO lugar del proyecto con un
     * "switch" para decidir el rol; todo lo demás queda desacoplado
     * gracias al polimorfismo.
     */
    public static RoleProfile from(SessionManager.Role role) {
        switch (role) {
            case ADMIN:
                return new AdminRoleProfile();
            case AUDITOR:
                return new AuditorRoleProfile();
            case CLIENT:
            default:
                return new ClientRoleProfile();
        }
    }
}
