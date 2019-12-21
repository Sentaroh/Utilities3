package com.sentaroh.android.Utilities3;

/*
下記サイトからのコピー
https://www.websec-room.com/2013/02/27/238
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SafePassword {

    private static int STRETCH_COUNT = 1000;

    /*
     * salt＋ハッシュ化したパスワードを取得
     */
    public static String getSaltedPassword(String password, String userId) {
        String salt = getSha256(userId);
        return getSha256(salt + password);
    }

    /*
     * salt + ストレッチングしたパスワードを取得(推奨)
     */
    public static String getStretchedPassword(String password, String userId) {
        String salt = getSha256(userId);
        String hash = "";

        for (int i = 0; i < STRETCH_COUNT; i++) {
            hash = getSha256(hash + salt + password);
        }

        return hash;
    }

    /*
     * 文字列から SHA256 のハッシュ値を取得
     */
    private static String getSha256(String target) {
        MessageDigest md = null;
        StringBuffer buf = new StringBuffer();
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(target.getBytes());
            byte[] digest = md.digest();

            for (int i = 0; i < digest.length; i++) {
                buf.append(String.format("%02x", digest[i]));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return buf.toString();
    }
}