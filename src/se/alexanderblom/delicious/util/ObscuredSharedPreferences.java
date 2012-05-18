package se.alexanderblom.delicious.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;

/**
 * Warning, this gives a false sense of security.  If an attacker has enough access to
 * acquire your password store, then he almost certainly has enough access to acquire your
 * source binary and figure out your encryption key.  However, it will prevent casual
 * investigators from acquiring passwords, and thereby may prevent undesired negative
 * publicity.
 * 
 * Adapted from http://stackoverflow.com/questions/785973/what-is-the-most-appropriate-way-to-store-user-settings-in-android-application
 */
public class ObscuredSharedPreferences implements SharedPreferences {
    protected static final String UTF8 = "utf-8";
    
    private char[] secret;

    protected SharedPreferences preferences;
    protected Context context;

    public ObscuredSharedPreferences(Context context, SharedPreferences preference, char[] secret) {
        this.preferences = preference;
        this.context = context;
    }

    public class Editor implements SharedPreferences.Editor {
        protected SharedPreferences.Editor editor;

        public Editor() {
            this.editor = preferences.edit();                    
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            editor.putString(key, encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            editor.putString(key, encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            editor.putString(key, encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            editor.putString(key, encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public Editor putString(String key, String value) {
            editor.putString(key, encrypt(value));
            return this;
        }
        
        @Override
		public SharedPreferences.Editor putStringSet(String key, Set<String> value) {
        	throw new UnsupportedOperationException();
		}

        @Override
        public void apply() {
            editor.apply();
        }

        @Override
        public Editor clear() {
            editor.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return editor.commit();
        }

        @Override
        public Editor remove(String s) {
            editor.remove(s);
            return this;
        }
    }

    public Editor edit() {
        return new Editor();
    }


    @Override
    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException();
    }
    
	@Override
	public Set<String> getStringSet(String key, Set<String> defValues) {
		throw new UnsupportedOperationException();
	}

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        final String v = preferences.getString(key, null);
        return v!=null ? Boolean.parseBoolean(decrypt(v)) : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        final String v = preferences.getString(key, null);
        return v!=null ? Float.parseFloat(decrypt(v)) : defValue;
    }

    @Override
    public int getInt(String key, int defValue) {
        final String v = preferences.getString(key, null);
        return v!=null ? Integer.parseInt(decrypt(v)) : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        final String v = preferences.getString(key, null);
        return v!=null ? Long.parseLong(decrypt(v)) : defValue;
    }

    @Override
    public String getString(String key, String defValue) {
        final String v = preferences.getString(key, null);
        return v != null ? decrypt(v) : defValue;
    }

    @Override
    public boolean contains(String s) {
        return preferences.contains(s);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    protected String encrypt(String value) {
        try {
            final byte[] bytes = value!=null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(secret));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(getSalt(), 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP),UTF8);

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String decrypt(String value) {
        try {
            final byte[] bytes = value!=null ? Base64.decode(value,Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(secret));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(getSalt(), 20));
            return new String(pbeCipher.doFinal(bytes),UTF8);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private byte[] getSalt() throws UnsupportedEncodingException {
    	return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(UTF8);
    }
}