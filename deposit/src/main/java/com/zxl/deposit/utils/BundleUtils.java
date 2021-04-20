package com.zxl.deposit.utils;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;

import com.zxl.deposit.IDeposit;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author long117long@126.com <br/>
 * @date 2019/12/24 <br/>
 */
public class BundleUtils {

    static ArrayList<Integer> integerArrayList = new ArrayList<>(0);
    static ArrayList<Parcelable> parcelableArrayList = new ArrayList<>(0);
    static ArrayList<String> stringArrayList = new ArrayList<>(0);

    /**
     * 根据参数类型，将参数放到bundle中
     *
     * @param bundle
     * @param key
     * @param obj
     */
    public static void putIntoBundle(Bundle bundle, String key, Object obj) {
        Class objClass = obj.getClass();

        //基本类型
        if (objClass.equals(Integer.class)) {
            bundle.putInt(key, (int) obj);
        } else if (objClass.equals(Boolean.class)) {
            bundle.putBoolean(key, (boolean) obj);
        } else if (objClass.equals(Short.class)) {
            bundle.putShort(key, (short) obj);
        } else if (objClass.equals(Byte.class)) {
            bundle.putByte(key, (byte) obj);
        } else if (objClass.equals(Long.class)) {
            bundle.putLong(key, (long) obj);
        } else if (objClass.equals(Character.class)) {
            bundle.putChar(key, (char) obj);
        } else if (objClass.equals(Double.class)) {
            bundle.putDouble(key, (double) obj);
        } else if (objClass.equals(Float.class)) {
            bundle.putFloat(key, (float) obj);
        } else if (objClass.equals(String.class)) {
            bundle.putString(key, (String) obj);
        }

        //基本类型的数组类型
        else if (objClass.equals(int[].class)) {
            bundle.putIntArray(key, (int[]) obj);
        } else if (objClass.equals(boolean[].class)) {
            bundle.putBooleanArray(key, (boolean[]) obj);
        } else if (objClass.equals(byte[].class)) {
            bundle.putByteArray(key, (byte[]) obj);
        } else if (objClass.equals(char[].class)) {
            bundle.putCharArray(key, (char[]) obj);
        } else if (objClass.equals(double[].class)) {
            bundle.putDoubleArray(key, (double[]) obj);
        } else if (objClass.equals(float[].class)) {
            bundle.putFloatArray(key, (float[]) obj);
        } else if (objClass.equals(long[].class)) {
            bundle.putLongArray(key, (long[]) obj);
        } else if (objClass.equals(short[].class)) {
            bundle.putShortArray(key, (short[]) obj);
        } else if (objClass.equals(String[].class)) {
            bundle.putStringArray(key, (String[]) obj);
        } else if (objClass.equals(Bundle.class)) {
            bundle.putBundle(key, (Bundle) obj);
        } else if (obj instanceof Parcelable) {
            bundle.putParcelable(key, (Parcelable) obj);
        } else if (objClass.equals(Parcelable[].class)) {
            bundle.putParcelableArray(key, (Parcelable[]) obj);
        } else if (obj instanceof Serializable) {
            bundle.putSerializable(key, (Serializable) obj);
        } else if (objClass.equals(Binder.class)) {
            bundle.putBinder(key, (Binder) obj);
        } else if (obj instanceof IBinder) {
            bundle.putBinder(key, (IBinder) obj);
        } else if (obj instanceof IDeposit) {
            bundle.putBinder(key, ((IDeposit) obj).asBinder());
        } else if (objClass.equals(integerArrayList.getClass())) {
            bundle.putIntegerArrayList(key, (ArrayList<Integer>) obj);
        } else if (objClass.equals(stringArrayList.getClass())) {
            bundle.putStringArrayList(key, (ArrayList<String>) obj);
        } else if (objClass.equals(parcelableArrayList.getClass())) {
            bundle.putParcelableArrayList(key, (ArrayList<Parcelable>) obj);
        } else {
            throw new RuntimeException("Not support " + obj.getClass() + " put into Bundle!");
        }
    }
}
