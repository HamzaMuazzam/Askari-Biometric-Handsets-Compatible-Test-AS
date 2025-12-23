package android.fpi;


import android.util.Log;


public class MtGpio {
	
	private boolean mOpen = false;
	public MtGpio() {
		mOpen = openDev()>=0?true:false;
		Log.d("MtGpio","openDev->ret:"+mOpen);
	}
	
	public boolean isOpen(){
		return mOpen;
	}
	
	public void sGpioDir(int pin, int dir){
		setGpioDir(pin,dir);
	}
	
	public void sGpioOut(int pin, int out){
		setGpioOut(pin,out);
	}
	
	public int getGpioPinState(int pin){
		return getGpioIn(pin);
	}
	
	public void sGpioMode(int pin, int mode){
		setGpioMode(pin, mode);
	}
	
	// JNI
	public native int openDev();
	public native void closeDev();
	public native int setGpioMode(int pin, int mode);
	public native int setGpioDir(int pin, int dir);
	public native int setGpioPullEnable(int pin, int enable);
	public native int setGpioPullSelect(int pin, int select);
	public native  int setGpioOut(int pin, int out);
	public native int getGpioIn(int pin); 
	
	static {
		System.loadLibrary("mtgpio");
	}
}
