package org.jboss.wsf.test;

import org.jboss.wsf.stack.cxf.client.configuration.SecurityProviderConfig;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * This class provides information about Bouncy Castle library and unlimited strength cryptography availability.
 * It needs to be in sync with actual JBossWS behaviour in {@link SecurityProviderConfig}
 *
 * @author Rostislav Svoboda
 * @author Alessio Soldano
 * @author Jan Bliznak
 */
public class CryptoCheckHelper
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        System.out.println("JCE unlimited strength cryptography:   "
              + ((!isUnlimitedStrengthCryptographyAvailable()) ? "NOT " : "") + "AVAILABLE");

        Provider p = getBCProviderFromInstalledSecurityProviders();
        if (p != null)
        {
            System.out.println("Bouncy Castle JCE Provider:            AVAILABLE - INSTALLED "
                  + "(ver: " + p.getVersion() + ", " +
                  p.getClass().getProtectionDomain().getCodeSource().getLocation() + ")");
        }
        else
        {
            p = getBCProviderFromClassPath();
            if (p != null)
            {
                System.out.println("Bouncy Castle JCE Provider:            AVAILABLE - ON CLASSPATH "
                      + "(ver: " + p.getVersion() + ", "
                      + p.getClass().getProtectionDomain().getCodeSource().getLocation() + ")");
            }
            else
            {
                System.out.println("Bouncy Castle JCE Provider:            NOT AVAILABLE");
            }
        }
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    }

    public static Provider getBCProviderFromInstalledSecurityProviders()
    {
        return java.security.Security.getProvider("BC");
    }

    public static Provider getBCProviderFromClassPath()
    {
        try
        {
            Class<?> clazz = SecurityProviderConfig.class
                  .getClassLoader()
                  .loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
            return (Provider) clazz.newInstance();
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public static boolean isBouncyCastleAvailable()
    {
        return (getBCProviderFromClassPath() != null) || getBCProviderFromInstalledSecurityProviders() != null;
    }

    public static boolean isUnlimitedStrengthCryptographyAvailable()
    {
        try
        {
            return (javax.crypto.Cipher.getMaxAllowedKeyLength("RC5") >= 256);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Exception checkAndWrapException(Exception e) throws Exception
    {
        if (!isBouncyCastleAvailable())
        {
            return new Exception("Bouncy Castle JCE provider does not seem to be properly installed; either install it " +
                  "or run the testsuite with -Dexclude-integration-tests-BC-related=true to exclude this test.", e);
        }
        else if (!isUnlimitedStrengthCryptographyAvailable())
        {
            return new Exception("JCE unlimited strength cryptography extension does not seem to be properly installed; " +
                  "either install it or run the testsuite with '-Dexclude-integration-tests-unlimited-strength-related=true'" +
                  " to exclude this test.", e);
        }
        else if (e.getCause() != null && e.getCause().getClass().getName().contains("SoapFault") &&
              e.getMessage() != null && e.getMessage().contains("algorithm"))
        {
            return new Exception("Please check for Bouncy Castle JCE provider and JCE unlimited strenght cryptography " +
                  "extension availability on server side.", e);
        }
        else
        {
            return e;
        }
    }
}

