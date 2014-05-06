package org.jboss.wsf.test;

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
        System.out.println("Bouncy Castle JCE Provider:            " + ((java.security.Security.getProvider("BC") == null)?"NOT ":"") + "INSTALLED" );
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }

}

