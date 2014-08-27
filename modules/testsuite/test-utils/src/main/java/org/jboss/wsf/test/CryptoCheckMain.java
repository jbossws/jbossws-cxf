package org.jboss.wsf.test;

import java.security.Provider;

/**
 *
 * @author Rostislav Svoboda
 */
public class CryptoCheckMain
{

    public static void main(String[] args) throws Exception 
    {
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        System.out.println("JCE unlimited strength cryptography:   " + ((javax.crypto.Cipher.getMaxAllowedKeyLength("RC5") < 256)?"NOT ":"") + "INSTALLED" );
        Provider p = java.security.Security.getProvider("BC");
        if (p != null) {
           System.out.println("Bouncy Castle JCE Provider:            INSTALLED (ver: " + p.getVersion() + ", " +
              p.getClass().getProtectionDomain().getCodeSource().getLocation() + ")" );
        } else {
           System.out.println("Bouncy Castle JCE Provider:            NOT INSTALLED" );   
        }
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }

}

