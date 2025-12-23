package com.fgtit.data;

import android.util.Base64;

public class Conversions {
	
	private static Conversions mCom=null;
	
	public static Conversions getInstance(){
		if(mCom==null){
			mCom=new Conversions();
		}
		return mCom;
	}
	
	public native int StdToIso(int itype,byte[] input,byte[] output);
	public native int IsoToStd(int itype,byte[] input,byte[] output);
	public native int GetDataType(byte[] input);
	public native int StdChangeCoord(byte[] input,int size,byte[] output,int dk);
	
	private String IsoChangeCoord(byte[] input, int dk){
		int dt=GetDataType(input);
		if(dt==3){
			byte output[] =new byte[512];
			byte stddat[]=new byte[512];
			byte crddat[]=new byte[512];
			IsoToStd(2,input,stddat);
			StdChangeCoord(stddat,256,crddat,dk);
			StdToIso(2,crddat,output);
			return Base64.encodeToString(output,0,378, Base64.DEFAULT);
		}
		return "";
	}
	
	private String IsoChangeOrientation(byte[] input, int dk){
		int dt=GetDataType(input);
		if(dt==3){
			byte output[] =new byte[512];
			byte stddat[]=new byte[512];
			byte crddat[]=new byte[512];
			IsoToStd(2,input,stddat);
			StdChangeCoord(stddat,256,crddat,dk);
			StdToIso(2,crddat,output);
			return Base64.encodeToString(output,0,378, Base64.DEFAULT);
		}
		return "";
	}
	
	public String ToIso(byte[] input, int dk){
		switch(GetDataType(input)){
		case 1:{
				byte mTmpData[]=new byte[512];
				byte mIsoData[]=new byte[512];
				StdChangeCoord(input, 256, mTmpData, dk);
				StdToIso(2,mTmpData,mIsoData);        					
				return Base64.encodeToString(mIsoData,0,378, Base64.DEFAULT);
			}
		case 2:{
				byte mTmpData1[]=new byte[512];
				byte mTmpData2[]=new byte[512];
				byte mIsoData[]=new byte[512];
				IsoToStd(1,input,mTmpData1);
				StdChangeCoord(mTmpData1, 256, mTmpData2, dk);
				StdToIso(2,mTmpData2,mIsoData);
				return Base64.encodeToString(mIsoData,0,378, Base64.DEFAULT);
			}
		case 3:
			return IsoChangeOrientation(input,dk);
		}
		return "";
	}
	
	public String ToStd(byte[] input, int dk){
		switch(GetDataType(input)){
		case 1:{
				byte mTmpData[]=new byte[512];
				StdChangeCoord(input, 256, mTmpData, dk);
				return Base64.encodeToString(mTmpData,0,256, Base64.DEFAULT);
			}
		case 2:{
				byte mTmpData1[]=new byte[512];
				byte mTmpData2[]=new byte[512];
				IsoToStd(1,input,mTmpData1);
				StdChangeCoord(mTmpData1, 256, mTmpData2, dk);
				return Base64.encodeToString(mTmpData2,0,256, Base64.DEFAULT);
			}
		case 3:{
				byte mTmpData1[]=new byte[512];
				byte mTmpData2[]=new byte[512];
				IsoToStd(2,input,mTmpData1);
				StdChangeCoord(mTmpData1, 256, mTmpData2, dk);
				return Base64.encodeToString(mTmpData2,0,256, Base64.DEFAULT);
			}
		}
		return "";
	}
	
	static {
		System.loadLibrary("conversions");
	}
}
