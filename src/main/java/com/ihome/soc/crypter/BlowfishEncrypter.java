/*
 * ihome inc.
 * soc
 */
package com.ihome.soc.crypter;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Blowfish加解密的方法
 * @author sihai
 *
 */
public class BlowfishEncrypter {
	
	private static final Log logger = LogFactory.getLog(BlowfishEncrypter.class);
	
	public static String ALGORITHM_NAME_BLOWFISH = "Blowfish";
	public static String DEFAULT_CHARSET = "utf-8";
	
	private static String          CIPHER_KEY    = "";
    private static String          CIPHER_NAME   = "Blowfish/CFB8/NoPadding";
    private static String          KEY_SPEC_NAME = "Blowfish";
    private static SecretKeySpec   secretKeySpec = null;
    private static IvParameterSpec ivParameterSpec = null;
    private static final ThreadLocal<BlowfishEncrypter> encrypter_pool = new ThreadLocal<BlowfishEncrypter>();
    
    // 
    private Cipher          enCipher;
    private Cipher          deCipher;
    
    public BlowfishEncrypter() {}

    /**
     * 初始化
     */
    public void init() {
    	try {
        	secretKeySpec = new SecretKeySpec(CIPHER_KEY.getBytes(), KEY_SPEC_NAME);
        	ivParameterSpec = new IvParameterSpec((DigestUtils.md5Hex(CIPHER_KEY)
                    .substring(0, 8))
                    .getBytes());
        	
            enCipher = Cipher.getInstance(CIPHER_NAME);
            deCipher = Cipher.getInstance(CIPHER_NAME);
            enCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            deCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        } catch (Exception e) {
            throw new RuntimeException("Init BlowfishEncrypter failed", e);
        }
    }
    
    public String getByteString(byte[] b) {
        StringBuffer s = new StringBuffer(b.length * 3);

        for (int i = 0; i < b.length; i++) {
            s.append("|" + Integer.toHexString(b[i] & 0xff).toUpperCase());
        }

        return s.toString();
    }

    public static BlowfishEncrypter getEncrypter() {
        BlowfishEncrypter encrypter = (BlowfishEncrypter) encrypter_pool.get();

        if (encrypter == null) {
            encrypter = new BlowfishEncrypter();
            encrypter.init();
            encrypter_pool.set(encrypter);
        }

        return encrypter;
    }

    /**
     * 加密的方法
     * @param str
     * @return
     */
    public String encrypt(String str) {
        String result = null;

        if (!StringUtils.isBlank(str)) {
            try {
                byte[] utf8 = str.getBytes();
                byte[] enc = enCipher.doFinal(utf8);

                result = new String(Base64.encodeBase64(enc)); 
            } catch (Exception ex) {
                logger.error("encrypt exception!", ex);
            }
        }

        return result;
    }

    /**
     * 解密的方法
     * @param str
     * @return
     */
    public String decrypt(String str) {
        String result = null;

        if (!StringUtils.isBlank(str)) {
            try {
                byte[] dec = Base64.decodeBase64(str.getBytes());
                result = new String(deCipher.doFinal(dec));
            } catch (Exception ex) {
                logger.warn("string to decrypt is:" + str
                          + " decrypt exception. cookie is reset to zero length String! ", ex);
                result = "";
            }
        }

        return result;
    }
    
    public byte[] decrypt(byte[] tar) {
        String src = this.decrypt(new String(tar));
        return src.getBytes();
    }

    public void destroy() {}

    public byte[] encrypt(byte[] src) {
        String tar = this.encrypt(new String(src));
        return tar.getBytes();
    }

    public String getAlgorithmName() {
        return ALGORITHM_NAME_BLOWFISH;
    }

    public String getCharset() {
        return DEFAULT_CHARSET;
    }

    public String getCipherName() {
        return CIPHER_NAME;
    }

    public String getKey() {
        return CIPHER_KEY;
    }

    public void setAlgorithmName(String algorithmName) {}

    public void setCharset(String charset) {}

    public void setCipherName(String cipherName) {}

    public static void setKey(String key) {
        CIPHER_KEY = key;
    }

    public boolean isUseIv() {
        return true;
    }

    public void setUseIv(boolean useIv) {}
}